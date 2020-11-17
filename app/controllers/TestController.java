package controllers;

import actions.Authorize;
import actions.Authorized;
import models.Dashboard;
import models.User;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import services.DashboardService;
import services.SerializationService;
import utils.DatabaseUtil;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

@Authorized
public class TestController extends Controller {

    @Inject
    DashboardService dashboardService;

    @Inject
    SerializationService serializationService;

    public CompletableFuture<Result> getAll(Http.Request request){
        User user = request.attrs().get(Authorize.Attrs.USER);
        return dashboardService.getAll(user)
                .thenCompose(res -> serializationService.toJsonNode(res))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtil::throwableToResult);
    }

    public CompletableFuture<Result> create (Http.Request request){
        return serializationService.parseBodyOfType(request, Dashboard.class)
                .thenCompose(data -> dashboardService.create(data))
                .thenCompose(res -> serializationService.toJsonNode(res))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtil::throwableToResult);
    }

    public CompletableFuture<Result> update(Http.Request request,String id){
        User user = request.attrs().get(Authorize.Attrs.USER);
        return serializationService.parseBodyOfType(request,Dashboard.class)
                .thenCompose(data -> dashboardService.update(user,data,id))
                .thenCompose(res -> serializationService.toJsonNode(res))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtil::throwableToResult);
    }

    public CompletableFuture<Result> delete(Http.Request request,String id){
        User user = request.attrs().get(Authorize.Attrs.USER);
        return dashboardService.delete(user,id)
                                .thenCompose(res -> serializationService.toJsonNode(res))
                                .thenApply(Results::ok)
                                .exceptionally(DatabaseUtil::throwableToResult);
    }

}
