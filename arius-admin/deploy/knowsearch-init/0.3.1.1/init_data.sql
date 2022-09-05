
/*
0.3.1原始sql
UPDATE logi_security_permission SET permission_name = 'Kibana', parent_id = 1609, leaf = 1, level = 2, description = 'Kibana', create_time = '2022-05-24 18:08:26.0', update_time = '2022-06-14 16:44:02.0', is_delete = 0, app_name = 'know_search' WHERE id = 1855;
UPDATE logi_security_permission SET permission_name = 'SQL查询', parent_id = 1609, leaf = 1, level = 2, description = 'SQL查询', create_time = '2022-05-24 18:08:26.0', update_time = '2022-06-14 16:44:02.0', is_delete = 0, app_name = 'know_search' WHERE id = 1857;
UPDATE logi_security_permission SET permission_name = 'DSL查询', parent_id = 1609, leaf = 1, level = 2, description = 'DSL查询', create_time = '2022-05-24 18:08:24.0', update_time = '2022-06-14 16:39:48.0', is_delete = 0, app_name = 'know_search' WHERE id = 1757;
UPDATE logi_security_role_permission SET role_id = 1, permission_id = 1609, create_time = '2022-06-14 17:41:03.0', update_time = '2022-08-25 10:31:42.0', is_delete = 0, app_name = 'know_search' WHERE id = 2059;
UPDATE logi_security_role_permission SET role_id = 1, permission_id = 1757, create_time = '2022-06-14 17:41:03.0', update_time = '2022-08-25 10:31:42.0', is_delete = 0, app_name = 'know_search' WHERE id = 2061;
UPDATE logi_security_role_permission SET role_id = 1, permission_id = 1855, create_time = '2022-06-14 17:41:03.0', update_time = '2022-08-25 10:34:13.0', is_delete = 0, app_name = 'know_search' WHERE id = 2063;
UPDATE logi_security_role_permission SET role_id = 1, permission_id = 1857, create_time = '2022-06-14 17:41:03.0', update_time = '2022-08-25 10:31:42.0', is_delete = 0, app_name = 'know_search' WHERE id = 2065;
UPDATE logi_security_role_permission SET role_id = 2, permission_id = 1609, create_time = '2022-06-14 18:08:56.0', update_time = '2022-08-25 10:31:42.0', is_delete = 0, app_name = 'know_search' WHERE id = 2241;
UPDATE logi_security_role_permission SET role_id = 2, permission_id = 1757, create_time = '2022-06-14 18:08:56.0', update_time = '2022-08-25 10:31:42.0', is_delete = 0, app_name = 'know_search' WHERE id = 2243;
UPDATE logi_security_role_permission SET role_id = 2, permission_id = 1855, create_time = '2022-06-14 18:08:56.0', update_time = '2022-08-25 10:33:12.0', is_delete = 1, app_name = 'know_search' WHERE id = 2245;
UPDATE logi_security_role_permission SET role_id = 2, permission_id = 1857, create_time = '2022-06-14 18:08:56.0', update_time = '2022-08-25 10:31:42.0', is_delete = 0, app_name = 'know_search' WHERE id = 2247;


*/
# 0.3.1.1变更sql

UPDATE logi_security_permission SET permission_name = 'DSL查询', parent_id = 0, leaf = 0, level = 1, description = 'DSL查询', create_time = '2022-05-24 18:08:24.0', update_time = '2022-09-02 19:01:17.0', is_delete = 0, app_name = 'know_search' WHERE id = 1757;
UPDATE logi_security_permission SET permission_name = 'Kibana', parent_id = 0, leaf = 0, level = 1, description = 'Kibana', create_time = '2022-05-24 18:08:26.0', update_time = '2022-09-02 19:01:17.0', is_delete = 0, app_name = 'know_search' WHERE id = 1855;
UPDATE logi_security_permission SET permission_name = 'SQL查询', parent_id = 0, leaf = 0, level = 1, description = 'SQL查询', create_time = '2022-05-24 18:08:26.0', update_time = '2022-09-02 19:01:17.0', is_delete = 0, app_name = 'know_search' WHERE id = 1857;
UPDATE logi_security_role_permission SET role_id = 1, permission_id = 1609, create_time = '2022-06-14 17:41:03.0', update_time = '2022-09-02 19:04:07.0', is_delete = 1, app_name = 'know_search' WHERE id = 2059;
UPDATE logi_security_role_permission SET role_id = 1, permission_id = 1757, create_time = '2022-06-14 17:41:03.0', update_time = '2022-08-25 10:31:42.0', is_delete = 0, app_name = 'know_search' WHERE id = 2061;
UPDATE logi_security_role_permission SET role_id = 1, permission_id = 1855, create_time = '2022-06-14 17:41:03.0', update_time = '2022-08-25 10:34:13.0', is_delete = 0, app_name = 'know_search' WHERE id = 2063;
UPDATE logi_security_role_permission SET role_id = 1, permission_id = 1857, create_time = '2022-06-14 17:41:03.0', update_time = '2022-08-25 10:31:42.0', is_delete = 0, app_name = 'know_search' WHERE id = 2065;
UPDATE logi_security_role_permission SET role_id = 2, permission_id = 1609, create_time = '2022-06-14 18:08:56.0', update_time = '2022-09-02 19:04:07.0', is_delete = 1, app_name = 'know_search' WHERE id = 2241;
UPDATE logi_security_role_permission SET role_id = 2, permission_id = 1757, create_time = '2022-06-14 18:08:56.0', update_time = '2022-08-25 10:31:42.0', is_delete = 0, app_name = 'know_search' WHERE id = 2243;
UPDATE logi_security_role_permission SET role_id = 2, permission_id = 1855, create_time = '2022-06-14 18:08:56.0', update_time = '2022-08-25 10:33:12.0', is_delete = 1, app_name = 'know_search' WHERE id = 2245;
UPDATE logi_security_role_permission SET role_id = 2, permission_id = 1857, create_time = '2022-06-14 18:08:56.0', update_time = '2022-08-25 10:31:42.0', is_delete = 0, app_name = 'know_search' WHERE id = 2247;