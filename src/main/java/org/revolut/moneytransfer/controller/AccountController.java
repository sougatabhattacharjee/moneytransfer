package org.revolut.moneytransfer.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.google.gson.Gson;
import org.eclipse.jetty.http.HttpStatus;
import org.revolut.moneytransfer.config.ResponseError;
import org.revolut.moneytransfer.domain.Account;
import org.revolut.moneytransfer.domain.AccountStatus;
import org.revolut.moneytransfer.domain.request.AccountBalanceRequest;
import org.revolut.moneytransfer.domain.request.AccountInfoRequest;
import org.revolut.moneytransfer.domain.request.AccountStatusRequest;
import org.revolut.moneytransfer.exception.AccountNotFoundException;
import org.revolut.moneytransfer.exception.CurrencyException;
import org.revolut.moneytransfer.exception.CurrencyNotMatchingException;
import org.revolut.moneytransfer.service.account.AccountDao;
import org.revolut.moneytransfer.service.account.AccountDaoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ResponseTransformer;
import spark.utils.StringUtils;

import java.util.List;
import java.util.Optional;

import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.path;
import static spark.Spark.post;
import static spark.Spark.put;

/**
 * Created by Sougata Bhattacharjee
 * On 14.09.18
 */
public class AccountController {
    private static final Logger LOG = LoggerFactory.getLogger(AccountController.class);
    private final ObjectMapper objectMapper;
    private static final String JSON = "application/json";
    private final AccountDao accountDao = new AccountDaoImpl();

