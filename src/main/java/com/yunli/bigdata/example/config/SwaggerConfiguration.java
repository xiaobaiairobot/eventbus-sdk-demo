package com.yunli.bigdata.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.yunli.bigdata.dsep.service.common.ConfigurationLoader;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration {
  private static final String HEADER_CONFIGURATION = "springfox.documentation.header";

  @Bean
  public Docket createRestApi(ConfigurationLoader loader) {
    return new Docket(DocumentationType.SWAGGER_2)
        .apiInfo(new ApiInfoBuilder().title(loader.get(HEADER_CONFIGURATION, "").toString()).version("1.0.0").build())
        .select()
        .apis(RequestHandlerSelectors.basePackage("com.yunli.bigdata.dsep"))
        .paths(PathSelectors.any())
        .build();
  }
}
