package com.yupi.yupaoBackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yupi.yupaoBackend.common.ErrorCode;
import com.yupi.yupaoBackend.exception.BusinessException;
import com.yupi.yupaoBackend.model.domain.User;
import com.yupi.yupaoBackend.model.vo.UserVO;
import com.yupi.yupaoBackend.service.UserService;

import com.yupi.yupaoBackend.mapper.UserMapper;
import com.yupi.yupaoBackend.utils.AlgorithmUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.yupi.yupaoBackend.contant.UserConstant.ADMIN_ROLE;
import static com.yupi.yupaoBackend.contant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现类
 *
* @author 陈君哲
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2024-05-24 11:23:43
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    //盐值，混淆密码，加密
    private static final String SALT = "chen";

    @Resource
    private UserMapper userMapper;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
        //1.校验
        if (StringUtils.isAnyBlank(userAccount,userPassword,checkPassword,planetCode)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        if (userAccount.length() < 4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号小于4位");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户密码小于8位");
        }
        if (planetCode.length() > 5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"星球编号过长");
        }


        //账户不能含有特殊字符
        String validPattern = "\\pP|\\pS|\\s+";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账户含有特殊字符");
        }

        //账户不能重复
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserAccount, userAccount);
        long count = this.count(queryWrapper);
        if (count > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账户重复");
        }

        //星球编号不能重复
        queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPlanetCode, planetCode);
        count = this.count(queryWrapper);
        if (count > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"星球编号重复");
        }

        //密码和校验密码相同
        if (!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码和校验密码不相同相同");
        }

        //2.对密码进行加密
        String md5Password = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        //3.插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(md5Password);
        user.setPlanetCode(planetCode);
        user.setAvatarUrl("https://tse1-mm.cn.bing.net/th/id/OIP-C.YxR-i4hhHwwQSBt67y1DkgAAAA?w=163&h=180&c=7&r=0&o=5&pid=1.7");
        user.setUsername(userAccount);
        boolean saveResult = this.save(user);
        if (!saveResult){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"注册失败");
        }
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {

        //1.校验
        if (StringUtils.isAnyBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        if (userAccount.length() < 4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号小于4位");
        }
        if (userPassword.length() < 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码小于8位");
        }

        //账户不能含有特殊字符
        String validPattern = "\\pP|\\pS|\\s+";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账户含有特殊字符");
        }

        //2.对密码进行加密
        String md5Password = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        //查询用户是否存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserAccount, userAccount);
        User user = this.getOne(queryWrapper);

        //用户不存在
        if (user == null){
            log.info("user login fail, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.NULL_ERROR,"用户不存在");
        }

        //用户名存在，但没有对应密码（密码错误）
        queryWrapper.eq(User::getUserPassword, md5Password);
        user = this.getOne(queryWrapper);
        if (user == null){
            log.info("user login fail, password cannot found!");
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码错误");
        }

        //3. 用户信息脱敏
        User safetyUser = getSafetyUser(user);
        //4. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);

        return safetyUser;
    }


    /**
     * 用户信息脱敏
     *
     * @param originUser
     * @return
     */
    @Override
    public User getSafetyUser(User originUser){
        if(originUser == null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"用户不存在");
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        //星球编号
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setTags(originUser.getTags());

        return safetyUser;
    }

    /**
     * 用户注销
     *
     * @return
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        //移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    /**
     * 根据标签搜索用户 （内存过滤）
     *
     * @param tagNameList 标签
     * @return
     */
    @Override
    public List<User> searchUsersByTags(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //1.查询所有用户
        List<User> userList = userMapper.selectList(null);
        Gson gson = new Gson();
        //2.过滤掉不包含传入标签的用户
        return userList.stream().filter(user -> {
            String tagsStr = user.getTags();
            if (StringUtils.isBlank(tagsStr)){
                return false;
            }
            //字符串反序列化
            Set<String> tempTagName = gson.fromJson(tagsStr, new TypeToken<Set<String>>(){}.getType());
            tempTagName = Optional.ofNullable(tempTagName).orElse(new HashSet<>());
            for (String tagName : tagNameList) {
                if(!tempTagName.contains(tagName)){
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());

    }

    /**
     * 更新用户信息
     * @param user
     * @return
     */
    @Override
    public int updateUser(User user, User loginUser) {
        long userId = user.getId();
        if (userId <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //todo 如果前端传来的值只有id，其余为空，则抛出参数异常
        /*User checkUser = new User();
        checkUser.setId(userId);
        if (checkUser.equals(user)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }*/

        //权限校验，仅管理员和自己可修改
        //如果是管理员，允许更新任意用户
        //如果不是管理员，允许更新任意用户
        if (!isAdmin(loginUser) && userId != loginUser.getId()){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User oldUser = userMapper.selectById(userId);
        if (oldUser == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return  userMapper.updateById(user);

    }

    /**
     * 获取当前登录用户信息
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (request == null){
            return null;
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null){
            throw  new BusinessException(ErrorCode.NO_LOGIN);
        }
        return (User) userObj;
    }


    /**
     * 判断是否为管理员
     *
     * @param request
     * @return
     */
    public boolean isAdmin(HttpServletRequest request){
        //仅管理员可操作
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User)userObj;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    public boolean isAdmin(User loginUser){
        //仅管理员可操作
        return loginUser.getUserRole() == ADMIN_ROLE;
    }

    @Override
    public List<User> matchUsers(long num, User loginUser) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>();
        queryWrapper.select(User::getId,User::getTags);
        queryWrapper.isNotNull(User::getTags);
        List<User> userList = list(queryWrapper);
        String tags = loginUser.getTags();
        System.out.println("=========="+ tags);
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {}.getType());
        //用户列表的下标 => 相似度
        List<ImmutablePair<User,Long>> list = new ArrayList<>();
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            String userTags = user.getTags();
            //无标签
            if (StringUtils.isBlank(userTags) || user.getId() == loginUser.getId()){
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>(){}.getType());
            //计算分数
            long distance = AlgorithmUtils.minDistance(userTagList, tagList);
            list.add(new ImmutablePair<>(user,distance));
        }
        //按编辑距离由小到大排序
        List<ImmutablePair<User, Long>> toUserPairList = list.stream()
                .sorted((a, b) -> (int) (a.getValue() - b.getValue()))
                .limit(num)
                .collect(Collectors.toList());
        //从中取出 user 并脱敏
        List<Long> userIdList = toUserPairList.stream().map(pair -> pair.getKey().getId()).collect(Collectors.toList());
        LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.in(User::getId,userIdList);

        //list  有序   1, 2, 3
        //因为 QueryWrapper.in 会破坏有序序列，变为无序
        //通过一个 Map 映射使无序序列变为有序
        //Map<id, User>
        Map<Long, List<User>> userIdUserListMap = list(userQueryWrapper).stream().
                map(user -> getSafetyUser(user)).
                collect(Collectors.groupingBy(User::getId));
        ArrayList<User> finalUserList = new ArrayList<>();
        for (Long userId : userIdList) {
            finalUserList.add(userIdUserListMap.get(userId).get(0));
        }
        return finalUserList;
    }

    /**
     * 根据标签搜索用户（SQL 版）
     *
     * @param tagNameList 标签
     * @return
     */
    @Deprecated
    private List<User> searchUsersBySQL(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //数据库直接查
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        for (String tagName : tagNameList) {
            queryWrapper = queryWrapper.like(User::getTags, tagName);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        List<User> SafetyUserList = userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
        return SafetyUserList;

    }
}




