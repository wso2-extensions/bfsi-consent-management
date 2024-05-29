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

package org.wso2.bfsi.identity.extensions.internal;

import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.oauth2.OAuth2Service;
import org.wso2.carbon.identity.openidconnect.RequestObjectService;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.Map;

/**
 * Data Holder for Identity Extensions.
 */
public class IdentityExtensionsDataHolder {

    private static volatile IdentityExtensionsDataHolder instance;
    private static ApplicationManagementService applicationManagementService;
//    private static ConsentManagementConfigurationService configurationService;
    Map<String, Object> configurationMap;
//    private OBRequestObjectValidator obRequestObjectValidator;
//    private OBResponseTypeHandler obResponseTypeHandler;
    private static RealmService realmService;
    private static OAuth2Service oAuth2Service;
    private RequestObjectService requestObjectService;

    private IdentityExtensionsDataHolder() {

    }

    public static IdentityExtensionsDataHolder getInstance() {

        if (instance == null) {
            synchronized (IdentityExtensionsDataHolder.class) {
                if (instance == null) {
                    instance = new IdentityExtensionsDataHolder();
                }
            }
        }
        return instance;
    }

    /**
     * To get the the instance of {@link ApplicationManagementService}.
     *
     * @return applicationManagementService
     */
    public ApplicationManagementService getApplicationManagementService() {

        return applicationManagementService;
    }

    /**
     * To set the ApplicationManagementService.
     *
     * @param applicationManagementService instance of {@link ApplicationManagementService}
     */
    public void setApplicationManagementService(ApplicationManagementService applicationManagementService) {

        IdentityExtensionsDataHolder.applicationManagementService = applicationManagementService;
    }

//    public ConsentManagementConfigurationService getOpenBankingConfigurationService() {
//
//        return configurationService;
//    }

//    public void setConfigurationService(ConsentManagementConfigurationService configurationService) {
//
//        IdentityExtensionsDataHolder.configurationService = configurationService;
//        this.configurationMap = configurationService.getConfigurations();
//        obRequestObjectValidator = (OBRequestObjectValidator) IdentityCommonUtils.getClassInstanceFromFQN(
//                this.configurationMap.get(ConsentManagementConstants.REQUEST_VALIDATOR).toString());
//        obResponseTypeHandler = (OBResponseTypeHandler) IdentityCommonUtils.getClassInstanceFromFQN(
//                this.configurationMap.get(ConsentManagementConstants.RESPONSE_HANDLER).toString());
////        this.setClaimProvider((ClaimProvider) OpenBankingUtils.getClassInstanceFromFQN(openBankingConfigurationService
////                .getConfigurations().get(IdentityCommonConstants.CLAIM_PROVIDER).toString()));
////        OBClaimProvider.setClaimProvider(getClaimProvider());
//    }

    public void setConfigurationMap(Map<String, Object> confMap) {

        configurationMap = confMap;
    }

    public Map<String, Object> getConfigurationMap() {

        return configurationMap;
    }

//    public OBRequestObjectValidator getObRequestObjectValidator() {
//        return obRequestObjectValidator;
//    }
//
//    public OBResponseTypeHandler getObResponseTypeHandler() {
//        return obResponseTypeHandler;
//    }

    public RealmService getRealmService() {

        if (realmService == null) {
            throw new RuntimeException("Realm Service is not available. Component did not start correctly.");
        }
        return realmService;
    }

    void setRealmService(RealmService realmService) {

        IdentityExtensionsDataHolder.realmService = realmService;
    }

    /**
     * To get the instance of {@link OAuth2Service}.
     *
     * @return OAuth2Service
     */
    public OAuth2Service getOAuth2Service() {

        return oAuth2Service;
    }

    /**
     * To set the OAuth2Service.
     *
     * @param oAuth2Service instance of {@link OAuth2Service}
     */
    public void setOAuth2Service(OAuth2Service oAuth2Service) {

        IdentityExtensionsDataHolder.oAuth2Service = oAuth2Service;
    }

    public RequestObjectService getRequestObjectService() {

        return requestObjectService;
    }

    /**
     * To set the RequestObjectService.
     *
     * @param requestObjectService instance of {@link RequestObjectService}
     */
    public void setRequestObjectService(RequestObjectService requestObjectService) {

        this.requestObjectService = requestObjectService;
    }
}
