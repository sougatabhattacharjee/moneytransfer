package org.revolut.moneytransfer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.revolut.moneytransfer.controller.AccountController;
import org.revolut.moneytransfer.controller.MoneyTransferController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.ResponseTransformer;
import spark.Spark;

import java.util.Collections;

import static spark.Spark.exception;

/**
 * Created by Sougata Bhattacharjee
 * On 14.09.18
 */
public class ApplicationMain {
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationMain.class);

    private static final Gson GSON = gsonDateTime();
    private static final ResponseTransformer JSON_TRANSFORMER = GSON::toJson;
    private static final String JSON = "application/json";

    public static void main(String[] Args) {
        startServer();
    }

    private static void createAccountRoute() {
        final AccountController accountController = new AccountController(getObjectMapper());
        accountController.initializeRoutes(GSON, JSON_TRANSFORMER);
    }

    private static void createMoneyTransferController() {
        final MoneyTransferController moneyTransferController = new MoneyTransferController(getObjectMapper());
        moneyTransferController.initializeRoutes(GSON, JSON_TRANSFORMER);
    }

    public static int startServer() {
        Spark.init();

        createAccountRoute();
        createMoneyTransferController();

        exception(JsonSyntaxException.class, ApplicationMain::handleInvalidInput);
        LOG.debug("Created exception handlers");

        Spark.awaitInitialization();
        LOG.debug("Ready");
        return Spark.port();
    }

    private static void handleInvalidInput(Exception e, Request request, Response response) {
        response.status(400);
        errorResponse(e, request, response);
    }

    private static void errorResponse(Exception e, Request request, Response response) {
        response.type(JSON);
        response.body(GSON.toJson(Collections.singletonMap("error", e.getMessage())));
    }

    /**
     * For testing, as we want to start and stop the server.
     */
    public static void stopServer() {
        LOG.debug("Asking server to stop");
        Spark.stop();
    }

    private static Gson gsonDateTime() {
        return new GsonBuilder()
                .registerTypeAdapter(DateTime.class, (JsonSerializer<DateTime>) (json, typeOfSrc, context) -> new JsonPrimitive(ISODateTimeFormat.dateTime().print(json)))
                .registerTypeAdapter(DateTime.class, (JsonDeserializer<DateTime>) (json, typeOfT, context) -> ISODateTimeFormat.dateTime().parseDateTime(json.getAsString()))
                .create();
    }

    private static ObjectMapper getObjectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, true);
        return mapper;
    }


}
