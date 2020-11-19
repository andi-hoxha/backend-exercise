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
            List<BaseContent> publicContents = getCollection("Content", BaseContent.class)
                    .find(Filters.and(Filters.eq("dashboardId", new ObjectId(dashboardId)),
                            (Filters.and(Filters.size("readACL", 0), Filters.size("writeACL", 0)))))
                    .into(new ArrayList<>());

            List<String> rolesAndUserId = user.getRoles().stream().map(Role::getName).collect(Collectors.toList());
            rolesAndUserId.add(user.getId().toHexString());

            List<BaseContent> privateContents = getCollection("Content", BaseContent.class)
                    .find(Filters.and(Filters.eq("dashboardId", new ObjectId(dashboardId)),
                            Filters.or(Filters.in("readACL", rolesAndUserId), Filters.in("writeACL", rolesAndUserId))
                    )).into(new ArrayList<>());

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
                if (!accessibilityUtil.readACL(user, dashboardId, "Dashboard", Dashboard.class) || !accessibilityUtil.writeACL(user, dashboardId, "Dashboard", Dashboard.class)) {
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

    public CompletableFuture<BaseContent> update(BaseContent content, String id, User user) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!accessibilityUtil.writeACL(user, id, "Content", BaseContent.class)) {
                    throw new RequestException(Http.Status.UNAUTHORIZED, user.getUsername() + " does not have access to modify this content.Please get");
                }
                return update(content, id, "Content", BaseContent.class);
            } catch (RequestException e) {
                throw new CompletionException(e);
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Service unavailable"));
            }
        });
    }

    public CompletableFuture<BaseContent> delete(User user, String contentId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!accessibilityUtil.writeACL(user, contentId, "Content", BaseContent.class)) {
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
                if (!accessibilityUtil.readACL(user, id, "Content", BaseContent.class)) {
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
}
