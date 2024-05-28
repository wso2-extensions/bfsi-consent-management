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

package org.wso2.bfsi.consent.management.extensions.validate.builder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.bfsi.consent.management.common.util.ConsentManagementConstants;
import org.wso2.bfsi.consent.management.extensions.common.ConsentExtensionUtils;
import org.wso2.bfsi.consent.management.extensions.internal.ConsentExtensionsDataHolder;
import org.wso2.bfsi.consent.management.extensions.validate.ConsentValidator;

import java.util.Map;

/**
 * Builder class for consent validator.
 */
public class ConsentValidateBuilder {

    private static final Log log = LogFactory.getLog(ConsentValidateBuilder.class);
    private ConsentValidator consentValidator = null;
    private String requestSignatureAlias = null;


    public void build() {

        Map<String, Object> configs =  ConsentExtensionsDataHolder.getInstance().getConfigurationService()
                .getConfigurations();
        String handlerConfig = (String)  configs.get(ConsentManagementConstants.CONSENT_VALIDATOR);
        consentValidator = (ConsentValidator) ConsentExtensionUtils.getClassInstanceFromFQN(handlerConfig);
        requestSignatureAlias = (String) configs.get(ConsentManagementConstants.SIGNATURE_ALIAS);
        log.debug("Admin handler loaded successfully");
    }

    public ConsentValidator getConsentValidator() {
        return consentValidator;
    }

    public String getRequestSignatureAlias() {
        return requestSignatureAlias;
    }
}
