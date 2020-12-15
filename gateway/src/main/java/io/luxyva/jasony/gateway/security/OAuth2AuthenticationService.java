package io.luxyva.jasony.gateway.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.web.client.HttpClientErrorException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * OAuth管理类,负责对用户身份验证,在令牌过期时刷新
 */
public class OAuth2AuthenticationService {
    
    private final Logger log = LoggerFactory.getLogger(OAuth2AuthenticationService.class);
    
    private static final long REFRESH_TOKEN_VALIDITY_MILLIS = 1000L;
    
    private final OAuth2TokenEndpointClient authorizationClient;
    
    private final OAuth2CookieHelper cookieHelper;
    
    private final PersistentTokenCache<OAuth2Cookies> recentRefreshed;
    
    public OAuth2AuthenticationService(OAuth2TokenEndpointClient tokenEndpointClient, OAuth2CookieHelper cookieHelper) {
        this.authorizationClient = tokenEndpointClient;
        this.cookieHelper = cookieHelper;
        this.recentRefreshed = new PersistentTokenCache<>(REFRESH_TOKEN_VALIDITY_MILLIS);
    }
    
    public ResponseEntity<OAuth2AccessToken> authenticate(HttpServletRequest request, HttpServletResponse response,
                                                          Map<String, String> params) {
        try {
            String username = params.get("username");
            String password = params.get("password");
            boolean rememberMe = Boolean.parseBoolean(params.get("rememberMe"));
            OAuth2AccessToken accessToken = authorizationClient.sendPasswordGrant(username, password);
            OAuth2Cookies cookies = new OAuth2Cookies();
            cookieHelper.createCookies(request, accessToken, rememberMe, cookies);
            cookies.addCookiesTo(response);
            log.debug("successfully authenticated user {}", params.get("username"));
            return ResponseEntity.ok(accessToken);
        } catch (HttpClientErrorException e) {
            log.error("failed to get OAuth2 tokens from UAA", e);
            throw new BadCredentialsException("Invalid credentials");
        }
    }
    
    public HttpServletRequest refreshToken(HttpServletRequest request, HttpServletResponse response,
                                           Cookie refreshCookie) {
        //检查Session是否过期
        if (cookieHelper.isSessionExpired(refreshCookie)) {
            log.info("session has expired");
            logout(request, response);
            return stripToken(request);
        }
        OAuth2Cookies cookies = getCachedCookies(refreshCookie.getName());
        synchronized (cookies) {
            if (cookies.getAccessTokenCookie() == null) {
                String refreshCookieValue = OAuth2CookieHelper.getRefreshTokenCookie(refreshCookie);
                OAuth2AccessToken accessToken = authorizationClient.sendRefreshGrant(refreshCookieValue);
                boolean rememberMe = OAuth2CookieHelper.isRememberMe(refreshCookie);
                cookieHelper.createCookies(request, accessToken, rememberMe, cookies);
                cookies.addCookiesTo(response);
            } else {
                log.debug("reusing cached refresh_token grant");
            }
            //更新AccessToken后替换Cookie
            CookieCollection requestCookies = new CookieCollection(request.getCookies());
            requestCookies.add(cookies.getAccessTokenCookie());
            requestCookies.add(cookies.getRefreshTokenCookie());
            return new CookiesHttpServletRequestWrapper(request, requestCookies.toArray());
        }
    }
    
    /**
     * 获取缓存中的Cookie
     * @param refreshTokenValue
     * @return
     */
    private OAuth2Cookies getCachedCookies(String refreshTokenValue) {
        synchronized (recentRefreshed) {
            OAuth2Cookies cookies = recentRefreshed.get(refreshTokenValue);
            if (cookies == null) {
                cookies = new OAuth2Cookies();
                recentRefreshed.put(refreshTokenValue, cookies);
            }
            return cookies;
        }
    }
    
    /**
     * 退出操作清除Cookie
     * @param request
     * @param response
     */
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        cookieHelper.clearCookies(request, response);
    }
    
    /**
     * 剖离Token
     * @param request
     * @return
     */
    public HttpServletRequest stripToken(HttpServletRequest request) {
        Cookie[] cookies = cookieHelper.stripCookies(request.getCookies());
        return new CookiesHttpServletRequestWrapper(request, cookies);
    }
}
