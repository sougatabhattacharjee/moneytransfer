package org.revolut.moneytransfer.controller;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.revolut.moneytransfer.ApplicationMain;
import org.revolut.moneytransfer.domain.Account;
import org.revolut.moneytransfer.domain.Currency;
import org.revolut.moneytransfer.domain.Money;
import org.revolut.moneytransfer.domain.MoneyTransfer;

import java.math.BigDecimal;

import static com.jayway.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Created by Sougata Bhattacharjee
 * On 21.09.18
 */
public class MoneyTransferControllerTest {

    private int port;
    private String baseURL;
    private static final String MONEY_TRANSFER_SCHEMA_JSON = "json/money-transfer-schema.json";
    private Account accountId1;
    private Account accountId2;
    private Account accountId3;
    private Account accountId4;


    @Before
    public void setUp() {
        port = ApplicationMain.startServer();
        baseURL = "http://localhost:" + port;
        accountId1 = createAndGetAccountId(getRandomAccountJson(new Money(BigDecimal.valueOf(300), Currency.EUR)));
        accountId2 = createAndGetAccountId(getRandomAccountJson(new Money(BigDecimal.valueOf(100), Currency.EUR)));
        accountId3 = createAndGetAccountId(getRandomAccountJson(new Money(BigDecimal.valueOf(200), Currency.EUR)));
        accountId4 = createAndGetAccountId(getRandomAccountJson(new Money(BigDecimal.valueOf(200), Currency.GBP)));

    }

    @After
    public void tearDown() throws InterruptedException {
        ApplicationMain.stopServer();

        //Waiting to spark shutting down
        Thread.sleep(500);
    }

    @Test
    public void testCreateTransfer() {

        final Money amountToTransfer = new Money(BigDecimal.valueOf(50), Currency.EUR);

        final String transferPayload = "{" +
                "\"sourceAccountId\":" + accountId1.getAccountId() + "," +
                "\"destinationAccountId\":" + accountId2.getAccountId() + "," +
                "\"amount\": {" +
                "        \"amount\":" + amountToTransfer.getAmount() + " ," +
                "        \"currency\": \"" + amountToTransfer.getCurrency() + "\"" +
                "    }," +
                "\"description\":\"Testing\"" +
                "}";

        given()
                .contentType("application/json")
                .body(transferPayload)
                .when()
                .post(baseURL + "/transfer")
                .prettyPeek()
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(matchesJsonSchemaInClasspath(MONEY_TRANSFER_SCHEMA_JSON))
                .log().all();

        Assert.assertEquals(getAccountBalanceById(accountId1.getAccountId()).getAmount(), BigDecimal.valueOf(250));  // initial amount was 300, after transfer it will be 250
        Assert.assertEquals(getAccountBalanceById(accountId2.getAccountId()).getAmount(), BigDecimal.valueOf(150));  // initial amount was 100, after transfer it will be 150
    }

    @Test
    public void testUnsuccessfulTransfer() {
        String transferPayload;

        final Money amountToTransfer = new Money(BigDecimal.valueOf(50), Currency.EUR);

        // when source id and destination id is same
        transferPayload = "{" +
                "\"sourceAccountId\":" + accountId1.getAccountId() + "," +
                "\"destinationAccountId\":" + accountId1.getAccountId() + "," +
                "\"amount\": {" +
                "        \"amount\":" + amountToTransfer.getAmount() + " ," +
                "        \"currency\": \"" + amountToTransfer.getCurrency() + "\"" +
                "    }," +
                "\"description\":\"Testing\"" +
                "}";
        given()
                .contentType("application/json")
                .body(transferPayload)
                .when()
                .post(baseURL + "/transfer")
                .prettyPeek()
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN)
                .and()
                .body("message", equalTo("Source and destination account must be different"));


