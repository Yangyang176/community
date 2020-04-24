package com.gs.community.service;

import com.gs.community.dto.CommentDTO;
import com.gs.community.dto.CommentQueryDTO;
import com.gs.community.dto.PaginationDTO;
import com.gs.community.dto.UserDTO;
import com.gs.community.enums.CommentTypeEnum;
import com.gs.community.enums.NotificationStatusEnum;
import com.gs.community.enums.NotificationTypeEnum;
import com.gs.community.exception.CustomizeErrorCode;
import com.gs.community.exception.CustomizeException;
import com.gs.community.mapper.*;
import com.gs.community.model.*;
import com.gs.community.util.TimeUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CommentService {
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private QuestionExtMapper questionExtMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CommentExtMapper commentExtMapper;
    @Autowired
    private NotificationMapper notificationMapper;
    @Autowired
    private UserAccountMapper userAccountMapper;
    @Autowired
    private UserAccountExtMapper userAccountExtMapper;
    @Autowired
    private TimeUtils timeUtils;
    @Value("${score1.comment.inc}")
    private Integer score1CommentInc;
    @Value("${score2.comment.inc}")
    private Integer score2CommentInc;
    @Value("${score3.comment.inc}")
    private Integer score3CommentInc;
    @Value("${score1.commented.inc}")
    private Integer score1CommentedInc;
    @Value("${score2.commented.inc}")
    private Integer score2CommentedInc;
    @Value("${score3.commented.inc}")
    private Integer score3CommentedInc;
    @Value("${user.score1.priorities}")
    private Integer score1Priorities;
    @Value("${user.score2.priorities}")
    private Integer score2Priorities;
    @Value("${user.score3.priorities}")
    private Integer score3Priorities;

    @Value("${user.group.r1.max}")
    private Integer r1Max;
    @Value("${user.group.r2.max}")
    private Integer r2Max;
    @Value("${user.group.r3.max}")
    private Integer r3Max;
    @Value("${user.group.r4.max}")
    private Integer r4Max;
    @Value("${user.group.r5.max}")
    private Integer r5Max;

    private Integer[] rMaxs = null;

    //懒汉式单例获取rMaxs
    private Integer[] getRMaxs() {
        if (rMaxs == null) {
            rMaxs = new Integer[]{r1Max, r2Max, r3Max, r4Max, r5Max};
        }
        return rMaxs;
    }

    @Transactional
    public void insert(Comment comment, User commentator, UserAccount userAccount) {
        if (comment.getParentId() == null || comment.getParentId() == 0) {
            throw new CustomizeException(CustomizeErrorCode.TARGET_PARAM_NOT_FOUND);
        }
        if (comment.getType() == null || !CommentTypeEnum.isExist(comment.getType())) {
            throw new CustomizeException(CustomizeErrorCode.TYPE_PARAM_WRONG);
        }
        if (comment.getType() == CommentTypeEnum.COMMENT.getType()) {
            //回复评论
            Comment parentComment = commentMapper.selectByPrimaryKey(comment.getParentId());
            if (parentComment == null) {
                throw new CustomizeException(CustomizeErrorCode.COMMENT_NOT_FOUND);
            }
            //查询所属问题
            Question parentQuestion = questionMapper.selectByPrimaryKey(parentComment.getParentId());
            if (parentQuestion == null) {
                throw new CustomizeException(CustomizeErrorCode.QUESTION_NOT_FOUND);
            }
            //给问题设置最新回复时间
            parentQuestion.setGmtLatestComment(System.currentTimeMillis());
            QuestionExample example = new QuestionExample();
            example.createCriteria().andIdEqualTo(parentQuestion.getId());
            int update = questionMapper.updateByExampleSelective(parentQuestion, example);
            if (update != 1) {
                throw new CustomizeException(CustomizeErrorCode.QUESTION_NOT_FOUND);
            }

            commentMapper.insert(comment);
            //增加评论数
            parentComment.setCommentCount(1);
            commentExtMapper.incComment(parentComment);
            //创建通知
            createNotify(comment, parentComment.getCommentator(), commentator.getName(), parentComment.getContent(), NotificationTypeEnum.REPLY_COMMENT, parentQuestion.getId());
        } else {
            //回复问题
            Question parentQuestion = questionMapper.selectByPrimaryKey(comment.getParentId());
            if (parentQuestion == null) {
                throw new CustomizeException(CustomizeErrorCode.QUESTION_NOT_FOUND);
            }
            //给问题设置最新回复时间
            parentQuestion.setGmtLatestComment(System.currentTimeMillis());
            QuestionExample example = new QuestionExample();
            example.createCriteria().andIdEqualTo(parentQuestion.getId());
            int update = questionMapper.updateByExampleSelective(parentQuestion, example);
            if (update != 1) {
                throw new CustomizeException(CustomizeErrorCode.QUESTION_NOT_FOUND);
            }
            commentMapper.insert(comment);
            parentQuestion.setCommentCount(1);
            questionExtMapper.incComment(parentQuestion);
            //创建通知
            createNotify(comment, parentQuestion.getCreator(), commentator.getName(), parentQuestion.getTitle(), NotificationTypeEnum.REPLY_QUESTION, parentQuestion.getId());
        }
        if (userAccount.getVipRank() != 0) {//VIP积分策略，可自行修改，这里简单处理
            score1CommentInc = score1CommentInc * 2;
            score2CommentInc = score2CommentInc * 2;
        }
        userAccount = new UserAccount();
        userAccount.setUserId(comment.getCommentator());
        userAccount.setScore1(score1CommentInc);
        userAccount.setScore2(score2CommentInc);
        userAccount.setScore3(score3CommentInc);
        userAccount.setScore(score1CommentInc * score1Priorities + score2CommentInc * score2Priorities + score3CommentInc * score3Priorities);
        userAccountExtMapper.incScore(userAccount);
        updateUserAccoundByUserId(comment.getCommentator());
        userAccount = null;
    }

    private void updateUserAccoundByUserId(Integer userId) {
        UserAccountExample userAccountExample = new UserAccountExample();
        userAccountExample.createCriteria().andUserIdEqualTo(userId);
        List<UserAccount> userAccounts = userAccountMapper.selectByExample(userAccountExample);
        UserAccount userAccount = userAccounts.get(0);
        Integer groupId = userAccount.getGroupId();
        Integer score = userAccount.getScore();
        Integer[] rMaxs = getRMaxs();
        Integer nowGroupId = 1;
        for (Integer rMax : rMaxs) {
            if (score > rMax) nowGroupId++;
            else break;
        }
        if (groupId > 0 && groupId < 10 && nowGroupId != groupId) {
            userAccount.setGroupId(nowGroupId);
            userAccountMapper.updateByExample(userAccount, userAccountExample);
        }
    }

    private void createNotify(Comment comment, Integer receiver, String notifierName, String outerTitle, NotificationTypeEnum notificationTypeEnum, Integer outerId) {
        if (receiver == comment.getCommentator()) {
            return;
        }
        Notification notification = new Notification();
        notification.setGmtCreate(System.currentTimeMillis());
        notification.setType(notificationTypeEnum.getType());
        notification.setNotifier(comment.getCommentator());
        notification.setReceiver(receiver);
        notification.setNotifierName(notifierName);
        notification.setOuterId(outerId);
        notification.setOuterTitle(outerTitle);
        notification.setStatus(NotificationStatusEnum.UNREAD.getStatus());
        notificationMapper.insert(notification);

        UserAccountExample userAccountExample = new UserAccountExample();
        userAccountExample.createCriteria().andUserIdEqualTo(receiver);
        List<UserAccount> userAccounts = userAccountMapper.selectByExample(userAccountExample);
        UserAccount userAccount = userAccounts.get(0);
        if (userAccount.getVipRank() != 0) {//VIP积分策略，可自行修改，这里简单处理
            score1CommentedInc = score1CommentedInc * 2;
            score2CommentedInc = score2CommentedInc * 2;
        }
        userAccount.setUserId(receiver);
        userAccount.setScore1(score1CommentedInc);
        userAccount.setScore2(score2CommentedInc);
        userAccount.setScore3(score3CommentedInc);
        userAccount.setScore(score1CommentedInc * score1Priorities + score2CommentedInc * score2Priorities + score3CommentedInc * score3Priorities);
        userAccountExtMapper.incScore(userAccount);
        updateUserAccoundByUserId(receiver);
        userAccount = null;
    }

    public List<CommentDTO> listByTargetId(Integer id, CommentTypeEnum typeEnum) {
        CommentExample commentExample = new CommentExample();
        commentExample.createCriteria().andParentIdEqualTo(id).andTypeEqualTo(typeEnum.getType());
        commentExample.setOrderByClause("gmt_create desc");
        List<Comment> comments = commentMapper.selectByExample(commentExample);

        if (comments.size() == 0) {
            return new ArrayList<>();
        }
        //获取去重评论人
        Set<Integer> commentators = comments.stream().map(comment -> comment.getCommentator()).collect(Collectors.toSet());
        List<Integer> userIds = new ArrayList<>();
        userIds.addAll(commentators);

        //获取评论人并转换为Map
        UserExample userExample = new UserExample();
        userExample.createCriteria().andIdIn(userIds);
        List<User> users = userMapper.selectByExample(userExample);
        Map<Integer, User> userMap = users.stream().collect(Collectors.toMap(user -> user.getId(), user -> user));
        //将comment转换为commentDTO
        List<CommentDTO> commentDTOS = comments.stream().map(comment -> {
            CommentDTO commentDTO = new CommentDTO();
            BeanUtils.copyProperties(comment, commentDTO);
            UserDTO userDTO = new UserDTO();
            BeanUtils.copyProperties(userMap.get(comment.getCommentator()), userDTO);
            commentDTO.setUser(userDTO);
            UserAccountExample userAccountExample = new UserAccountExample();
            userAccountExample.createCriteria().andUserIdEqualTo(userMap.get(comment.getCommentator()).getId());
            List<UserAccount> userAccounts = userAccountMapper.selectByExample(userAccountExample);
            commentDTO.setUserAccount(userAccounts.get(0));
            return commentDTO;
        }).collect(Collectors.toList());
        return commentDTOS;
    }

    public int delCommentByIdAndType(Integer userId, Integer groupId, Integer id, Integer type) {
        int c = 0;
        Comment comment = commentMapper.selectByPrimaryKey(id);
        if (groupId >= 18) {
            c = commentMapper.deleteByPrimaryKey(id);
        } else {
            CommentExample commentExample = new CommentExample();
            commentExample.createCriteria().andIdEqualTo(id).andCommentatorEqualTo(userId);
            c = commentMapper.deleteByExample(commentExample);
        }
        if (c > 0 && type == 1) {
            CommentExample commentExample = new CommentExample();
            commentExample.createCriteria().andTypeEqualTo(2).andParentIdEqualTo(id);
            c += commentMapper.deleteByExample(commentExample);
        }
        UserAccount userAccount = new UserAccount();
        userAccount.setUserId(comment.getCommentator());
        userAccount.setScore1(score1CommentInc);
        userAccount.setScore2(score2CommentInc);
        userAccount.setScore3(score3CommentInc);
        userAccount.setScore(score1CommentInc * score1Priorities + score2CommentInc * score2Priorities + score3CommentInc * score3Priorities);
        userAccountExtMapper.decScore(userAccount);
        userAccount = null;
        return c;
    }

    public PaginationDTO listByCommentQueryDTO(CommentQueryDTO commentQueryDTO) {
        Integer totalPage;
        Integer totalCount = commentExtMapper.countBySearch(commentQueryDTO);

        CommentExample commentExample = new CommentExample();
        CommentExample.Criteria criteria = commentExample.createCriteria();
        if (commentQueryDTO.getCommentator() != null)
            criteria.andCommentatorEqualTo(commentQueryDTO.getCommentator());
        if (commentQueryDTO.getId() != null)
            criteria.andIdEqualTo(commentQueryDTO.getId());
        if (commentQueryDTO.getParentId() != null)
            criteria.andParentIdEqualTo(commentQueryDTO.getParentId());
        if (commentQueryDTO.getType() != null)
            criteria.andTypeEqualTo(commentQueryDTO.getType());

        if (commentQueryDTO.getPage() == null || commentQueryDTO.getPage() <= 0) commentQueryDTO.setPage(1);
        if (commentQueryDTO.getSize() == null || commentQueryDTO.getSize() <= 0) commentQueryDTO.setSize(5);
        if (totalCount % commentQueryDTO.getSize() == 0) {
            totalPage = totalCount / commentQueryDTO.getSize();
        } else {
            totalPage = totalCount / commentQueryDTO.getSize() + 1;
        }

        if (commentQueryDTO.getPage() > totalPage) {
            commentQueryDTO.setPage(totalPage);
        }

        Integer offset = commentQueryDTO.getPage() < 1 ? 0 : commentQueryDTO.getSize() * (commentQueryDTO.getPage() - 1);
        commentQueryDTO.setOffset(offset);

        commentExample.setOrderByClause("gmt_modified desc");
        List<Comment> comments = commentMapper.selectByExampleWithRowbounds(commentExample, new RowBounds(commentQueryDTO.getSize() * (commentQueryDTO.getPage() - 1), commentQueryDTO.getSize()));
        PaginationDTO paginationDTO = new PaginationDTO();
        paginationDTO.setTotalCount(totalCount);
        if (comments.size() == 0) {
            return paginationDTO;
        }
        // 获取去重的评论人
        Set<Integer> commentators = comments.stream().map(comment -> comment.getCommentator()).collect(Collectors.toSet());
        List<Integer> userIds = new ArrayList();
        userIds.addAll(commentators);

        // 获取评论人并转换为 Map
        UserExample userExample = new UserExample();
        userExample.createCriteria()
                .andIdIn(userIds);
        List<User> users = userMapper.selectByExample(userExample);

        Map<Integer, User> userMap = users.stream().collect(Collectors.toMap(user -> user.getId(), user -> user));
        // 转换 comment 为 commentDTO
        List<CommentDTO> commentDTOS = comments.stream().map(comment -> {
            CommentDTO commentDTO = new CommentDTO();
            BeanUtils.copyProperties(comment, commentDTO);
            UserDTO userDTO = new UserDTO();
            BeanUtils.copyProperties(userMap.get(comment.getCommentator()),userDTO);
            commentDTO.setUser(userDTO);
            UserAccountExample userAccountExample = new UserAccountExample();
            userAccountExample.createCriteria().andUserIdEqualTo(userMap.get(comment.getCommentator()).getId());
            List<UserAccount> userAccounts = userAccountMapper.selectByExample(userAccountExample);
            commentDTO.setUserAccount(userAccounts.get(0));
            commentDTO.setGmtModifiedStr(timeUtils.getTime(commentDTO.getGmtModified(),null));
            return commentDTO;
        }).collect(Collectors.toList());

        paginationDTO.setData(commentDTOS);
        paginationDTO.setPagination(totalPage,commentQueryDTO.getPage());
        return paginationDTO;
    }
}
