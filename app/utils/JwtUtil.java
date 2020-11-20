package utils;

import constants.JwtConstants;
import exceptions.RequestException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import lombok.extern.slf4j.Slf4j;
import models.User;
import play.mvc.Http;

import java.util.Date;
import java.util.concurrent.CompletionException;

@Slf4j
public class JwtUtil {

    public static String getAccessToken(User user){
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("userId",user.getId().toHexString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JwtConstants.EXPIRATION_TIME_ACCESS))
                .signWith(SignatureAlgorithm.HS512,JwtConstants.SECRET_ACCESS)
                .compact();
    }

    public static Claims parse(String token){
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(JwtConstants.SECRET_ACCESS)
                    .parseClaimsJws(token)
                    .getBody();
            if(claims.isEmpty()){
                throw new RequestException(Http.Status.UNAUTHORIZED,"Your session has been expired please login again");
            }
            return claims;
        }catch (SignatureException e){
            throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST,"JWT token does not match locally computed signature.JWT token is not valid"));
        }catch (RequestException e){
            throw new CompletionException(e);
        }catch (Exception e){
            throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR,"Authentication service unavailable"));
        }
    }

}
