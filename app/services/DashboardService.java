package services;

import com.mongodb.client.model.Filters;
import exceptions.RequestException;
import models.Dashboard;
import models.Role;
import models.User;
import play.mvc.Http;
import utils.AccessibilityUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

@Singleton
public class DashboardService extends BaseService<Dashboard> {

    @Inject
    AccessibilityUtil accessibilityUtil;

    public CompletableFuture<List<Dashboard>> getAll(User user) {
            return CompletableFuture.supplyAsync(() -> {
                List<String> rolesAndUserId = user.getRoles().stream().map(Role::getName).collect(Collectors.toList());
                rolesAndUserId.add(user.getId().toHexString());

                List<Dashboard> publicDashboards = getCollection("Dashboard", Dashboard.class)
                        .find(Filters.and(Filters.size("readACL", 0), Filters.size("writeACL", 0)))
                        .into(new ArrayList<>());

                List<Dashboard> privateDashboards = getCollection("Dashboard", Dashboard.class)
                        .find(Filters.or(Filters.in("readACL", rolesAndUserId),Filters.in("writeACL",rolesAndUserId)))
                        .into(new ArrayList<>());
                publicDashboards.addAll(privateDashboards); // merge two lists
                return publicDashboards.stream().distinct().collect(Collectors.toList()); // return List of all dashboards that user has access
            });
    }

    public CompletableFuture<Dashboard> create(User user,Dashboard dashboard) {
        dashboard.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        dashboard.getWriteACL().add(user.getId().toHexString());
        return CompletableFuture.supplyAsync(()-> save(dashboard,"Dashboard",Dashboard.class));
    }


    public CompletableFuture<Dashboard> update(User user, Dashboard dashboard, String dashboardId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!accessibilityUtil.writeACL(user, dashboardId, "Dashboard", Dashboard.class)) {
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
                if (!accessibilityUtil.writeACL(user, dashboardId, "Dashboard", Dashboard.class)) {
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
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!accessibilityUtil.readACL(user, id, "Dashboard", Dashboard.class) || !accessibilityUtil.writeACL(user, id, "Dashboard", Dashboard.class)) {
                    throw new RequestException(Http.Status.UNAUTHORIZED, user.getUsername() + " does not have access to view this dashboard");
                }
                return findById(id,"Dashboard",Dashboard.class);
            }catch (RequestException e){
                throw new CompletionException(e);
            }catch (Exception e){
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR,"Service unavailable"));
            }
        });

    }

    public CompletableFuture<List<Dashboard>> dashboardHierarchy(User user){
        return getAll(user).thenCompose((input)-> CompletableFuture.supplyAsync(()-> hierarchy(input).join()));
    }

    public CompletableFuture<List<Dashboard>> getChildrenDashboardsHieararchy(User user,String dashboardId){
        return CompletableFuture.supplyAsync(() ->{
            List<Dashboard> allDashboards = getAll(user).join();
            Dashboard parentDashboard = getDashboardById(user,dashboardId).join();
            return hierarchy(parentDashboard,allDashboards).getChildren();
        });
    }

    public CompletableFuture<List<Dashboard>> hierarchy(List<Dashboard> input) {
        return CompletableFuture.supplyAsync(()-> {
            if(input == null){
                return new ArrayList<>();
            }
            return input.stream()
                    .filter(x -> x.getParentId() == null)
                    .map(next -> hierarchy(next, input))
                    .collect(Collectors.toList());
        });
    }

    public Dashboard hierarchy(Dashboard dashboard, List<Dashboard> list) {
        List<Dashboard> children = list.stream()
                .filter(el -> dashboard.getId().toHexString().equals(el.getParentId()))
                .map(next -> hierarchy(next, list))
                .collect(Collectors.toList());
        dashboard.setChildren(children);
        return dashboard;
    }

}
