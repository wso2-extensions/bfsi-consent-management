# BFSI Consent Management Connector
Consent management is the process of prompting, collecting, and managing bank customer’s consent before an API consumer collects or shares the customer's financial information. The connector includes a fully-featured consent management module that: 
 - securely exposes consent data through an API
 - provides in-built consent management user interfaces for customers and bank staff 
 - manages the entire consent life cycle

A consent goes through a phased life cycle as follows:

![Lifecycle of a consent](images/consent-lifecycle.png)

- **Consent provision**: An API consumer application sends a consent request to the bank containing the customer’s financial information that it wants to access.
- **Consent grant**: The bank redirects the consent request to the customer to approve/deny.
- **Consent verification**: The bank verifies if the customer has approved the API consumer application to access the information. If the bank customer has denied the consent, the bank must detect and stop the application from invoking the banking APIs.
- **Consent revocation**: A customer can revoke the consent via consent management applications. It can either be done by the customer themselves or by a bank representative upon the customer’s request.
- **Consent expiration**: When the consent validity period expires, the bank sets the consent status as expired. For the API consumer application to access the customer’s financial information again, the customer needs to regrant the consent.

BFSI Consent Management Connector provides the capability to manage the above phase of the consent lifecycle. To learn more about BFSI Consent Management Connector, click [here]()

## Setting up the BFSI Consent Management Connector

### Deploying Artifacts

Download the connector zip file and extract it. Copy the artifacts downloaded to the folders specified below. 

1. Copy the following artifacts to <IS_HOME>/repository/components/dropins folder.
    - org.wso2.bfsi.consent.management.common-1.0.0.jar
    - org.wso2.bfsi.consent.management.dao-1.0.0.jar
    - org.wso2.bfsi.consent.management.service-1.0.0.jar
    - org.wso2.bfsi.consent.management.extensions-1.0.0.jar
    - org.wso2.bfsi.identity.extensions-1.0.0.jar
    - classmate-1.5.1.jar
2. Copy the following artifacts to <IS_HOME>/repository/components/libs folder.
    - commons-beanutils-1.9.4.jar
    - validation-api-2.0.1.Final.jar
    - hibernate-validator-6.0.20.Final.jar
3. Copy the following artifacts to <IS_HOME>/repository/deployment/server/webapps folder.
    - api#bfsi#consent.war
    - bfsi#authenticationendpoint.war
4. Copy the following configuration file to <IS_HOME>/repository/conf folder.
    - bfsi-consent-management.xml

### Setting up a new database

1. Create a new database with the name - `bfsi_consentdb`.
2. Create database tables by running the relevant the db script inside from the `dbscripts` folder.
   
   | DBMS Type                 | DB Script                                                            |
   |---------------------------|----------------------------------------------------------------------|
   | MySQL 8.0                 | <a href="../resources/mysql.sql" download> mysql.sql  </a>           |
   | Oracle 19c                | <a href="../resources/oracle.sql" download> oracle.sql  </a>         |
   | Microsoft SQL Server 2017 | <a href="../resources/mssql.sql" download> mssql.sql  </a>           |
   | PostgreSQL 13             | <a href="../resources/postgresql.sql" download> postgresql.sql  </a> |

3. According to your DBMS, place the compatible JDBC drivers in the following directories:
 
       - `<IS_HOME>/repository/components/lib` 

   !!! tip 
   The supported JDBC driver versions are as follows:
        
       | DBMS version | JDBC driver version |
       |--------------|---------------------|
       | MySQL 8.0 | `mysql-connector-java-5.1.44.jar` |
       | Oracle 19c | `ojdbc10.jar` |
       | Microsoft SQL Server 2017 | `sqljdbc41.jar` |
       | PostgreSQL 13 | `postgresql-42.2.17.jar` |

### Configuring BFSI Consent Management Connector

1. Add the root and issuer of the client certificate to the client trustore.
``` bash
keytool -import -alias <alias_name> -file <localtion_to-the-root-certificate> -keystore <IS_HOME>/repository/resources/security/client-truststore.jks -storepass wso2carbon
```
``` bash
keytool -import -alias <alias_name> -file <localtion_to-the-issuer-certificate> -keystore <IS_HOME>/repository/resources/security/client-truststore.jks -storepass wso2carbon
```

2. Add the following configurations to the deployment toml file inside the <IS_HOME>/repository/conf folder.

- Add the following data source configuration

``` toml tab="MySQL"
[[datasource]]
id="WSO2BFSI_DB"
url = "jdbc:mysql://<DB_HOST>:<DB_PORT>/bfsi_consentdb?autoReconnect=true&amp;useSSL=false"
username = "<DB_USERNAME>"
password = "<DB_PASSOWRD>"
driver = "com.mysql.jdbc.Driver"
jmx_enable=false
pool_options.maxActive = "150"
pool_options.maxWait = "60000"
pool_options.minIdle = "5"
pool_options.testOnBorrow = true
pool_options.validationQuery="SELECT 1"
pool_options.validationInterval="30000"
pool_options.defaultAutoCommit=false
```

``` toml tab="Oracle"
[[datasource]]
id="WSO2BFSI_DB"
url = "jdbc:oracle:thin:bfsi_consentdb/password@<DB_HOST>:<DB_PORT>:<DB_USERNAME>"
username = "<DB_USERNAME>"
password = "<DB_PASSOWRD>"
driver = "oracle.jdbc.driver.OracleDriver"
jmx_enable=false
pool_options.maxActive = "150"
pool_options.maxWait = "60000"
pool_options.minIdle = "5"
pool_options.testOnBorrow = true
validationQuery="SELECT 1 FROM DUAL"
pool_options.validationInterval="30000"
pool_options.defaultAutoCommit=false
```

``` toml tab="MS SQL"
[[datasource]]
id="WSO2BFSI_DB"
url = "jdbc:sqlserver://<DB_HOST>:<DB_PORT>;databaseName=bfsi_consentdb"
username = "<DB_USERNAME>"
password = "<DB_PASSOWRD>"
driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver"
jmx_enable=false
pool_options.maxActive = "300"
pool_options.maxWait = "60000"
pool_options.minIdle = "5"
pool_options.testOnBorrow = true
pool_options.validationQuery="SELECT 1"
pool_options.validationInterval="30000"
pool_options.defaultAutoCommit=false
```

