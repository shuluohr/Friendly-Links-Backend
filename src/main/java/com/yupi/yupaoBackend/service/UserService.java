package com.yupi.yupaoBackend.service;

import com.yupi.yupaoBackend.common.BaseResponse;
import com.yupi.yupaoBackend.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yupaoBackend.model.request.TeamJoinRequest;
import com.yupi.yupaoBackend.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

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

    /**
     * 根据标签搜索用户
     *
     * @param tagNameList 标签
     * @return
     */
    List<User> searchUsersByTags(List<String> tagNameList);

    /**
     * 更新用户信息
     * @param user
     * @return
     */
    int updateUser(User user, User loginUser);

    /**
     * 获取当前登录用户信息
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 判断是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    boolean isAdmin(User loginUser);

    /**
     * 获取最匹配的用户
     * @param num
     * @param loginUser
     * @return
     */
    List<User> matchUsers(long num, User loginUser);

    /**
     * 添加好友
     * @param id
     * @return
     */
    boolean joinFriend(long id,User loginUser);

    /**
     * 获取我的好友列表
     * @param loginUser
     * @return
     */
    List<UserVO> getMyJoinFriendList(User loginUser);
}
