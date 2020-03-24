CREATE TABLE user_info
(
    id int PRIMARY KEY NOT NULL AUTO_INCREMENT,
    user_id int NOT NULL,
    realname varchar(50),
    userdetail varchar(1024),
    birthday varchar(20),
    marriage varchar(5),
    sex varchar(5) DEFAULT '男',
    blood varchar(5),
    figure varchar(5),
    constellation varchar(20),
    education varchar(20),
    trade varchar(50),
    job varchar(50),
    location varchar(30) DEFAULT '北京-北京-东城区'
);
ALTER TABLE user_info COMMENT = '用户信息表';