``` toml tab="PostgreSQL"
[[datasource]]
id="WSO2BFSI_DB"
url = "jdbc:postgresql://<DB_HOST>:<DB_PORT>/bfsi_consentdb"
username = "<DB_USERNAME>"
password = "<DB_PASSOWRD>"
driver = "org.postgresql.Driver"
jmx_enable=false
pool_options.maxActive = "150"
pool_options.maxWait = "60000"
pool_options.minIdle = "5"
pool_options.testOnBorrow = true
pool_options.validationQuery="SELECT 1"
pool_options.validationInterval="30000"
pool_options.defaultAutoCommit=false
```

- Add following resource access control configurations for the `consent` and `consentmgr` resources.

``` toml 
[[resource.access_control]]
context = "(.*)/api/bfsi/consent/(.*)"
secure="true"
http_method="all"
permissions=["/permission/admin"]
allowed_auth_handlers = ["BasicAuthentication"]

[[resource.access_control]]
context = "(.*)/consentmgr(.*)"
secure="false"
http_method="GET,DELETE"
```

- Add the following configurations to allow unregistered scopes.
``` toml 
[oauth]
drop_unregistered_scopes = false
```

- Config the Consent page in the authentication endpoint.
``` toml 
[oauth.endpoints]
oauth2_consent_page = "${carbon.protocol}://localhost:${carbon.management.port}/bfsi/authenticationendpoint/oauth2_authz.do"
oidc_consent_page = "${carbon.protocol}://localhost:${carbon.management.port}/bfsi/authenticationendpoint/oauth2_consent.do"
```

- Configuring authorization flow and token flow extensions.
``` toml 
[oauth.response_type]
token.enable = true
code.enable = false
id_token.enable = true
id_token_token.enable = false
device.enable = true

[[oauth.custom_response_type]]
name = "code"
class = "org.wso2.bfsi.identity.extensions.auth.extensions.response.handler.BFSICodeResponseTypeHandlerExtension"
validator = "org.wso2.bfsi.identity.extensions.auth.extensions.response.validator.BFSICodeResponseTypeValidator"

[[oauth.custom_response_type]]
name = "code id_token"
class = "org.wso2.bfsi.identity.extensions.auth.extensions.response.handler.BFSIHybridResponseTypeHandlerExtension"
validator = "org.wso2.bfsi.identity.extensions.auth.extensions.response.validator.BFSIHybridResponseTypeValidator"

[oauth.grant_type.refresh_token]
enable=true
grant_handler="org.wso2.bfsi.identity.extensions.grant.type.handlers.BFSIRefreshGrantHandler"

[oauth.grant_type.authorization_code]
enable=true
grant_handler="org.wso2.bfsi.identity.extensions.grant.type.handlers.BFSIAuthorizationCodeGrantHandler"

[oauth.grant_type.password]
enable=true
grant_handler="org.wso2.bfsi.identity.extensions.grant.type.handlers.BFSIPasswordGrantHandler"

[oauth.grant_type.client_credentials]
enable=true
grant_handler="org.wso2.bfsi.identity.extensions.grant.type.handlers.BFSIClientCredentialsGrantHandler"

[oauth.oidc.extensions]
request_object_validator = "org.wso2.bfsi.identity.extensions.auth.extensions.request.validator.BFSIRequestObjectValidationExtension"
claim_callback_handler="org.wso2.bfsi.identity.extensions.claims.BFSIDefaultOIDCClaimsCallbackHandler"
```

- Config the client certificate header name as follows.
``` toml 
[oauth.mutualtls]
client_certificate_header = "x-wso2-mutual-auth-cert"
```

## Tryout BFSI Consent Management Connector

### Initiate a consent
In this step, the API consumer creates a request to get the consent of the customer to access the accounts and its information from the bank.
A sample consent initiation request looks as follows.

``` curl
curl --location 'https://<IS_HOST>:<IS_PORT>/api/bfsi/consent/manage/account-access-consents' \
--header 'x-fapi-interaction-id: 93bac548-d2de-4546-b106-880a5018460d' \
--header 'x-wso2-client-id: <CLIENT_ID>' \
--header 'Content-Type: application/json' \
--header 'Authorization: Basic <Base 64 encoded admin credentials>' \
--cert <TRANSPORT_PUBLIC_KEY_FILE_PATH> --key <TRANSPORT_PRIVATE_KEY_FILE_PATH> \
--data '{
    "Data": {
        "Permissions": [
            "ReadAccountsDetail",
            "ReadBalances",
            "ReadBeneficiariesDetail",
            "ReadDirectDebits",
            "ReadProducts",
            "ReadStandingOrdersDetail",
            "ReadTransactionsCredits",
            "ReadTransactionsDebits",
            "ReadTransactionsDetail",
            "ReadOffers",
            "ReadPAN",
            "ReadParty",
            "ReadPartyPSU",
            "ReadScheduledPaymentsDetail",
        "ReadStatementsDetail"
        ],
        "ExpirationDateTime": "2022-05-02T00:00:00+00:00",
        "TransactionFromDateTime": "2021-05-03T00:00:00+00:00",
        "TransactionToDateTime": "2021-12-03T00:00:00+00:00"
    },
    "Risk": {}
}'
```

The response contains a Consent ID. A sample response looks as follows:

``` json
{
    "Data": {
        "StatusUpdateDateTime": "2024-05-21T16:48:52+05:30",
        "Status": "AwaitingAuthorization",
        "CreationDateTime": "2024-05-21T16:48:52+05:30",
        "ConsentId": "ece8e376-44bb-4b42-81c6-93f8afcd5371",
        "TransactionFromDateTime": "2021-05-03T00:00:00+00:00",
        "TransactionToDateTime": "2021-12-03T00:00:00+00:00",
        "ExpirationDateTime": "2022-05-02T00:00:00+00:00",
        "Permissions": [
            "ReadAccountsDetail",
            "ReadBalances",
            "ReadBeneficiariesDetail",
            "ReadDirectDebits",
            "ReadProducts",
            "ReadStandingOrdersDetail",
            "ReadTransactionsCredits",
            "ReadTransactionsDebits",
            "ReadTransactionsDetail",
            "ReadOffers",
            "ReadPAN",
            "ReadParty",
            "ReadPartyPSU",
            "ReadScheduledPaymentsDetail",
            "ReadStatementsDetail"
        ]
    },
    "Risk": {}
}
```

