package services;

import com.google.common.base.Strings;
import com.mongodb.client.model.Updates;
import constants.CollectionNames;
import exceptions.RequestException;
import executors.MongoExecutionContext;
import lombok.extern.slf4j.Slf4j;
import models.User;
import play.mvc.Http;
import utils.JwtUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static com.mongodb.client.model.Filters.*;
@Singleton
@Slf4j
public class AuthService {

    @Inject
    UserService userService;

    @Inject
    MongoExecutionContext ec;

    public CompletableFuture<String> login(String username,String password){
        return CompletableFuture.supplyAsync(()->{
            try{
                if(Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)){
                    throw new RequestException(Http.Status.BAD_REQUEST,"Username or password is empty");
                }
                User foundUser = userService.findOne("User",eq("username",username),User.class);
                if(foundUser == null){
                    throw new RequestException(Http.Status.NOT_FOUND,"Please check your inputs!");
                }
                String encodedPassword = Base64.getEncoder().encodeToString(password.getBytes());
                if(!encodedPassword.equalsIgnoreCase(foundUser.getPassword()) || !foundUser.getUsername().equalsIgnoreCase(username)){
                    throw new RequestException(Http.Status.BAD_REQUEST,"Either username or password is incorrect");
                }
                String accessToken = JwtUtil.getAccessToken(foundUser);
                foundUser.setAccessToken(accessToken);
                userService.getCollection(CollectionNames.USER,User.class).updateOne(eq("_id",foundUser.getId()), Updates.set("accessToken",accessToken));
                return accessToken;
            }catch (RequestException e) {
                throw new CompletionException(e);
            }catch (CompletionException e){
                throw e;
            }catch (Exception e){
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR,"service unavailable"));
            }
        },ec.current());
    }

}
