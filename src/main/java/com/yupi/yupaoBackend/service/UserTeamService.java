package com.yupi.yupaoBackend.service;

import com.yupi.yupaoBackend.model.domain.User;
import com.yupi.yupaoBackend.model.domain.UserTeam;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yupaoBackend.model.dto.TeamMemberGetDTO;

import java.util.List;

/**
* @author 14700
* @description 针对表【user_team(用户队伍关系表)】的数据库操作Service
* @createDate 2024-09-08 14:05:32
*/
public interface UserTeamService extends IService<UserTeam> {

}
