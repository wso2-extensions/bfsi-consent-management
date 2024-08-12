package org.wso2.bfsi.consent.management.extensions.util;

import org.wso2.bfsi.consent.management.dao.models.AuthorizationResource;
import org.wso2.bfsi.consent.management.dao.models.ConsentFile;
import org.wso2.bfsi.consent.management.dao.models.ConsentHistoryResource;
import org.wso2.bfsi.consent.management.dao.models.ConsentMappingResource;
import org.wso2.bfsi.consent.management.dao.models.ConsentResource;
import org.wso2.bfsi.consent.management.dao.models.ConsentStatusAuditRecord;
import org.wso2.bfsi.consent.management.dao.models.DetailedConsentResource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TestUtil {

    public static AuthorizationResource getSampleAuthorizationResource(String consentID, String authorizationID) {

        AuthorizationResource authorizationResource = new AuthorizationResource(consentID, TestConstants.SAMPLE_USER_ID,
                TestConstants.SAMPLE_AUTHORIZATION_STATUS, TestConstants.SAMPLE_AUTH_TYPE,
                System.currentTimeMillis() / 1000);
        authorizationResource.setAuthorizationID(authorizationID);

        return authorizationResource;
    }

    public static ArrayList<AuthorizationResource> getSampleAuthorizationResourceArray(String consentID,
                                                                                       String authorizationID) {
        return new ArrayList<>(List.of(TestUtil.getSampleAuthorizationResource(consentID, authorizationID)));
    }

    public static DetailedConsentResource getSampleDetailedConsentResource() {

        ArrayList<AuthorizationResource> authorizationResources = new ArrayList<>();
        authorizationResources.add(getSampleAuthorizationResource(TestConstants.SAMPLE_CONSENT_ID,
                TestConstants.SAMPLE_AUTH_ID));

        ArrayList<ConsentMappingResource> consentMappingResources = new ArrayList<>();
        consentMappingResources.add(getSampleConsentMappingResource(TestConstants.SAMPLE_AUTH_ID));
        consentMappingResources.add(getSampleInactiveConsentMappingResource(TestConstants.SAMPLE_AUTH_ID));

        return new DetailedConsentResource(
                TestConstants.SAMPLE_CONSENT_ID, TestConstants.SAMPLE_CLIENT_ID,
                TestConstants.VALID_INITIATION, TestConstants.ACCOUNTS,
                TestConstants.SAMPLE_AUTHORIZATION_STATUS, TestConstants.SAMPLE_CONSENT_FREQUENCY,
                TestConstants.SAMPLE_CONSENT_VALIDITY_PERIOD, System.currentTimeMillis() / 1000,
                System.currentTimeMillis() / 1000, TestConstants.SAMPLE_RECURRING_INDICATOR,
                TestConstants.SAMPLE_CONSENT_ATTRIBUTES_MAP, authorizationResources, consentMappingResources);
    }

    public static ConsentMappingResource getSampleConsentMappingResource(String authorizationID) {

        ConsentMappingResource consentMappingResource = new ConsentMappingResource(authorizationID,
                TestConstants.SAMPLE_ACCOUNT_ID, TestConstants.SAMPLE_PERMISSION,
                TestConstants.SAMPLE_MAPPING_STATUS);
        consentMappingResource.setMappingID(TestConstants.SAMPLE_MAPPING_ID);

        return consentMappingResource;
    }

    public static ConsentMappingResource getSampleInactiveConsentMappingResource(String authorizationID) {

        ConsentMappingResource consentMappingResource = getSampleConsentMappingResource(authorizationID);
        consentMappingResource.setMappingID(TestConstants.SAMPLE_MAPPING_ID_2);
        consentMappingResource.setMappingStatus(TestConstants.SAMPLE_NEW_MAPPING_STATUS);

        return consentMappingResource;
    }

    public static ConsentResource getSampleConsentResource(String status) {

        return new ConsentResource(null, UUID.randomUUID().toString(),
                TestConstants.SAMPLE_CONSENT_RECEIPT, TestConstants.SAMPLE_CONSENT_TYPE,
                TestConstants.SAMPLE_CONSENT_FREQUENCY, TestConstants.SAMPLE_CONSENT_VALIDITY_PERIOD,
                TestConstants.SAMPLE_RECURRING_INDICATOR, status,
                System.currentTimeMillis() / 1000, System.currentTimeMillis() / 1000);
    }

    public static ConsentHistoryResource getSampleConsentHistoryResource() {

        ConsentHistoryResource consentHistoryResource = new ConsentHistoryResource();

        consentHistoryResource.setTimestamp(TestConstants.SAMPLE_CONSENT_AMENDMENT_TIMESTAMP);
        consentHistoryResource.setReason(TestConstants.SAMPLE_AMENDMENT_REASON);
        consentHistoryResource.setDetailedConsentResource(getSampleDetailedConsentResource());

        return consentHistoryResource;
    }

    public static ConsentStatusAuditRecord getSampleConsentStatusAuditRecord(String consentID,
                                                                                 String currentStatus) {

        return new ConsentStatusAuditRecord(consentID, currentStatus,
                System.currentTimeMillis() / 1000, TestConstants.SAMPLE_REASON,
                TestConstants.SAMPLE_ACTION_BY, TestConstants.SAMPLE_PREVIOUS_STATUS);
    }

    public static ConsentFile getSampleConsentFileObject(String fileContent) {

        return new ConsentFile(UUID.randomUUID().toString(), fileContent);
    }
}