    public AccountController(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void initializeRoutes(Gson gson, ResponseTransformer jsonTransformer) {
        before((request, response) -> response.type(JSON));

        path("/health", () -> get("", (req, res) -> "healthy"));

        post("/account", JSON, (request, response) -> {
            final String body = request.body();

            if (StringUtils.isEmpty(body)) {
                response.status(HttpStatus.BAD_REQUEST_400);
                return new ResponseError("Cannot Process empty request");
            }
            try {
                final Account account = objectMapper.readValue(body, Account.class);
                response.status(HttpStatus.CREATED_201);
                return accountDao.insertNewAccount(account);
            } catch (InvalidFormatException ex) {
                response.status(HttpStatus.UNPROCESSABLE_ENTITY_422);
                return new ResponseError(ex.getMessage());
            } catch (JsonProcessingException ex) {
                response.status(HttpStatus.BAD_REQUEST_400);
                return new ResponseError(ex.getMessage());
            } catch (Exception ex) {
                response.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
                return new ResponseError(ex.getMessage());
            }

        }, jsonTransformer);

        get("/allAccounts", JSON, (request, response) -> {
            final String flag = request.queryParamOrDefault("flag", "");

            try {
                final List<Account> results;

                if (flag.isEmpty()) {
                    results = accountDao.getAllAccounts(Optional.empty());
                } else if (flag.equalsIgnoreCase("active")) {
                    results = accountDao.getAllAccounts(Optional.of(AccountStatus.ACTIVE));
                } else if (flag.equalsIgnoreCase("inactive")) {
                    results = accountDao.getAllAccounts(Optional.of(AccountStatus.INACTIVE));
                } else {
                    response.status(HttpStatus.BAD_REQUEST_400);
                    return new ResponseError("Flag value can only consists `ACTIVE` or `INACTIVE`");
                }
                response.status(HttpStatus.OK_200);
                return results;
            } catch (Exception ex) {
                response.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
                return new ResponseError(ex.getMessage());
            }

        }, jsonTransformer);

        get("/account/:id", JSON, (request, response) -> {
            final long accountId = Long.parseLong(request.params("id"));
            try {
                response.status(HttpStatus.OK_200);
                return accountDao.getAccountById(accountId);
            } catch (AccountNotFoundException ex) {
                response.status(HttpStatus.NOT_FOUND_404);
                return new ResponseError(ex.getMessage());
            } catch (Exception ex) {
                response.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
                return new ResponseError(ex.getMessage());
            }

        }, jsonTransformer);

        put("/updateAccountInfo", JSON, (request, response) -> {
            final AccountInfoRequest account;
            final String body = request.body();
            if (StringUtils.isEmpty(body)) {
                response.status(HttpStatus.BAD_REQUEST_400);
                return new ResponseError("Payload cannot be empty");
            }
            try {
                account = objectMapper.readValue(body, AccountInfoRequest.class);
                response.status(HttpStatus.OK_200);
                return accountDao.updateAccountById(account);
            } catch (InvalidFormatException ex) {
                response.status(HttpStatus.UNPROCESSABLE_ENTITY_422);
                return new ResponseError(ex.getMessage());
            } catch (JsonProcessingException ex) {
                response.status(HttpStatus.BAD_REQUEST_400);
                return new ResponseError(ex.getMessage());
            } catch (AccountNotFoundException ex) {
                response.status(HttpStatus.NOT_FOUND_404);
                return new ResponseError(ex.getMessage());
            } catch (Exception ex) {
                response.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
                return new ResponseError(ex.getMessage());
            }

        }, jsonTransformer);


        put("/updateAccountStatus", JSON, (request, response) -> {
            final AccountStatusRequest account;
            final String body = request.body();
            if (body.isEmpty()) {
                response.status(HttpStatus.BAD_REQUEST_400);
                return new ResponseError("Payload cannot be empty");
            }
            try {
                account = objectMapper.readValue(body, AccountStatusRequest.class);
                response.status(HttpStatus.OK_200);
                return accountDao.updateAccountById(account);
            } catch (InvalidFormatException ex) {
                response.status(HttpStatus.UNPROCESSABLE_ENTITY_422);
                return new ResponseError(ex.getMessage());
            } catch (JsonProcessingException ex) {
                response.status(HttpStatus.BAD_REQUEST_400);
                return new ResponseError(ex.getMessage());
            } catch (AccountNotFoundException ex) {
                response.status(HttpStatus.NOT_FOUND_404);
                return new ResponseError(ex.getMessage());
            } catch (Exception ex) {
                response.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
                return new ResponseError(ex.getMessage());
            }

        }, jsonTransformer);

        put("/updateAccountBalance/credit", JSON, (request, response) -> {
            final AccountBalanceRequest account;
            final String body = request.body();
            if (body.isEmpty()) {
                response.status(HttpStatus.BAD_REQUEST_400);
                return new ResponseError("Payload cannot be empty");
            }
            try {

                account = objectMapper.readValue(body, AccountBalanceRequest.class);
            } catch (Exception ex) {
                response.status(HttpStatus.BAD_REQUEST_400);
                return new ResponseError("Malformed Request");
            }
            try {
                response.status(HttpStatus.OK_200);
                return accountDao.updateAccountBalance(account, true);
            } catch (AccountNotFoundException ex) {
                response.status(HttpStatus.NOT_FOUND_404);
                return new ResponseError(ex.getMessage());
            } catch (CurrencyNotMatchingException ex) {
                response.status(HttpStatus.FORBIDDEN_403);
                return new ResponseError(ex.getMessage());
            } catch (Exception ex) {
                response.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
                return new ResponseError(ex.getMessage());
            }

        }, jsonTransformer);

        put("/updateAccountBalance/debit", JSON, (request, response) -> {
            final AccountBalanceRequest account;
            try {
                final String body = request.body();
                if (body.isEmpty()) {
                    response.status(HttpStatus.BAD_REQUEST_400);
                    return new ResponseError("Payload cannot be empty");
                }
                account = objectMapper.readValue(body, AccountBalanceRequest.class);
            } catch (Exception ex) {
                response.status(HttpStatus.BAD_REQUEST_400);
                return new ResponseError("Malformed Request");
            }
            try {
                response.status(HttpStatus.OK_200);
                return accountDao.updateAccountBalance(account, false);
            } catch (AccountNotFoundException ex) {
                response.status(HttpStatus.NOT_FOUND_404);
                return new ResponseError(ex.getMessage());
            } catch (CurrencyException ex) {
                response.status(HttpStatus.FORBIDDEN_403);
                return new ResponseError(ex.getMessage());
            } catch (Exception ex) {
                response.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
                return new ResponseError(ex.getMessage());
            }

        }, jsonTransformer);

    }

}
