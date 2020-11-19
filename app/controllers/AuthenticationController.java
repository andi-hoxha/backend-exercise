package controllers;

import models.requests.AuthRequestModel;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import services.AuthService;
import services.SerializationService;
import utils.DatabaseUtil;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

public class AuthenticationController extends Controller {

    @Inject
    AuthService authService;

    @Inject
    SerializationService serializationService;

    public CompletableFuture<Result> authenticate(Http.Request request){
        return serializationService.parseBodyOfType(request, AuthRequestModel.class)
                .thenCompose(data -> authService.login(data.getUsername(),data.getPassword()))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtil::throwableToResult);
    }
}
