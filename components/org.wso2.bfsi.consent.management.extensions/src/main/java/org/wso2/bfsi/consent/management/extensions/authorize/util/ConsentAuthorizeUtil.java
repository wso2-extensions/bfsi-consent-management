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

package org.wso2.bfsi.consent.management.extensions.authorize.util;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.bfsi.consent.management.dao.models.ConsentResource;
import org.wso2.bfsi.consent.management.extensions.common.ConsentException;
import org.wso2.bfsi.consent.management.extensions.common.ConsentExtensionConstants;
import org.wso2.bfsi.consent.management.extensions.common.ResponseStatus;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Base64;

/**
 * Util class for consent authorize operations.
 */
public class ConsentAuthorizeUtil {

    private static final Log log = LogFactory.getLog(ConsentAuthorizeUtil.class);

    /**
     * Method to extract request object from query params.
     *
     * @param spQueryParams  Query params
     * @return  requestObject
     */
    public static String extractRequestObject(String spQueryParams) throws ConsentException {

        if (StringUtils.isNotBlank(spQueryParams)) {
            String requestObject = null;
            String[] spQueries = spQueryParams.split("&");
            for (String param : spQueries) {
                if (param.contains("request=")) {
                    requestObject = (param.substring("request=".length())).replaceAll(
                            "\\r\\n|\\r|\\n|%20", "");
                }
            }
            if (requestObject != null) {
                return requestObject;
            }
        }
        throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Error while parsing the request object.");
    }

    /**
     * Method to validate the request object and extract consent ID.
     *
     * @param requestObject  Request object
     * @return consentId
     */
    public static String extractConsentId(String requestObject) throws ConsentException {

        String consentId = null;
        try {
            // validate request object and get the payload
            String requestObjectPayload;
            String[] jwtTokenValues = requestObject.split("\\.");
            if (jwtTokenValues.length == 3) {
                requestObjectPayload = new String(Base64.getUrlDecoder().decode(jwtTokenValues[1]),
                        StandardCharsets.UTF_8);
            } else {
                log.error("request object is not signed JWT");
                throw new ConsentException(ResponseStatus.BAD_REQUEST, "request object is not signed JWT");
            }
            Object payload = new JSONParser(JSONParser.MODE_PERMISSIVE).parse(requestObjectPayload);
            if (!(payload instanceof JSONObject)) {
                throw new ConsentException(ResponseStatus.BAD_REQUEST, "Payload is not a JSON object");
            }
            JSONObject jsonObject = (JSONObject) payload;

            // get consent id from the request object
            if (jsonObject.containsKey("claims")) {
                JSONObject claims = (JSONObject) jsonObject.get("claims");
                for (String claim : new String[]{"userinfo", "id_token"}) {
                    if (claims.containsKey(claim)) {
                        JSONObject claimObject = (JSONObject) claims.get(claim);
                        if (claimObject.containsKey("openbanking_intent_id")) {
                            JSONObject intentObject = (JSONObject) claimObject
                                    .get("openbanking_intent_id");
                            if (intentObject.containsKey("value")) {
                                consentId = (String) intentObject.get("value");
                                break;
                            }
                        }
                    }
                }
            }

            if (consentId == null) {
                log.error("intent_id not found in request object");
                throw new ConsentException(ResponseStatus.BAD_REQUEST, "intent_id not found in request object");
            }
            return consentId;

        } catch (ParseException e) {
            log.error("Error while parsing the request object.", e);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Error while parsing the request object.");
        }
    }

