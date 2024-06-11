package com.yupi.usercenter_java.service;

import com.yupi.usercenter_java.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户服务
 *
* @author 陈君哲
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2024-05-24 11:23:43
*/
public interface UserService extends IService<User> {



    /**
     * 用户注册  账户、密码、校验码
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @param planetCode 星球编号
     * @return  新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword,String planetCode);

    /**
     * 用户登录
     * @param userAccount
     * @param userPassword
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    User getSafetyUser(User originUser);

    /**
     *
     *用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);
}
