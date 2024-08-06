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

package org.wso2.bfsi.identity.extensions.util;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.bfsi.consent.management.common.exceptions.ConsentManagementException;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

/**
 * Test class for IdentityCommonUtils.
 */
public class IdentityCommonUtilsTest {

    @Test
    public void testGetConsentIdFromScopes() {
        String[] scopes = {"dummy-scope1", "dummy-scope2", "consent_id_ConsentId", "dummy-scope3", "BFSI_ConsentId",
                "TIME_ConsentId", "x5t#_ConsentId"};
        Assert.assertTrue(Arrays.toString(scopes).contains("consent_id"));
        String[] modifiedScopes = IdentityCommonUtils.removeInternalScopes(scopes);
        Assert.assertFalse(Arrays.toString(modifiedScopes).contains("consent_id"));
        Assert.assertFalse(Arrays.toString(modifiedScopes).contains("BFSI_"));
        Assert.assertFalse(Arrays.toString(modifiedScopes).contains("TIME_"));
        Assert.assertFalse(Arrays.toString(modifiedScopes).contains("x5t#_"));
    }

    @Test(description = "when valid transport cert, return x509 certificate")
    public void testParseCertificate() throws ConsentManagementException {
        Assert.assertNotNull(IdentityCommonUtils.parseCertificate(TestConstants.TEST_CLIENT_CERT));
    }

    @Test (expectedExceptions = ConsentManagementException.class)
    public void testParseCertificateWithInvalidCert() throws ConsentManagementException {
        Assert.assertNull(IdentityCommonUtils.parseCertificate("-----INVALID CERTIFICATE-----"));
    }

    @Test
    public void testParseCertificateWithInvalidBase64CharactersCert() throws ConsentManagementException {
        Assert.assertNotNull(IdentityCommonUtils.parseCertificate(TestConstants.WRONGLY_FORMATTED_CERT));
    }

    @Test
    public void testParseCertificateWithEmptyCert() throws ConsentManagementException {
        Assert.assertNull(IdentityCommonUtils.parseCertificate(""));
    }

    @Test(description = "when certificate expired, return true")
    public void testIsExpiredWithExpiredCert() throws ConsentManagementException {
        X509Certificate testCert = IdentityCommonUtils.parseCertificate(TestConstants.EXPIRED_SELF_CERT);
        Assert.assertNotNull(testCert);
        Assert.assertTrue(hasExpired(testCert));
    }

    @Test(description = "when valid certificate, return false")
    public void testIsExpired() throws ConsentManagementException {
        X509Certificate testCert = IdentityCommonUtils.parseCertificate(TestConstants.TEST_CLIENT_CERT);
        Assert.assertNotNull(testCert);
        Assert.assertFalse(hasExpired(testCert));
    }

    /**
     * Test util method to check cert expiry.
     *
     * @param peerCertificate
     * @return
     */
    public static boolean hasExpired(X509Certificate peerCertificate) {
        try {
            peerCertificate.checkValidity();
        } catch (CertificateException e) {
            return true;
        }
        return false;
    }
}