    /**
     * Method that consists the implementation for the validation of  payload and the consent,
     * this method also invokes the relevant methods to populate data for each flow.
     *
     * @param consentResource Consent Resource parameter containing consent related information retrieved
     *                        from database.
     * @return ConsentDataJson array
     */
    public static JSONArray getConsentData(ConsentResource consentResource) throws ConsentException {

        JSONArray consentDataJSON = new JSONArray();
        try {

            String receiptString = consentResource.getReceipt();
            Object receiptJSON = new JSONParser(JSONParser.MODE_PERMISSIVE).parse(receiptString);

            // Checking whether the request body is in JSON format
            if (!(receiptJSON instanceof JSONObject)) {
                log.error("Not a Json Object");
                throw new ConsentException(ResponseStatus.BAD_REQUEST, "Not a Json Object");
            }

            if (!ConsentExtensionConstants.AWAIT_AUTHORISE_STATUS.equals(consentResource.getCurrentStatus())) {
                log.error("Invalid status for the consent. Consent not in authorizable state.");
                // Currently throwing an error as a 400 response.
                // Developers have the option of appending a field IS_ERROR to the jsonObject
                // and showing it to the user in the webapp.If so,the IS_ERROR containsKey to be checked in
                // any later steps.
                throw new ConsentException(ResponseStatus.BAD_REQUEST, "Invalid status for the consent. " +
                        "Consent not in authorizable state.");
            }

            JSONObject receipt = (JSONObject) receiptJSON;

            // Checks if 'data' object is present in the receipt
            if (receipt.containsKey("Data")) {
                JSONObject data = (JSONObject) receipt.get("Data");

                String type = consentResource.getConsentType();
                switch (type) {
                    case ConsentExtensionConstants.ACCOUNTS:
                        populateAccountData(data, consentDataJSON);
                        break;
                    case ConsentExtensionConstants.PAYMENTS:
                        populatePaymentData(data, consentDataJSON);
                        break;
                    case ConsentExtensionConstants.FUNDS_CONFIRMATIONS:
                        populateCofData(data, consentDataJSON);
                        break;
                    default:
                        break;
                }
            } else {
                log.error("Data Object is missing");
                throw new ConsentException(ResponseStatus.BAD_REQUEST, "Data Object is missing");
            }
        } catch (ParseException e) {
            log.error("Error while retrieving consent", e);
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Error while retrieving consent");
        }
        return consentDataJSON;
    }

    /**
     * Populate Domestic and international Payment Details.
     *
     * @param data            data request from the request
     * @param consentDataJSON Consent information
     */
    private static void populatePaymentData(JSONObject data, JSONArray consentDataJSON) {

        JSONArray paymentTypeArray = new JSONArray();
        JSONObject jsonElementPaymentType = new JSONObject();

        if (data.containsKey(ConsentExtensionConstants.INITIATION)) {
            JSONObject initiation = (JSONObject) data.get(ConsentExtensionConstants.INITIATION);

            if (initiation.containsKey(ConsentExtensionConstants.CURRENCY_OF_TRANSFER)) {
                //For International Payments
                //Adding Payment Type
                paymentTypeArray.add(ConsentExtensionConstants.INTERNATIONAL_PAYMENTS);

                jsonElementPaymentType.put(ConsentExtensionConstants.TITLE,
                        ConsentExtensionConstants.PAYMENT_TYPE_TITLE);
                jsonElementPaymentType.put(StringUtils.lowerCase(ConsentExtensionConstants.DATA),
                        paymentTypeArray);
                consentDataJSON.add(jsonElementPaymentType);

                //Adding Currency Of Transfer
                JSONArray currencyTransferArray = new JSONArray();
                currencyTransferArray.add(initiation.getAsString(ConsentExtensionConstants.CURRENCY_OF_TRANSFER));

                JSONObject jsonElementCurTransfer = new JSONObject();
                jsonElementCurTransfer.put(ConsentExtensionConstants.TITLE,
                        ConsentExtensionConstants.CURRENCY_OF_TRANSFER_TITLE);
                jsonElementCurTransfer.put(StringUtils.lowerCase(ConsentExtensionConstants.DATA),
                        currencyTransferArray);
                consentDataJSON.add(jsonElementCurTransfer);
            } else {
                //Adding Payment Type
                paymentTypeArray.add(ConsentExtensionConstants.DOMESTIC_PAYMENTS);

                jsonElementPaymentType.put(ConsentExtensionConstants.TITLE,
                        ConsentExtensionConstants.PAYMENT_TYPE_TITLE);
                jsonElementPaymentType.put(StringUtils.lowerCase(ConsentExtensionConstants.DATA),
                        paymentTypeArray);
                consentDataJSON.add(jsonElementPaymentType);
            }

            //Adding InstructionIdentification
            JSONArray identificationArray = new JSONArray();
            identificationArray.add(initiation.getAsString(ConsentExtensionConstants.INSTRUCTION_IDENTIFICATION));

            JSONObject jsonElementIdentification = new JSONObject();
            jsonElementIdentification.put(ConsentExtensionConstants.TITLE,
                    ConsentExtensionConstants.INSTRUCTION_IDENTIFICATION_TITLE);
            jsonElementIdentification.put(StringUtils.lowerCase(ConsentExtensionConstants.DATA),
                    identificationArray);
            consentDataJSON.add(jsonElementIdentification);

            //Adding EndToEndIdentification
            JSONArray endToEndIdentificationArray = new JSONArray();
            endToEndIdentificationArray
                    .add(initiation.getAsString(ConsentExtensionConstants.END_TO_END_IDENTIFICATION));

            JSONObject jsonElementEndToEndIdentification = new JSONObject();
            jsonElementEndToEndIdentification.put(ConsentExtensionConstants.TITLE,
                    ConsentExtensionConstants.END_TO_END_IDENTIFICATION_TITLE);
            jsonElementEndToEndIdentification.put(StringUtils.lowerCase(ConsentExtensionConstants.DATA),
                    endToEndIdentificationArray);
            consentDataJSON.add(jsonElementEndToEndIdentification);

            //Adding InstructedAmount
            JSONObject instructedAmount = (JSONObject) initiation.get(ConsentExtensionConstants.INSTRUCTED_AMOUNT);
            JSONArray instructedAmountArray = new JSONArray();

            if (instructedAmount.getAsString(ConsentExtensionConstants.AMOUNT_TITLE) != null) {
                instructedAmountArray.add(ConsentExtensionConstants.AMOUNT_TITLE + " : " +
                        instructedAmount.getAsString(ConsentExtensionConstants.AMOUNT));
            }

            if (instructedAmount.getAsString(ConsentExtensionConstants.CURRENCY) != null) {
                instructedAmountArray.add(ConsentExtensionConstants.CURRENCY_TITLE + " : " +
                        instructedAmount.getAsString(ConsentExtensionConstants.CURRENCY));
            }

            JSONObject jsonElementInstructedAmount = new JSONObject();
            jsonElementInstructedAmount.put(ConsentExtensionConstants.TITLE,
                    ConsentExtensionConstants.INSTRUCTED_AMOUNT_TITLE);
            jsonElementInstructedAmount.put(StringUtils.lowerCase(ConsentExtensionConstants.DATA),
                    instructedAmountArray);
            consentDataJSON.add(jsonElementInstructedAmount);

            // Adding Debtor Account
            populateDebtorAccount(initiation, consentDataJSON);
            // Adding Creditor Account
            populateCreditorAccount(initiation, consentDataJSON);

        }
    }

