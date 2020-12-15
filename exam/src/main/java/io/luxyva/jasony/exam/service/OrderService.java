package io.luxyva.jasony.exam.service;

import io.luxyva.jasony.exam.domain.TOrder;
import io.luxyva.jasony.exam.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

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
