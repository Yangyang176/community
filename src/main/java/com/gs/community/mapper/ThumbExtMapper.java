package com.gs.community.mapper;

import com.gs.community.model.Comment;
import com.gs.community.model.Thumb;
import com.gs.community.model.ThumbExample;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

public interface ThumbExtMapper {
    int incLikeCount(Comment comment);
}