package org.artfable.revolut.test.task.rs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.artfable.revolut.test.task.config.RestController;
import org.artfable.revolut.test.task.model.Currency;
import org.artfable.revolut.test.task.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import java.io.IOException;

import static spark.Spark.*;


/**
 * @author artfable
 * 11.08.18
 */
@RestController
class AccountResource {

    private static final Logger logger = LoggerFactory.getLogger(AccountResource.class);

    private ObjectMapper objectMapper;
    private AccountService accountService;

    @Inject
    private AccountResource(ObjectMapper objectMapper, AccountService accountService) {
        this.objectMapper = objectMapper;
        this.accountService = accountService;

        String userPath = "/user/:userId";
        get(userPath + "/account", this::getAllAccountByUser);
        get("/account/:id", this::getAccount);
        post(userPath + "/account", this::openAccount);
        put( "/account/:id/topUp", this::topUpAccount);
        put( "/account/:id/withdraw", this::withdrawFromAccount);
        delete( "/account/:id", this::deleteAccount);

        put("/account/:id/transfer", this::transfer);

        logger.debug(AccountResource.class.getSimpleName() + " was initialized");
    }

    public String getAllAccountByUser(Request request, Response response) throws JsonProcessingException {
        String userId = request.params("userId");
        return objectMapper.writeValueAsString(accountService.getAllAccountsByUser(Long.parseLong(userId)));
    }

    public String getAccount(Request request, Response response) throws JsonProcessingException {
        Long accountId = Long.parseLong(request.params("id"));

        return objectMapper.writeValueAsString(accountService.getAccount(accountId));
    }

    public String openAccount(Request request, Response response) throws IOException {
        Long userId = Long.parseLong(request.params("userId"));

        Currency currency = Currency.valueOf(getField("currency", request).asText());
        return objectMapper.writeValueAsString(accountService.openAccount(userId, currency));
    }

    public String topUpAccount(Request request, Response response) throws IOException {
        Long accountId = Long.parseLong(request.params("id"));

        return objectMapper.writeValueAsString(accountService.topUpAccount(accountId, getField("amount", request).asDouble()));
    }

    public String withdrawFromAccount(Request request, Response response) throws IOException {
        Long accountId = Long.parseLong(request.params("id"));

        return objectMapper.writeValueAsString(accountService.withdrawFromAccount(accountId, getField("amount", request).asDouble()));
    }

    public boolean deleteAccount(Request request, Response response) {
        Long accountId = Long.parseLong(request.params("id"));

        return accountService.delete(accountId);
    }

    public String transfer(Request request, Response response) throws IOException {
        Long fromAccountId = Long.parseLong(request.params("id"));

        JsonNode jsonNode = objectMapper.readTree(request.body());
        Long toAccountId = getField("account", jsonNode).asLong();
        double amount = getField("amount", jsonNode).asDouble();

        return objectMapper.writeValueAsString(accountService.transfer(fromAccountId, toAccountId, amount));
    }

    private JsonNode getField(String name, Request request) throws IOException {
        return getField(name, objectMapper.readTree(request.body()));
    }

    private JsonNode getField(String name, JsonNode jsonNode) {
        if (jsonNode == null || !jsonNode.has(name)) {
            throw new IllegalArgumentException(name + " should be provided");
        }
        return jsonNode.get(name);
    }
}
