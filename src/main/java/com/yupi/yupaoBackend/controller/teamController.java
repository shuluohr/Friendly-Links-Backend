package com.yupi.yupaoBackend.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yupaoBackend.common.BaseResponse;
import com.yupi.yupaoBackend.common.ErrorCode;
import com.yupi.yupaoBackend.common.ResultUtils;
import com.yupi.yupaoBackend.exception.BusinessException;
import com.yupi.yupaoBackend.model.dto.TeamQuery;
import com.yupi.yupaoBackend.model.domain.Team;
import com.yupi.yupaoBackend.model.domain.User;
import com.yupi.yupaoBackend.model.request.TeamAddRequest;
import com.yupi.yupaoBackend.model.request.TeamJoinRequest;
import com.yupi.yupaoBackend.model.request.TeamQuitRequest;
import com.yupi.yupaoBackend.model.request.TeamUpdateRequest;
import com.yupi.yupaoBackend.model.vo.TeamUserVO;
import com.yupi.yupaoBackend.service.TeamService;
import com.yupi.yupaoBackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * 队伍接口
 *
 * @author 陈君哲
 */
@Slf4j
@RestController
@RequestMapping("team")
@CrossOrigin(allowCredentials = "true",origins = {"http://localhost:5173"},methods={RequestMethod.POST,RequestMethod.GET})
public class teamController {

    @Resource
    private TeamService teamService;

    @Resource
    private UserService userService;


    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if (teamAddRequest ==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest, team);
        User loginUser = userService.getLoginUser(request);
        Long result = teamService.addTeam(team, loginUser);
        return ResultUtils.success(ErrorCode.OK, team.getId());
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody long id,HttpServletRequest request) {
        if (id <= 0 ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.deleteTeam(id,loginUser);
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "队伍删除失败");
        }
        return ResultUtils.success(ErrorCode.OK, true);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        if (teamUpdateRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.updateTeam(teamUpdateRequest, loginUser);
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "队伍修改失败");
        }
        return ResultUtils.success(ErrorCode.OK, true);
    }

    @GetMapping("/get")
    public BaseResponse<Team> getTeam(long id) {
        if (id <= 0 ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");
        }
        Team result = teamService.getById(id);
        if (result == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取队伍信息失败");
        }
        return ResultUtils.success(ErrorCode.OK, result);
    }

    @GetMapping("/list")
    public BaseResponse<List<TeamUserVO>> listTeams(TeamQuery teamQuery, HttpServletRequest request){
        if (teamQuery == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        boolean isAdmin = userService.isAdmin(request);
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, isAdmin);
        return ResultUtils.success(ErrorCode.OK, teamList);
    }

    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamsByPage(TeamQuery teamQuery) throws InvocationTargetException, IllegalAccessException {
        if (teamQuery == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamQuery, team);
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> page = new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize());
        Page<Team> teamListPage = teamService.page(page,queryWrapper);
        if (teamListPage == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取队伍列表信息失败");
        }
        return ResultUtils.success(ErrorCode.OK, teamListPage);
    }

    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        if (teamJoinRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.joinTeam(teamJoinRequest, loginUser);
        return ResultUtils.success(ErrorCode.OK, result);
    }

    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request) {
        if (teamQuitRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.quitTeam(teamQuitRequest, loginUser);
        return ResultUtils.success(ErrorCode.OK, result);
    }

}
