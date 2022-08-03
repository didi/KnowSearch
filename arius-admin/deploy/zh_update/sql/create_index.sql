create index idx_project_id
    on es_manager_test3.project_template_info (project_id);
create index idx_project_id
    on es_manager_test3.index_template_info (project_id);
create index idx_data_center
    on es_manager_test3.index_template_info (data_center);
# 创建索引
create index idx_project_id
    on es_manager_test3.project_logi_cluster_auth (project_id);
create index idx_project_id
    on es_manager_test3.project_arius_resource_logic (project_id);

create index idx_log_id_statud
    on es_manager_test3.index_template_physical_info (logic_id, status);

create index idx_region_id
    on es_manager_test3.index_template_physical_info (region_id);
create index idx_region_id
    on es_manager_test3.es_cluster_role_host_info (region_id);