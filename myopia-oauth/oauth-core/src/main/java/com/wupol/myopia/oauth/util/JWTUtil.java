package com.wupol.myopia.oauth.util;

public class JWTUtil {

    public static final long TOKEN_EXPIRE_TIME = 7200 * 1000;
    private static final String ISSUER = "cheng";

    /**
     * 生成Token
     * 
     * @param username  用户标识（不一定是用户名，有可能是用户ID或者手机号什么的）
     * @param secretKey
     * @return
     */
    public static String generateToken(String username, String secretKey) {
        // Algorithm algorithm = Algorithm.HMAC256(secretKey);
        // Date now = new Date();
        // Date expireTime = new Date(now.getTime() + TOKEN_EXPIRE_TIME);

        // String token = JWT.create()
        // .withIssuer(ISSUER)
        // .withIssuedAt(now)
        // .withExpiresAt(expireTime)
        // .withClaim("username", username)
        // .sign(algorithm);

        // return token;
        return null;
    }

    /**
     * 校验Token
     * 
     * @param token
     * @param secretKey
     * @return
     */
    public static void verifyToken(String token, String secretKey) {
        // try {
        // Algorithm algorithm = Algorithm.HMAC256(secretKey);
        // JWTVerifier jwtVerifier = JWT.require(algorithm).withIssuer(ISSUER).build();
        // jwtVerifier.verify(token);
        // } catch (JWTDecodeException jwtDecodeException) {
        // throw new
        // TokenAuthenticationException(ResponseCodeEnum.TOKEN_INVALID.getCode(),
        // ResponseCodeEnum.TOKEN_INVALID.getMessage());
        // } catch (SignatureVerificationException signatureVerificationException) {
        // throw new
        // TokenAuthenticationException(ResponseCodeEnum.TOKEN_SIGNATURE_INVALID.getCode(),
        // ResponseCodeEnum.TOKEN_SIGNATURE_INVALID.getMessage());
        // } catch (TokenExpiredException tokenExpiredException) {
        // throw new
        // TokenAuthenticationException(ResponseCodeEnum.TOKEN_EXPIRED.getCode(),
        // ResponseCodeEnum.TOKEN_INVALID.getMessage());
        // } catch (Exception ex) {
        // throw new
        // TokenAuthenticationException(ResponseCodeEnum.UNKNOWN_ERROR.getCode(),
        // ResponseCodeEnum.UNKNOWN_ERROR.getMessage());
        // }
    }

    /**
     * 从Token中提取用户信息
     * 
     * @param token
     * @return
     */
    public static String getUserInfo(String token) {
        // DecodedJWT decodedJWT = JWT.decode(token);
        // String username = decodedJWT.getClaim("username").asString();
        // return username;
        return null;
    }

}
