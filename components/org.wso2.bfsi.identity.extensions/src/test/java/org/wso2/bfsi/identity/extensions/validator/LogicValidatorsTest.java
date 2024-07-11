/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.bfsi.identity.extensions.validator;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.hibernate.validator.HibernateValidator;
import org.mockito.MockedStatic;
import org.testng.annotations.Test;
import org.wso2.bfsi.consent.management.common.util.CommonUtils;
import org.wso2.bfsi.identity.extensions.util.IdentityCommonUtils;
import org.wso2.bfsi.identity.extensions.util.TestConstants;
import org.wso2.bfsi.identity.extensions.validator.resources.AudValidationRequestObject;
import org.wso2.bfsi.identity.extensions.validator.resources.SampleChildRequestObject;
import org.wso2.bfsi.identity.extensions.validator.resources.SampleRequestObject;
import org.wso2.bfsi.identity.extensions.validator.resources.ValidatorTestDataProvider;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth2.RequestObjectException;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;

import java.text.ParseException;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

/**
 * Logic validators test.
 */
public class LogicValidatorsTest {

    private SampleRequestObject sampleRequestObject = new SampleRequestObject();
    private static Validator uut = Validation.byProvider(HibernateValidator.class).configure().addProperty(
            "hibernate.uut.fail_fast", "true").buildValidatorFactory().getValidator();


    @Test(dataProvider = "dp-checkValidScopeFormat", dataProviderClass = ValidatorTestDataProvider.class)
    public void checkValidScopeFormat(String claimsString) throws ParseException {

        //Assign
        sampleRequestObject.setClaimSet(JWTClaimsSet.parse(claimsString));
        //Act
        Set<ConstraintViolation<SampleRequestObject>> violations = uut.validate(sampleRequestObject);

        //Assert
        String violation = violations.stream().findFirst().map(ConstraintViolation::getMessage).orElse(null);
        assertNull("Valid scope formats should pass", violation);
    }

    @Test(dataProvider = "dp-checkValidationsInherited", dataProviderClass = ValidatorTestDataProvider.class)
    public void checkAudienceValidations(String claimsString) throws ParseException, RequestObjectException {

        try (MockedStatic<OAuthServerConfiguration> configurationMock = mockStatic(OAuthServerConfiguration.class)) {

            OAuthServerConfiguration oAuthServerConfiguration = mock(OAuthServerConfiguration.class);
            configurationMock.when(OAuthServerConfiguration::getInstance).thenReturn(oAuthServerConfiguration);

            try (MockedStatic<OAuth2Util> oAuth2UtilMock = mockStatic(OAuth2Util.class)) {
                AudValidationRequestObject sampleChildRequestObject = new AudValidationRequestObject();

                oAuth2UtilMock.when(() -> OAuth2Util.getIdTokenIssuer(anyString())).thenReturn("issuer");

                sampleChildRequestObject.setClaimSet(JWTClaimsSet.parse(claimsString));
                sampleChildRequestObject.setSignedJWT(SignedJWT.parse(TestConstants.VALID_REQUEST));
                Set<ConstraintViolation<SampleRequestObject>> violations = uut.validate(sampleChildRequestObject);
                String violation = violations.stream().findFirst().map(ConstraintViolation::getMessage).orElse(null);
                assertNotNull("Inherited validations should work", violation);
            }
        }
    }

    @Test(dataProvider = "dp-checkValidationsInherited", dataProviderClass = ValidatorTestDataProvider.class)
    public void checkValidationsInherited(String claimsString) throws ParseException, RequestObjectException {

        try (MockedStatic<OAuthServerConfiguration> configurationMock = mockStatic(OAuthServerConfiguration.class);
             MockedStatic<IdentityCommonUtils> identityCommonUtilsMock = mockStatic(IdentityCommonUtils.class);
             MockedStatic<CommonUtils> commonUtilsMock = mockStatic(CommonUtils.class)) {

            SampleChildRequestObject sampleChildRequestObject = new SampleChildRequestObject();

            OAuthServerConfiguration oAuthServerConfiguration = mock(OAuthServerConfiguration.class);
            configurationMock.when(OAuthServerConfiguration::getInstance).thenReturn(oAuthServerConfiguration);
            identityCommonUtilsMock.when(() -> IdentityCommonUtils.getCertificateContent(anyString()))
                    .thenReturn("");
            identityCommonUtilsMock.when(() -> IdentityCommonUtils
                            .getAppPropertyFromSPMetaData(anyString(), anyString())).thenReturn("PS256");
            commonUtilsMock.when(() -> CommonUtils.isRegulatoryApp(anyString())).thenReturn(true);

            sampleChildRequestObject.setClaimSet(JWTClaimsSet.parse(claimsString));
            sampleChildRequestObject.setSignedJWT(SignedJWT.parse(TestConstants.VALID_REQUEST));
            Set<ConstraintViolation<SampleRequestObject>> violations = uut.validate(sampleChildRequestObject);
            String violation = violations.stream().findFirst().map(ConstraintViolation::getMessage).orElse(null);
            assertNotNull("Inherited validations should work", violation);
        }
    }

