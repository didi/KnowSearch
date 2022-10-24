#字段新增
alter table es_manager_test3.es_cluster_phy_info
    add platform_type varchar(100) default '' not null comment 'IaaS平台类型';

alter table es_manager_test3.es_cluster_phy_info
    add resource_type tinyint default -1 not null comment '集群资源类型，1-共享资源，2-独立资源，3-独享资源';

alter table es_manager_test3.es_cluster_phy_info
    add gateway_url varchar(200) default '' not null comment '集群gateway地址';


# 字段新增
alter table es_manager_test3.es_cluster_role_host_info
    modify rack varchar(30) default '' null comment '节点rack信息';
alter table es_manager_test3.es_cluster_role_host_info
    add region_id bigint default -1 not null comment '节点所属regionId';
alter table es_manager_test3.es_cluster_role_host_info
    add attributes text null comment 'es节点attributes信息 , 逗号分隔';


# es_package->es_package
alter table es_manager_test3.es_package
    modify `desc` varchar(384) default '' null comment '备注';
# es_work_order_task_detail->es_work_order_task_detail
alter table es_manager_test3.es_work_order_task_detail
    add constraint uniq_work_order_task_id_role_hostname_delete_flag
        unique (work_order_task_id, role, hostname, delete_flag);

# index_template_alias-> index_template_alias

# index_template_config->index_template_config
#字段内容变更


#表字段修改
alter table es_manager_test3.index_template_info
    add open_srv varchar(255) null comment '已开启的模板服务';

alter table es_manager_test3.index_template_info
    add disk_size decimal(10, 3) default -1.000 null comment '可用磁盘容量';



# 字段修改

# 新增字段
alter table es_manager_test3.index_template_physical_info
    add region_id int(10) default -1 not null comment '模板关联的regionId';

# index_template_type->index_template_type


# 字段新增
alter table es_manager_test3.project_arius_resource_logic
    add data_node_spec varchar(20) default '' not null comment '节点规格';

alter table es_manager_test3.project_arius_resource_logic
    add disk_total bigint(50) default 0 not null comment '集群磁盘总量 单位byte';

alter table es_manager_test3.project_arius_resource_logic
    add disk_usage bigint(50) default 0 not null comment '集群磁盘使用量 单位byte';

alter table es_manager_test3.project_arius_resource_logic
    add disk_usage_percent decimal(10, 5) default 0 not null comment '集群磁盘空闲率 单位 0 ~1';

alter table es_manager_test3.project_arius_resource_logic
    add es_cluster_version varchar(20) default '' not null comment 'es集群版本';

alter table es_manager_test3.project_arius_resource_logic
    add node_num int(10) default 0 not null comment '节点个数';



alter table es_manager_test3.es_cluster_region
    add name varchar(100) default '' not null comment 'region名称';

alter table es_manager_test3.es_cluster_region
    add config varchar(1024) default '' null comment 'region配置项';