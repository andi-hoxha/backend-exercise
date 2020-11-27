package services;

import static com.mongodb.client.model.Filters.*;

import exceptions.RequestException;
import lombok.extern.slf4j.Slf4j;
import models.Dashboard;
import models.Role;
import models.User;
import models.content.BaseContent;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import play.mvc.Http;
import types.UserACL;
import utils.AccessibilityUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

@Singleton
@Slf4j
public class ContentService extends BaseService<BaseContent> {

    @Inject
    AccessibilityUtil accessibilityUtil;

    public CompletableFuture<List<BaseContent>> getAll(User user, String dashboardId) {
        return CompletableFuture.supplyAsync(() -> {
            List<String> rolesAndUserId = user.getRoles().stream().map(Role::getName).collect(Collectors.toList());
            rolesAndUserId.add(user.getId().toHexString());
            Bson filter = or(
                    (and(eq("dashboardId", new ObjectId(dashboardId)),(and(size("readACL", 0), size("writeACL", 0))))),
                    (and(eq("dashboardId", new ObjectId(dashboardId)),
                            or(in("readACL", rolesAndUserId),in("writeACL", rolesAndUserId))))
                            );
            List<BaseContent> contents = findMany("Content",filter,BaseContent.class);
            if(contents == null){
                return new ArrayList<>();
            }
            return contents;
        });
    }

    public CompletableFuture<BaseContent> createContent(User user, BaseContent content, String dashboardId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!ObjectId.isValid(dashboardId)) {
                    throw new RequestException(Http.Status.BAD_REQUEST, "Dashboard id is not a valid id");
                }
                if (!accessibilityUtil.withACL(user, dashboardId, "Dashboard", Dashboard.class,UserACL.WRITE)) {
                    throw new RequestException(Http.Status.UNAUTHORIZED, user.getUsername() + " does not have access to read or write content in this dashboard: " + dashboardId);
                }
                content.setWriteACL(Arrays.asList(user.getId().toHexString()));
                content.setDashboardId(new ObjectId(dashboardId));

                return save(content, "Content", BaseContent.class);
            } catch (RequestException e) {
                throw new CompletionException(e);
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Service unavailable"));
            }
        });
    }

    public CompletableFuture<BaseContent> updateContent(BaseContent content,String contentId, User user) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!accessibilityUtil.withACL(user, contentId, "Content", BaseContent.class,UserACL.WRITE)) {
                    throw new RequestException(Http.Status.UNAUTHORIZED, user.getUsername() + " does not have access to modify this content.Please get");
                }
                return update(content, contentId, "Content", BaseContent.class);
            } catch (RequestException e) {
                throw new CompletionException(e);
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Service unavailable"));
            }
        });
    }

    public CompletableFuture<BaseContent> deleteContent(User user,String contentId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!accessibilityUtil.withACL(user, contentId, "Content", BaseContent.class,UserACL.WRITE)) {
                    throw new RequestException(Http.Status.UNAUTHORIZED, user.getUsername() + " does not have access to delete this content");
                }
                return delete(contentId, "Content", BaseContent.class);
            } catch (RequestException e) {
                throw new CompletionException(e);
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Service unavailable"));
            }
        });
    }

    public CompletableFuture<BaseContent> getContentById(User user, String id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!accessibilityUtil.withACL(user, id, "Content", BaseContent.class,UserACL.READ) &&
                    !accessibilityUtil.withACL(user,id,"Content",BaseContent.class,UserACL.WRITE)) {
                    throw new RequestException(Http.Status.UNAUTHORIZED, user.getUsername() + " does not have access to view this dashboard");
                }
                return findById(id, "Content", BaseContent.class);
            } catch (RequestException e) {
                throw new CompletionException(e);
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Service unavailable"));
            }
        });
    }

    private List<BaseContent> publicContents(String dashboardId){
        return getCollection("Content", BaseContent.class)
                .find(and(eq("dashboardId", new ObjectId(dashboardId)),
                        (and(size("readACL", 0), size("writeACL", 0)))))
                .into(new ArrayList<>());
    }

    private List<BaseContent> privateContents(String dashboardId,User user){
        List<String> rolesAndUserId = user.getRoles().stream().map(Role::getName).collect(Collectors.toList());
        rolesAndUserId.add(user.getId().toHexString());

        return getCollection("Content", BaseContent.class)
                .find(and(eq("dashboardId", new ObjectId(dashboardId)),
                        or(in("readACL", rolesAndUserId),in("writeACL", rolesAndUserId))
                )).into(new ArrayList<>());
    }
}