    /**
     * Populate account Details.
     *
     * @param data            data request from the request
     * @param consentDataJSON Consent information
     */
    private static void populateAccountData(JSONObject data, JSONArray consentDataJSON) throws ConsentException {

        //Adding Permissions
        JSONArray permissions = (JSONArray) data.get(ConsentExtensionConstants.PERMISSIONS);
        if (permissions != null) {
            JSONObject jsonElementPermissions = new JSONObject();
            jsonElementPermissions.put(ConsentExtensionConstants.TITLE,
                    ConsentExtensionConstants.PERMISSIONS);
            jsonElementPermissions.put(StringUtils.lowerCase(ConsentExtensionConstants.DATA),
                    permissions);
            consentDataJSON.add(jsonElementPermissions);
        }

        //Adding Expiration Date Time
        String expirationDate = data.getAsString(ConsentExtensionConstants.EXPIRATION_DATE);
        if (expirationDate != null) {
            if (!validateExpiryDateTime(expirationDate)) {
                log.error(ConsentAuthorizeConstants.CONSENT_EXPIRED);
                throw new ConsentException(ResponseStatus.BAD_REQUEST, ConsentAuthorizeConstants.CONSENT_EXPIRED);
            }
            JSONArray expiryArray = new JSONArray();
            expiryArray.add(expirationDate);

            JSONObject jsonElementExpiry = new JSONObject();
            jsonElementExpiry.put(ConsentExtensionConstants.TITLE,
                    ConsentExtensionConstants.EXPIRATION_DATE_TITLE);
            jsonElementExpiry.put(StringUtils.lowerCase(ConsentExtensionConstants.DATA),
                    expiryArray);
            consentDataJSON.add(jsonElementExpiry);
        }

        //Adding Transaction From Date Time
        String fromDateTime = data.getAsString(ConsentExtensionConstants.TRANSACTION_FROM_DATE);
        if (fromDateTime != null) {
            JSONArray fromDateTimeArray = new JSONArray();
            fromDateTimeArray.add(fromDateTime);

            JSONObject jsonElementFromDateTime = new JSONObject();
            jsonElementFromDateTime.put(ConsentExtensionConstants.TITLE,
                    ConsentExtensionConstants.TRANSACTION_FROM_DATE_TITLE);
            jsonElementFromDateTime.put(StringUtils.lowerCase(ConsentExtensionConstants.DATA),
                    fromDateTimeArray);
            consentDataJSON.add(jsonElementFromDateTime);
        }

        //Adding Transaction To Date Time
        String toDateTime = data.getAsString(ConsentExtensionConstants.TRANSACTION_TO_DATE);
        if (toDateTime != null) {
            JSONArray toDateTimeArray = new JSONArray();
            toDateTimeArray.add(toDateTime);

            JSONObject jsonElementToDateTime = new JSONObject();
            jsonElementToDateTime.put(ConsentExtensionConstants.TITLE,
                    ConsentExtensionConstants.TRANSACTION_TO_DATE_TITLE);
            jsonElementToDateTime.put(StringUtils.lowerCase(ConsentExtensionConstants.DATA),
                    toDateTimeArray);
            consentDataJSON.add(jsonElementToDateTime);
        }
    }

