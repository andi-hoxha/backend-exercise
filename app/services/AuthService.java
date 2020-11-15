package services;

import com.google.common.base.Strings;
import exceptions.RequestException;
import models.User;
import play.mvc.Http;
import utils.JwtUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Singleton
public class AuthService {

    @Inject
    UserService userService;

    public CompletableFuture<String> login(String username,String password){
        return CompletableFuture.supplyAsync(()->{
            try{
                if(Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)){
                    throw new IllegalArgumentException("Invalid arguments");
                }
                User foundUser = userService.findByUser(username).join();
                String encodedPassword = Base64.getEncoder().encodeToString(password.getBytes());
                if(!encodedPassword.equalsIgnoreCase(foundUser.getPassword())){
                    throw new RuntimeException("Incorrect password");
                }
                return JwtUtil.getAccessToken(foundUser);
            }catch (IllegalArgumentException e){
                throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST,"Either your login username or password is empty"));
            }catch (RuntimeException e){
                throw new CompletionException(new RequestException(Http.Status.FORBIDDEN,"Incorrect Password"));
            }
        });
    }

}
