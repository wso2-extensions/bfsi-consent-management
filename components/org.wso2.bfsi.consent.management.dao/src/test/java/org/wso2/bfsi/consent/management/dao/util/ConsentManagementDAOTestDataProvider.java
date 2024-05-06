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

package org.wso2.bfsi.consent.management.dao.util;

import org.testng.annotations.DataProvider;

public class ConsentManagementDAOTestDataProvider {

    @DataProvider(name = "storeConsentDataProvider")
    public Object[][] storeConsentResourceData() {

        /*
         * consentID
         * clientID
         * receipt
         * consentType
         * consentFrequency
         * validityPeriod
         * recurringIndicator
         * currentStatus
         */
        return ConsentMgtDAOTestData.DataProviders.CONSENT_RESOURCE_DATA_HOLDER;
    }

    @DataProvider(name = "updateConsentStatusDataProvider")
    public Object[][] updateConsentStatusData() {

        /*
         * newConsentStatus
         */
        return ConsentMgtDAOTestData.DataProviders.CONSENT_STATUS_UPDATE_DATA_HOLDER;
    }

    @DataProvider(name = "storeAuthorizationDataProvider")
    public Object[][] storeAuthorizationResourceData() {

        /*
         * authorizationID
         * consentID
         * authorizationType
         * userID
         * authorizationStatus
         */
        return ConsentMgtDAOTestData.DataProviders.AUTHORIZATION_RESOURCE_DATA_HOLDER;
    }

    @DataProvider(name = "updateAuthorizationStatusDataProvider")
    public Object[][] updateAuthorizationStatusData() {

        /*
         * newAuthorizationStatus
         */
        return ConsentMgtDAOTestData.DataProviders.CONSENT_AUTHORIZATION_STATUS_UPDATE_DATA_HOLDER;
    }

    @DataProvider(name = "updateAuthorizationUserDataProvider")
    public Object[][] updateAuthorizationUsersData() {

        /*
         * newAuthorizationUser
         */
        return ConsentMgtDAOTestData.DataProviders.CONSENT_AUTHORIZATION_USER_UPDATE_DATA_HOLDER;
    }

    @DataProvider(name = "storeConsentMappingDataProvider")
    public Object[][] storeConsentMappingResourceData() {

        /*
         * accountID
         * permission
         * mappingStatus
         */
        return ConsentMgtDAOTestData.DataProviders.CONSENT_MAPPING_RESOURCE_DATA_HOLDER;
    }

    @DataProvider(name = "updateConsentMappingStatusDataProvider")
    public Object[][] updateConsentMappingStatusData() {

        /*
         * newMappingStatus
         */
        return ConsentMgtDAOTestData.DataProviders.CONSENT_MAPPING_STATUS_UPDATE_DATA_HOLDER;
    }

    @DataProvider(name = "storeConsentAttributesDataProvider")
    public Object[][] storeConsentAttributesData() {

        /*
         * consentAttributesMap
         */
        return ConsentMgtDAOTestData.DataProviders.CONSENT_ATTRIBUTES_DATA_HOLDER;
    }

    @DataProvider(name = "getConsentAttributesDataProvider")
    public Object[][] getConsentAttributesData() {

        /*
         * consentAttributeKeys
         */
        return ConsentMgtDAOTestData.DataProviders.CONSENT_ATTRIBUTES_GET_DATA_HOLDER;
    }

    @DataProvider(name = "storeConsentFileDataProvider")
    public Object[][] storeConsentFileData() {

        /*
         * consentFile
         */
        return ConsentMgtDAOTestData.DataProviders.CONSENT_FILE_DATA_HOLDER;
    }

    @DataProvider(name = "storeConsentStatusAuditRecordDataProvider")
    public Object[][] storeConsentStatusAuditRecordData() {

        /*
         * currentStatus
         * reason
         * actionBy
         * previousStatus
         */
        return ConsentMgtDAOTestData.DataProviders.CONSENT_STATUS_AUDIT_RECORD_DATA_HOLDER;
    }

    @DataProvider(name = "storeConsentHistoryDataProvider")
    public Object[][] storeConsentHistoryData() {

        /*
         * historyID
         * consentID
         * changedAttributes
         * consentType
         * amendedTimestamp
         * amendmentReason
         */
        return ConsentMgtDAOTestData.DataProviders.CONSENT_HISTORY_DATA_HOLDER;
    }
}
