insert into index_template
(id, is_active, `name`, data_type, date_format, data_center, expire_time, hot_time, department_id, department, responsible, date_field, date_field_format, id_field, routing_field, expression,`desc`, quota, app_id, ingest_pipeline)
values
(1000, 1, 'arius.dsl.analyze.result', -1, '', 'cn', -1, '2', 'bu_809', '商业数据产品团队', '1,3',  'logTime', '', '', '', 'arius.dsl.analyze.result', 'DSL分析结果', 1, 3, '');

insert into index_template_physical
(id, logic_id, `name`, expression, cluster, rack, shard, shard_routing, version, role, config)
values
(1000, 1000, 'arius.dsl.analyze.result', 'arius.dsl.analyze.result', 'dc-es02', 'r1', 16, 10, 0, 1, "");




insert into index_template
(id, is_active, `name`, data_type, date_format, data_center, expire_time, hot_time,  department_id, department, responsible, date_field, date_field_format, id_field, routing_field, expression,`desc`, quota, app_id, ingest_pipeline)
values
(1001, 1, 'arius.dsl.metrics', 2, '_yyyy-MM-dd', 'cn', 150, '2', 'bu_809', '商业数据产品团队', '1,3',  'timeStamp', '', '', '', 'arius.dsl.metrics*', '用户查询聚合信息', 1, 3, '');

insert into index_template_physical
(id, logic_id, `name`, expression, cluster, rack, shard, shard_routing, version, role, config)
values
(1001, 1001, 'arius.dsl.metrics', 'arius.dsl.metrics*', 'dc-es02', 'r1', 16, 10, 0, 1, "");




insert into index_template
(id, is_active, `name`, data_type, date_format, data_center, expire_time, hot_time,  department_id, department, responsible, date_field, date_field_format, id_field, routing_field, expression,`desc`, quota, app_id, ingest_pipeline)
values
(1002, 1, 'arius.gateway.join', 2, '_yyyy-MM-dd', 'cn', 150, '2', 'bu_809', '商业数据产品团队', '1,3', 'timeStamp', '', '', '', 'arius.gateway.join*', 'gateway日志按照requestId进行join', 1, 3, '');

insert into index_template_physical
(id, logic_id, `name`, expression, cluster, rack, shard, shard_routing, version, role, config)
values
(1002, 1002, 'arius.gateway.join', 'arius.gateway.join*', 'dc-es02', 'r1', 16, 10, 0, 1, "");




insert into index_template
(id, is_active, `name`, data_type, date_format, data_center, expire_time, hot_time,  department_id, department, responsible, date_field, date_field_format, id_field, routing_field, expression,`desc`, quota, app_id, ingest_pipeline)
values
(1003, 1, 'arius_monitor_alarm_history', 2, '_yyyy-MM-dd', 'cn', 150, '2', 'bu_809', '商业数据产品团队', '1,3', 'etime', '', '', '', 'arius_monitor_alarm_history*', '日志服务所有历史报警入ES日志服务所有历史报警入ES日志服务所有历史报警入ES日志服务所有历史报警入ES', 1, 3, '');

insert into index_template_physical
(id, logic_id, `name`, expression, cluster, rack, shard, shard_routing, version, role, config)
values
(1003, 1003, 'arius_monitor_alarm_history', 'arius_monitor_alarm_history*', 'dc-es02', 'r1', 16, 10, 0, 1, "");




insert into index_template
(id, is_active, `name`, data_type, date_format, data_center, expire_time, hot_time,  department_id, department, responsible, date_field, date_field_format, id_field, routing_field, expression,`desc`, quota, app_id, ingest_pipeline)
values
(1004, 1, 'arius.dsl.template', 2, '', 'cn', -1, '2', 'bu_809', '商业数据产品团队', '1,3', 'logTime', '', '', '', 'arius.dsl.template', 'DSL审核与分析', 1, 3, '');

insert into index_template_physical
(id, logic_id, `name`, expression, cluster, rack, shard, shard_routing, version, role, config)
values
(1004, 1004, 'arius.dsl.template', 'arius.dsl.template', 'dc-es02', 'r1', 16, 10, 0, 1, "");







