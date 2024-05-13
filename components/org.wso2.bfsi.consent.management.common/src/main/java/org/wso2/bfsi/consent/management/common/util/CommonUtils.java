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

package org.wso2.bfsi.consent.management.common.util;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Common utility methods.
 */
public class CommonUtils {

    /**
     * Method to check whether the given string is a valid JSON.
     *
     * @param stringValue  string value
     * @return `true` if the given string is a valid JSON, `false` otherwise
     */
    public static boolean isValidJson(String stringValue) {
        try {
            (new JSONParser()).parse(stringValue);
            return true;
        } catch (ParseException E) {
            return false;
        }
    }
}
