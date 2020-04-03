package com.gs.community.service;

import com.gs.community.dto.PaginationDTO;
import com.gs.community.dto.QuestionDTO;
import com.gs.community.dto.QuestionQueryDTO;
import com.gs.community.dto.UserDTO;
import com.gs.community.enums.SortEnum;
import com.gs.community.exception.CustomizeErrorCode;
import com.gs.community.exception.CustomizeException;
import com.gs.community.mapper.*;
import com.gs.community.model.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionService {
    @Autowired(required = false)
    private QuestionMapper questionMapper;
    @Autowired(required = false)
    private UserMapper userMapper;
    @Autowired
    private QuestionExtMapper questionExtMapper;
    @Autowired
    private ThumbMapper thumbMapper;
    @Autowired
    private UserAccountMapper userAccountMapper;
    @Autowired
    private UserAccountExtMapper userAccountExtMapper;
    @Autowired
    private LikeService likeService;
    @Autowired
    private UserAccountService userAccountService;
    @Value("${score1.publish.inc}")
    private Integer score1PublishInc;
    @Value("${score2.publish.inc}")
    private Integer score2PublishInc;
    @Value("${score3.publish.inc}")
    private Integer score3PublishInc;
    @Value("${user.score1.priorities}")
    private Integer score1Priorities;
    @Value("${user.score2.priorities}")
    private Integer score2Priorities;
    @Value("${user.score3.priorities}")
    private Integer score3Priorities;

    @Autowired
    private Environment env;

    public PaginationDTO listwithColumn(Integer page, Integer size, String search, String tag, String sort, Integer column2, UserAccount userAccount) {
        if (StringUtils.isNotBlank(search)) {
            search = search.replace(",", "|").replace("+", "")
                    .replace("*", "").replace("&", "").replace("?", "");
        }
        PaginationDTO paginationDTO = new PaginationDTO();
        Integer totalPage;

        QuestionQueryDTO questionQueryDTO = new QuestionQueryDTO();
        questionQueryDTO.setSearch(search);
        if (column2 != null) questionQueryDTO.setColumn2(column2);

        if (StringUtils.isNotBlank(tag)) {
            tag = tag.replace("+", "").replace("*", "").replace("&", "")
                    .replace("?", "");
            questionQueryDTO.setTag(tag);
        }

        for (SortEnum sortEnum : SortEnum.values()) {
            if (sortEnum.name().toLowerCase().equals(sort)) {
                questionQueryDTO.setSort(sort);
                if (sortEnum == SortEnum.HOT7) {
                    questionQueryDTO.setTime(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 7);
                }
                if (sortEnum == SortEnum.HOT30) {
                    questionQueryDTO.setTime(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 30);
                }
                break;
            }
        }

        Integer totalCount = questionExtMapper.countBySearch(questionQueryDTO);
        if (totalCount % size == 0) {
            totalPage = totalCount / size;
        } else {
            totalPage = totalCount / size + 1;
        }
        paginationDTO.setPagination(totalPage, page);
        if (page < 1) {
            page = 1;
        }
        if (page > totalPage) {
            page = totalPage;
        }
        Integer offset = size * (page - 1);
        if (offset < 0) {
            offset = 0;
        }
        questionQueryDTO.setSize(size);
        questionQueryDTO.setPage(offset);
        List<Question> questions = questionExtMapper.selectBySearch(questionQueryDTO);
        List<QuestionDTO> queryDTOList = new ArrayList<>();
        for (Question question : questions) {
            User user = userMapper.selectByPrimaryKey(question.getCreator());
            UserAccountExample userAccountExample = new UserAccountExample();
            userAccountExample.createCriteria().andUserIdEqualTo(user.getId());
            List<UserAccount> userAccounts = userAccountMapper.selectByExample(userAccountExample);
            QuestionDTO queryDTO = new QuestionDTO();
            BeanUtils.copyProperties(question, queryDTO);
            UserDTO userDTO = new UserDTO();
            BeanUtils.copyProperties(user, userDTO);
            queryDTO.setUser(userDTO);
            queryDTO.setUserAccount(userAccounts.get(0));
            queryDTO.setUserGroupName(env.getProperty("user.group.r" + userAccounts.get(0).getGroupId()));
//            queryDTO.setGmtLatestCommentStr();
            if (queryDTO.getPermission() == 0) queryDTO.setIsVisible(1);
            else if (userAccount != null && userAccount.getGroupId() >= queryDTO.getPermission())
                queryDTO.setIsVisible(1);
            else queryDTO.setIsVisible(0);
            queryDTOList.add(queryDTO);
        }
        paginationDTO.setData(queryDTOList);
        return paginationDTO;
    }

    public PaginationDTO listByUserId(Integer userId, Integer page, Integer size) {
        PaginationDTO paginationDTO = new PaginationDTO();
        Integer totalPage;
        QuestionExample example = new QuestionExample();
        example.createCriteria().andCreatorEqualTo(userId);
        Integer totalCount = (int) questionMapper.countByExample(example);
        if (totalCount % size == 0) {
            totalPage = totalCount / size;
        } else {
            totalPage = totalCount / size + 1;
        }
        paginationDTO.setPagination(totalPage, page);
        if (page < 1) {
            page = 1;
        }
        if (page > totalPage) {
            page = totalPage;
        }
        Integer offset = size * (page - 1);
        if (offset < 0) {
            offset = 0;
        }
        QuestionExample questionExample = new QuestionExample();
        questionExample.createCriteria().andCreatorEqualTo(userId);
        questionExample.setOrderByClause("gmt_create desc");
        List<Question> questions = questionMapper.selectByExampleWithRowbounds(questionExample, new RowBounds(offset, size));
        List<QuestionDTO> queryDTOList = new ArrayList<>();
        for (Question question : questions) {
            User user = userMapper.selectByPrimaryKey(question.getCreator());
            QuestionDTO queryDTO = new QuestionDTO();
            BeanUtils.copyProperties(question, queryDTO);
            UserDTO userDTO = new UserDTO();
            BeanUtils.copyProperties(user, userDTO);
            queryDTO.setUser(userDTO);
            queryDTOList.add(queryDTO);
        }
        paginationDTO.setTotalCount(totalCount);
        paginationDTO.setData(queryDTOList);
        return paginationDTO;
    }

    public QuestionDTO getById(Integer id) {
        Question question = questionMapper.selectByPrimaryKey(id);
        if (question == null) {
            throw new CustomizeException(CustomizeErrorCode.QUESTION_NOT_FOUND);
        }
        QuestionDTO queryDTO = new QuestionDTO();
        BeanUtils.copyProperties(question, queryDTO);
        User user = userMapper.selectByPrimaryKey(question.getCreator());
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        queryDTO.setUser(userDTO);
        return queryDTO;
    }

    public void createOrUpdate(Question question, UserAccount userAccount) {
        if (question.getId() == null) {
            //插入
            question.setGmtCreate(System.currentTimeMillis());
            question.setGmtModified(question.getGmtCreate());
            question.setGmtLatestComment(question.getGmtCreate());
            question.setViewCount(0);
            question.setLikeCount(0);
            question.setCommentCount(0);
            question.setStatus(0);
            questionMapper.insert(question);
            if (userAccount.getVipRank() != 0) {
                score1PublishInc = score1PublishInc * 2;
                score2PublishInc = score2PublishInc * 2;
            }
            userAccount = new UserAccount();
            userAccount.setUserId(question.getCreator());
            userAccount.setScore1(score1PublishInc);
            userAccount.setScore2(score2PublishInc);
            userAccount.setScore3(score3PublishInc);
            userAccount.setScore(score1PublishInc * score1Priorities + score2PublishInc * score2Priorities + score3PublishInc * score3Priorities);
            userAccountExtMapper.incScore(userAccount);
            userAccount = null;
        } else {
            //修改
            /*Question updateQuestion = questionMapper.selectByPrimaryKey(question.getId());
//            Question updateQuestion = new Question();
            updateQuestion.setGmtModified(System.currentTimeMillis());
            updateQuestion.setTitle(question.getTitle());
            updateQuestion.setDescription(question.getDescription());
            updateQuestion.setTag(question.getTag());*/
            question.setGmtModified(System.currentTimeMillis());
            QuestionExample example = new QuestionExample();
            example.createCriteria().andIdEqualTo(question.getId());
            int updated = questionMapper.updateByExampleSelective(question, example);
            if (updated != 1) {
                throw new CustomizeException(CustomizeErrorCode.QUESTION_NOT_FOUND);
            }
        }
    }

    public void incView(Integer id) {
        Question question = new Question();
        question.setId(id);
        question.setViewCount(1);
        questionExtMapper.incView(question);
    }

    public List<QuestionDTO> selectRelated(QuestionDTO queryDTO) {
        if (StringUtils.isBlank(queryDTO.getTag())) {
            return new ArrayList<>();
        }
        String regexpTag = queryDTO.getTag().replace(",", "|").replace("+", "")
                .replace("*", "").replace("&", "").replace("?", "");
        Question question = new Question();
        question.setId(queryDTO.getId());
        question.setTag(regexpTag);
        List<Question> questions = questionExtMapper.selectRelated(question);
        List<QuestionDTO> questionDTOS = questions.stream().map(q -> {
            QuestionDTO questionDTO = new QuestionDTO();
            BeanUtils.copyProperties(q, questionDTO);
            return questionDTO;
        }).collect(Collectors.toList());
        return questionDTOS;
    }

    public List<QuestionDTO> listTopwithColumn(String search, String tag, String sort, Integer column2) {
        if (StringUtils.isNotBlank(search)) {
            search = search.replace(",", "|").replace("+", "")
                    .replace("*", "").replace("&", "").replace("?", "");
        }

        QuestionQueryDTO questionQueryDTO = new QuestionQueryDTO();
        questionQueryDTO.setSearch(search);
        if (column2 != null) questionQueryDTO.setColumn2(column2);

        if (StringUtils.isNotBlank(tag)) {
            tag = tag.replace("+", "").replace("*", "").replace("&", "")
                    .replace("?", "");
            questionQueryDTO.setTag(tag);
        }

        for (SortEnum sortEnum : SortEnum.values()) {
            if (sortEnum.name().toLowerCase().equals(sort)) {
                questionQueryDTO.setSort(sort);
                if (sortEnum == SortEnum.HOT7) {
                    questionQueryDTO.setTime(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 7);
                }
                if (sortEnum == SortEnum.HOT30) {
                    questionQueryDTO.setTime(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 30);
                }
                break;
            }
        }

        List<Question> questions = questionExtMapper.selectTop(questionQueryDTO);
        List<QuestionDTO> questionDTOList = new ArrayList<>();
        for (Question question : questions) {
            User user = userMapper.selectByPrimaryKey(question.getCreator());
            UserAccountExample userAccountExample = new UserAccountExample();
            userAccountExample.createCriteria().andUserIdEqualTo(user.getId());
            List<UserAccount> userAccounts = userAccountMapper.selectByExample(userAccountExample);
            QuestionDTO queryDTO = new QuestionDTO();
            BeanUtils.copyProperties(question, queryDTO);
            UserDTO userDTO = new UserDTO();
            BeanUtils.copyProperties(user, userDTO);
            queryDTO.setUser(userDTO);
            queryDTO.setUserAccount(userAccounts.get(0));
            questionDTOList.add(queryDTO);
        }
        return questionDTOList;
    }

    public PaginationDTO listByThumbExample(Integer userId, Integer page, Integer size, String likes) {
        Integer totalPage;
        ThumbExample thumbExample = new ThumbExample();
        thumbExample.createCriteria().andTypeEqualTo(1).andLikerEqualTo(userId);
        int totalCount = (int) thumbMapper.countByExample(thumbExample);
        if (totalCount % size == 0) {
            totalPage = totalCount / size;
        } else {
            totalPage = totalCount / size + 1;
        }

        if (page > totalPage) {
            page = totalPage;
        }

        if (page < 1) {
            page = 1;
        }

        Integer offset = size * (page - 1);

        thumbExample.setOrderByClause("gmt_modified desc");
        List<Thumb> thumbs = thumbMapper.selectByExampleWithRowbounds(thumbExample, new RowBounds(offset, size));
        List<Question> questionList = new ArrayList<>();
        PaginationDTO paginationDTO = new PaginationDTO();
        List<QuestionDTO> questionDTOList = new ArrayList<>();

        for (Thumb thumb : thumbs) {
            questionList.add(questionMapper.selectByPrimaryKey(thumb.getTargetId()));
        }
        for (Question question : questionList) {
            User user = userMapper.selectByPrimaryKey(question.getCreator());
            QuestionDTO questionDTO = new QuestionDTO();
            BeanUtils.copyProperties(question, questionDTO);
            UserDTO userDTO = new UserDTO();
            BeanUtils.copyProperties(user, userDTO);
            questionDTO.setUser(userDTO);
            questionDTOList.add(questionDTO);
        }

        paginationDTO.setData(questionDTOList);
        paginationDTO.setPagination(totalPage, page);
        paginationDTO.setTotalCount(totalCount);
        return paginationDTO;
    }

    public QuestionDTO getById(Integer id, Integer userId) {
        Question question = questionMapper.selectByPrimaryKey(id);
        if (question == null) {
            throw new CustomizeException(CustomizeErrorCode.QUESTION_NOT_FOUND);
        }
        QuestionDTO questionDTO = new QuestionDTO();
        BeanUtils.copyProperties(question, questionDTO);
        User user = userMapper.selectByPrimaryKey(question.getCreator());
        UserAccountExample userAccountExample = new UserAccountExample();
        userAccountExample.createCriteria().andUserIdEqualTo(user.getId());
        List<UserAccount> userAccounts = userAccountMapper.selectByExample(userAccountExample);
        UserAccount userAccount = userAccounts.get(0);
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        questionDTO.setUser(userDTO);
        questionDTO.setUserAccount(userAccount);
        questionDTO = setStatuses(questionDTO, userId);
        return questionDTO;
    }

    public QuestionDTO setStatuses(QuestionDTO questionDTO, Integer userId) {
        questionDTO.setEdited(questionDTO.getGmtCreate().longValue() != questionDTO.getGmtModified().longValue());
        if ((questionDTO.getStatus() & 1) == 1) questionDTO.setEssence(true);
        if ((questionDTO.getStatus() & 2) == 2) questionDTO.setSticky(true);
        if (userId != 0) {
            if (likeService.queryLike(questionDTO.getId(), 1, userId) > 0) questionDTO.setFavorite(true);
            if (userAccountService.isAdminByUserId(userId)) {
                questionDTO.setCanClassify(true);
                questionDTO.setCanDelete(true);
                questionDTO.setCanEssence(true);
                questionDTO.setCanSticky(true);
                if (userId == questionDTO.getCreator()) questionDTO.setCanEdit(true);
                questionDTO.setCanPromote(true);
            } else if (userId == questionDTO.getCreator()) {
                questionDTO.setCanEdit(true);
                questionDTO.setCanClassify(true);
                questionDTO.setCanDelete(true);
            }
        }
        return questionDTO;
    }

    public int delQuestionById(Integer userId, Integer groupId, Integer id) {
        int c = 0;
        Question question = questionMapper.selectByPrimaryKey(id);
        if (groupId >= 18) {
            c = questionMapper.deleteByPrimaryKey(id);
        } else {
            QuestionExample questionExample = new QuestionExample();
            questionExample.createCriteria().andIdEqualTo(id).andCreatorEqualTo(userId);
            c = questionMapper.deleteByExample(questionExample);
        }
        UserAccount userAccount = new UserAccount();
        userAccount.setUserId(question.getCreator());
        userAccount.setScore1(score1PublishInc);
        userAccount.setScore2(score2PublishInc);
        userAccount.setScore3(score3PublishInc);
        userAccount.setScore(score1PublishInc * score1Priorities + score2PublishInc * score2Priorities + score3PublishInc * score3Priorities);
        userAccountExtMapper.decScore(userAccount);
        userAccount = null;
        return c;
    }

    public Question getQuestionById(Integer id) {
        return questionMapper.selectByPrimaryKey(id);
    }

    public int updateQuestion(Question question) {
        return questionMapper.updateByPrimaryKeySelective(question);
    }

    public String getTextDescriptionFromHtml(String description) {
        String textDescription = description.replaceAll("</?[^>]+>", "");//剔除<html>的标签
        textDescription = textDescription.replaceAll("<a>\\s*|\t|\r|\n</a>", "");//去除字符串中的空格,回车,换行符,制表符
        textDescription = textDescription.replaceAll("&nbsp;", "");//去除&nbsp;
        return textDescription;
    }
}