insert into index_template
(id, is_active, `name`, data_type, date_format, data_center, expire_time, hot_time,  department_id, department, responsible, date_field, date_field_format, id_field, routing_field, expression,`desc`, quota, app_id, ingest_pipeline)
values
(1005, 1, 'arius.template.field', 2, '', 'cn', -1, '2', 'bu_809', '商业数据产品团队', '1,3', 'logTime', '', '', '', 'arius.template.field', '索引模板字段数据', 1, 3, '');

insert into index_template_physical
(id, logic_id, `name`, expression, cluster, rack, shard, shard_routing, version, role, config)
values
(1005, 1005, 'arius.template.field', 'arius.template.field', 'dc-es02', 'r1', 16, 10, 0, 1, "");




insert into index_template
(id, is_active, `name`, data_type, date_format, data_center, expire_time, hot_time,  department_id, department, responsible, date_field, date_field_format, id_field, routing_field, expression,`desc`, quota, app_id, ingest_pipeline)
values
(1006, 1, 'arius.dsl.field.use', 2, '', 'cn', -1, '2', 'bu_809', '商业数据产品团队', '1,3', 'logTime', '', '', '', 'arius.dsl.field.use', '字段使用索引', 1, 3, '');

insert into index_template_physical
(id, logic_id, `name`, expression, cluster, rack, shard, shard_routing, version, role, config)
values
(1006, 1006, 'arius.dsl.field.use', 'arius.dsl.field.use', 'dc-es02', 'r1', 16, 10, 0, 1, "");




insert into index_template
(id, is_active, `name`, data_type, date_format, data_center, expire_time, hot_time,  department_id, department, responsible, date_field, date_field_format, id_field, routing_field, expression,`desc`, quota, app_id, ingest_pipeline)
values
(1007, 1, 'arius.template.mapping', 2, '', 'cn', -1, '2', 'bu_809', '商业数据产品团队', '1,3', 'logTime', '', '', '', 'arius.template.mapping', '索引mapping', 1, 3, '');

insert into index_template_physical
(id, logic_id, `name`, expression, cluster, rack, shard, shard_routing, version, role, config)
values
(1007, 1007, 'arius.template.mapping', 'arius.template.mapping', 'dc-es02', 'r1', 16, 10, 0, 1, "");




insert into index_template
(id, is_active, `name`, data_type, date_format, data_center, expire_time, hot_time,  department_id, department, responsible, date_field, date_field_format, id_field, routing_field, expression,`desc`, quota, app_id, ingest_pipeline)
values
(1008, 1, 'arius.index.size', 2, '', 'cn', -1, '2', 'bu_809', '商业数据产品团队', '1,3', 'logTime', '', '', '', 'arius.index.size', '索引大小', 1, 3, '');

insert into index_template_physical
(id, logic_id, `name`, expression, cluster, rack, shard, shard_routing, version, role, config)
values
(1008, 1008, 'arius.index.size', 'arius.index.size', 'dc-es02', 'r1', 16, 10, 0, 1, "");




insert into index_template
(id, is_active, `name`, data_type, date_format, data_center, expire_time, hot_time,  department_id, department, responsible, date_field, date_field_format, id_field, routing_field, expression,`desc`, quota, app_id, ingest_pipeline)
values
(1009, 1, 'arius.template.hit', 2, '', 'cn', -1, '2', 'bu_809', '商业数据产品团队', '1,3', 'logTime', '', '', '', 'arius.template.hit', '查询索引命中', 1, 3, '');

insert into index_template_physical
(id, logic_id, `name`, expression, cluster, rack, shard, shard_routing, version, role, config)
values
(1009, 1009, 'arius.template.hit', 'arius.template.hit', 'dc-es02', 'r1', 16, 10, 0, 1, "");





insert into index_template
(id, is_active, `name`, data_type, date_format, data_center, expire_time, hot_time,  department_id, department, responsible, date_field, date_field_format, id_field, routing_field, expression,`desc`, quota, app_id, ingest_pipeline)
values
(1010, 1, 'arius.template.cold', 2, '', 'cn', -1, '2', 'bu_809', '商业数据产品团队', '1,3', 'logTime', '', '', '', 'arius.template.cold', '冷存储模版配置', 1, 3, '');

