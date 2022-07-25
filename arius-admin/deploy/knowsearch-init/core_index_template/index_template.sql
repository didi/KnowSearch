# ----------------------------------------------平台核心索引模版元数据
-- auto-generated definition
create table if not exists index_template_info
(
    id                bigint unsigned auto_increment comment '主键自增'
        primary key,
    name              varchar(128)                 default ''                not null comment '名称',
    data_type         tinyint                      default -1                not null comment '数据类型',
    date_format       varchar(50)                  default ''                not null comment '索引分区的时间后缀',
    is_active         tinyint(2)                   default 1                 not null comment '有效标记',
    data_center       varchar(20)                  default ''                not null comment '数据中心',
    expire_time       bigint                       default -1                not null comment '保存时长',
    hot_time          int(10)                      default -1                not null comment '热数据保存时长',
    responsible       varchar(500)                 default ''                null comment '责任人',
    date_field        varchar(50)                  default ''                not null comment '时间字段',
    date_field_format varchar(128)                 default ''                not null comment '时间字段的格式',
    id_field          varchar(512)                 default ''                null comment 'id字段',
    routing_field     varchar(512)                 default ''                null comment 'routing字段',
    expression        varchar(100)                 default ''                not null comment '索引表达式',
    `desc`            varchar(500)                 default ''                not null comment '索引描述',
    quota             decimal(10, 3)               default -1.000            not null comment '规格',
    project_id        int(10)                      default -1                not null comment 'project_id',
    ingest_pipeline   varchar(512)                 default ''                not null comment 'ingest_pipeline',
    block_read        tinyint(1) unsigned zerofill default 0                 not null comment '是否禁读，0：否，1：是',
    block_write       tinyint(1) unsigned zerofill default 0                 not null comment '是否禁写，0：否，1：是',
    create_time       timestamp                    default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time       timestamp                    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '修改时间',
    write_rate_limit  bigint(255)                  default -1                not null comment '写入限流值',
    resource_id       bigint                       default -1                not null comment '逻辑集群id',
    check_point_diff  bigint(100)                  default 0                 not null comment 'dcdr位点差',
    level             tinyint                      default 1                 not null comment '服务等级分为1,2,3',
    has_dcdr          tinyint(1) unsigned zerofill default 0                 not null comment '是否开启dcdr',
    open_srv          varchar(255)                                           null comment '已开启的模板服务',
    disk_size         decimal(10, 3)               default -1.000            null comment '可用磁盘容量'
)
    comment '逻辑索引模板表' charset = utf8;

create index idx_data_center
    on index_template_info (data_center);

create index idx_is_active
    on index_template_info (is_active);

create index idx_name
    on index_template_info (name);

create index idx_project_id
    on index_template_info (project_id);
insert into index_template_info (id, name, data_type, date_format, is_active, data_center, expire_time,
                                 hot_time, responsible, date_field, date_field_format, id_field,
                                 routing_field, expression, desc, quota, project_id, ingest_pipeline,
                                 block_read, block_write, create_time, update_time, write_rate_limit,
                                 resource_id, check_point_diff, level, has_dcdr, open_srv, disk_size)
