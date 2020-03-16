ALTER TABLE question ADD gmt_latest_comment bigint NOT NULL COMMENT '最新评论时间';
ALTER TABLE question ADD status bigint DEFAULT 0 NOT NULL COMMENT '1精帖，2置顶，3精帖+置顶';