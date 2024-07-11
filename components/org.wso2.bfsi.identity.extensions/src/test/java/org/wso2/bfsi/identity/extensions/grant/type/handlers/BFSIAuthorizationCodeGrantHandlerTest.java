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

package org.wso2.bfsi.identity.extensions.grant.type.handlers;

import org.mockito.MockedStatic;
import org.testng.annotations.Test;
import org.wso2.bfsi.consent.management.common.util.CommonUtils;
import org.wso2.bfsi.consent.management.common.util.ConsentManagementConstants;
import org.wso2.bfsi.identity.extensions.internal.IdentityExtensionsDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth.dao.OAuthAppDO;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dao.AccessTokenDAO;
import org.wso2.carbon.identity.oauth2.dao.OAuthTokenPersistenceFactory;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.internal.OAuth2ServiceComponentHolder;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.token.OauthTokenIssuer;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

/**
 * BFSI specific authorization code grant handler.
 */
public class BFSIAuthorizationCodeGrantHandlerTest {

    BFSIAuthorizationCodeGrantHandler handler;

    @Test
    public void testIssue() throws IdentityOAuth2Exception {

        try (MockedStatic<CommonUtils> commonUtilMock = mockStatic(CommonUtils.class);
             MockedStatic<IdentityExtensionsDataHolder> dataHolderMock = mockStatic(IdentityExtensionsDataHolder.class);
             MockedStatic<OAuthServerConfiguration> config = mockStatic(OAuthServerConfiguration.class);
             MockedStatic<OAuth2ServiceComponentHolder> oAuthComponentServiceHolder =
                     mockStatic(OAuth2ServiceComponentHolder.class)) {

            config.when(OAuthServerConfiguration::getInstance).thenReturn(mock(OAuthServerConfiguration.class));
            commonUtilMock.when(() -> CommonUtils.isRegulatoryApp(anyString())).thenReturn(true);
            IdentityExtensionsDataHolder dataHolder = mock(IdentityExtensionsDataHolder.class);
            doReturn(Map.of(ConsentManagementConstants.CONSENT_ID_CLAIM_NAME, "consent_id"))
                    .when(dataHolder).getConfigurationMap();
            dataHolderMock.when(IdentityExtensionsDataHolder::getInstance).thenReturn(dataHolder);

            OAuth2ServiceComponentHolder oAuth2ServiceComponentHolder = mock(OAuth2ServiceComponentHolder.class);
            oAuthComponentServiceHolder.when(OAuth2ServiceComponentHolder::getInstance)
                    .thenReturn(oAuth2ServiceComponentHolder);

            try (MockedStatic<OAuth2Util> oAuth2UtilMock = mockStatic(OAuth2Util.class);
                 MockedStatic<OAuthTokenPersistenceFactory> factoryMock =
                         mockStatic(OAuthTokenPersistenceFactory.class)) {

                AccessTokenDAO accessTokenDAO = mock(AccessTokenDAO.class);
                doNothing().when(accessTokenDAO).insertAccessToken(anyString(), any(), any(), anyString());
                OAuthTokenPersistenceFactory factory = mock(OAuthTokenPersistenceFactory.class);
                doReturn(accessTokenDAO).when(factory).getAccessTokenDAO();
                factoryMock.when(OAuthTokenPersistenceFactory::getInstance).thenReturn(factory);

                OauthTokenIssuer oauthTokenIssuer = mock(OauthTokenIssuer.class);
                OAuthAppDO appDO = mock(OAuthAppDO.class);
                oAuth2UtilMock.when(() -> OAuth2Util.getOAuthTokenIssuerForOAuthApp(anyString()))
                        .thenReturn(oauthTokenIssuer);
                oAuth2UtilMock.when(() -> OAuth2Util.getAppInformationByClientId(anyString()))
                        .thenReturn(appDO);

                OAuth2AccessTokenReqDTO accessTokenReqDTO = mock(OAuth2AccessTokenReqDTO.class);
                doReturn("client_id").when(accessTokenReqDTO).getClientId();
                doReturn("authorization_code").when(accessTokenReqDTO).getGrantType();
                OAuthTokenReqMessageContext tokReqMsgCtx = mock(OAuthTokenReqMessageContext.class);
                doReturn(accessTokenReqDTO).when(tokReqMsgCtx).getOauth2AccessTokenReqDTO();
                AuthenticatedUser authenticatedUser = mock(AuthenticatedUser.class);
                doReturn("user").when(authenticatedUser).getUserName();
                doReturn(authenticatedUser).when(tokReqMsgCtx).getAuthorizedUser();
                doReturn(new String[]{"openid", "accounts", "consent_id_1234"}).when(tokReqMsgCtx).getScope();

                handler = new BFSIAuthorizationCodeGrantHandler();
                handler.issue(tokReqMsgCtx);
            }
        }
    }
}
