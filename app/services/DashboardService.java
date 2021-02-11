package services;

import static com.mongodb.client.model.Filters.*;

import constants.CollectionNames;
import exceptions.RequestException;
import executors.MongoExecutionContext;
import io.goprime.mongolay.AccessLevelType;
import io.goprime.mongolay.MongoRelay;
import models.Dashboard;
import models.User;
import play.mvc.Http;
import types.UserACL;
import utils.AccessibilityUtil;
import utils.ServiceUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;


@Singleton
public class DashboardService extends BaseService<Dashboard> {

    @Inject
    AccessibilityUtil accessibilityUtil;

    @Inject
    MongoExecutionContext ec;

    public CompletableFuture<List<Dashboard>> getAll(User user) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MongoRelay mongoRelay = new MongoRelay(getMongoDatabase(),user.getUserRoles())
                        .withACL(Dashboard.class,AccessLevelType.READ);
                return mongoRelay.on(Dashboard.class).getCollection().find().into(new ArrayList<>());
            }catch (NullPointerException | IllegalArgumentException e){
                throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST,"invalid_paramaters"));
            }catch (Exception e){
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR,"Service_unavailable"));
            }
        },ec.current());
    }

    public CompletableFuture<Dashboard> create(User user, Dashboard dashboard) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if(user == null || dashboard == null){
                    throw new RequestException(Http.Status.BAD_REQUEST,"Either user or dashboard is empty");
                }
                dashboard.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                dashboard.getWriteACL().add(user.getId().toHexString());
                MongoRelay mongoRelay = new MongoRelay(getMongoDatabase(),user.getUserRoles()).withACL(Dashboard.class,AccessLevelType.WRITE);
                return mongoRelay.on(Dashboard.class).getCollection().insertOrUpdate(dashboard);
            }catch (RequestException e){
                e.printStackTrace();
                throw new CompletionException(e);
            }catch (Exception e){
                e.printStackTrace();
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR,"Service_unavailable"));
            }
        },ec.current());
    }


    public CompletableFuture<Dashboard> update(User user, Dashboard dashboard, String dashboardId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!accessibilityUtil.withACL(user, dashboardId, CollectionNames.DASHBOARD, Dashboard.class, UserACL.WRITE)) {
                    throw new RequestException(Http.Status.UNAUTHORIZED, user.getUsername() + " does not have access to modify this dashboard.Please get");
                }
                return update(dashboard, dashboardId, CollectionNames.DASHBOARD, Dashboard.class);
            } catch (RequestException e) {
                throw new CompletionException(e);
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Service unavailable"));
            }
        },ec.current());
    }

    public CompletableFuture<Dashboard> delete(User user, String dashboardId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!accessibilityUtil.withACL(user, dashboardId, CollectionNames.DASHBOARD, Dashboard.class, UserACL.WRITE)) {
                    throw new RequestException(Http.Status.UNAUTHORIZED, user.getUsername() + " does not have access to delete this dashboard or there isn't any dashboard with this id");
                }
                return delete(dashboardId, CollectionNames.DASHBOARD, Dashboard.class);
            } catch (RequestException e) {
                throw new CompletionException(e);
            } catch (Exception e){
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR,"Service unavailable"));
            }
        },ec.current());
    }

    public CompletableFuture<Dashboard> getDashboardById(User user, String id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!accessibilityUtil.withACL(user, id, CollectionNames.DASHBOARD, Dashboard.class, UserACL.READ) &&
                    !accessibilityUtil.withACL(user, id, CollectionNames.DASHBOARD, Dashboard.class, UserACL.WRITE)) {
                    throw new RequestException(Http.Status.UNAUTHORIZED, user.getUsername() + " does not have access to view this dashboard");
                }
                return findById(id, CollectionNames.DASHBOARD, Dashboard.class);
            } catch (RequestException e) {
                throw new CompletionException(e);
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Service unavailable"));
            }
        },ec.current());

    }

    public CompletableFuture<List<Dashboard>> dashboardHierarchy(User user) {
        return getAll(user).thenCompose(ServiceUtil::hierarchy);
    }

    public CompletableFuture<List<Dashboard>> getChildrenDashboardsHieararchy(User user, String dashboardId) {
        return getAll(user).thenCompose(data -> CompletableFuture.supplyAsync(() -> {
            Dashboard parentDashboard = getDashboardById(user, dashboardId).join();
            return ServiceUtil.hierarchy(parentDashboard, data).getChildren();
        }));
    }

    protected List<Dashboard> publicDashboards() {
        return getCollection(CollectionNames.DASHBOARD, Dashboard.class)
                .find(and(size(CollectionNames.DASHBOARD, 0), size("writeACL", 0)))
                .into(new ArrayList<>());
    }

    private List<Dashboard> privateDashboards(User user) {
        List<String> rolesAndUserId = ServiceUtil.userRoles(user);
        return getCollection(CollectionNames.DASHBOARD, Dashboard.class)
                .find(or(in("readACL", rolesAndUserId), in("writeACL", rolesAndUserId)))
                .into(new ArrayList<>());
    }

}