### Authorizing a consent
The API consumer application redirects the bank customer to authenticate and approve/deny application-provided consents.

1. Generate a request object by signing your JSON payload using the supported algorithms. Sample format is as belows.

``` json
{
    "kid": "<The KID value of the signing jwk set>",
    "alg": "<SUPPORTED_ALGORITHM>",
    "typ": "JWT"
}
{
    "max_age": 86400,
    "aud": "<This is the audience that the ID token is intended for. Example, https://<IS_HOST>:9443/oauth2/token>",
    "scope": "accounts openid",
    "iss": "<CLIENT_ID>",
    "claims": {
        "id_token": {
            "acr": {
                "values": [
                    "urn:openbanking:psd2:sca",
                    "urn:openbanking:psd2:ca"
                ],
                "essential": true
            },
            "openbanking_intent_id": {
                "value": "<CONSENT_ID>",
                "essential": true
            }
        },
        "userinfo": {
            "openbanking_intent_id": {
                "value": "<CONSENT_ID>",
                "essential": true
            }
        }
    },
    "response_type": "code id_token",  
    "redirect_uri": "<CLIENT_APPLICATION_REDIRECT_URI>",
    "state": "YWlzcDozMTQ2",
    "exp": <The expiration time of the request object in Epoch format>,
    "nbf": <Time before which the JWT MUST NOT be accepted for processing>,
    "nonce": "<PREVENTS_REPLAY_ATTACKS>",
    "client_id": "<CLIENT_ID>"
}
```

2. The bank sends the request to the customer stating the accounts and information that the API consumer wishes to access. This request is in the format of a URL as follows:

``` json tab="Format"
https://<IS_HOST>:<IS_PORT>/oauth2/authorize?response_type=code%20id_token&client_id=<CLIENT_ID>&scope=accounts%20openid&redirect_uri=<APPLICATION_REDIRECT_URI>&state=YWlzcDozMTQ2&request=<REQUEST_OBJECT>&prompt=login&nonce=<REQUEST_OBJECT_NONCE>
```
``` json tab="Sample"
https://localhost:9443/oauth2/authorize?response_type=code id_token&client_id=djBbFDbdq4xuR1GawhZ1wzHnEV8a&redirect_uri=https://www.google.com&scope=openid accounts&nonce=n-0S6_WzA2M0000016&request=eyJhbGciOiJQUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IlRLSHNQSUlHQkFKb1NxRUVnQWZMV0szaUNNcyJ9.eyJtYXhfYWdlIjo4NjQwMCwiYXVkIjoiaHR0cHM6Ly9sb2NhbGhvc3Q6OTQ0My9vYXV0aDIvdG9rZW4iLCJzY29wZSI6ImFjY291bnRzIG9wZW5pZCIsImlzcyI6ImRqQmJGRGJkcTR4dVIxR2F3aFoxd3pIbkVWOGEiLCJjbGFpbXMiOnsiaWRfdG9rZW4iOnsiYWNyIjp7InZhbHVlcyI6WyJ1cm46b3BlbmJhbmtpbmc6cHNkMjpzY2EiLCJ1cm46b3BlbmJhbmtpbmc6cHNkMjpjYSJdLCJlc3NlbnRpYWwiOnRydWV9LCJvcGVuYmFua2luZ19pbnRlbnRfaWQiOnsidmFsdWUiOiI1OTUzZWFiMS03ZTJiLTQ5MmMtOGJjMi1kYThjZDZhZWQyZDQiLCJlc3NlbnRpYWwiOnRydWV9fSwidXNlcmluZm8iOnsib3BlbmJhbmtpbmdfaW50ZW50X2lkIjp7InZhbHVlIjoiNTk1M2VhYjEtN2UyYi00OTJjLThiYzItZGE4Y2Q2YWVkMmQ0IiwiZXNzZW50aWFsIjp0cnVlfX19LCJyZXNwb25zZV90eXBlIjoiY29kZSBpZF90b2tlbiIsInJlZGlyZWN0X3VyaSI6Imh0dHBzOi8vd3d3Lmdvb2dsZS5jb20iLCJleHAiOjE3MTYyODUyNjQsIm5iZiI6MTcxNjI4Mjg2NSwibm9uY2UiOiJuLTBTNl9XekEyTTAwMDAwMTYiLCJjbGllbnRfaWQiOiJkakJiRkRiZHE0eHVSMUdhd2haMXd6SG5FVjhhIn0.AC2N1-4p7C37p9Iakcp5_bkAVvui_QcH9CAMx4eYJW--wAT6K9F9wzScFc14FpM817eWEa3H8L-OJqAMXNHxRKwuA8u3luCDvQb-TKayeBZ9v68gbdAiwnaKlisMgMzrW6_oQYiIT3XdqvWvjXAxdkzbwVccCKyMiD5gY2YiCNYj6Cw9H-mR8h5taaHj929UsN2qkK2U4WSvi8TmlnEk3T40zuvtbOHyHPA5mGZcmdihLDwjwM4G4mE9kyQBjtqji1_mpuWOu6ZhCo1J9vbjFW9vcXGTe-0mCQLH0esBvhNro4FD5-DRVd9L2oYEY-1n4ttb7rMJL6x6OZxw6FbDiA
```

3. Run the URL in a browser to prompt the invocation of the authorize API.
4. Upon successful authentication, the user is redirected to the consent authorize page. Use the login credentials of a user that has a `subscriber` role.
5. Data requested by the consent such as permissions, transaction period, and expiration date are displayed. Click Confirm to grant these permissions.
6. Upon providing consent, an authorization code is generated on the web page of the redirect_uri. See the sample given below: 

