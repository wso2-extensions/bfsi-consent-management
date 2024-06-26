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

package org.wso2.bfsi.consent.management.extensions.common;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.bfsi.consent.management.common.exceptions.ConsentManagementException;
import org.wso2.bfsi.consent.management.common.exceptions.ConsentManagementRuntimeException;
import org.wso2.bfsi.consent.management.common.util.Generated;
import org.wso2.bfsi.consent.management.dao.models.ConsentResource;
import org.wso2.bfsi.consent.management.dao.models.DetailedConsentResource;
import org.wso2.bfsi.consent.management.extensions.internal.ConsentExtensionsDataHolder;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Consent extension utils class.
 */
public class ConsentExtensionUtils {

    private static final Log log = LogFactory.getLog(ConsentExtensionUtils.class);

    /**
     * Method to get the consent type based on the request path.
     * @param requestPath  Request path of the request
     * @return Consent Type
     */
    public static String getConsentType(String requestPath) throws ConsentManagementException {
        if (requestPath.contains(ConsentExtensionConstants.ACCOUNT_CONSENT_PATH)) {
            return ConsentExtensionConstants.ACCOUNTS;
        } else if (requestPath.contains(ConsentExtensionConstants.COF_CONSENT_PATH)) {
            return ConsentExtensionConstants.FUNDS_CONFIRMATIONS;
        } else if (requestPath.contains(ConsentExtensionConstants.PAYMENT_CONSENT_PATH)) {
            return ConsentExtensionConstants.PAYMENTS;
        } else {
            throw new ConsentManagementException("Invalid consent type");
        }
    }

    /**
     * Validate the consent ID.
     *
     * @param consentId Consent Id to validate
     * @return Whether the consent ID is valid
     */
    public static boolean isConsentIdValid(String consentId) {
        return (Pattern.matches(ConsentExtensionConstants.UUID_REGEX, consentId));
    }

    /**
     * Convert long date values to ISO 8601 format.
     * @param dateValue  Date value in long
     * @return ISO 8601 formatted date
     */
    public static String convertToISO8601(long dateValue) {

        DateFormat simple = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        Date simpleDateVal = new Date(dateValue * 1000);
        return simple.format(simpleDateVal);
    }

    /**
     * Method to construct Initiation response.
     *
     * @param response       Response of the request
     * @param createdConsent Consent response received from service layer
     * @return  JSONObject Initiation Response
     */
    public static JSONObject getInitiationResponse(JSONObject response, DetailedConsentResource createdConsent) {
        JSONObject dataObject = response.getJSONObject(ConsentExtensionConstants.DATA);
        dataObject.put(ConsentExtensionConstants.CONSENT_ID, createdConsent.getConsentID());
        dataObject.put(ConsentExtensionConstants.CREATION_DATE_TIME, convertToISO8601(createdConsent.getCreatedTime()));
        dataObject.put(ConsentExtensionConstants.STTAUS_UPDATE_DATE_TIME,
                convertToISO8601(createdConsent.getUpdatedTime()));
        dataObject.put(ConsentExtensionConstants.STATUS, createdConsent.getCurrentStatus());

        response.remove(ConsentExtensionConstants.DATA);
        response.put(ConsentExtensionConstants.DATA, dataObject);

        return response;
    }

    /**
     * Method to construct Retrieval Initiation response.
     *
     * @param receiptJSON Initiation of the request
     * @param consent     Consent response received from service layer
     * @return  JSONObject Initiation Response
     */
    public static JSONObject getInitiationRetrievalResponse(JSONObject receiptJSON, ConsentResource consent) {

        JSONObject dataObject = receiptJSON.getJSONObject(ConsentExtensionConstants.DATA);
        dataObject.put(ConsentExtensionConstants.CONSENT_ID, consent.getConsentID());
        dataObject.put(ConsentExtensionConstants.CREATION_DATE_TIME, convertToISO8601(consent.getCreatedTime()));
        dataObject.put(ConsentExtensionConstants.STTAUS_UPDATE_DATE_TIME,
                convertToISO8601(consent.getUpdatedTime()));
        dataObject.put(ConsentExtensionConstants.STATUS, consent.getCurrentStatus());

        receiptJSON.remove(ConsentExtensionConstants.DATA);
        receiptJSON.put(ConsentExtensionConstants.DATA, dataObject);

        return receiptJSON;
    }

