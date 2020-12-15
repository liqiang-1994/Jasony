package io.luxyva.jasony.gateway.filter;

import io.luxyva.jasony.gateway.security.OAuth2AuthenticationService;
import io.luxyva.jasony.gateway.security.OAuth2CookieHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.ClientAuthenticationException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedClientException;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 在Access Token过期更新Token
 */
public class RefreshTokenFilter extends GenericFilterBean {
    
    private final Logger log = LoggerFactory.getLogger(RefreshTokenFilter.class);
    
    /**
     * 过期窗口时间
     */
    private static final int REFRESH_WINDOW_SECS = 30;
    
    private final OAuth2AuthenticationService authenticationService;
    
    private final TokenStore tokenStore;
    
    public RefreshTokenFilter(OAuth2AuthenticationService authenticationService, TokenStore tokenStore) {
        this.authenticationService = authenticationService;
        this.tokenStore = tokenStore;
    }
    
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        try {
            httpServletRequest = refreshTokenIfExpiring(httpServletRequest, httpServletResponse);
        } catch (ClientAuthenticationException e) {
            log.warn("Security exception: could not refresh tokens", e);
            httpServletRequest = authenticationService.stripToken(httpServletRequest);
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
    
    /**
     *
     * @param httpServletRequest
     * @param httpServletResponse
     * @return
     */
    public HttpServletRequest refreshTokenIfExpiring(HttpServletRequest httpServletRequest,
                                                     HttpServletResponse httpServletResponse) {
        HttpServletRequest newHttpServletRequest = httpServletRequest;
        Cookie accessTokenCookie = OAuth2CookieHelper.getAccessTokenCookie(newHttpServletRequest);
        if (mustRefreshToken(accessTokenCookie)) {
            Cookie refreshCookie = OAuth2CookieHelper.getRefreshTokenCookie(httpServletRequest);
            if (refreshCookie != null) {
                try {
                    newHttpServletRequest = authenticationService.refreshToken(httpServletRequest,
                        httpServletResponse, refreshCookie);
                } catch (HttpClientErrorException e) {
                    throw new UnauthorizedClientException("Could not refresh OAuth2 token", e);
                }
            } else if (accessTokenCookie != null) {
                log.warn("access token found, but no refresh token, stripping them all");
                OAuth2AccessToken token = tokenStore.readAccessToken(accessTokenCookie.getValue());
                if (token.isExpired() || token.getExpiresIn() < REFRESH_WINDOW_SECS) {
                    throw new InvalidTokenException("access token has expired or expires within " + REFRESH_WINDOW_SECS + " seconds, but there's no refresh token");
                }
            }
        }
        return newHttpServletRequest;
    }
    
    /**
     * 判断是否需要立即更新Token
     * @param accessTokenCookie
     * @return
     */
    private boolean mustRefreshToken(Cookie accessTokenCookie) {
        if (accessTokenCookie == null) {
            return true;
        }
        OAuth2AccessToken accessToken = tokenStore.readAccessToken(accessTokenCookie.getValue());
        if (accessToken.isExpired() || accessToken.getExpiresIn() < REFRESH_WINDOW_SECS) {
            return true;
        }
        return false;
    }
}

