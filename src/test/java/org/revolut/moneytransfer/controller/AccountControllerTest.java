package org.revolut.moneytransfer.controller;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.revolut.moneytransfer.ApplicationMain;
import org.revolut.moneytransfer.domain.Account;
import org.revolut.moneytransfer.domain.Currency;

import java.util.SplittableRandom;

import static com.jayway.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;


/**
 * Created by Sougata Bhattacharjee
 * On 21.09.18
 */
public class AccountControllerTest {

    private int port;
    private String baseURL;
    private static final String ACCOUNT_SCHEMA_JSON = "json/account-schema.json";

    @Before
    public void setUp() {
        port = ApplicationMain.startServer();
        baseURL = "http://localhost:" + port;
    }

    @After
    public void tearDown() throws InterruptedException {
        ApplicationMain.stopServer();

        //Waiting to spark shutting down
        Thread.sleep(500);
    }

    @Test
    public void testAccountCreationSuccessful() {

        given()
                .contentType("application/json")
                .body(getRandomAccountJson())
                .when()
                .post(baseURL + "/account")
                .prettyPeek()
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body(matchesJsonSchemaInClasspath(ACCOUNT_SCHEMA_JSON))
                .log().all();

    }

    @Test
    public void testAccountCreationUnSuccessful() {

        // when account payload is empty
        given()
                .contentType("application/json")
                .body(StringUtils.EMPTY)
                .when()
                .post(baseURL + "/account")
                .prettyPeek()
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

        // when account payload has unrecognized or missing entry
        given()
                .contentType("application/json")
                .body(getRandomAccountJson().replace("accountHolder", "random"))
                .when()
                .post(baseURL + "/account")
                .prettyPeek()
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

        // when account payload has unprocessable entity
        given()
                .contentType("application/json")
                .body(getRandomAccountJson().replace("EUR", "random"))
                .when()
                .post(baseURL + "/account")
                .prettyPeek()
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY);

    }

    @Test
    public void testAccountStatusUpdate() {

        final long accountId = createAndGetAccountId(getRandomAccountJson());

        // update status of an account, return 200 when successful
        final String updateStatusPayload = "{" +
                "\"accountId\":" + accountId + "," +
                "\"status\":\"INACTIVE\"" +
                "}";

        given()
                .contentType("application/json")
                .body(updateStatusPayload)
                .when()
                .put(baseURL + "/updateAccountStatus")
                .prettyPeek()
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(matchesJsonSchemaInClasspath(ACCOUNT_SCHEMA_JSON))
                .and()
                .body("status", equalToIgnoringCase("INACTIVE"))
                .log().all();

        given()
                .contentType("application/json")
                .body(updateStatusPayload.replace("INACTIVE", "ACTIVE"))
                .when()
                .put(baseURL + "/updateAccountStatus")
                .prettyPeek()
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(matchesJsonSchemaInClasspath(ACCOUNT_SCHEMA_JSON))
                .and()
                .body("status", equalToIgnoringCase("ACTIVE"))
                .log().all();

        // update status of an account by not existing id, return 404
        final String updateStatusPayloadByWrongId = "{" +
                "\"accountId\":" + 111 + "," +
                "\"status\":\"INACTIVE\"" +
                "}";

        given()
                .contentType("application/json")
                .body(updateStatusPayloadByWrongId)
                .when()
                .put(baseURL + "/updateAccountStatus")
                .prettyPeek()
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);

    }

    @Test
    public void testAccountInformationUpdate() {

        final long accountId = createAndGetAccountId(getRandomAccountJson());
        // update balance - credit

        // update balance - debit

        // update account information, return 200 when successful
        final String updateInfoPayload = "{" +
                "\"accountId\":" + accountId + "," +
                "\"accountHolder\":\"Revolut\"" +
                "}";

        given()
                .contentType("application/json")
                .body(updateInfoPayload)
                .when()
                .put(baseURL + "/updateAccountInfo")
                .prettyPeek()
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(matchesJsonSchemaInClasspath(ACCOUNT_SCHEMA_JSON))
                .and()
                .body("accountHolder.name", equalToIgnoringCase("Revolut"))
                .log().all();

        // update account information by not existing id, return 404
        final String updateInfoPayloadByWrongId = "{" +
                "\"accountId\":" + 12344 + "," +
                "\"accountHolder\":\"Revolut\"" +
                "}";

        given()
                .contentType("application/json")
                .body(updateInfoPayloadByWrongId)
                .when()
                .put(baseURL + "/updateAccountInfo")
                .prettyPeek()
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void testAccountBalanceUpdatePerformCredit() {

        /**
         * update balance - perform credit
         */

        final int initialAmount = 300;

        final String account = "{" +
                "\"accountHolder\":\"Revolut\"," +
                "\"balance\": {" +
                "        \"amount\":" + initialAmount + " ," +
                "        \"currency\": \"" + Currency.EUR + "\"" +
                "    }" +
                "}";

        final long accountId = createAndGetAccountId(account);

        final int amountToCredit = 100;

        // return 200 when successful
        final String updateInfoPayload = "{" +
                "\"accountId\":" + accountId + "," +
                "\"balance\": {" +
                "        \"amount\":" + amountToCredit + " ," +
                "        \"currency\": \"" + Currency.EUR + "\"" +
                "    }" +
                "}";

        given()
                .contentType("application/json")
                .body(updateInfoPayload)
                .when()
                .put(baseURL + "/updateAccountBalance/credit")
                .prettyPeek()
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(matchesJsonSchemaInClasspath(ACCOUNT_SCHEMA_JSON))
                .and()
                .body("balance.amount", equalTo(initialAmount + amountToCredit))
                .log().all();

        // return 403, when perform credit with different currency
        final String updateInfoPayloadWrongCurrency = "{" +
                "\"accountId\":" + accountId + "," +
                "\"balance\": {" +
                "        \"amount\":" + amountToCredit + " ," +
                "        \"currency\": \"" + Currency.CHF + "\"" +
                "    }" +
                "}";

        given()
                .contentType("application/json")
                .body(updateInfoPayloadWrongCurrency)
                .when()
                .put(baseURL + "/updateAccountBalance/credit")
                .prettyPeek()
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN);

        // return 404, when account does not exist
        final String updateInfoPayloadWrongId = "{" +
                "\"accountId\":" + 123123 + "," +
                "\"balance\": {" +
                "        \"amount\":" + amountToCredit + " ," +
                "        \"currency\": \"" + Currency.EUR + "\"" +
                "    }" +
                "}";

        given()
                .contentType("application/json")
                .body(updateInfoPayloadWrongId)
                .when()
                .put(baseURL + "/updateAccountBalance/credit")
                .prettyPeek()
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);

    }

    @Test
    public void testAccountBalanceUpdatePerformDebit() {

        /**
         * update balance - perform debit
         */

        final int initialAmount = 200;

        final String account = "{" +
                "\"accountHolder\":\"Revolut\"," +
                "\"balance\": {" +
                "        \"amount\":" + initialAmount + " ," +
                "        \"currency\": \"" + Currency.EUR + "\"" +
                "    }" +
                "}";

        final long accountId = createAndGetAccountId(account);

        final int amountToDebit = 100;

        // return 200 when successful
        final String updateInfoPayload = "{" +
                "\"accountId\":" + accountId + "," +
                "\"balance\": {" +
                "        \"amount\":" + amountToDebit + " ," +
                "        \"currency\": \"" + Currency.EUR + "\"" +
                "    }" +
                "}";

        given()
                .contentType("application/json")
                .body(updateInfoPayload)
                .when()
                .put(baseURL + "/updateAccountBalance/debit")
                .prettyPeek()
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(matchesJsonSchemaInClasspath(ACCOUNT_SCHEMA_JSON))
                .and()
                .body("balance.amount", equalTo(initialAmount - amountToDebit))
                .log().all();

        // return 403, when perform debit with insufficient fund
        final String updateInfoPayloadInsufficientFund = "{" +
                "\"accountId\":" + accountId + "," +
                "\"balance\": {" +
                "        \"amount\":" + 400 + " ," +
                "        \"currency\": \"" + Currency.EUR + "\"" +
                "    }" +
                "}";

        given()
                .contentType("application/json")
                .body(updateInfoPayloadInsufficientFund)
                .when()
                .put(baseURL + "/updateAccountBalance/debit")
                .prettyPeek()
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN);

        // return 403, when perform debit with different currency
        final String updateInfoPayloadWrongCurrency = "{" +
                "\"accountId\":" + accountId + "," +
                "\"balance\": {" +
                "        \"amount\":" + amountToDebit + " ," +
                "        \"currency\": \"" + Currency.CHF + "\"" +
                "    }" +
                "}";

        given()
                .contentType("application/json")
                .body(updateInfoPayloadWrongCurrency)
                .when()
                .put(baseURL + "/updateAccountBalance/debit")
                .prettyPeek()
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN);

        // return 404, when account does not exist
        final String updateInfoPayloadWrongId = "{" +
                "\"accountId\":" + 123123 + "," +
                "\"balance\": {" +
                "        \"amount\":" + amountToDebit + " ," +
                "        \"currency\": \"" + Currency.EUR + "\"" +
                "    }" +
                "}";

        given()
                .contentType("application/json")
                .body(updateInfoPayloadWrongId)
                .when()
                .put(baseURL + "/updateAccountBalance/debit")
                .prettyPeek()
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND);

    }

    private long createAndGetAccountId(final String json) {

        return given()
                .contentType("application/json")
                .body(json)
                .when()
                .post(baseURL + "/account")
                .as(Account.class).getAccountId();
    }


    private String getRandomAccountJson() {
        return "{" +
                "\"accountHolder\":\"" + RandomStringUtils.random(10) + "\"," +
                "\"balance\": {" +
                "        \"amount\":" + new SplittableRandom().nextInt(200, 1001) + " ," +
                "        \"currency\": \"" + Currency.EUR + "\"" +
                "    }" +
                "}";
    }
}
