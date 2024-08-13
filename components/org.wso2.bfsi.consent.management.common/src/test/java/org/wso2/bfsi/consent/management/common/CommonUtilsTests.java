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

package org.wso2.bfsi.consent.management.common;

import net.minidev.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.bfsi.consent.management.common.util.CarbonUtils;
import org.wso2.bfsi.consent.management.common.util.CommonTestDataProvider;
import org.wso2.bfsi.consent.management.common.util.CommonUtils;

import java.text.ParseException;

/**
 * Common Util test.
 */
public class CommonUtilsTests {

    @BeforeClass
    public void beforeClass() {

        //to execute util class initialization
        new CarbonUtils();
    }

    @Test(dataProvider = "jwtData", dataProviderClass = CommonTestDataProvider.class)
    public void testDecodeRequestJWT(String jwtToken, String jwtPart) throws ParseException {

        JSONObject result = CommonUtils.decodeRequestJWT(jwtToken, jwtPart);
        Assert.assertNotNull(result);
    }

    @Test(expectedExceptions = ParseException.class)
    public void testDecodeRequestJWTInvalidScenario() throws ParseException {

        CommonUtils.decodeRequestJWT("invalid_jwt", "header");
    }
}
