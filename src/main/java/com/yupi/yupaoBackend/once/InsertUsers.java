package com.yupi.yupaoBackend.once;

import com.yupi.yupaoBackend.mapper.UserMapper;
import com.yupi.yupaoBackend.model.domain.User;
import com.yupi.yupaoBackend.service.UserService;
import jakarta.annotation.Resource;
import org.junit.Test;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.lang.model.type.ArrayType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author 陈君哲
 */
@Component
public class InsertUsers {

    private final Integer Insert_Users_NUM = 10000000;


    @Resource
    private UserMapper userMapper;

    @Resource
    private UserService userService;


//    @Scheduled(initialDelay = 5000,fixedRate = Long.MAX_VALUE)
    public void insertUsers() {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
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
            user.setTags("[]");
            userMapper.insert(user);


        }
        stopWatch.stop();
        System.out.println(stopWatch.prettyPrint());
    }


}
