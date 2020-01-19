package com.imooc.mall.service.impl;

import com.imooc.mall.MallApplicationTests;
import com.imooc.mall.enums.RoleEnum;
import com.imooc.mall.pojo.User;
import com.imooc.mall.service.IUserService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import static org.junit.Assert.*;

public class UserServiceImplTest extends MallApplicationTests {

    @Autowired
    private IUserService iUserService;

    @Test
    public void register() {
        User user = new User();
        user.setUsername("jack");
        user.setPassword("12345");
        user.setEmail("12314@qq.com");
        user.setRole(RoleEnum.CUSTOMER.getCode());
        user.setCreateTime(new Date());
        user.setUpdateTime(user.getCreateTime());
        iUserService.register(user);

    }
}