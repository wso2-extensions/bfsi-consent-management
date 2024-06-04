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

package org.wso2.bfsi.consent.management.extensions.manage.impl;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.bfsi.consent.management.extensions.common.ConsentExtensionConstants;
import org.wso2.bfsi.consent.management.extensions.common.ResponseStatus;
import org.wso2.bfsi.consent.management.extensions.manage.ConsentManageValidator;
import org.wso2.bfsi.consent.management.extensions.manage.model.ConsentPayloadValidationResult;
import org.wso2.bfsi.consent.management.extensions.manage.utils.ConsentManageUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Payload validator class.
 */
public class DefaultConsentManageValidator implements ConsentManageValidator {

    private static final Log log = LogFactory.getLog(DefaultConsentManageValidator.class);

    private static final List<String> validPermissions = Arrays.asList(
            "ReadAccountsDetail",
            "ReadTransactionsDetail",
            "ReadBalances");

    @Override
    public ConsentPayloadValidationResult validateInitiation(JSONObject initiation, String consentType) {
        switch (consentType) {
            case ConsentExtensionConstants.ACCOUNTS:
                return validateAccountInitiation(initiation);
            case ConsentExtensionConstants.FUNDS_CONFIRMATIONS:
                return validateCOFInitiation(initiation);
            case ConsentExtensionConstants.PAYMENTS:
                return validatePaymentInitiation(initiation);
            default:
                return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST, "invalid_consent_type",
                        "Invalid consent type");
        }
    }

    /**
     * Method to validate account initiation request.
     *
     * @param initiation      Initiation Object
     * @return ConsentPayloadValidationResult     Validation Result
     */
    public static ConsentPayloadValidationResult validateAccountInitiation(JSONObject initiation) {
        ConsentPayloadValidationResult dataValidationResult = validateDataObjInRequestBody(initiation);
        if (!(boolean) dataValidationResult.isValid()) {
            return dataValidationResult;
        }

        JSONObject data = (JSONObject) initiation.get("Data");

        if (!data.containsKey("Permissions") || !(data.get("Permissions") instanceof JSONArray)) {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                    "Permissions are not in correct format");
        }

        JSONArray permissions = (JSONArray) data.get("Permissions");
        for (Object permission : permissions) {
            if (!(permission instanceof String)) {
                return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                        ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                        "Permissions should be string values");
            }
            String permissionString = (String) permission;
            if (!validPermissions.contains(permissionString)) {
                return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                        ResponseStatus.BAD_REQUEST.getReasonPhrase(), "Permissions are invalid");
            }
        }

        if (!data.containsKey("ExpirationDateTime") || !(data.get("ExpirationDateTime") instanceof String)) {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(), "ExpirationDateTime is invalid");
        }

        if (!ConsentManageUtils.isConsentExpirationTimeValid(data.getAsString("ExpirationDateTime"))) {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                    "ExpirationDateTime should be a future time");
        }

        if (!data.containsKey("TransactionFromDateTime") || !(data.get("TransactionFromDateTime") instanceof String)) {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(), "TransactionFromDateTime is invalid");
        }

        if (!data.containsKey("TransactionToDateTime") || !(data.get("TransactionToDateTime") instanceof String)) {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(), "TransactionToDateTime is invalid");
        }

        if (!ConsentManageUtils.isTransactionFromToTimeValid(data.getAsString("TransactionFromDateTime"),
                data.getAsString("TransactionToDateTime"))) {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                    "TransactionToDateTime should be after TransactionFromDateTime");
        }

        return new ConsentPayloadValidationResult(true);
    }

    /**
     * Method to validate COF initiation request.
     *
     * @param initiation      Initiation Object
     * @return ConsentPayloadValidationResult     Validation Result
     */
    public static ConsentPayloadValidationResult validateCOFInitiation(JSONObject initiation) {
        //Check request body is valid and not empty
        ConsentPayloadValidationResult dataValidationResult = validateDataObjInRequestBody(initiation);
        if (!(boolean) dataValidationResult.isValid()) {
            return dataValidationResult;
        }

        JSONObject data = (JSONObject) initiation.get(ConsentExtensionConstants.DATA);

        //Validate json payload expirationDateTime is a future date
        if (data.containsKey(ConsentExtensionConstants.EXPIRATION_DATE) && !ConsentManageUtils
                .isConsentExpirationTimeValid(data.getAsString(ConsentExtensionConstants.EXPIRATION_DATE))) {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                    "ExpirationDateTime should be after TransactionFromDateTime");
        }

        if (data.containsKey(ConsentExtensionConstants.DEBTOR_ACC)) {

            Object debtorAccountObj = data.get(ConsentExtensionConstants.DEBTOR_ACC);
            //Check whether debtor account is a JsonObject
            if (!(debtorAccountObj instanceof JSONObject)) {
                return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                        ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                        "Debtor Account is not in correct format");
            }

            JSONObject debtorAccount = (JSONObject) data.get(ConsentExtensionConstants.DEBTOR_ACC);
            //Check whether debtor account is not empty
            if (debtorAccount.isEmpty()) {
                return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                        ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                        "Data object is not in correct format");
            }


        } else {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                    "Debtor Account should be present in the request");
        }
        return new ConsentPayloadValidationResult(true);
    }

    /**
     * Method to validate payment initiation request.
     *
     * @param request      Initiation Object
     * @return ConsentPayloadValidationResult     Validation Result
     */
    public static ConsentPayloadValidationResult validatePaymentInitiation(JSONObject request) {

        //Check request body is valid and not empty
        ConsentPayloadValidationResult dataValidationResult = validateDataObjInRequestBody(request);
        if (!(boolean) dataValidationResult.isValid()) {
            return dataValidationResult;
        }
        JSONObject data = (JSONObject) request.get("Data");

        //Check request body is valid and not empty
        ConsentPayloadValidationResult validationResult = validateInitiationObjInRequestBody(data);
        if (!(boolean) validationResult.isValid()) {
            return validationResult;
        }
        JSONObject initiation = (JSONObject) data.get("Initiation");

        ConsentPayloadValidationResult initiationValidationResult =
                validatePaymentInitiationPayload(initiation);
        if (!(boolean) initiationValidationResult.isValid()) {
            return initiationValidationResult;
        }

        return new ConsentPayloadValidationResult(true);
    }

    /**
     * Check whether valid Data object is provided.
     *
     * @param requestBody Data object in initiation payload
     * @return whether the Data object is valid
     */
    public static ConsentPayloadValidationResult validateDataObjInRequestBody(JSONObject requestBody) {

        if (!requestBody.containsKey("Data") || !(requestBody.get("Data")
                instanceof JSONObject) || ((JSONObject) requestBody.get("Data")).isEmpty()) {
            log.error("Invalid request payload");
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(), "Invalid request payload");
        }

        return new ConsentPayloadValidationResult(true);
    }

    /**
     * Check whether valid Initiation object is provided.
     *
     * @param requestBody Initiation object in initiation payload
     * @return whether the Data object is valid
     */
    public static ConsentPayloadValidationResult validateInitiationObjInRequestBody(JSONObject requestBody) {

        if (!requestBody.containsKey("Initiation") || !(requestBody.get("Initiation")
                instanceof JSONObject) || ((JSONObject) requestBody.get("Initiation")).isEmpty()) {
            log.error("Invalid request payload");
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(), "Invalid request payload");
        }

        return new ConsentPayloadValidationResult(true);
    }

    /**
     * Method to validate payment initiation payload.
     *
     * @param initiation  Initiation Object of the request
     * @return JSONObject Validation Response
     */
    public static ConsentPayloadValidationResult validatePaymentInitiationPayload(JSONObject initiation) {

        //Validate DebtorAccount
        if (initiation.containsKey(ConsentExtensionConstants.DEBTOR_ACC)) {
            JSONObject debtorAccount = (JSONObject) initiation.get(ConsentExtensionConstants.DEBTOR_ACC);
            ConsentPayloadValidationResult validationResult = validateDebtorAccount(debtorAccount);
            if (!(boolean) validationResult.isValid()) {
                return validationResult;
            }
        }

        //Validate CreditorAccount
        if (initiation.containsKey(ConsentExtensionConstants.CREDITOR_ACC)) {
            JSONObject creditorAccount = (JSONObject) initiation.get(ConsentExtensionConstants.CREDITOR_ACC);
            ConsentPayloadValidationResult validationResult = validateCreditorAccount(creditorAccount);

            if (!(boolean) validationResult.isValid()) {
                return validationResult;
            }
        } else {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                    "Creditor account should be present in the request");
        }

        //Validate Local Instrument
        if (initiation.containsKey("LocalInstrument") && !ConsentManageUtils
                .validateLocalInstrument(initiation.getAsString("LocalInstrument"))) {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                    "Invalid Local Instrument value found");
        }

        JSONObject instructedAmount = (JSONObject) initiation.get(ConsentExtensionConstants.INSTRUCTED_AMOUNT);
        if (Double.parseDouble(instructedAmount.getAsString(ConsentExtensionConstants.AMOUNT)) < 1) {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                    "Invalid Instructed Amount value found");
        }

        if (!ConsentManageUtils.validateMaxInstructedAmount(
                instructedAmount.getAsString(ConsentExtensionConstants.AMOUNT))) {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                    "Instructed Amount value exceeds the maximum limit");
        }
        return new ConsentPayloadValidationResult(true);
    }

    /**
     * Method to validate debtor account.
     *
     * @param debtorAccount Debtor Account object
     * @return ConsentPayloadValidationResult Validation response
     */
    private static ConsentPayloadValidationResult validateDebtorAccount(JSONObject debtorAccount) {

        //Check Debtor Account Scheme name exists
        if (!debtorAccount.containsKey(ConsentExtensionConstants.SCHEME_NAME) ||
                StringUtils.isEmpty(debtorAccount.getAsString(ConsentExtensionConstants.SCHEME_NAME))) {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                    "Debtor Account Scheme Name should be present in the request");
        }

        //Validate Debtor Account Scheme name
        if (debtorAccount.containsKey(ConsentExtensionConstants.SCHEME_NAME) &&
                (debtorAccount.getAsString(ConsentExtensionConstants.SCHEME_NAME) == null ||
                        !ConsentManageUtils.isDebtorAccSchemeNameValid(debtorAccount
                                .getAsString(ConsentExtensionConstants.SCHEME_NAME)))) {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                    "Debtor Account Scheme Name is not in the correct format");
        }

        //Check Debtor Account Identification existing
        if (!debtorAccount.containsKey(ConsentExtensionConstants.IDENTIFICATION) ||
                StringUtils.isEmpty(debtorAccount.getAsString(ConsentExtensionConstants.IDENTIFICATION))) {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                    "Debtor Account Identification should be present in the request");
        }

        //Validate Debtor Account Identification
        if (debtorAccount.containsKey(ConsentExtensionConstants.IDENTIFICATION) &&
                (debtorAccount.getAsString(ConsentExtensionConstants.IDENTIFICATION) == null ||
                        !ConsentManageUtils.isDebtorAccIdentificationValid(debtorAccount
                                .getAsString(ConsentExtensionConstants.IDENTIFICATION)))) {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                    "Debtor Account Identification is not in the correct format");
        }

        //Validate Debtor Account Name
        if (debtorAccount.containsKey(ConsentExtensionConstants.NAME) &&
                (debtorAccount.getAsString(ConsentExtensionConstants.NAME) == null ||
                        !ConsentManageUtils.isDebtorAccNameValid(debtorAccount
                                .getAsString(ConsentExtensionConstants.NAME)))) {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                    "Debtor Account Name is not in the correct format");
        }

        //Validate Debtor Account Secondary Identification
        if (debtorAccount.containsKey(ConsentExtensionConstants.SECONDARY_IDENTIFICATION) &&
                (debtorAccount.getAsString(ConsentExtensionConstants.SECONDARY_IDENTIFICATION) == null ||
                        !ConsentManageUtils.isDebtorAccSecondaryIdentificationValid(debtorAccount
                                .getAsString(ConsentExtensionConstants.SECONDARY_IDENTIFICATION)))) {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                    "Debtor Account Secondary Identification is not in the correct format");
        }

        return new ConsentPayloadValidationResult(true);
    }

    /**
     * Method to validate debtor account.
     *
     * @param creditorAccount Creditor Account object
     * @return ConsentPayloadValidationResult Validation response
     */
    private static ConsentPayloadValidationResult validateCreditorAccount(JSONObject creditorAccount) {

        //Check Debtor Account Scheme name exists
        if (!creditorAccount.containsKey(ConsentExtensionConstants.SCHEME_NAME) ||
                StringUtils.isEmpty(creditorAccount.getAsString(ConsentExtensionConstants.SCHEME_NAME))) {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                    "Creditor Account Scheme Name should be present in the request");
        }

        //Validate Debtor Account Scheme name
        if (creditorAccount.containsKey(ConsentExtensionConstants.SCHEME_NAME) &&
                (creditorAccount.getAsString(ConsentExtensionConstants.SCHEME_NAME) == null ||
                        !ConsentManageUtils.isDebtorAccSchemeNameValid(creditorAccount
                                .getAsString(ConsentExtensionConstants.SCHEME_NAME)))) {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                    "Creditor Account Scheme Name is not in the correct format");
        }

        //Check Debtor Account Identification existing
        if (!creditorAccount.containsKey(ConsentExtensionConstants.IDENTIFICATION) ||
                StringUtils.isEmpty(creditorAccount.getAsString(ConsentExtensionConstants.IDENTIFICATION))) {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                    "Creditor Account Identification should be present in the request");
        }

        //Validate Debtor Account Identification
        if (creditorAccount.containsKey(ConsentExtensionConstants.IDENTIFICATION) &&
                (creditorAccount.getAsString(ConsentExtensionConstants.IDENTIFICATION) == null ||
                        !ConsentManageUtils.isDebtorAccIdentificationValid(creditorAccount
                                .getAsString(ConsentExtensionConstants.IDENTIFICATION)))) {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                    "Creditor Account Identification is not in the correct format");
        }

        //Validate Debtor Account Name
        if (creditorAccount.containsKey(ConsentExtensionConstants.NAME) &&
                (creditorAccount.getAsString(ConsentExtensionConstants.NAME) == null ||
                        !ConsentManageUtils.isDebtorAccNameValid(creditorAccount
                                .getAsString(ConsentExtensionConstants.NAME)))) {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                    "Creditor Account Name is not in the correct format");
        }

        //Validate Debtor Account Secondary Identification
        if (creditorAccount.containsKey(ConsentExtensionConstants.SECONDARY_IDENTIFICATION) &&
                (creditorAccount.getAsString(ConsentExtensionConstants.SECONDARY_IDENTIFICATION) == null ||
                        !ConsentManageUtils.isDebtorAccSecondaryIdentificationValid(creditorAccount
                                .getAsString(ConsentExtensionConstants.SECONDARY_IDENTIFICATION)))) {
            return new ConsentPayloadValidationResult(false, ResponseStatus.BAD_REQUEST,
                    ResponseStatus.BAD_REQUEST.getReasonPhrase(),
                    "Creditor Account Secondary Identification is not in the correct format");
        }

        return new ConsentPayloadValidationResult(true);
    }
}
