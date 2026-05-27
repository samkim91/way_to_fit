package com.waytofit.global.config
import com.waytofit.global.security.CurrentUserId
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springdoc.core.models.GroupedOpenApi
import org.springdoc.core.utils.SpringDocUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    init {
        SpringDocUtils.getConfig().addAnnotationsToIgnore(CurrentUserId::class.java)
    }

    @Bean
    fun openAPI(): OpenAPI {
        val securitySchemeName = "Bearer Authentication"
        return OpenAPI()
            .info(
                Info()
                    .title("WayToFit API")
                    .description("WayToFit REST API 문서")
                    .version("v1")
            )
            .components(
                Components().addSecuritySchemes(
                    securitySchemeName,
                    SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                )
            )
            .addSecurityItem(SecurityRequirement().addList(securitySchemeName))
    }

    @Bean
    fun superAdminApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("1. Super-Admin")
            .pathsToMatch("/api/v1/super-admin/**")
            .build()
    }

    @Bean
    fun serviceApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("2. Service")
            .pathsToMatch("/api/**")
            .pathsToExclude("/api/v1/super-admin/**")
            .build()
    }
}
