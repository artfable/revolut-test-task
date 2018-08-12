package org.artfable.revolut.test.task;

import com.despegar.http.client.HttpResponse;
import com.despegar.sparkjava.test.SparkServer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.artfable.revolut.test.task.model.Account;
import org.artfable.revolut.test.task.model.Currency;
import org.artfable.revolut.test.task.model.User;
import org.junit.ClassRule;
import org.junit.Test;
import spark.servlet.SparkApplication;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author artfable
 * 12.08.18
 */
public class IntegrationTest {

    public static class TestApplication implements SparkApplication {
        @Override
        public void init() {
            ApplicationInitializer.main(new String[]{});
        }
    }

    private ObjectMapper objectMapper = new ObjectMapper();

    @ClassRule
    public static SparkServer<TestApplication> testServer = new SparkServer<>(TestApplication.class, 8080);

    @Test
    public void transferTest() throws Exception {
        HttpResponse response = testServer.execute(testServer.post("/user", "", false));

        assertEquals(200, response.code());

        Long userId1 = objectMapper.readValue(response.body(), User.class).getId();

        response = testServer.execute(testServer.post("/user", "", false));
        Long userId2 = objectMapper.readValue(response.body(), User.class).getId();

        response = testServer.execute(testServer.post("/user/" + userId1 + "/account", "{\"currency\": \"EUR\"}", false));
        Long accountId1 = objectMapper.readValue(response.body(), Account.class).getId();

        response = testServer.execute(testServer.post("/user/" + userId2 + "/account", "{\"currency\": \"EUR\"}", false));
        Long accountId2 = objectMapper.readValue(response.body(), Account.class).getId();

        testServer.execute(testServer.put("/account/" + accountId1 + "/topUp", "{\"amount\": 100.25}", false));

        response = testServer.execute(testServer.put("/account/" + accountId1 + "/transfer", "{\"account\": " + accountId2 + ", \"amount\": \"10.10\"}", false));

        assertEquals(200, response.code());
        List<Account> accounts = objectMapper.readValue(response.body(), new TypeReference<List<Account>>() {});
        assertEquals(2, accounts.size());
        assertEquals(BigDecimal.valueOf(90.15), accounts.get(0).getAmount());
        assertEquals(BigDecimal.valueOf(10.10).setScale(2), accounts.get(1).getAmount());

        response = testServer.execute(testServer.get("/account/" + accountId1, false));

        assertEquals(200, response.code());
        Account account = objectMapper.readValue(response.body(), Account.class);
        assertEquals(1L, (long) account.getId());
        assertEquals(1L, (long) account.getUserId());
        assertEquals(Currency.EUR, account.getCurrency());
        assertEquals(BigDecimal.valueOf(90.15), account.getAmount());

        response = testServer.execute(testServer.get("/account/" + accountId2, false));

        assertEquals(200, response.code());
        account = objectMapper.readValue(response.body(), Account.class);
        assertEquals(2L, (long) account.getId());
        assertEquals(2L, (long) account.getUserId());
        assertEquals(Currency.EUR, account.getCurrency());
        assertEquals(BigDecimal.valueOf(10.10).setScale(2), account.getAmount());
    }

    @Test
    public void transferExchangeTest() throws Exception {
        HttpResponse response = testServer.execute(testServer.post("/user", "", false));

        User user = objectMapper.readValue(response.body(), User.class);

        assertNotNull(user);

        response = testServer.execute(testServer.post("/user/" + user.getId() + "/account", "{\"currency\": \"EUR\"}", false));
        Account first = objectMapper.readValue(response.body(), Account.class);

        response = testServer.execute(testServer.post("/user/" + user.getId() + "/account", "{\"currency\": \"USD\"}", false));
        Account second = objectMapper.readValue(response.body(), Account.class);

        testServer.execute(testServer.put("/account/" + first.getId() + "/topUp", "{\"amount\": 100.24}", false));

        response = testServer.execute(testServer.put("/account/" + first.getId() + "/transfer", "{\"account\": " + second.getId() + ", \"amount\": \"10\"}", false));
        assertEquals(200, response.code());
        List<Account> accounts = objectMapper.readValue(response.body(), new TypeReference<List<Account>>() {});
        assertEquals(2, accounts.size());
        assertEquals(BigDecimal.valueOf(90.24), accounts.get(0).getAmount());
        assertEquals(BigDecimal.valueOf(11).setScale(2), accounts.get(1).getAmount());
    }
}
