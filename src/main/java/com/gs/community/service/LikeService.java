package com.gs.community.service;

import com.gs.community.enums.LikeTypeEnum;
import com.gs.community.enums.NotificationStatusEnum;
import com.gs.community.enums.NotificationTypeEnum;
import com.gs.community.exception.CustomizeErrorCode;
import com.gs.community.exception.CustomizeException;
import com.gs.community.mapper.*;
import com.gs.community.model.*;
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
    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private NotificationMapper notificationMapper;

    @Transactional
    public int insert(Thumb thumb, User user) {
        if (thumb.getTargetId() == null || thumb.getTargetId() == 0) {
            throw new CustomizeException(CustomizeErrorCode.TARGET_PARAM_NOT_FOUND);
        }
        if (thumb.getType() == null || !LikeTypeEnum.isExist(thumb.getType())) {
            throw new CustomizeException(CustomizeErrorCode.TYPE_PARAM_WRONG);
        }
        if (thumb.getType() == LikeTypeEnum.COMMENT.getType()) {
            // 点赞评论
            Comment dbComment = commentMapper.selectByPrimaryKey(thumb.getTargetId());
            if (dbComment == null) {
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

            //获取点赞的评论所属的问题
            Question question = questionMapper.selectByPrimaryKey(dbComment.getParentId());
            if (question == null) {
                throw new CustomizeException(CustomizeErrorCode.QUESTION_NOT_FOUND);
            }

            // 增加点赞数
            dbComment.setId(thumb.getTargetId());
            dbComment.setLikeCount(1);
            thumbExtMapper.incLikeCount(dbComment);

            // 创建通知
            createNotify(thumb, dbComment.getCommentator(), user.getName(), dbComment.getContent(), NotificationTypeEnum.LIKE_COMMENT,
                    question.getId());
        } else if (thumb.getType() == LikeTypeEnum.QUESTION.getType()) {
            // 收藏问题
            Question dbQuestion = questionMapper.selectByPrimaryKey(thumb.getTargetId());
            if (dbQuestion == null) {
                throw new CustomizeException(CustomizeErrorCode.QUESTION_NOT_FOUND);
            }
            ThumbExample thumbExample = new ThumbExample();
            thumbExample.createCriteria().andTargetIdEqualTo(thumb.getTargetId())
                    .andTypeEqualTo(thumb.getType()).andLikerEqualTo(thumb.getLiker());
            List<Thumb> thumbs = thumbMapper.selectByExample(thumbExample);
            if (thumbs.size() >= 1)
                return 2023;
            thumbMapper.insert(thumb);

            //增加问题收藏数
            dbQuestion.setId(thumb.getTargetId());
            dbQuestion.setLikeCount(1);
            thumbExtMapper.incQuestionLikeCount(dbQuestion);

            //创建通知
            createNotify(thumb, dbQuestion.getCreator(), user.getName(), dbQuestion.getTitle(), NotificationTypeEnum.LIKE_QUESTION,
                    dbQuestion.getId());
        }
        return 0;
    }

    private void createNotify(Thumb thumb, Integer receiver, String notifierName, String outerTitle, NotificationTypeEnum notificationType, Integer outerId) {
        if (receiver == thumb.getLiker())
            return;
        Notification notification = new Notification();
        notification.setGmtCreate(System.currentTimeMillis());
        notification.setType(notificationType.getType());
        notification.setOuterId(outerId);
        notification.setNotifier(thumb.getLiker());
        notification.setStatus(NotificationStatusEnum.UNREAD.getStatus());
        notification.setReceiver(receiver);
        notification.setNotifierName(notifierName);
        notification.setOuterTitle(outerTitle);
        notificationMapper.insert(notification);
    }
}