The authorization code from the below URL is in the code parameter (code=e61579c3-fe9c-3dfe-9af2-0d2f03b95775).

```
https://wso2.com/#code=e61579c3-fe9c-3dfe-9af2-0d2f03b95775&id_token=eyJ4NXQiOiJNell4TW1Ga09HWXdNV0kwWldObU5EY3hOR1l3WW1NNFpUQTNNV0kyTkRBelpHUXpOR00wWkdSbE5qSmtPREZrWkRSaU9URmtNV0ZoTXpVMlpHVmxOZyIsImtpZCI6Ik16WXhNbUZrT0dZd01XSTBaV05tTkRjeE5a4f936c74e2ca7f4250208aa42.sk_04ejciXBj6DnpALyYaw
```

### Generating user access token

After authorizing the consent, an access token should be generated using the authorization code generated in the section above.

1. Generate the client assertion by signing the following JSON payload using supported algorithms.
``` json tab="Format"
{
    "alg": "<The algorithm used for signing.>",
    "kid": "<The thumbprint of the certificate.>",
    "typ": "JWT"
}

{
    "iss": "<This is the issuer of the token. For example, client ID of your application>",
    "sub": "<This is the subject identifier of the issuer. For example, client ID of your application>",
    "exp": "<This is the epoch time of the token expiration date/time>",
    "iat": "<This is the epoch time of the token issuance date/time>",
    "jti": "<This is an incremental unique value>",
    "aud": "<This is the audience that the ID token is intended for. For example, https://<IS_HOST>:9443/oauth2/token>"
}
<signature: For DCR, the client assertion is signed by the private key of the signing certificate. Otherwise, the private signature of the application certificate is used.>
```

``` json tab="Sample"
eyJraWQiOiJUS0hzUElJR0JBSm9TcUVFZ0FmTFdLM2lDTXMiLCJhbGciOiJQUzI1NiJ9.eyJzdWIiOiJkakJiRkRiZHE0eHVSMUdhd2haMXd6SG5FVjhhIiwiYXVkIjoiaHR0cHM6Ly9sb2NhbGhvc3Q6OTQ0My9vYXV0aDIvdG9rZW4iLCJpc3MiOiJkakJiRkRiZHE0eHVSMUdhd2haMXd6SG5FVjhhIiwiZXhwIjoxNzE2ODgxMDk2LCJpYXQiOjE3MTY4Nzc2NjcsImp0aSI6IjE2NDI3MzYzMjg1MjUifQ.Nh_Pi1rnCSpBzlTTXd-DwHutd9Gm7xJNFcsJ00-RjiHocT6JT6CKnaDAjt63tS0nmTH36pkNs7PG4fYXv-KpfjwTIqgxRNl8PwF3DNAOhOz8YlHqaeBbgLBTb4LN9EIZZyPfkGQUrPFoGtHsu_j6PIw9TetvihGtefGLGZxT4oDc736SADHrySpw-ZWP1AriYTaZzM8SmHePnpzifSEOUsnDLcI44EO_f-KOYb-P56MkCrVEUERqaQ3_Jqozf-TTFtndMIGV2yeiSbVYHLneL92AAGsEIm0VzQ7zHyX9YdGRu-7tok88qlDCCZSWD-0F7N6EcjQlT7hx3rUDRawq4g
```

2. Run the following cURL command in a command prompt to generate the access token. Update the placeholders with relevant values.

``` curl
curl --location 'https://<IS_HOST>:<IS_PORT>/oauth2/token' \
--header 'x-wso2-mutual-auth-cert: <certificate_added_to_SP>' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'client_assertion=<assertion_generated_above>' \
--data-urlencode 'client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer' \
--data-urlencode 'code=<Code_from_Authorizing a consent_step>' \
--data-urlencode 'redirect_uri=<Redirect_URI>' \
--data-urlencode 'scope=payments openid' \
--data-urlencode 'grant_type=authorization_code' \
--data-urlencode 'client_id=<Client_Id>'
```

3. Upon successful token generation, you can obtain a token as follows:

``` json
{
    "access_token":"866353bd-50dc-36d2-aeba-4d0c3f4f7bd6",
    "scope":"openid",
    "id_token":"eyJ4NXQiOiJPV1JpTXpaaVlURXhZVEl4WkdGa05UVTJOVE0zTWpkaFltTmxNVFZrTnpRMU56a3paVGc1TVRrNE0yWmxOMkZoWkdaalpURmlNemxsTTJJM1l6ZzJNZyIsImtpZCI6Ik9XUmlNelppWVRFeFlUSXhaR0ZrTlRVMk5UTTNNamRoWW1ObE1UVmtOelExTnprelpUZzVNVGs0TTJabE4yRmhaR1pqWlRGaU16bGxNMkkzWXpnMk1nX1JTMjU2IiwiYWxnIjoiUlMyNTYifQ.eyJpc2siOiJhYzVmZTNjZmZlMTkzYzUxYWI3MDU3ODIwODcyZGFhYmE5YzgwODE4MjJkOTU0MDYwMGQ3OTg2MjBmNjFmMDY2IiwiYXRfaGFzaCI6IkUxSjFySlhPNWxRRnE2MEVPa2ZjMWciLCJzdWIiOiJlNjBjYWQxYi1jOGYxLTQ2NzMtYmMxZC1mMjJlNjVlZGU1NzYiLCJhbXIiOlsiQmFzaWNBdXRoZW50aWNhdG9yIl0sImlzcyI6Imh0dHBzOlwvXC9sb2NhbGhvc3Q6OTQ0M1wvb2F1dGgyXC90b2tlbiIsIm5vbmNlIjoibi0wUzZfV3pBMk0wMDAwMDE2IiwiYXVkIjoiZGpCYkZEYmRxNHh1UjFHYXdoWjF3ekhuRVY4YSIsImNfaGFzaCI6ImRobzVVVktWRktNWkR5eHNvUHBNQWciLCJuYmYiOjE3MTY4Nzk4MTIsImF6cCI6ImRqQmJGRGJkcTR4dVIxR2F3aFoxd3pIbkVWOGEiLCJvcmdfaWQiOiIxMDA4NGE4ZC0xMTNmLTQyMTEtYTBkNS1lZmUzNmIwODIyMTEiLCJleHAiOjE3MTY4ODM0MTIsIm9yZ19uYW1lIjoiU3VwZXIiLCJpYXQiOjE3MTY4Nzk4MTIsImp0aSI6IjI4YmMwNzE0LWI3ZDQtNDIxNi1hOTBjLTNhOWQ2NjFlM2E5MCJ9.M2D4JqNiOoHJ_-qfv_GbsFY46vq2MKZidPDpbJ4eKwT9PTAhVEL2mzfCIipOwlTg1Q4Jx0MOzyHpsbUGv9ShIjU1Q6iL2NOroA6DudcIQm9S50SHRtuIwMyFlVEY3QNzFw3sWttYFhTLwor_5UqXMMYNvWmyxjRcvaEyfKv3HVIXae6NvAHeC1CPfSV931eLAoR6hPMmV72z97ZQk7SemzSssfIQPDl02n85Td-WhcnnOruVIpcTKlDdmw7ROedphKIQiS7dUY_Qaximjn3VjfwB7wtF62vm89OoUglfKAVXAszdCnapuo9ArvDy9yElpGh4ud4G8ImHGYD0-KBpxA",
    "token_type":"Bearer",
    "expires_in":3600
}
```

