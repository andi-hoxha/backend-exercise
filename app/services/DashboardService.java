package services;

import actions.Accessibilty;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.client.model.Filters;
import de.flapdoodle.embed.process.collections.Collections;
import exceptions.RequestException;
import models.Dashboard;
import models.Role;
import models.User;
import play.cache.AsyncCacheApi;
import play.cache.NamedCache;
import play.libs.Json;
import play.mvc.Http;
import utils.AccessibilityUtil;
import utils.CacheUtil;
import utils.ConverterUtil;
import utils.DatabaseUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

@Singleton
public class DashboardService extends BaseService<Dashboard> {

    @Inject
    @NamedCache("exercise")
    AsyncCacheApi cacheApi;

    public CompletableFuture<List<Dashboard>> getAll(User user) {
        if(CacheUtil.findDataInCache(cacheApi,"Dashboards") != null){
            return getDashboardFromCache();
        }else {
            return CompletableFuture.supplyAsync(() -> {
                List<Dashboard> publicDashboards = getCollection("Dashboard", Dashboard.class).find(Filters.or(Filters.size("readACL", 0), Filters.size("writeACL", 0))).into(new ArrayList<>());
                List<String> rolesAndUserId = user.getRoles().stream().map(Role::getName).collect(Collectors.toList());
                rolesAndUserId.add(user.getId().toHexString());
                List<Dashboard> privateDashboards = getCollection("Dashboard", Dashboard.class).find(Filters.in("readACL", rolesAndUserId)).into(new ArrayList<>());

                publicDashboards.addAll(privateDashboards); // merge two lists
                List<Dashboard> allDashboards = publicDashboards.stream().distinct().collect(Collectors.toList());// List of dashboards without duplicates
                CacheUtil.putInCache(cacheApi, "Dashboards", allDashboards);
                return allDashboards;
            });
        }
    }

    public CompletableFuture<Dashboard> create(Dashboard dashboard) {
        dashboard.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        return CompletableFuture.supplyAsync(()->{
            Dashboard createdDashboard;
            if(CacheUtil.findDataInCache(cacheApi,"Dashboards") == null){
                createdDashboard = save(dashboard,"Dashboard",Dashboard.class);
                CacheUtil.putInCache(cacheApi,"Dashboards",Collections.newArrayList(createdDashboard));
            }else {
                createdDashboard = save(dashboard, "Dashboard", Dashboard.class);
                List<Dashboard> cachedDashboards = getDashboardFromCache().join();
                if (!cachedDashboards.contains(dashboard)) {
                    cachedDashboards.add(createdDashboard);
                }
                CacheUtil.putInCache(cacheApi, "Dashboards", cachedDashboards);
            }
            return createdDashboard;
        });
    }


    public CompletableFuture<Dashboard> update(User user, Dashboard dashboard, String dashboardId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!AccessibilityUtil.writeACL(user, dashboardId, "Dashboard", Dashboard.class)) {
                    throw new RequestException(Http.Status.UNAUTHORIZED, user.getUsername() + " does not have access to modify this dashboard.Please get");
                }
                List<Dashboard> cachedDashboards = getDashboardFromCache().join();
                Dashboard updatedDashboard = update(dashboard, dashboardId, "Dashboard", Dashboard.class);
                Optional<Dashboard> dashboardOptional = cachedDashboards.stream().filter(x-> x.getId().equals(updatedDashboard.getId())).findAny();
                if(dashboardOptional.isPresent()){
                    int index = cachedDashboards.indexOf(dashboardOptional.get());
                    cachedDashboards.set(index,updatedDashboard);
                }
                cachedDashboards.add(updatedDashboard);
                return updatedDashboard;
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
                Dashboard deletedDashboard = delete(dashboardId, "Dashboard", Dashboard.class);
                List<Dashboard> cachedDashboards = getDashboardFromCache().join();
                if(!cachedDashboards.contains(deletedDashboard)){
                    return deletedDashboard;
                }
                cachedDashboards.remove(deletedDashboard);
                return deletedDashboard;
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
                if (!AccessibilityUtil.readACL(user, id, "Dashboard", Dashboard.class)) {
                    throw new RequestException(Http.Status.UNAUTHORIZED, user.getUsername() + " does not have access to view this dashboard");
                }
               return findById(id, "Dashboard", Dashboard.class);
            }catch (RequestException e){
                throw new CompletionException(e);
            }catch (Exception e){
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR,"Service unavailable"));
            }
        });
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

    private CompletableFuture<List<Dashboard>> getDashboardFromCache(){
        return CompletableFuture.supplyAsync(() -> {
            JsonNode cachedList = Json.toJson(CacheUtil.findDataInCache(cacheApi,"Dashboards"));
            return ConverterUtil.jsonNodeToList(cachedList,Dashboard.class);
        });
    }
}
