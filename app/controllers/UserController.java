package controllers;

import com.google.inject.Inject;
import models.User;
import play.mvc.*;
import services.SerializationService;
import services.UserService;
import utils.DatabaseUtil;

import java.util.concurrent.CompletableFuture;

public class UserController extends Controller {

    @Inject
    UserService userService;
    @Inject
    SerializationService serializationService;

    public CompletableFuture<Result> saveUsers(Http.Request request) {
            return serializationService.parseListBodyOfType(request,User.class)
                   .thenCompose(users -> userService.setup(users))
                    .thenApply(Results::ok)
                    .exceptionally(DatabaseUtil::throwableToResult);
    }
}