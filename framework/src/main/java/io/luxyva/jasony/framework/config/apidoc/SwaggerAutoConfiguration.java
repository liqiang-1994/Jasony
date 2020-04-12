package io.luxyva.jasony.framework.config.apidoc;

import io.luxyva.jasony.framework.config.JasonyProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.DispatcherServlet;
import springfox.documentation.schema.AlternateTypeRule;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.Servlet;
import java.nio.ByteBuffer;
import java.util.*;

import static springfox.documentation.builders.PathSelectors.regex;
import static io.luxyva.jasony.framework.config.JasonyConstants.SPRING_PROFILE_SWAGGER;

/**
 * Swagger注入配置类
 */
@Configuration
@ConditionalOnWebApplication
@ConditionalOnClass({
  ApiInfo.class,
  Servlet.class,
  DispatcherServlet.class,
  Docket.class
})
//@Profile(SPRING_PROFILE_SWAGGER)
@AutoConfigureAfter(JasonyProperties.class)
@EnableSwagger2
public class SwaggerAutoConfiguration {

  static final String STARTING_MESSAGE = "Starting Swagger";
  static final String STARTED_MESSAGE = "Started Swagger in {} ms";
  static final String MANAGEMENT_TITLE_SUFFIX = "Management API";
  static final String MANAGEMENT_GROUP_NAME = "management";
  static final String MANAGEMENT_DESCRIPTION = "Management endpoints documentation";

  private final JasonyProperties.Swagger swagger;

  private final Logger logger = LoggerFactory.getLogger(SwaggerAutoConfiguration.class);

  public SwaggerAutoConfiguration(JasonyProperties jasonyProperties) {
    this.swagger = jasonyProperties.getSwagger();
  }

  @Bean
  @ConditionalOnMissingBean(name = "swaggerSpringfoxApiDocket")
  public Docket swaggerSpringfoxApiDocket(List<SwaggerCustomizer> swaggerCustomizers,
                                          ObjectProvider<AlternateTypeRule> alternateTypeRules) {
    logger.debug(STARTING_MESSAGE);
    StopWatch watch = new StopWatch();
    watch.start();
    Docket docket = createDocket();
    swaggerCustomizers.forEach(customizer -> customizer.customize(docket));

    Optional.ofNullable(alternateTypeRules.getIfAvailable()).ifPresent(docket::alternateTypeRules);
    watch.stop();
    logger.debug(STARTED_MESSAGE, watch.getTotalTimeMillis());
    return docket;
  }

  @Bean
  public JasonySwaggerCustomizer jasonySwaggerCustomizer() {
    return new JasonySwaggerCustomizer(swagger);
  }

//  @Bean
//  @ConditionalOnClass(name = "org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties")
//  @ConditionalOnProperty("management.endpoints.web.base-path")
//  @ConditionalOnExpression("'${management.endpoints.web.base-path}'.length() > 0")
//  @ConditionalOnMissingBean(name = "swaggerSpringfoxManagementDocket")
//  public Docket swaggerSpringfoxManagementDocket(@Value("${spring.application.name:application}") String appName,
//                                                 @Value("${management.endpoints.web.base-path}") String managementContextPath) {
//    ApiInfo apiInfo = new ApiInfo(
//      StringUtils.capitalize(appName) + " " + MANAGEMENT_TITLE_SUFFIX,
//      MANAGEMENT_DESCRIPTION,
//      swagger.getVersion(),
//      "",
//      ApiInfo.DEFAULT_CONTACT,
//      "",
//      "",
//      new ArrayList<>()
//    );
//
//    return createDocket()
//      .apiInfo(apiInfo)
//      .useDefaultResponseMessages(swagger.isUseDefaultResponseMessages())
//      .groupName(MANAGEMENT_GROUP_NAME)
//      .host(swagger.getHost())
//      .protocols(new HashSet<>(Arrays.asList(swagger.getProtocols())))
//      .forCodeGeneration(true)
//      .directModelSubstitute(ByteBuffer.class, String.class)
//      .genericModelSubstitutes(ResponseEntity.class)
//      //.ignoredParameterTypes(Pageable.class)
//      .select()
//      .paths(regex(managementContextPath + ".*"))
//      .build();
//  }
@Bean
@ConditionalOnClass(name = "org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties")
@ConditionalOnProperty("management.endpoints.web.base-path")
@ConditionalOnExpression("'${management.endpoints.web.base-path}'.length() > 0")
@ConditionalOnMissingBean(name = "swaggerSpringfoxManagementDocket")
public Docket swaggerSpringfoxManagementDocket(@Value("${spring.application.name:application}") String appName,
                                               @Value("${management.endpoints.web.base-path}") String managementContextPath) {

  ApiInfo apiInfo = new ApiInfo(
    StringUtils.capitalize(appName) + " " + MANAGEMENT_TITLE_SUFFIX,
    MANAGEMENT_DESCRIPTION,
    swagger.getVersion(),
    "",
    ApiInfo.DEFAULT_CONTACT,
    "",
    "",
    new ArrayList<>()
  );

  return createDocket()
    .apiInfo(apiInfo)
    .useDefaultResponseMessages(swagger.isUseDefaultResponseMessages())
    .groupName(MANAGEMENT_GROUP_NAME)
    .host(swagger.getHost())
    .protocols(new HashSet<>(Arrays.asList(swagger.getProtocols())))
    .forCodeGeneration(true)
    .directModelSubstitute(ByteBuffer.class, String.class)
    .genericModelSubstitutes(ResponseEntity.class)
    //.ignoredParameterTypes(Pageable.class)
    .select()
    .paths(regex(managementContextPath + ".*"))
    .build();
}

  protected  Docket createDocket() {
    return new Docket(DocumentationType.SWAGGER_2);
  }
}
