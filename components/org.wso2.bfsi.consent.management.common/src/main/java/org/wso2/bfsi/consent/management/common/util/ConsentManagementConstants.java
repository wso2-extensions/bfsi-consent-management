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
    public static final String REQUEST_VALIDATOR = "Identity.Extensions.RequestObjectValidator";
    public static final String RESPONSE_HANDLER = "Identity.Extensions.ResponseTypeHandler";
    public static final String CLAIM_PROVIDER = "Identity.Extensions.ClaimProvider";
    public static final String ENABLE_TRANSPORT_CERT_AS_HEADER = "Identity.ClientTransportCertAsHeaderEnabled";
    public static final String REMOVE_USER_STORE_DOMAIN_FROM_SUBJECT =
            "Identity.TokenSubject.RemoveUserStoreDomainFromSubject";
    public static final String REMOVE_TENANT_DOMAIN_FROM_SUBJECT =
            "Identity.TokenSubject.RemoveTenantDomainFromSubject";
    public static final String CONSENT_ID_CLAIM_NAME = "Identity.ConsentIDClaimName";

    //Common Constants
    public static final String CONSENT = "consent";
    public static final String CONSENT_TYPE = "consentType";
    public static final String CONSENT_STATUS = "currentStatus";
    public static final String CONSENT_FREQUENCY = "consentFrequency";
    public static final String VALIDITY_PERIOD = "validityPeriod";
    public static final String CREATED_TIMESTAMP = "createdTimestamp";
    public static final String UPDATED_TIMESTAMP = "updatedTimestamp";
    public static final String RECURRING_IND = "recurringIndicator";
    public static final String CONSENT_ATTRIBUTES = "consentAttributes";
    public static final String USER_ID = "userId";
    public static final String CC_CONSENT_ID = "consentId";
    public static final String CONSENT_ID = "ConsentId";
    public static final String CLIENT_ID = "clientId";
    public static final String RECEIPT = "receipt";
    public static final String REGULATORY = "regulatory";
    public static final String CONSENT_RESOURCE = "consentResource";
    public static final String AUTH_RESOURCE = "authResource";
    public static final String IS_ERROR = "isError";
    public static final String TYPE = "type";
    public static final String APPLICATION = "application";
    public static final String SENSITIVE_DATA_MAP = "sensitiveDataMap";
    public static final String LOGGED_IN_USER = "loggedInUser";
    public static final String SP_QUERY_PARAMS = "spQueryParams";
    public static final String SCOPES = "scopeString";
    public static final String REQUEST_HEADERS = "requestHeaders";
    public static final String REQUEST_URI = "redirectURI";
    public static final String META_DATA = "metaDataMap";
    public static final String RESOURCE_PATH = "ResourcePath";
    public static final int STATUS_FOUND = 302;
    public static final String APPROVAL = "approval";
    public static final String COOKIES = "cookies";
    public static final String SESSION_DATA_KEY = "sessionDataKey";
    public static final String SESSION_DATA_KEY_CONSENT = "sessionDataKeyConsent";
    public static final String HAS_APPROVED_ALWAYS = "hasApprovedAlways";
    public static final String USER = "user";
    public static final String LOCATION = "Location";
    public static final String AUTH_ID = "authorizationId";
    public static final String AUTH_STATUS = "authorizationStatus";
    public static final String AUTH_TYPE = "authorizationType";
    public static final String UPDATED_TIME = "updatedTime";
    public static final String AUTH_RESOURCES = "authorizationResources";
    public static final String MAPPING_ID = "mappingId";
    public static final String ACCOUNT_ID = "accountId";
    public static final String PERMISSION = "permission";
    public static final String MAPPING_STATUS = "mappingStatus";
    public static final String MAPPING_RESOURCES = "consentMappingResources";
    public static final String RESOURCE = "resource";
}
