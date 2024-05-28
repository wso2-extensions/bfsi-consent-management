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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.bfsi.consent.management.extensions.common.ConsentExtensionConstants;
import org.wso2.bfsi.consent.management.extensions.common.ResponseStatus;
import org.wso2.bfsi.consent.management.extensions.manage.ConsentManageValidator;
import org.wso2.bfsi.consent.management.extensions.manage.model.ConsentPayloadValidationResult;

/**
 * Payload validator class.
 */
public class DefaultConsentManageValidator implements ConsentManageValidator {

    private static final Log log = LogFactory.getLog(DefaultConsentManageValidator.class);

    @Override
    public ConsentPayloadValidationResult validateInitiation(JSONObject initiation, String consentType) {
        switch (consentType) {
            case ConsentExtensionConstants.ACCOUNTS:
                return validateAccountInitiation(initiation);
            case ConsentExtensionConstants.FUNDS_CONFIRMATIONS:
                return validateCOFInitiation(initiation);
            case ConsentExtensionConstants.PAYMENTS:
                return validatePaymentInitiation(initiation);
            default:
                return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST, "invalid_consent_type",
                        "Invalid consent type");
        }
    }

    /**
     * Method to validate account initiation request.
     *
     * @param initiation      Initiation Object
     * @return ConsentPayloadValidationResult     Validation Result
     */
    public static ConsentPayloadValidationResult validateAccountInitiation(JSONObject initiation) {
        return new ConsentPayloadValidationResult(true);
    }

    /**
     * Method to validate COF initiation request.
     *
     * @param initiation      Initiation Object
     * @return ConsentPayloadValidationResult     Validation Result
     */
    public static ConsentPayloadValidationResult validateCOFInitiation(JSONObject initiation) {
        return new ConsentPayloadValidationResult(true);
    }

    /**
     * Method to validate payment initiation request.
     *
     * @param initiation      Initiation Object
     * @return ConsentPayloadValidationResult     Validation Result
     */
    public static ConsentPayloadValidationResult validatePaymentInitiation(JSONObject initiation) {

        //Check request body is valid and not empty
        ConsentPayloadValidationResult dataValidationResult = validateInitiationDataBody(initiation);
        if (!(boolean) dataValidationResult.isValid()) {
            return dataValidationResult;
        }

        //TODO: Validations
//        JSONObject data = (JSONObject) initiation.get("Data");

//        if (data.containsKey("Initiation")) {
//            JSONObject initiationValidationResult = validatePaymentInitiationPayload(requestPath,
//                            (JSONObject) data.get("Initiation"));
//            if (!(boolean) initiationValidationResult.get("isValid")) {
//                return initiationValidationResult;
//            }
//        }

        return new ConsentPayloadValidationResult(true);
    }

    /**
     * Check whether valid Data object is provided.
     *
     * @param initiationRequestBody Data object in initiation payload
     * @return whether the Data object is valid
     */
    public static ConsentPayloadValidationResult validateInitiationDataBody(JSONObject initiationRequestBody) {

        if (!initiationRequestBody.containsKey("Data") || !(initiationRequestBody.get("Data")
                instanceof JSONObject) || ((JSONObject) initiationRequestBody.get("Data")).isEmpty()) {
            log.error("Invalid request payload");
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST, "invalid_format",
                    "Invalid request payload");
        }

        return new ConsentPayloadValidationResult(true);
    }
}
