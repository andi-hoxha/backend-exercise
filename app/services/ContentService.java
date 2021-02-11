package services;

import static com.mongodb.client.model.Filters.*;

import com.fasterxml.jackson.databind.JsonNode;
import constants.CollectionNames;
import exceptions.RequestException;
import executors.MongoExecutionContext;
import lombok.extern.slf4j.Slf4j;
import models.Dashboard;
import models.User;
import models.content.BaseContent;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Http;
import types.UserACL;
import utils.AccessibilityUtil;
import utils.ServiceUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Singleton
@Slf4j
public class ContentService extends BaseService<BaseContent> {

    @Inject
    AccessibilityUtil accessibilityUtil;

    @Inject
    MongoExecutionContext ec;

    public CompletableFuture<List<BaseContent>> getAll(User user, String dashboardId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if(user == null || !ObjectId.isValid(dashboardId)){
                    throw new RequestException(Http.Status.BAD_REQUEST,"Either user or dashboardId is invalid");
                }
                List<String> rolesAndUserId = ServiceUtil.userRoles(user);
                Bson filter = or(
                        (and(eq("dashboardId", new ObjectId(dashboardId)), (and(size("readACL", 0), size("writeACL", 0))))),
                        (and(eq("dashboardId", new ObjectId(dashboardId)),
                                or(in("readACL", rolesAndUserId), in("writeACL", rolesAndUserId))))
                );
                List<BaseContent> contents = findMany(CollectionNames.CONTENT, filter, BaseContent.class);
                if (contents == null) {
                    return new ArrayList<>();
                }
                return contents;
            }catch (RequestException e){
                throw new CompletionException(e);
            }catch (Exception e){
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR,"Service unavailable"));
            }
        },ec.current());
    }

    public CompletableFuture<BaseContent> createContent(User user, BaseContent content, String dashboardId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!accessibilityUtil.withACL(user, dashboardId, CollectionNames.DASHBOARD, Dashboard.class,UserACL.WRITE)) {
                    throw new RequestException(Http.Status.UNAUTHORIZED, user.getUsername() + " does not have access to read or write content in this dashboard: " + dashboardId);
                }
                content.getWriteACL().add(user.getId().toHexString());
                content.setDashboardId(new ObjectId(dashboardId));

                 return save(content, CollectionNames.CONTENT, BaseContent.class);
            } catch (RequestException e) {
                throw new CompletionException(e);
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Service unavailable"));
            }
        },ec.current());
    }

    public CompletableFuture<BaseContent> updateContent(BaseContent content,String contentId, User user) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!accessibilityUtil.withACL(user, contentId, CollectionNames.CONTENT, BaseContent.class,UserACL.WRITE)) {
                    throw new RequestException(Http.Status.UNAUTHORIZED, user.getUsername() + " does not have access to modify this content.Please get");
                }
                return update(content, contentId, "Content", BaseContent.class);
            } catch (RequestException e) {
                throw new CompletionException(e);
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Service unavailable"));
            }
        },ec.current());
    }

    public CompletableFuture<BaseContent> deleteContent(User user,String contentId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!accessibilityUtil.withACL(user, contentId, CollectionNames.CONTENT, BaseContent.class,UserACL.WRITE)) {
                    throw new RequestException(Http.Status.UNAUTHORIZED, user.getUsername() + " does not have access to delete this content");
                }
                return delete(contentId, "Content", BaseContent.class);
            } catch (RequestException e) {
                throw new CompletionException(e);
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Service unavailable"));
            }
        },ec.current());
    }

    public CompletableFuture<BaseContent> getContentById(User user, String id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!accessibilityUtil.withACL(user, id, CollectionNames.CONTENT, BaseContent.class,UserACL.READ) &&
                    !accessibilityUtil.withACL(user,id,CollectionNames.CONTENT,BaseContent.class,UserACL.WRITE)) {
                    throw new RequestException(Http.Status.UNAUTHORIZED, user.getUsername() + " does not have access to view this dashboard");
                }
                return findById(id, "Content", BaseContent.class);
            } catch (RequestException e) {
                throw new CompletionException(e);
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Service unavailable"));
            }
        },ec.current());
    }

    private List<BaseContent> publicContents(String dashboardId){
        return getCollection(CollectionNames.CONTENT, BaseContent.class)
                .find(and(eq("dashboardId", new ObjectId(dashboardId)),
                        (and(size("readACL", 0), size("writeACL", 0)))))
                .into(new ArrayList<>());
    }

    private List<BaseContent> privateContents(String dashboardId,User user){
        List<String> rolesAndUserId = ServiceUtil.userRoles(user);
        return getCollection(CollectionNames.CONTENT, BaseContent.class)
                .find(and(eq("dashboardId", new ObjectId(dashboardId)),
                        or(in("readACL", rolesAndUserId),in("writeACL", rolesAndUserId))
                )).into(new ArrayList<>());
    }
}
