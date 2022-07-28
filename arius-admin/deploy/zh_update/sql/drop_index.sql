# 删除app_id和responsible的索引
drop index idx_app_id on appid_template_info;

drop index idx_responsibleids on appid_template_info;
# 删除索引
drop index idx_app_id on es_manager_test3.project_logi_cluster_auth;

drop index idx_responsible on es_manager_test3.project_logi_cluster_auth;