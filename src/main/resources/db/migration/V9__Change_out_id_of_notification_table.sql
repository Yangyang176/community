ALTER TABLE notification MODIFY notifier int(11) NOT NULL COMMENT '通知人id';
ALTER TABLE notification MODIFY receiver int(11) NOT NULL COMMENT '被通知人id';
ALTER TABLE notification CHANGE out_id outer_id int(11) NOT NULL COMMENT '所属问题id';
ALTER TABLE notification MODIFY type int(11) NOT NULL COMMENT '类型(问题,评论)';
ALTER TABLE notification MODIFY status int(11) NOT NULL DEFAULT '0' COMMENT '是否已读';
ALTER TABLE notification MODIFY notifier_name varchar(256) NOT NULL COMMENT '通知人姓名';
ALTER TABLE notification MODIFY outer_title varchar(256) NOT NULL COMMENT '问题标题';