insert into index_template_physical
(id, logic_id, `name`, expression, cluster, rack, shard, shard_routing, version, role, config)
values
(1010, 1010, 'arius.template.cold', 'arius.template.cold', 'dc-es02', 'r1', 16, 10, 0, 1, "");





insert into index_template
(id, is_active, `name`, data_type, date_format, data_center, expire_time, hot_time,  department_id, department, responsible, date_field, date_field_format, id_field, routing_field, expression,`desc`, quota, app_id, ingest_pipeline)
values
(1011, 1, 'arius.template.cold.core', 2, '', 'cn', -1, '2', 'bu_809', '商业数据产品团队', '1,3', 'logTime', '', '', '', 'arius.template.cold.core', '核心索引', 1, 3, '');

insert into index_template_physical
(id, logic_id, `name`, expression, cluster, rack, shard, shard_routing, version, role, config)
values
(1011, 1011, 'arius.template.cold.core', 'arius.template.cold.core', 'dc-es02', 'r1', 16, 10, 0, 1, "");





insert into index_template
(id, is_active, `name`, data_type, date_format, data_center, expire_time, hot_time,  department_id, department, responsible, date_field, date_field_format, id_field, routing_field, expression,`desc`, quota, app_id, ingest_pipeline)
values
(1012, 1, 'arius.md5.relation', 2, '', 'cn', -1, '2', 'bu_809', '商业数据产品团队', '1,3', 'logTime', '', '', '', 'arius.md5.relation', '查询模板平滑升级', 1, 3, '');

insert into index_template_physical
(id, logic_id, `name`, expression, cluster, rack, shard, shard_routing, version, role, config)
values
(1012, 1012, 'arius.md5.relation', 'arius.md5.relation', 'dc-es02', 'r1', 16, 10, 0, 1, "");




