alter table es_manager_test3.arius_work_order_info
    change approver_app_id approver_project_id int default -1 not null comment '审批人projectid' after title;

alter table es_manager_test3.arius_work_order_info
    change applicant_app_id applicant_project_id int default -1 not null comment '申请人projectid';
# appid->projectid
alter table es_manager_test3.index_template_info
    change app_id projectId int(10) default -1 not null comment 'project_id';
# appid->project_id
alter table es_manager_test3.project_arius_resource_logic
    change app_id projectId int(10) default -1 not null comment 'project_id';
# app_id ->project_id
alter table es_manager_test3.project_logi_cluster_auth
    change app_id projectId int(10) default -1 not null comment 'project_id';
# app_id ->project_id
alter table es_manager_test3.project_template_info
    change app_id projectId int(10) default -1 not null comment 'project_id';
# domain_account ->user_name
alter table es_manager_test3.user_metrics_config_info
    change domain_account user_name varchar(100) default '' not null comment '用户账号';