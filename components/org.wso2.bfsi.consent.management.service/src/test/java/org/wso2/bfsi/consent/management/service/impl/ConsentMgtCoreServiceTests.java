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
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
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
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dto.OAuthRevocationResponseDTO;
import org.wso2.carbon.identity.oauth2.model.AccessTokenDO;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

/**
 * Test for Open Banking consent management core service.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({DatabaseUtil.class, ConsentStoreInitializer.class, ConsentManagementDataHolder.class,
        TokenRevocationUtil.class})
public class ConsentMgtCoreServiceTests extends PowerMockTestCase {

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
    public void mock() throws ConsentManagementException, IdentityOAuth2Exception {

        sampleID = UUID.randomUUID().toString();
        mockStaticClasses();
    }

    private void mockStaticClasses() throws ConsentManagementException, IdentityOAuth2Exception {

        PowerMockito.mockStatic(DatabaseUtil.class);
        PowerMockito.when(DatabaseUtil.getDBConnection()).thenReturn(connectionMock);

        PowerMockito.mockStatic(ConsentStoreInitializer.class);
        PowerMockito.when(ConsentStoreInitializer.getInitializedConsentCoreDAOImpl())
        .thenReturn(mockedConsentCoreDAO);

        PowerMockito.mockStatic(ConsentManagementDataHolder.class);
        PowerMockito.when(ConsentManagementDataHolder.getInstance()).thenReturn(consentManagementDataHolderMock);

        PowerMockito.mockStatic(TokenRevocationUtil.class);
        PowerMockito.when(TokenRevocationUtil.getAuthenticatedUser(Mockito.anyString()))
                .thenReturn(new AuthenticatedUser());
        PowerMockito.when(TokenRevocationUtil.getAccessTokenDOSet(Mockito.any(), Mockito.any()))
                .thenReturn(new HashSet<AccessTokenDO>());
        PowerMockito.when(TokenRevocationUtil.revokeTokenByClient(Mockito.any(), Mockito.any()))
                .thenReturn(new OAuthRevocationResponseDTO());
    }

    @Test(priority = 2)
    public void testCreateAuthorizableConsent() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentResource()).when(mockedConsentCoreDAO)
                .storeConsentResource(Mockito.any(), Mockito.any());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleTestAuthorizationResource(null, null))
                .when(mockedConsentCoreDAO).storeAuthorizationResource(Mockito.any(), Mockito.any());

        DetailedConsentResource detailedConsentResource =
                consentCoreServiceImpl.createAuthorizableConsent(ConsentMgtServiceTestData
                                .getSampleTestConsentResource(), ConsentMgtServiceTestData.SAMPLE_USER_ID,
                        ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                        ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_TYPE, true);

        Assert.assertNotNull(detailedConsentResource);
        Assert.assertNotNull(detailedConsentResource.getConsentID());
        Assert.assertNotNull(detailedConsentResource.getClientID());
        Assert.assertNotNull(detailedConsentResource.getReceipt());
        Assert.assertNotNull(detailedConsentResource.getConsentType());
        Assert.assertNotNull(detailedConsentResource.getCurrentStatus());
    }

    @Test (priority = 2)
    public void testCreateAuthorizableConsentWithAttributes() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentResource()).when(mockedConsentCoreDAO)
                .storeConsentResource(Mockito.any(), Mockito.any());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleTestAuthorizationResource(null, null))
                .when(mockedConsentCoreDAO).storeAuthorizationResource(Mockito.any(), Mockito.any());
        Mockito.doReturn(true).when(mockedConsentCoreDAO).storeConsentAttributes(Mockito.any(),
                Mockito.any());

        DetailedConsentResource detailedConsentResource =
                consentCoreServiceImpl.createAuthorizableConsent(ConsentMgtServiceTestData
                                .getSampleStoredTestConsentResourceWithAttributes(),
                        ConsentMgtServiceTestData.SAMPLE_USER_ID,
                        ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                        ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_TYPE, true);

        Assert.assertNotNull(detailedConsentResource);
        Assert.assertNotNull(detailedConsentResource.getConsentID());
        Assert.assertNotNull(detailedConsentResource.getClientID());
        Assert.assertNotNull(detailedConsentResource.getReceipt());
        Assert.assertNotNull(detailedConsentResource.getConsentType());
        Assert.assertNotNull(detailedConsentResource.getCurrentStatus());
        Assert.assertNotNull(detailedConsentResource.getConsentAttributes());
    }

    @Test (priority = 2)
    public void testCreateAuthorizableConsentWithoutUserID() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentResource()).when(mockedConsentCoreDAO)
                .storeConsentResource(Mockito.any(), Mockito.any());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleTestAuthorizationResource(null, null))
                .when(mockedConsentCoreDAO).storeAuthorizationResource(Mockito.any(), Mockito.any());

        DetailedConsentResource detailedConsentResource =
                consentCoreServiceImpl.createAuthorizableConsent(ConsentMgtServiceTestData
                                .getSampleStoredTestConsentResourceWithAttributes(), null,
                        ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                        ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_TYPE, true);

        Assert.assertNotNull(detailedConsentResource);
        Assert.assertNotNull(detailedConsentResource.getConsentID());
        Assert.assertNotNull(detailedConsentResource.getClientID());
        Assert.assertNotNull(detailedConsentResource.getReceipt());
        Assert.assertNotNull(detailedConsentResource.getConsentType());
        Assert.assertNotNull(detailedConsentResource.getCurrentStatus());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateAuthorizableConsentWithoutClientID() throws Exception {

        ConsentResource consentResource = ConsentMgtServiceTestData.getSampleTestConsentResource();
        consentResource.setClientID(null);

        consentCoreServiceImpl.createAuthorizableConsent(consentResource, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_TYPE,
                false);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateAuthorizableConsentWithoutReceipt() throws Exception {

        ConsentResource consentResource = ConsentMgtServiceTestData.getSampleTestConsentResource();
        consentResource.setReceipt(null);

        consentCoreServiceImpl.createAuthorizableConsent(consentResource, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_TYPE,
                false);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateAuthorizableConsentWithoutConsentType() throws Exception {

        ConsentResource consentResource = ConsentMgtServiceTestData.getSampleTestConsentResource();
        consentResource.setConsentType(null);

        consentCoreServiceImpl.createAuthorizableConsent(consentResource, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_TYPE,
                false);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateAuthorizableConsentWithoutCurrentStatus() throws Exception {

        ConsentResource consentResource = ConsentMgtServiceTestData.getSampleTestConsentResource();
        consentResource.setCurrentStatus(null);

        consentCoreServiceImpl.createAuthorizableConsent(consentResource, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_TYPE,
                false);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateAuthorizableConsentWithImplicitAndNoAuthStatus() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentResource()).when(mockedConsentCoreDAO)
                .storeConsentResource(Mockito.any(), Mockito.any());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleTestAuthorizationResource(null, null))
                .when(mockedConsentCoreDAO).storeAuthorizationResource(Mockito.any(), Mockito.any());

        consentCoreServiceImpl.createAuthorizableConsent(ConsentMgtServiceTestData
                        .getSampleStoredTestConsentResourceWithAttributes(), null, null,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_TYPE, true);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateAuthorizableConsentWithImplicitAndNoAuthType() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentResource()).when(mockedConsentCoreDAO)
                .storeConsentResource(Mockito.any(), Mockito.any());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleTestAuthorizationResource(null, null))
                .when(mockedConsentCoreDAO).storeAuthorizationResource(Mockito.any(), Mockito.any());

        consentCoreServiceImpl.createAuthorizableConsent(ConsentMgtServiceTestData
                        .getSampleStoredTestConsentResourceWithAttributes(), null,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS, null, true);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testCreateAuthorizableConsentRollback() throws Exception {

        Mockito.doThrow(ConsentDataInsertionException.class).when(mockedConsentCoreDAO)
                .storeConsentResource(Mockito.any(), Mockito.any());

        consentCoreServiceImpl.createAuthorizableConsent(ConsentMgtServiceTestData
                        .getSampleTestConsentResource(), ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_TYPE, true);
    }

    @Test
    public void testCreateExclusiveConsent() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResourcesList())
                .when(mockedConsentCoreDAO).searchConsents(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                        Mockito.any(), Mockito.any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(),
                        Mockito.anyInt());
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentStatus(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData
                        .getSampleTestConsentStatusAuditRecord(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(Mockito.any(),
                        Mockito.any(ConsentStatusAuditRecord.class));
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(Mockito.any(),
                Mockito.any(ArrayList.class), Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleTestConsentResource())
                .when(mockedConsentCoreDAO).storeConsentResource(Mockito.any(), Mockito.any(ConsentResource.class));
        Mockito.doReturn(true).when(mockedConsentCoreDAO).storeConsentAttributes(Mockito.any(),
                Mockito.any(ConsentAttributes.class));
        Mockito.doReturn(ConsentMgtServiceTestData
                        .getSampleTestAuthorizationResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID, null))
                .when(mockedConsentCoreDAO).storeAuthorizationResource(Mockito.any(),
                        Mockito.any(AuthorizationResource.class));

        DetailedConsentResource exclusiveConsent =
                consentCoreServiceImpl.createExclusiveConsent(ConsentMgtServiceTestData
                                .getSampleStoredTestConsentResource(),
                        ConsentMgtServiceTestData.SAMPLE_USER_ID,
                        ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                        ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
        Assert.assertNotNull(exclusiveConsent);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentDataRetrieveError() throws Exception {

        Mockito.doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).searchConsents(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentResource()).when(mockedConsentCoreDAO)
                .storeConsentResource(Mockito.any(), Mockito.any());

        consentCoreServiceImpl.createExclusiveConsent(ConsentMgtServiceTestData.getSampleStoredTestConsentResource(),
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, false);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentDataUpdateError() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResourcesList())
                .when(mockedConsentCoreDAO).searchConsents(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.doThrow(ConsentDataUpdationException.class)
                .when(mockedConsentCoreDAO).updateConsentStatus(Mockito.any(), Mockito.anyString(),
                        Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentResource()).when(mockedConsentCoreDAO)
                .storeConsentResource(Mockito.any(), Mockito.any());

        consentCoreServiceImpl.createExclusiveConsent(ConsentMgtServiceTestData.getSampleStoredTestConsentResource(),
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentDataInsertError() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResourcesList())
                .when(mockedConsentCoreDAO).searchConsents(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                        Mockito.any(), Mockito.any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(),
                        Mockito.anyInt());
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentStatus(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentResource()).when(mockedConsentCoreDAO)
                .storeConsentResource(Mockito.any(), Mockito.any());
        Mockito.doThrow(ConsentDataInsertionException.class)
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(Mockito.any(),
                        Mockito.any(ConsentStatusAuditRecord.class));

        consentCoreServiceImpl.createExclusiveConsent(ConsentMgtServiceTestData.getSampleStoredTestConsentResource(),
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentWithoutClientID() throws Exception {

        ConsentResource sampleConsentResource = ConsentMgtServiceTestData.getSampleStoredTestConsentResource();
        sampleConsentResource.setClientID(null);

        consentCoreServiceImpl.createExclusiveConsent(sampleConsentResource,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentWithoutReceipt() throws Exception {

        ConsentResource sampleConsentResource = ConsentMgtServiceTestData.getSampleStoredTestConsentResource();
        sampleConsentResource.setReceipt(null);

        consentCoreServiceImpl.createExclusiveConsent(sampleConsentResource,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentWithoutConsentType() throws Exception {

        ConsentResource sampleConsentResource = ConsentMgtServiceTestData.getSampleStoredTestConsentResource();
        sampleConsentResource.setConsentType(null);

        consentCoreServiceImpl.createExclusiveConsent(sampleConsentResource,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentWithoutConsentStatus() throws Exception {

        ConsentResource sampleConsentResource = ConsentMgtServiceTestData.getSampleStoredTestConsentResource();
        sampleConsentResource.setCurrentStatus(null);

        consentCoreServiceImpl.createExclusiveConsent(sampleConsentResource,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentWithoutUserID() throws Exception {

        consentCoreServiceImpl.createExclusiveConsent(ConsentMgtServiceTestData.getSampleStoredTestConsentResource(),
                null, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentWithoutAuthStatus() throws Exception {

        consentCoreServiceImpl.createExclusiveConsent(ConsentMgtServiceTestData.getSampleStoredTestConsentResource(),
                ConsentMgtServiceTestData.SAMPLE_USER_ID, null,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentWithoutAuthType() throws Exception {

        consentCoreServiceImpl.createExclusiveConsent(ConsentMgtServiceTestData.getSampleStoredTestConsentResource(),
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                null, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
    }

    @Test
    public void testCreateExclusiveConsentWithImplicitAuthFalse() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleTestConsentResource())
                .when(mockedConsentCoreDAO).storeConsentResource(Mockito.any(), Mockito.any());

        DetailedConsentResource consentResource = consentCoreServiceImpl
                .createExclusiveConsent(ConsentMgtServiceTestData.getSampleStoredTestConsentResource(),
                        ConsentMgtServiceTestData.SAMPLE_USER_ID,
                        ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                        null, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, false);
        Assert.assertNotNull(consentResource);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentWithoutApplicableExistingConsentStatus() throws Exception {

        consentCoreServiceImpl.createExclusiveConsent(ConsentMgtServiceTestData.getSampleStoredTestConsentResource(),
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, null,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, true);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentWithoutNewExistingConsentStatus() throws Exception {

        consentCoreServiceImpl.createExclusiveConsent(ConsentMgtServiceTestData.getSampleStoredTestConsentResource(),
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                null, true);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateExclusiveConsentWithoutNewCurrentConsentStatus() throws Exception {

        consentCoreServiceImpl.createExclusiveConsent(ConsentMgtServiceTestData.getSampleStoredTestConsentResource(),
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                null, true);
    }

    @Test
    public void testGetConsent() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentResource()).when(mockedConsentCoreDAO)
                .getConsentResource(Mockito.any(), Mockito.anyString());

        // Get consent
        ConsentResource retrievedConsentResource = consentCoreServiceImpl.getConsent(ConsentMgtServiceTestData
                .getSampleStoredTestConsentResource().getConsentID(), false);

        Assert.assertNotNull(retrievedConsentResource);
    }

    @Test
    public void testGetConsentWithAttributes() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentResourceWithAttributes())
                .when(mockedConsentCoreDAO).getConsentResourceWithAttributes(Mockito.any(), Mockito.anyString());

        // Get consent
        ConsentResource retrievedConsentResource = consentCoreServiceImpl.getConsent(ConsentMgtServiceTestData
                .getSampleStoredTestConsentResource().getConsentID(), true);

        Assert.assertNotNull(retrievedConsentResource);
        Assert.assertNotNull(retrievedConsentResource.getConsentAttributes());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentRollBackWhenRetrieve() throws Exception {

        Mockito.doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .getConsentResource(Mockito.any(), Mockito.anyString());

        // Get consent
        consentCoreServiceImpl.getConsent(ConsentMgtServiceTestData
        .getSampleStoredTestConsentResource().getConsentID(),
                false);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentWithoutConsentID() throws Exception {

        consentCoreServiceImpl.getConsent(null, false);
    }

    @Test
    public void testGetDetailedConsent() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString());

        // Get consent
        DetailedConsentResource retrievedConsentResource = consentCoreServiceImpl
                .getDetailedConsent(ConsentMgtServiceTestData.getSampleStoredTestConsentResource().getConsentID());

        Assert.assertNotNull(retrievedConsentResource);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testGetDetailedConsentWithoutConsentID() throws Exception {

        // Get consent
        consentCoreServiceImpl.getDetailedConsent(null);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testGetDetailedConsentWithDataRetrievalException() throws Exception {

        Mockito.doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(Mockito.any(), Mockito.anyString());

        // Get consent
        consentCoreServiceImpl.getDetailedConsent(ConsentMgtServiceTestData
                .getSampleStoredTestConsentResource().getConsentID());
    }

    @Test
    public void testCreateConsentFile() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData
                        .getSampleTestConsentResource(ConsentMgtServiceTestData.AWAITING_UPLOAD_STATUS))
                .when(mockedConsentCoreDAO).getConsentResource(Mockito.any(), Mockito.anyString());
        Mockito.doReturn(true).when(mockedConsentCoreDAO).storeConsentFile(Mockito.any(), Mockito.any());
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentStatus(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());

        consentCoreServiceImpl.createConsentFile(ConsentMgtServiceTestData
                        .getSampleConsentFileObject(ConsentMgtServiceTestData.SAMPLE_CONSENT_FILE),
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.AWAITING_UPLOAD_STATUS);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentFileErrorWhenRetrieval() throws Exception {

        Mockito.doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .getConsentResource(Mockito.any(), Mockito.anyString());

        consentCoreServiceImpl.createConsentFile(ConsentMgtServiceTestData
                        .getSampleConsentFileObject(ConsentMgtServiceTestData.SAMPLE_CONSENT_FILE),
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.AWAITING_UPLOAD_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentFileRollBackWhenCreation() throws Exception {

        ConsentResource storedConsentResource = ConsentMgtServiceTestData
                .getSampleStoredTestConsentResource(ConsentMgtServiceTestData.AWAITING_UPLOAD_STATUS);

        Mockito.doReturn(storedConsentResource).when(mockedConsentCoreDAO)
                .getConsentResource(Mockito.any(), Mockito.anyString());
        Mockito.doThrow(ConsentDataInsertionException.class).when(mockedConsentCoreDAO)
                .storeConsentFile(Mockito.any(), Mockito.any());

        consentCoreServiceImpl.createConsentFile(ConsentMgtServiceTestData
                        .getSampleConsentFileObject(ConsentMgtServiceTestData.SAMPLE_CONSENT_FILE),
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.AWAITING_UPLOAD_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentFileRollBackWhenUpdating() throws Exception {

        ConsentResource storedConsentResource = ConsentMgtServiceTestData
                .getSampleStoredTestConsentResource(ConsentMgtServiceTestData.AWAITING_UPLOAD_STATUS);

        Mockito.doReturn(storedConsentResource).when(mockedConsentCoreDAO)
                .getConsentResource(Mockito.any(), Mockito.anyString());
        Mockito.doThrow(ConsentDataUpdationException.class).when(mockedConsentCoreDAO)
                .updateConsentStatus(Mockito.any(), Mockito.anyString(), Mockito.anyString());

        consentCoreServiceImpl.createConsentFile(ConsentMgtServiceTestData
                        .getSampleConsentFileObject(ConsentMgtServiceTestData.SAMPLE_CONSENT_FILE),
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                storedConsentResource.getCurrentStatus());
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentFileWithInvalidStatus()
            throws Exception {

        ConsentResource consentResource = ConsentMgtServiceTestData.getSampleTestConsentResource();
        consentResource.setCurrentStatus(ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);

        Mockito.doReturn(consentResource).when(mockedConsentCoreDAO).getConsentResource(Mockito.any(),
                Mockito.anyString());

        // Create consent file
        consentCoreServiceImpl.createConsentFile(ConsentMgtServiceTestData
                        .getSampleConsentFileObject(ConsentMgtServiceTestData.SAMPLE_CONSENT_FILE),
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.AWAITING_UPLOAD_STATUS);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentFileWithoutFileContent() throws Exception {

        consentCoreServiceImpl.createConsentFile(ConsentMgtServiceTestData
                        .getSampleConsentFileObject(null), ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.AWAITING_UPLOAD_STATUS);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentFileWithoutConsentID() throws Exception {

        ConsentFile sampleConsentFile =
                ConsentMgtServiceTestData.getSampleConsentFileObject(ConsentMgtServiceTestData.SAMPLE_CONSENT_FILE);

        sampleConsentFile.setConsentID(null);
        consentCoreServiceImpl.createConsentFile(sampleConsentFile, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.AWAITING_UPLOAD_STATUS);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentFileWithoutNewConsentStatus()
            throws Exception {

        consentCoreServiceImpl.createConsentFile(ConsentMgtServiceTestData
                        .getSampleConsentFileObject(ConsentMgtServiceTestData.SAMPLE_CONSENT_FILE),
                null, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.AWAITING_UPLOAD_STATUS);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentFileWithoutApplicableStatusForFileUpload()
            throws Exception {

        consentCoreServiceImpl.createConsentFile(ConsentMgtServiceTestData
                        .getSampleConsentFileObject(ConsentMgtServiceTestData.SAMPLE_CONSENT_FILE),
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                null);
    }

    @Test
    public void testGetConsentFileConsentData() throws Exception {

        Mockito.doReturn(new ConsentFile()).when(mockedConsentCoreDAO)
                .getConsentFile(Mockito.any(), Mockito.anyString());
        ConsentFile consentFile = consentCoreServiceImpl
                .getConsentFile("3d22259e-942c-46b8-8f75-a608c677a6e6");
        Assert.assertNotNull(consentFile);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testGetConsentFileWithoutConsentId() throws Exception {

        consentCoreServiceImpl.getConsentFile(null);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testGetConsentFileWithDataRetrievalError() throws Exception {

        Mockito.doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .getConsentFile(Mockito.any(), Mockito.anyString());
        ConsentFile consentFile = consentCoreServiceImpl
                .getConsentFile("3d22259e-942c-46b8-8f75-a608c677a6e6");
        Assert.assertNotNull(consentFile);
    }

    @Test
    public void testCreateConsentAuthorization() throws Exception {

        AuthorizationResource sampleAuthorizationResource =
                ConsentMgtServiceTestData.getSampleTestAuthorizationResource(sampleID, null);

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestAuthorizationResource())
                .when(mockedConsentCoreDAO).storeAuthorizationResource(Mockito.any(), Mockito.any());

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

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentAuthorizationRollbackWhenCreation() throws Exception {

        AuthorizationResource sampleAuthorizationResource =
                ConsentMgtServiceTestData.getSampleTestAuthorizationResource(sampleID, null);

        Mockito.doThrow(ConsentDataInsertionException.class).when(mockedConsentCoreDAO)
                .storeAuthorizationResource(Mockito.any(), Mockito.any());

        // Get consent
        consentCoreServiceImpl.createConsentAuthorization(sampleAuthorizationResource);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentAuthorizationWithoutConsentID() throws Exception {

        AuthorizationResource sampleAuthorizationResource =
                ConsentMgtServiceTestData.getSampleTestAuthorizationResource(null, null);
        sampleAuthorizationResource.setConsentID(null);

        //Create a consent authorization resource
        consentCoreServiceImpl.createConsentAuthorization(sampleAuthorizationResource);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentAuthorizationWithoutAuthorizationStatus() throws Exception {

        AuthorizationResource sampleAuthorizationResource =
                ConsentMgtServiceTestData.getSampleTestAuthorizationResource(sampleID, null);

        // Explicitly setting authorization status to null
        sampleAuthorizationResource.setAuthorizationStatus(null);

        //Create a consent authorization resource
        consentCoreServiceImpl.createConsentAuthorization(sampleAuthorizationResource);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentAuthorizationWithoutAuthorizationType() throws Exception {

        AuthorizationResource sampleAuthorizationResource =
                ConsentMgtServiceTestData.getSampleTestAuthorizationResource(sampleID, null);
        sampleAuthorizationResource.setAuthorizationType(null);

        //Create a consent authorization resource
        consentCoreServiceImpl.createConsentAuthorization(sampleAuthorizationResource);
    }

    @Test
    public void testGetAuthorizationResource() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestAuthorizationResource())
                .when(mockedConsentCoreDAO).getAuthorizationResource(Mockito.any(), Mockito.anyString());
        AuthorizationResource authorizationResource =
                consentCoreServiceImpl.getAuthorizationResource(ConsentMgtServiceTestData
                        .getSampleStoredTestAuthorizationResource().getAuthorizationID());
        Assert.assertNotNull(authorizationResource);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetAuthorizationResourceWithoutAuthID() throws Exception {

        consentCoreServiceImpl.getAuthorizationResource(null);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetAuthorizationResourceDataRetrieveError() throws Exception {

        Mockito.doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getAuthorizationResource(Mockito.any(), Mockito.anyString());
        consentCoreServiceImpl.getAuthorizationResource(ConsentMgtServiceTestData
                .getSampleStoredTestAuthorizationResource().getAuthorizationID());
    }

    @Test
    public void testSearchAuthorizationsWithConsentID() throws Exception {

        ArrayList<String> consentIDs = new ArrayList<>();
        consentIDs.add(UUID.randomUUID().toString());

        Mockito.doReturn(ConsentMgtServiceTestData
                        .getSampleAuthorizationResourcesList(consentIDs))
                .when(mockedConsentCoreDAO).searchConsentAuthorizations(Mockito.any(), Mockito.anyString(),
                        Mockito.anyString());
        ArrayList<AuthorizationResource> retrievedAuthorizations =
                consentCoreServiceImpl.searchAuthorizations(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID);
        Assert.assertNotNull(retrievedAuthorizations);
    }

    @Test
    public void testSearchAuthorizationsWithUserID() throws Exception {

        ArrayList<String> consentIDs = new ArrayList<>();
        consentIDs.add(UUID.randomUUID().toString());

        Mockito.doReturn(ConsentMgtServiceTestData
                        .getSampleAuthorizationResourcesList(consentIDs))
                .when(mockedConsentCoreDAO).searchConsentAuthorizations(Mockito.any(), Mockito.anyString(),
                        Mockito.anyString());
        ArrayList<AuthorizationResource> retrievedAuthorizations =
                consentCoreServiceImpl.searchAuthorizationsForUser(ConsentMgtServiceTestData.SAMPLE_USER_ID);
        Assert.assertNotNull(retrievedAuthorizations);
    }

    @Test
    public void testSearchAuthorizations() throws Exception {

        ArrayList<String> consentIDs = new ArrayList<>();
        consentIDs.add(UUID.randomUUID().toString());

        Mockito.doReturn(ConsentMgtServiceTestData
                        .getSampleAuthorizationResourcesList(consentIDs))
                .when(mockedConsentCoreDAO).searchConsentAuthorizations(Mockito.any(), Mockito.anyString(),
                        Mockito.anyString());
        ArrayList<AuthorizationResource> retrievedAuthorizations =
                consentCoreServiceImpl.searchAuthorizations(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.SAMPLE_USER_ID);
        Assert.assertNotNull(retrievedAuthorizations);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testSearchAuthorizationsDataRetrieveError() throws Exception {

        Mockito.doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).searchConsentAuthorizations(Mockito.any(), Mockito.anyString(),
                        Mockito.anyString());

        consentCoreServiceImpl.searchAuthorizations(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_USER_ID);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testUpdateAuthorizationStatusWithoutAuthId() throws Exception {

        consentCoreServiceImpl.updateAuthorizationStatus(null,
                ConsentMgtServiceTestData.SAMPLE_CONSUMED_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testUpdateAuthorizationStatusWithoutNewAuthStatus() throws Exception {

        consentCoreServiceImpl.updateAuthorizationStatus(ConsentMgtServiceTestData.CONSENT_ID,
                null);
    }

    @Test
    public void testUpdateAuthorizationStatus() throws Exception {

        Mockito.doNothing().when(mockedConsentCoreDAO).updateAuthorizationStatus(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());

        consentCoreServiceImpl.updateAuthorizationStatus(ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSUMED_STATUS);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testUpdateAuthorizationStatusWithDataUpdateError() throws Exception {

        Mockito.doThrow(ConsentDataUpdationException.class).when(mockedConsentCoreDAO)
                .updateAuthorizationStatus(Mockito.any(), Mockito.anyString(), Mockito.anyString());

        consentCoreServiceImpl.updateAuthorizationStatus(ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSUMED_STATUS);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testUpdateAuthorizationStatusWithDataRetrievalError() throws Exception {

        Mockito.doNothing().when(mockedConsentCoreDAO).updateAuthorizationStatus(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());
        Mockito.doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .getAuthorizationResource(Mockito.any(), Mockito.anyString());

        consentCoreServiceImpl.updateAuthorizationStatus(ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSUMED_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testUpdateAuthorizationUserWithoutAuthorizationID() throws Exception {

        consentCoreServiceImpl.updateAuthorizationUser(null,
                ConsentMgtServiceTestData.SAMPLE_USER_ID);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testUpdateAuthorizationUserWithoutUserID() throws Exception {

        consentCoreServiceImpl.updateAuthorizationUser(ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_ID_1,
                null);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testUpdateAuthorizationUserWithDataUpdateError() throws Exception {

        Mockito.doThrow(ConsentDataUpdationException.class).when(mockedConsentCoreDAO)
                .updateAuthorizationUser(Mockito.any(), Mockito.anyString(), Mockito.anyString());

        consentCoreServiceImpl.updateAuthorizationUser(ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_ID_1,
                ConsentMgtServiceTestData.SAMPLE_USER_ID);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testUpdateAuthorizationUserWithDataRetrieveError() throws Exception {

        Mockito.doNothing().when(mockedConsentCoreDAO).updateAuthorizationUser(Mockito.any(),
                Mockito.anyString(), Mockito.anyString());
        Mockito.doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .getAuthorizationResource(Mockito.any(), Mockito.anyString());

        consentCoreServiceImpl.updateAuthorizationUser(ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_ID_1,
                ConsentMgtServiceTestData.SAMPLE_USER_ID);
    }

    @Test
    public void testUpdateAuthorizationUser() throws Exception {

        Mockito.doNothing().when(mockedConsentCoreDAO).updateAuthorizationUser(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());

        consentCoreServiceImpl.updateAuthorizationUser(ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_ID_1,
                ConsentMgtServiceTestData.SAMPLE_USER_ID);
    }

    @Test
    public void testBindUserAccountsToConsentWithAccountIdList() throws Exception {

        Mockito.doNothing().when(mockedConsentCoreDAO).updateAuthorizationUser(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());
        Mockito.doNothing().when(mockedConsentCoreDAO).updateAuthorizationStatus(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData
                        .getSampleTestConsentMappingResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID))
                .when(mockedConsentCoreDAO).storeConsentMappingResource(Mockito.any(),
                        Mockito.any(ConsentMappingResource.class));
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentStatus(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData
                        .getSampleTestConsentStatusAuditRecord(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(Mockito.any(),
                        Mockito.any(ConsentStatusAuditRecord.class));

        Assert.assertTrue(consentCoreServiceImpl
                .bindUserAccountsToConsent(ConsentMgtServiceTestData.getSampleStoredTestConsentResource(),
                        ConsentMgtServiceTestData.SAMPLE_USER_ID, "authID",
                        ConsentMgtServiceTestData.SAMPLE_ACCOUNT_ID_LIST,
                        ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS));
    }

    @Test
    public void testBindUserAccountsToConsent() throws Exception {

        Mockito.doNothing().when(mockedConsentCoreDAO).updateAuthorizationUser(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());
        Mockito.doNothing().when(mockedConsentCoreDAO).updateAuthorizationStatus(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData
                        .getSampleTestConsentMappingResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID))
                .when(mockedConsentCoreDAO).storeConsentMappingResource(Mockito.any(),
                        Mockito.any(ConsentMappingResource.class));
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentStatus(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData
                        .getSampleTestConsentStatusAuditRecord(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(Mockito.any(),
                        Mockito.any(ConsentStatusAuditRecord.class));

        Assert.assertTrue(consentCoreServiceImpl
                .bindUserAccountsToConsent(ConsentMgtServiceTestData.getSampleStoredTestConsentResource(),
                        ConsentMgtServiceTestData.SAMPLE_USER_ID, "authID",
                        ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                        ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS));
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testBindUserAccountsToConsentWithoutNewCurrentConsentStatus() throws Exception {

        consentCoreServiceImpl.bindUserAccountsToConsent(ConsentMgtServiceTestData
        .getSampleStoredTestConsentResource(),
                ConsentMgtServiceTestData.SAMPLE_USER_ID, "authID",
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS, null);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testBindUserAccountsToConsentWithoutConsentID() throws Exception {

        ConsentResource consentResource = ConsentMgtServiceTestData.getSampleStoredTestConsentResource();
        consentResource.setConsentID(null);

        consentCoreServiceImpl.bindUserAccountsToConsent(consentResource,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, "authID",
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testBindUserAccountsToConsentWithoutClientID() throws Exception {

        ConsentResource consentResource = ConsentMgtServiceTestData.getSampleStoredTestConsentResource();
        consentResource.setClientID(null);

        consentCoreServiceImpl.bindUserAccountsToConsent(consentResource,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, "authID",
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testBindUserAccountsToConsentWithoutConsentType() throws Exception {

        ConsentResource consentResource = ConsentMgtServiceTestData.getSampleStoredTestConsentResource();
        consentResource.setConsentType(null);

        consentCoreServiceImpl.bindUserAccountsToConsent(consentResource,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, "authID",
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testBindUserAccountsToConsentWithoutUserID() throws Exception {

        consentCoreServiceImpl.bindUserAccountsToConsent(ConsentMgtServiceTestData
        .getSampleStoredTestConsentResource(),
                null, "authID", ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testBindUserAccountsToConsentWithoutAuthID() throws Exception {

        consentCoreServiceImpl.bindUserAccountsToConsent(ConsentMgtServiceTestData
        .getSampleStoredTestConsentResource(),
                ConsentMgtServiceTestData.SAMPLE_USER_ID, null,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testBindUserAccountsToConsentWithoutNewAuthStatus() throws Exception {

        consentCoreServiceImpl.bindUserAccountsToConsent(ConsentMgtServiceTestData
        .getSampleStoredTestConsentResource(),
                ConsentMgtServiceTestData.SAMPLE_USER_ID, "authID",
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP, null,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testBindUserAccountsToConsentWithEmptyAccountsAndPermissionsMap() throws Exception {

        Assert.assertTrue(consentCoreServiceImpl
                .bindUserAccountsToConsent(ConsentMgtServiceTestData.getSampleStoredTestConsentResource(),
                        ConsentMgtServiceTestData.SAMPLE_USER_ID, "authID", new HashMap<>(),
                        ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS));
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testBindUserAccountsToConsentDataUpdateError() throws Exception {

        Mockito.doThrow(ConsentDataUpdationException.class)
                .when(mockedConsentCoreDAO).updateAuthorizationUser(Mockito.any(), Mockito.anyString(),
                        Mockito.anyString());
        consentCoreServiceImpl.bindUserAccountsToConsent(ConsentMgtServiceTestData
        .getSampleStoredTestConsentResource(),
                ConsentMgtServiceTestData.SAMPLE_USER_ID, "authID",
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testBindUserAccountsToConsentDataInsertError() throws Exception {

        Mockito.doThrow(ConsentDataInsertionException.class)
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(Mockito.any(),
                        Mockito.any(ConsentStatusAuditRecord.class));
        consentCoreServiceImpl.bindUserAccountsToConsent(ConsentMgtServiceTestData
        .getSampleStoredTestConsentResource(),
                ConsentMgtServiceTestData.SAMPLE_USER_ID, "authID",
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
    }

    @Test
    public void testUpdateConsentStatus() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString());

        consentCoreServiceImpl.updateConsentStatus(ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSUMED_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testUpdateConsentStatusDataRetrievalError() throws Exception {

        Mockito.doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString());

        consentCoreServiceImpl.updateConsentStatus(ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSUMED_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testUpdateConsentStatusDataUpdateError() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString());
        Mockito.doThrow(ConsentDataUpdationException.class)
                .when(mockedConsentCoreDAO).updateConsentStatus(Mockito.any(), Mockito.anyString(),
                        Mockito.anyString());

        consentCoreServiceImpl.updateConsentStatus(ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSUMED_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testUpdateConsentStatusDataInsertError() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString());
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentStatus(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());
        Mockito.doThrow(ConsentDataInsertionException.class)
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(Mockito.any(),
                        Mockito.any(ConsentStatusAuditRecord.class));

        consentCoreServiceImpl.updateConsentStatus(ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSUMED_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testUpdateConsentStatusWithoutConsentId() throws Exception {

        consentCoreServiceImpl.updateConsentStatus(null,
                ConsentMgtServiceTestData.SAMPLE_CONSUMED_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testUpdateConsentStatusWithoutConsentStatus() throws Exception {

        consentCoreServiceImpl.updateConsentStatus(ConsentMgtServiceTestData.CONSENT_ID,
                null);
    }

    @Test
    public void testCreateConsentAccountMapping() throws Exception {

        AuthorizationResource storedAuthorizationResource =
                ConsentMgtServiceTestData.getSampleStoredTestAuthorizationResource();

        ConsentMappingResource storedConsentMappingResource =
                ConsentMgtServiceTestData.getSampleStoredTestConsentMappingResource(sampleID);

        Mockito.doReturn(storedConsentMappingResource).when(mockedConsentCoreDAO)
                .storeConsentMappingResource(Mockito.any(), Mockito.any());

        ArrayList<ConsentMappingResource> storedConsentMappingResources =
                consentCoreServiceImpl.createConsentAccountMappings(storedAuthorizationResource.getAuthorizationID(),
                        ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP);

        Assert.assertNotNull(storedConsentMappingResources);
        for (ConsentMappingResource resource : storedConsentMappingResources) {
            Assert.assertNotNull(resource.getAccountID());
            Assert.assertNotNull(resource.getPermission());
            Assert.assertNotNull(resource.getAuthorizationID());
            Assert.assertEquals(resource.getMappingStatus(), ConsentCoreServiceConstants.ACTIVE_MAPPING_STATUS);
        }
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentAccountMappingRollBackWhenCreation() throws Exception {

        AuthorizationResource storedAuthorizationResource =
                ConsentMgtServiceTestData.getSampleStoredTestAuthorizationResource();

        Mockito.doThrow(ConsentDataInsertionException.class).when(mockedConsentCoreDAO)
                .storeConsentMappingResource(Mockito.any(), Mockito.any());

        consentCoreServiceImpl.createConsentAccountMappings(storedAuthorizationResource.getAuthorizationID(),
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentAccountMappingWithoutAuthID() throws Exception {

        consentCoreServiceImpl.createConsentAccountMappings(null,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testCreateConsentAccountMappingWithoutAccountAndPermissionsMap() throws Exception {

        consentCoreServiceImpl.createConsentAccountMappings(sampleID, new HashMap<>());
    }

    @Test
    public void testDeactivateAccountMappings() throws Exception {

        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(Mockito.any(),
                Mockito.any(), Mockito.any());
        Assert.assertTrue(consentCoreServiceImpl
                .deactivateAccountMappings(ConsentMgtServiceTestData.UNMATCHED_MAPPING_IDS));
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testDeactivateAccountMappingsWithEmptyMappingIDList() throws Exception {

        consentCoreServiceImpl.deactivateAccountMappings(new ArrayList<>());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testDeactivateAccountMappingsRollback() throws Exception {

        Mockito.doThrow(ConsentDataUpdationException.class).when(mockedConsentCoreDAO)
                .updateConsentMappingStatus(Mockito.any(), Mockito.any(), Mockito.any());
        consentCoreServiceImpl.deactivateAccountMappings(ConsentMgtServiceTestData.UNMATCHED_MAPPING_IDS);
    }

    @Test
    public void testUpdateAccountMappingStatus() throws Exception {

        Mockito.doNothing().when(mockedConsentCoreDAO)
                .updateConsentMappingStatus(Mockito.any(), Mockito.any(), Mockito.any());

        consentCoreServiceImpl.updateAccountMappingStatus(ConsentMgtServiceTestData.MAPPING_IDS_LIST,
                ConsentMgtServiceTestData.SAMPLE_MAPPING_STATUS);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testUpdateAccountMappingStatusWithoutMappingIds() throws Exception {

        Mockito.doNothing().when(mockedConsentCoreDAO)
                .updateConsentMappingStatus(Mockito.any(), Mockito.any(), Mockito.any());

        consentCoreServiceImpl.updateAccountMappingStatus(new ArrayList<>(),
                ConsentMgtServiceTestData.SAMPLE_MAPPING_STATUS);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testUpdateAccountMappingStatusDataUpdateError() throws Exception {

        Mockito.doThrow(ConsentDataUpdationException.class).when(mockedConsentCoreDAO)
                .updateConsentMappingStatus(Mockito.any(), Mockito.any(), Mockito.any());

        consentCoreServiceImpl.updateAccountMappingStatus(ConsentMgtServiceTestData.MAPPING_IDS_LIST,
                ConsentMgtServiceTestData.SAMPLE_MAPPING_STATUS);
    }

    @Test
    public void testRevokeConsent() throws Exception {

        DetailedConsentResource retrievedDetailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

        Mockito.doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(Mockito.any(), Mockito.any());
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentStatus(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleTestConsentStatusAuditRecord(
                        retrievedDetailedConsentResource.getConsentID(),
                        retrievedDetailedConsentResource.getCurrentStatus()))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(Mockito.any(),
                        Mockito.any(ConsentStatusAuditRecord.class));
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(Mockito.any(),
                Mockito.any(), Mockito.anyString());

        boolean isConsentRevoked = consentCoreServiceImpl.revokeConsentWithReason(
                ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, false,
                ConsentCoreServiceConstants.CONSENT_REVOKE_REASON);

        Assert.assertTrue(isConsentRevoked);
    }

    @Test
    public void testRevokeConsentWithReason() throws Exception {

        DetailedConsentResource retrievedDetailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

        Mockito.doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(Mockito.any(), Mockito.any());
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentStatus(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleTestConsentStatusAuditRecord(
                        retrievedDetailedConsentResource.getConsentID(),
                        retrievedDetailedConsentResource.getCurrentStatus()))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(Mockito.any(),
                        Mockito.any(ConsentStatusAuditRecord.class));
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(Mockito.any(),
                Mockito.any(), Mockito.anyString());

        boolean isConsentRevoked = consentCoreServiceImpl.revokeConsentWithReason(
                ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentCoreServiceConstants.CONSENT_REVOKE_REASON);

        Assert.assertTrue(isConsentRevoked);
    }

    @Test
    public void testRevokeConsentWithUserId() throws Exception {

        DetailedConsentResource retrievedDetailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

        Mockito.doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(Mockito.any(), Mockito.any());
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentStatus(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleTestConsentStatusAuditRecord(
                        retrievedDetailedConsentResource.getConsentID(),
                        retrievedDetailedConsentResource.getCurrentStatus()))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(Mockito.any(),
                        Mockito.any(ConsentStatusAuditRecord.class));
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(Mockito.any(),
                Mockito.any(), Mockito.anyString());

        boolean isConsentRevoked = consentCoreServiceImpl.revokeConsentWithReason(
                ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentCoreServiceConstants.CONSENT_REVOKE_REASON);

        Assert.assertTrue(isConsentRevoked);
    }

    @Test
    public void testRevokeConsentAndTokens() throws Exception {

        DetailedConsentResource retrievedDetailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

        Mockito.doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(Mockito.any(), Mockito.any());
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentStatus(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleTestConsentStatusAuditRecord(
                        retrievedDetailedConsentResource.getConsentID(),
                        retrievedDetailedConsentResource.getCurrentStatus()))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(Mockito.any(),
                        Mockito.any(ConsentStatusAuditRecord.class));
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(Mockito.any(),
                Mockito.any(), Mockito.anyString());

        boolean isConsentRevoked = consentCoreServiceImpl.revokeConsent(
        ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                true);

        Assert.assertTrue(isConsentRevoked);
    }

    @Test
    public void testRevokeConsentAndTokensTokenRevokeError() throws Exception {

        DetailedConsentResource retrievedDetailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

        Mockito.doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(Mockito.any(), Mockito.any());
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentStatus(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleTestConsentStatusAuditRecord(
                        retrievedDetailedConsentResource.getConsentID(),
                        retrievedDetailedConsentResource.getCurrentStatus()))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(Mockito.any(),
                        Mockito.any(ConsentStatusAuditRecord.class));
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(Mockito.any(),
                Mockito.any(), Mockito.anyString());
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

    @Test
    public void testRevokeConsentWithoutConsentAttributes() throws Exception {

        DetailedConsentResource retrievedDetailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();
        retrievedDetailedConsentResource.setConsentAttributes(null);

        Mockito.doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(Mockito.any(), Mockito.any());
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentStatus(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleTestConsentStatusAuditRecord(
                        retrievedDetailedConsentResource.getConsentID(),
                        retrievedDetailedConsentResource.getCurrentStatus()))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(Mockito.any(),
                        Mockito.any(ConsentStatusAuditRecord.class));
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(Mockito.any(),
                Mockito.any(), Mockito.anyString());

        boolean isConsentRevoked = consentCoreServiceImpl.revokeConsent(
        ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                false);

        Assert.assertTrue(isConsentRevoked);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeConsentWithoutConsentID() throws Exception {

        consentCoreServiceImpl.revokeConsentWithReason(null,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                false, ConsentCoreServiceConstants.CONSENT_REVOKE_REASON);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeConsentWithoutNewConsentStatus() throws Exception {

        consentCoreServiceImpl.revokeConsent(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                null, ConsentMgtServiceTestData.SAMPLE_USER_ID, false);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeConsentDataRetrievalError() throws Exception {

        Mockito.doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(Mockito.any(), Mockito.any());

        consentCoreServiceImpl.revokeConsentWithReason(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                false, ConsentCoreServiceConstants.CONSENT_REVOKE_REASON);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeConsentDataInsertionError() throws Exception {

        DetailedConsentResource retrievedDetailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

        Mockito.doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(Mockito.any(), Mockito.any());
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentStatus(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());
        Mockito.doThrow(ConsentDataInsertionException.class)
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(Mockito.any(),
                        Mockito.any(ConsentStatusAuditRecord.class));

        consentCoreServiceImpl.revokeConsent(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                false);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeConsentDataUpdateError() throws Exception {

        DetailedConsentResource retrievedDetailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

        Mockito.doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(Mockito.any(), Mockito.any());
        Mockito.doThrow(ConsentDataUpdationException.class).when(mockedConsentCoreDAO)
                .updateConsentStatus(Mockito.any(), Mockito.anyString(), Mockito.anyString());

        consentCoreServiceImpl.revokeConsentWithReason(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                false, ConsentCoreServiceConstants.CONSENT_REVOKE_REASON);
    }


    @Test
    public void testRevokeConsentWithoutReason() throws Exception {

        DetailedConsentResource retrievedDetailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

        Mockito.doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(Mockito.any(), Mockito.any());
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentStatus(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleTestConsentStatusAuditRecord(
                        retrievedDetailedConsentResource.getConsentID(),
                        retrievedDetailedConsentResource.getCurrentStatus()))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(Mockito.any(),
                        Mockito.any(ConsentStatusAuditRecord.class));
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(Mockito.any(),
                Mockito.any(), Mockito.anyString());

        boolean isConsentRevoked = consentCoreServiceImpl.revokeConsent(
        ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);

        Assert.assertTrue(isConsentRevoked);
    }

    @Test (priority = 1)
    public void testRevokeConsentWithUserIDWithoutReason() throws Exception {

        DetailedConsentResource retrievedDetailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();

        Mockito.doReturn(retrievedDetailedConsentResource).when(mockedConsentCoreDAO)
                .getDetailedConsentResource(Mockito.any(), Mockito.any());
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentStatus(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleTestConsentStatusAuditRecord(
                        retrievedDetailedConsentResource.getConsentID(),
                        retrievedDetailedConsentResource.getCurrentStatus()))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(Mockito.any(),
                        Mockito.any(ConsentStatusAuditRecord.class));
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(Mockito.any(),
                Mockito.any(), Mockito.anyString());

        boolean isConsentRevoked = consentCoreServiceImpl.revokeConsent(
        ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_USER_ID);

        Assert.assertTrue(isConsentRevoked);
    }

    @Test
    public void testRevokeExistingApplicableConsents() throws Exception {

        ArrayList<DetailedConsentResource> detailedConsentResources = new ArrayList<>();
        detailedConsentResources.add(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource());

        Mockito.doReturn(detailedConsentResources).when(mockedConsentCoreDAO)
                .searchConsents(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                        Mockito.any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt());
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentStatus(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentStatusAuditRecord(sampleID,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS)).when(mockedConsentCoreDAO)
                .storeConsentStatusAuditRecord(Mockito.any(), Mockito.any(ConsentStatusAuditRecord.class));
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(Mockito.any(),
                Mockito.any(), Mockito.any());

        Assert.assertTrue(consentCoreServiceImpl.revokeExistingApplicableConsents(sampleID,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                false));
    }

    @Test
    public void testRevokeExistingApplicableConsentsWithTokens() throws Exception {

        ArrayList<DetailedConsentResource> detailedConsentResources = new ArrayList<>();
        detailedConsentResources.add(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource());

        Mockito.doReturn(detailedConsentResources).when(mockedConsentCoreDAO)
                .searchConsents(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                        Mockito.any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt());
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentStatus(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentStatusAuditRecord(sampleID,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS)).when(mockedConsentCoreDAO)
                .storeConsentStatusAuditRecord(Mockito.any(), Mockito.any(ConsentStatusAuditRecord.class));
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(Mockito.any(),
                Mockito.any(), Mockito.any());

        Assert.assertTrue(consentCoreServiceImpl.revokeExistingApplicableConsents(sampleID,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                true));
    }

    @Test
    public void testRevokeExistingApplicableConsentsWithConsentsWithNoAttributes() throws Exception {

        DetailedConsentResource detailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();
        detailedConsentResource.setConsentAttributes(null);

        ArrayList<DetailedConsentResource> detailedConsentResources = new ArrayList<>();
        detailedConsentResources.add(detailedConsentResource);

        Mockito.doReturn(detailedConsentResources).when(mockedConsentCoreDAO)
                .searchConsents(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                        Mockito.any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt());
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentStatus(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentStatusAuditRecord(sampleID,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS)).when(mockedConsentCoreDAO)
                .storeConsentStatusAuditRecord(Mockito.any(), Mockito.any(ConsentStatusAuditRecord.class));
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(Mockito.any(),
                Mockito.any(), Mockito.any());

        consentCoreServiceImpl.revokeExistingApplicableConsents(sampleID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, false);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeExistingApplicableConsentsRetrieveError() throws Exception {

        Mockito.doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .searchConsents(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

        consentCoreServiceImpl.revokeExistingApplicableConsents(sampleID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, false);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeExistingApplicableConsentsUpdateError() throws Exception {

        ArrayList<DetailedConsentResource> detailedConsentResources = new ArrayList<>();
        detailedConsentResources.add(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource());

        Mockito.doReturn(detailedConsentResources).when(mockedConsentCoreDAO)
                .searchConsents(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.doThrow(ConsentDataUpdationException.class).when(mockedConsentCoreDAO)
                .updateConsentStatus(Mockito.any(), Mockito.anyString(), Mockito.anyString());

        consentCoreServiceImpl.revokeExistingApplicableConsents(sampleID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, false);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeExistingApplicableConsentsInsertionError() throws Exception {

        ArrayList<DetailedConsentResource> detailedConsentResources = new ArrayList<>();
        detailedConsentResources.add(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource());

        Mockito.doReturn(detailedConsentResources).when(mockedConsentCoreDAO)
                .searchConsents(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentStatus(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());
        Mockito.doThrow(ConsentDataInsertionException.class).when(mockedConsentCoreDAO)
                .storeConsentStatusAuditRecord(Mockito.any(), Mockito.any());

        consentCoreServiceImpl.revokeExistingApplicableConsents(sampleID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, false);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeExistingApplicableConsentsWithoutClientID() throws Exception {

        consentCoreServiceImpl.revokeExistingApplicableConsents(null, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, false);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeExistingApplicableConsentsWithoutRevokedConsentStatus() throws Exception {

        consentCoreServiceImpl.revokeExistingApplicableConsents(ConsentMgtServiceTestData.SAMPLE_CLIENT_ID,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, null, false);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeExistingApplicableConsentsWithoutUserID() throws Exception {

        consentCoreServiceImpl.revokeExistingApplicableConsents(ConsentMgtServiceTestData.SAMPLE_CLIENT_ID,
                null, ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS
                , false);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeExistingApplicableConsentsWithoutConsentType() throws Exception {

        consentCoreServiceImpl.revokeExistingApplicableConsents(ConsentMgtServiceTestData.SAMPLE_CLIENT_ID,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, null,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS
                , false);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testRevokeExistingApplicableConsentsWithoutApplicableStatusToRevoke() throws Exception {

        consentCoreServiceImpl.revokeExistingApplicableConsents(ConsentMgtServiceTestData.SAMPLE_CLIENT_ID,
                ConsentMgtServiceTestData.SAMPLE_USER_ID, ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPE,
                null, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS
                , false);
    }

    @Test
    public void testReAuthorizeExistingAuthResources() throws Exception {

        ConsentResource consentResource = ConsentMgtServiceTestData.getSampleStoredTestConsentResource();
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData
                        .getSampleTestConsentMappingResource(ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID))
                .when(mockedConsentCoreDAO).storeConsentMappingResource(Mockito.any(),
                        Mockito.any(ConsentMappingResource.class));
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(Mockito.any(),
                Mockito.any(), Mockito.anyString());
        Assert.assertTrue(consentCoreServiceImpl
                .reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                        ConsentMgtServiceTestData.SAMPLE_USER_ID,
                        ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, consentResource.getCurrentStatus()));
    }

    @Test
    public void testReAuthorizeExistingAuthResourceAccountsAddScenario() throws Exception {

        ConsentMappingResource consentMappingResource =
                ConsentMgtServiceTestData.getSampleTestConsentMappingResource(sampleID);
        consentMappingResource.setAccountID("accountID1");
        ArrayList<ConsentMappingResource> mappingResources = new ArrayList<>();
        mappingResources.add(consentMappingResource);

        DetailedConsentResource detailedConsentResource =
                ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource();
        detailedConsentResource.setConsentMappingResources(mappingResources);

        ConsentResource consentResource = ConsentMgtServiceTestData.getSampleStoredTestConsentResource();
        Mockito.doReturn(detailedConsentResource)
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData
                        .getSampleTestConsentMappingResource(ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID))
                .when(mockedConsentCoreDAO).storeConsentMappingResource(Mockito.any(),
                        Mockito.any(ConsentMappingResource.class));
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(Mockito.any(),
                Mockito.any(), Mockito.anyString());
        Assert.assertTrue(consentCoreServiceImpl
                .reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                        ConsentMgtServiceTestData.SAMPLE_USER_ID,
                        ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, consentResource.getCurrentStatus()));
    }

    @Test
    public void testReAuthorizeExistingAuthResourceNoAccountsRemoveOrAddScenario() throws Exception {

        ConsentResource consentResource = ConsentMgtServiceTestData.getSampleStoredTestConsentResource();
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData
                        .getSampleTestConsentMappingResource(ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID))
                .when(mockedConsentCoreDAO).storeConsentMappingResource(Mockito.any(),
                        Mockito.any(ConsentMappingResource.class));
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(Mockito.any(),
                Mockito.any(), Mockito.anyString());
        Assert.assertTrue(consentCoreServiceImpl
                .reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                        ConsentMgtServiceTestData.SAMPLE_USER_ID,
                        ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP2,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, consentResource.getCurrentStatus()));
    }

    @Test
    public void testReAuthorizeExistingAuthResourceAccountsRemoveScenario() throws Exception {

        ConsentResource consentResource = ConsentMgtServiceTestData.getSampleStoredTestConsentResource();
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResourceWithMultipleAccountIDs())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData
                        .getSampleTestConsentMappingResource(ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID))
                .when(mockedConsentCoreDAO).storeConsentMappingResource(Mockito.any(),
                        Mockito.any(ConsentMappingResource.class));
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(Mockito.any(),
                Mockito.any(), Mockito.anyString());
        Assert.assertTrue(consentCoreServiceImpl
                .reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                        ConsentMgtServiceTestData.SAMPLE_USER_ID,
                        ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP3,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, consentResource.getCurrentStatus()));
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeExistingAuthResourcesWithoutConsentID() throws Exception {

        Assert.assertTrue(consentCoreServiceImpl.reAuthorizeExistingAuthResource(null,
                ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS));
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeExistingAuthResourcesWithoutAuthID() throws Exception {

        Assert.assertTrue(consentCoreServiceImpl
                .reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        null, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                        ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS));
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeExistingAuthResourcesWithoutUserID() throws Exception {

        Assert.assertTrue(consentCoreServiceImpl
                .reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        sampleID, null, ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS));
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeExistingAuthResourcesWithoutCurrentConsentStatus() throws Exception {

        Assert.assertTrue(consentCoreServiceImpl
                .reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        sampleID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                        ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                        null, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS));
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeExistingAuthResourcesWithoutNewConsentStatus() throws Exception {

        Assert.assertTrue(consentCoreServiceImpl
                .reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                        ConsentMgtServiceTestData.SAMPLE_USER_ID,
                        ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, null));
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeExistingAuthResourcesWithoutAccountsAndPermissionsMap() throws Exception {

        Assert.assertTrue(consentCoreServiceImpl
                .reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                        ConsentMgtServiceTestData.SAMPLE_USER_ID,
                        new HashMap<>(), ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS));
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeExistingAuthResourcesDataRetrieveError() throws Exception {

        Mockito.doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString());
        consentCoreServiceImpl.reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeExistingAuthResourcesDataInsertError() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData
                        .getSampleConsentMappingResourcesList(ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST))
                .when(mockedConsentCoreDAO).getConsentMappingResources(Mockito.any(), Mockito.anyString());
        Mockito.doThrow(ConsentDataInsertionException.class)
                .when(mockedConsentCoreDAO).storeConsentMappingResource(Mockito.any(),
                        Mockito.any(ConsentMappingResource.class));
        consentCoreServiceImpl.reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeExistingAuthResourcesDataUpdateError() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData
                        .getSampleConsentMappingResourcesList(ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST))
                .when(mockedConsentCoreDAO).getConsentMappingResources(Mockito.any(), Mockito.anyString());
        Mockito.doThrow(ConsentDataUpdationException.class)
                .when(mockedConsentCoreDAO).updateConsentMappingStatus(Mockito.any(), Mockito.any(),
                        Mockito.anyString());
        consentCoreServiceImpl.reAuthorizeExistingAuthResource(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS);
    }

    @Test
    public void testReAuthorizeConsentWithNewAuthResource() throws Exception {

        AuthorizationResource authorizationResource = ConsentMgtServiceTestData
                .getSampleTestAuthorizationResource(sampleID, null);
        ArrayList<String> consentIDs = new ArrayList<>();
        consentIDs.add(sampleID);

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleAuthorizationResourcesList(consentIDs))
                .when(mockedConsentCoreDAO).searchConsentAuthorizations(Mockito.any(), Mockito.anyString(),
                        Mockito.anyString());
        Mockito.doNothing().when(mockedConsentCoreDAO).updateAuthorizationStatus(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());
        Mockito.doReturn(authorizationResource).when(mockedConsentCoreDAO).storeAuthorizationResource(Mockito.any(),
                Mockito.any(AuthorizationResource.class));
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData
                        .getSampleTestConsentMappingResource(sampleID))
                .when(mockedConsentCoreDAO).storeConsentMappingResource(Mockito.any(),
                        Mockito.any(ConsentMappingResource.class));
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(Mockito.any(),
                Mockito.any(), Mockito.anyString());
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentStatus(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData
                        .getSampleTestConsentStatusAuditRecord(sampleID,
                                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(Mockito.any(),
                        Mockito.any(ConsentStatusAuditRecord.class));

        consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(sampleID, sampleID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeConsentWithNewAuthResourceDataRetrieveError() throws Exception {

        Mockito.doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).searchConsentAuthorizations(Mockito.any(), Mockito.any(), Mockito.any());

        consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(sampleID, sampleID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeConsentWithNewAuthResourceDataUpdateError() throws Exception {

        ArrayList<String> consentIDs = new ArrayList<>();
        consentIDs.add(sampleID);
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleAuthorizationResourcesList(consentIDs))
                .when(mockedConsentCoreDAO).searchConsentAuthorizations(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.doThrow(ConsentDataUpdationException.class)
                .when(mockedConsentCoreDAO).updateAuthorizationStatus(Mockito.any(), Mockito.anyString(),
                        Mockito.anyString());

        consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(sampleID, sampleID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeConsentWithNewAuthResourceDataInsertError() throws Exception {

        AuthorizationResource authorizationResource = ConsentMgtServiceTestData
                .getSampleTestAuthorizationResource(sampleID, null);

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResourcesList())
                .when(mockedConsentCoreDAO).searchConsents(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                        Mockito.any(), Mockito.any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(),
                        Mockito.anyInt());
        Mockito.doNothing().when(mockedConsentCoreDAO).updateAuthorizationStatus(Mockito.any(), Mockito.anyString(),
                Mockito.anyString());
        Mockito.doReturn(authorizationResource).when(mockedConsentCoreDAO).storeAuthorizationResource(Mockito.any(),
                Mockito.any(AuthorizationResource.class));
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString());
        Mockito.doThrow(ConsentDataInsertionException.class)
                .when(mockedConsentCoreDAO).storeConsentMappingResource(Mockito.any(),
                        Mockito.any(ConsentMappingResource.class));

        consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(sampleID, sampleID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeConsentWithNewAuthResourceWithoutConsentID() throws Exception {

        consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(null, sampleID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeConsentWithNewAuthResourceWithoutUserID() throws Exception {

        consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(sampleID, null,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeConsentWithNewAuthResourceWithoutAccountsMap() throws Exception {

        consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(sampleID, sampleID,
                null, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeConsentWithNewAuthResourceWithoutCurrentConsentStatus() throws Exception {

        consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(sampleID, sampleID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP, null,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeConsentWithNewAuthResourceWithoutNewConsentStatus() throws Exception {

        consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(sampleID, sampleID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                null, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeConsentWithNewAuthResourceWithoutNewExistingAuthStatus() throws Exception {

        consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(sampleID, sampleID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, null,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeConsentWithNewAuthResourceWithoutNewAuthStatus() throws Exception {

        consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(sampleID, sampleID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                null, ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testReAuthorizeConsentWithNewAuthResourceWithoutNewAuthType() throws Exception {

        consentCoreServiceImpl.reAuthorizeConsentWithNewAuthResource(sampleID, sampleID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_AUTHORIZATION_STATUS, null);
    }

    @Test
    public void storeConsentAttributes() throws Exception {

        consentCoreServiceImpl.storeConsentAttributes(ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void storeConsentAttributesWithoutParameters() throws Exception {

        consentCoreServiceImpl.storeConsentAttributes(null, null);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void storeConsentAttributesWithoutConsentId() throws Exception {

        consentCoreServiceImpl.storeConsentAttributes(null,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void storeConsentAttributesWithoutAttributeMap() throws Exception {

        consentCoreServiceImpl.storeConsentAttributes(ConsentMgtServiceTestData.CONSENT_ID,
                null);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void storeConsentAttributesEmptyAttributeMap() throws Exception {

        consentCoreServiceImpl.storeConsentAttributes(ConsentMgtServiceTestData.CONSENT_ID,
                new HashMap<>());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void storeConsentAttributesDataInsertError() throws Exception {

        Mockito.doThrow(ConsentDataInsertionException.class)
                .when(mockedConsentCoreDAO).storeConsentAttributes(Mockito.any(), Mockito.any());

        consentCoreServiceImpl.storeConsentAttributes(ConsentMgtServiceTestData.CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);
    }

    @Test
    public void testGetConsentAttributesWithAttributeKeys() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleTestConsentResource())
                .when(mockedConsentCoreDAO).getConsentResource(Mockito.any(), Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData
                        .getSampleTestConsentAttributesObject(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID))
                .when(mockedConsentCoreDAO).getConsentAttributes(Mockito.any(), Mockito.anyString(),
                        Mockito.any());
        ConsentAttributes consentAttributes =
                consentCoreServiceImpl.getConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                        ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
        Assert.assertNotNull(consentAttributes);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentAttributesWithoutConsentID() throws Exception {

        consentCoreServiceImpl.getConsentAttributes(null,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentAttributesWithEmptyAttributeKeys() throws Exception {

        Mockito.doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getConsentAttributes(Mockito.any(), Mockito.anyString());
        consentCoreServiceImpl.getConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                new ArrayList<>());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentAttributesConsentResourceReteivealError() throws Exception {

        Mockito.doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getConsentAttributes(Mockito.any(), Mockito.anyString(), Mockito.any());
        consentCoreServiceImpl.getConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentAttributesDataRetrieveError() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleTestConsentResource())
                .when(mockedConsentCoreDAO).getConsentResource(Mockito.any(), Mockito.anyString());
        Mockito.doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getConsentAttributes(Mockito.any(), Mockito.anyString(), Mockito.any());
        consentCoreServiceImpl.getConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
    }

    @Test
    public void testGetConsentAttributes() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleTestConsentResource())
                .when(mockedConsentCoreDAO).getConsentResource(Mockito.any(), Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData
                        .getSampleTestConsentAttributesObject(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID))
                .when(mockedConsentCoreDAO).getConsentAttributes(Mockito.any(), Mockito.anyString());
        ConsentAttributes consentAttributes =
                consentCoreServiceImpl.getConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID);
        Assert.assertNotNull(consentAttributes);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentAttributesWithoutAttributeKeys() throws Exception {

        Mockito.doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getConsentAttributes(Mockito.any(), Mockito.anyString());
        consentCoreServiceImpl.getConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentAttributesWithoutAttributesWithoutConsentID() throws Exception {

        consentCoreServiceImpl.getConsentAttributes(null);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testGetConsentAttributesConsentResourceRetrieveError() throws Exception {

        Mockito.doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getConsentResource(Mockito.any(), Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData
                        .getSampleTestConsentAttributesObject(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID))
                .when(mockedConsentCoreDAO).getConsentAttributes(Mockito.any(), Mockito.anyString());
        ConsentAttributes consentAttributes =
                consentCoreServiceImpl.getConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID);
        Assert.assertNotNull(consentAttributes);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testGetConsentAttributesWithDataRetrieveError() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleTestConsentResource())
                .when(mockedConsentCoreDAO).getConsentResource(Mockito.any(), Mockito.anyString());
        Mockito.doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .getConsentAttributes(Mockito.any(), Mockito.anyString());
        ConsentAttributes consentAttributes =
                consentCoreServiceImpl.getConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID);
        Assert.assertNotNull(consentAttributes);
    }

    @Test
    public void testGetConsentAttributesByName() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP)
                .when(mockedConsentCoreDAO).getConsentAttributesByName(Mockito.any(), Mockito.anyString());
        Map<String, String> retrievedAttributesMap =
                consentCoreServiceImpl.getConsentAttributesByName("x-request-id");
        Assert.assertTrue(retrievedAttributesMap.containsKey("x-request-id"));
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentAttributesByNameWithoutAttributeName() throws Exception {

        consentCoreServiceImpl.getConsentAttributesByName(null);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentAttributesByNameDataRetrieveError() throws Exception {

        Mockito.doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getConsentAttributesByName(Mockito.any(), Mockito.anyString());
        consentCoreServiceImpl.getConsentAttributesByName("x-request-id");
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentIdByConsentAttributeNameAndValueWithoutAttributeName() throws Exception {

        consentCoreServiceImpl.getConsentIdByConsentAttributeNameAndValue(null,
                "domestic-payments");
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentIdByConsentAttributeNameAndValueWithoutAttributeValues() throws Exception {

        consentCoreServiceImpl.getConsentIdByConsentAttributeNameAndValue("payment-type",
                null);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentIdByConsentAttributeNameAndValueDataRetrieveError() throws Exception {

        Mockito.doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getConsentIdByConsentAttributeNameAndValue(Mockito.any(),
                        Mockito.anyString(), Mockito.anyString());
        consentCoreServiceImpl.getConsentIdByConsentAttributeNameAndValue("payment-type",
                "domestic-payments");
    }

    @Test
    public void testGetConsentIdByConsentAttributeNameAndValue() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.SAMPLE_CONSENT_IS_ARRAY)
                .when(mockedConsentCoreDAO).getConsentIdByConsentAttributeNameAndValue(Mockito.any(),
                        Mockito.anyString(), Mockito.anyString());
        ArrayList<String> consentIdList = consentCoreServiceImpl.getConsentIdByConsentAttributeNameAndValue(
                "payment-type", "domestic-payments");
        Assert.assertFalse(consentIdList.isEmpty());
    }

    @Test
    public void testUpdateConsentAttributes() throws Exception {

        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentAttributes(Mockito.any(),
                Mockito.anyString(), Mockito.anyMap());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleTestConsentAttributesObject(
                ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID))
                .when(mockedConsentCoreDAO).getConsentAttributes(Mockito.any(), Mockito.anyString());
        consentCoreServiceImpl.updateConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testUpdateConsentAttributesWithoutConsentId() throws Exception {

        consentCoreServiceImpl.updateConsentAttributes(null,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testUpdateConsentAttributesWithoutAttributes() throws Exception {

        consentCoreServiceImpl.updateConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID, null);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testUpdateConsentAttributesWithEmptyAttributes() throws Exception {

        consentCoreServiceImpl.updateConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                new HashMap<>());
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testUpdateConsentAttributesWithDataUpdateError() throws Exception {

        Mockito.doThrow(ConsentDataUpdationException.class).when(mockedConsentCoreDAO)
                .updateConsentAttributes(Mockito.any(), Mockito.anyString(), Mockito.anyMap());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleTestConsentAttributesObject(
                        ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID))
                .when(mockedConsentCoreDAO).getConsentAttributes(Mockito.any(), Mockito.anyString());
        consentCoreServiceImpl.updateConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testUpdateConsentAttributesWithDataRetrieveError() throws Exception {

        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentAttributes(Mockito.any(),
                Mockito.anyString(), Mockito.anyMap());
        Mockito.doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .getConsentAttributes(Mockito.any(), Mockito.anyString());
        consentCoreServiceImpl.updateConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP);
    }

    @Test
    public void testDeleteConsentAttributes() throws Exception {

        Mockito.doReturn(true).when(mockedConsentCoreDAO).deleteConsentAttributes(Mockito.any(),
                Mockito.anyString(), Mockito.any());
        consentCoreServiceImpl.deleteConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testDeleteConsentAttributesDeleteError() throws Exception {

        Mockito.doThrow(ConsentDataDeletionException.class)
                .when(mockedConsentCoreDAO).deleteConsentAttributes(Mockito.any(), Mockito.anyString(),
                        Mockito.any());
        consentCoreServiceImpl.deleteConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testDeleteConsentAttributesWithoutConsentID() throws Exception {

        Mockito.doReturn(true).when(mockedConsentCoreDAO).deleteConsentAttributes(Mockito.any(),
                Mockito.anyString(), Mockito.any());
        consentCoreServiceImpl.deleteConsentAttributes(null,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_KEYS);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testDeleteConsentAttributesWithoutAttributeKeysList() throws Exception {

        Mockito.doReturn(true).when(mockedConsentCoreDAO).deleteConsentAttributes(Mockito.any(),
                Mockito.anyString(), Mockito.any());
        consentCoreServiceImpl.deleteConsentAttributes(ConsentMgtServiceTestData.UNMATCHED_CONSENT_ID,
                null);
    }

    @Test
    public void testSearchConsentStatusAuditRecords() throws Exception {

        ArrayList<ConsentStatusAuditRecord> consentStatusAuditRecords = new ArrayList<>();

        Mockito.doReturn(consentStatusAuditRecords).when(mockedConsentCoreDAO)
                .getConsentStatusAuditRecordsByConsentId(Mockito.any(), Mockito.any(ArrayList.class),
                Mockito.anyInt(),
                        Mockito.anyInt());
        ArrayList<ConsentStatusAuditRecord> statusAuditRecords =
                consentCoreServiceImpl.searchConsentStatusAuditRecords(ConsentMgtServiceTestData.CONSENT_ID,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_ACTION_BY,
                        0L, 0L, "1234");
        Assert.assertNotNull(statusAuditRecords);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testSearchConsentStatusAuditRecordsWithDataRetrievalError() throws Exception {

        Mockito.doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .getConsentStatusAuditRecords(Mockito.any(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyString());
        ArrayList<ConsentStatusAuditRecord> statusAuditRecords =
                consentCoreServiceImpl.searchConsentStatusAuditRecords(ConsentMgtServiceTestData.CONSENT_ID,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, ConsentMgtServiceTestData.SAMPLE_ACTION_BY,
                        0L, 0L, "1234");
        Assert.assertNotNull(statusAuditRecords);
    }

    @Test
    public void testGetConsentStatusAuditRecords() throws Exception {

        ArrayList<ConsentStatusAuditRecord> consentStatusAuditRecords = new ArrayList<>();
        ArrayList<String> consentIds = new ArrayList<>();

        Mockito.doReturn(consentStatusAuditRecords).when(mockedConsentCoreDAO)
                .getConsentStatusAuditRecordsByConsentId(Mockito.any(), Mockito.any(ArrayList.class),
                Mockito.anyInt(),
                        Mockito.anyInt());
        ArrayList<ConsentStatusAuditRecord> statusAuditRecords =
                consentCoreServiceImpl.getConsentStatusAuditRecords(consentIds, null, null);
        Assert.assertNotNull(statusAuditRecords);
    }

    @Test(expectedExceptions = ConsentManagementException.class)
    public void testGetConsentStatusAuditRecordsWithDataRetrievalError() throws Exception {
        ArrayList<String> consentIds = new ArrayList<>();

        Mockito.doThrow(ConsentDataRetrievalException.class).when(mockedConsentCoreDAO)
                .getConsentStatusAuditRecordsByConsentId(Mockito.any(), Mockito.any(ArrayList.class),
                Mockito.any(), Mockito.any());
        ArrayList<ConsentStatusAuditRecord> statusAuditRecords =
                consentCoreServiceImpl.getConsentStatusAuditRecords(consentIds, null, null);
        Assert.assertNotNull(statusAuditRecords);
    }

    @Test
    public void testStoreConsentAmendmentHistory() throws Exception {

        boolean result = consentCoreServiceImpl.storeConsentAmendmentHistory(sampleID,
                ConsentMgtServiceTestData.getSampleTestConsentHistoryResource(),
                ConsentMgtServiceTestData.getSampleDetailedStoredTestCurrentConsentResource());

        Assert.assertTrue(result);
    }

    @Test
    public void testStoreConsentAmendmentHistoryWithoutPassingCurrentConsent() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestCurrentConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString());

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

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testStoreConsentAmendmentHistoryWithoutConsentID() throws Exception {

        consentCoreServiceImpl.storeConsentAmendmentHistory(null,
                ConsentMgtServiceTestData.getSampleTestConsentHistoryResource(),
                ConsentMgtServiceTestData.getSampleDetailedStoredTestCurrentConsentResource());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testStoreConsentAmendmentHistoryWithoutConsentHistoryResource() throws Exception {

        consentCoreServiceImpl.storeConsentAmendmentHistory(sampleID,
                null,
                ConsentMgtServiceTestData.getSampleDetailedStoredTestCurrentConsentResource());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testStoreConsentAmendmentHistoryWithZeroAsConsentAmendedTimestamp() throws Exception {

        ConsentHistoryResource consentHistoryResource = ConsentMgtServiceTestData
        .getSampleTestConsentHistoryResource();
        consentHistoryResource.setTimestamp(0);
        consentCoreServiceImpl.storeConsentAmendmentHistory(sampleID, consentHistoryResource,
                ConsentMgtServiceTestData.getSampleDetailedStoredTestCurrentConsentResource());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testStoreConsentAmendmentHistoryWithoutConsentAmendedReason() throws Exception {

        ConsentHistoryResource consentHistoryResource = ConsentMgtServiceTestData
        .getSampleTestConsentHistoryResource();
        consentHistoryResource.setReason(null);
        consentCoreServiceImpl.storeConsentAmendmentHistory(sampleID,
                consentHistoryResource,
                ConsentMgtServiceTestData.getSampleDetailedStoredTestCurrentConsentResource());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testStoreConsentAmendmentHistoryDataInsertError() throws Exception {

        Mockito.doThrow(ConsentDataInsertionException.class)
                .when(mockedConsentCoreDAO).storeConsentAmendmentHistory(Mockito.any(), Mockito.anyString(),
                        Mockito.anyLong(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString());

        consentCoreServiceImpl.storeConsentAmendmentHistory(sampleID,
                ConsentMgtServiceTestData.getSampleTestConsentHistoryResource(),
                ConsentMgtServiceTestData.getSampleDetailedStoredTestCurrentConsentResource());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testStoreConsentAmendmentHistoryDataRetrievalError() throws Exception {

        Mockito.doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString());

        consentCoreServiceImpl.storeConsentAmendmentHistory(sampleID,
                ConsentMgtServiceTestData.getSampleTestConsentHistoryResource(), null);
    }

    @Test ()
    public void testGetConsentAmendmentHistoryData() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentHistoryDataMap())
                .when(mockedConsentCoreDAO).retrieveConsentAmendmentHistory(Mockito.any(), Mockito.any());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString());

        Map<String, ConsentHistoryResource>  consentAmendmentHistory =
                consentCoreServiceImpl.getConsentAmendmentHistoryData(sampleID);

        Assert.assertTrue(consentAmendmentHistory.containsKey(ConsentMgtServiceTestData.SAMPLE_HISTORY_ID));
        Assert.assertNotNull(consentAmendmentHistory.get(ConsentMgtServiceTestData.SAMPLE_HISTORY_ID));
    }

    @Test
    public void testGetConsentAmendmentHistoryDataWithOnlyBasicConsentData() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleConsentHistoryBasicConsentDataMap())
                .when(mockedConsentCoreDAO).retrieveConsentAmendmentHistory(Mockito.any(), Mockito.any());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString());

        Map<String, ConsentHistoryResource>  consentAmendmentHistory =
                consentCoreServiceImpl.getConsentAmendmentHistoryData(sampleID);

        Assert.assertTrue(consentAmendmentHistory.containsKey(ConsentMgtServiceTestData.SAMPLE_HISTORY_ID));
        Assert.assertNotNull(consentAmendmentHistory.get(ConsentMgtServiceTestData.SAMPLE_HISTORY_ID));
    }

    @Test
    public void testGetConsentAmendmentHistoryDataWithOnlyConsentAttributesData() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleConsentHistoryConsentAttributesDataMap())
                .when(mockedConsentCoreDAO).retrieveConsentAmendmentHistory(Mockito.any(), Mockito.any());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString());

        Map<String, ConsentHistoryResource>  consentAmendmentHistory =
                consentCoreServiceImpl.getConsentAmendmentHistoryData(sampleID);

        Assert.assertTrue(consentAmendmentHistory.containsKey(ConsentMgtServiceTestData.SAMPLE_HISTORY_ID));
        Assert.assertNotNull(consentAmendmentHistory.get(ConsentMgtServiceTestData.SAMPLE_HISTORY_ID));
    }

    @Test
    public void testGetConsentAmendmentHistoryDataWithOnlyConsentMappingsData() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleConsentHistoryConsentMappingsDataMap())
                .when(mockedConsentCoreDAO).retrieveConsentAmendmentHistory(Mockito.any(), Mockito.any());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString());

        Map<String, ConsentHistoryResource>  consentAmendmentHistory =
                consentCoreServiceImpl.getConsentAmendmentHistoryData(sampleID);

        Assert.assertTrue(consentAmendmentHistory.containsKey(ConsentMgtServiceTestData.SAMPLE_HISTORY_ID));
        Assert.assertNotNull(consentAmendmentHistory.get(ConsentMgtServiceTestData.SAMPLE_HISTORY_ID));
    }

    @Test
    public void testGetConsentAmendmentHistoryDataWithNoConsentHistoryEntries() throws Exception {

        Mockito.doReturn(new HashMap<>())
                .when(mockedConsentCoreDAO).retrieveConsentAmendmentHistory(Mockito.any(), Mockito.any());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString());

        Map<String, ConsentHistoryResource>  consentAmendmentHistory =
                consentCoreServiceImpl.getConsentAmendmentHistoryData(sampleID);

        Assert.assertEquals(0, consentAmendmentHistory.size());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentAmendmentHistoryDataWithoutConsentID() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentHistoryDataMap())
                .when(mockedConsentCoreDAO).retrieveConsentAmendmentHistory(Mockito.any(), Mockito.any());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString());

        consentCoreServiceImpl.getConsentAmendmentHistoryData(null);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testGetConsentAmendmentHistoryDataRetrieveError() throws Exception {

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentHistoryDataMap())
                .when(mockedConsentCoreDAO).retrieveConsentAmendmentHistory(Mockito.any(), Mockito.any());
        Mockito.doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString());

        consentCoreServiceImpl.getConsentAmendmentHistoryData(sampleID);
    }

    @Test
    public void testSearchConsents() throws Exception {

        ArrayList<DetailedConsentResource> detailedConsentResources = new ArrayList<>();
        detailedConsentResources.add(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource());

        Mockito.doReturn(detailedConsentResources)
                .when(mockedConsentCoreDAO).searchConsents(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                        Mockito.any(), Mockito.any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(),
                        Mockito.anyInt());

        consentCoreServiceImpl.searchDetailedConsents(ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST,
                ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST, ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPES_LIST,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_STATUSES_LIST,
                ConsentMgtServiceTestData.SAMPLE_USER_IDS_LIST,
                12345L, 23456L, null, null);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testSearchConsentsRetrieveError() throws Exception {

        Mockito.doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).searchConsents(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

        consentCoreServiceImpl.searchDetailedConsents(ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST,
                ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST, ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPES_LIST,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_STATUSES_LIST,
                ConsentMgtServiceTestData.SAMPLE_USER_IDS_LIST,
                12345L, 23456L, null, null);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testSearchConsentsWithLimits() throws Exception {

        Mockito.doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).searchConsents(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                        Mockito.any(), Mockito.any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(),
                        Mockito.anyInt());

        consentCoreServiceImpl.searchDetailedConsents(ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST,
                ConsentMgtServiceTestData.SAMPLE_CLIENT_IDS_LIST, ConsentMgtServiceTestData.SAMPLE_CONSENT_TYPES_LIST,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_STATUSES_LIST,
                ConsentMgtServiceTestData.SAMPLE_USER_IDS_LIST,
                12345L, 23456L, 1, 0);
    }

    @Test
    public void testAmendConsentData() throws Exception {

        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentReceipt(Mockito.any(),
                Mockito.anyString(), Mockito.anyString());
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentValidityTime(Mockito.any(),
                Mockito.anyString(), Mockito.anyLong());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getConsentResource(Mockito.any(), Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentStatusAuditRecord(sampleID,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(Mockito.any(),
                        Mockito.any(ConsentStatusAuditRecord.class));

        ConsentResource consentResource =
                consentCoreServiceImpl.amendConsentData(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                        ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                        ConsentMgtServiceTestData.SAMPLE_USER_ID);

        Assert.assertNotNull(consentResource);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendConsentDataWithoutConsentID() throws Exception {

        consentCoreServiceImpl.amendConsentData(null, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                ConsentMgtServiceTestData.SAMPLE_USER_ID);

    }

    @Test
    public void testAmendConsentValidityPeriod() throws Exception {

        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentValidityTime(Mockito.any(),
                Mockito.anyString(), Mockito.anyLong());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getConsentResource(Mockito.any(), Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentStatusAuditRecord(sampleID,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(Mockito.any(),
                        Mockito.any(ConsentStatusAuditRecord.class));

        ConsentResource consentResource =
                consentCoreServiceImpl.amendConsentData(sampleID, null,
                        ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                        ConsentMgtServiceTestData.SAMPLE_USER_ID);

        Assert.assertNotNull(consentResource);
    }

    @Test
    public void testAmendConsentReceipt() throws Exception {

        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentReceipt(Mockito.any(),
                Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getConsentResource(Mockito.any(), Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentStatusAuditRecord(sampleID,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(Mockito.any(),
                        Mockito.any(ConsentStatusAuditRecord.class));

        ConsentResource consentResource =
                consentCoreServiceImpl.amendConsentData(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                        null, ConsentMgtServiceTestData.SAMPLE_USER_ID);

        Assert.assertNotNull(consentResource);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendConsentDataWithoutReceiptAndValidityTime() throws Exception {

        consentCoreServiceImpl.amendConsentData(sampleID, null, null,
                ConsentMgtServiceTestData.SAMPLE_USER_ID);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendConsentDataUpdateError() throws Exception {

        Mockito.doThrow(ConsentDataUpdationException.class)
                .when(mockedConsentCoreDAO).updateConsentReceipt(Mockito.any(), Mockito.anyString(),
                        Mockito.anyString());

        consentCoreServiceImpl.amendConsentData(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                ConsentMgtServiceTestData.SAMPLE_USER_ID);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendConsentDataRetrieveError() throws Exception {

        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentReceipt(Mockito.any(),
                Mockito.anyString(), Mockito.anyString());
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentValidityTime(Mockito.any(),
                Mockito.anyString(), Mockito.anyLong());
        Mockito.doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getConsentResource(Mockito.any(), Mockito.anyString());

        consentCoreServiceImpl.amendConsentData(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                ConsentMgtServiceTestData.SAMPLE_USER_ID);
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendConsentDataInsertError() throws Exception {

        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentReceipt(Mockito.any(),
                Mockito.anyString(), Mockito.anyString());
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentValidityTime(Mockito.any(),
                Mockito.anyString(), Mockito.anyLong());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getConsentResource(Mockito.any(), Mockito.anyString());
        Mockito.doThrow(ConsentDataInsertionException.class)
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(Mockito.any(),
                        Mockito.any(ConsentStatusAuditRecord.class));

        consentCoreServiceImpl.amendConsentData(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                ConsentMgtServiceTestData.SAMPLE_USER_ID);
    }

    @Test
    public void testAmendDetailedConsentData() throws Exception {

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

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendDetailedConsentDataWithoutConsentID() throws Exception {

        consentCoreServiceImpl.amendDetailedConsent(null, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP,
                ConsentMgtServiceTestData.SAMPLE_USER_ID,
                new HashMap<>());
    }

    @Test
    public void testAmendDetailedConsentDataWithoutReceiptOnly() throws Exception {

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

    @Test
    public void testAmendDetailedConsentDataWithoutValidityTimeOnly() throws Exception {

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

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendDetailedConsentDataWithoutReceiptAndValidityTime() throws Exception {

        consentCoreServiceImpl.amendDetailedConsent(sampleID, null, null,
                ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP,
                ConsentMgtServiceTestData.SAMPLE_USER_ID,
                new HashMap<>());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendDetailedConsentDataWithoutUserId() throws Exception {

        consentCoreServiceImpl.amendDetailedConsent(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP, null,
                new HashMap<>());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendDetailedConsentDataWithoutAuthId() throws Exception {

        consentCoreServiceImpl.amendDetailedConsent(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD, null,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                new HashMap<>());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendDetailedConsentDataWithoutNewConsentStatus() throws Exception {

        consentCoreServiceImpl.amendDetailedConsent(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP, null,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                new HashMap<>());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendDetailedConsentDataWithoutNewConsentAttributes() throws Exception {

        consentCoreServiceImpl.amendDetailedConsent(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS, null,
                ConsentMgtServiceTestData.SAMPLE_USER_ID,
                new HashMap<>());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendDetailedConsentDataWithoutAccountIdMapWithPermissions() throws Exception {

        consentCoreServiceImpl.amendDetailedConsent(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                null,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP, ConsentMgtServiceTestData.SAMPLE_USER_ID,
                new HashMap<>());
    }

    @Test
    public void testAmendDetailedConsentDataWithAdditionalAmendmentData() throws Exception {

        setInitialDataForAmendDetailedConsentSuccessFlow();
        Mockito.doReturn(new AuthorizationResource()).when(mockedConsentCoreDAO)
                .storeAuthorizationResource(Mockito.any(), Mockito.any(AuthorizationResource.class));
        Mockito.doReturn(new ConsentMappingResource()).when(mockedConsentCoreDAO)
                .storeConsentMappingResource(Mockito.any(), Mockito.any(ConsentMappingResource.class));

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

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendDetailedConsentDataWithAdditionalAmendmentDataWithoutConsentIdInAuthResources()
            throws Exception {

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

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendDetailedConsentDataWithAdditionalAmendmentDataWithoutAccountIdInMappingResources()
            throws Exception {

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

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendDetailedConsentDataUpdateError() throws Exception {

        Mockito.doThrow(ConsentDataUpdationException.class)
                .when(mockedConsentCoreDAO).updateConsentReceipt(Mockito.any(), Mockito.anyString(),
                        Mockito.anyString());

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

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendDetailedConsentDataRetrieveError() throws Exception {

        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentReceipt(Mockito.any(),
                Mockito.anyString(), Mockito.anyString());
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentValidityTime(Mockito.any(),
                Mockito.anyString(), Mockito.anyLong());
        Mockito.doThrow(ConsentDataRetrievalException.class)
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString());

        consentCoreServiceImpl.amendDetailedConsent(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP,
                ConsentMgtServiceTestData.SAMPLE_USER_ID,
                new HashMap<>());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendDetailedConsentDataInsertError() throws Exception {

        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentReceipt(Mockito.any(),
                Mockito.anyString(), Mockito.anyString());
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentValidityTime(Mockito.any(),
                Mockito.anyString(), Mockito.anyLong());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString());
        Mockito.doThrow(ConsentDataInsertionException.class)
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(Mockito.any(),
                        Mockito.any(ConsentStatusAuditRecord.class));

        consentCoreServiceImpl.amendDetailedConsent(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP,
                ConsentMgtServiceTestData.SAMPLE_USER_ID,
                new HashMap<>());
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testAmendDetailedConsentDataDeletionError() throws Exception {

        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentReceipt(Mockito.any(),
                Mockito.anyString(), Mockito.anyString());
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentValidityTime(Mockito.any(),
                Mockito.anyString(), Mockito.anyLong());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getConsentResource(Mockito.any(), Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentStatusAuditRecord(sampleID,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(Mockito.any(),
                        Mockito.any(ConsentStatusAuditRecord.class));

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData
                        .getSampleTestConsentMappingResource(ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID))
                .when(mockedConsentCoreDAO).storeConsentMappingResource(Mockito.any(),
                        Mockito.any(ConsentMappingResource.class));
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(Mockito.any(),
                Mockito.any(), Mockito.anyString());

        Mockito.doThrow(ConsentDataDeletionException.class).when(mockedConsentCoreDAO)
                .deleteConsentAttributes(Mockito.any(), Mockito.anyString(), Mockito.any());
        Mockito.doReturn(true).when(mockedConsentCoreDAO).storeConsentAttributes(Mockito.any(),
                Mockito.any(ConsentAttributes.class));
        consentCoreServiceImpl.amendDetailedConsent(sampleID, ConsentMgtServiceTestData.SAMPLE_CONSENT_RECEIPT,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_VALIDITY_PERIOD,
                ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID,
                ConsentMgtServiceTestData.SAMPLE_ACCOUNT_IDS_AND_PERMISSIONS_MAP,
                ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS,
                ConsentMgtServiceTestData.SAMPLE_CONSENT_ATTRIBUTES_MAP,
                ConsentMgtServiceTestData.SAMPLE_USER_ID,
                new HashMap<>());
    }

    private void setInitialDataForAmendDetailedConsentSuccessFlow() throws Exception {

        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentReceipt(Mockito.any(),
                Mockito.anyString(), Mockito.anyString());
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentValidityTime(Mockito.any(),
                Mockito.anyString(), Mockito.anyLong());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getConsentResource(Mockito.any(), Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData.getSampleStoredTestConsentStatusAuditRecord(sampleID,
                        ConsentMgtServiceTestData.SAMPLE_CURRENT_STATUS))
                .when(mockedConsentCoreDAO).storeConsentStatusAuditRecord(Mockito.any(),
                        Mockito.any(ConsentStatusAuditRecord.class));

        Mockito.doReturn(ConsentMgtServiceTestData.getSampleDetailedStoredTestConsentResource())
                .when(mockedConsentCoreDAO).getDetailedConsentResource(Mockito.any(), Mockito.anyString());
        Mockito.doReturn(ConsentMgtServiceTestData
                        .getSampleTestConsentMappingResource(ConsentMgtServiceTestData.UNMATCHED_AUTHORIZATION_ID))
                .when(mockedConsentCoreDAO).storeConsentMappingResource(Mockito.any(),
                        Mockito.any(ConsentMappingResource.class));
        Mockito.doNothing().when(mockedConsentCoreDAO).updateConsentMappingStatus(Mockito.any(),
                Mockito.any(), Mockito.anyString());

        Mockito.doReturn(true).when(mockedConsentCoreDAO).deleteConsentAttributes(Mockito.any(),
                Mockito.anyString(), Mockito.any());
        Mockito.doReturn(true).when(mockedConsentCoreDAO).storeConsentAttributes(Mockito.any(),
                Mockito.any(ConsentAttributes.class));
    }
}
