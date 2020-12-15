package io.luxyva.jasony.exam.web.rest;

import io.luxyva.jasony.exam.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {

  @Autowired
  private OrderService orderService;

  @GetMapping("/order")
  public void insertOrder() {
    orderService.insert();
  }
}
