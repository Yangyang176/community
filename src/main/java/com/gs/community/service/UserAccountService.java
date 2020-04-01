package com.gs.community.service;

import com.gs.community.mapper.UserAccountMapper;
import com.gs.community.model.UserAccount;
import com.gs.community.model.UserAccountExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserAccountService {

    @Autowired
    private UserAccountMapper userAccountMapper;
    public UserAccount selectUserAccountByUserId(Integer userId) {
        UserAccountExample userAccountExample = new UserAccountExample();
        userAccountExample.createCriteria().andUserIdEqualTo(userId);
        List<UserAccount> userAccounts = userAccountMapper.selectByExample(userAccountExample);
        UserAccount userAccount = userAccounts.get(0);
        return userAccount;
    }

    public boolean isAdminByUserId(Integer userId) {
        if (selectUserAccountByUserId(userId).getGroupId()>=18) return true;
        else return false;
    }
}
