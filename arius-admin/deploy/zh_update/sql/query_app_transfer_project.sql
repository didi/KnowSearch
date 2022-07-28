# 通过query_app表反向生成project_id

insert into es_manager_test2.logi_security_project (id, project_name, description, dept_id, running, is_delete,
                                                    app_name, project_code)
select id,
       name                                            project_name,
       memo                                            description,
       0                                               dept_id,
       1                                               running,
       0                                               is_delete,
       'know_search'                                   app_name,
       concat('p', ceil(rand() * (100000000 - 1) + 1)) project_code
from query_app;

# 通过query_app生成project_config
insert into es_manager_test2.project_arius_config(project_id, analyze_response_enable, is_source_separated,
                                                  aggr_analyze_enable, dsl_analyze_enable, slow_query_times, is_active,
                                                  memo)
select id,
       analyze_response_enable,
       is_source_separated,
       aggr_analyze_enable,
       dsl_analyze_enable,
       1000    slow_query_times,
       1    as is_active,
       memo as memo
from query_app;

# 通过query_app生成es_user
insert into es_manager_test2.arius_es_user(id, index_exp, data_center, is_root, memo, ip, verify_code, is_active,
                                           query_threshold, cluster, search_type,
                                           project_id, is_default_display)
select id,
       index_exp,
       data_center,
       is_root,
       memo,
       ip,
       verify_code,
       is_active,
       query_threshold,
       cluster,
       search_type,
       id as project_id,
       1  as is_default_display
from query_app;