### Validate the consent

During the actual resource access, consent need to be validated against the pre-defined conditions. The access token generated in the previous step is used to validate the consent. 

1. Generate the JWT payload for the validate endpoint
    
``` json tab="Format"
{
    "alg": "PS256",
    "typ": "JWT"
}
{
    "headers": {
        "Authorization": "Bearer <USER_ACCESS_TOKEN>",
        "activityid": "83ca0d8c-32f2-401d-a7f8-87bad66458ba",
        "charset": "UTF-8",
        "Accept": "application/json",
        "x-fapi-financial-id": "open-bank",
        "Connection": "Keep-Alive",
        "User-Agent": "Apache-HttpClient/4.5.3 (Java/1.8.0_311)",
        "Host": "localhost:8243",
        "Accept-Encoding": "gzip,deflate",
        "Content-Length": "190",
        "Content-Type": "application/json; charset=UTF-8"
    },
    "consentId": "<CONSENT_ID>",
    "clientId": "<CLIENT_ID>",
    "resourceParams": {
        "resource": "/aisp/accounts",
        "context": "/open-banking/v3.1/aisp",
        "httpMethod": "POST"
    },
    "body": {
        "Data": {
            "Permissions": [
                "ReadAccountsDetail",
                "ReadBalances",
                "ReadBeneficiariesDetail",
                "ReadStatementsDetail"
            ]
        },
        "Risk": {}
    },
    "userId": "<USER_ID>",
    "electedResource": "/accounts"
}
```

