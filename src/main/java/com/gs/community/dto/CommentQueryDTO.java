package com.gs.community.dto;

import lombok.Data;

@Data
public class CommentQueryDTO {
    private Integer id;
    private Integer parentId;
    private Integer type;
    private Integer commentator;
    private Integer page;
    private Integer size;
    private Integer offset;
}
