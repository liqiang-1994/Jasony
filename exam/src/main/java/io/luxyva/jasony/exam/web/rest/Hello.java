package io.luxyva.jasony.exam.web.rest;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
//import io.luxyva.jasony.common.models.UserVM;
import io.luxyva.jasony.exam.domain.User;
import io.luxyva.jasony.exam.service.OrderService;
import io.luxyva.jasony.exam.service.UserService;
import io.luxyva.jasony.exam.web.remote.UserClient;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api")
@Retry(name = "backendA", fallbackMethod = "fuck1")
@RateLimiter(name = "backendA", fallbackMethod = "fuck2")
@CircuitBreaker(name = "backendA", fallbackMethod = "fuck3")
@Bulkhead(name = "backendA", fallbackMethod = "fuck4")
@Slf4j
public class Hello {

  @Autowired
  private UserService userService;
  @Autowired
  private OrderService orderService;

  public static int x = 1;

  @Autowired
  private UserClient userClient;
  @Autowired
  private RedissonClient redissonClient;

  @Value("${server.port}")
  private String port;


  @GetMapping("/hello")
  public String hello() throws Exception {
    RSet<User> userRSet1 = redissonClient.getSet("user1");
    User user1 = new User();
    user1.setId(1L);
    user1.setName("lq");
    user1.setNickName("liqiang");
    user1.setUnitId(1L);
    userRSet1.add(user1);

    User user3 = new User();
    user3.setId(2L);
    user3.setName("lqs");
    user3.setNickName("lssiqiang");
    user3.setUnitId(2L);
    userRSet1.add(user3);

    User user5 = new User();
    user5.setId(22L);
    user5.setName("lqs");
    user5.setNickName("lssiqiang");
    user5.setUnitId(4L);
    userRSet1.add(user5);

    RSet<User> userRSet2 = redissonClient.getSet("user2");
    User user2 = new User();
    user2.setId(1L);
    user2.setName("lq");
    user2.setNickName("liqiang");
    user2.setUnitId(3L);
    userRSet2.add(user2);

    User user4 = new User();
    user4.setId(2L);
    user4.setName("lqs");
    user4.setNickName("lssiqiang");
    user4.setUnitId(5L);
    userRSet2.add(user4);
    if (user1.equals(user2)) {
      log.info("---equals");
    }
    Set<User> x = userRSet1.readDiff("user2");

    userRSet1.delete();
    userRSet2.delete();

    //userService.insertUser();
    return "i am email" + port;
  }

  @GetMapping("/echo")
  public String echo() {
    RBucket<String> rBucket = redissonClient.getBucket("name");
    return rBucket.get();
  }


 /* @GetMapping("/user")
  public List<UserVM> getAll() {
    List<UserVM> userVM = userClient.getAll();
    return userVM;
  }*/

  @GetMapping("/fuck4")
  public String fuck4(Exception e) {
    log.info("fuck4 ........", e);
    //userService.insertUser();
    //orderService.insert();
    return "4Not service is available fuxk fuck fuvk";
  }

  public String fuck3(Exception e) {
    log.info("fuck3 ........", e);

    return "3Not service is available fuxk fuck fuvk";
  }

  public String fuck2(Exception e) {
    log.info("fuck2 ........", e);

    return "2Not service is available fuxk fuck fuvk";
  }

  public String fuck1(Exception e) {
    log.info("fuck1 ........", e);

    return "1Not service is available fuxk fuck fuvk";
  }

  @Bulkhead(name = "BACKEND4", type = Bulkhead.Type.THREADPOOL)
  public CompletableFuture<String> doSomethingAsync4() throws InterruptedException {
    Thread.sleep(500);
    return CompletableFuture.completedFuture("Test");
  }
  @Bulkhead(name = "BACKEND3", type = Bulkhead.Type.THREADPOOL)
  public CompletableFuture<String> doSomethingAsync3() throws InterruptedException {
    Thread.sleep(500);
    return CompletableFuture.completedFuture("Test");
  }
  @Bulkhead(name = "BACKEND2", type = Bulkhead.Type.THREADPOOL)
  public CompletableFuture<String> doSomethingAsync2() throws InterruptedException {
    Thread.sleep(500);
    return CompletableFuture.completedFuture("Test");
  }
  @Bulkhead(name = "BACKEND1", type = Bulkhead.Type.THREADPOOL)
  public CompletableFuture<String> doSomethingAsync1() throws InterruptedException {
    Thread.sleep(500);
    return CompletableFuture.completedFuture("Test");
  }
}
