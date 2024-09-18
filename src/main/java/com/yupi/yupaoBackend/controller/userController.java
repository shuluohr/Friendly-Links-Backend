package com.yupi.yupaoBackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yupaoBackend.common.BaseResponse;
import com.yupi.yupaoBackend.common.ErrorCode;
import com.yupi.yupaoBackend.common.PageRequest;
import com.yupi.yupaoBackend.common.ResultUtils;
import com.yupi.yupaoBackend.exception.BusinessException;
import com.yupi.yupaoBackend.model.domain.User;
import com.yupi.yupaoBackend.model.request.UserLoginRequest;
import com.yupi.yupaoBackend.model.request.UserRegisterRequest;
import com.yupi.yupaoBackend.model.vo.UserVO;
import com.yupi.yupaoBackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.naming.EjbRef;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.yupi.yupaoBackend.contant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 *
 * @author 陈君哲
 */
@Slf4j
@RestController
@RequestMapping("user")
@CrossOrigin(allowCredentials = "true",origins = {"http://localhost:5173"},methods={RequestMethod.POST,RequestMethod.GET})
//@CrossOrigin(origins = {"http://120.26.249.212/api"},methods = {RequestMethod.GET, RequestMethod.POST})
public class userController {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate redisTemplate;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        if (userRegisterRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        if (StringUtils.isAnyBlank(userAccount,userPassword,checkPassword,planetCode)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        return ResultUtils.success(ErrorCode.OK,result);
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        if (userLoginRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();

        if (StringUtils.isAnyBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        User result = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(ErrorCode.OK,result);
    }

    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request){
        if (request == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.userLogout(request);
        return ResultUtils.success(ErrorCode.OK,result);
    }

    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request){
        User currentUser = (User)request.getSession().getAttribute(USER_LOGIN_STATE);
        if (currentUser == null){
            throw new BusinessException(ErrorCode.NO_LOGIN,"没有登录");
        }
        long userId = currentUser.getId();
        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(ErrorCode.OK,safetyUser);
    }

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUser(String username, HttpServletRequest request){
        //仅管理员可查询
        if (!userService.isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH, "没有管理员权限，仅管理员可查询");
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(username)){
            queryWrapper.like(User::getUsername, username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> result = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(ErrorCode.OK,result);
    }

    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList){
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList = userService.searchUsersByTags(tagNameList);
        return ResultUtils.success(ErrorCode.OK,userList);
    }

    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(PageRequest pageRequest, HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        String redisKey = String.format("yupao:user:recommend:%s", loginUser.getId());
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        //如果有缓存，直接读缓存
        Page<User> userPage = (Page<User>)valueOperations.get(redisKey);
        if(userPage != null){
            return ResultUtils.success(ErrorCode.OK,  userPage);
        }
        //无缓存，查数据库
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        userPage = userService.page(new Page<>(pageRequest.getPageNum(),pageRequest.getPageSize()),queryWrapper);

        //写缓存
        try {
            valueOperations.set(redisKey,userPage,30000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("redis set key error",e);
        }
        return ResultUtils.success(ErrorCode.OK,userPage);

    }

    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user,HttpServletRequest request){
        //1.校验参数是否为空
        if (user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2.校验权限，service里实现
        //3.触发更新
        User loginUser = userService.getLoginUser(request);
        Integer result = userService.updateUser(user,loginUser);
        //更新失败
        if (result < 1){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //更新成功
        return ResultUtils.success(ErrorCode.OK, result);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id,HttpServletRequest request){
        //仅管理员可删除
        if (!userService.isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH, "没有管理员权限，仅管理员可查询");
        }
        if (id <= 0){
            throw new BusinessException(ErrorCode.NULL_ERROR, "没有对应的用户信息");
        }

        boolean result = userService.removeById(id);
        return ResultUtils.success(ErrorCode.OK,result);
    }


    /**
     * 获取最匹配的用户
     * @param num
     * @param request
     * @return
     */
    @GetMapping("/match")
    public BaseResponse<List<User>> matchUsers(long num, HttpServletRequest request){
        if (num <= 0 || num > 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        List<User> result = userService.matchUsers(num, loginUser);
        return ResultUtils.success(ErrorCode.OK, result);
    }

}
