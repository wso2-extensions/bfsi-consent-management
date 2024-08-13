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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.bfsi.consent.management.common.config.ConsentManagementConfigurationService;
import org.wso2.bfsi.identity.extensions.claims.RoleClaimProviderImpl;
import org.wso2.bfsi.identity.extensions.listener.TokenRevocationListener;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.oauth.event.OAuthEventInterceptor;
import org.wso2.carbon.identity.oauth2.OAuth2Service;
import org.wso2.carbon.identity.openidconnect.ClaimProvider;
import org.wso2.carbon.identity.openidconnect.RequestObjectService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * Identity common data holder.
 */
@Component(
        name = "org.wso2.bfsi.identity.extensions.internal.IdentityExtensionsServiceComponent",
        immediate = true
)
public class IdentityExtensionsServiceComponent {

    private static Log log = LogFactory.getLog(IdentityExtensionsServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        log.debug("Identity Extensions component activated.");
        context.getBundleContext().registerService(ClaimProvider.class.getName(), new RoleClaimProviderImpl(),
                null);
        context.getBundleContext().registerService(OAuthEventInterceptor.class.getName(), new TokenRevocationListener(),
                null);

        log.debug("Registered BFSI related Identity services.");
    }

    @Reference(
            name = "ApplicationManagementService",
            service = ApplicationManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetApplicationManagementService"
    )
    protected void setApplicationManagementService(ApplicationManagementService mgtService) {

        IdentityExtensionsDataHolder.getInstance().setApplicationManagementService(mgtService);
    }

    protected void unsetApplicationManagementService(ApplicationManagementService mgtService) {

        IdentityExtensionsDataHolder.getInstance().setApplicationManagementService(null);
    }

    @Reference(
            service = ConsentManagementConfigurationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigService"
    )
    public void setConfigService(ConsentManagementConfigurationService configurationService) {

        IdentityExtensionsDataHolder.getInstance().setConfigurationService(configurationService);
    }

    public void unsetConfigService(ConsentManagementConfigurationService configurationService) {

        IdentityExtensionsDataHolder.getInstance().setConfigurationService(configurationService);
    }

    @Reference(
            name = "realm.service",
            service = RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService"
    )
    protected void setRealmService(RealmService realmService) {

        log.debug("Setting the Realm Service");
        IdentityExtensionsDataHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {

        log.debug("UnSetting the Realm Service");
        IdentityExtensionsDataHolder.getInstance().setRealmService(null);
    }

    @Reference(
            service = OAuth2Service.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetOAuth2Service"
    )
    public void setOAuth2Service(OAuth2Service oAuth2Service) {
        log.debug("Setting the OAuth2 Service");
        IdentityExtensionsDataHolder.getInstance().setOAuth2Service(oAuth2Service);
    }

    public void unsetOAuth2Service(OAuth2Service oAuth2Service) {
        log.debug("UnSetting the OAuth2 Service");
        IdentityExtensionsDataHolder.getInstance().setOAuth2Service(null);
    }

    @Reference(
            name = "RequestObjectService",
            service = RequestObjectService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRequestObjectService"
    )
    protected void setRequestObjectService(RequestObjectService requestObjectService) {

        IdentityExtensionsDataHolder.getInstance().setRequestObjectService(requestObjectService);
    }

    protected void unsetRequestObjectService(RequestObjectService requestObjectService) {

        IdentityExtensionsDataHolder.getInstance().setRequestObjectService(null);
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        log.debug("Identity Extensions component deactivated.");
    }
}
