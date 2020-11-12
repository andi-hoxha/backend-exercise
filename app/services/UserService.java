package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import com.mongodb.client.model.Filters;
import exceptions.NotFoundException;
import exceptions.RequestException;
import executors.MongoExecutionContext;
import models.User;
import play.libs.Json;
import play.mvc.Http;

import repositories.UserRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Singleton
public class UserService extends BaseService<User> implements UserRepository {

    public CompletableFuture<JsonNode> setup(List<User> users){
       return CompletableFuture.supplyAsync(()->{
          users.forEach(user -> {
              String password = user.getPassword();
              String encodedPassword = Base64.getEncoder().encodeToString(password.getBytes());
              user.setPassword(encodedPassword);
          });
           saveAll(users,"User",User.class);
           return Json.newObject();
       });
    }


    @Override
    public CompletableFuture<User> findByUser(String username) {
       return CompletableFuture.supplyAsync(()->{
           try{
                if(Strings.isNullOrEmpty(username)){
                    throw new IllegalArgumentException("Email cannot be empty!");
                }
                User foundUser = getCollection("User",User.class).find(Filters.eq("username",username)).first();
                if(foundUser == null){
                    throw new NotFoundException("Cannot find any user with this email : " + username);
               }
                return foundUser;
           }catch (IllegalArgumentException e){
               throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST,"Email cannot be empty"));
           }catch (NotFoundException e){
               throw new CompletionException(new RequestException(Http.Status.NOT_FOUND,"Cannot find any user with this email: " + username));
           }
       });
    }


}
