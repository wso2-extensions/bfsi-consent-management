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

package org.wso2.bfsi.identity.extensions.listener;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.bfsi.consent.management.common.config.ConsentManagementConfigParser;
import org.wso2.bfsi.consent.management.common.exceptions.ConsentManagementException;
import org.wso2.bfsi.consent.management.common.util.ConsentManagementConstants;
import org.wso2.bfsi.consent.management.common.util.Generated;
import org.wso2.bfsi.consent.management.service.impl.ConsentCoreServiceImpl;
import org.wso2.carbon.identity.oauth.event.AbstractOAuthEventInterceptor;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.bean.OAuthClientAuthnContext;
import org.wso2.carbon.identity.oauth2.dto.OAuthRevocationRequestDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuthRevocationResponseDTO;
import org.wso2.carbon.identity.oauth2.model.AccessTokenDO;
import org.wso2.carbon.identity.oauth2.model.RefreshTokenValidationDataDO;

import java.util.Map;

/**
 * Event listener to revoke consents when access token is revoked.
 */
public class TokenRevocationListener extends AbstractOAuthEventInterceptor {

    private static final Log log = LogFactory.getLog(TokenRevocationListener.class);

    /**
     * Revoke the consent bound to the access token after revoking the access token.
     *
     * @param revokeRequestDTO
     * @param revokeResponseDTO
     * @param accessTokenDO
     * @param refreshTokenDO
     * @param params
     * @throws IdentityOAuth2Exception
     */
    @Override
    @Generated(message = "Excluding from code coverage since it requires a service call")
    public void onPostTokenRevocationByClient(OAuthRevocationRequestDTO revokeRequestDTO,
                                              OAuthRevocationResponseDTO revokeResponseDTO,
                                              AccessTokenDO accessTokenDO,
                                              RefreshTokenValidationDataDO refreshTokenDO,
                                              Map<String, Object> params) throws IdentityOAuth2Exception {

        if (!revokeRequestDTO.getoAuthClientAuthnContext().isAuthenticated()) {
            return;
        }
        String consentId = "";
        if (accessTokenDO != null) {
            consentId = getConsentIdFromScopes(accessTokenDO.getScope());
        } else if (refreshTokenDO != null) {
            consentId = getConsentIdFromScopes(refreshTokenDO.getScope());
        }
        if (StringUtils.isNotEmpty(consentId)) {
            try {
                OAuthClientAuthnContext context = revokeRequestDTO.getoAuthClientAuthnContext();
                Map contextParams = context.getParameters();
                // Skip consent revocation if the request is from consent revocation flow.
                boolean isConsentRevocationFlow =
                        contextParams.containsKey(ConsentManagementConstants.IS_CONSENT_REVOCATION_FLOW)
                        && (boolean) context.getParameter(ConsentManagementConstants.IS_CONSENT_REVOCATION_FLOW);
                if (!isConsentRevocationFlow) {
                    (new ConsentCoreServiceImpl()).revokeConsentWithReason(consentId, ConsentManagementConstants.
                            DEFAULT_STATUS_FOR_REVOKED_CONSENTS, null, false, "Revoked by token revocation");
                }
            } catch (ConsentManagementException e) {
                log.error(String.format("Error occurred while revoking consent on token revocation. %s",
                        e.getMessage().replaceAll("[\r\n]", "")));
            }
        }
    }

    /**
     * Return consent-id when a string array of scopes is given.
     *
     * @param scopes
     * @return
     */
    public String getConsentIdFromScopes(String[] scopes) {

        String consentIdClaim = ConsentManagementConfigParser.getInstance().getConfiguration()
                .get(ConsentManagementConstants.CONSENT_ID_CLAIM_NAME).toString();
        if (scopes != null) {
            for (String scope : scopes) {
                if (scope.contains(consentIdClaim)) {
                    return scope.split(consentIdClaim)[1];
                }
            }
        }
        return null;
    }
}
