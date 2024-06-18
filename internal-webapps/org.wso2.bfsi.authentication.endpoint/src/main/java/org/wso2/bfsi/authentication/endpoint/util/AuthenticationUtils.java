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

package org.wso2.bfsi.authentication.endpoint.util;

import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for authentication related operations.
 */
public class AuthenticationUtils {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationUtils.class);

    /**
     * @param data error response received from consent data retrieval endpoint
     * @return formatted error response to be send to call back uri
     */
    public static String getErrorResponseForRedirectURL(JSONObject data) {

        String errorResponse = "";
        try {
            if (data.containsKey(Constants.ERROR)) {
                errorResponse = errorResponse.concat(Constants.ERROR_URI_FRAGMENT)
                        .concat(URLEncoder.encode(data.get(Constants.ERROR).toString(),
                                StandardCharsets.UTF_8.toString()));
            }
            if (data.containsKey(Constants.ERROR_DESCRIPTION)) {
                errorResponse = errorResponse.concat(Constants.ERROR_DESCRIPTION_PARAMETER)
                        .concat(URLEncoder.encode(data.get(Constants.ERROR_DESCRIPTION).toString(),
                                StandardCharsets.UTF_8.toString()));
            }
            if (data.containsKey(Constants.STATE)) {
                errorResponse = errorResponse.concat(Constants.STATE_PARAMETER)
                        .concat(URLEncoder.encode(data.get(Constants.STATE).toString(),
                                StandardCharsets.UTF_8.toString()));
            }

        } catch (UnsupportedEncodingException e) {
            log.error("Error while building error response", e);
        }
        return errorResponse;
    }
}
