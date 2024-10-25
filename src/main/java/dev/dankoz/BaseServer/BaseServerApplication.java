package dev.dankoz.BaseServer;

import dev.dankoz.BaseServer.config.properties.ApiKeysProperties;
import dev.dankoz.BaseServer.config.properties.RsaKeyProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties({RsaKeyProperties.class, ApiKeysProperties.class})
@SpringBootApplication()
public class BaseServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(BaseServerApplication.class, args);
	}


}
