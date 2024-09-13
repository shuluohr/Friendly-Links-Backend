package com.yupi.yupaoBackend.service;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author 陈君哲
 */
@SpringBootTest
public class RedissonTest {

    @Resource
    private RedissonClient redissonClient;

    @Test
    public void testRedisson() {
        RList<String> list = redissonClient.getList("test-list");
        list.add("yupi");
        System.out.println("rlist: " + list.get(0));
        list.remove(0);
    }


}