    /**
     * Populate funds confirmation Details.
     *
     * @param initiation      data from the request
     * @param consentDataJSON Consent information
     */
    private static void populateCofData(JSONObject initiation, JSONArray consentDataJSON) throws ConsentException {

        //Adding Expiration Date Time
        if (initiation.getAsString(ConsentExtensionConstants.EXPIRATION_DATE) != null) {

            if (!validateExpiryDateTime(initiation.getAsString(ConsentExtensionConstants.EXPIRATION_DATE))) {
                log.error(ConsentAuthorizeConstants.CONSENT_EXPIRED);
                throw new ConsentException(ResponseStatus.BAD_REQUEST, ConsentAuthorizeConstants.CONSENT_EXPIRED);
            }

            String expiry = initiation.getAsString(ConsentExtensionConstants.EXPIRATION_DATE);
            JSONArray expiryArray = new JSONArray();
            expiryArray.add(expiry);

            JSONObject jsonElementExpiry = new JSONObject();
            jsonElementExpiry.put(ConsentExtensionConstants.TITLE,
                    ConsentExtensionConstants.EXPIRATION_DATE_TITLE);
            jsonElementExpiry.put(StringUtils.lowerCase(ConsentExtensionConstants.DATA), expiryArray);
            consentDataJSON.add(jsonElementExpiry);
        } else {
            JSONArray expiryArray = new JSONArray();
            expiryArray.add(ConsentExtensionConstants.OPEN_ENDED_AUTHORIZATION);

            JSONObject jsonElementExpiry = new JSONObject();
            jsonElementExpiry.put(ConsentExtensionConstants.TITLE,
                    ConsentExtensionConstants.EXPIRATION_DATE_TITLE);
            jsonElementExpiry.put(StringUtils.lowerCase(ConsentExtensionConstants.DATA), expiryArray);
            consentDataJSON.add(jsonElementExpiry);
        }

        if (initiation.get(ConsentExtensionConstants.DEBTOR_ACC) != null) {
            //Adding Debtor Account
            populateDebtorAccount(initiation, consentDataJSON);
        }
    }

    /**
     * Method to add debtor account details to consent data to send it to the consent page.
     *
     * @param initiation      Initiation object from the request
     * @param consentDataJSON Consent information object
     */
    public static void populateDebtorAccount(JSONObject initiation, JSONArray consentDataJSON) {
        if (initiation.get(ConsentExtensionConstants.DEBTOR_ACC) != null) {
            JSONObject debtorAccount = (JSONObject) initiation.get(ConsentExtensionConstants.DEBTOR_ACC);
            JSONArray debtorAccountArray = new JSONArray();

            //Adding Debtor Account Scheme Name
            if (debtorAccount.getAsString(ConsentExtensionConstants.SCHEME_NAME) != null) {
                debtorAccountArray.add(ConsentExtensionConstants.SCHEME_NAME_TITLE + " : " +
                        debtorAccount.getAsString(ConsentExtensionConstants.SCHEME_NAME));
            }

            //Adding Debtor Account Identification
            if (debtorAccount.getAsString(ConsentExtensionConstants.IDENTIFICATION) != null) {
                debtorAccountArray.add(ConsentExtensionConstants.IDENTIFICATION_TITLE + " : " +
                        debtorAccount.getAsString(ConsentExtensionConstants.IDENTIFICATION));
            }

            //Adding Debtor Account Name
            if (debtorAccount.getAsString(ConsentExtensionConstants.NAME) != null) {
                debtorAccountArray.add(ConsentExtensionConstants.NAME_TITLE + " : " +
                        debtorAccount.getAsString(ConsentExtensionConstants.NAME));
            }

            //Adding Debtor Account Secondary Identification
            if (debtorAccount.getAsString(ConsentExtensionConstants.SECONDARY_IDENTIFICATION) != null) {
                debtorAccountArray.add(ConsentExtensionConstants.SECONDARY_IDENTIFICATION_TITLE + " : " +
                        debtorAccount.getAsString(ConsentExtensionConstants.SECONDARY_IDENTIFICATION));
            }


            JSONObject jsonElementDebtor = new JSONObject();
            jsonElementDebtor.put(ConsentExtensionConstants.TITLE,
                    ConsentExtensionConstants.DEBTOR_ACC_TITLE);
            jsonElementDebtor.put(StringUtils.lowerCase(ConsentExtensionConstants.DATA), debtorAccountArray);
            consentDataJSON.add(jsonElementDebtor);
        }
    }


