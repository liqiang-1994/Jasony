package io.luxyva.jasony.registry.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class H {

  @GetMapping("/h")
  public String h(){
    return "i am registry";
  }
}
