package dev.dankoz.BaseServer;

import dev.dankoz.BaseServer.config.RsaKeyProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableConfigurationProperties(RsaKeyProperties.class)
@SpringBootApplication()
public class BaseServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(BaseServerApplication.class, args);
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/auth/test").allowedOrigins("http://localhost:4200/login");
			}
		};
	}

}
