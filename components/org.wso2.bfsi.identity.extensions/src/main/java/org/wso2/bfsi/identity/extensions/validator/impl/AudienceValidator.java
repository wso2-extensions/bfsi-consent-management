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

package org.wso2.bfsi.identity.extensions.validator.impl;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.bfsi.consent.management.common.exceptions.ConsentManagementException;
import org.wso2.bfsi.identity.extensions.util.IdentityCommonUtils;
import org.wso2.bfsi.identity.extensions.validator.annotation.ValidAudience;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * To validate if the audience is the same as the token issuer of the SP.
 */
public class AudienceValidator implements ConstraintValidator<ValidAudience, Object> {

    private String audienceXpath;
    private String clientIdXPath;
    private static Log log = LogFactory.getLog(AudienceValidator.class);

    @Override
    public void initialize(ValidAudience constraintAnnotation) {

        this.audienceXpath = constraintAnnotation.audience();
        this.clientIdXPath = constraintAnnotation.clientId();
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {

        try {
            final Object audiences = new PropertyUtilsBean().getProperty(object, audienceXpath);
            final String clientId = BeanUtils.getProperty(object, clientIdXPath);

            return validateAudience(audiences, clientId);

        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | NestedNullException e) {
            log.error("Error while resolving validation fields", e);
            return false;
        }
    }

    public boolean validateAudience(Object aud, String clientId) {

        String issuer;

        try {
            issuer = OAuth2Util.getIdTokenIssuer(IdentityCommonUtils.getSpTenantDomain(clientId));
        } catch (IdentityOAuth2Exception | ConsentManagementException e) {
            log.error("Unable to retrieve the ID token issuer per tenant ", e);
            return false;
        }

        return validateAudience(issuer, aud);
    }

    private boolean validateAudience(String currentAudience, Object audiencesObj) {

        if (audiencesObj instanceof List) {
            List audiences = (List) audiencesObj;
            for (Object a : audiences) {
                if (a instanceof String) {
                    String aud = (String) a;
                    if (StringUtils.equals(currentAudience, aud)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

}
