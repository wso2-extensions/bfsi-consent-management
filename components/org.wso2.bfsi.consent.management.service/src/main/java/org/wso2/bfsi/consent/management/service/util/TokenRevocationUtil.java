package org.wso2.bfsi.consent.management.service.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.wso2.bfsi.consent.management.common.config.ConsentManagementConfigParser;
import org.wso2.bfsi.consent.management.common.util.ConsentManagementConstants;
import org.wso2.bfsi.consent.management.common.util.Generated;
import org.wso2.bfsi.consent.management.dao.models.DetailedConsentResource;
import org.wso2.bfsi.consent.management.service.internal.ConsentManagementDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.OAuth2Service;
import org.wso2.carbon.identity.oauth2.bean.OAuthClientAuthnContext;
import org.wso2.carbon.identity.oauth2.dao.OAuthTokenPersistenceFactory;
import org.wso2.carbon.identity.oauth2.dto.OAuthRevocationRequestDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuthRevocationResponseDTO;
import org.wso2.carbon.identity.oauth2.model.AccessTokenDO;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Util class containing utility methods to revoke tokens.

 */
public class TokenRevocationUtil {

    private static final Log log = LogFactory.getLog(TokenRevocationUtil.class);

    public static void revokeTokens(DetailedConsentResource detailedConsentResource, String userID)
            throws IdentityOAuth2Exception {

        OAuth2Service oAuth2Service = getOAuth2Service();
        String clientId = detailedConsentResource.getClientID();
        String consentId = detailedConsentResource.getConsentID();
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(userID);
        Set<AccessTokenDO> accessTokenDOSet = getAccessTokenDOSet(detailedConsentResource, authenticatedUser);

        String consentIdClaim = ConsentManagementConfigParser.getInstance().getConfiguration()
                .get(ConsentManagementConstants.CONSENT_ID_CLAIM_NAME).toString();

        if (!accessTokenDOSet.isEmpty()) {
            Set<String> activeTokens = new HashSet<>();
            // Get tokens to revoke to an array
            for (AccessTokenDO accessTokenDO : accessTokenDOSet) {
                // Filter tokens by consent ID claim
                if (Arrays.asList(accessTokenDO.getScope()).contains(consentIdClaim +
                        detailedConsentResource.getConsentID())) {
                    activeTokens.add(accessTokenDO.getAccessToken());
                }
            }

            if (!activeTokens.isEmpty()) {
                // set authorization context details for the given user
                OAuthClientAuthnContext oAuthClientAuthnContext = new OAuthClientAuthnContext();
                oAuthClientAuthnContext.setAuthenticated(true);
                oAuthClientAuthnContext.setClientId(clientId);
                oAuthClientAuthnContext.addParameter(ConsentManagementConstants.IS_CONSENT_REVOCATION_FLOW, true);

                // set common properties of token revocation request
                OAuthRevocationRequestDTO revokeRequestDTO = new OAuthRevocationRequestDTO();
                revokeRequestDTO.setOauthClientAuthnContext(oAuthClientAuthnContext);
                revokeRequestDTO.setConsumerKey(clientId);
                revokeRequestDTO.setTokenType(GrantType.REFRESH_TOKEN.toString());

                for (String activeToken : activeTokens) {
                    // set access token to be revoked
                    revokeRequestDTO.setToken(activeToken);
                    OAuthRevocationResponseDTO oAuthRevocationResponseDTO =
                            revokeTokenByClient(oAuth2Service, revokeRequestDTO);

                    if (oAuthRevocationResponseDTO.isError()) {
                        log.error(String.format("Error while revoking access token for consent ID: %s",
                                consentId.replaceAll("[\r\n]", "")));
                        throw new IdentityOAuth2Exception(
                                String.format("Error while revoking access token for consent ID: %s. Caused by, %s",
                                        consentId, oAuthRevocationResponseDTO.getErrorMsg()));
                    }
                }
            }
        }
    }

    @Generated(message = "Excluded from code coverage since used for testing purposes")
    static OAuth2Service getOAuth2Service() {

        return ConsentManagementDataHolder.getInstance().getOAuth2Service();
    }

    @Generated(message = "Excluded from code coverage since used for testing purposes")
    public static AuthenticatedUser getAuthenticatedUser(String userID) throws IdentityOAuth2Exception {
        // set domain name
        if (UserCoreUtil.getDomainFromThreadLocal() == null) {
            UserCoreUtil.setDomainInThreadLocal(UserCoreUtil.extractDomainFromName(userID));
        }
        if (ConsentManagementConfigParser.getInstance().isPSUFederated()) {
            AuthenticatedUser authenticatedUser =
                    AuthenticatedUser.createFederateAuthenticatedUserFromSubjectIdentifier(userID);
            authenticatedUser.setUserStoreDomain(OAuth2Util.getUserStoreForFederatedUser(authenticatedUser));
            authenticatedUser.setTenantDomain(MultitenantUtils.getTenantDomain(userID));
            authenticatedUser.setFederatedIdPName(ConsentManagementConfigParser.getInstance().getFederatedIDPName());
            authenticatedUser.setUserName(MultitenantUtils.getTenantAwareUsername(userID));
            return authenticatedUser;
        } else {
            return AuthenticatedUser.createLocalAuthenticatedUserFromSubjectIdentifier(userID);
        }
    }

    @Generated(message = "Excluded from code coverage since used for testing purposes")
    public static Set<AccessTokenDO> getAccessTokenDOSet(DetailedConsentResource detailedConsentResource,
                                                  AuthenticatedUser authenticatedUser) throws IdentityOAuth2Exception {

        return OAuthTokenPersistenceFactory.getInstance().getAccessTokenDAO()
                .getAccessTokens(detailedConsentResource.getClientID(), authenticatedUser,
                        authenticatedUser.getUserStoreDomain(), false);
    }

    @Generated(message = "Excluded from code coverage since used for testing purposes")
    public static OAuthRevocationResponseDTO revokeTokenByClient(OAuth2Service oAuth2Service,
                                                          OAuthRevocationRequestDTO revocationRequestDTO) {

        return oAuth2Service.revokeTokenByOAuthClient(revocationRequestDTO);
    }
}
