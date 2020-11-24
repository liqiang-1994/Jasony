package io.luxyva.jasony.auth.config;

import io.luxyva.jasony.framework.config.JasonyConstants;
import io.luxyva.jasony.framework.config.JasonyProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

import javax.servlet.http.HttpServletResponse;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Collection;

@Configuration
@EnableAuthorizationServer
public class AuthConfiguration extends AuthorizationServerConfigurerAdapter implements ApplicationContextAware {
    
    private static final int MIN_ACCESS_TOKEN_VALIDITY_SEC = 60;
    
    private ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    @EnableResourceServer
    public static class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {
    
        private final TokenStore tokenStore;
    
        private final JasonyProperties jasonyProperties;
    
        private final CorsFilter corsFilter;
    
        public ResourceServerConfiguration(TokenStore tokenStore, JasonyProperties jasonyProperties, CorsFilter corsFilter) {
            this.tokenStore = tokenStore;
            this.jasonyProperties = jasonyProperties;
            this.corsFilter = corsFilter;
        }
        
        @Override
        public void configure(HttpSecurity http) throws Exception {
            http
                .exceptionHandling()
                .authenticationEntryPoint(((request, response, e) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED)))
                .and()
                .csrf()
                .disable()
                .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
                .headers()
                .frameOptions()
                .disable()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/api/register").permitAll()
                .antMatchers("/api/activate").permitAll()
                .antMatchers("/api/authenticate").permitAll()
                .antMatchers("/api/account/reset-password/init").permitAll()
                .antMatchers("/api/account/reset-password/finish").permitAll()
                .antMatchers("/api/**").authenticated()
                .antMatchers("/management/health").permitAll()
                .antMatchers("/management/**").hasAuthority(JasonyConstants.ADMIN)
                .antMatchers("/v2/api-docs/**").permitAll()
                .antMatchers("/swagger-resources/configuration/ui").permitAll()
                .antMatchers("/swagger-ui/index.html").hasAuthority(JasonyConstants.ADMIN);
        }
        
        @Override
        public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
            resources.resourceId("jasony-auth").tokenStore(tokenStore);
        }
    }
    
    private final JasonyProperties jasonyProperties;
    
    private final AuthProperties authProperties;
    
    private final PasswordEncoder passwordEncoder;
    
    public AuthConfiguration(JasonyProperties jasonyProperties, AuthProperties authProperties, PasswordEncoder passwordEncoder) {
        this.jasonyProperties = jasonyProperties;
        this.authProperties = authProperties;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        int accessTokenValidity = authProperties.getWebClientConfiguration().getAccessTokenValidityInSeconds();
        accessTokenValidity = Math.max(accessTokenValidity, MIN_ACCESS_TOKEN_VALIDITY_SEC);
        int refreshTokenValidity = authProperties.getWebClientConfiguration().getRefreshTokenValidityInSecondsForRememberMe();
        refreshTokenValidity = Math.max(refreshTokenValidity, accessTokenValidity);
        clients.inMemory()
            .withClient(authProperties.getWebClientConfiguration().getClientId())
            .secret(passwordEncoder.encode(authProperties.getWebClientConfiguration().getSecret()))
            .scopes("openid")
            .autoApprove(true)
            .authorizedGrantTypes("implicit", "refresh_token", "password", "authorization_code")
            .accessTokenValiditySeconds(accessTokenValidity)
            .refreshTokenValiditySeconds(refreshTokenValidity)
            .and()
            .withClient(jasonyProperties.getSecurity().getClientAuthorization().getClientId())
            .secret(passwordEncoder.encode(jasonyProperties.getSecurity().getClientAuthorization().getClientSecret()))
            .scopes("web_app")
            .authorities(JasonyConstants.ADMIN)
            .autoApprove(true)
            .authorizedGrantTypes("client_credentials")
            .accessTokenValiditySeconds((int) jasonyProperties.getSecurity().getAuthentication().getJwt().getTokenValidityInSeconds())
            .refreshTokenValiditySeconds((int) jasonyProperties.getSecurity().getAuthentication().getJwt().getTokenValidityInSecondsForRememberMe());
    }
    
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        Collection<TokenEnhancer> tokenEnhancers = applicationContext.getBeansOfType(TokenEnhancer.class).values();
        TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
        tokenEnhancerChain.setTokenEnhancers(new ArrayList<>(tokenEnhancers));
        endpoints
            .authenticationManager(authenticationManager)
            .tokenStore(tokenStore())
            .tokenEnhancer(tokenEnhancerChain)
            .reuseRefreshTokens(false);
    }
    
    @Autowired
    @Qualifier("authenticationManagerBean")
    private AuthenticationManager authenticationManager;
    
    @Bean
    public JwtTokenStore tokenStore() {
        return new JwtTokenStore(jwtAccessTokenConverter());
    }
    
    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        KeyPair keyPair = new KeyStoreKeyFactory(
            new ClassPathResource(authProperties.getKeyStore().getName()), authProperties.getKeyStore().getPassword().toCharArray())
                              .getKeyPair(authProperties.getKeyStore().getAlias());
        converter.setKeyPair(keyPair);
        return converter;
    }
    
    @Override
    public void configure(AuthorizationServerSecurityConfigurer configurer) throws Exception {
        configurer.tokenKeyAccess("permitAll()").checkTokenAccess("isAuthenticated()");
    }
}
