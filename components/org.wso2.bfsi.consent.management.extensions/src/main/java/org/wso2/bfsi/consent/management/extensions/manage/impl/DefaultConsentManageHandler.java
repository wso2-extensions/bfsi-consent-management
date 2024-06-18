/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.bfsi.consent.management.extensions.manage.impl;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.bfsi.consent.management.common.exceptions.ConsentManagementException;
import org.wso2.bfsi.consent.management.dao.models.ConsentResource;
import org.wso2.bfsi.consent.management.dao.models.DetailedConsentResource;
import org.wso2.bfsi.consent.management.extensions.common.ConsentException;
import org.wso2.bfsi.consent.management.extensions.common.ConsentExtensionConstants;
import org.wso2.bfsi.consent.management.extensions.common.ConsentExtensionUtils;
import org.wso2.bfsi.consent.management.extensions.common.ResponseStatus;
import org.wso2.bfsi.consent.management.extensions.internal.ConsentExtensionsDataHolder;
import org.wso2.bfsi.consent.management.extensions.manage.ConsentManageHandler;
import org.wso2.bfsi.consent.management.extensions.manage.ConsentManageValidator;
import org.wso2.bfsi.consent.management.extensions.manage.model.ConsentManageData;
import org.wso2.bfsi.consent.management.extensions.manage.model.ConsentPayloadValidationResult;
import org.wso2.bfsi.consent.management.extensions.manage.utils.ConsentManageConstants;


/**
 * Consent manage handler default implementation.
 */
public class DefaultConsentManageHandler implements ConsentManageHandler {

    private static final Log log = LogFactory.getLog(DefaultConsentManageHandler.class);
    @Override
    public void handleGet(ConsentManageData consentManageData) throws ConsentException {

        //Check whether client ID exists
        if (StringUtils.isEmpty(consentManageData.getClientId())) {
            log.error("Client ID missing in the request.");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Client ID missing in the request.");
        }

        if (consentManageData.getRequestPath() == null) {
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Resource Not Found");
        }
        String consentId = consentManageData.getRequestPath().split("/")[1];
        if (ConsentExtensionUtils.isConsentIdValid(consentId)) {
            try {
                ConsentResource consent = ConsentExtensionsDataHolder.getInstance().getConsentCoreService()
                        .getConsent(consentId, false);
                if (consent == null) {
                    throw new ConsentException(ResponseStatus.BAD_REQUEST, "Consent not found");
                }
                String consentType = ConsentExtensionUtils.getConsentType(consentManageData.getRequestPath());
                if (!consentType.equals(consent.getConsentType())) {
                    throw new ConsentException(ResponseStatus.BAD_REQUEST, "Consent Type mismatch");
                }
                // Check whether the client id is matching
                if (!consent.getClientID().equals(consentManageData.getClientId())) {
                    //Throwing same error as null scenario since client will not be able to identify if consent
                    // exists if consent does not belong to them
                    throw new ConsentException(ResponseStatus.BAD_REQUEST, "Client ID mismatch");
                }
                JSONObject receiptJSON = (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE)
                        .parse(consent.getReceipt());;
                consentManageData.setResponsePayload(ConsentExtensionUtils.getInitiationRetrievalResponse(receiptJSON,
                        consent));
                consentManageData.setResponseStatus(ResponseStatus.OK);
            } catch (ConsentManagementException | ParseException e) {
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Error Occurred");
            }
        } else {
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Invalid consent Id found");
        }
    }

    @Override
    public void handlePost(ConsentManageData consentManageData) throws ConsentException {

        //Check whether client ID exists
        if (StringUtils.isEmpty(consentManageData.getClientId())) {
            log.error("Client ID missing in the request.");
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Client ID missing in the request.");
        }
        if (consentManageData.getRequestPath() == null) {
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Resource Not Found");
        }

        try {
            //Get the request payload from the ConsentManageData
            Object request = consentManageData.getPayload();
            if (!(request instanceof JSONObject)) {
                log.error("Payload is not in the correct format");
                throw new ConsentException(ResponseStatus.BAD_REQUEST, "Payload is not in the correct format");
            }

            String consentType = ConsentExtensionUtils.getConsentType(consentManageData.getRequestPath());

            JSONObject requestObject = (JSONObject) request;

            //Validate Initiation request
            ConsentPayloadValidationResult validationResponse =
                    getConsentManageValidator().validateInitiation(requestObject, consentType);
            if (!validationResponse.isValid()) {
                log.error(validationResponse.getErrorMessage().replaceAll("[\r\n]+", " "));
                throw new ConsentException(validationResponse.getHttpCode(), validationResponse.getErrorCode(),
                        validationResponse.getErrorMessage());
            }

            ConsentResource requestedConsent = new ConsentResource(consentManageData.getClientId(),
                    requestObject.toString(), consentType, ConsentExtensionConstants.AWAIT_AUTHORISE_STATUS);

            DetailedConsentResource createdConsent = ConsentExtensionsDataHolder.getInstance().getConsentCoreService()
                    .createAuthorizableConsent(requestedConsent, null, ConsentExtensionConstants.CREATED_STATUS,
                            ConsentExtensionConstants.DEFAULT_AUTH_TYPE, true);

            consentManageData.setResponsePayload(ConsentExtensionUtils.getInitiationResponse(requestObject,
                    createdConsent));
            consentManageData.setResponseStatus(ResponseStatus.CREATED);

        } catch (ConsentManagementException e) {
            log.error(e.getMessage().replaceAll("[\r\n]+", " "));
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Error Occurred");
        }

    }

