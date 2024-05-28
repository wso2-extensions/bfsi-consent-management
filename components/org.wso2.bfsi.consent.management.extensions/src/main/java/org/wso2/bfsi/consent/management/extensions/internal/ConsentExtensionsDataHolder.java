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

package org.wso2.bfsi.consent.management.extensions.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.bfsi.consent.management.common.config.ConsentManagementConfigurationService;
import org.wso2.bfsi.consent.management.extensions.admin.builder.ConsentAdminBuilder;
import org.wso2.bfsi.consent.management.extensions.authorize.builder.ConsentStepsBuilder;
import org.wso2.bfsi.consent.management.extensions.common.ConsentExtensionExporter;
import org.wso2.bfsi.consent.management.extensions.manage.builder.ConsentManageBuilder;
import org.wso2.bfsi.consent.management.extensions.validate.builder.ConsentValidateBuilder;
import org.wso2.bfsi.consent.management.service.ConsentCoreService;

import java.security.KeyStore;

/**
 * Contains Data holder class for consent extensions.
 */
public class ConsentExtensionsDataHolder {

    private static Log log = LogFactory.getLog(ConsentExtensionsDataHolder.class);
    private static volatile ConsentExtensionsDataHolder instance;
    private ConsentManagementConfigurationService configurationService;
    private ConsentCoreService consentCoreService;
    private ConsentStepsBuilder consentStepsBuilder;
    private ConsentAdminBuilder consentAdminBuilder;
    private ConsentManageBuilder consentManageBuilder;
    private ConsentValidateBuilder consentValidateBuilder;
    private KeyStore trustStore = null;

    // Prevent instantiation
    private ConsentExtensionsDataHolder() {}

    /**
     * Return a singleton instance of the data holder.
     *
     * @return A singleton instance of the data holder
     */
    public static synchronized ConsentExtensionsDataHolder getInstance() {
        if (instance == null) {
            synchronized (ConsentExtensionsDataHolder.class) {
                if (instance == null) {
                    instance = new ConsentExtensionsDataHolder();
                }
            }
        }
        return instance;
    }

    public ConsentManagementConfigurationService getConfigurationService() {

        return configurationService;
    }

    public void setConfigurationService(ConsentManagementConfigurationService configurationService) {

        this.configurationService = configurationService;

        ConsentStepsBuilder consentStepsBuilder = new ConsentStepsBuilder();
        consentStepsBuilder.build();
        this.setConsentStepsBuilder(consentStepsBuilder);
        ConsentExtensionExporter.setConsentStepsBuilder(consentStepsBuilder);

        ConsentAdminBuilder consentAdminBuilder = new ConsentAdminBuilder();
        consentAdminBuilder.build();
        this.setConsentAdminBuilder(consentAdminBuilder);
        ConsentExtensionExporter.setConsentAdminBuilder(consentAdminBuilder);

        ConsentManageBuilder consentManageBuilder = new ConsentManageBuilder();
        consentManageBuilder.build();
        this.setConsentManageBuilder(consentManageBuilder);
        ConsentExtensionExporter.setConsentManageBuilder(consentManageBuilder);

        ConsentValidateBuilder consentValidateBuilder = new ConsentValidateBuilder();
        consentValidateBuilder.build();
        this.setConsentValidateBuilder(consentValidateBuilder);
        ConsentExtensionExporter.setConsentValidateBuilder(consentValidateBuilder);
    }

    public ConsentManageBuilder getConsentManageBuilder() {
        return consentManageBuilder;
    }

    public void setConsentManageBuilder(ConsentManageBuilder consentManageBuilder) {
        this.consentManageBuilder = consentManageBuilder;
    }

    public ConsentStepsBuilder getConsentStepsBuilder() {
        return consentStepsBuilder;
    }

    public void setConsentStepsBuilder(ConsentStepsBuilder consentStepsBuilder) {
        this.consentStepsBuilder = consentStepsBuilder;
    }

    public ConsentAdminBuilder getConsentAdminBuilder() {
        return consentAdminBuilder;
    }

    public void setConsentAdminBuilder(ConsentAdminBuilder consentAdminBuilder) {
        this.consentAdminBuilder = consentAdminBuilder;
    }

    public ConsentValidateBuilder getConsentValidateBuilder() {
        return consentValidateBuilder;
    }

    public void setConsentValidateBuilder(ConsentValidateBuilder consentValidateBuilder) {
        this.consentValidateBuilder = consentValidateBuilder;
    }

    public ConsentCoreService getConsentCoreService() {
        return consentCoreService;
    }

    public void setConsentCoreService(ConsentCoreService consentCoreService) {
        this.consentCoreService = consentCoreService;
    }

    public KeyStore getTrustStore() {
        return trustStore;
    }

    public void setTrustStore(KeyStore trustStore) {
        this.trustStore = trustStore;
    }
}
