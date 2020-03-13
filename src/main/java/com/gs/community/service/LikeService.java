package com.gs.community.service;

import com.gs.community.enums.LikeTypeEnum;
import com.gs.community.exception.CustomizeErrorCode;
import com.gs.community.exception.CustomizeException;
import com.gs.community.mapper.CommentMapper;
import com.gs.community.mapper.ThumbExtMapper;
import com.gs.community.mapper.ThumbMapper;
import com.gs.community.model.Comment;
import com.gs.community.model.Thumb;
import com.gs.community.model.ThumbExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LikeService {

    @Autowired
    private ThumbMapper thumbMapper;
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private ThumbExtMapper thumbExtMapper;

    @Transactional
    public int insert(Thumb thumb) {
        if (thumb.getTargetId() == null || thumb.getTargetId() == 0) {
            throw new CustomizeException(CustomizeErrorCode.TARGET_PARAM_NOT_FOUND);
        }
        if (thumb.getType() == null || !LikeTypeEnum.isExist(thumb.getType())) {
            throw new CustomizeException(CustomizeErrorCode.TYPE_PARAM_WRONG);
        }
        if (thumb.getType() == LikeTypeEnum.COMMENT.getType()) {
            // 点赞评论
            Comment dbcomment = commentMapper.selectByPrimaryKey(thumb.getTargetId());
            if (dbcomment == null) {
                throw new CustomizeException(CustomizeErrorCode.COMMENT_NOT_FOUND);
            }
            ThumbExample thumbExample = new ThumbExample();
            thumbExample.createCriteria().andTargetIdEqualTo(thumb.getTargetId())
                    .andTypeEqualTo(thumb.getType())
                    .andLikerEqualTo(thumb.getLiker());
            List<Thumb> thumbs = thumbMapper.selectByExample(thumbExample);
            if (thumbs.size() >= 1)
                return 2022;
            thumbMapper.insert(thumb);

            // 增加点赞数
            dbcomment.setId(thumb.getTargetId());
            dbcomment.setLikeCount(1);
            thumbExtMapper.incLikeCount(dbcomment);
        } else {
            // 增加点赞数
        }
        return 0;
    }
}
