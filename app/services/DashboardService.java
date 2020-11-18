package services;

import com.mongodb.client.model.Filters;
import exceptions.RequestException;
import models.Dashboard;
import models.Role;
import models.User;
import play.mvc.Http;
import utils.AccessibilityUtil;
import javax.inject.Singleton;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

@Singleton
public class DashboardService extends BaseService<Dashboard> {

    public CompletableFuture<List<Dashboard>> getAll(User user) {
            return CompletableFuture.supplyAsync(() -> {
                List<Dashboard> publicDashboards = getCollection("Dashboard", Dashboard.class).find(Filters.or(Filters.size("readACL", 0), Filters.size("writeACL", 0))).into(new ArrayList<>());
                List<String> rolesAndUserId = user.getRoles().stream().map(Role::getName).collect(Collectors.toList());
                rolesAndUserId.add(user.getId().toHexString());
                List<Dashboard> privateDashboards = getCollection("Dashboard", Dashboard.class).find(Filters.in("readACL", rolesAndUserId)).into(new ArrayList<>());

                publicDashboards.addAll(privateDashboards); // merge two lists
                return publicDashboards.stream().distinct().collect(Collectors.toList()); // return List of all dashboards that user has access
            });
    }

    public CompletableFuture<Dashboard> create(Dashboard dashboard) {
        dashboard.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        return CompletableFuture.supplyAsync(()-> save(dashboard,"Dashboard",Dashboard.class));
    }


    public CompletableFuture<Dashboard> update(User user, Dashboard dashboard, String dashboardId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!AccessibilityUtil.writeACL(user, dashboardId, "Dashboard", Dashboard.class)) {
                    throw new RequestException(Http.Status.UNAUTHORIZED, user.getUsername() + " does not have access to modify this dashboard.Please get");
                }
                return update(dashboard,dashboardId,"Dashboard",Dashboard.class);
            } catch (RequestException e) {
                throw new CompletionException(e);
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Service unavailable"));
            }
        });
    }

    public CompletableFuture<Dashboard> delete(User user, String dashboardId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!AccessibilityUtil.writeACL(user, dashboardId, "Dashboard", Dashboard.class)) {
                    throw new RequestException(Http.Status.UNAUTHORIZED, user.getUsername() + " does not have access to delete this dashboard");
                }
                return delete(dashboardId,"Dashboard",Dashboard.class);
            } catch (RequestException e) {
                throw new CompletionException(e);
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Service unavailable"));
            }
        });
    }

    public CompletableFuture<Dashboard> getDashboardById(User user,String id) {
        try {
            if (!AccessibilityUtil.readACL(user, id, "Dashboard", Dashboard.class)) {
                throw new RequestException(Http.Status.UNAUTHORIZED, user.getUsername() + " does not have access to view this dashboard");
            }
            return CompletableFuture.supplyAsync(() -> findById(id, "Dashboard", Dashboard.class));
        }catch (RequestException e){
            throw new CompletionException(e);
        }catch (Exception e){
            throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR,"Service unavailable"));
        }
    }

    public CompletableFuture<List<Dashboard>> hierarchy(User user) {
        return getAll(user).thenCompose(input -> CompletableFuture.supplyAsync(() -> input.stream()
                .filter(x -> x.getParentId() == null)
                .map(next -> hierarchy(next, input))
                .collect(Collectors.toList())));
    }

    private Dashboard hierarchy(Dashboard dashboard, List<Dashboard> list) {
        List<Dashboard> children = list.stream()
                .filter(el -> dashboard.getId().toHexString().equals(el.getParentId()))
                .map(next -> hierarchy(next, list))
                .collect(Collectors.toList());
        dashboard.setChildren(children);
        return dashboard;
    }

}
