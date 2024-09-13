package com.yupi.yupaoBackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.yupaoBackend.model.domain.UserTeam;
import com.yupi.yupaoBackend.service.UserTeamService;
import com.yupi.yupaoBackend.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author 14700
* @description 针对表【user_team(用户队伍关系表)】的数据库操作Service实现
* @createDate 2024-09-08 14:05:32
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




