package com.gs.community.mapper;

import com.gs.community.model.UserAccount;
import com.gs.community.model.UserAccountExample;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

public interface UserAccountExtMapper {
    int incScore(UserAccount userAccount);

    int decScore(UserAccount userAccount);
}