    @Test(dataProvider = "dp-checkValidationsInherited", dataProviderClass = ValidatorTestDataProvider.class)
    public void checkSigningAlgoWithCertificate(String claimsString) throws ParseException, RequestObjectException {

        try (MockedStatic<OAuthServerConfiguration> configurationMock = mockStatic(OAuthServerConfiguration.class);
             MockedStatic<IdentityCommonUtils> identityCommonUtilsMock = mockStatic(IdentityCommonUtils.class);
             MockedStatic<CommonUtils> commonUtilsMock = mockStatic(CommonUtils.class)) {

            SampleChildRequestObject sampleChildRequestObject = new SampleChildRequestObject();

            OAuthServerConfiguration oAuthServerConfiguration = mock(OAuthServerConfiguration.class);
            configurationMock.when(OAuthServerConfiguration::getInstance).thenReturn(oAuthServerConfiguration);
            identityCommonUtilsMock.when(() -> IdentityCommonUtils.getCertificateContent(anyString()))
                    .thenReturn("test-certificate-content");
            identityCommonUtilsMock.when(() -> IdentityCommonUtils
                    .getAppPropertyFromSPMetaData(anyString(), anyString())).thenReturn("PS256");
            commonUtilsMock.when(() -> CommonUtils.isRegulatoryApp(anyString())).thenReturn(true);

            sampleChildRequestObject.setClaimSet(JWTClaimsSet.parse(claimsString));
            sampleChildRequestObject.setSignedJWT(SignedJWT.parse(TestConstants.VALID_REQUEST));
            Set<ConstraintViolation<SampleRequestObject>> violations = uut.validate(sampleChildRequestObject);
            String violation = violations.stream().findFirst().map(ConstraintViolation::getMessage).orElse(null);
            assertNotNull("Inherited validations should work", violation);
        }
    }

    @Test(dataProvider = "dp-checkValidationsInherited", dataProviderClass = ValidatorTestDataProvider.class)
    public void checkSigningAlgoWithEmptyAlgo(String claimsString) throws ParseException, RequestObjectException {

        try (MockedStatic<OAuthServerConfiguration> configurationMock = mockStatic(OAuthServerConfiguration.class);
             MockedStatic<IdentityCommonUtils> identityCommonUtilsMock = mockStatic(IdentityCommonUtils.class);
             MockedStatic<CommonUtils> commonUtilsMock = mockStatic(CommonUtils.class)) {

            SampleChildRequestObject sampleChildRequestObject = new SampleChildRequestObject();

            OAuthServerConfiguration oAuthServerConfiguration = mock(OAuthServerConfiguration.class);
            configurationMock.when(OAuthServerConfiguration::getInstance).thenReturn(oAuthServerConfiguration);
            identityCommonUtilsMock.when(() -> IdentityCommonUtils.getCertificateContent(anyString()))
                    .thenReturn("");
            identityCommonUtilsMock.when(() -> IdentityCommonUtils
                    .getAppPropertyFromSPMetaData(anyString(), anyString())).thenReturn("");
            commonUtilsMock.when(() -> CommonUtils.isRegulatoryApp(anyString())).thenReturn(true);

            sampleChildRequestObject.setClaimSet(JWTClaimsSet.parse(claimsString));
            sampleChildRequestObject.setSignedJWT(SignedJWT.parse(TestConstants.VALID_REQUEST));
            Set<ConstraintViolation<SampleRequestObject>> violations = uut.validate(sampleChildRequestObject);
            String violation = violations.stream().findFirst().map(ConstraintViolation::getMessage).orElse(null);
            assertNotNull("Inherited validations should work", violation);
        }
    }

    @Test(dataProvider = "dp-checkValidScopeFormat", dataProviderClass = ValidatorTestDataProvider.class)
    public void checkOpenBankingValidator(String claimsString) throws ParseException {

        //Assign
        sampleRequestObject.setClaimSet(JWTClaimsSet.parse(claimsString));
        //Act
        String violation = BFSIValidator.getInstance().getFirstViolation(sampleRequestObject);

        //Assert
        assertNull("Valid scope formats should pass", violation);
    }
}
