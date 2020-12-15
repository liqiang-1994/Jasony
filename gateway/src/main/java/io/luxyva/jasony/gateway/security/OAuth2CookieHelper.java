package io.luxyva.jasony.gateway.security;

import io.luxyva.jasony.gateway.config.oauth.OAuth2Properties;
import org.apache.http.conn.util.PublicSuffixMatcher;
import org.apache.http.conn.util.PublicSuffixMatcherLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.apache.http.conn.util.InetAddressUtils.isIPv4Address;
import static org.apache.http.conn.util.InetAddressUtils.isIPv6Address;
import org.apache.http.conn.util.PublicSuffixMatcher;
import org.apache.http.conn.util.PublicSuffixMatcherLoader;

/**
 * OAuth2 工具类
 */
public class OAuth2CookieHelper {
    
    private final Logger log = LoggerFactory.getLogger(OAuth2CookieHelper.class);
    
    public static final String ACCESS_TOKEN_COOKIE = OAuth2AccessToken.ACCESS_TOKEN;
    
    public static final String REFRESH_TOKEN_COOKIE = OAuth2AccessToken.REFRESH_TOKEN;
    
    public static final String SESSION_TOKEN_COOKIE = "session_token";
    
    private static final List<String> COOKIE_NAMES = Arrays.asList(ACCESS_TOKEN_COOKIE, REFRESH_TOKEN_COOKIE, SESSION_TOKEN_COOKIE);
    
    private static final long REFRESH_TOKEN_EXPIRATION_WINDOW_SECS = 3L;
    
    private JsonParser jsonParser = JsonParserFactory.getJsonParser();
    
    PublicSuffixMatcher suffixMatcher;
    
    private OAuth2Properties oAuth2Properties;
    
    public OAuth2CookieHelper(OAuth2Properties oAuth2Properties) {
        this.oAuth2Properties = oAuth2Properties;
        this.suffixMatcher = PublicSuffixMatcherLoader.getDefault();
    }
    
    public static Cookie getAccessTokenCookie(HttpServletRequest request) {
        return getCookie(request, ACCESS_TOKEN_COOKIE);
    }
    
    public static Cookie getRefreshTokenCookie(HttpServletRequest request) {
        Cookie cookie = getCookie(request, REFRESH_TOKEN_COOKIE);
        if (cookie == null) {
            cookie = getCookie(request, SESSION_TOKEN_COOKIE);
        }
        return cookie;
    }
    