        // when source account does not exist
        transferPayload = "{" +
                "\"sourceAccountId\":" + 1212122 + "," +
                "\"destinationAccountId\":" + accountId1.getAccountId() + "," +
                "\"amount\": {" +
                "        \"amount\":" + amountToTransfer.getAmount() + " ," +
                "        \"currency\": \"" + amountToTransfer.getCurrency() + "\"" +
                "    }," +
                "\"description\":\"Testing\"" +
                "}";
        given()
                .contentType("application/json")
                .body(transferPayload)
                .when()
                .post(baseURL + "/transfer")
                .prettyPeek()
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN)
                .and()
                .body("message", equalTo("Source account does not exist"));


        // when destination account does not exist
        transferPayload = "{" +
                "\"sourceAccountId\":" + accountId1.getAccountId() + "," +
                "\"destinationAccountId\":" + 1212121313 + "," +
                "\"amount\": {" +
                "        \"amount\":" + amountToTransfer.getAmount() + " ," +
                "        \"currency\": \"" + amountToTransfer.getCurrency() + "\"" +
                "    }," +
                "\"description\":\"Testing\"" +
                "}";
        given()
                .contentType("application/json")
                .body(transferPayload)
                .when()
                .post(baseURL + "/transfer")
                .prettyPeek()
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN)
                .and()
                .body("message", equalTo("Destination account does not exist"));


        // when source and destination account has different currency
        transferPayload = "{" +
                "\"sourceAccountId\":" + accountId1.getAccountId() + "," +
                "\"destinationAccountId\":" + accountId4.getAccountId() + "," +
                "\"amount\": {" +
                "        \"amount\":" + amountToTransfer.getAmount() + " ," +
                "        \"currency\": \"" + amountToTransfer.getCurrency() + "\"" +
                "    }," +
                "\"description\":\"Testing\"" +
                "}";
        given()
                .contentType("application/json")
                .body(transferPayload)
                .when()
                .post(baseURL + "/transfer")
                .prettyPeek()
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN)
                .and()
                .body("message", equalTo("Source and Destination account have different Currency"));


        // when source account does not have sufficient fund
        final Money amountToTransfer1 = new Money(BigDecimal.valueOf(500), Currency.EUR);
        transferPayload = "{" +
                "\"sourceAccountId\":" + accountId1.getAccountId() + "," +
                "\"destinationAccountId\":" + accountId2.getAccountId() + "," +
                "\"amount\": {" +
                "        \"amount\":" + amountToTransfer1.getAmount() + " ," +
                "        \"currency\": \"" + amountToTransfer1.getCurrency() + "\"" +
                "    }," +
                "\"description\":\"Testing\"" +
                "}";
        given()
                .contentType("application/json")
                .body(transferPayload)
                .when()
                .post(baseURL + "/transfer")
                .prettyPeek()
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN)
                .and()
                .body("message", equalTo("Source Account does not have sufficient fund"));

    }


    @Test
    public void testGetTransferByAccountId() {

        /**
         * 1. Account 1 transfer 50 EUR to Account 2
         * 2. Account 1 transfer 50 EUR to Account 3
         * 3. Account 1 transfer 10 EUR to Account 2
         * 4. Account 1 transfer 40 EUR to Account 3
         * 5. Account 2 transfer 20 EUR to Account 3
         * 6. Account 2 transfer 30 EUR to Account 1
         * 7. Account 2 transfer 10 EUR to Account 1
         */

        final String transferPayload1 = "{" +
                "\"sourceAccountId\":" + accountId1.getAccountId() + "," +
                "\"destinationAccountId\":" + accountId2.getAccountId() + "," +
                "\"amount\": {" +
                "        \"amount\":" + BigDecimal.valueOf(50) + " ," +
                "        \"currency\": \"" + Currency.EUR + "\"" +
                "    }," +
                "\"description\":\"Testing\"" +
                "}";

        final String transferPayload2 = "{" +
                "\"sourceAccountId\":" + accountId1.getAccountId() + "," +
                "\"destinationAccountId\":" + accountId3.getAccountId() + "," +
                "\"amount\": {" +
                "        \"amount\":" + BigDecimal.valueOf(50) + " ," +
                "        \"currency\": \"" + Currency.EUR + "\"" +
                "    }," +
                "\"description\":\"Testing\"" +
                "}";

        final String transferPayload3 = "{" +
                "\"sourceAccountId\":" + accountId1.getAccountId() + "," +
                "\"destinationAccountId\":" + accountId2.getAccountId() + "," +
                "\"amount\": {" +
                "        \"amount\":" + BigDecimal.valueOf(10) + " ," +
                "        \"currency\": \"" + Currency.EUR + "\"" +
                "    }," +
                "\"description\":\"Testing\"" +
                "}";

        final String transferPayload4 = "{" +
                "\"sourceAccountId\":" + accountId1.getAccountId() + "," +
                "\"destinationAccountId\":" + accountId3.getAccountId() + "," +
                "\"amount\": {" +
                "        \"amount\":" + BigDecimal.valueOf(40) + " ," +
                "        \"currency\": \"" + Currency.EUR + "\"" +
                "    }," +
                "\"description\":\"Testing\"" +
                "}";

        final String transferPayload5 = "{" +
                "\"sourceAccountId\":" + accountId2.getAccountId() + "," +
                "\"destinationAccountId\":" + accountId3.getAccountId() + "," +
                "\"amount\": {" +
                "        \"amount\":" + BigDecimal.valueOf(20) + " ," +
                "        \"currency\": \"" + Currency.EUR + "\"" +
                "    }," +
                "\"description\":\"Testing\"" +
                "}";

        final String transferPayload6 = "{" +
                "\"sourceAccountId\":" + accountId2.getAccountId() + "," +
                "\"destinationAccountId\":" + accountId1.getAccountId() + "," +
                "\"amount\": {" +
                "        \"amount\":" + BigDecimal.valueOf(30) + " ," +
                "        \"currency\": \"" + Currency.EUR + "\"" +
                "    }," +
                "\"description\":\"Testing\"" +
                "}";

        final String transferPayload7 = "{" +
                "\"sourceAccountId\":" + accountId2.getAccountId() + "," +
                "\"destinationAccountId\":" + accountId1.getAccountId() + "," +
                "\"amount\": {" +
                "        \"amount\":" + BigDecimal.valueOf(10) + " ," +
                "        \"currency\": \"" + Currency.EUR + "\"" +
                "    }," +
                "\"description\":\"Testing\"" +
                "}";

        createTransfer(transferPayload1);
        createTransfer(transferPayload2);
        createTransfer(transferPayload3);
        createTransfer(transferPayload4);
        createTransfer(transferPayload5);
        createTransfer(transferPayload6);
        createTransfer(transferPayload7);


        // account 1 perform 4 transfers
        final MoneyTransfer[] transfersForAccount1 = given()
                .queryParam("accountId", accountId1.getAccountId())
                .when()
                .get(baseURL + "/alltransfer")
                .as(MoneyTransfer[].class);

        Assert.assertThat(transfersForAccount1.length, is(4));

        // account 2 perform 3 transfers
        final MoneyTransfer[] transfersForAccount2 = given()
                .queryParam("accountId", accountId2.getAccountId())
                .when()
                .get(baseURL + "/alltransfer")
                .as(MoneyTransfer[].class);

        Assert.assertThat(transfersForAccount2.length, is(3));

        // all transfers without query by id, total 7 transfers performed
        final MoneyTransfer[] allTransfers = given()
                .when()
                .get(baseURL + "/alltransfer")
                .as(MoneyTransfer[].class);

        Assert.assertThat(allTransfers.length, is(7));

        Assert.assertEquals(getAccountBalanceById(accountId1.getAccountId()).getAmount(), BigDecimal.valueOf(190));
        Assert.assertEquals(getAccountBalanceById(accountId2.getAccountId()).getAmount(), BigDecimal.valueOf(100));
        Assert.assertEquals(getAccountBalanceById(accountId3.getAccountId()).getAmount(), BigDecimal.valueOf(310));
        Assert.assertEquals(getAccountBalanceById(accountId4.getAccountId()).getAmount(), BigDecimal.valueOf(200));
    }


    private void createTransfer(final String payload) {
        given()
                .contentType("application/json")
                .body(payload)
                .when()
                .post(baseURL + "/transfer");
    }

    private Account createAndGetAccountId(final String json) {
        return given()
                .contentType("application/json")
                .body(json)
                .when()
                .post(baseURL + "/account")
                .as(Account.class);
    }

    private Money getAccountBalanceById(final long accountId) {
        return given()
                .when()
                .get(baseURL + "/account" + "/" + accountId)
                .as(Account.class).getBalance();
    }


    private String getRandomAccountJson(final Money amount) {
        return "{" +
                "\"accountHolder\":\"" + RandomStringUtils.random(10) + "\"," +
                "\"balance\": {" +
                "        \"amount\":" + amount.getAmount() + " ," +
                "        \"currency\": \"" + amount.getCurrency() + "\"" +
                "    }" +
                "}";
    }

}
