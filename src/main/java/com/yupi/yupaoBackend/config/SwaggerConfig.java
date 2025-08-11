package com.yupi.yupaoBackend.config;
import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


/**
 * http://localhost:8080/api/doc.html
 */

@Configuration
@EnableKnife4j
@Profile("dev")
public class SwaggerConfig {

    @Bean
    // This method returns an OpenAPI object with the specified information
    public OpenAPI springShopOpenAPI() {
        // Create a new OpenAPI object
        return new OpenAPI()
                // Set the title of the API
                .info(new Info().title("友情连接")
                        // Set the description of the API
                        .description("友情连接的接口文档")
                        // Set the version of the API
                        .version("v0.0.1")
                        // Set the license of the API
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                // Set the external documentation of the API
                .externalDocs(new ExternalDocumentation()
                        // Set the description of the external documentation
                        .description("SpringShop Wiki Documentation")
                        // Set the URL of the external documentation
                        .url("https://springshop.wiki.github.org/docs"));
    }

}

