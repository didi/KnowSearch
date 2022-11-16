# gateway表修改
alter table gateway_cluster_info
    add ecm_access tinyint(2) default 0 null comment '是否接入gateway';

alter table gateway_cluster_info
    add memo varchar(255) null comment '备注';

alter table gateway_cluster_info
    add component_id int default -1 null comment 'ecm关联组建id';
alter table gateway_cluster_info
    add version varchar(30) default '' null comment 'gateway版本号';
alter table gateway_cluster_info
    add health int default -1 null comment '集群健康';
alter table gateway_cluster_info
    add proxy_address varchar(255) default '' null comment '代理地址';
alter table gateway_cluster_info
    add data_center varchar(255) default '' null comment ' 数据中心 ';

# admin表修改
alter table es_cluster_region
    add divide_attribute_key varchar(100) default '' null comment 'region划分方式，为空是根据节点名称划分';

alter table index_template_info
    add priority_level tinyint(4) default 0 null comment '恢复优先级';

ALTER TABLE gateway_cluster_node_info
    ADD node_name VARCHAR(50) DEFAULT '' NULL COMMENT '节点名称';

-- auto-generated definition
create table fast_index_task_info
(
    id                        int auto_increment
        primary key,
    task_id                   int                                   not null comment 'arius_op_task表中的ID',
    task_type                 int         default 2                 not null comment '子任务类型：1.template 2.index',
    template_id               int                                   null comment '模版id',
    template_name             varchar(255)                          null comment '模版名称',
    index_name                varchar(255)                          not null comment '索引名称',
    index_types               varchar(255)                          null comment '索引的type，多个用'',''隔开',
    target_index_name         varchar(255)                          not null comment '目标索引名称',
    mappings                  text                                  null,
    settings                  text                                  null,
    fast_dump_task_id         varchar(40)                           null comment 'fastDump内核任务Id，每个索引子任务拥有一个，重试任务时，重新设置该id',
    task_status               int(2)      default -1                not null comment '状态：-1.未提交 0.等待执行 1.执行中 2.执行成功 3.执行失败 4.已取消',
    read_file_rate_limit      decimal                               null comment '任务读取限流速率',
    total_document_num        decimal(20) default 0                 null comment '任务总文档数',
    shard_num                 decimal(5)                            null comment 'shard数量',
    succ_document_num         decimal(20)                           null comment '成功迁移的文档数',
    succ_shard_num            decimal(5)                            null comment '成功shard数',
    failed_document_num       decimal(20) default 0                 null comment '失败文档数',
    task_submit_result        text                                  null comment '任务提交内核返回结果',
    create_time               timestamp   default CURRENT_TIMESTAMP null comment '任务创建时间',
    update_time               timestamp   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '任务更新时间',
    task_start_time           timestamp                             null comment '任务开始时间',
    task_end_time             timestamp                             null comment '任务结束时间',
    scheduled_task_start_time timestamp                             null comment '计划任务开始时间',
    last_response             text                                  null,
    task_cost_time            decimal                               null
)
    comment 'fastDump任务状态';

create index idx_
    on fast_index_task_info (task_status);

create index idx_scheduled_task_start_time
    on fast_index_task_info (scheduled_task_start_time);

create index idx_task_id
    on fast_index_task_info (task_id);