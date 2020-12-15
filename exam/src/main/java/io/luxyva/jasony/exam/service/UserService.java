package io.luxyva.jasony.exam.service;

import io.luxyva.jasony.exam.domain.TOrder;
import io.luxyva.jasony.exam.domain.User;
import io.luxyva.jasony.exam.mapper.OrderMapper;
import io.luxyva.jasony.exam.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
@Slf4j
@Transactional
public class UserService {

  @Autowired
  private UserMapper userMapper;

  @Autowired
  private RedissonClient redissonClient;

  public void insertUser() {
    Random random = new Random();
    for (int i = 0; i < 10000; i++) {
      User user = new User();
      long next = random.nextLong();
      user.setId(Long.valueOf(next));
      user.setName("xx");
      user.setNickName("oo");
      log.info(next + "         ------------");
    }
  }

  @Autowired
  private OrderMapper orderMapper;

  public void insert() {
    for (int i = 0; i < 100; i++) {
      TOrder tOrder = new TOrder();
      tOrder.setId((long) i);
      tOrder.setName("name" + i);
      tOrder.setUserId((long) i);
      tOrder.setUnitId((long)i);
      orderMapper.insert(tOrder);
    }
  }
}