insert into index_template
(id, is_active, `name`, data_type, date_format, data_center, expire_time, hot_time,  department_id, department, responsible, date_field, date_field_format, id_field, routing_field, expression,`desc`, quota, app_id, ingest_pipeline)
values
(1013, 1, 'arius.schedule.job', 2, '
', 'cn', -1, '2', 'bu_809', '商业数据产品团队', '1,3', 'logTime', '', '', '', 'arius.schedule.job', '调度记录', 1, 3, '');

insert into index_template_physical
(id, logic_id, `name`, expression, cluster, rack, shard, shard_routing, version, role, config)
values
(1013, 1013, 'arius.schedule.job', 'arius.schedule.job', 'dc-es02', 'r1', 16, 10, 0, 1, "");




insert into index_template
(id, is_active, `name`, data_type, date_format, data_center, expire_time, hot_time,  department_id, department, responsible, date_field, date_field_format, id_field, routing_field, expression,`desc`, quota, app_id, ingest_pipeline)
values
(1014, 1, 'arius_stats_info', 2, '_yyyy-MM-dd', 'cn', 90, '2', 'bu_809', '商业数据产品团队', '1,3', 'timestamp', '', '', '', 'arius_stats_info*', '调度记录', 1, 3, '');

insert into index_template_physical
(id, logic_id, `name`, expression, cluster, rack, shard, shard_routing, version, role, config)
values
(1014, 1014, 'arius_stats_info', 'arius_stats_info*', 'dc-es02', 'r1', 16, 10, 0, 1, "");




insert into index_template
(id, is_active, `name`, data_type, date_format, data_center, expire_time, hot_time,  department_id, department, responsible, date_field, date_field_format, id_field, routing_field, expression,`desc`, quota, app_id, ingest_pipeline)
values
(1015, 1, 'arius.field.cardinal.number', 2, '', 'cn', -1, '2', 'bu_809', '商业数据产品团队', '1,3', 'logTime', '', '', '', 'arius.field.cardinal.number', '索引基数统计', 1, 3, '');

insert into index_template_physical
(id, logic_id, `name`, expression, cluster, rack, shard, shard_routing, version, role, config)
values
(1015, 1015, 'arius.field.cardinal.number', 'arius.field.cardinal.number', 'dc-es02', 'r1', 16, 10, 0, 1, "");




insert into index_template
(id, is_active, `name`, data_type, date_format, data_center, expire_time, hot_time,  department_id, department, responsible, date_field, date_field_format, id_field, routing_field, expression,`desc`, quota, app_id, ingest_pipeline)
values
(1016, 1, 'foundation_fd.data-online.arius.gateway', 2, '_yyyy-MM-dd', 'cn', -1, '2', 'bu_809', '商业数据产品团队', '1,3', 'logTime', '', '', '', 'foundation_fd.data-online.arius.gateway*', '索引基数统计', 1, 3, '');

insert into index_template_physical
(id, logic_id, `name`, expression, cluster, rack, shard, shard_routing, version, role, config)
values
(1016, 1016, 'foundation_fd.data-online.arius.gateway', 'foundation_fd.data-online.arius.gateway*', 'dc-es02', 'r1', 16, 10, 0, 1, "");




insert into index_template
(id, is_active, `name`, data_type, date_format, data_center, expire_time, hot_time,  department_id, department, responsible, date_field, date_field_format, id_field, routing_field, expression,`desc`, quota, app_id, ingest_pipeline)
values
(1017, 1, 'arius.appid.template.access', 2, '', 'cn', -1, '2', 'bu_809', '商业数据产品团队', '1,3', 'logTime', '', '', '', 'arius.appid.template.access', 'appid维度访问次数索引', 1, 3, '');

insert into index_template_physical
(id, logic_id, `name`, expression, cluster, rack, shard, shard_routing, version, role, config)
values
(1017, 1017, 'arius.appid.template.access', 'arius.appid.template.access', 'dc-es02', 'r1', 16, 10, 0, 1, "");




insert into index_template
(id, is_active, `name`, data_type, date_format, data_center, expire_time, hot_time,  department_id, department, responsible, date_field, date_field_format, id_field, routing_field, expression,`desc`, quota, app_id, ingest_pipeline)
values
(1018, 1, 'arius.indexname.access', 2, '', 'cn', -1, '2', 'bu_809', '商业数据产品团队', '1,3', 'logTime', '', '', '', 'arius.indexname.access', '索引维度访问次数索引', 1, 3, '');

insert into index_template_physical
(id, logic_id, `name`, expression, cluster, rack, shard, shard_routing, version, role, config)
values
(1018, 1018, 'arius.indexname.access', 'arius.indexname.access', 'dc-es02', 'r1', 16, 10, 0, 1, "");





insert into index_template
(id, is_active, `name`, data_type, date_format, data_center, expire_time, hot_time,  department_id, department, responsible, date_field, date_field_format, id_field, routing_field, expression,`desc`, quota, app_id, ingest_pipeline)
values
(1019, 1, 'arius.template.label', 2, '', 'cn', -1, '2', 'bu_809', '商业数据产品团队', '1,3', 'logTime', '', '', '', 'arius.template.label', '索引标签数据', 1, 3, '');

insert into index_template_physical
(id, logic_id, `name`, expression, cluster, rack, shard, shard_routing, version, role, config)
values
(1019, 1019, 'arius.template.label', 'arius.template.label', 'dc-es02', 'r1', 16, 10, 0, 1, "");





insert into index_template
(id, is_active, `name`, data_type, date_format, data_center, expire_time, hot_time,  department_id, department, responsible, date_field, date_field_format, id_field, routing_field, expression,`desc`, quota, app_id, ingest_pipeline)
values
(1020, 1, 'arius.template.label', 2, '', 'cn', -1, '2', 'bu_809', '商业数据产品团队', '1,3', 'logTime', '', '', '', 'arius.template.label', '索引标签数据', 1, 3, '');

insert into index_template_physical
(id, logic_id, `name`, expression, cluster, rack, shard, shard_routing, version, role, config)
values
(1020, 1020, 'arius.template.label', 'arius.template.label', 'dc-es02', 'r1', 16, 10, 0, 1, "");






insert into index_template
(id, is_active, `name`, data_type, date_format, data_center, expire_time, hot_time,  department_id, department, responsible, date_field, date_field_format, id_field, routing_field, expression,`desc`, quota, app_id, ingest_pipeline)
values
(1021, 1, 'arius.admin.db', 2, '', 'cn', -1, '2', 'bu_809', '商业数据产品团队', '1,3', 'logTime', '', '', '', 'arius.admin.db', 'Arius数据库索引', 1, 3, '');

insert into index_template_physical
(id, logic_id, `name`, expression, cluster, rack, shard, shard_routing, version, role, config)
values
(1021, 1021, 'arius.admin.db', 'arius.admin.db', 'dc-es02', 'r1', 16, 10, 0, 1, "");





insert into index_template
(id, is_active, `name`, data_type, date_format, data_center, expire_time, hot_time,  department_id, department, responsible, date_field, date_field_format, id_field, routing_field, expression,`desc`, quota, app_id, ingest_pipeline)
values
(1022, 1, 'arius.template.customize.label', 2, '', 'cn', -1, '2', 'bu_809', '商业数据产品团队', '1,3', 'logTime', '', '', '', 'arius.template.customize.label', '用户自定义标签', 1, 3, '');

insert into index_template_physical
(id, logic_id, `name`, expression, cluster, rack, shard, shard_routing, version, role, config)
values
(1022, 1022, 'arius.template.customize.label', 'arius.template.customize.label', 'dc-es02', 'r1', 16, 10, 0, 1, "");





insert into index_template
(id, is_active, `name`, data_type, date_format, data_center, expire_time, hot_time,  department_id, department, responsible, date_field, date_field_format, id_field, routing_field, expression,`desc`, quota, app_id, ingest_pipeline)
values
(1023, 1, 'health_check_info', 2, '_yyyy-MM-dd', 'cn', 30, '2', 'bu_809', '商业数据产品团队', '1,3', 'logTime', '', '', '', 'health_check_info*', 'es健康检查统计', 1, 3, '');

insert into index_template_physical
(id, logic_id, `name`, expression, cluster, rack, shard, shard_routing, version, role, config)
values
(1023, 1023, 'health_check_info', 'health_check_info*', 'dc-es02', 'r1', 16, 10, 0, 1, "");




insert into index_template
(id, is_active, `name`, data_type, date_format, data_center, expire_time, hot_time,  department_id, department, responsible, date_field, date_field_format, id_field, routing_field, expression,`desc`, quota, app_id, ingest_pipeline)
values
(1024, 1, 'healht_check_white_list', 2, '', 'cn', -1, '2', 'bu_809', '商业数据产品团队', '1,3', 'logTime', '', '', '', 'healht_check_white_list', 'es健康检查统计白名单, 不进行健康检查', 1, 3, '');

insert into index_template_physical
(id, logic_id, `name`, expression, cluster, rack, shard, shard_routing, version, role, config)
values
(1024, 1024, 'healht_check_white_list', 'healht_check_white_list', 'dc-es02', 'r1', 16, 10, 0, 1, "");





insert into index_template
(id, is_active, `name`, data_type, date_format, data_center, expire_time, hot_time,  department_id, department, responsible, date_field, date_field_format, id_field, routing_field, expression,`desc`, quota, app_id, ingest_pipeline)
values
(1025, 1, 'index_health_degree', 2, '_yyyy-MM-dd', 'cn', 15, '2', 'bu_809', '商业数据产品团队', '1,3', 'timestamp', '', '', '', 'index_health_degree*', '健康分计算结果索引', 1, 3, '');

insert into index_template_physical
(id, logic_id, `name`, expression, cluster, rack, shard, shard_routing, version, role, config)
values
(1025, 1025, 'index_health_degree', 'index_health_degree*', 'dc-es02', 'r1', 16, 10, 0, 1, "");




insert into index_template
(id, is_active, `name`, data_type, date_format, data_center, expire_time, hot_time,  department_id, department, responsible, date_field, date_field_format, id_field, routing_field, expression,`desc`, quota, app_id, ingest_pipeline)
values
(1026, 1, 'arius_cost_depart_cluster_detail', 2, '_yyyy-MM-dd', 'cn', 15, '2', 'bu_809', '商业数据产品团队', '1,3', 'logTime', '', '', '', 'arius_cost_depart_cluster_detail*', 'arius独立集群成本统计, 不要随意改成按月保存', 1, 3, '');

insert into index_template_physical
(id, logic_id, `name`, expression, cluster, rack, shard, shard_routing, version, role, config)
values
(1026, 1026, 'arius_cost_depart_cluster_detail', 'arius_cost_depart_cluster_detail*', 'dc-es02', 'r1', 16, 10, 0, 1, "");






insert into index_template
(id, is_active, `name`, data_type, date_format, data_center, expire_time, hot_time,  department_id, department, responsible, date_field, date_field_format, id_field, routing_field, expression,`desc`, quota, app_id, ingest_pipeline)
values
(1027, 1, 'arius_cost_depart_index_detail', 2, '_yyyy-MM-dd', 'cn', 150, '2', 'bu_809', '商业数据产品团队', '1,3', 'logTime', '', '', '', 'arius_cost_depart_index_detail*', 'arius索引成本统计, 不要随意改成按月保存', 1, 3, '');

insert into index_template_physical
(id, logic_id, `name`, expression, cluster, rack, shard, shard_routing, version, role, config)
values
(1027, 1027, 'arius_cost_depart_index_detail', 'arius_cost_depart_index_detail*', 'dc-es02', 'r1', 16, 10, 0, 1, "");





insert into index_template
(id, is_active, `name`, data_type, date_format, data_center, expire_time, hot_time,  department_id, department, responsible, date_field, date_field_format, id_field, routing_field, expression,`desc`, quota, app_id, ingest_pipeline)
values
(1028, 1, 'arius_cost_t2_total', 2, '_yyyy-MM-dd', 'cn', 150, '2', 'bu_809', '商业数据产品团队', '1,3', 'logTime', '', '', '', 'arius_cost_t2_total*', 'arius二级部门成本统计, 不要随意改成按月保存', 1, 3, '');

insert into index_template_physical
(id, logic_id, `name`, expression, cluster, rack, shard, shard_routing, version, role, config)
values
(1028, 1028, 'arius_cost_t2_total', 'arius_cost_t2_total*', 'dc-es02', 'r1', 16, 10, 0, 1, "");





insert into index_template
(id, is_active, `name`, data_type, date_format, data_center, expire_time, hot_time,  department_id, department, responsible, date_field, date_field_format, id_field, routing_field, expression,`desc`, quota, app_id, ingest_pipeline)
values
(1029, 1, 'arius_cost_t3_total', 2, '_yyyy-MM-dd', 'cn', 150, '2', 'bu_809', '商业数据产品团队', '1,3', 'logTime', '', '', '', 'arius_cost_t3_total*', 'arius三级部门成本统计, 不要随意改成按月保存', 1, 3, '');

insert into index_template_physical
(id, logic_id, `name`, expression, cluster, rack, shard, shard_routing, version, role, config)
values
(1029, 1029, 'arius_cost_t3_total', 'arius_cost_t3_total*', 'dc-es02', 'r1', 16, 10, 0, 1, "");


insert into index_template
(id, is_active, `name`, data_type, date_format, data_center, expire_time, hot_time,  department_id, department, responsible, date_field, date_field_format, id_field, routing_field, expression,`desc`, quota, app_id, ingest_pipeline)
values
(1030, 1, 'arius_template_quota_usage', 2, '', 'cn', -1, '2', 'bu_809', '商业数据产品团队', '1,3', 'logTime', '', '', '', 'arius_template_quota_usage', 'DSL审核与分析', 1, 3, '');

insert into index_template_physical
(id, logic_id, `name`, expression, cluster, rack, shard, shard_routing, version, role, config)
values
(1030, 1030, 'arius_template_quota_usage', 'arius_template_quota_usage', 'dc-es02', 'r1', 16, 10, 0, 1, "");
