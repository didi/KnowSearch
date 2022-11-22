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

ALTER TABLE index_template_info
    ADD priority_level TINYINT(4) DEFAULT 0 NULL COMMENT '恢复优先级';

ALTER TABLE gateway_cluster_node_info
    ADD node_name VARCHAR(50) DEFAULT '' NULL COMMENT ' 节点名称 ';


# es_cluster_phy_info 表修改
ALTER TABLE es_cluster_phy_info
    ADD ecm_access TINYINT(2) DEFAULT 0 NULL COMMENT '是否接入 gateway';

ALTER TABLE es_cluster_phy_info
    ADD component_id INT DEFAULT -1 NULL COMMENT 'ecm 关联组建 id';


ALTER TABLE es_cluster_phy_info
    ADD proxy_address VARCHAR(255) DEFAULT '' NULL COMMENT ' 代理地址 ';

ALTER TABLE es_cluster_phy_info
    ADD gateway_ids VARCHAR(1024) DEFAULT '' NULL COMMENT ' gateway ids,逗号分割 ';

#创建集群插件信息
CREATE TABLE plugin_info
(
    id           BIGINT UNSIGNED AUTO_INCREMENT COMMENT 'id 主键自增'
        PRIMARY KEY,
    name  VARCHAR(50)   DEFAULT ''                NOT NULL COMMENT ' 插件名 ',
    cluster_id   VARCHAR(100)  DEFAULT ''                NOT NULL COMMENT ' 集群 id',
    version      VARCHAR(50)   DEFAULT ''                NOT NULL COMMENT ' 插件版本 ',
    memo         VARCHAR(1024) DEFAULT ''                NOT NULL COMMENT ' 插件存储地址 ',
    component_id INT                                     NOT NULL COMMENT '组建 ID',
    plugin_type  INT                                     NOT NULL COMMENT '插件类型（1. 平台;2. 引擎 )',
    cluster_type INT                                     NOT NULL COMMENT '集群类型 (1.es;2.gateway)',
    create_time          TIMESTAMP     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_time          TIMESTAMP     DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
)
    COMMENT '插件信息管理' CHARSET = utf8;

#es_cluster_role_host_info新增字段
ALTER TABLE es_cluster_role_host_info
    ADD component_host_id BIGINT DEFAULT -1 NULL COMMENT '关联组建id';
-- auto-generated definition
CREATE TABLE logi_op_component
(
    id                         INT(11) UNSIGNED AUTO_INCREMENT COMMENT '组件自增id'
        PRIMARY KEY,
    status                     INT          NOT NULL COMMENT '状态(0 green,1 yellow,2 red,3 unKnow)',
    contain_component_ids      VARCHAR(200) NULL COMMENT '包含组件id列表',
    name                       VARCHAR(100) NULL COMMENT '组件名',
    package_id                 INT          NULL COMMENT '关联安装包id',
    depend_config_component_id INT          NULL COMMENT '配置依赖组件',
    username                   VARCHAR(50)  NULL COMMENT '用户名',
    password                   VARCHAR(50)  NULL COMMENT '密码',
    is_open_tsl                TINYINT      NULL COMMENT '是否开启tsl',
    create_time                TIMESTAMP    NULL COMMENT '创建时间',
    update_time                TIMESTAMP    NULL COMMENT '更新时间',
    is_deleted                 INT          NULL COMMENT '0未删除1删除'
)
    ;
-- auto-generated definition
CREATE TABLE logi_op_component_group_config
(
    id                       INT(11) UNSIGNED AUTO_INCREMENT COMMENT '自增id'
        PRIMARY KEY,
    component_id             INT           NULL COMMENT '关联组件id',
    group_name               VARCHAR(50)   NULL COMMENT '分组名',
    system_config            VARCHAR(5000) NULL COMMENT '系统配置',
    running_config           VARCHAR(5000) NULL COMMENT '运行时配置',
    file_config              VARCHAR(5000) NULL COMMENT '文件配置',
    install_directory_config VARCHAR(200)  NULL COMMENT '安装目录',
    process_num_config       VARCHAR(200)  NULL COMMENT '进程数',
    hosts                    VARCHAR(200)  NULL COMMENT '分组下的ip',
    version                  VARCHAR(50)   NULL COMMENT '版本',
    create_time              TIMESTAMP     NULL COMMENT '创建时间',
    update_time              TIMESTAMP     NULL COMMENT '更新时间'
);
-- auto-generated definition
CREATE TABLE logi_op_component_host
(
    host         VARCHAR(11) DEFAULT '' NOT NULL COMMENT '主机',
    component_id INT                    NOT NULL COMMENT '关联组件id',
    status       INT                    NULL COMMENT '状态（在线或离线）',
    group_name   VARCHAR(11)            NULL COMMENT '分组名',
    process_num  INT                    NULL COMMENT '进程数',
    is_deleted   INT                    NULL COMMENT '是否卸载',
    create_time  TIMESTAMP              NULL COMMENT '创建时间',
    update_time  TIMESTAMP              NULL COMMENT '更新时间'
);

