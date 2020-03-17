package com.gs.community.service;

import com.gs.community.dto.PaginationDTO;
import com.gs.community.dto.QuestionDTO;
import com.gs.community.dto.QuestionQueryDTO;
import com.gs.community.enums.SortEnum;
import com.gs.community.exception.CustomizeErrorCode;
import com.gs.community.exception.CustomizeException;
import com.gs.community.mapper.QuestionExtMapper;
import com.gs.community.mapper.QuestionMapper;
import com.gs.community.mapper.ThumbMapper;
import com.gs.community.mapper.UserMapper;
import com.gs.community.model.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    public PaginationDTO list(Integer page, Integer size, String search, String tag, String sort) {
        if (StringUtils.isNotBlank(search)) {
            search = search.replace(",", "|").replace("+", "")
                    .replace("*", "").replace("&", "").replace("?", "");
        }
        PaginationDTO paginationDTO = new PaginationDTO();
        Integer totalPage;

        QuestionQueryDTO questionQueryDTO = new QuestionQueryDTO();
        questionQueryDTO.setSearch(search);

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
            QuestionDTO queryDTO = new QuestionDTO();
            BeanUtils.copyProperties(question, queryDTO);
            queryDTO.setUser(user);
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
            queryDTO.setUser(user);
            queryDTOList.add(queryDTO);
        }
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
        queryDTO.setUser(user);
        return queryDTO;
    }

    public void createOrUpdate(Question question) {
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
        } else {
            //修改
            Question updateQuestion = questionMapper.selectByPrimaryKey(question.getId());
//            Question updateQuestion = new Question();
            updateQuestion.setGmtModified(System.currentTimeMillis());
            updateQuestion.setTitle(question.getTitle());
            updateQuestion.setDescription(question.getDescription());
            updateQuestion.setTag(question.getTag());
            QuestionExample example = new QuestionExample();
            example.createCriteria().andIdEqualTo(question.getId());
            int updated = questionMapper.updateByExampleSelective(updateQuestion, example);
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

    public List<QuestionDTO> listTop(String search, String tag, String sort) {
        if (StringUtils.isNotBlank(search)) {
            search = search.replace(",", "|").replace("+", "")
                    .replace("*", "").replace("&", "").replace("?", "");
        }

        QuestionQueryDTO questionQueryDTO = new QuestionQueryDTO();
        questionQueryDTO.setSearch(search);

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
            QuestionDTO queryDTO = new QuestionDTO();
            BeanUtils.copyProperties(question, queryDTO);
            queryDTO.setUser(user);
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
            questionDTO.setUser(user);
            questionDTOList.add(questionDTO);
        }

        paginationDTO.setData(questionDTOList);
        paginationDTO.setPagination(totalPage, page);
        paginationDTO.setTotalCount(totalCount);
        return paginationDTO;
    }
}
