package com.example.hay_mart.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI myOpenAPI(){
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8080");
        localServer.setDescription("Server URL in local environment");

        Contact contact = new Contact();
        contact.setName("HAY MART");
        contact.setEmail("hay_mart123@gmail.com");
        contact.setUrl("https://github.com/");

        License license = new License()
            .name("Mit License")
            .url("https://mit-test.com");

        Info info = new Info()
            .title("UAS JAVA LANJUTAN")
            .version("0.1")
            .contact(contact)
            .description("This API is use for project of java")
            .termsOfService("http://www.test.com/term")
            .license(license);


        return new OpenAPI().info(info).servers(List.of(localServer))
                    .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                    .components(new io.swagger.v3.oas.models.Components()
                                    .addSecuritySchemes("Bearer Authentication",
                                        new SecurityScheme()
                                            .name("Bearer Authentication")
                                            .type(SecurityScheme.Type.HTTP)
                                            .scheme("bearer")
                                            .bearerFormat("JWT")));


    }
}
