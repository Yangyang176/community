CREATE TABLE nav
(
    id int PRIMARY KEY NOT NULL AUTO_INCREMENT,
    title varchar(50) COMMENT '导航标题',
    url varchar(256),
    priority int DEFAULT 0 COMMENT '权重',
    gmt_create bigint,
    gmt_modified bigint,
    status int COMMENT '状态'
);
ALTER TABLE nav COMMENT = '主页动态导航';