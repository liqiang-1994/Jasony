package io.luxyva.jasony.base.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "base")
@Configuration
@Getter
@Setter
public class ApplicationProperties {
    private String fileUpAddress;
    
    private String upAccessKey;
    
    private String upSecretKey;
    
}
