package com.gs.community.dto;

import lombok.Data;

@Data
public class QuestionQueryDTO {
    private Integer page;
    private Integer size;
    private String search;
    private String tag;
    private String sort;
    private Long time;
    private Integer column2;
}