values (24007, 'arius.appid.template.access', 2, '', 1, 'cn', -1, -1, 'admin', 'logTime', '', '', '',
        'arius.appid.template.access', 'appid维度访问次数索引', 1.000, 1, '', 0, 0, '2019-12-19 01:05:29.0',
        '2022-07-21 15:43:00.0', -1, 3865, 0, 1, 0, '2', 1.000),
       (1, 'arius.dsl.analyze.result', 0, '', 1, 'cn', -1, -1, '', 'logTime', '', '', '', 'arius.dsl.analyze.result',
        'DSL分析结果', 60.000, 1, 'arius.dsl.analyze.result', 0, 0, '2021-07-24 15:21:49.0', '2022-07-21 12:21:13.0', -1,
        3865, 0, 1, 0, '2', 60.000),
       (5, 'arius.dsl.metrics', 0, '_yyyy-MM-dd', 1, 'cn', 150, -1, '', 'timeStamp', '', '', '', 'arius.dsl.metrics*',
        '用户查询聚合信息', 60.000, 1, 'arius.dsl.metrics', 0, 0, '2021-07-24 15:21:52.0', '2022-07-21 15:43:18.0', -1, 3865, 0,
        1, 0, '2,1,4', 60.000),
       (7, 'arius.dsl.template', 0, '', 1, 'cn', -1, -1, '', 'logTime', '', '', '', 'arius.dsl.template', 'DSL审核与分析',
        60.000, 1, 'arius.dsl.template', 0, 0, '2021-07-24 15:21:53.0', '2022-07-21 12:20:49.0', -1, 3865, 0, 1, 0, '2',
        60.000),
       (9, 'arius.gateway.join', 0, '_yyyy-MM-dd', 1, 'cn', 23, -1, '', 'timeStamp', '', '', '', 'arius.gateway.join*',
        'gateway日志按照requestId进行join', 60.000, 1, 'arius.gateway.join', 0, 0, '2021-07-24 15:21:55.0',
        '2022-07-21 15:43:15.0', -1, 3865, 0, 1, 0, '2,1,4', 60.000),
       (14377, 'arius.template.access', 0, '', 1, 'cn', -1, -1, '1', 'logTime', '', '', '', 'arius.template.access',
        '索引模板维度访问次数索引', 30.000, 1, 'arius.template.access', 0, 0, '2021-11-30 12:18:00.0', '2022-07-21 15:43:10.0', -1,
        3865, 0, 1, 0, '2', 30.000),
       (14379, 'arius_cat_index_info', 0, '_yyyy-MM-dd', 1, 'cn', 3, -1, '1', 'timestamp', '', '', '',
        'arius_cat_index_info*', '索引列表', 30.000, 1, 'arius_cat_index_info', 0, 0, '2021-11-30 12:18:07.0',
        '2022-07-21 15:43:09.0', -1, 3865, 0, 1, 0, '1,4,2', 30.000),
       (14381, 'arius_gateway_metrics', 0, '_yyyy-MM-dd', 1, 'cn', 30, -1, '1', 'timestamp', '', '', '',
        'arius_gateway_metrics*', 'gateway指标数据', 30.000, 1, 'arius_gateway_metrics', 0, 0, '2021-11-30 12:18:10.0',
        '2022-07-21 15:43:07.0', -1, 3865, 0, 1, 0, '1,4,2', 30.000),
       (14387, 'arius_stats_cluster_info', 0, '_yyyy-MM-dd', 1, 'cn', 180, -1, '1', 'timestamp', '', '', '',
        'arius_stats_cluster_info*', '', 30.000, 1, 'arius_stats_cluster_info', 0, 0, '2021-11-30 12:18:20.0',
        '2022-07-21 15:43:05.0', -1, 3865, 0, 1, 0, '1,4,2', 30.000),
       (14493, 'arius_stats_cluster_task_info', 0, '_yyyy-MM-dd', 1, 'cn', 7, -1, '1', 'timestamp', 'epoch_millis', '',
        '', 'arius_stats_cluster_task_info*', '111', 0.012, 1, 'arius_stats_cluster_task_info', 0, 1,
        '2022-01-14 15:27:52.0', '2022-07-21 15:43:04.0', -1, 3865, 0, 1, 0, '1,4,2', 0.012),
       (24001, 'arius_stats_dashboard_info', 2, '_yyyy-MM-dd', 1, 'cn', 15, -1, '59,312,60', 'timestamp',
        'epoch_millis', '', '', 'arius_stats_dashboard_info*', '', 0.006, 1697, 'arius_stats_dashboard_info', 0, 1,
        '2022-04-18 09:51:19.0', '2022-07-21 15:43:02.0', -1, 3865, 0, 1, 0, '1,4,2', 0.006),
       (14353, 'arius_stats_index_info', 1, '_yyyy-MM-dd', 1, 'cn', 180, -1, '1', 'timestamp', 'epoch_millis', '', '',
        'arius_stats_index_info*', '', 30.000, 1, 'arius_stats_index_info', 0, 1, '2021-11-30 12:16:44.0',
        '2022-07-21 15:43:14.0', -1, 3865, 0, 1, 0, '1,4,2', 30.000),
       (14361, 'arius_stats_node_info', 0, '_yyyy-MM-dd', 1, 'cn', 180, -1, '1', 'timestamp', 'epoch_millis', '', '',
        'arius_stats_node_info*', '', 30.000, 1, 'arius_stats_node_info', 0, 0, '2021-11-30 12:17:03.0',
        '2022-07-21 15:43:12.0', -1, 3865, 0, 1, 0, '1,4,2', 30.000);

