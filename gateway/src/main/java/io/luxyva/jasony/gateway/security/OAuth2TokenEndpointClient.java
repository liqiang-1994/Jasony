package io.luxyva.jasony.gateway.security;

import org.springframework.security.oauth2.common.OAuth2AccessToken;

/**
 * 获取OAuth2授权服务器授权信息
 */
public interface OAuth2TokenEndpointClient {
    
    OAuth2AccessToken sendPasswordGrant(String username, String password);
    
    OAuth2AccessToken sendRefreshGrant(String refreshTokenValue);
}
