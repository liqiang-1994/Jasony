package io.luxyva.jasony.gateway.security;

import io.luxyva.jasony.framework.config.JasonyProperties;
import io.luxyva.jasony.gateway.config.oauth.OAuth2Properties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

@Component
public class UaaTokenEndpointClient extends OAuth2TokenEndpointClientAdapter implements OAuth2TokenEndpointClient {
    
    public UaaTokenEndpointClient(@Qualifier("loadBalancedRestTemplate") RestTemplate restTemplate,
                                  JasonyProperties jasonyProperties, OAuth2Properties oAuth2Properties) {
        super(restTemplate, jasonyProperties, oAuth2Properties);
    }
    
    @Override
    protected void addAuthentication(HttpHeaders httpHeaders, MultiValueMap<String, String> params) {
        httpHeaders.add("Authorization", getAuthorizationHeader());
    }
    
    public String getAuthorizationHeader() {
        String clientId = getClientId();
        String clientSecret = getClientSecret();
        String authorization = clientId + ":" + clientSecret;
        return "Basic " + Base64Utils.encodeToString(authorization.getBytes(StandardCharsets.UTF_8));
    }
}
