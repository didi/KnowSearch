drop database if exists zeus_es;
create database zeus_es;
use zeus_es;
set names utf8;

CREATE TABLE `user` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(32) not null comment 'login name',
  `password` varchar(128) not null default '',
  `role` int not null default 0 comment '0:normal, 10:admin',
  PRIMARY KEY (`id`),
  UNIQUE KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `grp` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `pid` bigint unsigned not null default 0,
  `name` varchar(255) not null default '',
  `users` varchar(2048) not null,
  PRIMARY KEY (`id`),
  KEY (`pid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tpl` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `gid` bigint unsigned not null,
  `updator` varchar(64) not null default '',
  `updated` int not null,
  `keywords` varchar(255) not null default '',
  `batch` int unsigned not null default 0,
  `tolerance` int unsigned not null default 0,
  `timeout` int unsigned not null default 0,
  `pause` varchar(255) not null default '',
  `script` text not null,
  `args` varchar(512) not null default '',
  `account` varchar(64) not null,
  PRIMARY KEY (`id`),
  KEY (`gid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `tpl_host` (
  `ii` bigint unsigned NOT NULL AUTO_INCREMENT,
  `id` bigint unsigned not null comment 'tpl id',
  `hostname` varchar(128) not null,
  PRIMARY KEY (`ii`),
  UNIQUE KEY (`id`, `hostname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `task_meta` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `creator` varchar(64) not null default '',
  `created` int not null,
  `keywords` varchar(255) not null default '',
  `batch` int unsigned not null default 0,
  `tolerance` int unsigned not null default 0,
  `timeout` int unsigned not null default 0,
  `pause` varchar(255) not null default '',
  `script` text not null,
  `args` varchar(512) not null default '',
  `account` varchar(64) not null,
  PRIMARY KEY (`id`),
  KEY (`creator`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `task_pause` (
  `id` bigint unsigned not null,
  `hostname` varchar(128) not null default '',
  `has` tinyint not null default 0,
  KEY (`id`, `hostname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `task_action` (
  `id` bigint unsigned not null,
  `action` varchar(32) not null,
  `ts` int not null default 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `task_scheduler` (
  `id` bigint unsigned not null,
  `scheduler` varchar(128) not null default '',
  KEY (`id`, `scheduler`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `scheduler_health` (
  `scheduler` varchar(128) not null,
  `ts` datetime not null,
  UNIQUE KEY (`scheduler`),
  KEY (`ts`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `host_doing` (
  `id` bigint unsigned not null,
  `hostname` varchar(128) not null,
  `ts` int not null default 0,
  `action` varchar(16) not null,
  UNIQUE KEY (`id`, `hostname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `user_token` (
  `id` int unsigned not null primary key auto_increment,
  `username` varchar(32) not null,
  `token` varchar(32) not null,
  KEY (`username`),
  KEY (`token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `vmop` (
  `id` bigint unsigned not null primary key auto_increment,
  `hostname` varchar(128) not null,
  `username` varchar(32) not null,
  KEY (`hostname`, `username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `task_scheduling` (
  `id` bigint unsigned not null primary key auto_increment,
  `task_id` bigint not null,
  UNIQUE KEY (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `edge_agent_info` (
 `ident` varchar(128) not null,
 `ip` varchar(64) not null default "",
 `ts` int not null default 0,
  UNIQUE KEY (`ident`),
  KEY(`ip`),
  KEY (`ts`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
