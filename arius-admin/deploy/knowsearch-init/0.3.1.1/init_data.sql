
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
# 1.更新level和leaf
UPDATE logi_security_permission SET permission_name = 'DSL查询', parent_id = 0, leaf = 0, level = 1, description = 'DSL查询', create_time = '2022-05-24 18:08:24.0', update_time = '2022-09-02 19:01:17.0', is_delete = 0, app_name = 'know_search' WHERE id = 1757;
UPDATE logi_security_permission SET permission_name = 'Kibana', parent_id = 0, leaf = 0, level = 1, description = 'Kibana', create_time = '2022-05-24 18:08:26.0', update_time = '2022-09-02 19:01:17.0', is_delete = 0, app_name = 'know_search' WHERE id = 1855;
UPDATE logi_security_permission SET permission_name = 'SQL查询', parent_id = 0, leaf = 0, level = 1, description = 'SQL查询', create_time = '2022-05-24 18:08:26.0', update_time = '2022-09-02 19:01:17.0', is_delete = 0, app_name = 'know_search' WHERE id = 1857;
#1.1修改logi_security_role_permission
UPDATE logi_security_role_permission SET role_id = 1, permission_id = 1609, create_time = '2022-06-14 17:41:03.0', update_time = '2022-09-02 19:04:07.0', is_delete = 1, app_name = 'know_search' WHERE id = 2059;
UPDATE logi_security_role_permission SET role_id = 1, permission_id = 1757, create_time = '2022-06-14 17:41:03.0', update_time = '2022-08-25 10:31:42.0', is_delete = 0, app_name = 'know_search' WHERE id = 2061;
UPDATE logi_security_role_permission SET role_id = 1, permission_id = 1855, create_time = '2022-06-14 17:41:03.0', update_time = '2022-08-25 10:34:13.0', is_delete = 0, app_name = 'know_search' WHERE id = 2063;
UPDATE logi_security_role_permission SET role_id = 1, permission_id = 1857, create_time = '2022-06-14 17:41:03.0', update_time = '2022-08-25 10:31:42.0', is_delete = 0, app_name = 'know_search' WHERE id = 2065;
UPDATE logi_security_role_permission SET role_id = 2, permission_id = 1609, create_time = '2022-06-14 18:08:56.0', update_time = '2022-09-02 19:04:07.0', is_delete = 1, app_name = 'know_search' WHERE id = 2241;
UPDATE logi_security_role_permission SET role_id = 2, permission_id = 1757, create_time = '2022-06-14 18:08:56.0', update_time = '2022-08-25 10:31:42.0', is_delete = 0, app_name = 'know_search' WHERE id = 2243;
UPDATE logi_security_role_permission SET role_id = 2, permission_id = 1855, create_time = '2022-06-14 18:08:56.0', update_time = '2022-08-25 10:33:12.0', is_delete = 1, app_name = 'know_search' WHERE id = 2245;
UPDATE logi_security_role_permission SET role_id = 2, permission_id = 1857, create_time = '2022-06-14 18:08:56.0', update_time = '2022-08-25 10:31:42.0', is_delete = 0, app_name = 'know_search' WHERE id = 2247;
#2.更新name
UPDATE logi_security_permission SET permission_name = 'Kibana查询', parent_id = 0, leaf = 0, level = 1, description = 'Kibana查询', create_time = '2022-05-24 18:08:26.0', update_time = '2022-09-05 14:19:29.0', is_delete = 0, app_name = 'know_search' WHERE id = 1855;
#3.新增3个权限点
INSERT INTO logi_security_permission (permission_name, parent_id, leaf, level, description, create_time, update_time, is_delete, app_name) VALUES ('DSL', 0, 0, 1, 'DSL', '2022-05-24 18:08:24.0', '2022-09-02 19:01:17.0', 0, 'know_search');
INSERT INTO logi_security_permission (permission_name, parent_id, leaf, level, description, create_time, update_time, is_delete, app_name) VALUES ('Kibana', 0, 0, 1, 'Kibana', '2022-05-24 18:08:26.0', '2022-09-02 19:01:17.0', 0, 'know_search');
INSERT INTO logi_security_permission (permission_name, parent_id, leaf, level, description, create_time, update_time, is_delete, app_name) VALUES ('SQL', 0, 0, 1, 'SQL', '2022-05-24 18:08:26.0', '2022-09-02 19:01:17.0', 0, 'know_search');
#3.1 新增logi_security_role_permission
insert into logi_security_role_permission(role_id, permission_id, is_delete, app_name)
values (1, 1877, 0, 'know_search'),
       (1, 1879, 0, 'know_search'),
       (1, 1881, 0, 'know_search'),
       (2, 1877, 0, 'know_search'),
       (2, 1879, 1, 'know_search'),
       (2, 1881, 0, 'know_search');

#4.再次更新level和leaf
UPDATE logi_security_permission SET permission_name = 'DSL查询', parent_id = 1877, leaf = 1, level = 2, description = 'DSL查询' WHERE id = 1757;
UPDATE logi_security_permission SET permission_name = 'Kibana查询', parent_id = 1879, leaf = 1, level = 2, description = 'Kibana查询' WHERE id = 1855;
UPDATE logi_security_permission SET permission_name = 'SQL查询', parent_id = 1881, leaf = 1, level = 2, description = 'SQL查询' WHERE id = 1857;