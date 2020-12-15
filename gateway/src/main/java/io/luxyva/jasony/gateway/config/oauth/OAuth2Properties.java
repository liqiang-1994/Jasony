package io.luxyva.jasony.gateway.config.oauth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "oauth2", ignoreUnknownFields = false)
public class OAuth2Properties {
    
    private WebClientConfiguration webClientConfiguration = new WebClientConfiguration();
    
    private SignatureVerification signatureVerification = new SignatureVerification();
    
    public WebClientConfiguration getWebClientConfiguration() {
        return webClientConfiguration;
    }
    
    public SignatureVerification getSignatureVerification() {
        return signatureVerification;
    }
    
    public static class WebClientConfiguration {
        private String clientId = "web_app";
        private String secret = "changeit";
        private int sessionTimeoutInSecondes = 1800;
        private String cookieDomain;
    
        public String getClientId() {
            return clientId;
        }
    
        public void setClientId(String clientId) {
            this.clientId = clientId;
        }
    
        public String getSecret() {
            return secret;
        }
    
        public void setSecret(String secret) {
            this.secret = secret;
        }
    
        public int getSessionTimeoutInSecondes() {
            return sessionTimeoutInSecondes;
        }
    
        public void setSessionTimeoutInSecondes(int sessionTimeoutInSecondes) {
            this.sessionTimeoutInSecondes = sessionTimeoutInSecondes;
        }
    
        public String getCookieDomain() {
            return cookieDomain;
        }
    
        public void setCookieDomain(String cookieDomain) {
            this.cookieDomain = cookieDomain;
        }
    }
    
    public static class SignatureVerification {
        private long publicKeyRefreshRateLimit = 10 * 1000L;
        private long ttl = 24 * 60 * 60 * 1000L;
        private String publicKeyEndpointUri = "http://uaa/oauth/token_key";
    
        public long getPublicKeyRefreshRateLimit() {
            return publicKeyRefreshRateLimit;
        }
    
        public void setPublicKeyRefreshRateLimit(long publicKeyRefreshRateLimit) {
            this.publicKeyRefreshRateLimit = publicKeyRefreshRateLimit;
        }
    
        public long getTtl() {
            return ttl;
        }
    
        public void setTtl(long ttl) {
            this.ttl = ttl;
        }
    
        public String getPublicKeyEndpointUri() {
            return publicKeyEndpointUri;
        }
    
        public void setPublicKeyEndpointUri(String publicKeyEndpointUri) {
            this.publicKeyEndpointUri = publicKeyEndpointUri;
        }
    }
}
