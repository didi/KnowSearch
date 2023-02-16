# admin表修改
alter table es_cluster_phy_info
    add  kibana_address varchar(200) default '' null comment 'kibana外链地址';
alter table es_cluster_phy_info
    add  cerebro_address varchar(200) default '' null comment 'cerebro外链地址';
alter table es_cluster_phy_info
    add  cerebro_address varchar(200) default '' null comment '代理地址';
ALTER TABLE es_cluster_phy_info
    ADD proxy_address VARCHAR(255) DEFAULT '' NULL COMMENT ' 代理地址 ';
# 可观测性组件:
# 1. 所有logi_security_*表名都改成了kf_security_*
# 2. logi_task表新增字段"node_name_white_list_str", logi_worker表新增字段"node_name"

rename table logi_security_config to kf_security_config;
rename table logi_security_dept to kf_security_dept;
rename table logi_security_message to kf_security_message;
rename table logi_security_oplog to kf_security_oplog;
rename table logi_security_oplog_extra to kf_security_oplog_extra;
rename table logi_security_permission to kf_security_permission;
rename table logi_security_project to kf_security_project;
rename table logi_security_resource_type to kf_security_resource_type;
rename table logi_security_role to kf_security_role;
rename table logi_security_role_permission to kf_security_role_permission;
rename table logi_security_user to kf_security_user;
rename table logi_security_user_project to kf_security_user_project;
rename table logi_security_user_resource to kf_security_user_resource;
rename table logi_security_user_role to kf_security_user_role;

alter table logi_task
    add node_name_white_list_str varchar(3000) default '' not null comment '执行节点名对应白名单集';

alter table logi_worker
    add node_name varchar(100) default '' not null comment 'node 名';

INSERT INTO `kf_security_permission` (`permission_name`, `parent_id`, `leaf`, `level`, `description`, `create_time`, `update_time`, `is_delete`, `app_name`) VALUES ('Grafana', 0, 0, 1, 'Grafana', '2022-05-24 18:08:26', '2022-12-22 15:16:17', 0, 'know_search');
INSERT INTO `kf_security_role_permission` (`role_id`, `permission_id`, `create_time`, `update_time`, `is_delete`, `app_name`) VALUES (1, (select id from kf_security_permission ksp where ksp.permission_name='Grafana' and ksp.app_name='know_search' and ksp.is_delete=0 ), '2022-06-01 21:19:42', '2022-08-25 10:31:42', 0, 'know_search');
INSERT INTO `kf_security_permission` (`permission_name`, `parent_id`, `leaf`, `level`, `description`, `create_time`, `update_time`, `is_delete`, `app_name`) VALUES ('查看Grafana', (select id from kf_security_permission ksp where ksp.permission_name='Grafana' and ksp.app_name='know_search' and ksp.is_delete=0 ), 1, 2, '查看Grafana', '2022-05-24 18:08:26', '2022-12-22 15:16:17', 0, 'know_search');
INSERT INTO `kf_security_role_permission` (`role_id`, `permission_id`, `create_time`, `update_time`, `is_delete`, `app_name`) VALUES (1, (select id from kf_security_permission ksp where ksp.permission_name='查看Grafana' and ksp.app_name='know_search' and ksp.is_delete=0 ), '2022-06-01 21:19:42', '2022-08-25 10:31:42', 0, 'know_search');

-- 设置supperapp为索引模式
update `arius_es_user` set cluster='' , search_type=1  where id=1;
