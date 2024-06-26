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

package org.wso2.bfsi.consent.management.extensions.common;

/**
 * Constant class for consent extension module.
 */
public class ConsentExtensionConstants {

    public static final String ERROR_URI_FRAGMENT = "#error=";
    public static final String ERROR_DESCRIPTION_PARAMETER = "&error_description=";
    public static final String STATE_PARAMETER = "&state=";
    public static final String UUID_REGEX =
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";
    public static final String IS_ERROR = "isError";
    public static final String ACCOUNTS = "accounts";
    public static final String PAYMENTS = "payments";
    public static final String FUNDS_CONFIRMATIONS = "fundsconfirmations";
    public static final String ACCOUNT_CONSENT_PATH = "account-access-consents";
    public static final String COF_CONSENT_PATH = "funds-confirmation-consents";
    public static final String PAYMENT_CONSENT_PATH = "payment-consents";
    public static final String CONSENT_DATA = "consentData";
    public static final String TITLE = "title";
    public static final String CONSENT_ID = "ConsentId";
    public static final String DEBTOR_ACCOUNT_ID = "AccountId";
    public static final String ACCOUNT_ID = "account_id";
    public static final String DATA_REQUESTED = "data_requested";
    public static final String DATA = "Data";
    public static final String ACCOUNT_DATA = "account_data";
    public static final String SELECTED_ACCOUNT = "selectedAccount";
    public static final String DISPLAY_NAME = "display_name";
    public static final String CONSENT_TYPE = "consent_type";
    public static final String AWAIT_AUTHORISE_STATUS = "AwaitingAuthorisation";
    public static final String AUTHORIZED_STATUS = "Authorised";
    public static final String REVOKED_STATUS = "Revoked";
    public static final String REJECTED_STATUS = "Rejected";
    public static final String CREATED_STATUS = "created";
    public static final String DEFAULT_AUTH_TYPE = "authorization";
    public static final String PERMISSIONS = "Permissions";
    public static final String EXPIRATION_DATE = "ExpirationDateTime";
    public static final String EXPIRATION_DATE_TITLE = "Expiration Date Time";
    public static final String TRANSACTION_FROM_DATE = "TransactionFromDateTime";
    public static final String TRANSACTION_FROM_DATE_TITLE = "Transaction From Date Time";
    public static final String TRANSACTION_TO_DATE = "TransactionToDateTime";
    public static final String TRANSACTION_TO_DATE_TITLE = "Transaction To Date Time";
    public static final String INITIATION = "Initiation";
    public static final String PAYMENT_TYPE_TITLE = "Payment Type";
    public static final String CURRENCY_OF_TRANSFER = "CurrencyOfTransfer";
    public static final String CURRENCY_OF_TRANSFER_TITLE = "Currency of Transfer";
    public static final String INTERNATIONAL_PAYMENTS = "International Payments";
    public static final String DOMESTIC_PAYMENTS = "Domestic Payments";
    public static final String END_TO_END_IDENTIFICATION = "EndToEndIdentification";
    public static final String END_TO_END_IDENTIFICATION_TITLE = "End to End Identification";
    public static final String INSTRUCTION_IDENTIFICATION = "InstructionIdentification";
    public static final String INSTRUCTION_IDENTIFICATION_TITLE = "Instruction Identification";
    public static final String DEBTOR_ACC = "DebtorAccount";
    public static final String DEBTOR_ACC_TITLE = "Debtor Account";
    public static final String CREDITOR_ACC = "CreditorAccount";
    public static final String CREDITOR_ACC_TITLE = "Creditor Account";
    public static final String SCHEME_NAME = "SchemeName";
    public static final String SCHEME_NAME_TITLE = "Scheme Name";
    public static final String IDENTIFICATION = "Identification";
    public static final String IDENTIFICATION_TITLE = "Identification";
    public static final String NAME = "Name";
    public static final String NAME_TITLE = "Name";
    public static final String SECONDARY_IDENTIFICATION = "SecondaryIdentification";
    public static final String SECONDARY_IDENTIFICATION_TITLE = "Secondary Identification";
    public static final String OPEN_ENDED_AUTHORIZATION = "Open Ended Authorization Requested";
    public static final String INSTRUCTED_AMOUNT = "InstructedAmount";

    public static final String INSTRUCTED_AMOUNT_TITLE = "Instructed Amount";
    public static final String CURRENCY = "Currency";
    public static final String CURRENCY_TITLE = "Currency";
    public static final String AMOUNT = "Amount";
    public static final String AMOUNT_TITLE = "Amount";
    public static final String PAYMENT_ACCOUNT = "paymentAccount";
    public static final String COF_ACCOUNT = "cofAccount";
    public static final String PRIMARY = "primary";
    public static final String ACCOUNT_IDS = "accountIds";
    public static final String ERROR = "error";
    public static final String ERROR_DESCRIPTION = "error_description";
    public static final String STATE = "state";
    public static final String REDIRECT_URI = "redirect_uri";
    public static final String CLAIMS = "claims";
    public static final String USER_INFO = "userinfo";
    public static final String ID_TOKEN = "id_token";
    public static final String OB_INTENT_ID = "openbanking_intent_id";
    public static final String VALUE = "value";
    public static final String CREATION_DATE_TIME = "CreationDateTime";
    public static final String STTAUS_UPDATE_DATE_TIME = "StatusUpdateDateTime";
    public static final String STATUS = "Status";
}
