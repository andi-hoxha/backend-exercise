package controllers;

import actions.Authorize;
import actions.Authorized;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Dashboard;
import models.User;
import models.content.BaseContent;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import services.ContentService;
import services.DashboardService;
import services.SerializationService;
import utils.DatabaseUtil;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Authorized
public class ContentController extends Controller {

    @Inject
    ContentService contentService;

    @Inject
    DashboardService dashboardService;

    @Inject
    SerializationService serializationService;

    public CompletableFuture<Result> getAll(Http.Request request,String dashboardId){
        User user = request.attrs().get(Authorize.Attrs.USER);
        return contentService.getAll(user,dashboardId)
                .thenCompose(data -> serializationService.toJsonNode(data))
                .thenCompose(res -> CompletableFuture.supplyAsync(()->{
                    List<Dashboard> children = dashboardService.getChildrenDashboardsHieararchy(user,dashboardId).join();
                    ObjectNode node = Json.newObject();
                    node.put("dashboards",Json.toJson(children));
                    node.put("contents",res);
                    return node;
                }))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtil::throwableToResult);
    }

    public CompletableFuture<Result> createContent(Http.Request request, String dashboardId){
        User user = request.attrs().get(Authorize.Attrs.USER);
        return serializationService.parseBodyOfType(request, BaseContent.class)
                .thenCompose(res -> contentService.createContent(user,res,dashboardId))
                .thenCompose(data -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtil::throwableToResult);
    }

    public CompletableFuture<Result> updateContent(Http.Request request,String dashboardId,String contentId){
        User user = request.attrs().get(Authorize.Attrs.USER);
        return serializationService.parseBodyOfType(request,BaseContent.class)
                .thenCompose(data -> contentService.updateContent(data,contentId,user))
                .thenCompose(res -> serializationService.toJsonNode(res))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtil::throwableToResult);
    }

    public CompletableFuture<Result> deleteContent(Http.Request request,String dashboardId,String contentId){
        User user = request.attrs().get(Authorize.Attrs.USER);
        return contentService.deleteContent(user,contentId)
                .thenCompose(res -> serializationService.toJsonNode(res))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtil::throwableToResult);
    }

}
