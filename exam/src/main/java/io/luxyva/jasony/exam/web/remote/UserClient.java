package io.luxyva.jasony.exam.web.remote;

//import io.luxyva.jasony.common.models.UserVM;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "discuss")
public interface UserClient {

//  @GetMapping("/api/userList")
//  public List<UserVM> getAll();


}