    /**
     * Method to add debtor account details to consent data to send it to the consent page.
     *
     * @param initiation     Initiation object from the request
     * @param consentDataJSON  Consent information object
     */
    public static void populateCreditorAccount(JSONObject initiation, JSONArray consentDataJSON) {
        if (initiation.get(ConsentExtensionConstants.CREDITOR_ACC) != null) {
            JSONObject creditorAccount = (JSONObject) initiation.get(ConsentExtensionConstants.CREDITOR_ACC);
            JSONArray creditorAccountArray = new JSONArray();
            //Adding Debtor Account Scheme Name
            if (creditorAccount.getAsString(ConsentExtensionConstants.SCHEME_NAME) != null) {
                creditorAccountArray.add(ConsentExtensionConstants.SCHEME_NAME_TITLE + " : " +
                        creditorAccount.getAsString(ConsentExtensionConstants.SCHEME_NAME));
            }
            //Adding Debtor Account Identification
            if (creditorAccount.getAsString(ConsentExtensionConstants.IDENTIFICATION) != null) {
                creditorAccountArray.add(ConsentExtensionConstants.IDENTIFICATION_TITLE + " : " +
                        creditorAccount.getAsString(ConsentExtensionConstants.IDENTIFICATION));
            }
            //Adding Debtor Account Name
            if (creditorAccount.getAsString(ConsentExtensionConstants.NAME) != null) {
                creditorAccountArray.add(ConsentExtensionConstants.NAME_TITLE + " : " +
                        creditorAccount.getAsString(ConsentExtensionConstants.NAME));
            }
            //Adding Debtor Account Secondary Identification
            if (creditorAccount.getAsString(ConsentExtensionConstants.SECONDARY_IDENTIFICATION) != null) {
                creditorAccountArray.add(ConsentExtensionConstants.SECONDARY_IDENTIFICATION_TITLE + " : " +
                        creditorAccount.getAsString(ConsentExtensionConstants.SECONDARY_IDENTIFICATION));
            }

            JSONObject jsonElementCreditor = new JSONObject();
            jsonElementCreditor.put(ConsentExtensionConstants.TITLE,
                    ConsentExtensionConstants.CREDITOR_ACC_TITLE);
            jsonElementCreditor.put(StringUtils.lowerCase(ConsentExtensionConstants.DATA),
                    creditorAccountArray);
            consentDataJSON.add(jsonElementCreditor);
        }
    }

    /**
     * Method to append Dummy data for Account ID. Ideally should be separate step calling accounts service
     *
     * @return accountsJSON
     */
    public static JSONArray appendDummyAccountID() {

        JSONArray accountsJSON = new JSONArray();
        JSONObject accountOne = new JSONObject();
        accountOne.put("account_id", "12345");
        accountOne.put("display_name", "Salary Saver Account");

        accountsJSON.add(accountOne);

        JSONObject accountTwo = new JSONObject();
        accountTwo.put("account_id", "67890");
        accountTwo.put("account_id", "67890");
        accountTwo.put("display_name", "Max Bonus Account");

        accountsJSON.add(accountTwo);

        return accountsJSON;

    }

    /**
     * Check if the expiry date time of the consent containsKey elapsed.
     *
     * @param expiryDate The expiry date/time of consent
     * @return boolean result of validation
     */
    public static boolean validateExpiryDateTime(String expiryDate) throws ConsentException {

        try {
            OffsetDateTime expDate = OffsetDateTime.parse(expiryDate);
            if (log.isDebugEnabled()) {
                log.debug(String.format(ConsentAuthorizeConstants.DATE_PARSE_MSG, expDate, OffsetDateTime.now()));
            }
            return OffsetDateTime.now().isBefore(expDate);
        } catch (DateTimeParseException e) {
            log.error(ConsentAuthorizeConstants.EXP_DATE_PARSE_ERROR, e);
            throw new ConsentException(ResponseStatus.BAD_REQUEST,
                    ConsentAuthorizeConstants.ACC_CONSENT_RETRIEVAL_ERROR);
        }
    }
}
