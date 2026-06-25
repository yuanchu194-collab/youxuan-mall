package com.youxuan.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j / Swagger 基础配置，统一设置接口文档标题和版本。
 */
@Configuration
public class Knife4jConfig {

    /**
     * 提供 OpenAPI 基础信息，后续各服务接口会自动归入文档。
     */
    @Bean
    public OpenAPI youxuanOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("优选商城接口文档")
                        .description("youxuan-mall-cloud API")
                        .version("0.0.1"));
    }
}
