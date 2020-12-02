package actions;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import static com.mongodb.client.model.Filters.*;
import constants.JwtConstants;
import exceptions.RequestException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import models.User;
import mongo.IMongoDB;
import org.bson.types.ObjectId;
import play.Logger;
import play.libs.typedmap.TypedKey;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import utils.JwtUtil;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

public class Authorize extends Action<Authorized> {
    @Inject
    IMongoDB mongoDB;

    @Override
    public CompletionStage<Result> call(Http.Request req){
        Optional<String> header = req.header(JwtConstants.HEADER_KEY);
        if(!header.isPresent() || !header.get().startsWith(JwtConstants.TOKEN_PREFIX)){
            return CompletableFuture.completedFuture(unauthorized("Either header key or token prefix is missing.You are not authorized! Please login again."));
        }
        String token = header.get().substring(JwtConstants.TOKEN_PREFIX_INDEX);
        if(Strings.isNullOrEmpty(token)){
            return CompletableFuture.completedFuture(unauthorized("You are not authorized.There is no token found in request header! Please login again."));
        }
        Claims claims;
        try{
            claims = JwtUtil.parse(token);
        }catch (ExpiredJwtException e){
            throw new CompletionException(new RequestException(Http.Status.REQUEST_TIMEOUT,"Your token has been expired.Please login again!"));
        }
        String userId = (String) claims.get("userId");
        if(!ObjectId.isValid(userId)){
            throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST,"User Object Id is invalid.Please provide a valid Object Id"));
        }
        User user = mongoDB.getMongoDatabase().getCollection("User", User.class).find(eq("_id",new ObjectId(userId))).first();
        if(user == null){
            throw new CompletionException(new RequestException(Http.Status.NOT_FOUND,"User has not been found!"));
        }

        return delegate.call(req.addAttr(Attrs.USER,user));
    }

    public static class Attrs{
        public static final TypedKey<User> USER = TypedKey.create("user");
    }
}
