package actions;

import com.google.common.base.Strings;
import com.mongodb.client.model.Filters;
import exceptions.RequestException;
import lombok.SneakyThrows;
import models.BaseModel;
import models.User;
import mongo.IMongoDB;
import org.bson.types.ObjectId;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class Accessibilty extends Action<AccessControl> {

    @Inject
    IMongoDB mongoDB;

    @SneakyThrows
    @Override
    public CompletionStage<Result> call(Http.Request req) {
        final String COLLECTION_NAME = configuration.collectionName();
        final Class<? extends BaseModel> OBJECT_CLASS = configuration.value();

        User user = req.attrs().get(Authorize.Attrs.USER);
        List<String> roles = user.getRoles().stream().map(BaseModel::getId).map(ObjectId::toHexString).collect(Collectors.toList());
        roles.add(user.getId().toHexString());

        List<String> uri = Arrays.stream(req.uri().split("/")).collect(Collectors.toList());
        Optional<String> resourceId = uri.stream().filter(x -> x.length() == 24).findAny();
        if (!resourceId.isPresent()) {
            throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, "There is no id inside of request URI"));
        }
        parametersCheck(user,resourceId.get(),COLLECTION_NAME,OBJECT_CLASS);

        Object resource;
        if (configuration.key().equalsIgnoreCase("Read")) {
            resource = mongoDB.getMongoDatabase().getCollection(COLLECTION_NAME, OBJECT_CLASS)
                    .find(Filters.or(Filters.and(Filters.eq("_id", new ObjectId(resourceId.get())), Filters.in("readACL", roles)),
                            Filters.and(Filters.eq("_id", new ObjectId(resourceId.get())), (Filters.or(Filters.size("readACL", 0), Filters.size("writeACL", 0)))))).
                            first();
            if (resource != null) {
                return delegate.call(req);
            }
            return CompletableFuture.completedFuture(forbidden("You are not authorized to read this resource"));
        } else if (configuration.key().equalsIgnoreCase("Write")) {
            resource = mongoDB.getMongoDatabase().getCollection(COLLECTION_NAME, OBJECT_CLASS)
                    .find(Filters.or(Filters.and(Filters.eq("_id", new ObjectId(resourceId.get())), Filters.in("writeACL", roles)),
                            Filters.and(Filters.eq("_id", new ObjectId(resourceId.get())), (Filters.or(Filters.size("readACL", 0), Filters.size("writeACL", 0)))))).
                            first();
            if (resource != null) {
                return delegate.call(req);
            }
            return CompletableFuture.completedFuture(forbidden("You are not authorized to write(edit) this resource"));
        }
        return CompletableFuture.completedFuture(internalServerError("Service unavailable"));
    }

    public <T> CompletionStage<Result> parametersCheck(User user, String resourceId, String collectionName, Class<T> objectClass) throws RequestException {
        if (user == null) {
            return CompletableFuture.completedFuture(forbidden("You are not authorized to write(edit) this resource"));
        }
        if (!ObjectId.isValid(resourceId) || Strings.isNullOrEmpty(collectionName)) {
            return CompletableFuture.completedFuture(forbidden("You are not authorized to write(edit) this resource"));
        }
        if (objectClass == null) {
            return CompletableFuture.completedFuture(forbidden("You are not authorized to write(edit) this resource"));
        }
        return CompletableFuture.completedFuture(internalServerError("Service unavailable"));
    }

}
