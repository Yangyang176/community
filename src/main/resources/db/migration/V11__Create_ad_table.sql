CREATE TABLE ad
(
    id int PRIMARY KEY NOT NULL AUTO_INCREMENT,
    title varchar(256) DEFAULT null  COMMENT '广告标题',
    url varchar(512) DEFAULT null  COMMENT '广告链接',
    image varchar(256) DEFAULT null COMMENT '广告图片',
    gmt_start bigint COMMENT '生效时间',
    gmt_end bigint COMMENT '失效时间',
    gmt_create bigint,
    gmt_modified bigint,
    status int COMMENT '广告状态',
    pos varchar(10) NOT NULL COMMENT '广告位置'
);
ALTER TABLE ad COMMENT = '广告';