package com.gs.community.mapper;

import com.gs.community.dto.QuestionQueryDTO;
import com.gs.community.model.Question;

import java.util.List;

public interface QuestionExtMapper {
    int incView(Question record);
    int incComment(Question record);
    List<Question> selectTop(QuestionQueryDTO questionQueryDTO);
    List<Question> selectRelated(Question question);
    Integer countBySearch(QuestionQueryDTO questionQueryDTO);
    List<Question> selectBySearch(QuestionQueryDTO questionQueryDTO);
}