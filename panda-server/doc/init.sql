create database panda;

use panda;

create table project (
  id INT(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '',
  name varchar(50) NOT NULL COMMENT '',
  dep varchar(50) NOT NULL COMMENT '',
  owner varchar(50) NOT NULL COMMENT '',
  managers varchar(255) NOT NULL DEFAULT '' COMMENT '',
  state TINYINT NOT NULL DEFAULT 0 COMMENT '',
  retry_count int NOT NULL DEFAULT 0 COMMENT '',
  description varchar(255) NOT NULL DEFAULT '' COMMENT '',
  zip_path varchar(255) NOT NULL DEFAULT '' COMMENT '',
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '',
  update_time TIMESTAMP ON UPDATE CURRENT_TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '',
  PRIMARY KEY (id),
  UNIQUE unq_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table user (
  id INT(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '',
  username varchar(50) NOT NULL COMMENT '',
  department VARCHAR(50) NOT NULL COMMENT '',
  role TINYINT NOT NULL DEFAULT 0 COMMENT '0: 普通用户,1: 管理员',
  PRIMARY KEY (id),
  UNIQUE unq_name (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table submit (
  id INT(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '',
  pid INT(11) NOT NULL COMMENT '',
  submit_user VARCHAR(50) NOT NULL COMMENT '',
  cluster_master VARCHAR(50) NOT NULL COMMENT '',
  attempt INT NOT NULL DEFAULT 0 COMMENT '',
  executor_num int NOT NULL DEFAULT 0 COMMENT '',
  executor_cores int NOT NULL DEFAULT 0 COMMENT '',
  executor_memory VARCHAR(10) NOT NULL DEFAULT '' COMMENT '',
  application_id varchar(50) NOT NULL DEFAULT '' COMMENT '',
  tracking_url varchar(255) NOT NULL DEFAULT '' COMMENT '',
  start_time DATETIME DEFAULT NULL COMMENT '',
  end_time DATETIME DEFAULT NULL COMMENT '',
  state TINYINT NOT NULL DEFAULT 0 COMMENT '',
  update_time TIMESTAMP ON UPDATE CURRENT_TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE INDEX project_id_idx USING BTREE on submit (pid);

