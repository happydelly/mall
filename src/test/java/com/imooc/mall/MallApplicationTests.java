package com.imooc.mall;

import com.imooc.mall.dao.CategoryMapper;
import com.imooc.mall.pojo.Category;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MallApplicationTests {

    @Autowired
    private CategoryMapper categoryMapper;



    @Test
    public void contextLoads() {
        Category category  = categoryMapper.findById(100001);
       //Assert.assertTrue(category!=null);
        System.out.println(category.toString());
    }

    @Test
    public void queryById(){
        Category category = categoryMapper.queryById(100001);
        System.out.println(category.toString());
    }

}
