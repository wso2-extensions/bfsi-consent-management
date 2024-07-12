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

package org.wso2.bfsi.consent.management.service.impl;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.bfsi.consent.management.common.exceptions.ConsentManagementException;
import org.wso2.bfsi.consent.management.dao.ConsentCoreDAO;
import org.wso2.bfsi.consent.management.dao.exceptions.ConsentDataDeletionException;
import org.wso2.bfsi.consent.management.dao.exceptions.ConsentDataInsertionException;
import org.wso2.bfsi.consent.management.dao.exceptions.ConsentDataRetrievalException;
import org.wso2.bfsi.consent.management.dao.exceptions.ConsentDataUpdationException;
import org.wso2.bfsi.consent.management.dao.models.AuthorizationResource;
import org.wso2.bfsi.consent.management.dao.models.ConsentAttributes;
import org.wso2.bfsi.consent.management.dao.models.ConsentFile;
import org.wso2.bfsi.consent.management.dao.models.ConsentHistoryResource;
import org.wso2.bfsi.consent.management.dao.models.ConsentMappingResource;
import org.wso2.bfsi.consent.management.dao.models.ConsentResource;
import org.wso2.bfsi.consent.management.dao.models.ConsentStatusAuditRecord;
import org.wso2.bfsi.consent.management.dao.models.DetailedConsentResource;
import org.wso2.bfsi.consent.management.dao.persistence.ConsentStoreInitializer;
import org.wso2.bfsi.consent.management.service.constants.ConsentCoreServiceConstants;
import org.wso2.bfsi.consent.management.service.internal.ConsentManagementDataHolder;
import org.wso2.bfsi.consent.management.service.util.ConsentMgtServiceTestData;
import org.wso2.bfsi.consent.management.service.util.DatabaseUtil;
import org.wso2.bfsi.consent.management.service.util.TokenRevocationUtil;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.oauth2.dto.OAuthRevocationResponseDTO;
import org.wso2.carbon.identity.oauth2.model.AccessTokenDO;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;

/**
 * Test for BFSI consent management core service.
 */
public class ConsentMgtCoreServiceTests {

    private ConsentCoreServiceImpl consentCoreServiceImpl;
    @Mock
    private ConsentCoreDAO mockedConsentCoreDAO;
    private String sampleID;
    @Mock
    private ConsentManagementDataHolder consentManagementDataHolderMock;
    @Mock
    Connection connectionMock;
    @Mock
    ConsentResource consentResourceMock;

    @BeforeClass
    public void initTest() {

        connectionMock = Mockito.mock(Connection.class);
        consentCoreServiceImpl = new ConsentCoreServiceImpl();
        mockedConsentCoreDAO = Mockito.mock(ConsentCoreDAO.class);
        consentManagementDataHolderMock = Mockito.mock(ConsentManagementDataHolder.class);
        consentResourceMock = Mockito.mock(ConsentResource.class);
    }

    @BeforeMethod
    public void mock() {

        sampleID = UUID.randomUUID().toString();
    }

    private void mockStaticClasses(MockedStatic<DatabaseUtil> databaseUtilMockedStatic,
                                   MockedStatic<ConsentStoreInitializer> consentStoreMock,
                                   MockedStatic<ConsentManagementDataHolder> dataHolderMock,
                                   MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock) {

        databaseUtilMockedStatic.when(DatabaseUtil::getDBConnection).thenReturn(connectionMock);

        consentStoreMock.when(ConsentStoreInitializer::getInitializedConsentCoreDAOImpl)
                .thenReturn(mockedConsentCoreDAO);

        dataHolderMock.when(ConsentManagementDataHolder::getInstance).thenReturn(consentManagementDataHolderMock);

        tokenRevocationUtilMock.when(() -> TokenRevocationUtil.getAuthenticatedUser(anyString()))
                .thenReturn(new AuthenticatedUser());
        tokenRevocationUtilMock.when(() -> TokenRevocationUtil.getAccessTokenDOSet(any(), any()))
                .thenReturn(new HashSet<AccessTokenDO>());
        tokenRevocationUtilMock.when(() -> TokenRevocationUtil.revokeTokenByClient(any(), any()))
                .thenReturn(new OAuthRevocationResponseDTO());
    }

    @Test(priority = 2)
    public void testCreateAuthorizableConsent() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doReturn(ConsentMgtServiceTestData.getSampleStoredConsentResource()).when(mockedConsentCoreDAO)
                    .storeConsentResource(any(), any());
            doReturn(ConsentMgtServiceTestData.getSampleTestAuthorizationResource(null, null))
                    .when(mockedConsentCoreDAO).storeAuthorizationResource(any(), any());

            DetailedConsentResource detailedConsentResource =
                    consentCoreServiceImpl.createAuthorizableConsent(ConsentMgtServiceTestData
                                    .getSampleTestConsentResource(), ConsentMgtServiceTestData.SAMPLE_USER_ID,
                            ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                            ConsentMgtServiceTestData.SAMPLE_AUTH_TYPE, true);

