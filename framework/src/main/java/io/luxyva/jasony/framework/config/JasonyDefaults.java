package io.luxyva.jasony.framework.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface JasonyDefaults {

  interface  Mail {
    boolean enabled = false;
    String from = "";
    String baseUrl = "";
  }

  interface Security {

    interface ClientAuthorization {
      String accessTokenUri = null;
      String tokenServiceId = null;
      String clientId = null;
      String clientSecret = null;
    }

    interface Authentication {

      interface Jwt {
        String secret = null;
        String base64Secret = null;
        long tokenValidityInSeconds = 1800; //30 minutes
        long tokenValidityInSecondsForRememberMe = 2592000; // 30 days
      }
    }

    interface RememberMe {
      String key = null;
    }
  }

  interface Swagger {

    String title = "Application API";
    String description = "API documentation";
    String version = "0.0.1";
    String termsOfServiceUrl = null;
    String contactName = null;
    String contactUrl = null;
    String contactEmail = null;
    String license = null;
    String licenseUrl = null;
    String defaultIncludePattern = "/api/.*";
    String host = null;
    String[] protocols = {};
    boolean useDefaultResponseMessages = true;
  }

  interface Metrics {

    interface Jmx {

      boolean enabled = false;
    }

    interface Logs {

      boolean enabled = false;
      long reportFrequency = 60;

    }

    interface Prometheus {

      boolean enabled = false;
      String endpoint = "/prometheusMetrics";
    }
  }

  interface Logging {

    boolean useJsonFormat = false;

    interface Logstash {

      boolean enabled = false;
      String host = "localhost";
      int port = 5000;
      int queueSize = 512;
    }
  }

  interface Social {

    String redirectAfterSignIn = "/#/home";
  }

  interface Gateway {

    Map<String, List<String>> authorizedMicroservicesEndpoints = new LinkedHashMap<>();

    interface RateLimiting {

      boolean enabled = false;
      long limit = 100_000L;
      int durationInSeconds = 3_600;

    }
  }

  interface Ribbon {

    String[] displayOnActiveProfiles = null;
  }

  interface ClientApp {

    String name = "jasonyApp";
  }

  interface Registry {

    String password = null;
  }

}