# ------------------------------------物理模版关联元数据逻辑模版信息
-- auto-generated definition
create table if not exists index_template_physical_info
(
    id            bigint unsigned auto_increment comment '主键自增'
        primary key,
    logic_id      int(10)      default -1                not null comment '逻辑模板id',
    name          varchar(128) default ''                not null comment '模板名字',
    expression    varchar(128) default ''                not null comment '表达式',
    cluster       varchar(128) default ''                not null comment '集群名字',
    rack          varchar(512) default ''                not null comment 'rack',
    shard         int(10)      default 1                 not null comment 'shard个数',
    shard_routing int(10)      default 1                 not null comment '内核的逻辑shard',
    version       int(10)      default 0                 not null comment '版本',
    role          tinyint      default 1                 not null comment '角色 1master 2slave',
    status        tinyint      default 1                 not null comment '1 常规 -1 索引删除中 -2已删除',
    config        text                                   null comment '配置 json格式',
    create_time   timestamp    default CURRENT_TIMESTAMP null comment '创建时间',
    update_time   timestamp    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    region_id     int(10)      default -1                not null comment '模板关联的regionId'
)
    comment '物理模板信息' charset = utf8;

create index idx_cluster_name_status
    on index_template_physical_info (cluster, name, status);

create index idx_log_id_statud
    on index_template_physical_info (logic_id, status);

create index idx_logic_id
    on index_template_physical_info (logic_id);

create index idx_region_id
    on index_template_physical_info (region_id);

create index idx_status
    on index_template_physical_info (status);
insert into index_template_physical_info (id, logic_id, name, expression, cluster, rack, shard,
                                          shard_routing, version, role, status, config, create_time,
                                          update_time, region_id)
