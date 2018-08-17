package org.artfable.revolut.test.task.rs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.artfable.revolut.test.task.config.rs.RestController;
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
    public AccountResource(ObjectMapper objectMapper, AccountService accountService) {
        this.objectMapper = objectMapper;
        this.accountService = accountService;
    }

    public static void init(Injector injector) {
        String userPath = "/user/:userId";
        get(userPath + "/account", (request, response) -> injector.getInstance(AccountResource.class).getAllAccountByUser(request, response));
        get("/account/:id", (request, response) -> injector.getInstance(AccountResource.class).getAccount(request, response));
        post(userPath + "/account", (request, response) -> injector.getInstance(AccountResource.class).openAccount(request, response));
        put("/account/:id/topUp", (request, response) -> injector.getInstance(AccountResource.class).topUpAccount(request, response));
        put("/account/:id/withdraw", (request, response) -> injector.getInstance(AccountResource.class).withdrawFromAccount(request, response));
        delete("/account/:id", (request, response) -> injector.getInstance(AccountResource.class).deleteAccount(request, response));

        put("/account/:id/transfer", (request, response) -> injector.getInstance(AccountResource.class).transfer(request, response));

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
