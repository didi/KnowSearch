# config修改 arius_config_info

# cluster_monitor_task_v2-> arius_meta_job_cluster_distribute
ALTER TABLE zh_test.cluster_monitor_task_v2 RENAME TO es_manager_test3.arius_meta_job_cluster_distribute;

# arius_work_task->arius_op_task
ALTER TABLE zh_test.arius_work_task RENAME TO es_manager_test3.arius_op_task;


# work_order->arius_work_order_info
ALTER TABLE zh_test.work_order RENAME TO es_manager_test3.arius_work_order_info;
#  es_data_source-----》es_cluster_phy_info
ALTER TABLE zh_test.es_data_source RENAME TO es_manager_test3.es_cluster_phy_info;
#  es_role_cluster_host-----》es_cluster_role_host_info
ALTER TABLE zh_test.es_role_cluster_host RENAME TO es_manager_test3.es_cluster_role_host_info;
# es_role_cluster->es_cluster_role_info
ALTER TABLE zh_test.es_role_cluster RENAME TO es_manager_test3.es_cluster_role_info;
# es_config->es_config
# es_machine_norms->es_machine_norms
# gateway_cluster->gateway_cluster_info
ALTER TABLE zh_test.gateway_cluster RENAME TO es_manager_test3.gateway_cluster_info;

# gateway_node->gateway_cluster_info
ALTER TABLE zh_test.gateway_node RENAME TO es_manager_test3.gateway_cluster_info;
#index_template->index_template_info
ALTER TABLE zh_test.index_template RENAME TO es_manager_test3.index_template_info;
# index_template_physical -> index_template_physical_info
ALTER TABLE zh_test.index_template_physical RENAME TO es_manager_test3.index_template_physical_info;
# arius_resource_logic->project_arius_resource_logic
ALTER TABLE zh_test.arius_resource_logic RENAME TO es_manager_test3.project_arius_resource_logic;
# app_logic_cluster_auth ->project_logi_cluster_auth
ALTER TABLE zh_test.app_logic_cluster_auth RENAME TO es_manager_test3.project_logi_cluster_auth;
# metrics_config-》user_metrics_config_info
ALTER TABLE zh_test.metrics_config RENAME TO es_manager_test3.user_metrics_config_info;
# appid_template_info  -> project_template_info
ALTER TABLE zh_test.appid_template_info RENAME TO es_manager_test3.project_template_info;