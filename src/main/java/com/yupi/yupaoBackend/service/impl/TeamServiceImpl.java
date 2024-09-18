package com.yupi.yupaoBackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.yupaoBackend.common.ErrorCode;
import com.yupi.yupaoBackend.contant.enums.TeamStatusEnum;
import com.yupi.yupaoBackend.exception.BusinessException;
import com.yupi.yupaoBackend.model.domain.Team;
import com.yupi.yupaoBackend.model.domain.User;
import com.yupi.yupaoBackend.model.domain.UserTeam;
import com.yupi.yupaoBackend.model.dto.TeamQuery;
import com.yupi.yupaoBackend.model.request.TeamJoinRequest;
import com.yupi.yupaoBackend.model.request.TeamQuitRequest;
import com.yupi.yupaoBackend.model.request.TeamUpdateRequest;
import com.yupi.yupaoBackend.model.vo.TeamUserVO;
import com.yupi.yupaoBackend.model.vo.UserVO;
import com.yupi.yupaoBackend.service.TeamService;
import com.yupi.yupaoBackend.mapper.TeamMapper;
import com.yupi.yupaoBackend.service.UserService;
import com.yupi.yupaoBackend.service.UserTeamService;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
* @author 14700
* @description 针对表【team(队伍表)】的数据库操作Service实现
* @createDate 2024-09-08 14:01:57
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redissonClient;
    /**
     * 创建队伍
     *
     * @param team
     * @param loginUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User loginUser) {
        //1. 请求参数是否为空？
        if(team == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"传入参数为空");
        }
        //2. 是否登录，未登录不允许创建
        if (loginUser == null){
            throw new BusinessException(ErrorCode.NO_LOGIN,"未登录不允许创建");
        }
        final long loginUserId = loginUser.getId();
        //3. 校验信息
        //   1. 队伍人数 > 1 且 <= 20
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if(!(maxNum > 1 && maxNum <= 20)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍人数不满足要求");
        }
        //   2. 队伍标题 <= 20
        String name = team.getName();
        if(StringUtils.isBlank(name) || name.length() > 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍标题不满足要求");
        }
        //   3. 描述 <= 512
        String description = team.getDescription();
        if(StringUtils.isNotBlank(description) && description.length() > 512){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍描述过长");
        }
        //   4. status 是否公开（int) 不传默认为 0（公开）
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (statusEnum == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍状态不满足要求");
        }
        //   5. 如果 status 是加密状态，一定要有密码，且密码 <= 32
        String password = team.getPassword();
        if (TeamStatusEnum.SECRET.equals(statusEnum)){
            if (StringUtils.isBlank(password) || password.length() > 32){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码设置不正确");
            }
        }

        //   6. 超时时间 > 当前时间
        Date date = new Date();
        Date expireTime = team.getExpireTime();
        if(expireTime.before(date)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"超时时间 > 当前时间");
        }
        //   7. 校验用户最多创建 5 个队伍
        //todo 有 bug，可能同时创建出来 100 个队伍
        LambdaQueryWrapper<Team> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Team::getUserId, loginUserId);
        long hasTeamNum = count(queryWrapper);
        if (hasTeamNum >= 5){
            throw new BusinessException(ErrorCode.NO_AUTH,"最多创建 5 个队伍");
        }
        //4. 插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(loginUserId);
        boolean result = save(team);
        Long teamId = team.getId();
        if (!result || teamId == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"创建队伍失败");
        }
        //5. 插入用户 => 队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(loginUserId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());

        result = userTeamService.save(userTeam);
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"创建队伍失败");
        }

        return teamId;
    }

    @Override
    public List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin) {
        LambdaQueryWrapper<Team> queryWrapper = new LambdaQueryWrapper<>();
        if (teamQuery != null){
            Long id = teamQuery.getId();
            if (id != null && id > 0){
                queryWrapper.eq(Team::getUserId, id);
            }
            List<Long> idList = teamQuery.getIdList();
            if (CollectionUtils.isNotEmpty(idList)){
                queryWrapper.in(Team::getId, idList);
            }
            String searchText = teamQuery.getSearchText();
            if (StringUtils.isNotBlank(searchText)){
                queryWrapper.and(qw->qw.like(Team::getName, searchText).or().like(Team::getDescription, searchText));
            }
            String name = teamQuery.getName();
            if (StringUtils.isNotBlank(name)){
                queryWrapper.like(Team::getName, name);
            }
            String description = teamQuery.getDescription();
            if (StringUtils.isNotBlank(description)){
                queryWrapper.like(Team::getDescription, description);
            }
            Integer maxNum = teamQuery.getMaxNum();
            // 查询最大人数相等的
            if (maxNum != null && maxNum > 0){
                queryWrapper.eq(Team::getMaxNum, maxNum);
            }
            Long userId = teamQuery.getUserId();
            // 根据创建人来查询
            if (userId != null && userId > 0){
                queryWrapper.eq(Team::getUserId, userId);
            }
            //根据状态来查询
            Integer status = teamQuery.getStatus();
            TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
            if (statusEnum == null){
                statusEnum = TeamStatusEnum.PUBLIC;
            }
            //只有管理员才能查看加密还有非公开的房间
            if (!isAdmin && statusEnum.equals(TeamStatusEnum.PRIVATE)){
                throw new BusinessException(ErrorCode.NO_AUTH, "没有权限");
            }
            queryWrapper.eq(Team::getStatus, statusEnum.getStatus());

        }
        //不展示已过期的队伍（根据过期时间筛选）
        //expire is null or expireTime > now()
        queryWrapper.and(qw -> qw.gt(Team::getExpireTime, new Date()).or().isNull(Team::getExpireTime));
        List<Team> teamList = list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)){
            return new ArrayList<>();
        }
        ArrayList<TeamUserVO> teamUserVOList = new ArrayList<>();
        //关联查询创建人的用户信息
        //todo 关联查询已加入队伍的用户信息（todo 自己写 sql 的方式实现，不然很耗性能）
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if (userId == null){
                continue;
            }
            User user = userService.getById(userId);
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);
            //脱敏用户信息
            if (user != null){
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                teamUserVO.setCreateUser(userVO);
            }
            teamUserVOList.add(teamUserVO);
        }

        return teamUserVOList;
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        //1. 请求参数是否为空？
        if(teamUpdateRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"传入参数为空");
        }
        //2. 判断队伍是否存在
        Long id = teamUpdateRequest.getId();
        if (id == null || id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team oldTeam = getById(id);
        if (oldTeam == null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍不存在");
        }
        //3. 只有管理员和队伍的创建者可以修改队伍信息
        if (oldTeam.getUserId() != loginUser.getId() && !userService.isAdmin(loginUser)){
            throw new BusinessException(ErrorCode.NO_AUTH,"无权限");
        }
        //4如果队伍状态改为加密，必须要有密码
        TeamStatusEnum newTeamStatus = TeamStatusEnum.getEnumByValue(teamUpdateRequest.getStatus());
        TeamStatusEnum oldTeamStatus = TeamStatusEnum.getEnumByValue(oldTeam.getStatus());
        if (newTeamStatus.equals(TeamStatusEnum.SECRET)){
            String password = teamUpdateRequest.getPassword();
            if (StringUtils.isBlank(password) && !oldTeamStatus.equals(TeamStatusEnum.SECRET)){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"加密房间要设置密码");
            }
        }
        //5. todo 如果用户传入的新值和老值一致，则不用 update 了（自行实现）
        //6. 更新成功
        Team updateTeam = new Team();
        BeanUtils.copyProperties(teamUpdateRequest, updateTeam);
        boolean result = updateById(updateTeam);
        return result;
    }

    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        if (teamJoinRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"传入参数为空");
        }

        Long teamId = teamJoinRequest.getTeamId();
        if (teamId == null || teamId <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = getById(teamId);
        if (team == null){
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        if (team.getExpireTime() != null && team.getExpireTime().before(new Date())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期");
        }
        Integer status = team.getStatus();
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if (TeamStatusEnum.PRIVATE.equals(teamStatusEnum)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "禁止加入私有队伍");
        }
        String password = teamJoinRequest.getPassword();
        if (TeamStatusEnum.SECRET.equals(teamStatusEnum)){
            if (StringUtils.isBlank(password) || !password.equals(team.getPassword())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
            }
        }
        //该用户已加入的队伍数量
        long userId = loginUser.getId();
        //分布式锁解决多服务器多线程多条插入问题
        RLock lock = redissonClient.getLock("yupao:TeamServiceImpl:joinTeam:lock");
        try {
            while (true){
                if (lock.tryLock(0,-1, TimeUnit.MILLISECONDS)){
                    LambdaQueryWrapper<UserTeam> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(UserTeam::getUserId, userId);
                    long hasJoinNum = userTeamService.count(queryWrapper);
                    if (hasJoinNum > 5) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多创建和加入 5 个队伍");
                    }
                    //不能重复加入已加入的队伍
                    queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(UserTeam::getUserId, userId);
                    queryWrapper.eq(UserTeam::getTeamId, teamId);
                    long hasUserJoinTeamNum = userTeamService.count(queryWrapper);
                    if (hasUserJoinTeamNum >= 1) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已加入该队伍");
                    }

                    //已加入队伍的人数
                    long teamHasJoinNum = countTeamUserByTeamId(teamId);
                    if (teamHasJoinNum >= team.getMaxNum()){
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已满");
                    }
                    //新增队伍 - 用户关联信息
                    UserTeam userTeam = new UserTeam();
                    userTeam.setUserId(userId);
                    userTeam.setTeamId(teamId);
                    userTeam.setJoinTime(new Date());
                    return  userTeamService.save(userTeam);

                }
            }

        } catch (InterruptedException e) {
            log.error("doJoinTeamWork error",e);
            return false;
        } finally {
            //只能释放自己的锁
            if (lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
    }

    /**
     * 退出队伍
     *
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        if (teamQuitRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"传入参数为空");
        }
        Long teamId = teamQuitRequest.getTeamId();
        Team team = getTeamById(teamId);
        long userId = loginUser.getId();
        UserTeam queryUserTeam = new UserTeam();
        queryUserTeam.setUserId(userId);
        queryUserTeam.setTeamId(teamId);
        LambdaQueryWrapper<UserTeam> queryWrapper = new LambdaQueryWrapper<>(queryUserTeam);
        long count = userTeamService.count(queryWrapper);
        if (count == 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"未加入队伍");
        }
        long teamHasJoinNum = countTeamUserByTeamId(teamId);
        //队伍只剩一人，解散
        if(teamHasJoinNum == 1){
            // 删除队伍
            removeById(teamId);

        }else {
            // 队伍还剩至少两人
            // 是否为队长
            if (team.getUserId() == userId){
                // 把队伍转移给最早加入的用户
                // 1. 查询已加入队伍的所有用户和加入时间
                LambdaQueryWrapper<UserTeam> userTeamQueryWrapper = new LambdaQueryWrapper<>();
                userTeamQueryWrapper.eq(UserTeam::getTeamId, teamId);
                userTeamQueryWrapper.last("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
                if (CollectionUtils.isEmpty(userTeamList) || userTeamList.size() < 2){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                UserTeam nextUserTeam = userTeamList.get(1);
                Long nextTeamLeaderId = nextUserTeam.getUserId();
                // 更新当前队伍的队长
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(nextTeamLeaderId);
                boolean result = updateById(updateTeam);
                if (!result){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新队伍队长失败");
                }

            }// 移除关系

        }
        return userTeamService.remove(queryWrapper);
    }

    /**
     * 解散队伍
     *
     * @param id
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(long id, User loginUser) {
        // 1. 校验队伍是否存在
        Team team = getTeamById(id);
        long teamId = team.getId();
        // 2. 校验你是不是队伍的队长
        if (team.getUserId() != loginUser.getId()){
            throw new BusinessException(ErrorCode.NO_AUTH, "无访问权限");
        }
        // 3. 移除所有加入队伍的关联信息
        LambdaQueryWrapper<UserTeam> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserTeam::getTeamId, teamId);
        boolean result = userTeamService.remove(queryWrapper);
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        // 4. 删除队伍
        return  removeById(teamId);

    }

    /**
     * 获取某队伍当前人数
     * @param teamId
     * @return
     */
    private long countTeamUserByTeamId(long teamId){
        //已加入队伍的人数
        LambdaQueryWrapper<UserTeam> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserTeam::getTeamId, teamId);
        return  userTeamService.count(queryWrapper);

    }

    /**
     * 根据 id 获取队伍信息
     * @param teamId
     * @return
     */
    public Team getTeamById(Long teamId) {
        if (teamId == null || teamId <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = getById(teamId);
        if (team == null){
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        return team;
    }
}




