package com.gs.community.service;

import com.gs.community.mapper.UserMapper;
import com.gs.community.model.User;
import com.gs.community.model.UserExample;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    public void createOrUpdate(User user) {
        UserExample userExample = new UserExample();
        userExample.createCriteria().andAccountIdEqualTo(user.getAccountId());
        List<User> users = userMapper.selectByExample(userExample);
        if (users.size() == 0) {
            //插入
            if (user.getName() == null)
                user.setName(getUserName("GitHub"));
            user.setGmtCreate(System.currentTimeMillis());
            user.setGmtModified(user.getGmtCreate());
            userMapper.insert(user);
        } else {
            //修改
            User dbUser = users.get(0);
            User updateUser = new User();
            if (dbUser.getName() == null && user.getName() == null)
                updateUser.setName(getUserName("Github"));
            if (user.getName() != null)
                updateUser.setName(user.getName());
            updateUser.setAvatarUrl(user.getAvatarUrl());
            updateUser.setToken(user.getToken());
            updateUser.setGmtModified(System.currentTimeMillis());
            UserExample example = new UserExample();
            example.createCriteria().andIdEqualTo(dbUser.getId());
            userMapper.updateByExampleSelective(updateUser, example);
        }
    }

    private String getUserName(String authorizeSize) {
        String str = RandomStringUtils.random(5, "abcdefghijklmnopqrstuvwxyz1234567890");
        String name = authorizeSize + "用户_" + str;
        return name;
    }
}
