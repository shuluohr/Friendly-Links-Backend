package com.yupi.yupaoBackend.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yupaoBackend.common.BaseResponse;
import com.yupi.yupaoBackend.common.DeleteRequest;
import com.yupi.yupaoBackend.common.ErrorCode;
import com.yupi.yupaoBackend.common.ResultUtils;
import com.yupi.yupaoBackend.exception.BusinessException;
import com.yupi.yupaoBackend.model.domain.UserTeam;
import com.yupi.yupaoBackend.model.dto.TeamMemberGetDTO;
import com.yupi.yupaoBackend.model.dto.TeamQuery;
import com.yupi.yupaoBackend.model.domain.Team;
import com.yupi.yupaoBackend.model.domain.User;
import com.yupi.yupaoBackend.model.request.*;
import com.yupi.yupaoBackend.model.vo.TeamUserVO;
import com.yupi.yupaoBackend.service.TeamService;
import com.yupi.yupaoBackend.service.UserService;
import com.yupi.yupaoBackend.service.UserTeamService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 队伍接口
 *
 * @author 陈君哲
 */
@Slf4j
@RestController
@RequestMapping("team")
@CrossOrigin(allowCredentials = "true",originPatterns={"http://localhost:5173","http://ip:5173","http://ip"},methods={RequestMethod.POST,RequestMethod.GET})
//@CrossOrigin(allowCredentials = "true",origins = {"http://localhost:5173"},methods={RequestMethod.POST,RequestMethod.GET})
public class TeamController {

    @Resource
    private TeamService teamService;

    @Resource
    private UserService userService;

    @Resource
    private UserTeamService userTeamService;


    /**
     * 创建队伍
     * @param teamAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addTeam (@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) throws InterruptedException {
        if (teamAddRequest ==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest, team);
        User loginUser = userService.getLoginUser(request);
        Long teamId = teamService.addTeam(team, loginUser);
        return ResultUtils.success(ErrorCode.OK, team.getId());
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
    public BaseResponse<List<TeamUserVO>> getTeam(long id, HttpServletRequest request) {
        if (id <= 0 ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");
        }
        Team team = teamService.getById(id);

        if (team == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取队伍信息失败");
        }
        TeamUserVO teamUserVO = new TeamUserVO();
        BeanUtils.copyProperties(team, teamUserVO);
        // 2.判断用户是否已加入队伍
        LambdaQueryWrapper<UserTeam> userTeamQueryWrapper = new LambdaQueryWrapper<>();
        try {
            User loginUser = userService.getLoginUser(request);
            userTeamQueryWrapper.eq(UserTeam::getUserId, loginUser.getId());
            userTeamQueryWrapper.eq(UserTeam::getTeamId, team.getId());
            List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
            // 已加入的队伍 id 集合
            Set<Long> hasJoinTeamIdSet = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            boolean hasJoin = hasJoinTeamIdSet.contains(team.getId());
            teamUserVO.setHasJoinTeam(hasJoin);
        } catch (Exception e) {}
        // 3.判断已加入队伍的人数
        this.setHasJoinTeamNum(Arrays.asList(teamUserVO));
        return ResultUtils.success(ErrorCode.OK, Arrays.asList(teamUserVO));
    }

    @GetMapping("/list")
    public BaseResponse<List<TeamUserVO>> listTeams(TeamQuery teamQuery, HttpServletRequest request){
        if (teamQuery == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        boolean isAdmin = userService.isAdmin(request);
        // 1.查询队伍列表
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, isAdmin);
        // 2.判断用户是否已加入队伍
        final List<Long> teamIdList = teamList.stream().map(TeamUserVO::getId).toList();
        LambdaQueryWrapper<UserTeam> userTeamQueryWrapper = new LambdaQueryWrapper<>();
        try {
            User loginUser = userService.getLoginUser(request);
            userTeamQueryWrapper.eq(UserTeam::getUserId, loginUser.getId());
            userTeamQueryWrapper.in(UserTeam::getTeamId, teamIdList);
            List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
            // 已加入的队伍 id 集合
            Set<Long> hasJoinTeamIdSet = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            teamList.forEach(team -> {
                boolean hasJoin = hasJoinTeamIdSet.contains(team.getId());
                team.setHasJoinTeam(hasJoin);
            });
        } catch (Exception e) {}
        // 3.判断已加入队伍的人数
        this.setHasJoinTeamNum(teamList);
        return ResultUtils.success(ErrorCode.OK, teamList);
    }

    //todo
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

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");
        }
        long id = deleteRequest.getId();
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.deleteTeam(id,loginUser);
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "队伍删除失败");
        }
        return ResultUtils.success(ErrorCode.OK, true);
    }

    /**
     * 获取我创建的队伍
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/my/create")
    public BaseResponse<List<TeamUserVO>> listMyCreateTeams(TeamQuery teamQuery, HttpServletRequest request){
        if (teamQuery == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        User loginUser = userService.getLoginUser(request);
        teamQuery.setUserId(loginUser.getId());
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, true);
        // 已加入的队伍 id 集合
        // 将已加入的队伍 设为true

        teamList.forEach(team -> {
            team.setHasJoinTeam(true);
        });

        // 3.判断已加入队伍的人数
        this.setHasJoinTeamNum(teamList);
        return ResultUtils.success(ErrorCode.OK, teamList);
    }

    /**
     * 获取我加入的队伍
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamUserVO>> listMyJoinTeams(TeamQuery teamQuery, HttpServletRequest request){
        if (teamQuery == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        User loginUser = userService.getLoginUser(request);
        LambdaQueryWrapper<UserTeam> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserTeam::getUserId, loginUser.getId());
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);

        // 3.判断已加入队伍的人数

        // 取出不重复的队伍 id
        // teamId userId
        // 1, 2
        // 1, 3
        // 2, 3
        // result
        // 1 => 2, 3
        // 2 => 3
        Map<Long, List<UserTeam>> listMap = userTeamList.stream()
                .collect(Collectors.groupingBy(UserTeam::getTeamId));
        ArrayList<Long> idList = new ArrayList<>(listMap.keySet());
        teamQuery.setIdList(idList);
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, true);
        // 将已加入的队伍 设为true
        teamList.forEach(team -> {
            team.setHasJoinTeam(true);
        });
        // 3.判断已加入队伍的人数
        this.setHasJoinTeamNum(teamList);
        return ResultUtils.success(ErrorCode.OK, teamList);

    }

    @GetMapping("/member/list")
    public BaseResponse<List<User>> TeamMemberList(TeamMemberGetDTO teamMemberGetRequest){
        if (teamMemberGetRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        List<User> memberList = teamService.memberList(teamMemberGetRequest);

        return ResultUtils.success(ErrorCode.OK, memberList);
    }

    // 判断已加入队伍的人数
    private void setHasJoinTeamNum(List<TeamUserVO> teamList){
        List<Long> teamIdList = teamList.stream().map(team -> team.getId()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(teamIdList)){
            throw new BusinessException(ErrorCode.NULL_ERROR, "暂无队伍");
        }
        LambdaQueryWrapper<UserTeam> userTeamJoinQueryWrapper =  new LambdaQueryWrapper<>();
        userTeamJoinQueryWrapper.in(UserTeam::getTeamId, teamIdList);
        List<UserTeam> userTeamList = userTeamService.list(userTeamJoinQueryWrapper);
        //队伍 id => 加入这个队伍的用户列表
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamList.forEach(team -> team.setHasJoinTeamNum(teamIdUserTeamList.getOrDefault(team.getId(), new ArrayList<>()).size()));
    }


}
