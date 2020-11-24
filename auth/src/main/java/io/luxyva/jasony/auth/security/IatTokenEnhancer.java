package io.luxyva.jasony.auth.security;

import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Token添加iat(创建时间)
 */
@Component
public class IatTokenEnhancer implements TokenEnhancer {
    
    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        addClaim((DefaultOAuth2AccessToken) accessToken);
        return accessToken;
    }
    
    private void addClaim(DefaultOAuth2AccessToken accessToken) {
        DefaultOAuth2AccessToken token = accessToken;
        Map<String, Object> otherInfo = token.getAdditionalInformation();
        if (otherInfo.isEmpty()) {
            otherInfo = new LinkedHashMap<String, Object>();
        }
        otherInfo.put("iat", (int) (System.currentTimeMillis() / 1000L));
        token.setAdditionalInformation(otherInfo);
    }
}
