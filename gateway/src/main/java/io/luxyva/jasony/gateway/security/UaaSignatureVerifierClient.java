package io.luxyva.jasony.gateway.security;

import io.luxyva.jasony.gateway.config.oauth.OAuth2Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.jwt.crypto.sign.SignatureVerifier;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 从UAA获取公钥
 */
@Component
public class UaaSignatureVerifierClient implements OAuth2SignatureVerifierClient {
    
    private final Logger log = LoggerFactory.getLogger(UaaSignatureVerifierClient.class);
    
    private final RestTemplate restTemplate;
    
    private final OAuth2Properties oAuth2Properties;
    
    public UaaSignatureVerifierClient(DiscoveryClient discoveryClient, @Qualifier("loadBalancedRestTemplate") RestTemplate restTemplate,
                                      OAuth2Properties oAuth2Properties) {
        this.restTemplate = restTemplate;
        this.oAuth2Properties = oAuth2Properties;
        //获取所有的认证服务
        discoveryClient.getServices();
    }
    
    public String getpublicKeyEndpoint() {
        String tokenEndpointUrl = oAuth2Properties.getSignatureVerification().getPublicKeyEndpointUri();
        if (tokenEndpointUrl == null) {
            throw new InvalidClientException("no token endpoint configured in application properties");
        }
        return tokenEndpointUrl;
    }
    
    
    @Override
    
    public SignatureVerifier getSignatureVerifier() throws Exception {
        try {
            HttpEntity<Void> request = new HttpEntity<>(new HttpHeaders());
            String key = (String) restTemplate
                                      .exchange(getpublicKeyEndpoint(), HttpMethod.GET, request, Map.class).getBody()
                                      .get("value");
            return new RsaVerifier(key);
        } catch (IllegalStateException e) {
            log.warn("could not contact UAA to get public key");
            return null;
        }
    }
}
