package io.luxyva.jasony.gateway.security;

import org.springframework.security.jwt.crypto.sign.SignatureVerifier;

public interface OAuth2SignatureVerifierClient {
    
    SignatureVerifier getSignatureVerifier() throws Exception;
}
