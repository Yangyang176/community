CREATE TABLE user_account
(
    id int PRIMARY KEY NOT NULL AUTO_INCREMENT,
    user_id int NOT NULL,
    group_id int COMMENT '用户组',
    vip_rank int COMMENT '用户等级',
    score int COMMENT '总积分',
    score1 int COMMENT '经验',
    score2 int COMMENT '贡献',
    score3 int COMMENT '金币'
);
ALTER TABLE user_account COMMENT = '用户积分表';