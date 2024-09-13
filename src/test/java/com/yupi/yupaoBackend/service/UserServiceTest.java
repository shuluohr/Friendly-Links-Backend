package com.yupi.yupaoBackend.service;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.yupi.yupaoBackend.mapper.UserMapper;
import com.yupi.yupaoBackend.model.domain.User;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户服务测试
 *
 * @author 陈君哲
 */
@SpringBootTest
@RunWith(Runner.class)
class UserServiceTest {

    @Resource
    private UserService userService;


    @Test
    void testAddUser(){
        User user = new User();
        user.setUsername("YuPi");
        user.setUserAccount("123");
        user.setAvatarUrl("https://ts2.cn.mm.bing.net/th?id=OIP-C.hsnwZAr2R2xUr97ScoSjrgAAAA&w=250&h=250&c=8&rs=1&qlt=90&o=6&pid=3.1&rm=2");
        user.setGender(0);
        user.setUserPassword("123456");
        user.setPhone("87654321");
        user.setEmail("1234@qq.com");


        boolean result = userService.save(user);
        System.out.println(user.getId());
        assertTrue(result);
    }

    @Test
    public void userRegister() {
//        String userAccount = "yupi";
//        String userPassword = "";
//        String checkPassword = "123456";
//        long result = userService.userRegister(userAccount, userPassword, checkPassword);
//        Assertions.assertEquals(-1, result);
//        userAccount = "yu";
//        result = userService.userRegister(userAccount, userPassword, checkPassword);
//        Assertions.assertEquals(-1, result);
//        userPassword = "123456";
//        result = userService.userRegister(userAccount, userPassword, checkPassword);
//        Assertions.assertEquals(-1, result);
//        userAccount = " yu pi";
//        userPassword = "12345678";
//        result = userService.userRegister(userAccount, userPassword, checkPassword);
//        Assertions.assertEquals(-1, result);
//
//        checkPassword = "123456789";
//        result = userService.userRegister(userAccount, userPassword, checkPassword);
//        Assertions.assertEquals(-1, result);
//        userAccount = "123";
//        result = userService.userRegister(userAccount, userPassword, checkPassword);
//        Assertions.assertEquals(-1, result);
//        userAccount = "yupi";
//        userPassword = "123456789";
//        result = userService.userRegister(userAccount, userPassword, checkPassword);
//        Assertions.assertTrue(result > 0);
    }

    @Test
    public void searchUsersByTags() {
        List<String> tagName = Arrays.asList("java","python");
        List<User> userList = userService.searchUsersByTags(tagName);
        Assert.assertNotNull(userList);
    }

    /**
     * 批量插入
     */
    @Test
    public void insertUsers() {

        final Integer Insert_Users_NUM = 100000;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<User> userList = new ArrayList();
        for (int i = 0; i < Insert_Users_NUM; i++) {
            User user = new User();
            user.setUsername("假用户");
            user.setUserAccount("假账户");
            user.setAvatarUrl("https://tse1-mm.cn.bing.net/th/id/OIP-C.YxR-i4hhHwwQSBt67y1DkgAAAA?w=163&h=180&c=7&r=0&o=5&pid=1.7");
            user.setGender(1);
            user.setUserPassword("12345678");
            user.setPhone("8127383234");
            user.setEmail("12345678@qq.com");
            user.setUserRole(0);
            user.setPlanetCode("11111"+i);
            user.setTags("[假]");
            userList.add(user);
        }
        userService.saveBatch(userList,10000);
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());



    }





    
}