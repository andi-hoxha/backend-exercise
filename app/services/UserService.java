package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import com.mongodb.client.model.Filters;
import exceptions.NotFoundException;
import exceptions.RequestException;
import models.Role;
import models.User;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Http;

import repositories.UserRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

@Singleton
public class UserService extends BaseService<User> implements UserRepository {

    @Inject
    RoleService roleService;

    public CompletableFuture<JsonNode> setup(List<User> users){
       return CompletableFuture.supplyAsync(()->{
           List<Role> roles = roleService.getAll("Role",Role.class);
          users.forEach(user -> {
              List<Role> userRoles = getRoles(user,roles);
              String password = user.getPassword();
              String encodedPassword = Base64.getEncoder().encodeToString(password.getBytes());
              user.setPassword(encodedPassword);
              user.setRoles(userRoles);

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

    public List<Role> getRoles(User user,List<Role> roleList){
        List<ObjectId> roleIds =  user.getRoleIds().stream().map(ObjectId::new).collect(Collectors.toList());
        return roleList.stream().reduce(new ArrayList<Role>(),(accumulator,next)->{
            roleIds.forEach(id -> {
               if(next.getId().equals(id)){
                   accumulator.add(next);
               }
           });
           return accumulator;
        },(a,b)->{
            a.addAll(b);
            return a;
        });

    }

}
