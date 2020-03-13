CREATE TABLE thumb
(
    id int PRIMARY KEY NOT NULL AUTO_INCREMENT,
    target_id int NOT NULL COMMENT '点赞目标(问题/评论)',
    type int NOT NULL COMMENT '目标类型',
    liker int NOT NULL COMMENT '点赞者',
    gmt_create bigint NOT NULL,
    gmt_modified bigint NOT NULL
);
ALTER TABLE thumb COMMENT = '点赞表';