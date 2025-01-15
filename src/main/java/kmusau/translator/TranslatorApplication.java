package kmusau.translator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class TranslatorApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(TranslatorApplication.class, args);
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/voice/download")
						.allowedOrigins("*") // or specify allowed origins
						.allowedMethods("GET", "POST", "PUT") // specify allowed HTTP methods
						.allowedHeaders("*"); // specify allowed headers
			}
		};
	}
}
