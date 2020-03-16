ALTER TABLE question ADD gmt_latest_comment bigint NULL;
ALTER TABLE question ADD status int DEFAULT 0 NOT NULL COMMENT '1.精帖 2.置顶 3.精帖+置顶';