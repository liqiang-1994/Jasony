package io.luxyva.jasony.framework.config.apidoc;

import springfox.documentation.spring.web.plugins.Docket;

@FunctionalInterface
public interface SwaggerCustomizer {

  void customize(Docket docket);
}