``` json tab="Sample"
eyJhbGciOiJQUzI1NiIsInR5cCI6IkpXVCJ9.eyJoZWFkZXJzIjp7IkF1dGhvcml6YXRpb24iOiJCZWFyZXIgZXlKNE5YUWlPaUpPVkdSdFdtcE5ORnBFYXpOT2Fsa3dXWHBqTlUxdFdtMVBSR2QzVFZSRk0wMVhXWGRPUkVVMVRWZFNiRnBFWnpST2VtTTBXa0VpTENKcmFXUWlPaUpOZWxsNFRXMUdhMDlIV1hkTlYwa3dXbGRPYlU1RVkzaE9SMWwzV1cxTk5GcFVRVE5OVjBreVRrUkJlbHBIVVhwT1IwMHdXa2RTYkU1cVNtdFBSRVpyV2tSU2FVOVVSbXROVjBab1RYcFZNbHBIVm14T1oxOVNVekkxTmlJc0ltRnNaeUk2SWxKVE1qVTJJbjAuZXlKemRXSWlPaUpoWkcxcGJrQjNjMjh5TG1OdmJVQmpZWEppYjI0dWMzVndaWElpTENKaGRYUWlPaUpCVUZCTVNVTkJWRWxQVGw5VlUwVlNJaXdpWVhWa0lqb2lXR2xZVkd4dFZtbElNWEl6VmtSQlIwSmxYMGM0ZEZaMlZuTm5ZU0lzSW01aVppSTZNVFkwTWpBMU5UWXdPQ3dpWVhwd0lqb2lXR2xZVkd4dFZtbElNWEl6VmtSQlIwSmxYMGM0ZEZaMlZuTm5ZU0lzSW5OamIzQmxJam9pWm5WdVpITmpiMjVtYVhKdFlYUnBiMjV6SUc5d1pXNXBaQ0lzSW1semN5STZJbWgwZEhCek9sd3ZYQzlzYjJOaGJHaHZjM1E2T1RRME5sd3ZiMkYxZEdneVhDOTBiMnRsYmlJc0ltTnVaaUk2ZXlKNE5YUWpVekkxTmlJNkluWlpiMVZaVWxOUk4wTm5iMWw0VGsxWFYwOTZRemgxVG1aUmNtbHpOSEJZVVZnd1dtMXBkRko0ZW5NaWZTd2laWGh3SWpveE5qUXlNRFU1TWpBNExDSnBZWFFpT2pFMk5ESXdOVFUyTURnc0ltcDBhU0k2SW1KbVlXTTVOamMxTFdJNVpUZ3ROREV6TkMwNE1HUXhMVGcxTURGbE1XSXpZbU5tWXlJc0ltTnZibk5sYm5SZmFXUWlPaUk1WldJeVl6RXhaQzB5TmpObUxUUXdNekl0T0RsbFlpMWtaVEE0TmpNd056SmhNV1lpZlEuTHJ0eVNieEpfZW9RdVc1WFh2X013M2poWG14cEpCOVNyQXBxVGlrTDNHZ1J2WFZvZlNzdnFVZklyVHdhODRHSGo2a01YWm9GRlVpVkZPN1ZpNUJsd0FPSkpXN19waXhfVmRfT3VoUzRQd1pjQ1NMM29INzRyWHdCeE9wQmFWSjBON0RvVDZnX2RNUEpGanlhaTNheHQ0SjhQUmN4a0sza0VDd01oTG9PNGZraWJVdTRZaFZaRkFWc0VyZlFXdURGMmVxd3NXQTFUWlNidnZ5M2pMaVFRWTlnT29jY1BxTmJRVE9vR002akRUM2dpTXYxeGExTlBIOW9rZjBaMmFJbXJQWXoyMnlOZ3JXVDFaTjlXZ21lM3RuWXFkOXI5UWlMdHcweVE5WmlfcTFJWEVwazFDSmRNNUhyY3lmOEl4QkhMVEtPdDJFVHhpcWhpS1oybzk3ZElnIiwiYWN0aXZpdHlpZCI6IjgzY2EwZDhjLTMyZjItNDAxZC1hN2Y4LTg3YmFkNjY0NThiYSIsImNoYXJzZXQiOiJVVEYtOCIsIkFjY2VwdCI6ImFwcGxpY2F0aW9uL2pzb24iLCJ4LWZhcGktZmluYW5jaWFsLWlkIjoib3Blbi1iYW5rIiwiQ29ubmVjdGlvbiI6IktlZXAtQWxpdmUiLCJVc2VyLUFnZW50IjoiQXBhY2hlLUh0dHBDbGllbnQvNC41LjMgKEphdmEvMS44LjBfMzExKSIsIkhvc3QiOiJsb2NhbGhvc3Q6ODI0MyIsIkFjY2VwdC1FbmNvZGluZyI6Imd6aXAsZGVmbGF0ZSIsIkNvbnRlbnQtTGVuZ3RoIjoiMTkwIiwiQ29udGVudC1UeXBlIjoiYXBwbGljYXRpb24vanNvbjsgY2hhcnNldD1VVEYtOCJ9LCJjb25zZW50SWQiOiI5ZWIyYzExZC0yNjNmLTQwMzItODllYi1kZTA4NjMwNzJhMWYiLCJjbGllbnRJZCI6IlhpWFRsbVZpSDFyM1ZEQUdCZV9HOHRWdlZzZ2EiLCJyZXNvdXJjZVBhcmFtcyI6eyJyZXNvdXJjZSI6Ii9jYnBpaS9mdW5kcy1jb25maXJtYXRpb25zIiwiY29udGV4dCI6Ii9vcGVuLWJhbmtpbmcvdjMuMS9jYnBpaSIsImh0dHBNZXRob2QiOiJQT1NUIn0sImJvZHkiOnsiRGF0YSI6eyJSZWZlcmVuY2UiOiJQdXJjaGFzZTAxIiwiQ29uc2VudElkIjoiOWViMmMxMWQtMjYzZi00MDMyLTg5ZWItZGUwODYzMDcyYTFmIiwiSW5zdHJ1Y3RlZEFtb3VudCI6eyJBbW91bnQiOiIxMC4wMCIsIkN1cnJlbmN5IjoiVVNEIn19fSwidXNlcklkIjoiYWRtaW5Ad3NvMi5jb21AY2FyYm9uLnN1cGVyQGNhcmJvbi5zdXBlciIsImVsZWN0ZWRSZXNvdXJjZSI6Ii9mdW5kcy1jb25maXJtYXRpb25zIn0.VUVG-xXzFzqLnDML9YVbajFt1nfjJMHyypfHGSU5KWjv3aAShz7i4ZjospHb2qJiWvegONW4sycT7TwmRJyGS27ijd5dda9F5ET-SswQz0_zXQh9fNFYjNPeYJ7MJ5rrFTg4wCRahlcGMMebhXMrvuJsJ8Q7P-6HtzQph6N9HifkhJ2crOqdBmX-eK2ySlabeHxVqZVWLvTIn6gJVz-Iw-BD-4rBD5KyZEP94C8xZrs3vARnp2-NOAgG79694e_40Bq4rKm7JkHdP8xrMfL0IRSWoeVhwhpFqM2iQhG3xUhhzAecw5bJTwjG2hnOt2D1welJc8-ABt8IrEW4lKBQ7Q
```

2. Run the following cURL command in a command prompt to validate the access token. Update the placeholders with relevant values.

