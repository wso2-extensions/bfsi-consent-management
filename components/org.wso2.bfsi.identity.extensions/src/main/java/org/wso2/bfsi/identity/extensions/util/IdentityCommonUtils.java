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

import com.google.common.base.Charsets;
import com.nimbusds.jose.JWSAlgorithm;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.wso2.bfsi.consent.management.common.exceptions.ConsentManagementException;
import org.wso2.bfsi.consent.management.common.exceptions.ConsentManagementRuntimeException;
import org.wso2.bfsi.consent.management.common.util.Generated;
import org.wso2.bfsi.identity.extensions.internal.IdentityExtensionsDataHolder;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth.cache.SessionDataCache;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheEntry;
import org.wso2.carbon.identity.oauth.cache.SessionDataCacheKey;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

/**
 * Common utility class for Identity Extensions.
 */
public class IdentityCommonUtils {

    private static final Log log = LogFactory.getLog(IdentityCommonUtils.class);

    /**
     * Method to obtain the Object when the full class path is given.
     *
     * @param classpath full class path
     * @return new object instance
     */
    @Generated(message = "Ignoring since method contains no logics")
    public static Object getClassInstanceFromFQN(String classpath) {

        try {
            return Class.forName(classpath).getDeclaredConstructor().newInstance();
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
     * Method to decode request object and retrieve values.
     *
     * @param request HTTP Servlet request.
     * @param key key to retrieve.
     * @return value.
     */
    @SuppressFBWarnings("SERVLET_PARAMETER")
    // Suppressed content - request.getParameter("response_type")
    // Suppression reason - False Positive : These endpoints are secured with access control
    // as defined in the IS deployment.toml file
    // Suppressed warning count - 3
    public static String decodeRequestObjectAndGetKey(HttpServletRequest request, String key)
            throws OAuthProblemException {

        try {
            if (request.getParameterMap().containsKey(IdentityCommonConstants.REQUEST_URI) &&
                    request.getParameter(IdentityCommonConstants.REQUEST_URI) != null) {

                // Consider as PAR request
                String[] requestUri = request.getParameter(IdentityCommonConstants.REQUEST_URI)
                        .replaceAll("[\r\n]", "").split(":");
                String requestUriRef = requestUri[requestUri.length - 1];
                SessionDataCacheEntry valueFromCache = SessionDataCache.getInstance()
                        .getValueFromCache(new SessionDataCacheKey(requestUriRef));
                if (valueFromCache != null) {
                    String essentialClaims = valueFromCache.getoAuth2Parameters().getEssentialClaims();
                    if (essentialClaims != null) {
                        String[] essentialClaimsWithExpireTime = essentialClaims.split(":");
                        essentialClaims = essentialClaimsWithExpireTime[0];
                        essentialClaims = essentialClaims.split("\\.")[1];
                        byte[] requestObject;
                        try {
                            requestObject = Base64.getDecoder().decode(essentialClaims);
                        } catch (IllegalArgumentException e) {

                            // Decode if the requestObject is base64-url encoded.
                            requestObject = Base64.getUrlDecoder().decode(essentialClaims);
                        }
                        JSONObject requestObjectVal = (JSONObject) (new JSONParser(JSONParser.MODE_PERMISSIVE))
                                .parse(new String(requestObject, StandardCharsets.UTF_8));
                        return requestObjectVal.containsKey(key) ? requestObjectVal.getAsString(key) : null;
                    }
                } else {
                    throw OAuthProblemException.error("invalid_request_uri")
                            .description("Provided request URI is not valid");
                }
            }
        } catch (ParseException e) {
            throw OAuthProblemException.error("invalid_request")
                    .description("Error occurred while parsing the request object");
        }
        return null;

    }

    /**
     * Remove the internal scopes from the space delimited list of authorized scopes.
     *
     * @param scopes Authorized scopes of the token
     * @return scopes by removing the internal scopes
     */
    public static String[] removeInternalScopes(String[] scopes) {

        String consentIdClaim = IdentityExtensionsDataHolder.getInstance().getConfigurationMap()
                .get(IdentityCommonConstants.CONSENT_ID_CLAIM_NAME).toString();

        if (scopes != null && scopes.length > 0) {
            List<String> scopesList = new LinkedList<>(Arrays.asList(scopes));
            scopesList.removeIf(s -> s.startsWith(consentIdClaim));
            scopesList.removeIf(s -> s.startsWith(IdentityCommonConstants.BFSI_PREFIX));
            scopesList.removeIf(s -> s.startsWith(IdentityCommonConstants.TIME_PREFIX));
            scopesList.removeIf(s -> s.startsWith(IdentityCommonConstants.CERT_PREFIX));
            return scopesList.toArray(new String[scopesList.size()]);
        }
        return scopes;
    }

    /**
     * Method to obtain Hash Value for a given String, default algorithm SHA256withRSA.
     *
     * @param value String value that required to be Hashed
     * @return Hashed String
     * @throws IdentityOAuth2Exception
     */
    public static String getHashValue(String value, String digestAlgorithm) throws IdentityOAuth2Exception {

        if (digestAlgorithm == null) {
            JWSAlgorithm digAlg = OAuth2Util.mapSignatureAlgorithmForJWSAlgorithm(
                    OAuthServerConfiguration.getInstance().getIdTokenSignatureAlgorithm());
            digestAlgorithm = OAuth2Util.mapDigestAlgorithm(digAlg);
        }
        MessageDigest md;
        try {
            md = MessageDigest.getInstance(digestAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IdentityOAuth2Exception("Error creating the hash value. Invalid Digest Algorithm: " +
                    digestAlgorithm);
        }
        //generating hash value
        md.update(value.getBytes(Charsets.UTF_8));
        byte[] digest = md.digest();
        int leftHalfBytes = digest.length / 2;
        byte[] leftmost = new byte[leftHalfBytes];
        System.arraycopy(digest, 0, leftmost, 0, leftHalfBytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(leftmost)
                .replace("\n", "").replace("\r", "");
    }

    /**
     * Get the configured certificate header name.
     *
     * @return value of the cert header name configuration
     */
    public static String getMTLSAuthHeader() {

        return Optional.ofNullable(IdentityUtil.getProperty(IdentityCommonConstants.MTLS_AUTH_HEADER))
                .orElse("CONFIG_NOT_FOUND");
    }

    /**
     * Parse the certificate content.
     *
     * @param content the content to be passed
     * @return the parsed certificate
     * @throws ConsentManagementException  if an error occurs while parsing the certificate
     */
    public static X509Certificate parseCertificate(String content) throws ConsentManagementException {

        try {
            if (StringUtils.isNotBlank(content)) {
                // removing illegal base64 characters before decoding
                content = removeIllegalBase64Characters(content);
                byte[] bytes = Base64.getDecoder().decode(content);

                return (java.security.cert.X509Certificate) CertificateFactory.getInstance(IdentityCommonConstants.X509)
                        .generateCertificate(new ByteArrayInputStream(bytes));
            }
            log.error("Certificate passed through the request is empty");
            return null;
        } catch (CertificateException | IllegalArgumentException e) {
            throw new ConsentManagementException("Certificate passed through the request not valid", e);
        }
    }

    /**
     * Remove illegal base64 characters from input string.
     *
     * @param value certificate as a string
     * @return certificate without illegal base64 characters
     */
    private static String removeIllegalBase64Characters(String value) {
        if (value.contains(IdentityCommonConstants.BEGIN_CERT)
                && value.contains(IdentityCommonConstants.END_CERT)) {

            // extracting certificate content
            value = value.substring(value.indexOf(IdentityCommonConstants.BEGIN_CERT)
                            + IdentityCommonConstants.BEGIN_CERT.length(),
                    value.indexOf(IdentityCommonConstants.END_CERT));
        }
        // remove spaces, \r, \\r, \n, \\n, ], [ characters from certificate string
        return value.replaceAll("\\\\r|\\\\n|\\r|\\n|\\[|]| ", StringUtils.EMPTY);
    }
}
