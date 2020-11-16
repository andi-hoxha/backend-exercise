package constants;

public class JwtConstants {
    public static final String SECRET_ACCESS = "PrimeSecretKeyToGenerateJwtAccessToken";
    public static final long EXPIRATION_TIME_ACCESS = 3600000;
    public static final String HEADER_KEY = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final Integer TOKEN_PREFIX_INDEX = 7;
}
