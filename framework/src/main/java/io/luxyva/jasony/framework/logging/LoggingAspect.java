package io.luxyva.jasony.framework.logging;

import io.luxyva.jasony.framework.config.JasonyConstants;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

import java.util.Arrays;

@Aspect
public class LoggingAspect {

  private final Environment env;

  public LoggingAspect(Environment env) {
    this.env = env;
  }

  @Pointcut("within(@org.springframework.stereotype.Repository *)" +
    " || within(@org.springframework.stereotype.Service *)" +
    " || within(@org.springframework.web.bind.annotation.RestController *)")
  public void springBeanPointcut() {

  }

  @Pointcut("within(io.luxyva.jasony.*.mapper..*)"+
    " || within(io.luxyva.jasony.*.service..*)"+
    " || within(io.luxyva.jasony.*.web.rest..*)")
  public void applicationPackagePointcut() {

  }

  @AfterThrowing(pointcut = "applicationPackagePointcut() && springBeanPointcut()", throwing = "e")
  public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
    if (env.acceptsProfiles(Profiles.of(JasonyConstants.SPRING_PROFILE_DEVELOPMENT))) {
      logger(joinPoint)
        .error(
          "Exception in {}() with case = '{}' and exception = '{}'",
          joinPoint.getSignature().getName(),
          e.getCause() != null ? e.getCause() : "NULL",
          e.getMessage(),
          e
          );
    } else {
      logger(joinPoint)
        .error(
          "Exception in {}() with case = {}",
          joinPoint.getSignature().getName(),
          e.getCause() != null ? e.getCause() : "NULL"
          );
    }
  }

  @Around("applicationPackagePointcut() && springBeanPointcut()")
  public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
    Logger log = logger(joinPoint);
    if (log.isDebugEnabled()) {
      log.debug("Enter: {}() with argument[s] = {}", joinPoint.getSignature().getName(), Arrays.toString(joinPoint.getArgs()));
    }
    try {
      Object result = joinPoint.proceed();
      if (log.isDebugEnabled()) {
        log.debug("Exit: {}() with result = {}", joinPoint.getSignature().getName(), result);
      }
      return result;
    } catch (IllegalArgumentException e) {
      log.error("Illegal argument: {} in {}()", Arrays.toString(joinPoint.getArgs()), joinPoint.getSignature().getName());
      throw e;
    }
  }


  private Logger logger(JoinPoint joinPoint) {
    return LoggerFactory.getLogger(joinPoint.getSignature().getDeclaringTypeName());
  }


}
