package io.luxyva.jasony.gateway.security;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * 对修改Cookie的请求进行包装
 */
public class CookiesHttpServletRequestWrapper extends HttpServletRequestWrapper {
    
    private Cookie[] cookies;
    
    public CookiesHttpServletRequestWrapper(HttpServletRequest request, Cookie[] cookies) {
        super(request);
        this.cookies = cookies;
    }
    
    @Override
    public Cookie[] getCookies() {
        return cookies;
    }
}
