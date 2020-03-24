package com.gs.community.service;

import com.gs.community.dto.ResultDTO;
import com.gs.community.exception.CustomizeErrorCode;
import com.gs.community.mapper.UserInfoMapper;
import com.gs.community.mapper.UserMapper;
import com.gs.community.model.User;
import com.gs.community.model.UserExample;
import com.gs.community.model.UserInfo;
import com.gs.community.model.UserInfoExample;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserInfoMapper userInfoMapper;

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

    public Object updateUserMailById(String userId, String mail) {
        UserExample userExample = new UserExample();
        userExample.createCriteria()
                .andEmailEqualTo(mail);
        List<User> users = userMapper.selectByExample(userExample);
        if (users.size() != 0) {
            return ResultDTO.errorOf(CustomizeErrorCode.SUBMIT_MAIL_FAILED);
        }
        User updateUser = new User();
        updateUser.setEmail(mail);
        UserExample example = new UserExample();
        example.createCriteria()
                .andIdEqualTo(Integer.parseInt(userId));
        try {
            userMapper.updateByExampleSelective(updateUser, example);
            return ResultDTO.okOf("邮箱绑定/更新成功！！！");
        } catch (Exception e) {
            return ResultDTO.errorOf(CustomizeErrorCode.SUBMIT_MAIL_FAILED);
        }

    }

    public Object registerOrLoginWithMail(String mail, String token) {

        UserExample userExample = new UserExample();
        userExample.createCriteria()
                .andEmailEqualTo(mail);
        List<User> users = userMapper.selectByExample(userExample);
        User updateUser = new User();
        if (users.size() != 0) {//登录
            User dbUser = users.get(0);
            if (dbUser.getName() == null || StringUtils.isBlank(dbUser.getName()))//数据库为空，当前为空
                updateUser.setName(getUserName("邮箱"));

            updateUser.setGmtModified(System.currentTimeMillis());
            updateUser.setToken(token);
            UserExample example = new UserExample();
            example.createCriteria()
                    .andIdEqualTo(dbUser.getId());
            userMapper.updateByExampleSelective(updateUser, example);
            return ResultDTO.okOf(token);
        } else {
            //注册
            updateUser.setEmail(mail);
            updateUser.setName(getUserName("邮箱"));
            updateUser.setToken(token);
            updateUser.setAvatarUrl("/img/avatar/" + (int) (Math.random() * 11) + ".jpg");
            updateUser.setGmtCreate(System.currentTimeMillis());
            updateUser.setGmtModified(updateUser.getGmtCreate());
            userMapper.insert(updateUser);
//            UserExample example = new UserExample();
//            example.createCriteria()
//                    .andEmailEqualTo(mail);
//            List<User> insertUsers = userMapper.selectByExample(example);
//            User insertUser = insertUsers.get(0);
//            initUserTable(insertUser,new UserInfo());
            return ResultDTO.okOf(token);
        }

    }

    public int createOrUpdateBaidu(User user, User loginuser, UserInfo userInfo) {
        UserExample userExample = new UserExample();
        userExample.createCriteria()
                .andBaiduAccountIdEqualTo(user.getBaiduAccountId());
        List<User> users = userMapper.selectByExample(userExample);
        User updateUser = new User();
        if (users.size() == 0) {  // 插入

            user.setGmtCreate(System.currentTimeMillis());
            user.setGmtModified(user.getGmtCreate());
            if (user.getName() == null || StringUtils.isBlank(user.getName()))
                user.setName(getUserName("Baidu"));
            if (loginuser == null) {//创建
                userMapper.insert(user);
                UserExample userExample2 = new UserExample();
                userExample2.createCriteria()
                        .andBaiduAccountIdEqualTo(user.getBaiduAccountId());
                List<User> users2 = userMapper.selectByExample(userExample2);
                if (users2.size() != 0) {//表示user表已创建
                    User dbUser = users2.get(0);
                    initUserTable(dbUser, userInfo);
                   /* userInfo.setUserId(dbUser.getId());
                    userInfoMapper.insert(userInfo);*/
                }
                return 1;
                //  userMapper.insert(user);

            }
            if (loginuser != null) {//绑定或者换绑
                updateUser.setBaiduAccountId(user.getBaiduAccountId());
                updateUserInfo(user, updateUser, loginuser, userInfo);
                return 2;
            }

        } else {
            //登录更新
            User dbUser = users.get(0);
            if (dbUser.getName() == null && (user.getName() == null || StringUtils.isBlank(user.getName())))//数据库为空，当前为空
                updateUser.setName(getUserName("Baidu"));
            updateUser.setGmtModified(System.currentTimeMillis());
            updateUser.setToken(user.getToken());
            // updateUser.setAvatarUrl(user.getAvatarUrl());
            UserExample example = new UserExample();
            example.createCriteria()
                    .andIdEqualTo(dbUser.getId());
            userMapper.updateByExampleSelective(updateUser, example);
            return 3;
        }
        return 0;
    }

    public void initUserTable(User user, UserInfo userInfo) {
        // UserInfo userInfo = new UserInfo();
        userInfo.setUserId(user.getId());
        userInfoMapper.insert(userInfo);
        /*UserAccount userAccount = new UserAccount();
        userAccount = initUserAccount(userAccount);
        userAccount.setUserId(user.getId());
        userAccountMapper.insert(userAccount);*/
        userInfo = null;
//        userAccount=null;
    }

    public void updateUserInfo(User user, User updateUser, User loginuser, UserInfo userInfo) {
        updateUser.setGmtModified(System.currentTimeMillis());
        updateUser.setToken(user.getToken());
        //updateUser.setAvatarUrl(user.getAvatarUrl());
        UserExample example = new UserExample();
        example.createCriteria()
                .andIdEqualTo(loginuser.getId());
        userMapper.updateByExampleSelective(updateUser, example);

        UserInfoExample userInfoExample = new UserInfoExample();
        userInfoExample.createCriteria()
                .andUserIdEqualTo(loginuser.getId());
        List<UserInfo> dbUserInfos = userInfoMapper.selectByExample(userInfoExample);
        if (dbUserInfos.size() == 0) {//信息为空插入
            userInfo.setUserId(loginuser.getId());
            userInfoMapper.insert(userInfo);
        } else {//信息不空更新
            UserInfoExample userInfEexample = new UserInfoExample();
            userInfEexample.createCriteria()
                    .andUserIdEqualTo(loginuser.getId());
            userInfoMapper.updateByExampleSelective(userInfo, userInfEexample);
        }

    }
}
