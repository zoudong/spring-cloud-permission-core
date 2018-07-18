package com.zoudong.permission.utils.jwt;

import com.alibaba.fastjson.JSONObject;
import com.zoudong.permission.constant.JwtConstant;
import com.zoudong.permission.exception.BusinessException;
import io.jsonwebtoken.*;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.xml.bind.DatatypeConverter;
import java.util.Date;
import java.util.prefs.BackingStoreException;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
public class JwtUtil {
    /**
     * 由字符串生成加密key
     *
     * @return
     */
    public SecretKey generalKey() {
        String stringKey = JwtConstant.JWT_SECRET;
        byte[] encodedKey = Base64.decodeBase64(stringKey);
        SecretKey key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
        return key;
    }

    /**
     * 创建jwt
     *
     * @param id
     * @param subject
     * @param ttlMillis
     * @return
     * @throws Exception
     */
    public String createJWT(String id, String subject, long ttlMillis) throws Exception {

        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        SecretKey key = generalKey();
        JwtBuilder builder = Jwts.builder()
                .setId(id)
                .setIssuedAt(now)
                .setSubject(subject)
                .signWith(signatureAlgorithm, key);
        if (ttlMillis >= 0) {
            long expMillis = nowMillis + ttlMillis;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp);
        }
        return builder.compact();
    }

    /**
     * 解密jwt
     *
     * @param jwt
     * @return
     * @throws Exception
     */
    public Claims parseJWT(String jwt) throws Exception {
        SecretKey key = generalKey();
        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(jwt).getBody();
        return claims;
    }


    public static String paseJwt(ServletRequest servletRequest, ServletResponse servletResponse)throws Exception {
        String jwt=(String)servletRequest.getAttribute(AUTHORIZATION);
        String account = null;
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(DatatypeConverter.parseBase64Binary(JwtConstant.JWT_SECRET))
                    .parseClaimsJws(jwt)
                    .getBody();
            account= JSONObject.parseObject(claims.getSubject()).getString("account");// 用户名
        } catch (ExpiredJwtException e) {
            throw new BusinessException("token_pase_error","JWT 令牌过期:" + e.getMessage());
        } catch (UnsupportedJwtException e) {
            throw new BusinessException("JWT 令牌无效:" + e.getMessage());
        } catch (MalformedJwtException e) {
            throw new BusinessException("JWT 令牌格式错误:" + e.getMessage());
        } catch (SignatureException e) {
            throw new BusinessException("JWT 令牌签名无效:" + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("JWT 令牌参数异常:" + e.getMessage());
        } catch (Exception e) {
            throw new BusinessException("JWT 令牌错误:" + e.getMessage());
        }

        return jwt;
    }


}
