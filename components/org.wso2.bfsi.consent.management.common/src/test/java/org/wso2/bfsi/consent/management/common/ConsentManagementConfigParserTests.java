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

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.bfsi.consent.management.common.config.ConsentManagementConfigParser;
import org.wso2.bfsi.consent.management.common.config.ConsentManagementConfigurationService;
import org.wso2.bfsi.consent.management.common.config.ConsentManagementConfigurationServiceImpl;
import org.wso2.bfsi.consent.management.common.exceptions.ConsentManagementRuntimeException;
import org.wso2.bfsi.consent.management.common.util.CarbonUtils;
import org.wso2.bfsi.consent.management.common.util.CommonTestUtil;

import java.io.File;

/**
 * Tests for ConsentManagementConfigParser.
 */
public class ConsentManagementConfigParserTests {

    String absolutePathForTestResources;
    ConsentManagementConfigurationService configService;

    @BeforeClass
    public void beforeClass() throws ReflectiveOperationException {

        //to execute util class initialization
        new CarbonUtils();
        System.setProperty("some.property", "property.value");
        System.setProperty("carbon.home", ".");
        CommonTestUtil.injectEnvironmentVariable("CARBON_HOME", ".");
        String path = "src/test/resources";
        File file = new File(path);
        absolutePathForTestResources = file.getAbsolutePath();
    }

    //Runtime exception is thrown here because carbon home is not defined properly for an actual carbon product
    @Test(expectedExceptions = ConsentManagementRuntimeException.class, priority = 1)
    public void testConfigParserInitiationWithoutPath() {

        ConsentManagementConfigParser.getInstance();
    }

    @Test(priority = 2)
    public void testConfigParserInitiation() {

        System.setProperty("carbon.home", absolutePathForTestResources);
        ConsentManagementConfigParser parser = ConsentManagementConfigParser.getInstance();
        Assert.assertNotNull(parser);
    }

    @Test(priority = 3)
    public void testSingleton() {

        ConsentManagementConfigParser instance1 = ConsentManagementConfigParser.getInstance();
        ConsentManagementConfigParser instance2 = ConsentManagementConfigParser.getInstance();
        Assert.assertEquals(instance2, instance1);
    }

    @Test(priority = 4)
    public void testCarbonPath() {

        String carbonConfigDirPath = CarbonUtils.getCarbonConfigDirPath();
        System.setProperty("carbon.config.dir.path", carbonConfigDirPath);
        Assert.assertEquals(CarbonUtils.getCarbonConfigDirPath(), carbonConfigDirPath);
    }

    @Test(priority = 5)
    public void testGetConfigurationsFromService() {

        configService = new ConsentManagementConfigurationServiceImpl();
        Assert.assertNotNull(configService.getConfigurations());
    }

    @Test(priority = 5)
    public void testGetDataSourceName() {

        Assert.assertEquals(ConsentManagementConfigParser.getInstance().getDataSourceName(), "jdbc/WSO2OB_DB");
    }

    @Test(priority = 5)
    public void testGetConnectionVerificationTimeout() {

        Assert.assertEquals(ConsentManagementConfigParser.getInstance().getConnectionVerificationTimeout(), 2);
    }
}
