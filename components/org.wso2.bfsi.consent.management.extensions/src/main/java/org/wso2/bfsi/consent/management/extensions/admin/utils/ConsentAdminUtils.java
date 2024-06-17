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

package org.wso2.bfsi.consent.management.extensions.admin.utils;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.wso2.bfsi.consent.management.dao.models.AuthorizationResource;
import org.wso2.bfsi.consent.management.dao.models.ConsentMappingResource;
import org.wso2.bfsi.consent.management.dao.models.DetailedConsentResource;
import org.wso2.bfsi.consent.management.extensions.common.ConsentException;
import org.wso2.bfsi.consent.management.extensions.common.ConsentExtensionConstants;
import org.wso2.bfsi.consent.management.extensions.common.ResponseStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 * Consent admin utils.
 */
public class ConsentAdminUtils {

    /**
     * Validate and retrieve query param.
     * 1. Check whether the key exists as a query param.
     * 2. Validate whether the value is a string.
     * 3. Retrieve query param.
     *
     * @param queryParams  query params
     * @param key          key to be retrieved
     * @return   query param value
     */
    public static String validateAndGetQueryParam(Map queryParams, String key) {
        if (queryParams.containsKey(key) && (((ArrayList<?>) queryParams.get(key)).get(0) instanceof String)) {
            return (String) ((ArrayList<?>) queryParams.get(key)).get(0);
        }
        return null;
    }

    /**
     * Get array list from query param.
     * @param queryParam    query param values
     * @return  array list constructed from the query param value
     */
    public static ArrayList<String> getArrayListFromQueryParam(String queryParam) {
        return queryParam != null ? new ArrayList<>(Arrays.asList(queryParam.split(","))) : null;
    }

    /**
     * Get long values from query param.
     * @param queryParam    query param values
     * @return  long value constructed from the query param value
     */
    public static long getLongFromQueryParam(String queryParam) {
        return queryParam != null ? Long.parseLong(queryParam) : null;
    }

    /**
     * Get int values from query param.
     * @param queryParam    query param values
     * @return  int value constructed from the query param value
     */
    public static int getIntFromQueryParam(String queryParam) {
        return queryParam != null ? Integer.parseInt(queryParam) : null;
    }

    /**
     * Convert detailed consent resource to JSON.
     * @param detailedConsentResource   detailed consent resource
     * @return  JSON object constructed from the detailed consent resource
     */
    public static JSONObject detailedConsentToJSON(DetailedConsentResource detailedConsentResource) {
        JSONObject consentResource = new JSONObject();

        consentResource.appendField(ConsentExtensionConstants.CC_CONSENT_ID,
                detailedConsentResource.getConsentID());
        consentResource.appendField(ConsentExtensionConstants.CLIENT_ID, detailedConsentResource.getClientID());
        try {
            consentResource.appendField(ConsentExtensionConstants.RECEIPT, (new JSONParser(JSONParser.MODE_PERMISSIVE)).
                    parse(detailedConsentResource.getReceipt()));
        } catch (ParseException e) {
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Exception occurred while parsing" +
                    " receipt");
        }
        consentResource.appendField(ConsentExtensionConstants.CONSENT_TYPE, detailedConsentResource.getConsentType());
        consentResource.appendField(ConsentExtensionConstants.CURRENT_STATUS,
                detailedConsentResource.getCurrentStatus());
        consentResource.appendField(ConsentExtensionConstants.CONSENT_FREQUENCY,
                detailedConsentResource.getConsentFrequency());
        consentResource.appendField(ConsentExtensionConstants.VALIDITY_PERIOD,
                detailedConsentResource.getValidityPeriod());
        consentResource.appendField(ConsentExtensionConstants.CREATED_TIMESTAMP,
                detailedConsentResource.getCreatedTime());
        consentResource.appendField(ConsentExtensionConstants.UPDATED_TIMESTAMP,
                detailedConsentResource.getUpdatedTime());
        consentResource.appendField(ConsentExtensionConstants.RECURRING_INDICATOR,
                detailedConsentResource.isRecurringIndicator());
        JSONObject attributes = new JSONObject();
        Map<String, String> attMap = detailedConsentResource.getConsentAttributes();
        for (Map.Entry<String, String> entry : attMap.entrySet()) {
            attributes.appendField(entry.getKey(), entry.getValue());
        }
        consentResource.appendField(ConsentExtensionConstants.CONSENT_ATTRIBUTES, attributes);
        JSONArray authorizationResources = new JSONArray();
        ArrayList<AuthorizationResource> authArray = detailedConsentResource.getAuthorizationResources();
        for (AuthorizationResource resource : authArray) {
            JSONObject resourceJSON = new JSONObject();
            resourceJSON.appendField(ConsentExtensionConstants.AUTH_ID, resource.getAuthorizationID());
            resourceJSON.appendField(ConsentExtensionConstants.CC_CONSENT_ID, resource.getConsentID());
            resourceJSON.appendField(ConsentExtensionConstants.USER_ID, resource.getUserID());
            resourceJSON.appendField(ConsentExtensionConstants.AUTH_STATUS, resource.getAuthorizationStatus());
            resourceJSON.appendField(ConsentExtensionConstants.AUTH_TYPE, resource.getAuthorizationType());
            resourceJSON.appendField(ConsentExtensionConstants.UPDATE_TIME, resource.getUpdatedTime());
            authorizationResources.add(resourceJSON);
        }
        consentResource.appendField(ConsentExtensionConstants.AUTH_RESOURCES, authorizationResources);
        JSONArray consentMappingResources = new JSONArray();
        ArrayList<ConsentMappingResource> mappingArray = detailedConsentResource.getConsentMappingResources();
        for (ConsentMappingResource resource : mappingArray) {
            JSONObject resourceJSON = new JSONObject();
            resourceJSON.appendField(ConsentExtensionConstants.MAPPING_ID, resource.getMappingID());
            resourceJSON.appendField(ConsentExtensionConstants.AUTH_ID, resource.getAuthorizationID());
            resourceJSON.appendField(ConsentExtensionConstants.ACCOUNT_ID, resource.getAccountID());
            resourceJSON.appendField(ConsentExtensionConstants.PERMISSION, resource.getPermission());
            resourceJSON.appendField(ConsentExtensionConstants.MAPPING_STATUS, resource.getMappingStatus());
            consentMappingResources.add(resourceJSON);
        }
        consentResource.appendField(ConsentExtensionConstants.MAPPING_RESOURCES, consentMappingResources);
        return consentResource;
    }
}