    /**
     * 根据名称获取对应Cookie信息
     * @param request
     * @param cookieName
     * @return
     */
    private static Cookie getCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(cookieName)) {
                    String value = cookie.getValue();
                    if (StringUtils.hasText(value)) {
                        return cookie;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * 创建Cookie
     * @param request
     * @param auth2AccessToken
     * @param rememberMe
     * @param oAuth2Cookies
     */
    public void createCookies(HttpServletRequest request, OAuth2AccessToken auth2AccessToken, boolean rememberMe,
                              OAuth2Cookies oAuth2Cookies) {
        String domain = getCookieDomain(request);
        log.debug("creating cookies for domain {}", domain);
        Cookie accessTokenCookie = new Cookie(ACCESS_TOKEN_COOKIE, auth2AccessToken.getValue());
        setCookieProperties(accessTokenCookie, request.isSecure(), domain);
        log.debug("created access token cookie '{}'", accessTokenCookie.getName());
    
        OAuth2RefreshToken refreshToken = auth2AccessToken.getRefreshToken();
        Cookie refreshTokenCookie = createRefreshTokenCookie(refreshToken, rememberMe);
        setCookieProperties(refreshTokenCookie, request.isSecure(), domain);
        log.debug("created refresh token cookie '{}', age: {}", refreshTokenCookie.getName(),
            refreshTokenCookie.getMaxAge());
        oAuth2Cookies.setCookies(accessTokenCookie, refreshTokenCookie);
    }
    
    public boolean isSessionExpired(Cookie refreshCookie) {
        if (isRememberMe(refreshCookie)) {
            return false;
        }
        int validity = oAuth2Properties.getWebClientConfiguration().getSessionTimeoutInSecondes();
        if (validity < 0) {
            return false;
        }
        Integer iat = getClaim(refreshCookie.getValue(), "iat", Integer.class);
        if (iat == null) {
            return false;
        }
        int now = (int) (System.currentTimeMillis() / 1000L);
        int sessionDuration = now - iat;
        log.debug("session duration {} secs, will timeout at {}", sessionDuration, validity);
        return sessionDuration > validity;
    }
    
    /**
     * 是否勾选记住我
     * @param refreshTokenCookie
     * @return
     */
    public static boolean isRememberMe(Cookie refreshTokenCookie) {
        return refreshTokenCookie.getName().equals(REFRESH_TOKEN_COOKIE);
    }
    
    public static String getRefreshTokenCookie(Cookie refreshCookie) {
        String value = refreshCookie.getValue();
        int i = value.indexOf('|');
        if (i > 0) {
            return value.substring(i + 1);
        }
        return value;
    }
    
    private Cookie createRefreshTokenCookie(OAuth2RefreshToken refreshToken, boolean rememberMe) {
        int maxAge = -1;
        String name = SESSION_TOKEN_COOKIE;
        String value = refreshToken.getValue();
        if (rememberMe) {
            name = REFRESH_TOKEN_COOKIE;
            Integer exp = getClaim(refreshToken.getValue(), AccessTokenConverter.EXP, Integer.class);
            if (exp != null) {
                int now = (int) (System.currentTimeMillis() / 1000L);
                maxAge = exp - now;
                log.debug("refresh token valid for another {} secs", maxAge);
                maxAge -= REFRESH_TOKEN_EXPIRATION_WINDOW_SECS;
            }
        }
        Cookie refreshTokenCookie = new Cookie(name, value);
        refreshTokenCookie.setMaxAge(maxAge);
        return refreshTokenCookie;
    }
    
    @SuppressWarnings("unchecked")
    private <T> T getClaim(String refreshToken, String claimName, Class<T> clazz) {
        Jwt jwt = JwtHelper.decode(refreshToken);
        String claims = jwt.getClaims();
        Map<String, Object> claimMap = jsonParser.parseMap(claims);
        Object claimValue = claimMap.get(claimName);
        if (claimValue == null) {
            return null;
        }
        if (clazz.isAssignableFrom(claimValue.getClass())) {
            throw new InvalidTokenException("claim is not excepted type: " + claimName);
        }
        return (T) claimValue;
    }
    
    /**
     * 获取顶级域名
     * @param request
     * @return
     */
    private String getCookieDomain(HttpServletRequest request) {
        String domain = oAuth2Properties.getWebClientConfiguration().getCookieDomain();
        if (domain != null) {
            return domain;
        }
        domain = request.getServerName().toLowerCase();
        if (domain.startsWith("www.")) {
            domain = domain.substring(4);
        }
        if (!isIPv4Address(domain) && !isIPv6Address(domain)) {
            String suffix = suffixMatcher.getDomainRoot(domain);
            if (suffix != null && !suffix.equals(domain)) {
                return "." + suffix;
            }
        }
        return null;
    }
    
    /**
     * 设置Cookie相关属性
     * @param cookie
     * @param isSecure
     * @param domain
     */
    private void setCookieProperties(Cookie cookie, boolean isSecure, String domain) {
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setSecure(isSecure);
        if (domain != null) {
            cookie.setDomain(domain);
        }
    }
    
    /**
     * 清除Cookies
     * @param request
     * @param response
     */
    public void clearCookies(HttpServletRequest request, HttpServletResponse response) {
        String domain = getCookieDomain(request);
        for (String cookieName : COOKIE_NAMES) {
            clearCookie(request, response, domain, cookieName);
        }
    }
    
    private void clearCookie(HttpServletRequest request, HttpServletResponse response, String domain, String cookieName) {
        Cookie cookie = new Cookie(cookieName, "");
        setCookieProperties(cookie, request.isSecure(), domain);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        log.debug("clear cookie {}", cookie.getName());
    }
    
    Cookie[] stripCookies(Cookie[] cookies) {
        CookieCollection cookieCollection = new CookieCollection(cookies);
        if (cookieCollection.removeAll(COOKIE_NAMES)) {
            return cookieCollection.toArray();
        }
        return cookies;
    }
    
}
