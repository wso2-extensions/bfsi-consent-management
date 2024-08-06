package org.wso2.bfsi.consent.management.extensions.util;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TestConstants {

    public static final OffsetDateTime EXP_DATE = OffsetDateTime.now().plusDays(50);
    public static final OffsetDateTime FIRST_PAYMENT_DATE = OffsetDateTime.now().plusDays(5);
    public static final OffsetDateTime FINAL_PAYMENT_DATE = OffsetDateTime.now().plusDays(10);

    public static final String SAMPLE_CONSENT_RECEIPT = "{\"validUntil\": \"2020-10-20\", \"frequencyPerDay\": 1," +
            " \"recurringIndicator\": false, \"combinedServiceIndicator\": true}";
    public static final String SAMPLE_CONSENT_TYPE = "accounts";
    public static final int SAMPLE_CONSENT_FREQUENCY = 1;
    public static final Long SAMPLE_CONSENT_VALIDITY_PERIOD = 1638337852L;
    public static final String SAMPLE_CURRENT_STATUS = "Authorised";
    public static final boolean SAMPLE_RECURRING_INDICATOR = true;
    public static final String SAMPLE_CLIENT_ID = "sampleClientID";
    public static final String SAMPLE_CONSENT_ID = "464ef174-9877-4c71-940c-93d6e069eaf9";
    public static final String SAMPLE_AUTH_ID = "88888";
    public static final String SAMPLE_AUTH_TYPE = "authorizationType";
    public static final String SAMPLE_USER_ID = "admin@wso2.com";
    public static final String SAMPLE_AUTHORIZATION_STATUS = "awaitingAuthorisation";
    public static final String SAMPLE_MAPPING_ID = "sampleMappingId";
    public static final String SAMPLE_MAPPING_ID_2 = "sampleMappingId2";
    public static final String SAMPLE_ACCOUNT_ID = "123456789";
    public static final String SAMPLE_MAPPING_STATUS = "active";
    public static final String SAMPLE_NEW_MAPPING_STATUS = "inactive";
    public static final String SAMPLE_PERMISSION = "samplePermission";
    public static final String AUTHORISED_STATUS = "Authorised";
    public static final String ACCOUNTS = "accounts";
    public static final String PAYMENTS = "payments";
    public static final String FUNDS_CONFIRMATIONS = "fundsconfirmations";
    public static final String COF_PATH = "/funds-confirmations";
    public static final Map<String, String> SAMPLE_CONSENT_ATTRIBUTES_MAP = new HashMap<String, String>() {
        {
            put("x-request-id", UUID.randomUUID().toString());
            put("idempotency-key", UUID.randomUUID().toString());
            put("sampleAttributeKey", "sampleAttributeValue");

        }
    };
    public static final String VALID_INITIATION = "{" +
            "   \"Data\": {" +
            "       \"Permissions\": [" +
            "           \"ReadAccountsDetail\"," +
            "           \"ReadBalances\"," +
            "           \"ReadBeneficiariesDetail\"," +
            "           \"ReadDirectDebits\"," +
            "           \"ReadAccountsBasic\"" +
            "       ]," +
            "       \"ExpirationDateTime\": \"" + EXP_DATE + "\"," +
            "       \"TransactionFromDateTime\": \"2021-05-03T00:00:00+00:00\"," +
            "       \"TransactionToDateTime\": \"2021-12-03T00:00:00+00:00\"" +
            "   }," +
            "   \"Risk\": {}" +
            "}";

    public static final String ACC_INITIATION_WITH_LIMITED_PERMISSIONS = "{" +
            "   \"Data\": {" +
            "       \"Permissions\": [" +
            "           \"ReadProducts\"," +
            "           \"ReadStandingOrdersDetail\"," +
            "           \"ReadTransactionsCredits\"," +
            "           \"ReadTransactionsDebits\"," +
            "           \"ReadTransactionsDetail\"," +
            "           \"ReadOffers\"," +
            "           \"ReadPAN\"," +
            "           \"ReadParty\"," +
            "           \"ReadPartyPSU\"," +
            "           \"ReadScheduledPaymentsDetail\"," +
            "           \"ReadStatementsDetail\"" +
            "       ]," +
            "       \"ExpirationDateTime\": \"" + EXP_DATE + "\"," +
            "       \"TransactionFromDateTime\": \"2021-05-03T00:00:00+00:00\"," +
            "       \"TransactionToDateTime\": \"2021-12-03T00:00:00+00:00\"" +
            "   }," +
            "   \"Risk\": {}" +
            "}";

    public static final String ACC_INITIATION_EXPIRED = "{" +
            "   \"Data\": {" +
            "       \"Permissions\": [" +
            "           \"ReadAccountsDetail\"," +
            "           \"ReadBalances\"," +
            "           \"ReadBeneficiariesDetail\"," +
            "           \"ReadDirectDebits\"," +
            "           \"ReadAccountsBasic\"" +
            "       ]," +
            "       \"ExpirationDateTime\": \"2021-05-03T00:00:00+00:00\"," +
            "       \"TransactionFromDateTime\": \"2021-05-03T00:00:00+00:00\"," +
            "       \"TransactionToDateTime\": \"2021-12-03T00:00:00+00:00\"" +
            "   }," +
            "   \"Risk\": {}" +
            "}";

    public static final String COF_RECEIPT = "{" +
            "   \"Data\": {" +
            "       \"DebtorAccount\": {" +
            "           \"SchemeName\": \"OB.IBAN\"," +
            "            \"Identification\": \"GB76LOYD30949301273801\"," +
            "            \"Name\": \"Andrea Smith\"," +
            "            \"SecondaryIdentification\": \"Roll 56988\"" +
            "        }," +
            "       \"ExpirationDateTime\": \"" + EXP_DATE + "\"" +
            "   }" +
            "}";

    public static final String COF_RECEIPT_EXPIRED = "{" +
            "   \"Data\": {" +
            "       \"DebtorAccount\": {" +
            "           \"SchemeName\": \"OB.IBAN\"," +
            "            \"Identification\": \"GB76LOYD30949301273801\"," +
            "            \"Name\": \"Andrea Smith\"," +
            "            \"SecondaryIdentification\": \"Roll 56988\"" +
            "        }," +
            "       \"ExpirationDateTime\": \"2021-05-03T00:00:00+00:00\"," +
            "   }" +
            "}";

    public static final String COF_SUBMISSION = "{" +
            "   \"Data\": {" +
            "       \"ConsentId\": \"" + SAMPLE_CONSENT_ID + "\"," +
            "       \"DebtorAccount\": {" +
            "           \"SchemeName\": \"OB.IBAN\"," +
            "            \"Identification\": \"GB76LOYD30949301273801\"," +
            "            \"Name\": \"Andrea Smith\"," +
            "            \"SecondaryIdentification\": \"Roll 56988\"" +
            "        }," +
            "       \"ExpirationDateTime\": \"" + EXP_DATE + "\"" +
            "   }" +
            "}";

    public static final String PAYMENT_INITIATION = "{\n" +
            "   \"Data\":{\n" +
            "      \"ReadRefundAccount\":\"Yes\",\n" +
            "      \"Permission\":\"Create\",\n" +
            "      \"Initiation\":{\n" +
            "         \"Frequency\":\"EvryDay\",\n" +
            "         \"Reference\":\"Pocket money for Damien\",\n" +
            "         \"NumberOfPayments\":\"10\",\n" +
            "         \"Purpose\":\"1234\",\n" +
            "         \"ChargeBearer\":\"BorneByCreditor\",\n" +
            "         \"FirstPaymentDateTime\":\"" + FIRST_PAYMENT_DATE + "\"," +
            "         \"FinalPaymentDateTime\":\"" + FINAL_PAYMENT_DATE + "\"," +
            "         \"DebtorAccount\":{\n" +
            "            \"SchemeName\":\"OB.SortCodeAccountNumber\",\n" +
            "            \"Identification\":\"30080012343456\",\n" +
            "            \"Name\":\"Andrea Smith\",\n" +
            "            \"SecondaryIdentification\":\"30080012343456\"\n" +
            "         },\n" +
            "         \"CreditorAccount\":{\n" +
            "            \"SchemeName\":\"OB.SortCodeAccountNumber\",\n" +
            "            \"Identification\":\"08080021325698\",\n" +
            "            \"Name\":\"ACME Inc\",\n" +
            "            \"SecondaryIdentification\":\"0002\"\n" +
            "         },\n" +
            "         \"InstructedAmount\":{\n" +
            "            \"Amount\":\"30.80\",\n" +
            "            \"Currency\":\"GBP\"\n" +
            "         }\n" +
            "      }\n" +
            "   },\n" +
            "   \"Risk\":{\n" +
            "      \"PaymentContextCode\":\"EcommerceGoods\"\n" +
            "   }\n" +
            "}";

    public static final String PAYMENT_SUBMISSION = "{\n" +
            "   \"Data\":{\n" +
            "      \"ReadRefundAccount\":\"Yes\",\n" +
            "      \"Permission\":\"Create\",\n" +
            "      \"ConsentId\": \"" + SAMPLE_CONSENT_ID + "\"," +
            "      \"Initiation\":{\n" +
            "         \"Frequency\":\"EvryDay\",\n" +
            "         \"Reference\":\"Pocket money for Damien\",\n" +
            "         \"NumberOfPayments\":\"10\",\n" +
            "         \"Purpose\":\"1234\",\n" +
            "         \"ChargeBearer\":\"BorneByCreditor\",\n" +
            "         \"FirstPaymentDateTime\":\"" + FIRST_PAYMENT_DATE + "\"," +
            "         \"FinalPaymentDateTime\":\"" + FINAL_PAYMENT_DATE + "\"," +
            "         \"DebtorAccount\":{\n" +
            "            \"SchemeName\":\"OB.SortCodeAccountNumber\",\n" +
            "            \"Identification\":\"30080012343456\",\n" +
            "            \"Name\":\"Andrea Smith\",\n" +
            "            \"SecondaryIdentification\":\"30080012343456\"\n" +
            "         },\n" +
            "         \"CreditorAccount\":{\n" +
            "            \"SchemeName\":\"OB.SortCodeAccountNumber\",\n" +
            "            \"Identification\":\"08080021325698\",\n" +
            "            \"Name\":\"ACME Inc\",\n" +
            "            \"SecondaryIdentification\":\"0002\"\n" +
            "         },\n" +
            "         \"InstructedAmount\":{\n" +
            "            \"Amount\":\"30.80\",\n" +
            "            \"Currency\":\"GBP\"\n" +
            "         }\n" +
            "      }\n" +
            "   },\n" +
            "   \"Risk\":{\n" +
            "      \"PaymentContextCode\":\"EcommerceGoods\"\n" +
            "   }\n" +
            "}";

    public static final String PAYMENT_SUBMISSION_WITHOUT_DATA = "{\n" +
            "   \"Risk\":{\n" +
            "      \"PaymentContextCode\":\"EcommerceGoods\"\n" +
            "   }\n" +
            "}";

    public static final String PAYMENT_SUBMISSION_WITHOUT_INITIATION = "{\n" +
            "   \"Data\":{\n" +
            "      \"ReadRefundAccount\":\"Yes\",\n" +
            "      \"Permission\":\"Create\",\n" +
            "      \"ConsentId\": \"" + SAMPLE_CONSENT_ID + "\"" +
            "   },\n" +
            "   \"Risk\":{\n" +
            "      \"PaymentContextCode\":\"EcommerceGoods\"\n" +
            "   }\n" +
            "}";


    public static final String PAYMENT_SUBMISSION_WITH_DIFFERENT_INITIATION = "{\n" +
            "   \"Data\":{\n" +
            "      \"ReadRefundAccount\":\"Yes\",\n" +
            "      \"Permission\":\"Create\",\n" +
            "      \"ConsentId\": \"" + SAMPLE_CONSENT_ID + "\"," +
            "      \"Initiation\":{\n" +
            "         \"Frequency\":\"EvryDay\",\n" +
            "         \"Reference\":\"Pocket money for Damien\",\n" +
            "         \"NumberOfPayments\":\"10\",\n" +
            "         \"Purpose\":\"1234\",\n" +
            "         \"ChargeBearer\":\"BorneByCreditor\",\n" +
            "         \"FirstPaymentDateTime\":\"" + FIRST_PAYMENT_DATE + "\"," +
            "         \"FinalPaymentDateTime\":\"" + FINAL_PAYMENT_DATE + "\"," +
            "         \"InstructedAmount\":{\n" +
            "            \"Amount\":\"30.80\",\n" +
            "            \"Currency\":\"GBP\"\n" +
            "         }\n" +
            "      }\n" +
            "   },\n" +
            "   \"Risk\":{\n" +
            "      \"PaymentContextCode\":\"EcommerceGoods\"\n" +
            "   }\n" +
            "}";
}
