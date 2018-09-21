package org.revolut.moneytransfer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.eclipse.jetty.http.HttpStatus;
import org.revolut.moneytransfer.config.ResponseError;
import org.revolut.moneytransfer.domain.MoneyTransfer;
import org.revolut.moneytransfer.exception.MoneyTransferWebServiceException;
import org.revolut.moneytransfer.service.transfer.MoneyTransferDao;
import org.revolut.moneytransfer.service.transfer.MoneyTransferDaoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ResponseTransformer;
import spark.utils.StringUtils;

import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.path;
import static spark.Spark.post;

/**
 * Created by Sougata Bhattacharjee
 * On 16.09.18
 */
public class MoneyTransferController {
    private static final Logger LOG = LoggerFactory.getLogger(MoneyTransferController.class);
    private final ObjectMapper objectMapper;
    private static final String JSON = "application/json";
    private final MoneyTransferDao transferDao = new MoneyTransferDaoImpl();

    public MoneyTransferController(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void initializeRoutes(Gson gson, ResponseTransformer jsonTransformer) {
        before((request, response) -> response.type(JSON));

        path("/health", () -> get("", (req, res) -> "healthy"));

        post("/money-transfer", "application/x-www-form-urlencoded", (request, response) -> {
            final MoneyTransfer moneyTransferRequest;
            try {
                final String body = request.body();
                if (StringUtils.isEmpty(body)) {
                    response.status(HttpStatus.BAD_REQUEST_400);
                    return new ResponseError("Payload cannot be empty, for details please check the API definition (swagger.yaml)");
                }
                moneyTransferRequest = objectMapper.readValue(body, MoneyTransfer.class);
            } catch (Exception ex) {
                response.status(HttpStatus.BAD_REQUEST_400);
                return new ResponseError("Malformed Request, for details please check the API definition (swagger.yaml)");
            }
            try {
                response.status(HttpStatus.OK_200);
                return transferDao.newMoneyTransfer(moneyTransferRequest);
            } catch (MoneyTransferWebServiceException ex) {
                response.status(HttpStatus.FORBIDDEN_403);
                return new ResponseError(ex.getMessage());
            } catch (Exception ex) {
                response.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
                return new ResponseError(ex.getMessage());
            }
        }, jsonTransformer);

        get("/alltransfer", "application/x-www-form-urlencoded", (request, response) -> {

            final String accountId = request.queryParamOrDefault("accountId", "");

            try {
                response.status(HttpStatus.OK_200);
                if (accountId.isEmpty())
                    return transferDao.getAllTransfers();
                else
                    return transferDao.getAllTransfersByAccount(Long.parseLong(accountId));
            } catch (Exception ex) {
                response.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
                return new ResponseError(ex.getMessage());
            }
        }, jsonTransformer);


    }

}
