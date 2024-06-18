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

package org.wso2.bfsi.consent.management.common.util;

/**
 * Common Constants class for Consent Management.
 */
public class ConsentManagementConstants {

    public static final String CONFIG_FILE = "/bfsi-consent-management.xml";
    public static final String CARBON_HOME = "carbon.home";
    public static final String OB_CONFIG_QNAME = "http://wso2.org/projects/carbon/bfsi-consent-management.xml";
    public static final String CONSENT_CONFIG_TAG = "ConsentManagement";
    public static final String JDBC_PERSISTENCE_CONFIG = "ConsentManagement.JDBCPersistenceManager.DataSource.Name";
    public static final String DB_VERIFICATION_TIMEOUT =
            "ConsentManagement.JDBCPersistenceManager.ConnectionVerificationTimeout";
    public static final String MANAGE_HANDLER = "ConsentManagement.ManageHandler";
    public static final String AUTHORIZE_STEPS_CONFIG_TAG = "AuthorizeSteps";
    public static final String STEP_CONFIG_TAG = "Step";
    public static final String CONSENT_JWT_PAYLOAD_VALIDATION = "ConsentManagement.Validation.JWTPayloadValidation";
    public static final String SIGNATURE_ALIAS = "ConsentManagement.Validation.RequestSignatureAlias";
    public static final String CONSENT_VALIDATOR = "ConsentManagement.Validation.Validator";
    public static final String ADMIN_HANDLER = "ConsentManagement.AdminHandler";
    public static final String CACHE_MODIFY_EXPIRY = "ConsentManagement.CacheModifiedExpiry";
    public static final String CACHE_ACCESS_EXPIRY = "ConsentManagement.CacheAccessExpiry";
    public static final String PRESERVE_CONSENT = "ConsentManagement.PreserveConsentLink";
    public static final String AUTH_SERVLET_EXTENSION = "AuthenticationWebApp.ServletExtension";
    public static final String CONSENT_API_USERNAME = "ConsentManagement.ConsentAPICredentials.Username";
    public static final String CONSENT_API_PASSWORD = "ConsentManagement.ConsentAPICredentials.Password";
    public static final String MAX_INSTRUCTED_AMOUNT = "ConsentManagement.Payments" +
            ".MaximumInstructedAmount";
    public static final String REQUEST_VALIDATOR = "Identity.Extensions.RequestObjectValidator";
    public static final String RESPONSE_HANDLER = "Identity.Extensions.ResponseTypeHandler";
    public static final String CLAIM_PROVIDER = "Identity.Extensions.ClaimProvider";
    public static final String ENABLE_TRANSPORT_CERT_AS_HEADER = "Identity.ClientTransportCertAsHeaderEnabled";
    public static final String REMOVE_USER_STORE_DOMAIN_FROM_SUBJECT =
            "Identity.TokenSubject.RemoveUserStoreDomainFromSubject";
    public static final String REMOVE_TENANT_DOMAIN_FROM_SUBJECT =
            "Identity.TokenSubject.RemoveTenantDomainFromSubject";
    public static final String CONSENT_ID_CLAIM_NAME = "Identity.ConsentIDClaimName";
}
