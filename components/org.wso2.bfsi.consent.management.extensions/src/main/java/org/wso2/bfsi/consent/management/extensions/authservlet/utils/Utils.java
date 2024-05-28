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

package org.wso2.bfsi.consent.management.extensions.authservlet.utils;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.owasp.encoder.Encode;
import org.wso2.bfsi.consent.management.extensions.common.ConsentExtensionConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

/**
 * Utility methods.
 */
public class Utils {

    private static final Log log = LogFactory.getLog(Utils.class);

    /**
     * To get the property value for the given key from the ResourceBundle.
     * Retrieve the value of property entry for key, return key if a value is not found for key
     *
     * @param resourceBundle  ResourceBundle
     * @param key  Key
     * @return  Value of the property entry for key
     */
    public static String i18n(ResourceBundle resourceBundle, String key) {

        try {
            return Encode.forHtml((StringUtils.isNotBlank(resourceBundle.getString(key)) ?
                    resourceBundle.getString(key) : key));
        } catch (Exception e) {
            // Intentionally catching Exception and if something goes wrong while finding the value for key, return
            // default, not to break the UI
            return Encode.forHtml(key);
        }
    }

    /**
     * Split claims based on a deliminator and create map of claimID and displayName.
     *
     * @param requestedClaimList   Requested claim list
     * @return  List of claims
     */
    public static List<Map<String, String>> splitClaims(String[] requestedClaimList) {

        List<Map<String, String>> requestedClaims = new ArrayList<>();

        for (String claim : requestedClaimList) {
            String[] requestedClaimData = claim.split("_", 2);
            if (requestedClaimData.length == 2) {
                Map<String, String> data = new HashMap<>();
                data.put("claimId", requestedClaimData[0]);
                data.put("displayName", requestedClaimData[1]);
                requestedClaims.add(data);
            }
        }
        return requestedClaims;
    }

    /**
     * Method to populate payments data to be sent to consent page.
     *
     * @param request  HttpServletRequest
     * @param dataSet  Request payload JSONObject
     * @return Map of Payments data
     */
    public static Map<String, Object> populatePaymentsData(HttpServletRequest request, JSONObject dataSet) {

        String selectedAccount = null;
        Map<String, Object> returnMaps = new HashMap<>();

        //Sets "data_requested" that contains the human-readable scope-requested information
        JSONArray dataRequestedJsonArray = (JSONArray) dataSet.get(ConsentExtensionConstants.CONSENT_DATA);
        Map<String, List<String>> dataRequested = new LinkedHashMap<>();

        for (int requestedDataIndex = 0; requestedDataIndex < dataRequestedJsonArray.size(); requestedDataIndex++) {
            JSONObject dataObj = (JSONObject) dataRequestedJsonArray.get(requestedDataIndex);
            String title = dataObj.getAsString(ConsentExtensionConstants.TITLE);
            JSONArray dataArray = (JSONArray) dataObj.get(StringUtils.lowerCase(ConsentExtensionConstants.DATA));

            ArrayList<String> listData = new ArrayList<>();
            for (int dataIndex = 0; dataIndex < dataArray.size(); dataIndex++) {
                listData.add((String) dataArray.get(dataIndex));
            }
            dataRequested.put(title, listData);
        }
        returnMaps.put(ConsentExtensionConstants.DATA_REQUESTED, dataRequested);

        //Assigning value of the "Debtor Account" key in the map to the variable "selectedAccount".
        if (dataRequested.containsKey("Debtor Account")) {
            selectedAccount = getDebtorAccFromConsentData(dataRequestedJsonArray);
        } else {
            // add accounts list
            request.setAttribute(ConsentExtensionConstants.ACCOUNT_DATA, addAccList(dataSet));
        }

        request.setAttribute(ConsentExtensionConstants.SELECTED_ACCOUNT, selectedAccount);
        request.setAttribute(ConsentExtensionConstants.CONSENT_TYPE, ConsentExtensionConstants.PAYMENTS);

        return returnMaps;

    }

    /**
     * Method to retrieve debtor account from consent data object.
     *
     * @param consentDataObject Object containing consent related data
     * @return Debtor account
     */
    public static String getDebtorAccFromConsentData(JSONArray consentDataObject) {

        for (int requestedDataIndex = 0; requestedDataIndex < consentDataObject.size(); requestedDataIndex++) {
            JSONObject dataObj = (JSONObject) consentDataObject.get(requestedDataIndex);
            String title = dataObj.getAsString(ConsentExtensionConstants.TITLE);

            if (ConsentExtensionConstants.DEBTOR_ACC_TITLE.equals(title)) {
                JSONArray dataArray = (JSONArray) dataObj.get(StringUtils.lowerCase(ConsentExtensionConstants.DATA));

                for (int dataIndex = 0; dataIndex < dataArray.size(); dataIndex++) {
                    String data = (String) dataArray.get(dataIndex);
                    if (data.contains(ConsentExtensionConstants.IDENTIFICATION_TITLE)) {

                        //Values are set to the array as {name:value} Strings in Consent Retrieval step,
                        // hence splitting by : and getting the 2nd element to get the value
                        return (((String) dataArray.get(dataIndex)).split(":")[1]).trim();
                    }
                }
            }
        }
        return null;
    }

    private static List<Map<String, String>>  addAccList (JSONObject dataSet) {
        // add accounts list
        List<Map<String, String>> accountData = new ArrayList<>();
        JSONArray accountsArray = (JSONArray) dataSet.get("accounts");
        for (int accountIndex = 0; accountIndex < accountsArray.size(); accountIndex++) {
            JSONObject object = (JSONObject) accountsArray.get(accountIndex);
            String accountId = object.getAsString(ConsentExtensionConstants.ACCOUNT_ID);
            String displayName = object.getAsString(ConsentExtensionConstants.DISPLAY_NAME);
            Map<String, String> data = new HashMap<>();
            data.put(ConsentExtensionConstants.ACCOUNT_ID, accountId);
            data.put(ConsentExtensionConstants.DISPLAY_NAME, displayName);
            accountData.add(data);
        }

        return accountData;
    }
}
