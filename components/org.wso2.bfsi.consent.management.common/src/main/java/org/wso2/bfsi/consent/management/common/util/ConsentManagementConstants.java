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
    public static final String JDBC_PERSISTENCE_CONFIG = "ConsentManagement.JDBCPersistenceManager.DataSource.Name";
    public static final String DB_CONNECTION_VERIFICATION_TIMEOUT =
            "ConsentManagement.JDBCPersistenceManager.ConnectionVerificationTimeout";

    public static final String REQUEST_VALIDATOR = "Identity.Extensions.RequestObjectValidator";

    public static final String RESPONSE_HANDLER = "Identity.Extensions.ResponseTypeHandler";
}
