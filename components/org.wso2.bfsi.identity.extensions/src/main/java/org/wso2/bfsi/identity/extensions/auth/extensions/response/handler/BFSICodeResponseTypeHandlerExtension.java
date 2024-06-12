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

package org.wso2.bfsi.identity.extensions.auth.extensions.response.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.bfsi.consent.management.common.util.CommonUtils;
import org.wso2.bfsi.consent.management.common.util.Generated;
import org.wso2.bfsi.identity.extensions.internal.IdentityExtensionsDataHolder;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.RequestObjectException;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;
import org.wso2.carbon.identity.oauth2.authz.handlers.CodeResponseTypeHandler;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AuthorizeRespDTO;

/**
 * Extension to append scope with BFSI_ prefix at the end of auth flow, before offering auth code.
 */
public class BFSICodeResponseTypeHandlerExtension extends CodeResponseTypeHandler {

    private static final Log log = LogFactory.getLog(BFSICodeResponseTypeHandlerExtension.class);
    static BFSIResponseTypeHandler bfsiResponseTypeHandler =
            IdentityExtensionsDataHolder.getInstance().getObResponseTypeHandler();

    /**
     * Extension point to get updated scope and refresh token validity period.
     *
     * @param oauthAuthzMsgCtx  OAuthAuthzReqMessageContext
     * @return OAuth2AuthorizeRespDTO
     * @throws IdentityOAuth2Exception If an error occurred while issuing the code.
     */
    @Override
    public OAuth2AuthorizeRespDTO issue(OAuthAuthzReqMessageContext oauthAuthzMsgCtx) throws IdentityOAuth2Exception {

        try {
            if (!CommonUtils.isRegulatoryApp(oauthAuthzMsgCtx.getAuthorizationReqDTO().getConsumerKey())) {
                return issueCode(oauthAuthzMsgCtx);
            }
        } catch (RequestObjectException e) {
            log.error("Error while reading regulatory property", e);
            throw new IdentityOAuth2Exception("Error while reading regulatory property");
        }

        oauthAuthzMsgCtx.setRefreshTokenvalidityPeriod(
                bfsiResponseTypeHandler.updateRefreshTokenValidityPeriod(oauthAuthzMsgCtx));
        String[] approvedScopes = bfsiResponseTypeHandler.updateApprovedScopes(oauthAuthzMsgCtx);
        if (approvedScopes != null) {
            oauthAuthzMsgCtx.setApprovedScope(approvedScopes);
        } else {
            log.error("Error while updating scopes");
            throw new IdentityOAuth2Exception("Error while updating scopes");
        }
        return issueCode(oauthAuthzMsgCtx);
    }

    /**
     * Separated method to call parent issue.
     *
     * @param oAuthAuthzReqMessageContext OAuthAuthzReqMessageContext
     * @return OAuth2AuthorizeRespDTO
     * @throws IdentityOAuth2Exception If an error occurred while issuing the code.
     */
    @Generated(message = "Cannot test super calls")
    OAuth2AuthorizeRespDTO issueCode(
            OAuthAuthzReqMessageContext oAuthAuthzReqMessageContext) throws IdentityOAuth2Exception {

        return super.issue(oAuthAuthzReqMessageContext);
    }
}
