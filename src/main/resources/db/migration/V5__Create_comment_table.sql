CREATE TABLE comment
(
    id int PRIMARY KEY COMMENT 'part of primary key' AUTO_INCREMENT,
    parent_id int NOT NULL COMMENT '父类ID',
    type int NOT NULL COMMENT '父类类型',
    commentator int NOT NULL COMMENT '评论人ID',
    gmt_create bigint NOT NULL COMMENT '创建时间',
    gmt_modified bigint NOT NULL COMMENT '更新时间',
    like_count bigint DEFAULT 0 NOT NULL COMMENT '点赞数',
    content varchar(1024) NOT NULL COMMENT '评论内容'
);