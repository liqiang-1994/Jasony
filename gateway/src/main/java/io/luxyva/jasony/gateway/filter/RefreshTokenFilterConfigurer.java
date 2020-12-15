package io.luxyva.jasony.gateway.filter;

import io.luxyva.jasony.gateway.security.OAuth2AuthenticationService;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationProcessingFilter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.DefaultSecurityFilterChain;

public class RefreshTokenFilterConfigurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {
    
    private OAuth2AuthenticationService authenticationService;
    private final TokenStore tokenStore;
    
    public RefreshTokenFilterConfigurer(OAuth2AuthenticationService authenticationService, TokenStore tokenStore) {
        this.authenticationService = authenticationService;
        this.tokenStore = tokenStore;
    }
    
    @Override
    public void configure(HttpSecurity http) throws Exception {
        RefreshTokenFilter tokenFilter = new RefreshTokenFilter(authenticationService, tokenStore);
        http.addFilterBefore(tokenFilter, OAuth2AuthenticationProcessingFilter.class);
    }
    
}
