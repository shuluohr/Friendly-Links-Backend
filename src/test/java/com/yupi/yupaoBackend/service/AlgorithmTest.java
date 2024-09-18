package com.yupi.yupaoBackend.service;

import com.yupi.yupaoBackend.utils.AlgorithmUtils;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author 陈君哲
 */
public class AlgorithmTest {

    @Test
    void testTagList(){
        List<String> tagLis1 = Arrays.asList("Java", "大一", "男");
        List<String> tagLis2 = Arrays.asList("Java", "大一", "女");
        List<String> tagLis3 = Arrays.asList("Python", "大二", "女");
        int test1 = AlgorithmUtils.minDistance(tagLis1, tagLis2);
        int test2 = AlgorithmUtils.minDistance(tagLis1, tagLis3);
        System.out.println(test1);
        System.out.println(test2);
    }
}
