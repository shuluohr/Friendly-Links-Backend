package com.yupi.yupaoBackend;



import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yupi.yupaoBackend.mapper.UserMapper;
import com.yupi.yupaoBackend.model.domain.User;
import com.yupi.yupaoBackend.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;


@Slf4j
@SpringBootTest
//@RunWith(Runner.class)
class UserCenterJavaApplicationTests {

    @Resource
    private UserMapper userMapper;

    @Test
    void contextLoads() {

        QueryWrapper<User> id = new QueryWrapper<User>().select("username").eq("userPassword", "123456");
        List<User> oldUser = userMapper.selectList(id);
        System.out.println(oldUser);
    }


    @Test
    void test(){
        HashMap<String, String> map = new HashMap<>();
        map.put("admin", "11");
        map.put("yupi", "22");


    }

}
