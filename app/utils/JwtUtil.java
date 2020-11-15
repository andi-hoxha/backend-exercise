package utils;

import constants.JwtConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import models.User;

import java.util.Date;

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
        Claims claims = Jwts.parser()
                .setSigningKey(JwtConstants.SECRET_ACCESS)
                .parseClaimsJws(token)
                .getBody();
        return claims;
    }

}