    @Override
    public void handleDelete(ConsentManageData consentManageData) throws ConsentException {

        String[] requestPathArray;
        if (consentManageData.getRequestPath() == null) {
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Resource Not Found");
        } else {
            requestPathArray = consentManageData.getRequestPath().split("/");
        }

        if (requestPathArray.length > 1 && StringUtils.isNotEmpty(requestPathArray[0])) {
            String consentId = requestPathArray[1];
            if (ConsentExtensionUtils.isConsentIdValid(consentId)) {
                try {
                    ConsentResource consentResource = ConsentExtensionsDataHolder.getInstance().getConsentCoreService()
                            .getConsent(consentId, false);

                    if (consentResource == null) {
                        throw new ConsentException(ResponseStatus.BAD_REQUEST, "Consent not found");
                    }

                    String consentType = ConsentExtensionUtils.getConsentType(consentManageData.getRequestPath());
                    if (!consentType.equals(consentResource.getConsentType())) {
                        throw new ConsentException(ResponseStatus.BAD_REQUEST, "Consent Type mismatch");
                    }

                    if (!consentResource.getClientID().equals(consentManageData.getClientId())) {
                        //Throwing this error in a generic manner since client will not be able to identify if consent
                        // exists if consent does not belong to them
                        throw new ConsentException(ResponseStatus.BAD_REQUEST,
                                ConsentManageConstants.NO_CONSENT_FOR_CLIENT_ERROR);
                    }

                    if (ConsentExtensionConstants.REVOKED_STATUS.equals(consentResource.getCurrentStatus())) {
                        throw new ConsentException(ResponseStatus.BAD_REQUEST,
                                "Consent already in revoked state");
                    }

                    //Revoke tokens related to the consent if the flag 'shouldRevokeTokens' is true.
                    boolean shouldRevokeTokens = ConsentExtensionConstants.AUTHORIZED_STATUS
                            .equals(consentResource.getCurrentStatus());
                    boolean success = ConsentExtensionsDataHolder.getInstance().getConsentCoreService()
                            .revokeConsent(consentId, ConsentExtensionConstants.REVOKED_STATUS, null,
                            shouldRevokeTokens);
                    if (!success) {
                        throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                                "Token revocation unsuccessful");
                    }
                    consentManageData.setResponseStatus(ResponseStatus.NO_CONTENT);
                } catch (ConsentManagementException e) {
                    log.error(e.getMessage().replaceAll("[\r\n]+", ""));
                    throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR,
                            e.getMessage().replaceAll("[\r\n]+", ""));
                }
            } else {
                throw new ConsentException(ResponseStatus.BAD_REQUEST, "Consent ID invalid");
            }
        } else {
            throw new ConsentException(ResponseStatus.BAD_REQUEST, "Request Path Invalid");
        }

    }

    @Override
    public void handlePut(ConsentManageData consentManageData) throws ConsentException {

        throw new ConsentException(ResponseStatus.METHOD_NOT_ALLOWED, "Method PUT is not supported");
    }

    @Override
    public void handlePatch(ConsentManageData consentManageData) throws ConsentException {

        throw new ConsentException(ResponseStatus.METHOD_NOT_ALLOWED, "Method PATCH is not supported");
    }

    @Override
    public void handleFileUploadPost(ConsentManageData consentManageData) throws ConsentException {

        throw new ConsentException(ResponseStatus.METHOD_NOT_ALLOWED, "Method File Upload POST is not supported");
    }

    @Override
    public void handleFileGet(ConsentManageData consentManageData) throws ConsentException {

        throw new ConsentException(ResponseStatus.METHOD_NOT_ALLOWED, "Method File Upload GET is not supported");
    }

    @Override
    public ConsentManageValidator getConsentManageValidator() {
        return new DefaultConsentManageValidator();
    }
}
