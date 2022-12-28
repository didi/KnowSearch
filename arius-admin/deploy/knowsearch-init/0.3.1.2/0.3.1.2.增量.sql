# admin表修改
alter table es_cluster_phy_info
    add  kibana_address varchar(200) default '' null comment 'kibana外链地址';
alter table es_cluster_phy_info
    add  cerebro_address varchar(200) default '' null comment 'cerebro外链地址';
alter table es_cluster_phy_info
    add  cerebro_address varchar(200) default '' null comment '代理地址';
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