    @Generated(message = "Ignoring since method contains no logics")
    public static <T> T  getClassInstanceFromFQN(String classpath, Class<T> className) {

        try {
            Object classObj = Class.forName(classpath).getDeclaredConstructor().newInstance();
            return className.cast(classObj);
        } catch (ClassNotFoundException e) {
            log.error("Class not found: " + classpath.replaceAll("[\r\n]", ""));
            throw new ConsentManagementRuntimeException("Cannot find the defined class", e);
        } catch (InstantiationException | InvocationTargetException |
                 NoSuchMethodException | IllegalAccessException e) {
            //Throwing a runtime exception since we cannot proceed with invalid objects
            throw new ConsentManagementRuntimeException("Defined class" + classpath + "cannot be instantiated.", e);
        }
    }

    /**
     * Validate a JWT signature by providing the alias in the client truststore.
     * Skipped in unit tests since @KeystoreManager cannot be mocked
     *
     * @param jwtString string value of the JWT to be validated
     * @param alias     alias in the trust store
     * @return boolean value depicting whether the signature is valid
     * @throws ConsentManagementException error with message mentioning the cause
     */
    public static boolean validateJWTSignatureWithPublicKey(String jwtString, String alias)
            throws ConsentManagementException {

        Certificate certificate = getCertificateFromAlias(alias);

        if (certificate == null) {
            throw new ConsentManagementException("Certificate not found for provided alias");
        }
        PublicKey publicKey = certificate.getPublicKey();

        try {
            JWSVerifier verifier = new RSASSAVerifier((RSAPublicKey) publicKey);
            return SignedJWT.parse(jwtString).verify(verifier);
        } catch (JOSEException | java.text.ParseException e) {
            log.error("Error occurred while validating JWT signature", e);
            throw new ConsentManagementException("Error occurred while validating JWT signature");
        }

    }

    /**
     * Util method to get the configured trust store by carbon config or cached instance.
     *
     * @return Keystore instance of the truststore
     * @throws ConsentManagementException Error when loading truststore or carbon truststore config unavailable
     */
    public static KeyStore getTrustStore() throws ConsentManagementException {
        if (ConsentExtensionsDataHolder.getInstance().getTrustStore() == null) {
            String trustStoreLocation = System.getProperty("javax.net.ssl.trustStore");
            String trustStorePassword = System.getProperty("javax.net.ssl.trustStorePassword");
            String trustStoreType = System.getProperty("javax.net.ssl.trustStoreType");

            if (trustStoreLocation == null || trustStorePassword == null || trustStoreType == null) {
                log.error("Either of the Trust store configs (Location, Password or Type) is not available");
                throw new ConsentManagementException("Trust store config not available");
            }

            try (InputStream keyStoreStream = new FileInputStream(trustStoreLocation)) {
                KeyStore trustStore = KeyStore.getInstance(trustStoreType); // or "PKCS12"
                trustStore.load(keyStoreStream, trustStorePassword.toCharArray());
                ConsentExtensionsDataHolder.getInstance().setTrustStore(trustStore);
            } catch (IOException | CertificateException | KeyStoreException | NoSuchAlgorithmException e) {
                throw new ConsentManagementException("Error while loading truststore.", e);
            }
        }
        return ConsentExtensionsDataHolder.getInstance().getTrustStore();
    }

    /**
     * Util method to get the certificate from the trust store by alias.
     * @param alias  Alias of the certificate
     * @return  Certificate instance
     * @throws ConsentManagementException  Error while retrieving certificate from truststore
     */
    public static Certificate getCertificateFromAlias(String alias) throws ConsentManagementException {
        try {
            KeyStore trustStore = getTrustStore();
            return trustStore.getCertificate(alias);
        } catch (KeyStoreException | ConsentManagementException e) {
            throw new ConsentManagementException("Error while retrieving certificate from truststore");
        }
    }
}
