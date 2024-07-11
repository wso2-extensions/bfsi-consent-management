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

package org.wso2.bfsi.identity.extensions.auth.extensions.response.validator;

import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.mockito.MockedStatic;
import org.testng.annotations.Test;
import org.wso2.bfsi.consent.management.common.util.CommonUtils;
import org.wso2.carbon.identity.oauth2.RequestObjectException;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * OBCodeResponseTypeValidator Test class.
 */
public class OBCodeResponseTypeValidatorTest {

    @Test
    public void checkValidCodeResponseTypeValidation() throws OAuthProblemException, RequestObjectException {

        try (MockedStatic<CommonUtils> mock = mockStatic(CommonUtils.class)) {
            // Mock
            HttpServletRequest httpServletRequestMock = mock(HttpServletRequest.class);
            when(httpServletRequestMock.getParameter("response_type")).thenReturn("code");
            when(httpServletRequestMock.getParameter("client_id")).thenReturn("1234567654321");

            mock.when(() -> CommonUtils.isRegulatoryApp(anyString())).thenReturn(false);

            BFSICodeResponseTypeValidator uut = spy(new BFSICodeResponseTypeValidator());

            // Act
            uut.validateRequiredParameters(httpServletRequestMock);
        }
    }
}
