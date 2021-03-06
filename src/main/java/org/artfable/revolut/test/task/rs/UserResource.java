package org.artfable.revolut.test.task.rs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.artfable.revolut.test.task.config.rs.RestController;
import org.artfable.revolut.test.task.model.User;
import org.artfable.revolut.test.task.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import static spark.Spark.*;

/**
 * REST endpoint for {@link User}. It is needed only for grouping accounts.
 *
 * @author artfable
 * 12.08.18
 */
@RestController
class UserResource {

    private static final Logger logger = LoggerFactory.getLogger(UserResource.class);

    private ObjectMapper objectMapper;
    private UserService userService;

    @Inject
    public UserResource(ObjectMapper objectMapper, UserService userService) {
        this.objectMapper = objectMapper;
        this.userService = userService;
    }

    public static void init(Injector injector) {
        // each time should be provided new instance for a request.
        get("/user", (request, response) -> injector.getInstance(UserResource.class).getAllUsers(request, response));
        get("/user/:id", (request, response) -> injector.getInstance(UserResource.class).getUser(request, response));
        post("/user", (request, response) -> injector.getInstance(UserResource.class).createNewUser(request, response));
        delete("/user/:id", (request, response) -> injector.getInstance(UserResource.class).deleteUser(request, response));

        logger.debug(UserResource.class.getSimpleName() + " was initialized");
    }

    public String getAllUsers(Request request, Response response) throws JsonProcessingException {
        return objectMapper.writeValueAsString(userService.getAllUsers());
    }

    public String getUser(Request request, Response response) throws JsonProcessingException {
        return objectMapper.writeValueAsString(userService.getUser(Long.parseLong(request.params("id"))));
    }

    public String createNewUser(Request request, Response response) throws JsonProcessingException {
        return objectMapper.writeValueAsString(userService.createNewUser());
    }

    public boolean deleteUser(Request request, Response response) {
        long id = Long.parseLong(request.params("id"));
        return userService.deleteUser(id);
    }
}