``` curl
curl --location 'https://<IS_HOST>:<IS_PORT>/api/bfsi/consent/validate/validate' \
--header 'Content-Type: application/jwt' \
--header 'Authorization: Basic <Base 64 encoded admin credentials>' \
--cert <TRANSPORT_PUBLIC_KEY_FILE_PATH> --key <TRANSPORT_PRIVATE_KEY_FILE_PATH> \
--data 'eyJhbGciOiJQUzI1NiIsInR5cCI6IkpXVCJ9.eyJoZWFkZXJzIjp7IkF1dGhvcml6YXRpb24iOiJCZWFyZXIgZXlKNE5YUWlPaUpPVkdSdFdtcE5ORnBFYXpOT2Fsa3dXWHBqTlUxdFdtMVBSR2QzVFZSRk0wMVhXWGRPUkVVMVRWZFNiRnBFWnpST2VtTTBXa0VpTENKcmFXUWlPaUpOZWxsNFRXMUdhMDlIV1hkTlYwa3dXbGRPYlU1RVkzaE9SMWwzV1cxTk5GcFVRVE5OVjBreVRrUkJlbHBIVVhwT1IwMHdXa2RTYkU1cVNtdFBSRVpyV2tSU2FVOVVSbXROVjBab1RYcFZNbHBIVm14T1oxOVNVekkxTmlJc0ltRnNaeUk2SWxKVE1qVTJJbjAuZXlKemRXSWlPaUpoWkcxcGJrQjNjMjh5TG1OdmJVQmpZWEppYjI0dWMzVndaWElpTENKaGRYUWlPaUpCVUZCTVNVTkJWRWxQVGw5VlUwVlNJaXdpWVhWa0lqb2lXR2xZVkd4dFZtbElNWEl6VmtSQlIwSmxYMGM0ZEZaMlZuTm5ZU0lzSW01aVppSTZNVFkwTWpBMU5UWXdPQ3dpWVhwd0lqb2lXR2xZVkd4dFZtbElNWEl6VmtSQlIwSmxYMGM0ZEZaMlZuTm5ZU0lzSW5OamIzQmxJam9pWm5WdVpITmpiMjVtYVhKdFlYUnBiMjV6SUc5d1pXNXBaQ0lzSW1semN5STZJbWgwZEhCek9sd3ZYQzlzYjJOaGJHaHZjM1E2T1RRME5sd3ZiMkYxZEdneVhDOTBiMnRsYmlJc0ltTnVaaUk2ZXlKNE5YUWpVekkxTmlJNkluWlpiMVZaVWxOUk4wTm5iMWw0VGsxWFYwOTZRemgxVG1aUmNtbHpOSEJZVVZnd1dtMXBkRko0ZW5NaWZTd2laWGh3SWpveE5qUXlNRFU1TWpBNExDSnBZWFFpT2pFMk5ESXdOVFUyTURnc0ltcDBhU0k2SW1KbVlXTTVOamMxTFdJNVpUZ3ROREV6TkMwNE1HUXhMVGcxTURGbE1XSXpZbU5tWXlJc0ltTnZibk5sYm5SZmFXUWlPaUk1WldJeVl6RXhaQzB5TmpObUxUUXdNekl0T0RsbFlpMWtaVEE0TmpNd056SmhNV1lpZlEuTHJ0eVNieEpfZW9RdVc1WFh2X013M2poWG14cEpCOVNyQXBxVGlrTDNHZ1J2WFZvZlNzdnFVZklyVHdhODRHSGo2a01YWm9GRlVpVkZPN1ZpNUJsd0FPSkpXN19waXhfVmRfT3VoUzRQd1pjQ1NMM29INzRyWHdCeE9wQmFWSjBON0RvVDZnX2RNUEpGanlhaTNheHQ0SjhQUmN4a0sza0VDd01oTG9PNGZraWJVdTRZaFZaRkFWc0VyZlFXdURGMmVxd3NXQTFUWlNidnZ5M2pMaVFRWTlnT29jY1BxTmJRVE9vR002akRUM2dpTXYxeGExTlBIOW9rZjBaMmFJbXJQWXoyMnlOZ3JXVDFaTjlXZ21lM3RuWXFkOXI5UWlMdHcweVE5WmlfcTFJWEVwazFDSmRNNUhyY3lmOEl4QkhMVEtPdDJFVHhpcWhpS1oybzk3ZElnIiwiYWN0aXZpdHlpZCI6IjgzY2EwZDhjLTMyZjItNDAxZC1hN2Y4LTg3YmFkNjY0NThiYSIsImNoYXJzZXQiOiJVVEYtOCIsIkFjY2VwdCI6ImFwcGxpY2F0aW9uL2pzb24iLCJ4LWZhcGktZmluYW5jaWFsLWlkIjoib3Blbi1iYW5rIiwiQ29ubmVjdGlvbiI6IktlZXAtQWxpdmUiLCJVc2VyLUFnZW50IjoiQXBhY2hlLUh0dHBDbGllbnQvNC41LjMgKEphdmEvMS44LjBfMzExKSIsIkhvc3QiOiJsb2NhbGhvc3Q6ODI0MyIsIkFjY2VwdC1FbmNvZGluZyI6Imd6aXAsZGVmbGF0ZSIsIkNvbnRlbnQtTGVuZ3RoIjoiMTkwIiwiQ29udGVudC1UeXBlIjoiYXBwbGljYXRpb24vanNvbjsgY2hhcnNldD1VVEYtOCJ9LCJjb25zZW50SWQiOiI5ZWIyYzExZC0yNjNmLTQwMzItODllYi1kZTA4NjMwNzJhMWYiLCJjbGllbnRJZCI6IlhpWFRsbVZpSDFyM1ZEQUdCZV9HOHRWdlZzZ2EiLCJyZXNvdXJjZVBhcmFtcyI6eyJyZXNvdXJjZSI6Ii9jYnBpaS9mdW5kcy1jb25maXJtYXRpb25zIiwiY29udGV4dCI6Ii9vcGVuLWJhbmtpbmcvdjMuMS9jYnBpaSIsImh0dHBNZXRob2QiOiJQT1NUIn0sImJvZHkiOnsiRGF0YSI6eyJSZWZlcmVuY2UiOiJQdXJjaGFzZTAxIiwiQ29uc2VudElkIjoiOWViMmMxMWQtMjYzZi00MDMyLTg5ZWItZGUwODYzMDcyYTFmIiwiSW5zdHJ1Y3RlZEFtb3VudCI6eyJBbW91bnQiOiIxMC4wMCIsIkN1cnJlbmN5IjoiVVNEIn19fSwidXNlcklkIjoiYWRtaW5Ad3NvMi5jb21AY2FyYm9uLnN1cGVyQGNhcmJvbi5zdXBlciIsImVsZWN0ZWRSZXNvdXJjZSI6Ii9mdW5kcy1jb25maXJtYXRpb25zIn0.VUVG-xXzFzqLnDML9YVbajFt1nfjJMHyypfHGSU5KWjv3aAShz7i4ZjospHb2qJiWvegONW4sycT7TwmRJyGS27ijd5dda9F5ET-SswQz0_zXQh9fNFYjNPeYJ7MJ5rrFTg4wCRahlcGMMebhXMrvuJsJ8Q7P-6HtzQph6N9HifkhJ2crOqdBmX-eK2ySlabeHxVqZVWLvTIn6gJVz-Iw-BD-4rBD5KyZEP94C8xZrs3vARnp2-NOAgG79694e_40Bq4rKm7JkHdP8xrMfL0IRSWoeVhwhpFqM2iQhG3xUhhzAecw5bJTwjG2hnOt2D1welJc8-ABt8IrEW4lKBQ7Q'
```

3. Upon successfull validation of the consent, you will receive a response as follows:

