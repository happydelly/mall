package com.imooc.mall.dao;

import com.imooc.mall.MallApplicationTests;
import com.imooc.mall.pojo.Order;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

public class OrderMapperTest extends MallApplicationTests {

    @Autowired
    private  OrderMapper orderMapper;

    @Test
    public void selectByPrimaryKey(){
        Order order = orderMapper.selectByPrimaryKey(123456);
        System.out.println(order.toString());
    }

}
