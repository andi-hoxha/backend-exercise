package constants;

public class JwtConstants {
    public static final String SECRET_ACCESS = "PrimeSecretKeyToGenerateJwtAccessToken";
    public static final String REFRESH_TOKEN = "PrimeSecretKeyToGenerateJwtRefreshToken";
    public static final long EXPIRATION_TIME_ACCESS = 200_000_000;
    public static final long EXPIRATION_TIME_REFRESH = 600_000_000;
    public static final String HEADER_KEY = "Authorization";
    public static final String AUTHORIZATION_KEY = "Bearer ";
}
