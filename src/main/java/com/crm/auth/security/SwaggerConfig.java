package com.crm.auth.security;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CRM System API Documentation")
                        .description("Bu CRM tizimi uchun Swagger hujjati. Lead, Client, User, Notification, Attendance va boshqa API’larni ishlatish mumkin.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Azizbek Qudratov")
                                .email("devkudratov@gmail.com")
                                .url("https://github.com/your-repo"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")));
    }
}
