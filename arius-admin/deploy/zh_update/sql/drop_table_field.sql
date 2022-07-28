alter table es_manager_test3.index_template_info
    drop department_id ;
alter table es_manager_test3.index_template_info
    drop responsible ;

alter table es_manager_test3.project_arius_resource_logic
    drop responsible ;

alter table es_manager_test3.project_arius_resource_logic
    drop department_id;
alter table es_manager_test3.project_arius_resource_logic
    drop department;

alter table es_manager_test3.project_logi_cluster_auth
    drop responsible ;
alter table es_manager_test3.project_template_info
    drop responsible_ids ;