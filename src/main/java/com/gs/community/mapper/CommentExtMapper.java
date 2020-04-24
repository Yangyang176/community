package com.gs.community.mapper;

import com.gs.community.dto.CommentQueryDTO;
import com.gs.community.model.Comment;

public interface CommentExtMapper {
    int incComment(Comment record);

    int countBySearch(CommentQueryDTO commentQueryDTO);
}