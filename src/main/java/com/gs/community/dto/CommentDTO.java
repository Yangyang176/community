package com.gs.community.dto;

import com.gs.community.model.UserAccount;
import lombok.Data;

@Data
public class CommentDTO {
    private Integer id;
    private Integer parentId;
    private Integer type;
    private Integer commentator;
    private Long gmtCreate;
    private Long gmtModified;
    private String gmtModifiedStr;
    private Integer likeCount;
    private String content;
    private UserDTO user;
    private Integer commentCount;
    private UserAccount userAccount;
}
