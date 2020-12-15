package io.luxyva.jasony.common.client;

import io.luxyva.jasony.common.models.UserVM;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "discuss")
public interface UserClient {

  @GetMapping("/api/userList")
  public List<UserVM> getAll();
}
