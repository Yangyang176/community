package com.gs.community.exception;

public enum CustomizeErrorCode implements ICustomizeErrorCode {
    QUESTION_NOT_FOUND(2001, "你的问题不在了，要不换个试试？"),
    TARGET_PARAM_NOT_FOUND(2002, "未选中任何问题或评论"),
    NO_LOGIN(2003, "未登录不能进行当前操作，请先登录！"),
    SYS_ERROR(2004, "服务器冒烟了，要不待会再试试？"),
    TYPE_PARAM_WRONG(2005, "类型错误或不存在！"),
    COMMENT_NOT_FOUND(2006, "你要回复的评论不在了，要不换个试试？"),
    CONTENT_IS_EMPTY(2007, "回复内容不能为空！"),
    READ_NOTIFICATION_FAIL(2008, "兄弟，你这是读别人的通知呢？"),
    NOTIFICATION_NOT_FOUND(2009, "通知可能被藏起来了！"),
    FILE_UPLOAD_FAIL(2010, "文件上传失败！"),
    CAN_NOT_EDIT_QUESTION(2020, "您无法编辑此问题"),
    CAN_NOT_LIKE(2021, "点赞/收藏异常"),
    CAN_NOT_LIKE_AGAIN(2022, "不要重复点赞哦"),
    CAN_NOT_LIKE_QUESTION_AGAIN(2023, "不要重复收藏哦"),
    SEND_MAIL_FAILED(2030, "验证码发送失败请重试!请确保您的邮箱正确！"),
    SUBMIT_MAIL_FAILED(2031, "邮箱绑定/更新失败,可能该号码已在其他账号上绑定，请重试！"),
    USER_IS_EMPTY(2040, "该用户不存在或已被屏蔽！"),;
    private Integer code;
    private String message;

    CustomizeErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
