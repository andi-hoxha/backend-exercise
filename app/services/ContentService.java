package services;

import com.mongodb.client.model.Filters;
import exceptions.RequestException;
import lombok.extern.slf4j.Slf4j;
import models.Dashboard;
import models.Role;
import models.User;
import models.content.BaseContent;
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

    @Inject
    DashboardService dashboardService;

    public CompletableFuture<List<BaseContent>> getAll(User user, String dashboardId) {
        return CompletableFuture.supplyAsync(() -> {
            List<BaseContent> publicContents = publicContents(dashboardId);
            List<BaseContent> privateContents = privateContents(dashboardId,user);

            publicContents.addAll(privateContents);// merge two lists
            if (publicContents.isEmpty()) {
                return new ArrayList<>();
            }
            return publicContents.stream().distinct().collect(Collectors.toList()); // return List of all dashboards that user has access
        });
    }

    public CompletableFuture<BaseContent> create(User user, BaseContent content, String dashboardId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!ObjectId.isValid(dashboardId)) {
                    throw new RequestException(Http.Status.BAD_REQUEST, "Dashboard id is not a valid id");
                }
                if (dashboardService.publicDashboards().stream().noneMatch(x-> x.getId().toHexString().equals(dashboardId)) ||
                    (!accessibilityUtil.withACL(user, dashboardId, "Dashboard", Dashboard.class, UserACL.READ) ||
                    !accessibilityUtil.withACL(user, dashboardId, "Dashboard", Dashboard.class,UserACL.WRITE))) {
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

    public CompletableFuture<BaseContent> update(BaseContent content,String dashboardId,String contentId, User user) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (publicContents(dashboardId).stream().noneMatch(x -> x.getId().toHexString().equals(contentId)) ||
                    !accessibilityUtil.withACL(user, contentId, "Content", BaseContent.class,UserACL.WRITE)) {
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

    public CompletableFuture<BaseContent> delete(User user,String dashboardId,String contentId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (publicContents(dashboardId).stream().noneMatch(x -> x.getId().toHexString().equals(contentId)) ||
                    !accessibilityUtil.withACL(user, contentId, "Content", BaseContent.class,UserACL.WRITE)) {
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
                if (!accessibilityUtil.withACL(user, id, "Content", BaseContent.class,UserACL.READ)) {
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
                .find(Filters.and(Filters.eq("dashboardId", new ObjectId(dashboardId)),
                        (Filters.and(Filters.size("readACL", 0), Filters.size("writeACL", 0)))))
                .into(new ArrayList<>());
    }

    private List<BaseContent> privateContents(String dashboardId,User user){
        List<String> rolesAndUserId = user.getRoles().stream().map(Role::getName).collect(Collectors.toList());
        rolesAndUserId.add(user.getId().toHexString());

        return getCollection("Content", BaseContent.class)
                .find(Filters.and(Filters.eq("dashboardId", new ObjectId(dashboardId)),
                        Filters.or(Filters.in("readACL", rolesAndUserId), Filters.in("writeACL", rolesAndUserId))
                )).into(new ArrayList<>());
    }
}