values (6011, 24007, 'arius.appid.template.access', 'arius.appid.template.access', 'logi-elasticsearch-7.6.0',
        'r1,r2,r3,r4,r5', 10, 1, 1, 1, 1, '{"pipeLineRateLimit":1000000}', '2019-12-19 01:05:30.0',
        '2022-07-19 15:40:42.0', 3741),
       (1, 1, 'arius.dsl.analyze.result', 'arius.dsl.analyze.result', 'logi-elasticsearch-7.6.0', 'r2', 1, 4, 0, 1, 1,
        '{"defaultWriterFlags":true,"groupId":"7f500f20-3b0b-41d9-bdfa-9d331b266f4b","pipeLineRateLimit":1000000}',
        '2021-07-24 15:21:49.0', '2022-06-28 15:39:23.0', 3741),
       (5, 5, 'arius.dsl.metrics', 'arius.dsl.metrics*', 'logi-elasticsearch-7.6.0', 'r2', 1, 4, 0, 1, 1,
        '{"defaultWriterFlags":true,"groupId":"ad7cd006-620c-447f-ab7e-30679c6104cf","pipeLineRateLimit":2208000}',
        '2021-07-24 15:21:52.0', '2022-06-28 15:39:23.0', 3741),
       (7, 7, 'arius.dsl.template', 'arius.dsl.template', 'logi-elasticsearch-7.6.0', 'r1', 1, 4, 0, 1, 1,
        '{"defaultWriterFlags":true,"groupId":"9d383c43-4445-4c36-bfed-2003d1eae6c0","pipeLineRateLimit":1000000}',
        '2021-07-24 15:21:53.0', '2022-06-28 15:39:23.0', 3741),
       (9, 9, 'arius.gateway.join', 'arius.gateway.join*', 'logi-elasticsearch-7.6.0', 'r2', 1, 4, 0, 1, 1,
        '{"defaultWriterFlags":true,"groupId":"dee03516-b674-4742-a399-82f83970ca50","pipeLineRateLimit":1104000}',
        '2021-07-24 15:21:55.0', '2022-06-28 15:39:23.0', 3741),
       (21259, 14377, 'arius.template.access', 'arius.template.access', 'logi-elasticsearch-7.6.0', 'r2', 1, 4, 0, 1, 1,
        '{"defaultWriterFlags":true,"groupId":"dee03516-b674-4742-a399-82f83970ca50","pipeLineRateLimit":1000000}',
        '2021-07-24 15:21:55.0', '2022-07-01 02:24:22.0', 3741),
       (21249, 14379, 'arius_cat_index_info', 'arius_cat_index_info*', 'logi-elasticsearch-7.6.0', 'r2', 1, 4, 0, 1, 1,
        '{"defaultWriterFlags":true,"groupId":"dee03516-b674-4742-a399-82f83970ca50","pipeLineRateLimit":1000000}',
        '2021-07-24 15:21:55.0', '2022-07-01 02:22:36.0', 3741),
       (21263, 14381, 'arius_gateway_metrics', 'arius_gateway_metrics*', 'logi-elasticsearch-7.6.0', '', 1, 4, 0, 1, 1,
        '{"defaultWriterFlags":true,"groupId":"dee03516-b674-4742-a399-82f83970ca50","pipeLineRateLimit":1000000}',
        '2021-07-24 15:21:55.0', '2022-07-01 02:24:50.0', 3741),
       (21261, 14387, 'arius_stats_cluster_info', 'arius_stats_cluster_info*', 'logi-elasticsearch-7.6.0', 'r2', 1, 4,
        0, 1, 1,
        '{"defaultWriterFlags":true,"groupId":"dee03516-b674-4742-a399-82f83970ca50","pipeLineRateLimit":1104000}',
        '2021-07-24 15:21:55.0', '2022-06-28 15:39:23.0', 3741),
       (21257, 14493, 'arius_stats_cluster_task_info', 'arius_stats_cluster_task_info*', 'logi-elasticsearch-7.6.0',
        'r2', 1, 4, 0, 1, 1,
        '{"defaultWriterFlags":true,"groupId":"dee03516-b674-4742-a399-82f83970ca50","pipeLineRateLimit":1000000}',
        '2021-07-24 15:21:55.0', '2022-07-01 02:23:58.0', 3741),
       (21245, 24001, 'arius_stats_dashboard_info', 'arius_stats_dashboard_info*', 'logi-elasticsearch-7.6.0',
        'r2,r3,r4,r5,r1', 1, 1, 1, 1, 1,
        '{"defaultWriterFlags":true,"manualPipeLineRateLimit":-1,"pipeLineRateLimit":1000000}', '2022-04-18 09:51:19.0',
        '2022-07-19 15:40:43.0', 3741),
       (21251, 14353, 'arius_stats_index_info', 'arius_stats_index_info*', 'logi-elasticsearch-7.6.0', 'r2', 1, 4, 0, 1,
        1, '{"defaultWriterFlags":true,"groupId":"dee03516-b674-4742-a399-82f83970ca50","pipeLineRateLimit":1000000}',
        '2021-07-24 15:21:55.0', '2022-07-01 02:22:58.0', 3741),
       (6001, 14361, 'arius_stats_node_info', 'arius_stats_node_info*', 'logi-elasticsearch-7.6.0', 'r2', 1, 4, 0, 1, 1,
        '{"defaultWriterFlags":true,"groupId":"8d14658c-e9b6-40a4-9db2-12707698c926","pipeLineRateLimit":1000000}',
        '2021-07-24 15:22:21.0', '2022-07-01 03:00:03.0', 3741);
