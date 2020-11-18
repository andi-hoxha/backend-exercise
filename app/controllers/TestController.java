package controllers;

import actions.Authorize;
import actions.Authorized;
import com.mongodb.client.model.Filters;
import models.BaseModel;
import models.Dashboard;
import models.User;
import mongo.IMongoDB;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import services.DashboardService;
import services.SerializationService;
import utils.DatabaseUtil;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Authorized
public class TestController extends Controller {

    @Inject
    DashboardService dashboardService;

    @Inject
    SerializationService serializationService;

    @Inject
    IMongoDB mongoDB;

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


    public CompletableFuture<Result> getId(Http.Request request,String id){
        User user = request.attrs().get(Authorize.Attrs.USER);
        List<String> roles = user.getRoles().stream().map(BaseModel::getId).map(ObjectId::toHexString).collect(Collectors.toList());
        roles.add(user.getId().toHexString());
       Dashboard dashboard =  mongoDB.getMongoDatabase().getCollection("Dashboard",Dashboard.class)
                .find(Filters.or(Filters.and(Filters.eq("_id",new ObjectId(id)),Filters.in("readACL",roles)),
                        Filters.and(Filters.eq("_id",new ObjectId(id)),(Filters.or(Filters.size("readACL",0),Filters.size("writeACL",0)))))).
                        first();
        if(dashboard == null) {
            return CompletableFuture.completedFuture(notFound("Not found"));
        }
        return CompletableFuture.supplyAsync(()-> ok(Json.toJson(dashboard)));
    }
}
