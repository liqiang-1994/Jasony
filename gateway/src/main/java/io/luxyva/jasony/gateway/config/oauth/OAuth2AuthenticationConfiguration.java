package io.luxyva.jasony.gateway.config.oauth;

import io.luxyva.jasony.gateway.filter.RefreshTokenFilterConfigurer;
import io.luxyva.jasony.gateway.security.CookieTokenExtractor;
import io.luxyva.jasony.gateway.security.OAuth2AuthenticationService;
import io.luxyva.jasony.gateway.security.OAuth2CookieHelper;
import io.luxyva.jasony.gateway.security.OAuth2TokenEndpointClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.authentication.TokenExtractor;
import org.springframework.security.oauth2.provider.token.TokenStore;

@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class OAuth2AuthenticationConfiguration extends ResourceServerConfigurerAdapter {
    private final OAuth2Properties oAuth2Properties;
    private final OAuth2TokenEndpointClient tokenEndpointClient;
    private final TokenStore tokenStore;
    
    public OAuth2AuthenticationConfiguration(OAuth2Properties oAuth2Properties, OAuth2TokenEndpointClient tokenEndpointClient, TokenStore tokenStore) {
        this.oAuth2Properties = oAuth2Properties;
        this.tokenEndpointClient = tokenEndpointClient;
        this.tokenStore = tokenStore;
    }
    
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
            .antMatchers("/auth/login").permitAll()
            .antMatchers("/auth/logout").authenticated()
            .and()
            .apply(refreshTokenFilterConfigurerAdapter());
    }
    
    private RefreshTokenFilterConfigurer refreshTokenFilterConfigurerAdapter() {
        return new RefreshTokenFilterConfigurer(authenticationService(), tokenStore);
    }
    
    @Bean
    public OAuth2CookieHelper cookieHelper() {
        return new OAuth2CookieHelper(oAuth2Properties);
    }
    
    @Bean
    public OAuth2AuthenticationService authenticationService() {
        return new OAuth2AuthenticationService(tokenEndpointClient, cookieHelper());
    }
    
    @Override
    public void configure(ResourceServerSecurityConfigurer configurer) {
        configurer.tokenExtractor(tokenExtractor());
    }
    
    @Bean
    public TokenExtractor tokenExtractor() {
        return new CookieTokenExtractor();
    }
}
