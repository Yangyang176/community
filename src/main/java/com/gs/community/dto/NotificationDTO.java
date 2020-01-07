package com.gs.community.dto;

import com.gs.community.model.User;
import lombok.Data;

@Data
public class NotificationDTO {
    private Integer id;
    private Long gmtCreate;
    private Integer status;
    private Integer notifier;
    private String notifierName;
    private Integer outerId;
    private String outerTitle;
    private Integer type;
    private String typeName;
}
