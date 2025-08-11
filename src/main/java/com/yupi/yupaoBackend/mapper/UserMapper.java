package com.yupi.yupaoBackend.mapper;

import com.yupi.yupaoBackend.model.domain.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author 14700
* @description 针对表【user(用户表)】的数据库操作Mapper
* @createDate 2024-05-24 11:23:43
* @Entity generator.domain.User
*/
public interface UserMapper extends BaseMapper<User> {

    void deleteById();

}




