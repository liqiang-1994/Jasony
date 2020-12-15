package io.luxyva.jasony.gateway.security;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

class OAuth2Cookies {
    
    private Cookie accessTokenCookie;
    private Cookie refreshTokenCookie;
    
    public Cookie getAccessTokenCookie() {
        return accessTokenCookie;
    }
    
    public Cookie getRefreshTokenCookie() {
        return refreshTokenCookie;
    }
    
    public void setCookies(Cookie accessTokenCookie, Cookie refreshTokenCookie) {
        this.accessTokenCookie = accessTokenCookie;
        this.refreshTokenCookie = refreshTokenCookie;
    }
    
    /**
     * 在认证成功后将 accessToken, refreshToken添加到cookies
     * @param response
     */
    void addCookiesTo(HttpServletResponse response) {
        response.addCookie(getAccessTokenCookie());
        response.addCookie(getRefreshTokenCookie());
    }
}
