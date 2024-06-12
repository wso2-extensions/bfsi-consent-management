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

package org.wso2.bfsi.identity.extensions.auth.extensions.request.validator;

import com.nimbusds.jwt.JWTClaimsSet;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.bfsi.identity.extensions.auth.extensions.request.validator.models.BFSIRequestObject;
import org.wso2.bfsi.identity.extensions.auth.extensions.request.validator.models.ValidationResponse;
import org.wso2.bfsi.identity.extensions.util.IdentityCommonConstants;
import org.wso2.carbon.identity.oauth2.RequestObjectException;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * The extension class for enforcing BFSI Request Object Validations. For Tool kits to extend.
 */
public class DefaultBFSIRequestObjectValidator extends BFSIRequestObjectValidator {

    private static final String CLAIMS = "claims";
    private static final String[] CLAIM_FIELDS = new String[]{"id_token", "userinfo"};
    private static final String OPENBANKING_INTENT_ID = "openbanking_intent_id";
    private static final String VALUE = "value";
    private static final String CLIENT_ID = "client_id";
    private static final String SCOPE = "scope";

    private static final Log log = LogFactory.getLog(DefaultBFSIRequestObjectValidator.class);

    public DefaultBFSIRequestObjectValidator() {
    }

    /**
     * Extension point for tool kits. Perform validation and return the error message if any, else null.
     *
     * @param bfsiRequestObject request object
     * @param dataMap         provides scope related data needed for validation from service provider meta data
     * @return the response object with error message.
     */
    @Override
    public ValidationResponse validateBFSIConstraints(BFSIRequestObject bfsiRequestObject,
                                                      Map<String, Object> dataMap) {

        ValidationResponse superValidationResponse = super.validateBFSIConstraints(bfsiRequestObject, dataMap);

        if (superValidationResponse.isValid()) {
            try {
                if (!isClientIdAndScopePresent(bfsiRequestObject)) {
                    return new ValidationResponse(false, "Client Id and scope are mandatory to" +
                            " include in the request object.");
                }
                String violation = validateScope(bfsiRequestObject, dataMap);
                if (StringUtils.isEmpty(violation)) {
                    return new ValidationResponse(true);
                } else {
                    return new ValidationResponse(false, violation);
                }
            } catch (RequestObjectException e) {
                return new ValidationResponse(false, e.getMessage());
            }
        } else {
            return superValidationResponse;
        }
    }

    /**
     * Extract clientId and scope from ob request object and check whether it's present.
     *
     * @param bfsiRequestObject
     * @return result received from validateConsentIdWithClientId method
     * @throws RequestObjectException if error occurred while validating
     */
    private boolean isClientIdAndScopePresent(BFSIRequestObject bfsiRequestObject) throws RequestObjectException {
        JSONObject jsonObject = bfsiRequestObject.getSignedJWT().getPayload().toJSONObject();
        final String clientId = jsonObject.getAsString(CLIENT_ID);
        String scope = jsonObject.getAsString(SCOPE);
        if (StringUtils.isBlank(clientId) || StringUtils.isBlank(scope)) {
            log.error("Client id or scope cannot be empty");
            throw new RequestObjectException("Client id or scope cannot be empty");
        }
        return true;
    }

    private String validateScope(BFSIRequestObject bfsiRequestObject, Map<String, Object> dataMap) {

        try {
            //remove scope claim
            JWTClaimsSet claimsSet = bfsiRequestObject.getClaimsSet();
            JSONObject claimsSetJsonObject = claimsSet.toJSONObject();
            if (claimsSetJsonObject.containsKey("scope")) {
                String scopeClaimString = claimsSetJsonObject.remove("scope").toString();
                List allowedScopes = (List) dataMap.get("scope");
                List<String> requestedScopes = new ArrayList<>(Arrays.asList(scopeClaimString.split(" ")));
                StringBuilder stringBuilder = new StringBuilder();

                // iterate through requested scopes and remove if not allowed
                for (String scope : requestedScopes) {
                    if (IdentityCommonConstants.OPENID_SCOPE.equals(scope)) {
                        stringBuilder.append(scope).append(" ");
                        if (log.isDebugEnabled()) {
                            log.debug("Adding Openid scope to the request object");
                        }
                    }
                    if (allowedScopes.contains(scope)) {
                        stringBuilder.append(scope).append(" ");
                        if (log.isDebugEnabled()) {
                            log.debug(String.format("Removed scope %s from the request object",
                                    scope.replaceAll("[\r\n]", "")));
                        }
                    }
                }
                String modifiedScopeString = stringBuilder.toString().trim();
                // throw an error if no valid scopes found or only openid scope is found
                if (StringUtils.isBlank(modifiedScopeString) || modifiedScopeString.split(" ").length <= 1) {
                    throw new RequestObjectException("No valid scopes found in the request");
                }
                claimsSetJsonObject.put("scope", modifiedScopeString);
                //Set claims set to request object
                JWTClaimsSet validatedClaimsSet = JWTClaimsSet.parse(claimsSetJsonObject);
                bfsiRequestObject.setClaimSet(validatedClaimsSet);
                log.debug("Successfully set the modified claims-set to the request object");
            }
        } catch (ParseException | RequestObjectException e) {
            return e.getMessage();
        }
        return StringUtils.EMPTY;
    }
}