-- auto-generated definition
CREATE TABLE logi_op_package
(
    id           BIGINT AUTO_INCREMENT COMMENT '软件id'
        PRIMARY KEY,
    name         VARCHAR(255) NOT NULL COMMENT '软件名称',
    url          VARCHAR(255) NULL COMMENT '文件地址',
    version      VARCHAR(255) NOT NULL COMMENT '软件版本',
    `describe`   VARCHAR(255) NULL COMMENT '描述',
    type         TINYINT      NULL COMMENT '依赖类型,0是配置依赖，1是配置独立',
    script_id    BIGINT       NOT NULL COMMENT '脚本id',
    create_time  TIMESTAMP    NULL COMMENT '创建时间',
    update_time  TIMESTAMP    NULL COMMENT '更新时间',
    creator      VARCHAR(255) NULL COMMENT '创建者',
    package_type TINYINT      NULL COMMENT '软件包类型,1-es安装包、2-gateway安装包、3-es引擎插件、4-gateway引擎插件、5-es平台插件、6-gateway平台插件'
);
-- auto-generated definition
CREATE TABLE logi_op_package_group_config
(
    id             BIGINT AUTO_INCREMENT COMMENT '配置组id'
        PRIMARY KEY,
    group_name     VARCHAR(5000) DEFAULT '' NOT NULL COMMENT '配置组名称',
    system_config  VARCHAR(5000)            NULL COMMENT '系统配置',
    running_config VARCHAR(5000)            NULL COMMENT '运行配置',
    file_config    VARCHAR(255)             NULL COMMENT '文件配置',
    package_id     BIGINT                   NULL COMMENT '软件包id',
    create_time    TIMESTAMP                NULL COMMENT '创建时间',
    update_time    TIMESTAMP                NULL COMMENT '更新时间'
);
-- auto-generated definition
CREATE TABLE logi_op_script
(
    id          BIGINT AUTO_INCREMENT COMMENT '脚本id'
        PRIMARY KEY,
    name        VARCHAR(255) NOT NULL COMMENT '脚本名称',
    template_id VARCHAR(255) NULL COMMENT 'Zeus模板id',
    content_url VARCHAR(255) NULL COMMENT '文件地址',
    `describe`  VARCHAR(255) NULL COMMENT '描述',
    create_time TIMESTAMP    NULL COMMENT '创建时间',
    update_time TIMESTAMP    NULL COMMENT '更新时间'
);
-- auto-generated definition
CREATE TABLE logi_op_task
(
    id          INT(11) UNSIGNED AUTO_INCREMENT COMMENT '任务id自增'
        PRIMARY KEY,
    status      INT           NULL COMMENT '任务状态',
    `describe`  VARCHAR(200)  NULL COMMENT '描述',
    type        INT           NULL COMMENT '任务类型',
    is_finish   INT           NULL COMMENT '是否结束',
    content     VARCHAR(5000) NULL COMMENT '任务内容',
    create_time TIMESTAMP     NULL COMMENT '创建时间',
    update_time TIMESTAMP     NULL COMMENT '更新时间'
);
-- auto-generated definition
CREATE TABLE logi_op_task_detail
(
    id              INT(11) UNSIGNED NOT NULL COMMENT '关联任务id',
    execute_task_id INT              NULL COMMENT 'zeus的执行任务id',
    status          INT              NULL COMMENT '状态',
    host            VARCHAR(50)      NULL COMMENT '主机',
    group_name      VARCHAR(100)     NULL COMMENT '关联分组名',
    process_num     INT              NULL COMMENT '进程数',
    create_time     TIMESTAMP        NULL COMMENT '创建时间',
    update_time     TIMESTAMP        NULL COMMENT '更新时间'
);

#arius_config_info修改业务类型对应的配置值
UPDATE arius_config_info SET `value`  = '[{"code":0,"desc":"系统日志","label":"system"},{"code":1,"desc":"日志数据","label":"log"},{"code":2,"desc":"用户上报数据","label":"olap"},{"code":3,"desc":"RDS数据","label":"binlog"},{"code":4,"desc":"离线导入数据","label":"offline"}]'  WHERE id = 1671;
