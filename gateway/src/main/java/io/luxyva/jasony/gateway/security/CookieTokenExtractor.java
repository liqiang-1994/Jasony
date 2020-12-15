package io.luxyva.jasony.gateway.security;

import org.springframework.security.oauth2.provider.authentication.BearerTokenExtractor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * 从Cookie中提取Access Token
 */
public class CookieTokenExtractor extends BearerTokenExtractor {
    
    @Override
    public String extractToken(HttpServletRequest request) {
        String result;
        Cookie accessTokenCookie = OAuth2CookieHelper.getAccessTokenCookie(request);
        if (accessTokenCookie != null) {
            result = accessTokenCookie.getValue();
        } else {
            result = super.extractToken(request);
        }
        return result;
    }
    
}
