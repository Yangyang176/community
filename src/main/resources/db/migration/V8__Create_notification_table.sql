CREATE TABLE notification
(
    id int PRIMARY KEY AUTO_INCREMENT,
    notifier int NOT NULL,
    receiver int NOT NULL,
    out_id int NOT NULL,
    type int NOT NULL,
    gmt_create bigint NOT NULL,
    status int DEFAULT 0 NOT NULL,
    notifier_name varchar(256) NOT NULL,
    outer_title varchar(256) NOT NULL
);