package dev.dankoz.BaseServer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@ConfigurationProperties(prefix = "api")
public record ApiKeysProperties(String tommorowApiKey) {

}