            Assert.assertNotNull(detailedConsentResource);
            Assert.assertNotNull(detailedConsentResource.getConsentID());
            Assert.assertNotNull(detailedConsentResource.getClientID());
            Assert.assertNotNull(detailedConsentResource.getReceipt());
            Assert.assertNotNull(detailedConsentResource.getConsentType());
            Assert.assertNotNull(detailedConsentResource.getCurrentStatus());
        }
    }

    @Test (priority = 2)
    public void testCreateAuthorizableConsentWithAttributes() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doReturn(ConsentMgtServiceTestData.getSampleStoredConsentResource()).when(mockedConsentCoreDAO)
                    .storeConsentResource(any(), any());
            doReturn(ConsentMgtServiceTestData.getSampleTestAuthorizationResource(null, null))
                    .when(mockedConsentCoreDAO).storeAuthorizationResource(any(), any());
            doReturn(true).when(mockedConsentCoreDAO).storeConsentAttributes(any(),
                    any());

            DetailedConsentResource detailedConsentResource =
                    consentCoreServiceImpl.createAuthorizableConsent(ConsentMgtServiceTestData
                                    .getSampleStoredTestConsentResourceWithAttributes(),
                            ConsentMgtServiceTestData.SAMPLE_USER_ID,
                            ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                            ConsentMgtServiceTestData.SAMPLE_AUTH_TYPE, true);

            Assert.assertNotNull(detailedConsentResource);
            Assert.assertNotNull(detailedConsentResource.getConsentID());
            Assert.assertNotNull(detailedConsentResource.getClientID());
            Assert.assertNotNull(detailedConsentResource.getReceipt());
            Assert.assertNotNull(detailedConsentResource.getConsentType());
            Assert.assertNotNull(detailedConsentResource.getCurrentStatus());
            Assert.assertNotNull(detailedConsentResource.getConsentAttributes());
        }
    }

    @Test (priority = 2)
    public void testCreateAuthorizableConsentWithoutUserID() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doReturn(ConsentMgtServiceTestData.getSampleStoredConsentResource()).when(mockedConsentCoreDAO)
                    .storeConsentResource(any(), any());
            doReturn(ConsentMgtServiceTestData.getSampleTestAuthorizationResource(null, null))
                    .when(mockedConsentCoreDAO).storeAuthorizationResource(any(), any());

            DetailedConsentResource detailedConsentResource =
                    consentCoreServiceImpl.createAuthorizableConsent(ConsentMgtServiceTestData
                                    .getSampleStoredTestConsentResourceWithAttributes(), null,
                            ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                            ConsentMgtServiceTestData.SAMPLE_AUTH_TYPE, true);

            Assert.assertNotNull(detailedConsentResource);
            Assert.assertNotNull(detailedConsentResource.getConsentID());
            Assert.assertNotNull(detailedConsentResource.getClientID());
            Assert.assertNotNull(detailedConsentResource.getReceipt());
            Assert.assertNotNull(detailedConsentResource.getConsentType());
            Assert.assertNotNull(detailedConsentResource.getCurrentStatus());
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateAuthorizableConsentWithoutClientID() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            ConsentResource consentResource = ConsentMgtServiceTestData.getSampleTestConsentResource();
            consentResource.setClientID(null);

            consentCoreServiceImpl.createAuthorizableConsent(consentResource, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_AUTH_TYPE,
                    false);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateAuthorizableConsentWithoutReceipt() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            ConsentResource consentResource = ConsentMgtServiceTestData.getSampleTestConsentResource();
            consentResource.setReceipt(null);

            consentCoreServiceImpl.createAuthorizableConsent(consentResource, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_AUTH_TYPE,
                    false);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateAuthorizableConsentWithoutConsentType() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            ConsentResource consentResource = ConsentMgtServiceTestData.getSampleTestConsentResource();
            consentResource.setConsentType(null);

            consentCoreServiceImpl.createAuthorizableConsent(consentResource, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_AUTH_TYPE,
                    false);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateAuthorizableConsentWithoutCurrentStatus() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            ConsentResource consentResource = ConsentMgtServiceTestData.getSampleTestConsentResource();
            consentResource.setCurrentStatus(null);

            consentCoreServiceImpl.createAuthorizableConsent(consentResource, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_AUTH_TYPE,
                    false);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateAuthorizableConsentWithImplicitAndNoAuthStatus() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doReturn(ConsentMgtServiceTestData.getSampleStoredConsentResource()).when(mockedConsentCoreDAO)
                    .storeConsentResource(any(), any());
            doReturn(ConsentMgtServiceTestData.getSampleTestAuthorizationResource(null, null))
                    .when(mockedConsentCoreDAO).storeAuthorizationResource(any(), any());

            consentCoreServiceImpl.createAuthorizableConsent(ConsentMgtServiceTestData
                            .getSampleStoredTestConsentResourceWithAttributes(), null, null,
                    ConsentMgtServiceTestData.SAMPLE_AUTH_TYPE, true);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateAuthorizableConsentWithImplicitAndNoAuthType() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doReturn(ConsentMgtServiceTestData.getSampleStoredConsentResource()).when(mockedConsentCoreDAO)
                    .storeConsentResource(any(), any());
            doReturn(ConsentMgtServiceTestData.getSampleTestAuthorizationResource(null, null))
                    .when(mockedConsentCoreDAO).storeAuthorizationResource(any(), any());

            consentCoreServiceImpl.createAuthorizableConsent(ConsentMgtServiceTestData
                            .getSampleStoredTestConsentResourceWithAttributes(), null,
                    ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS, null, true);
        }
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testCreateAuthorizableConsentRollback() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doThrow(ConsentDataInsertionException.class).when(mockedConsentCoreDAO)
                    .storeConsentResource(any(), any());

            consentCoreServiceImpl.createAuthorizableConsent(ConsentMgtServiceTestData
                            .getSampleTestConsentResource(), ConsentMgtServiceTestData.SAMPLE_USER_ID,
                    ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_AUTH_TYPE, true);
        }
    }

    @Test
    public void testCreateExclusiveConsent() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResourcesList())
                    .when(mockedConsentCoreDAO).searchConsents(any(), any(), any(), any(),
                            any(), any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(),
                            Mockito.anyInt());
            doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                    anyString());
            doReturn(ConsentMgtServiceTestData
                    .getSampleTestConsentStatusAuditRecord(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                            ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                    .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                            any(ConsentStatusAuditRecord.class));
            doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
                    any(ArrayList.class), anyString());
            doReturn(ConsentMgtServiceTestData.getSampleTestConsentResource())
                    .when(mockedConsentCoreDAO).storeConsentResource(any(), any(ConsentResource.class));
            doReturn(true).when(mockedConsentCoreDAO).storeConsentAttributes(any(),
                    any(ConsentAttributes.class));
            doReturn(ConsentMgtServiceTestData
                    .getSampleTestAuthorizationResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID, null))
                    .when(mockedConsentCoreDAO).storeAuthorizationResource(any(),
                            any(AuthorizationResource.class));

            DetailedConsentResource exclusiveConsent =
                    consentCoreServiceImpl.createExclusiveConsent(ConsentMgtServiceTestData
                                    .getSampleStoredConsentResource(),
                            ConsentMgtServiceTestData.SAMPLE_USER_ID,
                            ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                            ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE,
                            ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                            ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
            Assert.assertNotNull(exclusiveConsent);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentDataRetrieveError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doThrow(ConsentDataRetrievalException.class)
                    .when(mockedConsentCoreDAO).searchConsents(any(), any(), any(), any(),
                            any(), any(), any(), any(), any(), any());
            doReturn(ConsentMgtServiceTestData.getSampleStoredConsentResource()).when(mockedConsentCoreDAO)
                    .storeConsentResource(any(), any());

            consentCoreServiceImpl.createExclusiveConsent(ConsentMgtServiceTestData.getSampleStoredConsentResource(),
                    ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, false);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentDataUpdateError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResourcesList())
                    .when(mockedConsentCoreDAO).searchConsents(any(), any(), any(), any(),
                            any(), any(), any(), any(), any(), any());
            doThrow(ConsentDataUpdationException.class)
                    .when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                            anyString());
            doReturn(ConsentMgtServiceTestData.getSampleStoredConsentResource()).when(mockedConsentCoreDAO)
                    .storeConsentResource(any(), any());

            consentCoreServiceImpl.createExclusiveConsent(ConsentMgtServiceTestData.getSampleStoredConsentResource(),
                    ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentDataInsertError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResourcesList())
                    .when(mockedConsentCoreDAO).searchConsents(any(), any(), any(), any(),
                            any(), any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(),
                            Mockito.anyInt());
            doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                    anyString());
            doReturn(ConsentMgtServiceTestData.getSampleStoredConsentResource()).when(mockedConsentCoreDAO)
                    .storeConsentResource(any(), any());
            doThrow(ConsentDataInsertionException.class)
                    .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                            any(ConsentStatusAuditRecord.class));

            consentCoreServiceImpl.createExclusiveConsent(ConsentMgtServiceTestData.getSampleStoredConsentResource(),
                    ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentWithoutClientID() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            ConsentResource sampleConsentResource = ConsentMgtServiceTestData.getSampleStoredConsentResource();
            sampleConsentResource.setClientID(null);

            consentCoreServiceImpl.createExclusiveConsent(sampleConsentResource,
                    ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentWithoutReceipt() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            ConsentResource sampleConsentResource = ConsentMgtServiceTestData.getSampleStoredConsentResource();
            sampleConsentResource.setReceipt(null);

            consentCoreServiceImpl.createExclusiveConsent(sampleConsentResource,
                    ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentWithoutConsentType() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            ConsentResource sampleConsentResource = ConsentMgtServiceTestData.getSampleStoredConsentResource();
            sampleConsentResource.setConsentType(null);

            consentCoreServiceImpl.createExclusiveConsent(sampleConsentResource,
                    ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentWithoutConsentStatus() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            ConsentResource sampleConsentResource = ConsentMgtServiceTestData.getSampleStoredConsentResource();
            sampleConsentResource.setCurrentStatus(null);

            consentCoreServiceImpl.createExclusiveConsent(sampleConsentResource,
                    ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentWithoutUserID() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.createExclusiveConsent(ConsentMgtServiceTestData.getSampleStoredConsentResource(),
                    null, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentWithoutAuthStatus() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.createExclusiveConsent(ConsentMgtServiceTestData.getSampleStoredConsentResource(),
                    ConsentMgtServiceTestData.SAMPLE_USER_ID, null,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentWithoutAuthType() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.createExclusiveConsent(ConsentMgtServiceTestData.getSampleStoredConsentResource(),
                    ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                    null, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
        }
    }

    @Test
    public void testCreateExclusiveConsentWithImplicitAuthFalse() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doReturn(ConsentMgtServiceTestData.getSampleTestConsentResource())
                    .when(mockedConsentCoreDAO).storeConsentResource(any(), any());
            doReturn(true).when(mockedConsentCoreDAO).storeConsentAttributes(any(), any());
            doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(), anyString());
            doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(), any(), anyString());
            doReturn(ConsentMgtServiceTestData.getSampleStoredTestAuthorizationResource())
                    .when(mockedConsentCoreDAO).storeAuthorizationResource(any(), any());
            ArrayList<DetailedConsentResource> detailedConsentResources = new ArrayList<>();
            detailedConsentResources.add(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource());

            doReturn(detailedConsentResources).when(mockedConsentCoreDAO)
                    .searchConsents(any(), any(), any(), any(), any(),
                            any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt());
            doReturn(ConsentMgtServiceTestData
                    .getSampleTestConsentStatusAuditRecord(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                            ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                    .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                            any(ConsentStatusAuditRecord.class));

            DetailedConsentResource consentResource = consentCoreServiceImpl
                    .createExclusiveConsent(ConsentMgtServiceTestData.getSampleStoredConsentResource(),
                            ConsentMgtServiceTestData.SAMPLE_USER_ID,
                            ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                            null, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                            ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, false);
            Assert.assertNotNull(consentResource);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentWithoutApplicableExistingConsentStatus() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.createExclusiveConsent(ConsentMgtServiceTestData.getSampleStoredConsentResource(),
                    ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, null,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentWithoutNewExistingConsentStatus() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.createExclusiveConsent(ConsentMgtServiceTestData.getSampleStoredConsentResource(),
                    ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    null, true);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentWithoutNewCurrentConsentStatus() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.createExclusiveConsent(ConsentMgtServiceTestData.getSampleStoredConsentResource(),
                    ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    null, true);
        }
    }

    @Test
    public void testGetConsent() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doReturn(ConsentMgtServiceTestData.getSampleStoredConsentResource()).when(mockedConsentCoreDAO)
                    .getConsentResource(any(), anyString());

            // Get consent
            ConsentResource retrievedConsentResource = consentCoreServiceImpl.getConsent(ConsentMgtServiceTestData
                    .getSampleStoredConsentResource().getConsentID(), false);

            Assert.assertNotNull(retrievedConsentResource);
        }
    }

    @Test
    public void testGetConsentWithAttributes() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentResourceWithAttributes())
                    .when(mockedConsentCoreDAO).getConsentResourceWithAttributes(any(), anyString());

            // Get consent
            ConsentResource retrievedConsentResource = consentCoreServiceImpl.getConsent(ConsentMgtServiceTestData
                    .getSampleStoredConsentResource().getConsentID(), true);

            Assert.assertNotNull(retrievedConsentResource);
            Assert.assertNotNull(retrievedConsentResource.getConsentAttributes());
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentRollBackWhenRetrieve() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                    .getConsentResource(any(), anyString());

            // Get consent
            consentCoreServiceImpl.getConsent(ConsentMgtServiceTestData
                            .getSampleStoredConsentResource().getConsentID(),
                    false);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentWithoutConsentID() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.getConsent(null, false);
        }
    }

    @Test
    public void testGetDetailedConsent() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                    .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());

            // Get consent
            DetailedConsentResource retrievedConsentResource = consentCoreServiceImpl
                    .getDetailedConsent(ConsentMgtServiceTestData.getSampleStoredConsentResource().getConsentID());

            Assert.assertNotNull(retrievedConsentResource);
        }
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testGetDetailedConsentWithoutConsentID() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            // Get consent
            consentCoreServiceImpl.getDetailedConsent(null);
        }
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testGetDetailedConsentWithDataRetrievalException() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                    .getDetailedConsentResource(any(), anyString());

            // Get consent
            consentCoreServiceImpl.getDetailedConsent(ConsentMgtServiceTestData
                    .getSampleStoredConsentResource().getConsentID());
        }
    }

    @Test
    public void testCreateConsentFile() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doReturn(ConsentMgtServiceTestData
                    .getSampleTestConsentResource(ConsentMgtServiceTestData.AWAITING_UPLOAD_STATUS))
                    .when(mockedConsentCoreDAO).getConsentResource(any(), anyString());
            doReturn(true).when(mockedConsentCoreDAO).storeConsentFile(any(), any());
            doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                    anyString());

            consentCoreServiceImpl.createConsentFile(ConsentMgtServiceTestData
                            .getSampleConsentFileObject(ConsentMgtServiceTestData.SAMPLE_CONSENT_FILE),
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                    ConsentMgtServiceTestData.AWAITING_UPLOAD_STATUS);
        }
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentFileErrorWhenRetrieval() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                    .getConsentResource(any(), anyString());

            consentCoreServiceImpl.createConsentFile(ConsentMgtServiceTestData
                            .getSampleConsentFileObject(ConsentMgtServiceTestData.SAMPLE_CONSENT_FILE),
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                    ConsentMgtServiceTestData.AWAITING_UPLOAD_STATUS);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentFileRollBackWhenCreation() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            ConsentResource storedConsentResource = ConsentMgtServiceTestData
                    .getSampleStoredConsentResource(ConsentMgtServiceTestData.AWAITING_UPLOAD_STATUS);

            doReturn(storedConsentResource).when(mockedConsentCoreDAO)
                    .getConsentResource(any(), anyString());
            doThrow(ConsentDataInsertionException.class).when(mockedConsentCoreDAO)
                    .storeConsentFile(any(), any());

            consentCoreServiceImpl.createConsentFile(ConsentMgtServiceTestData
                            .getSampleConsentFileObject(ConsentMgtServiceTestData.SAMPLE_CONSENT_FILE),
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                    ConsentMgtServiceTestData.AWAITING_UPLOAD_STATUS);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentFileRollBackWhenUpdating() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            ConsentResource storedConsentResource = ConsentMgtServiceTestData
                    .getSampleStoredConsentResource(ConsentMgtServiceTestData.AWAITING_UPLOAD_STATUS);

            doReturn(storedConsentResource).when(mockedConsentCoreDAO)
                    .getConsentResource(any(), anyString());
            doThrow(ConsentDataUpdationException.class).when(mockedConsentCoreDAO)
                    .updateConsentStatus(any(), anyString(), anyString());

            consentCoreServiceImpl.createConsentFile(ConsentMgtServiceTestData
                            .getSampleConsentFileObject(ConsentMgtServiceTestData.SAMPLE_CONSENT_FILE),
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                    storedConsentResource.getCurrentStatus());
        }
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentFileWithInvalidStatus()
            throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            ConsentResource consentResource = ConsentMgtServiceTestData.getSampleTestConsentResource();
            consentResource.setCurrentStatus(ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);

            doReturn(consentResource).when(mockedConsentCoreDAO).getConsentResource(any(),
                    anyString());

            // Create consent file
            consentCoreServiceImpl.createConsentFile(ConsentMgtServiceTestData
                            .getSampleConsentFileObject(ConsentMgtServiceTestData.SAMPLE_CONSENT_FILE),
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                    ConsentMgtServiceTestData.AWAITING_UPLOAD_STATUS);
        }
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentFileWithoutFileContent() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.createConsentFile(ConsentMgtServiceTestData
                            .getSampleConsentFileObject(null), ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.AWAITING_UPLOAD_STATUS);
        }
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentFileWithoutConsentID() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            ConsentFile sampleConsentFile =
                    ConsentMgtServiceTestData.getSampleConsentFileObject(ConsentMgtServiceTestData.SAMPLE_CONSENT_FILE);

            sampleConsentFile.setConsentID(null);
            consentCoreServiceImpl.createConsentFile(sampleConsentFile, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.AWAITING_UPLOAD_STATUS);
        }
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentFileWithoutNewConsentStatus()
            throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.createConsentFile(ConsentMgtServiceTestData
                            .getSampleConsentFileObject(ConsentMgtServiceTestData.SAMPLE_CONSENT_FILE),
                    null, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                    ConsentMgtServiceTestData.AWAITING_UPLOAD_STATUS);
        }
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentFileWithoutApplicableStatusForFileUpload() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.createConsentFile(ConsentMgtServiceTestData
                            .getSampleConsentFileObject(ConsentMgtServiceTestData.SAMPLE_CONSENT_FILE),
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                    null);
        }
    }

    @Test
    public void testGetConsentFileConsentData() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doReturn(new ConsentFile()).when(mockedConsentCoreDAO).getConsentFile(any(), anyString());
            ConsentFile consentFile = consentCoreServiceImpl
                    .getConsentFile("3d22259e-942c-46b8-8f75-a608c677a6e6");
            Assert.assertNotNull(consentFile);
        }
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testGetConsentFileWithoutConsentId() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.getConsentFile(null);
        }
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testGetConsentFileWithDataRetrievalError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                    .getConsentFile(any(), anyString());
            ConsentFile consentFile = consentCoreServiceImpl
                    .getConsentFile("3d22259e-942c-46b8-8f75-a608c677a6e6");
            Assert.assertNotNull(consentFile);
        }
    }

    @Test
    public void testCreateConsentAuthorization() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            AuthorizationResource sampleAuthorizationResource =
                    ConsentMgtServiceTestData.getSampleTestAuthorizationResource(sampleID, null);

            doReturn(ConsentMgtServiceTestData.getSampleStoredTestAuthorizationResource())
                    .when(mockedConsentCoreDAO).storeAuthorizationResource(any(), any());

            //Create a consent authorization resource
            AuthorizationResource storedAuthorizationResource =
                    consentCoreServiceImpl.createConsentAuthorization(sampleAuthorizationResource);

            Assert.assertNotNull(storedAuthorizationResource);
            Assert.assertNotNull(storedAuthorizationResource.getAuthorizationID());
            Assert.assertNotNull(storedAuthorizationResource.getConsentID());
            Assert.assertNotNull(storedAuthorizationResource.getAuthorizationType());
            Assert.assertNotNull(storedAuthorizationResource.getUserID());
            Assert.assertNotNull(storedAuthorizationResource.getAuthorizationStatus());
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentAuthorizationRollbackWhenCreation() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            AuthorizationResource sampleAuthorizationResource =
                    ConsentMgtServiceTestData.getSampleTestAuthorizationResource(sampleID, null);

            doThrow(ConsentDataInsertionException.class).when(mockedConsentCoreDAO)
                    .storeAuthorizationResource(any(), any());

            // Get consent
            consentCoreServiceImpl.createConsentAuthorization(sampleAuthorizationResource);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentAuthorizationWithoutConsentID() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            AuthorizationResource sampleAuthorizationResource =
                    ConsentMgtServiceTestData.getSampleTestAuthorizationResource(null, null);
            sampleAuthorizationResource.setConsentID(null);

            //Create a consent authorization resource
            consentCoreServiceImpl.createConsentAuthorization(sampleAuthorizationResource);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentAuthorizationWithoutAuthorizationStatus() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            AuthorizationResource sampleAuthorizationResource =
                    ConsentMgtServiceTestData.getSampleTestAuthorizationResource(sampleID, null);

            // Explicitly setting authorization status to null
            sampleAuthorizationResource.setAuthorizationStatus(null);

            //Create a consent authorization resource
            consentCoreServiceImpl.createConsentAuthorization(sampleAuthorizationResource);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentAuthorizationWithoutAuthorizationType() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            AuthorizationResource sampleAuthorizationResource =
                    ConsentMgtServiceTestData.getSampleTestAuthorizationResource(sampleID, null);
            sampleAuthorizationResource.setAuthorizationType(null);

            //Create a consent authorization resource
            consentCoreServiceImpl.createConsentAuthorization(sampleAuthorizationResource);
        }
    }

    @Test
    public void testGetAuthorizationResource() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doReturn(ConsentMgtServiceTestData.getSampleStoredTestAuthorizationResource())
                    .when(mockedConsentCoreDAO).getAuthorizationResource(any(), anyString());
            AuthorizationResource authorizationResource =
                    consentCoreServiceImpl.getAuthorizationResource(ConsentMgtServiceTestData
                            .getSampleStoredTestAuthorizationResource().getAuthorizationID());
            Assert.assertNotNull(authorizationResource);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetAuthorizationResourceWithoutAuthID() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.getAuthorizationResource(null);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetAuthorizationResourceDataRetrieveError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doThrow(ConsentDataRetrievalException.class)
                    .when(mockedConsentCoreDAO).getAuthorizationResource(any(), anyString());
            consentCoreServiceImpl.getAuthorizationResource(ConsentMgtServiceTestData
                    .getSampleStoredTestAuthorizationResource().getAuthorizationID());
        }
    }

    @Test
    public void testSearchAuthorizationsWithConsentID() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            ArrayList<String> consentIDs = new ArrayList<>();
            consentIDs.add(UUID.randomUUID().toString());

            doReturn(ConsentMgtServiceTestData
                            .getSampleAuthorizationResourcesList(consentIDs))
                    .when(mockedConsentCoreDAO).searchConsentAuthorizations(any(), anyString(),
                            anyString());
            ArrayList<AuthorizationResource> retrievedAuthorizations =
                    consentCoreServiceImpl.searchAuthorizations(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID);
            Assert.assertNotNull(retrievedAuthorizations);
        }
    }

    @Test
    public void testSearchAuthorizationsWithUserID() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            ArrayList<String> consentIDs = new ArrayList<>();
            consentIDs.add(UUID.randomUUID().toString());

            doReturn(ConsentMgtServiceTestData
                    .getSampleAuthorizationResourcesList(consentIDs))
                    .when(mockedConsentCoreDAO).searchConsentAuthorizations(any(), anyString(),
                            anyString());
            ArrayList<AuthorizationResource> retrievedAuthorizations =
                    consentCoreServiceImpl.searchAuthorizationsForUser(ConsentMgtServiceTestData.SAMPLE_USER_ID);
            Assert.assertNotNull(retrievedAuthorizations);
        }
    }

    @Test
    public void testSearchAuthorizations() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            ArrayList<String> consentIDs = new ArrayList<>();
            consentIDs.add(UUID.randomUUID().toString());

            doReturn(ConsentMgtServiceTestData
                    .getSampleAuthorizationResourcesList(consentIDs))
                    .when(mockedConsentCoreDAO).searchConsentAuthorizations(any(), anyString(),
                            anyString());
            ArrayList<AuthorizationResource> retrievedAuthorizations =
                    consentCoreServiceImpl.searchAuthorizations(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                            ConsentMgtServiceTestData.SAMPLE_USER_ID);
            Assert.assertNotNull(retrievedAuthorizations);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testSearchAuthorizationsDataRetrieveError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doThrow(ConsentDataRetrievalException.class)
                    .when(mockedConsentCoreDAO).searchConsentAuthorizations(any(), anyString(),
                            anyString());

            consentCoreServiceImpl.searchAuthorizations(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                    ConsentMgtServiceTestData.SAMPLE_USER_ID);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testUpdateAuthorizationStatusWithoutAuthId() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.updateAuthorizationStatus(null,
                    ConsentMgtServiceTestData.SAMPLE_CONSUMED_STATUS);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testUpdateAuthorizationStatusWithoutNewAuthStatus() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.updateAuthorizationStatus(ConsentMgtServiceTestData.CONSENT_ID,
                    null);
        }
    }

    @Test
    public void testUpdateAuthorizationStatus() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doNothing().when(mockedConsentCoreDAO).updateAuthorizationStatus(any(), anyString(),
                    anyString());
            doReturn(ConsentMgtServiceTestData.getSampleStoredTestAuthorizationResource())
                    .when(mockedConsentCoreDAO).getAuthorizationResource(any(), anyString());

            consentCoreServiceImpl.updateAuthorizationStatus(ConsentMgtServiceTestData.CONSENT_ID,
                    ConsentMgtServiceTestData.SAMPLE_CONSUMED_STATUS);
        }
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testUpdateAuthorizationStatusWithDataUpdateError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doThrow(ConsentDataUpdationException.class).when(mockedConsentCoreDAO)
                    .updateAuthorizationStatus(any(), anyString(), anyString());

            consentCoreServiceImpl.updateAuthorizationStatus(ConsentMgtServiceTestData.CONSENT_ID,
                    ConsentMgtServiceTestData.SAMPLE_CONSUMED_STATUS);
        }
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testUpdateAuthorizationStatusWithDataRetrievalError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doNothing().when(mockedConsentCoreDAO).updateAuthorizationStatus(any(), anyString(),
                    anyString());
            doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                    .getAuthorizationResource(any(), anyString());

            consentCoreServiceImpl.updateAuthorizationStatus(ConsentMgtServiceTestData.CONSENT_ID,
                    ConsentMgtServiceTestData.SAMPLE_CONSUMED_STATUS);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testUpdateAuthorizationUserWithoutAuthorizationID() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.updateAuthorizationUser(null,
                    ConsentMgtServiceTestData.SAMPLE_USER_ID);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testUpdateAuthorizationUserWithoutUserID() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.updateAuthorizationUser(ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_ID_1,
                    null);
        }
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testUpdateAuthorizationUserWithDataUpdateError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doThrow(ConsentDataUpdationException.class).when(mockedConsentCoreDAO)
                    .updateAuthorizationUser(any(), anyString(), anyString());

            consentCoreServiceImpl.updateAuthorizationUser(ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_ID_1,
                    ConsentMgtServiceTestData.SAMPLE_USER_ID);
        }
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testUpdateAuthorizationUserWithDataRetrieveError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doNothing().when(mockedConsentCoreDAO).updateAuthorizationUser(any(),
                    anyString(), anyString());
            doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                    .getAuthorizationResource(any(), anyString());

            consentCoreServiceImpl.updateAuthorizationUser(ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_ID_1,
                    ConsentMgtServiceTestData.SAMPLE_USER_ID);
        }
    }

    @Test
    public void testUpdateAuthorizationUser() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doNothing().when(mockedConsentCoreDAO).updateAuthorizationUser(any(), anyString(),
                    anyString());
            doReturn(ConsentMgtServiceTestData.getSampleStoredTestAuthorizationResource())
                    .when(mockedConsentCoreDAO).getAuthorizationResource(any(), anyString());

            consentCoreServiceImpl.updateAuthorizationUser(ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_ID_1,
                    ConsentMgtServiceTestData.SAMPLE_USER_ID);
        }
    }

    @Test
    public void testBindUserAccountsToConsentWithAccountIdList() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doNothing().when(mockedConsentCoreDAO).updateAuthorizationUser(any(), anyString(),
                    anyString());
            doNothing().when(mockedConsentCoreDAO).updateAuthorizationStatus(any(), anyString(),
                    anyString());
            doReturn(ConsentMgtServiceTestData
                    .getSampleTestConsentMappingResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID))
                    .when(mockedConsentCoreDAO).storeConsentMappingResource(any(),
                            any(ConsentMappingResource.class));
            doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                    anyString());
            doReturn(ConsentMgtServiceTestData
                    .getSampleTestConsentStatusAuditRecord(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                            ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                    .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                            any(ConsentStatusAuditRecord.class));

            Assert.assertTrue(consentCoreServiceImpl
                    .bindUserAccountsToConsent(ConsentMgtServiceTestData.getSampleStoredConsentResource(),
                            ConsentMgtServiceTestData.SAMPLE_USER_ID, "authID",
                            ConsentMgtServiceTestData.SAMPLE_ACCOUNT_ID_LIST,
                            ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                            ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS));
        }
    }

    @Test
    public void testBindUserAccountsToConsent() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doNothing().when(mockedConsentCoreDAO).updateAuthorizationUser(any(), anyString(),
                    anyString());
            doNothing().when(mockedConsentCoreDAO).updateAuthorizationStatus(any(), anyString(),
                    anyString());
            doReturn(ConsentMgtServiceTestData
                    .getSampleTestConsentMappingResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID))
                    .when(mockedConsentCoreDAO).storeConsentMappingResource(any(),
                            any(ConsentMappingResource.class));
            doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                    anyString());
            doReturn(ConsentMgtServiceTestData
                    .getSampleTestConsentStatusAuditRecord(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                            ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                    .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                            any(ConsentStatusAuditRecord.class));

            Assert.assertTrue(consentCoreServiceImpl
                    .bindUserAccountsToConsent(ConsentMgtServiceTestData.getSampleStoredConsentResource(),
                            ConsentMgtServiceTestData.SAMPLE_USER_ID, "authID",
                            ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                            ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                            ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS));
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testBindUserAccountsToConsentWithoutNewCurrentConsentStatus() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.bindUserAccountsToConsent(ConsentMgtServiceTestData
                            .getSampleStoredConsentResource(),
                    ConsentMgtServiceTestData.SAMPLE_USER_ID, "authID",
                    ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                    ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS, null);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testBindUserAccountsToConsentWithoutConsentID() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);
        }
        ConsentResource consentResource = ConsentMgtServiceTestData.getSampleStoredConsentResource();
        consentResource.setConsentID(null);

        consentCoreServiceImpl.bindUserAccountsToConsent(consentResource,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, "authID",
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testBindUserAccountsToConsentWithoutClientID() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            ConsentResource consentResource = ConsentMgtServiceTestData.getSampleStoredConsentResource();
            consentResource.setClientID(null);

            consentCoreServiceImpl.bindUserAccountsToConsent(consentResource,
                    ConsentMgtServiceTestData.SAMPLE_USER_ID, "authID",
                    ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                    ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testBindUserAccountsToConsentWithoutConsentType() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            ConsentResource consentResource = ConsentMgtServiceTestData.getSampleStoredConsentResource();
            consentResource.setConsentType(null);

            consentCoreServiceImpl.bindUserAccountsToConsent(consentResource,
                    ConsentMgtServiceTestData.SAMPLE_USER_ID, "authID",
                    ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                    ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testBindUserAccountsToConsentWithoutUserID() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.bindUserAccountsToConsent(ConsentMgtServiceTestData
                            .getSampleStoredConsentResource(),
                    null, "authID", ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                    ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testBindUserAccountsToConsentWithoutAuthID() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.bindUserAccountsToConsent(ConsentMgtServiceTestData
                            .getSampleStoredConsentResource(),
                    ConsentMgtServiceTestData.SAMPLE_USER_ID, null,
                    ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                    ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testBindUserAccountsToConsentWithoutNewAuthStatus() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.bindUserAccountsToConsent(ConsentMgtServiceTestData
                            .getSampleStoredConsentResource(),
                    ConsentMgtServiceTestData.SAMPLE_USER_ID, "authID",
                    ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP, null,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testBindUserAccountsToConsentWithEmptyAccountsAndPermissionsMap() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            Assert.assertTrue(consentCoreServiceImpl
                    .bindUserAccountsToConsent(ConsentMgtServiceTestData.getSampleStoredConsentResource(),
                            ConsentMgtServiceTestData.SAMPLE_USER_ID, "authID", new HashMap<>(),
                            ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                            ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS));
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testBindUserAccountsToConsentDataUpdateError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doThrow(ConsentDataUpdationException.class)
                    .when(mockedConsentCoreDAO).updateAuthorizationUser(any(), anyString(),
                            anyString());
            consentCoreServiceImpl.bindUserAccountsToConsent(ConsentMgtServiceTestData
                            .getSampleStoredConsentResource(),
                    ConsentMgtServiceTestData.SAMPLE_USER_ID, "authID",
                    ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                    ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testBindUserAccountsToConsentDataInsertError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doThrow(ConsentDataInsertionException.class)
                    .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                            any(ConsentStatusAuditRecord.class));
            consentCoreServiceImpl.bindUserAccountsToConsent(ConsentMgtServiceTestData
                            .getSampleStoredConsentResource(),
                    ConsentMgtServiceTestData.SAMPLE_USER_ID, "authID",
                    ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                    ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
        }
    }

    @Test
    public void testUpdateConsentStatus() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                    .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());

            consentCoreServiceImpl.updateConsentStatus(ConsentMgtServiceTestData.CONSENT_ID,
                    ConsentMgtServiceTestData.SAMPLE_CONSUMED_STATUS);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testUpdateConsentStatusDataRetrievalError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doThrow(ConsentDataRetrievalException.class)
                    .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());

            consentCoreServiceImpl.updateConsentStatus(ConsentMgtServiceTestData.CONSENT_ID,
                    ConsentMgtServiceTestData.SAMPLE_CONSUMED_STATUS);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testUpdateConsentStatusDataUpdateError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                    .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());
            doThrow(ConsentDataUpdationException.class)
                    .when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                            anyString());

            consentCoreServiceImpl.updateConsentStatus(ConsentMgtServiceTestData.CONSENT_ID,
                    ConsentMgtServiceTestData.SAMPLE_CONSUMED_STATUS);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testUpdateConsentStatusDataInsertError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                    .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());
            doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                    anyString());
            doThrow(ConsentDataInsertionException.class)
                    .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                            any(ConsentStatusAuditRecord.class));

            consentCoreServiceImpl.updateConsentStatus(ConsentMgtServiceTestData.CONSENT_ID,
                    ConsentMgtServiceTestData.SAMPLE_CONSUMED_STATUS);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testUpdateConsentStatusWithoutConsentId() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.updateConsentStatus(null,
                    ConsentMgtServiceTestData.SAMPLE_CONSUMED_STATUS);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testUpdateConsentStatusWithoutConsentStatus() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.updateConsentStatus(ConsentMgtServiceTestData.CONSENT_ID,
                    null);
        }
    }

    @Test
    public void testCreateConsentAccountMapping() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            AuthorizationResource storedAuthorizationResource =
                    ConsentMgtServiceTestData.getSampleStoredTestAuthorizationResource();

            ConsentMappingResource storedConsentMappingResource =
                    ConsentMgtServiceTestData.getSampleStoredTestConsentMappingResource(sampleID);

            doReturn(storedConsentMappingResource).when(mockedConsentCoreDAO)
                    .storeConsentMappingResource(any(), any());

            ArrayList<ConsentMappingResource> storedConsentMappingResources = consentCoreServiceImpl
                    .createConsentAccountMappings(storedAuthorizationResource.getAuthorizationID(),
                            ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP);

            Assert.assertNotNull(storedConsentMappingResources);
            for (ConsentMappingResource resource : storedConsentMappingResources) {
                Assert.assertNotNull(resource.getAccountID());
                Assert.assertNotNull(resource.getPermission());
                Assert.assertNotNull(resource.getAuthorizationID());
                Assert.assertEquals(resource.getMappingStatus(), ConsentCoreServiceConstants.ACTIVE_MAPPING_STATUS);
            }
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentAccountMappingRollBackWhenCreation() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            AuthorizationResource storedAuthorizationResource =
                    ConsentMgtServiceTestData.getSampleStoredTestAuthorizationResource();

            doThrow(ConsentDataInsertionException.class).when(mockedConsentCoreDAO)
                    .storeConsentMappingResource(any(), any());

            consentCoreServiceImpl.createConsentAccountMappings(storedAuthorizationResource.getAuthorizationID(),
                    ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentAccountMappingWithoutAuthID() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.createConsentAccountMappings(null,
                    ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentAccountMappingWithoutAccountAndPermissionsMap() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.createConsentAccountMappings(sampleID, new HashMap<>());
        }
    }

    @Test
    public void testDeactivateAccountMappings() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
                    any(), any());
            Assert.assertTrue(consentCoreServiceImpl
                    .deactivateAccountMappings(ConsentMgtServiceTestData.UNMATCHED_MAPPING_IDS));
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testDeactivateAccountMappingsWithEmptyMappingIDList() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.deactivateAccountMappings(new ArrayList<>());
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testDeactivateAccountMappingsRollback() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doThrow(ConsentDataUpdationException.class).when(mockedConsentCoreDAO)
                    .updateConsentMappingStatus(any(), any(), any());
            consentCoreServiceImpl.deactivateAccountMappings(ConsentMgtServiceTestData.UNMATCHED_MAPPING_IDS);
        }
    }

    @Test
    public void testUpdateAccountMappingStatus() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doNothing().when(mockedConsentCoreDAO)
                    .updateConsentMappingStatus(any(), any(), any());

            consentCoreServiceImpl.updateAccountMappingStatus(ConsentMgtServiceTestData.MAPPING_IDS_LIST,
                    ConsentMgtServiceTestData.SAMPLE_MAPPING_STATUS);
        }
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testUpdateAccountMappingStatusWithoutMappingIds() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doNothing().when(mockedConsentCoreDAO)
                    .updateConsentMappingStatus(any(), any(), any());

            consentCoreServiceImpl.updateAccountMappingStatus(new ArrayList<>(),
                    ConsentMgtServiceTestData.SAMPLE_MAPPING_STATUS);
        }
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testUpdateAccountMappingStatusDataUpdateError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doThrow(ConsentDataUpdationException.class).when(mockedConsentCoreDAO)
                    .updateConsentMappingStatus(any(), any(), any());

            consentCoreServiceImpl.updateAccountMappingStatus(ConsentMgtServiceTestData.MAPPING_IDS_LIST,
                    ConsentMgtServiceTestData.SAMPLE_MAPPING_STATUS);
        }
    }

    @Test
    public void testRevokeConsent() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            DetailedConsentResource retrievedDetailedConsentResource =
                    ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

            doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                    .getDetailedConsentResource(any(), any());
            doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                    anyString());
            doReturn(ConsentMgtServiceTestData.getSampleTestConsentStatusAuditRecord(
                    retrievedDetailedConsentResource.getConsentID(),
                    retrievedDetailedConsentResource.getCurrentStatus()))
                    .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                            any(ConsentStatusAuditRecord.class));
            doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
                    any(), anyString());

            boolean isConsentRevoked = consentCoreServiceImpl.revokeConsentWithReason(
                    ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_USER_ID, false,
                    ConsentCoreServiceConstants.CONSENT_REVOKE_REASON);

            Assert.assertTrue(isConsentRevoked);
        }
    }

    @Test
    public void testRevokeConsentWithReason() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            DetailedConsentResource retrievedDetailedConsentResource =
                    ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

            doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                    .getDetailedConsentResource(any(), any());
            doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                    anyString());
            doReturn(ConsentMgtServiceTestData.getSampleTestConsentStatusAuditRecord(
                    retrievedDetailedConsentResource.getConsentID(),
                    retrievedDetailedConsentResource.getCurrentStatus()))
                    .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                            any(ConsentStatusAuditRecord.class));
            doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
                    any(), anyString());

            boolean isConsentRevoked = consentCoreServiceImpl.revokeConsentWithReason(
                    ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentCoreServiceConstants.CONSENT_REVOKE_REASON);

            Assert.assertTrue(isConsentRevoked);
        }
    }

    @Test
    public void testRevokeConsentWithUserId() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            DetailedConsentResource retrievedDetailedConsentResource =
                    ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

            doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                    .getDetailedConsentResource(any(), any());
            doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                    anyString());
            doReturn(ConsentMgtServiceTestData.getSampleTestConsentStatusAuditRecord(
                    retrievedDetailedConsentResource.getConsentID(),
                    retrievedDetailedConsentResource.getCurrentStatus()))
                    .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                            any(ConsentStatusAuditRecord.class));
            doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
                    any(), anyString());

            boolean isConsentRevoked = consentCoreServiceImpl.revokeConsentWithReason(
                    ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentCoreServiceConstants.CONSENT_REVOKE_REASON);

            Assert.assertTrue(isConsentRevoked);
        }
    }

    @Test
    public void testRevokeConsentAndTokens() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            DetailedConsentResource retrievedDetailedConsentResource =
                    ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

            doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                    .getDetailedConsentResource(any(), any());
            doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                    anyString());

            doReturn(ConsentMgtServiceTestData.getSampleTestConsentStatusAuditRecord(
                    retrievedDetailedConsentResource.getConsentID(),
                    retrievedDetailedConsentResource.getCurrentStatus()))
                    .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                            any(ConsentStatusAuditRecord.class));
            doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
                    any(), anyString());

            boolean isConsentRevoked = consentCoreServiceImpl.revokeConsent(
                    ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                    true);

            Assert.assertTrue(isConsentRevoked);
        }
    }

    @Test
    public void testRevokeConsentAndTokensTokenRevokeError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            DetailedConsentResource retrievedDetailedConsentResource =
                    ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

            doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                    .getDetailedConsentResource(any(), any());
            doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                    anyString());

            doReturn(ConsentMgtServiceTestData.getSampleTestConsentStatusAuditRecord(
                    retrievedDetailedConsentResource.getConsentID(),
                    retrievedDetailedConsentResource.getCurrentStatus()))
                    .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                            any(ConsentStatusAuditRecord.class));
            doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
                    any(), anyString());
            try {
                boolean isConsentRevoked = consentCoreServiceImpl.revokeConsentWithReason(
                        ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                        ConsentMgtServiceTestData.SAMPLE_USER_ID, true,
                        ConsentCoreServiceConstants.CONSENT_REVOKE_REASON);
                Assert.assertTrue(isConsentRevoked);
            } catch (Exception e) {
                Assert.assertTrue(e instanceof ConsentManagementException);
            }
        }
    }

    @Test
    public void testRevokeConsentWithoutConsentAttributes() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            DetailedConsentResource retrievedDetailedConsentResource =
                    ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();
            retrievedDetailedConsentResource.setConsentAttributes(null);

            doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                    .getDetailedConsentResource(any(), any());
            doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                    anyString());
            doReturn(ConsentMgtServiceTestData.getSampleTestConsentStatusAuditRecord(
                    retrievedDetailedConsentResource.getConsentID(),
                    retrievedDetailedConsentResource.getCurrentStatus()))
                    .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                            any(ConsentStatusAuditRecord.class));
            doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
                    any(), anyString());

            boolean isConsentRevoked = consentCoreServiceImpl.revokeConsent(
                    ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                    false);

            Assert.assertTrue(isConsentRevoked);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeConsentWithoutConsentID() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.revokeConsentWithReason(null,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                    false, ConsentCoreServiceConstants.CONSENT_REVOKE_REASON);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeConsentWithoutNewConsentStatus() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.revokeConsent(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                    null, ConsentMgtServiceTestData.SAMPLE_USER_ID, false);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeConsentDataRetrievalError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                    .getDetailedConsentResource(any(), any());

            consentCoreServiceImpl.revokeConsentWithReason(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                    false, ConsentCoreServiceConstants.CONSENT_REVOKE_REASON);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeConsentDataInsertionError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            DetailedConsentResource retrievedDetailedConsentResource =
                    ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

            doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                    .getDetailedConsentResource(any(), any());
            doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                    anyString());
            doThrow(ConsentDataInsertionException.class)
                    .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                            any(ConsentStatusAuditRecord.class));

            consentCoreServiceImpl.revokeConsent(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                    false);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeConsentDataUpdateError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            DetailedConsentResource retrievedDetailedConsentResource =
                    ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

            doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                    .getDetailedConsentResource(any(), any());
            doThrow(ConsentDataUpdationException.class).when(mockedConsentCoreDAO)
                    .updateConsentStatus(any(), anyString(), anyString());

            consentCoreServiceImpl.revokeConsentWithReason(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                    false, ConsentCoreServiceConstants.CONSENT_REVOKE_REASON);
        }
    }


    @Test
    public void testRevokeConsentWithoutReason() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            DetailedConsentResource retrievedDetailedConsentResource =
                    ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

            doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                    .getDetailedConsentResource(any(), any());
            doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                    anyString());
            doReturn(ConsentMgtServiceTestData.getSampleTestConsentStatusAuditRecord(
                    retrievedDetailedConsentResource.getConsentID(),
                    retrievedDetailedConsentResource.getCurrentStatus()))
                    .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                            any(ConsentStatusAuditRecord.class));
            doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
                    any(), anyString());

            boolean isConsentRevoked = consentCoreServiceImpl.revokeConsent(
                    ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);

            Assert.assertTrue(isConsentRevoked);
        }
    }

    @Test (priority = 1)
    public void testRevokeConsentWithUserIDWithoutReason() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            DetailedConsentResource retrievedDetailedConsentResource =
                    ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

            doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                    .getDetailedConsentResource(any(), any());
            doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                    anyString());
            doReturn(ConsentMgtServiceTestData.getSampleTestConsentStatusAuditRecord(
                    retrievedDetailedConsentResource.getConsentID(),
                    retrievedDetailedConsentResource.getCurrentStatus()))
                    .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                            any(ConsentStatusAuditRecord.class));
            doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
                    any(), anyString());

            boolean isConsentRevoked = consentCoreServiceImpl.revokeConsent(
                    ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID);

            Assert.assertTrue(isConsentRevoked);
        }
    }

    @Test
    public void testRevokeExistingApplicableConsents() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            ArrayList<DetailedConsentResource> detailedConsentResources = new ArrayList<>();
            detailedConsentResources.add(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource());

            doReturn(detailedConsentResources).when(mockedConsentCoreDAO)
                    .searchConsents(any(), any(), any(), any(), any(),
                            any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt());
            doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                    anyString());
            doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentStatusAuditRecord(sampleID,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS)).when(mockedConsentCoreDAO)
                    .storeConsentStatusAuditRecord(any(), any(ConsentStatusAuditRecord.class));
            doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
                    any(), any());

            Assert.assertTrue(consentCoreServiceImpl.revokeExistingApplicableConsents(sampleID,
                    ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    false));
        }
    }

    @Test
    public void testRevokeExistingApplicableConsentsWithTokens() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            ArrayList<DetailedConsentResource> detailedConsentResources = new ArrayList<>();
            detailedConsentResources.add(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource());

            doReturn(detailedConsentResources).when(mockedConsentCoreDAO)
                    .searchConsents(any(), any(), any(), any(), any(),
                            any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt());
            doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                    anyString());
            doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentStatusAuditRecord(sampleID,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS)).when(mockedConsentCoreDAO)
                    .storeConsentStatusAuditRecord(any(), any(ConsentStatusAuditRecord.class));
            doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
                    any(), any());

            Assert.assertTrue(consentCoreServiceImpl.revokeExistingApplicableConsents(sampleID,
                    ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    true));
        }
    }

    @Test
    public void testRevokeExistingApplicableConsentsWithConsentsWithNoAttributes() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            DetailedConsentResource detailedConsentResource =
                    ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();
            detailedConsentResource.setConsentAttributes(null);

            ArrayList<DetailedConsentResource> detailedConsentResources = new ArrayList<>();
            detailedConsentResources.add(detailedConsentResource);

            doReturn(detailedConsentResources).when(mockedConsentCoreDAO)
                    .searchConsents(any(), any(), any(), any(), any(),
                            any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt());
            doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                    anyString());
            doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentStatusAuditRecord(sampleID,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS)).when(mockedConsentCoreDAO)
                    .storeConsentStatusAuditRecord(any(), any(ConsentStatusAuditRecord.class));
            doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
                    any(), any());

            consentCoreServiceImpl.revokeExistingApplicableConsents(sampleID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, false);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeExistingApplicableConsentsRetrieveError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                    .searchConsents(any(), any(), any(), any(), any(),
                            any(), any(), any(), any(), any());

            consentCoreServiceImpl.revokeExistingApplicableConsents(sampleID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, false);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeExistingApplicableConsentsUpdateError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            ArrayList<DetailedConsentResource> detailedConsentResources = new ArrayList<>();
            detailedConsentResources.add(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource());

            doReturn(detailedConsentResources).when(mockedConsentCoreDAO)
                    .searchConsents(any(), any(), any(), any(), any(),
                            any(), any(), any(), any(), any());
            doThrow(ConsentDataUpdationException.class).when(mockedConsentCoreDAO)
                    .updateConsentStatus(any(), anyString(), anyString());

            consentCoreServiceImpl.revokeExistingApplicableConsents(sampleID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, false);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeExistingApplicableConsentsInsertionError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            ArrayList<DetailedConsentResource> detailedConsentResources = new ArrayList<>();
            detailedConsentResources.add(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource());

            doReturn(detailedConsentResources).when(mockedConsentCoreDAO)
                    .searchConsents(any(), any(), any(), any(), any(),
                            any(), any(), any(), any(), any());
            doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                    anyString());
            doThrow(ConsentDataInsertionException.class).when(mockedConsentCoreDAO)
                    .storeConsentStatusAuditRecord(any(), any());

            consentCoreServiceImpl.revokeExistingApplicableConsents(sampleID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, false);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeExistingApplicableConsentsWithoutClientID() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.revokeExistingApplicableConsents(null, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, false);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeExistingApplicableConsentsWithoutRevokedConsentStatus() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.revokeExistingApplicableConsents(ConsentMgtServiceTestData.SAMPLE_CLIENT_ID,
                    ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, null, false);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeExistingApplicableConsentsWithoutUserID() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.revokeExistingApplicableConsents(ConsentMgtServiceTestData.SAMPLE_CLIENT_ID,
                    null, ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS
                    , false);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeExistingApplicableConsentsWithoutConsentType() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.revokeExistingApplicableConsents(ConsentMgtServiceTestData.SAMPLE_CLIENT_ID,
                    ConsentMgtServiceTestData.SAMPLE_USER_ID, null,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS
                    , false);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeExistingApplicableConsentsWithoutApplicableStatusToRevoke() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.revokeExistingApplicableConsents(ConsentMgtServiceTestData.SAMPLE_CLIENT_ID,
                    ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE,
                    null, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS
                    , false);
        }
    }

    @Test
    public void testReAuthorizeExistingAuthResources() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            ConsentResource consentResource = ConsentMgtServiceTestData.getSampleStoredConsentResource();
            doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                    .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());
            doReturn(ConsentMgtServiceTestData
                    .getSampleTestConsentMappingResource(ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID))
                    .when(mockedConsentCoreDAO).storeConsentMappingResource(any(),
                            any(ConsentMappingResource.class));
            doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
                    any(), anyString());
            Assert.assertTrue(consentCoreServiceImpl
                    .reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                            ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                            ConsentMgtServiceTestData.SAMPLE_USER_ID,
                            ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                            ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, consentResource.getCurrentStatus()));
        }
    }

    @Test
    public void testReAuthorizeExistingAuthResourceAccountsAddScenario() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            ConsentMappingResource consentMappingResource =
                    ConsentMgtServiceTestData.getSampleTestConsentMappingResource(sampleID);
            consentMappingResource.setAccountID("accountID1");
            ArrayList<ConsentMappingResource> mappingResources = new ArrayList<>();
            mappingResources.add(consentMappingResource);

            DetailedConsentResource detailedConsentResource =
                    ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();
            detailedConsentResource.setConsentMappingResources(mappingResources);

            ConsentResource consentResource = ConsentMgtServiceTestData.getSampleStoredConsentResource();
            doReturn(detailedConsentResource)
                    .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());
            doReturn(ConsentMgtServiceTestData
                    .getSampleTestConsentMappingResource(ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID))
                    .when(mockedConsentCoreDAO).storeConsentMappingResource(any(),
                            any(ConsentMappingResource.class));
            doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
                    any(), anyString());
            Assert.assertTrue(consentCoreServiceImpl
                    .reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                            ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                            ConsentMgtServiceTestData.SAMPLE_USER_ID,
                            ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                            ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, consentResource.getCurrentStatus()));
        }
    }

    @Test
    public void testReAuthorizeExistingAuthResourceNoAccountsRemoveOrAddScenario() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            ConsentResource consentResource = ConsentMgtServiceTestData.getSampleStoredConsentResource();
            doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                    .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());
            doReturn(ConsentMgtServiceTestData
                    .getSampleTestConsentMappingResource(ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID))
                    .when(mockedConsentCoreDAO).storeConsentMappingResource(any(),
                            any(ConsentMappingResource.class));
            doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
                    any(), anyString());
            Assert.assertTrue(consentCoreServiceImpl
                    .reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                            ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                            ConsentMgtServiceTestData.SAMPLE_USER_ID,
                            ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP2,
                            ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, consentResource.getCurrentStatus()));
        }
    }

    @Test
    public void testReAuthorizeExistingAuthResourceAccountsRemoveScenario() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            ConsentResource consentResource = ConsentMgtServiceTestData.getSampleStoredConsentResource();
            doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResourceWithMultipleAccountIDs())
                    .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());
            doReturn(ConsentMgtServiceTestData
                    .getSampleTestConsentMappingResource(ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID))
                    .when(mockedConsentCoreDAO).storeConsentMappingResource(any(),
                            any(ConsentMappingResource.class));
            doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
                    any(), anyString());
            Assert.assertTrue(consentCoreServiceImpl
                    .reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                            ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                            ConsentMgtServiceTestData.SAMPLE_USER_ID,
                            ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP3,
                            ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, consentResource.getCurrentStatus()));
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeExistingAuthResourcesWithoutConsentID() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            Assert.assertTrue(consentCoreServiceImpl.reAuthorizeExistingAuthResource(null,
                    ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                    ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS));
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeExistingAuthResourcesWithoutAuthID() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            Assert.assertTrue(consentCoreServiceImpl
                    .reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                            null, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                            ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                            ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                            ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS));
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeExistingAuthResourcesWithoutUserID() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            Assert.assertTrue(consentCoreServiceImpl
                    .reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                            sampleID, null, ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                            ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                            ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS));
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeExistingAuthResourcesWithoutCurrentConsentStatus() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            Assert.assertTrue(consentCoreServiceImpl
                    .reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                            sampleID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                            ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                            null, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS));
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeExistingAuthResourcesWithoutNewConsentStatus() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            Assert.assertTrue(consentCoreServiceImpl
                    .reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                            ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                            ConsentMgtServiceTestData.SAMPLE_USER_ID,
                            ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                            ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, null));
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeExistingAuthResourcesWithoutAccountsAndPermissionsMap() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            Assert.assertTrue(consentCoreServiceImpl
                    .reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                            ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                            ConsentMgtServiceTestData.SAMPLE_USER_ID,
                            new HashMap<>(), ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                            ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS));
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeExistingAuthResourcesDataRetrieveError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doThrow(ConsentDataRetrievalException.class)
                    .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());
            consentCoreServiceImpl.reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                    ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                    ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeExistingAuthResourcesDataInsertError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                    .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());
            doReturn(ConsentMgtServiceTestData
                    .getSampleConsentMappingResourcesList(ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST))
                    .when(mockedConsentCoreDAO).getConsentMappingResources(any(), anyString());
            doThrow(ConsentDataInsertionException.class)
                    .when(mockedConsentCoreDAO).storeConsentMappingResource(any(),
                            any(ConsentMappingResource.class));
            consentCoreServiceImpl.reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                    ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                    ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeExistingAuthResourcesDataUpdateError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                    .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());
            doReturn(ConsentMgtServiceTestData
                    .getSampleConsentMappingResourcesList(ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST))
                    .when(mockedConsentCoreDAO).getConsentMappingResources(any(), anyString());
            doThrow(ConsentDataUpdationException.class)
                    .when(mockedConsentCoreDAO).updateConsentMappingStatus(any(), any(),
                            anyString());
            consentCoreServiceImpl.reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                    ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                    ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
        }
    }

    @Test
    public void testReAuthorizeConsentWithNewAuthResource() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            AuthorizationResource authorizationResource = ConsentMgtServiceTestData
                    .getSampleTestAuthorizationResource(sampleID, null);
            ArrayList<String> consentIDs = new ArrayList<>();
            consentIDs.add(sampleID);

            doReturn(ConsentMgtServiceTestData.getSampleAuthorizationResourcesList(consentIDs))
                    .when(mockedConsentCoreDAO).searchConsentAuthorizations(any(), anyString(),
                            anyString());
            doNothing().when(mockedConsentCoreDAO).updateAuthorizationStatus(any(), anyString(),
                    anyString());
            doReturn(authorizationResource).when(mockedConsentCoreDAO).storeAuthorizationResource(any(),
                    any(AuthorizationResource.class));
            doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                    .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());
            doReturn(ConsentMgtServiceTestData
                    .getSampleTestConsentMappingResource(sampleID))
                    .when(mockedConsentCoreDAO).storeConsentMappingResource(any(),
                            any(ConsentMappingResource.class));
            doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
                    any(), anyString());
            doNothing().when(mockedConsentCoreDAO).updateConsentStatus(any(), anyString(),
                    anyString());
            doReturn(ConsentMgtServiceTestData
                    .getSampleTestConsentStatusAuditRecord(sampleID,
                            ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                    .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                            any(ConsentStatusAuditRecord.class));

            consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(sampleID, sampleID,
                    ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeConsentWithNewAuthResourceDataRetrieveError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doThrow(ConsentDataRetrievalException.class)
                    .when(mockedConsentCoreDAO).searchConsentAuthorizations(any(), any(), any());

            consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(sampleID, sampleID,
                    ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeConsentWithNewAuthResourceDataUpdateError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            ArrayList<String> consentIDs = new ArrayList<>();
            consentIDs.add(sampleID);
            doReturn(ConsentMgtServiceTestData.getSampleAuthorizationResourcesList(consentIDs))
                    .when(mockedConsentCoreDAO).searchConsentAuthorizations(any(), any(), any());
            doThrow(ConsentDataUpdationException.class)
                    .when(mockedConsentCoreDAO).updateAuthorizationStatus(any(), anyString(),
                            anyString());

            consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(sampleID, sampleID,
                    ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeConsentWithNewAuthResourceDataInsertError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            AuthorizationResource authorizationResource = ConsentMgtServiceTestData
                    .getSampleTestAuthorizationResource(sampleID, null);

            doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResourcesList())
                    .when(mockedConsentCoreDAO).searchConsents(any(), any(), any(), any(),
                            any(), any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(),
                            Mockito.anyInt());
            doNothing().when(mockedConsentCoreDAO).updateAuthorizationStatus(any(), anyString(),
                    anyString());
            doReturn(authorizationResource).when(mockedConsentCoreDAO).storeAuthorizationResource(any(),
                    any(AuthorizationResource.class));
            doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                    .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());
            doThrow(ConsentDataInsertionException.class)
                    .when(mockedConsentCoreDAO).storeConsentMappingResource(any(),
                            any(ConsentMappingResource.class));

            consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(sampleID, sampleID,
                    ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeConsentWithNewAuthResourceWithoutConsentID() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(null, sampleID,
                    ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeConsentWithNewAuthResourceWithoutUserID() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(sampleID, null,
                    ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeConsentWithNewAuthResourceWithoutAccountsMap() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(sampleID, sampleID,
                    null, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeConsentWithNewAuthResourceWithoutCurrentConsentStatus() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(sampleID, sampleID,
                    ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP, null,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeConsentWithNewAuthResourceWithoutNewConsentStatus() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(sampleID, sampleID,
                    ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    null, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeConsentWithNewAuthResourceWithoutNewExistingAuthStatus() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(sampleID, sampleID,
                    ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, null,
                    ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeConsentWithNewAuthResourceWithoutNewAuthStatus() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(sampleID, sampleID,
                    ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    null, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeConsentWithNewAuthResourceWithoutNewAuthType() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(sampleID, sampleID,
                    ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS, null);
        }
    }

    @Test
    public void storeConsentAttributes() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.storeConsentAttributes(ConsentMgtServiceTestData.CONSENT_ID,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void storeConsentAttributesWithoutParameters() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.storeConsentAttributes(null, null);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void storeConsentAttributesWithoutConsentId() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.storeConsentAttributes(null,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void storeConsentAttributesWithoutAttributeMap() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.storeConsentAttributes(ConsentMgtServiceTestData.CONSENT_ID,
                    null);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void storeConsentAttributesEmptyAttributeMap() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.storeConsentAttributes(ConsentMgtServiceTestData.CONSENT_ID,
                    new HashMap<>());
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void storeConsentAttributesDataInsertError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doThrow(ConsentDataInsertionException.class)
                    .when(mockedConsentCoreDAO).storeConsentAttributes(any(), any());

            consentCoreServiceImpl.storeConsentAttributes(ConsentMgtServiceTestData.CONSENT_ID,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);
        }
    }

    @Test
    public void testGetConsentAttributesWithAttributeKeys() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doReturn(ConsentMgtServiceTestData.getSampleTestConsentResource())
                    .when(mockedConsentCoreDAO).getConsentResource(any(), anyString());
            doReturn(ConsentMgtServiceTestData
                    .getSampleTestConsentAttributesObject(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID))
                    .when(mockedConsentCoreDAO).getConsentAttributes(any(), anyString(),
                            any());
            ConsentAttributes consentAttributes =
                    consentCoreServiceImpl.getConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                            ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
            Assert.assertNotNull(consentAttributes);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentAttributesWithoutConsentID() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.getConsentAttributes(null,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentAttributesWithEmptyAttributeKeys() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doThrow(ConsentDataRetrievalException.class)
                    .when(mockedConsentCoreDAO).getConsentAttributes(any(), anyString());
            consentCoreServiceImpl.getConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                    new ArrayList<>());
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentAttributesConsentResourceReteivealError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doThrow(ConsentDataRetrievalException.class)
                    .when(mockedConsentCoreDAO).getConsentAttributes(any(), anyString(), any());
            consentCoreServiceImpl.getConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentAttributesDataRetrieveError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doReturn(ConsentMgtServiceTestData.getSampleTestConsentResource())
                    .when(mockedConsentCoreDAO).getConsentResource(any(), anyString());
            doThrow(ConsentDataRetrievalException.class)
                    .when(mockedConsentCoreDAO).getConsentAttributes(any(), anyString(), any());
            consentCoreServiceImpl.getConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
        }
    }

    @Test
    public void testGetConsentAttributes() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doReturn(ConsentMgtServiceTestData.getSampleTestConsentResource())
                    .when(mockedConsentCoreDAO).getConsentResource(any(), anyString());
            doReturn(ConsentMgtServiceTestData
                    .getSampleTestConsentAttributesObject(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID))
                    .when(mockedConsentCoreDAO).getConsentAttributes(any(), anyString());
            ConsentAttributes consentAttributes =
                    consentCoreServiceImpl.getConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID);
            Assert.assertNotNull(consentAttributes);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentAttributesWithoutAttributeKeys() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doThrow(ConsentDataRetrievalException.class)
                    .when(mockedConsentCoreDAO).getConsentAttributes(any(), anyString());
            consentCoreServiceImpl.getConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentAttributesWithoutAttributesWithoutConsentID() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.getConsentAttributes(null);
        }
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testGetConsentAttributesConsentResourceRetrieveError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doThrow(ConsentDataRetrievalException.class)
                    .when(mockedConsentCoreDAO).getConsentResource(any(), anyString());
            doReturn(ConsentMgtServiceTestData
                    .getSampleTestConsentAttributesObject(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID))
                    .when(mockedConsentCoreDAO).getConsentAttributes(any(), anyString());
            ConsentAttributes consentAttributes =
                    consentCoreServiceImpl.getConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID);
            Assert.assertNotNull(consentAttributes);
        }
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testGetConsentAttributesWithDataRetrieveError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doReturn(ConsentMgtServiceTestData.getSampleTestConsentResource())
                    .when(mockedConsentCoreDAO).getConsentResource(any(), anyString());
            doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                    .getConsentAttributes(any(), anyString());
            ConsentAttributes consentAttributes =
                    consentCoreServiceImpl.getConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID);
            Assert.assertNotNull(consentAttributes);
        }
    }

    @Test
    public void testGetConsentAttributesByName() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doReturn(ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP)
                    .when(mockedConsentCoreDAO).getConsentAttributesByName(any(), anyString());
            Map<String, String> retrievedAttributesMap =
                    consentCoreServiceImpl.getConsentAttributesByName("x-request-id");
            Assert.assertTrue(retrievedAttributesMap.containsKey("x-request-id"));
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentAttributesByNameWithoutAttributeName() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.getConsentAttributesByName(null);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentAttributesByNameDataRetrieveError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doThrow(ConsentDataRetrievalException.class)
                    .when(mockedConsentCoreDAO).getConsentAttributesByName(any(), anyString());
            consentCoreServiceImpl.getConsentAttributesByName("x-request-id");
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentIdByConsentAttributeNameAndValueWithoutAttributeName() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.getConsentIdByConsentAttributeNameAndValue(null,
                    "domestic-payments");
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentIdByConsentAttributeNameAndValueWithoutAttributeValues() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.getConsentIdByConsentAttributeNameAndValue("payment-type",
                    null);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentIdByConsentAttributeNameAndValueDataRetrieveError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doThrow(ConsentDataRetrievalException.class)
                    .when(mockedConsentCoreDAO).getConsentIdByConsentAttributeNameAndValue(any(),
                            anyString(), anyString());
            consentCoreServiceImpl.getConsentIdByConsentAttributeNameAndValue("payment-type",
                    "domestic-payments");
        }
    }

    @Test
    public void testGetConsentIdByConsentAttributeNameAndValue() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doReturn(ConsentMgtServiceTestData.SAMPLE_CONSENT_IS_ARRAY)
                    .when(mockedConsentCoreDAO).getConsentIdByConsentAttributeNameAndValue(any(),
                            anyString(), anyString());
            ArrayList<String> consentIdList = consentCoreServiceImpl.getConsentIdByConsentAttributeNameAndValue(
                    "payment-type", "domestic-payments");
            Assert.assertFalse(consentIdList.isEmpty());
        }
    }

    @Test
    public void testUpdateConsentAttributes() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doNothing().when(mockedConsentCoreDAO).updateConsentAttributes(any(),
                    anyString(), Mockito.anyMap());
            doReturn(ConsentMgtServiceTestData.getSampleTestConsentAttributesObject(
                    ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID))
                    .when(mockedConsentCoreDAO).getConsentAttributes(any(), anyString());
            consentCoreServiceImpl.updateConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);
        }
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testUpdateConsentAttributesWithoutConsentId() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.updateConsentAttributes(null,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);
        }
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testUpdateConsentAttributesWithoutAttributes() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);
        }
        consentCoreServiceImpl.updateConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID, null);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testUpdateConsentAttributesWithEmptyAttributes() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.updateConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                    new HashMap<>());
        }
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testUpdateConsentAttributesWithDataUpdateError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doThrow(ConsentDataUpdationException.class).when(mockedConsentCoreDAO)
                    .updateConsentAttributes(any(), anyString(), Mockito.anyMap());
            doReturn(ConsentMgtServiceTestData.getSampleTestConsentAttributesObject(
                    ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID))
                    .when(mockedConsentCoreDAO).getConsentAttributes(any(), anyString());
            consentCoreServiceImpl.updateConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);
        }
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testUpdateConsentAttributesWithDataRetrieveError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doNothing().when(mockedConsentCoreDAO).updateConsentAttributes(any(),
                    anyString(), Mockito.anyMap());
            doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                    .getConsentAttributes(any(), anyString());
            consentCoreServiceImpl.updateConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);
        }
    }

    @Test
    public void testDeleteConsentAttributes() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doReturn(true).when(mockedConsentCoreDAO).deleteConsentAttributes(any(),
                    anyString(), any());
            consentCoreServiceImpl.deleteConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testDeleteConsentAttributesDeleteError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doThrow(ConsentDataDeletionException.class)
                    .when(mockedConsentCoreDAO).deleteConsentAttributes(any(), anyString(),
                            any());
            consentCoreServiceImpl.deleteConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testDeleteConsentAttributesWithoutConsentID() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doReturn(true).when(mockedConsentCoreDAO).deleteConsentAttributes(any(),
                    anyString(), any());
            consentCoreServiceImpl.deleteConsentAttributes(null,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testDeleteConsentAttributesWithoutAttributeKeysList() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doReturn(true).when(mockedConsentCoreDAO).deleteConsentAttributes(any(),
                    anyString(), any());
            consentCoreServiceImpl.deleteConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                    null);
        }
    }

    @Test
    public void testSearchConsentStatusAuditRecords() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            ArrayList<ConsentStatusAuditRecord> consentStatusAuditRecords = new ArrayList<>();

            doReturn(consentStatusAuditRecords).when(mockedConsentCoreDAO)
                    .getConsentStatusAuditRecordsByConsentId(any(), any(ArrayList.class),
                            Mockito.anyInt(),
                            Mockito.anyInt());
            ArrayList<ConsentStatusAuditRecord> statusAuditRecords =
                    consentCoreServiceImpl.searchConsentStatusAuditRecords(ConsentMgtServiceTestData.CONSENT_ID,
                            ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_ACTION_BY,
                            0L, 0L, "1234");
            Assert.assertNotNull(statusAuditRecords);
        }
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testSearchConsentStatusAuditRecordsWithDataRetrievalError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                    .getConsentStatusAuditRecords(any(), anyString(), anyString(),
                            anyString(), Mockito.anyLong(), Mockito.anyLong(), anyString());
            ArrayList<ConsentStatusAuditRecord> statusAuditRecords =
                    consentCoreServiceImpl.searchConsentStatusAuditRecords(ConsentMgtServiceTestData.CONSENT_ID,
                            ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_ACTION_BY,
                            0L, 0L, "1234");
            Assert.assertNotNull(statusAuditRecords);
        }
    }

    @Test
    public void testGetConsentStatusAuditRecords() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            ArrayList<ConsentStatusAuditRecord> consentStatusAuditRecords = new ArrayList<>();
            ArrayList<String> consentIds = new ArrayList<>();

            doReturn(consentStatusAuditRecords).when(mockedConsentCoreDAO)
                    .getConsentStatusAuditRecordsByConsentId(any(), any(ArrayList.class),
                            Mockito.anyInt(),
                            Mockito.anyInt());
            ArrayList<ConsentStatusAuditRecord> statusAuditRecords =
                    consentCoreServiceImpl.getConsentStatusAuditRecords(consentIds, null, null);
            Assert.assertNotNull(statusAuditRecords);
        }
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testGetConsentStatusAuditRecordsWithDataRetrievalError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            ArrayList<String> consentIds = new ArrayList<>();

            doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                    .getConsentStatusAuditRecordsByConsentId(any(), any(ArrayList.class),
                            any(), any());
            ArrayList<ConsentStatusAuditRecord> statusAuditRecords =
                    consentCoreServiceImpl.getConsentStatusAuditRecords(consentIds, null, null);
            Assert.assertNotNull(statusAuditRecords);
        }
    }

    @Test
    public void testStoreConsentAmendmentHistory() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            boolean result = consentCoreServiceImpl.storeConsentAmendmentHistory(sampleID,
                    ConsentMgtServiceTestData.getSampleTestConsentHistoryResource(),
                    ConsentMgtServiceTestData.getSampleDetailedStoredTestCurrentConsentResource());

            Assert.assertTrue(result);
        }
    }

    @Test
    public void testStoreConsentAmendmentHistoryWithoutPassingCurrentConsent() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestCurrentConsentResource())
                    .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());

            ConsentHistoryResource consentHistoryResource = new ConsentHistoryResource();
            consentHistoryResource.setTimestamp(ConsentMgtServiceTestData.SAMPLE_CONSENT_AMENDMENT_TIMESTAMP);
            consentHistoryResource.setReason(ConsentMgtServiceTestData.SAMPLE_AMENDMENT_REASON);
            consentHistoryResource.setDetailedConsentResource(ConsentMgtServiceTestData
                    .getSampleDetailedStoredTestCurrentConsentResource());

            boolean result = consentCoreServiceImpl.storeConsentAmendmentHistory(
                    ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                    consentHistoryResource, null);

            Assert.assertTrue(result);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testStoreConsentAmendmentHistoryWithoutConsentID() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.storeConsentAmendmentHistory(null,
                    ConsentMgtServiceTestData.getSampleTestConsentHistoryResource(),
                    ConsentMgtServiceTestData.getSampleDetailedStoredTestCurrentConsentResource());
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testStoreConsentAmendmentHistoryWithoutConsentHistoryResource() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.storeConsentAmendmentHistory(sampleID,
                    null,
                    ConsentMgtServiceTestData.getSampleDetailedStoredTestCurrentConsentResource());
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testStoreConsentAmendmentHistoryWithZeroAsConsentAmendedTimestamp() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            ConsentHistoryResource consentHistoryResource = ConsentMgtServiceTestData
                    .getSampleTestConsentHistoryResource();
            consentHistoryResource.setTimestamp(0);
            consentCoreServiceImpl.storeConsentAmendmentHistory(sampleID, consentHistoryResource,
                    ConsentMgtServiceTestData.getSampleDetailedStoredTestCurrentConsentResource());
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testStoreConsentAmendmentHistoryWithoutConsentAmendedReason() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            ConsentHistoryResource consentHistoryResource = ConsentMgtServiceTestData
                    .getSampleTestConsentHistoryResource();
            consentHistoryResource.setReason(null);
            consentCoreServiceImpl.storeConsentAmendmentHistory(sampleID,
                    consentHistoryResource,
                    ConsentMgtServiceTestData.getSampleDetailedStoredTestCurrentConsentResource());
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testStoreConsentAmendmentHistoryDataInsertError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doThrow(ConsentDataInsertionException.class)
                    .when(mockedConsentCoreDAO).storeConsentAmendmentHistory(any(), anyString(),
                            Mockito.anyLong(), anyString(), anyString(), anyString(),
                            anyString());

            consentCoreServiceImpl.storeConsentAmendmentHistory(sampleID,
                    ConsentMgtServiceTestData.getSampleTestConsentHistoryResource(),
                    ConsentMgtServiceTestData.getSampleDetailedStoredTestCurrentConsentResource());
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testStoreConsentAmendmentHistoryDataRetrievalError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doThrow(ConsentDataRetrievalException.class)
                    .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());

            consentCoreServiceImpl.storeConsentAmendmentHistory(sampleID,
                    ConsentMgtServiceTestData.getSampleTestConsentHistoryResource(), null);
        }
    }

    @Test
    public void testGetConsentAmendmentHistoryData() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentHistoryDataMap())
                    .when(mockedConsentCoreDAO).retrieveConsentAmendmentHistory(any(), any());
            doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                    .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());

            Map<String, ConsentHistoryResource> consentAmendmentHistory =
                    consentCoreServiceImpl.getConsentAmendmentHistoryData(sampleID);

            Assert.assertTrue(consentAmendmentHistory.containsKey(ConsentMgtServiceTestData.SAMPLE_HISTORY_ID));
            Assert.assertNotNull(consentAmendmentHistory.get(ConsentMgtServiceTestData.SAMPLE_HISTORY_ID));
        }
    }

    @Test
    public void testGetConsentAmendmentHistoryDataWithOnlyBasicConsentData() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doReturn(ConsentMgtServiceTestData.getSampleConsentHistoryBasicConsentDataMap())
                    .when(mockedConsentCoreDAO).retrieveConsentAmendmentHistory(any(), any());
            doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                    .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());

            Map<String, ConsentHistoryResource> consentAmendmentHistory =
                    consentCoreServiceImpl.getConsentAmendmentHistoryData(sampleID);

            Assert.assertTrue(consentAmendmentHistory.containsKey(ConsentMgtServiceTestData.SAMPLE_HISTORY_ID));
            Assert.assertNotNull(consentAmendmentHistory.get(ConsentMgtServiceTestData.SAMPLE_HISTORY_ID));
        }
    }

    @Test
    public void testGetConsentAmendmentHistoryDataWithOnlyConsentAttributesData() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doReturn(ConsentMgtServiceTestData.getSampleConsentHistoryConsentAttributesDataMap())
                    .when(mockedConsentCoreDAO).retrieveConsentAmendmentHistory(any(), any());
            doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                    .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());

            Map<String, ConsentHistoryResource> consentAmendmentHistory =
                    consentCoreServiceImpl.getConsentAmendmentHistoryData(sampleID);

            Assert.assertTrue(consentAmendmentHistory.containsKey(ConsentMgtServiceTestData.SAMPLE_HISTORY_ID));
            Assert.assertNotNull(consentAmendmentHistory.get(ConsentMgtServiceTestData.SAMPLE_HISTORY_ID));
        }
    }

    @Test
    public void testGetConsentAmendmentHistoryDataWithOnlyConsentMappingsData() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doReturn(ConsentMgtServiceTestData.getSampleConsentHistoryConsentMappingsDataMap())
                    .when(mockedConsentCoreDAO).retrieveConsentAmendmentHistory(any(), any());
            doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                    .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());

            Map<String, ConsentHistoryResource> consentAmendmentHistory =
                    consentCoreServiceImpl.getConsentAmendmentHistoryData(sampleID);

            Assert.assertTrue(consentAmendmentHistory.containsKey(ConsentMgtServiceTestData.SAMPLE_HISTORY_ID));
            Assert.assertNotNull(consentAmendmentHistory.get(ConsentMgtServiceTestData.SAMPLE_HISTORY_ID));
        }
    }

    @Test
    public void testGetConsentAmendmentHistoryDataWithNoConsentHistoryEntries() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doReturn(new HashMap<>())
                    .when(mockedConsentCoreDAO).retrieveConsentAmendmentHistory(any(), any());
            doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                    .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());

            Map<String, ConsentHistoryResource> consentAmendmentHistory =
                    consentCoreServiceImpl.getConsentAmendmentHistoryData(sampleID);

            Assert.assertEquals(0, consentAmendmentHistory.size());
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentAmendmentHistoryDataWithoutConsentID() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentHistoryDataMap())
                    .when(mockedConsentCoreDAO).retrieveConsentAmendmentHistory(any(), any());
            doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                    .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());

            consentCoreServiceImpl.getConsentAmendmentHistoryData(null);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentAmendmentHistoryDataRetrieveError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentHistoryDataMap())
                    .when(mockedConsentCoreDAO).retrieveConsentAmendmentHistory(any(), any());
            doThrow(ConsentDataRetrievalException.class)
                    .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());

            consentCoreServiceImpl.getConsentAmendmentHistoryData(sampleID);
        }
    }

    @Test
    public void testSearchConsents() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            ArrayList<DetailedConsentResource> detailedConsentResources = new ArrayList<>();
            detailedConsentResources.add(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource());

            doReturn(detailedConsentResources)
                    .when(mockedConsentCoreDAO).searchConsents(any(), any(), any(), any(),
                            any(), any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(),
                            Mockito.anyInt());

            consentCoreServiceImpl.searchDetailedConsents(ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST,
                    ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPES_LIST,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_STATUSES_LIST,
                    ConsentMgtServiceTestData.SAMPLE_USER_IDS_LIST,
                    12345L, 23456L, null, null);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testSearchConsentsRetrieveError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doThrow(ConsentDataRetrievalException.class)
                    .when(mockedConsentCoreDAO).searchConsents(any(), any(), any(), any(),
                            any(), any(), any(), any(), any(), any());

            consentCoreServiceImpl.searchDetailedConsents(ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST,
                    ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPES_LIST,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_STATUSES_LIST,
                    ConsentMgtServiceTestData.SAMPLE_USER_IDS_LIST,
                    12345L, 23456L, null, null);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testSearchConsentsWithLimits() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doThrow(ConsentDataRetrievalException.class)
                    .when(mockedConsentCoreDAO).searchConsents(any(), any(), any(), any(),
                            any(), any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(),
                            Mockito.anyInt());

            consentCoreServiceImpl.searchDetailedConsents(ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST,
                    ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPES_LIST,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_STATUSES_LIST,
                    ConsentMgtServiceTestData.SAMPLE_USER_IDS_LIST,
                    12345L, 23456L, 1, 0);
        }
    }

    @Test
    public void testAmendConsentData() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doNothing().when(mockedConsentCoreDAO).updateConsentReceipt(any(),
                    anyString(), anyString());
            doNothing().when(mockedConsentCoreDAO).updateConsentValidityTime(any(),
                    anyString(), Mockito.anyLong());
            doReturn(ConsentMgtServiceTestData.getSampleStoredConsentResource())
                    .when(mockedConsentCoreDAO).getConsentResource(any(), anyString());
            doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentStatusAuditRecord(sampleID,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                    .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                            any(ConsentStatusAuditRecord.class));

            ConsentResource consentResource =
                    consentCoreServiceImpl.amendConsentData(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                            ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                            ConsentMgtServiceTestData.SAMPLE_USER_ID);

            Assert.assertNotNull(consentResource);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendConsentDataWithoutConsentID() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.amendConsentData(null, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                    ConsentMgtServiceTestData.SAMPLE_USER_ID);
        }

    }

    @Test
    public void testAmendConsentValidityPeriod() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doNothing().when(mockedConsentCoreDAO).updateConsentValidityTime(any(),
                    anyString(), Mockito.anyLong());
            doReturn(ConsentMgtServiceTestData.getSampleStoredConsentResource())
                    .when(mockedConsentCoreDAO).getConsentResource(any(), anyString());
            doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentStatusAuditRecord(sampleID,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                    .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                            any(ConsentStatusAuditRecord.class));

            ConsentResource consentResource =
                    consentCoreServiceImpl.amendConsentData(sampleID, null,
                            ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                            ConsentMgtServiceTestData.SAMPLE_USER_ID);

            Assert.assertNotNull(consentResource);
        }
    }

    @Test
    public void testAmendConsentReceipt() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doNothing().when(mockedConsentCoreDAO).updateConsentReceipt(any(),
                    anyString(), anyString());
            doReturn(ConsentMgtServiceTestData.getSampleStoredConsentResource())
                    .when(mockedConsentCoreDAO).getConsentResource(any(), anyString());
            doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentStatusAuditRecord(sampleID,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                    .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                            any(ConsentStatusAuditRecord.class));

            ConsentResource consentResource =
                    consentCoreServiceImpl.amendConsentData(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                            null, ConsentMgtServiceTestData.SAMPLE_USER_ID);

            Assert.assertNotNull(consentResource);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendConsentDataWithoutReceiptAndValidityTime() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.amendConsentData(sampleID, null, null,
                    ConsentMgtServiceTestData.SAMPLE_USER_ID);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendConsentDataUpdateError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doThrow(ConsentDataUpdationException.class)
                    .when(mockedConsentCoreDAO).updateConsentReceipt(any(), anyString(),
                            anyString());

            consentCoreServiceImpl.amendConsentData(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                    ConsentMgtServiceTestData.SAMPLE_USER_ID);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendConsentDataRetrieveError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doNothing().when(mockedConsentCoreDAO).updateConsentReceipt(any(),
                    anyString(), anyString());
            doNothing().when(mockedConsentCoreDAO).updateConsentValidityTime(any(),
                    anyString(), Mockito.anyLong());
            doThrow(ConsentDataRetrievalException.class)
                    .when(mockedConsentCoreDAO).getConsentResource(any(), anyString());

            consentCoreServiceImpl.amendConsentData(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                    ConsentMgtServiceTestData.SAMPLE_USER_ID);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendConsentDataInsertError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doNothing().when(mockedConsentCoreDAO).updateConsentReceipt(any(),
                    anyString(), anyString());
            doNothing().when(mockedConsentCoreDAO).updateConsentValidityTime(any(),
                    anyString(), Mockito.anyLong());
            doReturn(ConsentMgtServiceTestData.getSampleStoredConsentResource())
                    .when(mockedConsentCoreDAO).getConsentResource(any(), anyString());
            doThrow(ConsentDataInsertionException.class)
                    .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                            any(ConsentStatusAuditRecord.class));

            consentCoreServiceImpl.amendConsentData(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                    ConsentMgtServiceTestData.SAMPLE_USER_ID);
        }
    }

    @Test
    public void testAmendDetailedConsentData() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            setInitialDataForAmendDetailedConsentSuccessFlow();
            DetailedConsentResource detailedConsentResource =
                    consentCoreServiceImpl.amendDetailedConsent(sampleID,
                            ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                            ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                            ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                            ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                            ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                            ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP,
                            ConsentMgtServiceTestData.SAMPLE_USER_ID,
                            new HashMap<>());

            Assert.assertNotNull(detailedConsentResource);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendDetailedConsentDataWithoutConsentID() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.amendDetailedConsent(null, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                    ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                    ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP,
                    ConsentMgtServiceTestData.SAMPLE_USER_ID,
                    new HashMap<>());
        }
    }

    @Test
    public void testAmendDetailedConsentDataWithoutReceiptOnly() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            setInitialDataForAmendDetailedConsentSuccessFlow();
            DetailedConsentResource detailedConsentResource =
                    consentCoreServiceImpl.amendDetailedConsent(sampleID,
                            ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                            null,
                            ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                            ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                            ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                            ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP,
                            ConsentMgtServiceTestData.SAMPLE_USER_ID,
                            new HashMap<>());

            Assert.assertNotNull(detailedConsentResource);
        }
    }

    @Test
    public void testAmendDetailedConsentDataWithoutValidityTimeOnly() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            setInitialDataForAmendDetailedConsentSuccessFlow();
            DetailedConsentResource detailedConsentResource =
                    consentCoreServiceImpl.amendDetailedConsent(sampleID, null,
                            ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                            ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                            ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                            ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                            ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP,
                            ConsentMgtServiceTestData.SAMPLE_USER_ID,
                            new HashMap<>());

            Assert.assertNotNull(detailedConsentResource);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendDetailedConsentDataWithoutReceiptAndValidityTime() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.amendDetailedConsent(sampleID, null, null,
                    ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                    ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP,
                    ConsentMgtServiceTestData.SAMPLE_USER_ID,
                    new HashMap<>());
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendDetailedConsentDataWithoutUserId() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.amendDetailedConsent(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                    ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                    ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP, null,
                    new HashMap<>());
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendDetailedConsentDataWithoutAuthId() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.amendDetailedConsent(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD, null,
                    ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                    new HashMap<>());
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendDetailedConsentDataWithoutNewConsentStatus() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.amendDetailedConsent(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                    ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                    ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP, null,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                    new HashMap<>());
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendDetailedConsentDataWithoutNewConsentAttributes() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.amendDetailedConsent(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                    ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                    ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, null,
                    ConsentMgtServiceTestData.SAMPLE_USER_ID,
                    new HashMap<>());
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendDetailedConsentDataWithoutAccountIdMapWithPermissions() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            consentCoreServiceImpl.amendDetailedConsent(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                    ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                    null,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                    new HashMap<>());
        }
    }

    @Test
    public void testAmendDetailedConsentDataWithAdditionalAmendmentData() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            setInitialDataForAmendDetailedConsentSuccessFlow();
            doReturn(new AuthorizationResource()).when(mockedConsentCoreDAO)
                    .storeAuthorizationResource(any(), any(AuthorizationResource.class));
            doReturn(new ConsentMappingResource()).when(mockedConsentCoreDAO)
                    .storeConsentMappingResource(any(), any(ConsentMappingResource.class));

            DetailedConsentResource detailedConsentResource =
                    consentCoreServiceImpl.amendDetailedConsent(sampleID, ConsentMgtServiceTestData
                                    .SAMPLE_CONSENT_RECEIPT,
                            ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                            ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                            ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                            ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                            ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP,
                            ConsentMgtServiceTestData.SAMPLE_USER_ID,
                            ConsentMgtServiceTestData.getSampleAdditionalConsentAmendmentDataMap());

            Assert.assertNotNull(detailedConsentResource);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendDetailedConsentDataWithAdditionalAmendmentDataWithoutConsentIdInAuthResources()
            throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            setInitialDataForAmendDetailedConsentSuccessFlow();
            consentCoreServiceImpl.amendDetailedConsent(sampleID, ConsentMgtServiceTestData
                            .SAMPLE_CONSENT_RECEIPT,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                    ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                    ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP,
                    ConsentMgtServiceTestData.SAMPLE_USER_ID,
                    ConsentMgtServiceTestData.getSampleAdditionalConsentAmendmentDataMapWithoutConsentId());
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendDetailedConsentDataWithAdditionalAmendmentDataWithoutAccountIdInMappingResources()
            throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            setInitialDataForAmendDetailedConsentSuccessFlow();
            consentCoreServiceImpl.amendDetailedConsent(sampleID, ConsentMgtServiceTestData
                            .SAMPLE_CONSENT_RECEIPT,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                    ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                    ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP,
                    ConsentMgtServiceTestData.SAMPLE_USER_ID,
                    ConsentMgtServiceTestData.getSampleAdditionalConsentAmendmentDataMapWithoutAccountId());
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendDetailedConsentDataUpdateError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doThrow(ConsentDataUpdationException.class)
                    .when(mockedConsentCoreDAO).updateConsentReceipt(any(), anyString(),
                            anyString());

            consentCoreServiceImpl.amendDetailedConsent(sampleID, ConsentMgtServiceTestData
                            .SAMPLE_CONSENT_RECEIPT,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                    ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                    ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP,
                    ConsentMgtServiceTestData.SAMPLE_USER_ID,
                    new HashMap<>());
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendDetailedConsentDataRetrieveError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doNothing().when(mockedConsentCoreDAO).updateConsentReceipt(any(),
                    anyString(), anyString());
            doNothing().when(mockedConsentCoreDAO).updateConsentValidityTime(any(),
                    anyString(), Mockito.anyLong());
            doThrow(ConsentDataRetrievalException.class)
                    .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());

            consentCoreServiceImpl.amendDetailedConsent(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                    ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                    ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP,
                    ConsentMgtServiceTestData.SAMPLE_USER_ID,
                    new HashMap<>());
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendDetailedConsentDataInsertError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doNothing().when(mockedConsentCoreDAO).updateConsentReceipt(any(),
                    anyString(), anyString());
            doNothing().when(mockedConsentCoreDAO).updateConsentValidityTime(any(),
                    anyString(), Mockito.anyLong());
            doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                    .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());
            doThrow(ConsentDataInsertionException.class)
                    .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                            any(ConsentStatusAuditRecord.class));

            consentCoreServiceImpl.amendDetailedConsent(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                    ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                    ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP,
                    ConsentMgtServiceTestData.SAMPLE_USER_ID,
                    new HashMap<>());
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendDetailedConsentDataDeletionError() throws Exception {

        try (MockedStatic<DatabaseUtil> databaseUtilMock = mockStatic(DatabaseUtil.class);
             MockedStatic<ConsentStoreInitializer> consentStoreMock = mockStatic(ConsentStoreInitializer.class);
             MockedStatic<ConsentManagementDataHolder> dataHolderMock = mockStatic(ConsentManagementDataHolder.class);
             MockedStatic<TokenRevocationUtil> tokenRevocationUtilMock = mockStatic(TokenRevocationUtil.class)) {

            mockStaticClasses(databaseUtilMock, consentStoreMock, dataHolderMock, tokenRevocationUtilMock);

            doNothing().when(mockedConsentCoreDAO).updateConsentReceipt(any(),
                    anyString(), anyString());
            doNothing().when(mockedConsentCoreDAO).updateConsentValidityTime(any(),
                    anyString(), Mockito.anyLong());
            doReturn(ConsentMgtServiceTestData.getSampleStoredConsentResource())
                    .when(mockedConsentCoreDAO).getConsentResource(any(), anyString());
            doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentStatusAuditRecord(sampleID,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                    .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                            any(ConsentStatusAuditRecord.class));

            doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                    .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());
            doReturn(ConsentMgtServiceTestData
                    .getSampleTestConsentMappingResource(ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID))
                    .when(mockedConsentCoreDAO).storeConsentMappingResource(any(),
                            any(ConsentMappingResource.class));
            doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
                    any(), anyString());

            doThrow(ConsentDataDeletionException.class).when(mockedConsentCoreDAO)
                    .deleteConsentAttributes(any(), anyString(), any());
            doReturn(true).when(mockedConsentCoreDAO).storeConsentAttributes(any(),
                    any(ConsentAttributes.class));
            consentCoreServiceImpl.amendDetailedConsent(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                    ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                    ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                    ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                    ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP,
                    ConsentMgtServiceTestData.SAMPLE_USER_ID,
                    new HashMap<>());
        }
    }

    private void setInitialDataForAmendDetailedConsentSuccessFlow() throws Exception {

        doNothing().when(mockedConsentCoreDAO).updateConsentReceipt(any(),
                anyString(), anyString());
        doNothing().when(mockedConsentCoreDAO).updateConsentValidityTime(any(),
                anyString(), Mockito.anyLong());
        doReturn(ConsentMgtServiceTestData.getSampleStoredConsentResource())
                .when(mockedConsentCoreDAO).getConsentResource(any(), anyString());
        doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentStatusAuditRecord(sampleID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(any(),
                        any(ConsentStatusAuditRecord.class));

        doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(any(), anyString());
        doReturn(ConsentMgtServiceTestData
                .getSampleTestConsentMappingResource(ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID))
                .when(mockedConsentCoreDAO).storeConsentMappingResource(any(),
                        any(ConsentMappingResource.class));
        doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(any(),
                any(), anyString());

        doReturn(true).when(mockedConsentCoreDAO).deleteConsentAttributes(any(),
                anyString(), any());
        doReturn(true).when(mockedConsentCoreDAO).storeConsentAttributes(any(),
                any(ConsentAttributes.class));
    }
}
