package com.yupi.yupaoBackend.service;

import com.yupi.yupaoBackend.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yupaoBackend.model.domain.User;
import com.yupi.yupaoBackend.model.dto.TeamMemberGetDTO;
import com.yupi.yupaoBackend.model.dto.TeamQuery;
import com.yupi.yupaoBackend.model.request.TeamJoinRequest;
import com.yupi.yupaoBackend.model.request.TeamQuitRequest;
import com.yupi.yupaoBackend.model.request.TeamUpdateRequest;
import com.yupi.yupaoBackend.model.vo.TeamUserVO;

import java.util.List;

/**
* @author 14700
* @description 针对表【team(队伍表)】的数据库操作Service
* @createDate 2024-09-08 14:01:57
*/
public interface TeamService extends IService<Team> {

    /**
     *创建队伍
     *
     * @param team
     * @return
     */
    long addTeam(Team team, User loginUser);

    /**
     * 搜索队伍
     *
     * @param teamQuery
     * @return
     */
    List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin);

    /**
     * 修改队伍信息
     * @param teamUpdateRequest
     * @param loginUser
     * @return
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);

    /**
     * 加入队伍
     * @param teamJoinRequest
     * @return
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    /**
     * 退出队伍
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    /**
     * 解散队伍
     * @param id
     * @return
     */
    boolean deleteTeam(long id, User loginUser);

    /**
     * 获取队伍
     * @param teamMemberGetRequest
     * @return
     */
    List<User> memberList(TeamMemberGetDTO teamMemberGetRequest);
}