# -----------------------------逻辑模版关联元数据配置信息
-- auto-generated definition
create table if not exists index_template_config
(
    is_source_separated      tinyint        default 0                 not null comment '是否是索引处分分离的 0 不是 1 是',
    idc_flags                tinyint(1)     default 0                 not null comment 'idc标识',
    adjust_rack_shard_factor decimal(10, 2) default 1.00              not null comment '模板shard的资源消耗因子',
    mapping_improve_enable   tinyint        default 1                 not null comment 'mapping优化开关 1 开 0 关',
    pre_create_flags         tinyint(1)     default 1                 not null comment '预创建标识',
    update_time              timestamp      default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    disable_source_flags     tinyint(1)     default 0                 not null comment '禁用source标识',
    disable_index_rollover   tinyint(1)     default 1                 not null comment '禁用indexRollover功能',
    dynamic_limit_enable     tinyint        default 1                 not null comment '动态限流开关 1 开 0 关',
    logic_id                 int(10)        default -1                not null comment '逻辑模板id',
    create_time              timestamp      default CURRENT_TIMESTAMP not null comment '创建时间',
    shard_num                int            default 1                 not null comment 'shard数量',
    adjust_rack_tps_factor   decimal(10, 2) default 1.00              not null comment '容量规划时，tps的系数 ',
    id                       bigint unsigned auto_increment comment '主键'
        primary key
)
    comment '模板配置信息' charset = utf8;

create index idx_logic_id
    on index_template_config (logic_id);

insert into index_template_config (is_source_separated, idc_flags, adjust_rack_shard_factor,
                                   mapping_improve_enable, pre_create_flags, update_time,
                                   disable_source_flags, disable_index_rollover, dynamic_limit_enable,
                                   logic_id, create_time, shard_num, adjust_rack_tps_factor, id)
values (0, 0, 1.00, 0, 1, '2022-04-18 09:51:19.0', 0, 0, 1, 24007, '2022-04-18 09:51:19.0', 1, 1.00, 25),
       (0, 0, 1.00, 0, 1, '2022-07-15 10:36:39.0', 0, 1, 1, 1, '2021-10-12 22:03:22.0', -1, 1.00, 1),
       (0, 0, 1.00, 0, 1, '2021-10-12 22:03:22.0', 0, 1, 1, 5, '2021-10-12 22:03:22.0', -1, 1.00, 3),
       (0, 0, 1.00, 0, 1, '2021-10-12 22:03:22.0', 0, 1, 1, 7, '2021-10-12 22:03:22.0', -1, 1.00, 5),
       (0, 0, 1.00, 0, 1, '2021-10-12 22:03:22.0', 0, 1, 1, 9, '2021-10-12 22:03:22.0', -1, 1.00, 7),
       (0, 0, 1.00, 0, 1, '2021-10-14 22:01:36.0', 0, 1, 1, 14377, '2021-10-14 22:01:36.0', -1, 1.00, 13),
       (0, 0, 1.00, 0, 1, '2021-10-14 22:01:38.0', 0, 1, 1, 14379, '2021-10-14 22:01:38.0', -1, 1.00, 15),
       (0, 0, 1.00, 0, 1, '2021-10-14 22:01:44.0', 0, 1, 1, 14381, '2021-10-14 22:01:44.0', -1, 1.00, 17),
       (0, 0, 1.00, 0, 1, '2021-10-14 22:31:55.0', 0, 1, 1, 14387, '2021-10-14 22:31:55.0', -1, 1.00, 19),
       (0, 0, 1.00, 0, 1, '2021-10-14 22:31:55.0', 0, 1, 1, 14493, '2021-10-14 22:31:55.0', -1, 1.00, 21),
       (0, 0, 1.00, 0, 1, '2022-03-08 13:11:17.0', 0, 0, 1, 24001, '2022-03-08 13:11:17.0', -1, 1.00, 23),
       (0, 0, 1.00, 0, 1, '2021-10-14 21:59:35.0', 0, 1, 1, 14353, '2021-10-14 21:59:35.0', -1, 1.00, 9),
       (0, 0, 1.00, 0, 1, '2021-10-14 21:59:35.0', 0, 1, 1, 14361, '2021-10-14 21:59:35.0', -1, 1.00, 11);