``` json
{
    "isValid": true,
    "consentInformation": "eyJhbGciOiJSUzI1NiJ9.eyJjbGllbnRJZCI6Ilc0UWFNall2OXJuNDlPN2RwZUJRRDVETEdGUWEiLCJjdXJyZW50U3RhdHVzIjoiQXV0aG9yaXNlZCIsImNyZWF0ZWRUaW1lc3RhbXAiOjE3MjEwMzU0OTQsInJlY3VycmluZ0luZGljYXRvciI6ZmFsc2UsImF1dGhvcml6YXRpb25SZXNvdXJjZXMiOlt7InVwZGF0ZWRUaW1lIjoxNzIxMDM1NTI4LCJjb25zZW50SWQiOiI1NWNmOGMyMC1mZTg5LTQzNDEtYTE3OS0wZWIwOGE3NzE5OTQiLCJhdXRob3JpemF0aW9uSWQiOiIwNTc5ZDQ1Zi03ODY0LTQ0ZTUtYWM5Yy1kMWM3MGRkN2I0ZDkiLCJhdXRob3JpemF0aW9uVHlwZSI6ImF1dGhvcmlzYXRpb24iLCJ1c2VySWQiOiJlNjBjYWQxYi1jOGYxLTQ2NzMtYmMxZC1mMjJlNjVlZGU1NzYiLCJhdXRob3JpemF0aW9uU3RhdHVzIjoiQXV0aG9yaXNlZCJ9XSwidXBkYXRlZFRpbWVzdGFtcCI6MTcyMTAzNTUyOCwiY29uc2VudF90eXBlIjoiYWNjb3VudHMiLCJ2YWxpZGl0eVBlcmlvZCI6MCwiY29uc2VudEF0dHJpYnV0ZXMiOnt9LCJjb25zZW50SWQiOiI1NWNmOGMyMC1mZTg5LTQzNDEtYTE3OS0wZWIwOGE3NzE5OTQiLCJjb25zZW50TWFwcGluZ1Jlc291cmNlcyI6W3sibWFwcGluZ0lkIjoiYmNmOThjYWEtZTRlNS00ZDg0LWFiNjMtNDE4MDYzZjdkNjM0IiwibWFwcGluZ1N0YXR1cyI6ImFjdGl2ZSIsImFjY291bnRfaWQiOiIxMjM0NSIsImF1dGhvcml6YXRpb25JZCI6IjA1NzlkNDVmLTc4NjQtNDRlNS1hYzljLWQxYzcwZGQ3YjRkOSIsInBlcm1pc3Npb24iOiJwcmltYXJ5In1dLCJhZGRpdGlvbmFsQ29uc2VudEluZm8iOnt9LCJyZWNlaXB0Ijp7IlJpc2siOnt9LCJEYXRhIjp7IlRyYW5zYWN0aW9uVG9EYXRlVGltZSI6IjIwMjQtMDctMThUMTQ6NTQ6NDkuNjEyMjc3KzA1OjMwIiwiRXhwaXJhdGlvbkRhdGVUaW1lIjoiMjAyNC0wNy0yMFQxNDo1NDo0OS42MDA5ODYrMDU6MzAiLCJQZXJtaXNzaW9ucyI6WyJSZWFkQWNjb3VudHNEZXRhaWwiLCJSZWFkQmFsYW5jZXMiLCJSZWFkVHJhbnNhY3Rpb25zRGV0YWlsIl0sIlRyYW5zYWN0aW9uRnJvbURhdGVUaW1lIjoiMjAyNC0wNy0xNVQxNDo1NDo0OS42MTIxMjcrMDU6MzAifX0sImNvbnNlbnRGcmVxdWVuY3kiOjB9.Q0OLsP97Gvdw_QutSYJIBF-aYoWwIwREdosqUBt4W7ba8MznZ9ZuEdMFp0ELvTY3-_fAFs_s9CYdENw2pLI_CsSmo8Ca7XoFL-T0gkTCZex_AFZWFfnZDmTgHvLgq6iHtADBVVNX_7uurk49j8GP4zI-X6fn_RSHReal_bjSqWBvjqvkqUUDj-3PJ8QOMGQ_4U00ofKoEWfMM4KKv1mxT37F4Ul2UMoBVl5J2PLrqGRoUtV-P1kjSH2ZwY9DH0tG6mWDN7dNjJjlkJMYGZES9qowEycX6Hks53MwBNeNGMJO_G8TOmu4hv85RekRMdX_g3TpZp9Y0spxxog5RU-rMg"
}
```

4. Validation response will contain the required fields inside `consentInformation` JWT that needs to be passed to the backend service for further processing.

``` json
{
   "clientId":"W4QaMjYv9rn49O7dpeBQD5DLGFQa",
   "currentStatus":"Authorised",
   "createdTimestamp":1721035494,
   "recurringIndicator":false,
   "authorizationResources":[
      {
         "updatedTime":1721035528,
         "consentId":"55cf8c20-fe89-4341-a179-0eb08a771994",
         "authorizationId":"0579d45f-7864-44e5-ac9c-d1c70dd7b4d9",
         "authorizationType":"authorisation",
         "userId":"e60cad1b-c8f1-4673-bc1d-f22e65ede576",
         "authorizationStatus":"Authorised"
      }
   ],
   "updatedTimestamp":1721035528,
   "consent_type":"accounts",
   "validityPeriod":0,
   "consentAttributes":{
      
   },
   "consentId":"55cf8c20-fe89-4341-a179-0eb08a771994",
   "consentMappingResources":[
      {
         "mappingId":"bcf98caa-e4e5-4d84-ab63-418063f7d634",
         "mappingStatus":"active",
         "account_id":"12345",
         "authorizationId":"0579d45f-7864-44e5-ac9c-d1c70dd7b4d9",
         "permission":"primary"
      }
   ],
   "additionalConsentInfo":{
      
   },
   "receipt":{
      "Risk":{
         
      },
      "Data":{
         "TransactionToDateTime":"2024-07-18T14:54:49.612277+05:30",
         "ExpirationDateTime":"2024-07-20T14:54:49.600986+05:30",
         "Permissions":[
            "ReadAccountsDetail",
            "ReadBalances",
            "ReadTransactionsDetail"
         ],
         "TransactionFromDateTime":"2024-07-15T14:54:49.612127+05:30"
      }
   },
   "consentFrequency":0
}
```


