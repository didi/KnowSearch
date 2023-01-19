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
ALTER TABLE gateway_cluster_node_info
    ADD machine_spec VARCHAR(255) DEFAULT '' NULL COMMENT '机器规格';
ALTER TABLE gateway_cluster_node_info
    ADD cpu_usage  DOUBLE DEFAULT 0.0 NULL COMMENT 'cpu使用率';
ALTER TABLE gateway_cluster_node_info
    ADD http_connection_num BIGINT DEFAULT 0 NULL COMMENT 'http连接数';


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
    name         VARCHAR(500)   DEFAULT ''                NOT NULL COMMENT ' 插件名 ',
    cluster_id   VARCHAR(100)  DEFAULT ''                NOT NULL COMMENT ' 集群 id',
    version      VARCHAR(50)   DEFAULT ''                NOT NULL COMMENT ' 插件版本 ',
    memo         VARCHAR(1024) DEFAULT ''                NULL COMMENT ' 插件存储地址 ',
    component_id INT                                     NOT NULL COMMENT '组建 ID',
    plugin_type  INT                                     NOT NULL COMMENT '插件类型（1. 平台;2. 引擎 )',
    cluster_type INT                                     NOT NULL COMMENT '集群类型 (1.es;2.gateway)',
    create_time  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_time  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
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
    update_time              TIMESTAMP     NULL COMMENT '更新时间',
    machine_spec             VARCHAR(500)  NULL COMMENT '机器规格'
);
-- auto-generated definition
-- auto-generated definition
CREATE TABLE logi_op_component_host
(
    host         VARCHAR(11)  DEFAULT '' NOT NULL COMMENT '主机',
    component_id INT                     NOT NULL COMMENT '关联组件id',
    status       INT                     NULL COMMENT '状态（在线或离线）',
    group_name   VARCHAR(50)             NULL COMMENT '分组名',
    process_num  INT                     NULL COMMENT '进程数',
    is_deleted   INT                     NULL COMMENT '是否卸载',
    create_time  TIMESTAMP               NULL COMMENT '创建时间',
    update_time  TIMESTAMP               NULL COMMENT '更新时间',
    machine_spec VARCHAR(255) DEFAULT '' NULL COMMENT '机器规格',
    CONSTRAINT logi_op_component_host_pk
        UNIQUE (host, component_id, group_name)
);

-- auto-generated definition
CREATE TABLE logi_op_package
(
    id           INT(11) UNSIGNED AUTO_INCREMENT COMMENT '软件id'
        PRIMARY KEY,
    name         VARCHAR(255) NOT NULL COMMENT '软件名称',
    url          VARCHAR(255) NULL COMMENT '文件地址',
    version      VARCHAR(255) NOT NULL COMMENT '软件版本',
    `describe`   VARCHAR(255) NULL COMMENT '描述',
    type         TINYINT      NULL COMMENT '依赖类型,0是配置依赖，1是配置独立',
    script_id    INT       NOT NULL COMMENT '脚本id',
    create_time  TIMESTAMP    NULL COMMENT '创建时间',
    update_time  TIMESTAMP    NULL COMMENT '更新时间',
    creator      VARCHAR(255) NULL COMMENT '创建者',
    package_type TINYINT      NULL COMMENT '软件包类型,1-es安装包、2-gateway安装包、3-es引擎插件、4-gateway引擎插件、5-es平台插件、6-gateway平台插件'
);
-- auto-generated definition
CREATE TABLE logi_op_package_group_config
(
    id             INT(11) UNSIGNED AUTO_INCREMENT COMMENT '配置组id'
        PRIMARY KEY,
    group_name     VARCHAR(50) DEFAULT '' NOT NULL COMMENT '配置组名称',
    system_config  VARCHAR(5000)            NULL COMMENT '系统配置',
    running_config VARCHAR(5000)            NULL COMMENT '运行配置',
    file_config    VARCHAR(5000)             NULL COMMENT '文件配置',
    package_id     INT                   NULL COMMENT '软件包id',
    create_time    TIMESTAMP                NULL COMMENT '创建时间',
    update_time    TIMESTAMP                NULL COMMENT '更新时间'
);
-- auto-generated definition
CREATE TABLE logi_op_script
(
    id          INT(11) UNSIGNED AUTO_INCREMENT COMMENT '脚本id'
        PRIMARY KEY,
    name        VARCHAR(255) NOT NULL COMMENT '脚本名称',
    template_id VARCHAR(255) NULL COMMENT 'Zeus模板id',
    content_url VARCHAR(255) NULL COMMENT '文件地址',
    `describe`  VARCHAR(255) NULL COMMENT '描述',
    create_time TIMESTAMP    NULL COMMENT '创建时间',
    update_time TIMESTAMP    NULL COMMENT '更新时间',
    creator     VARCHAR(255) NULL COMMENT '创建者'
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

#0.3.2新增权限点
INSERT INTO `logi_security_permission` (`id`, `permission_name`, `parent_id`, `leaf`, `level`, `description`, `create_time`, `update_time`, `is_delete`, `app_name`) VALUES (1883,'脚本中心', 0, 0, 1, '脚本中心', '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search');
INSERT INTO `logi_security_permission` (`id`, `permission_name`, `parent_id`, `leaf`, `level`, `description`, `create_time`, `update_time`, `is_delete`, `app_name`) VALUES (1885,'软件中心', 0, 0, 1, '软件中心', '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search');

INSERT INTO `logi_security_permission` (`id`, `permission_name`, `parent_id`, `leaf`, `level`, `description`, `create_time`, `update_time`, `is_delete`, `app_name`)  VALUES (1887,'插件安装', 1593, 1, 2, '插件安装', '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search');
INSERT INTO `logi_security_permission` (`id`, `permission_name`, `parent_id`, `leaf`, `level`, `description`, `create_time`, `update_time`, `is_delete`, `app_name`) VALUES (1889,'批量操作', 1593, 1, 2, '批量操作', '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search');
INSERT INTO `logi_security_permission` (`id`, `permission_name`, `parent_id`, `leaf`, `level`, `description`, `create_time`, `update_time`, `is_delete`, `app_name`) VALUES (1891,'数据迁移', 1593, 1, 2, '数据迁移', '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search');

INSERT INTO `logi_security_permission` (`id`, `permission_name`, `parent_id`, `leaf`, `level`, `description`, `create_time`, `update_time`, `is_delete`, `app_name`) VALUES (1893,'新建gateway', 1599, 1, 2, '新建gateway', '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search');
INSERT INTO `logi_security_permission` (`id`, `permission_name`, `parent_id`, `leaf`, `level`, `description`, `create_time`, `update_time`, `is_delete`, `app_name`) VALUES (1895,'升级', 1599, 1, 2, '升级', '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search');
INSERT INTO `logi_security_permission` (`id`, `permission_name`, `parent_id`, `leaf`, `level`, `description`, `create_time`, `update_time`, `is_delete`, `app_name`) VALUES (1897,'重启', 1599, 1, 2, '重启', '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search');
INSERT INTO `logi_security_permission` (`id`, `permission_name`, `parent_id`, `leaf`, `level`, `description`, `create_time`, `update_time`, `is_delete`, `app_name`) VALUES (1899,'扩缩容', 1599, 1, 2, '扩缩容', '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search');
INSERT INTO `logi_security_permission` (`id`, `permission_name`, `parent_id`, `leaf`, `level`, `description`, `create_time`, `update_time`, `is_delete`, `app_name`) VALUES (1901,'回滚', 1599, 1, 2, '回滚', '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search');

INSERT INTO `logi_security_permission` (`id`, `permission_name`, `parent_id`, `leaf`, `level`, `description`, `create_time`, `update_time`, `is_delete`, `app_name`) VALUES (1903,'开关：异步Translog', 1603, 1, 2, '开关：异步Translog', '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search');
INSERT INTO `logi_security_permission` (`id`, `permission_name`, `parent_id`, `leaf`, `level`, `description`, `create_time`, `update_time`, `is_delete`, `app_name`) VALUES (1905,'恢复优先级', 1603, 1, 2, '恢复优先级', '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search');

INSERT INTO `logi_security_permission` (`id`, `permission_name`, `parent_id`, `leaf`, `level`, `description`, `create_time`, `update_time`, `is_delete`, `app_name`) VALUES (1907,'异步Translog', 1607, 1, 2, '异步Translog', '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search');
INSERT INTO `logi_security_permission` (`id`, `permission_name`, `parent_id`, `leaf`, `level`, `description`, `create_time`, `update_time`, `is_delete`, `app_name`) VALUES (1909,'恢复优先级', 1607, 1, 2, '恢复优先级', '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search');

INSERT INTO `logi_security_permission` (`id`, `permission_name`, `parent_id`, `leaf`, `level`, `description`, `create_time`, `update_time`, `is_delete`, `app_name`) VALUES (1911,'查看查询模板列表及详情', 1759, 1, 2, '查看查询模板列表及详情', '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search');

INSERT INTO `logi_security_permission` (`id`, `permission_name`, `parent_id`, `leaf`, `level`, `description`, `create_time`, `update_time`, `is_delete`, `app_name`) VALUES (1913,'回滚（配置变更、升级任务特有）', 1621, 1, 2, '回滚（配置变更、升级任务特有）', '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search');
INSERT INTO `logi_security_permission` (`id`, `permission_name`, `parent_id`, `leaf`, `level`, `description`, `create_time`, `update_time`, `is_delete`, `app_name`) VALUES (1915,'修改限流值（数据迁移任务）', 1621, 1, 2, '修改限流值（数据迁移任务）', '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search');

INSERT INTO `logi_security_permission` (`id`, `permission_name`, `parent_id`, `leaf`, `level`, `description`, `create_time`, `update_time`, `is_delete`, `app_name`) VALUES (1917,'查看脚本中心列表', 1883, 1, 2, '查看脚本中心列表', '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search');
INSERT INTO `logi_security_permission` (`id`, `permission_name`, `parent_id`, `leaf`, `level`, `description`, `create_time`, `update_time`, `is_delete`, `app_name`) VALUES (1919,'新建脚本', 1883, 1, 2, '新建脚本', '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search');
INSERT INTO `logi_security_permission` (`id`, `permission_name`, `parent_id`, `leaf`, `level`, `description`, `create_time`, `update_time`, `is_delete`, `app_name`) VALUES (1921,'编辑', 1883, 1, 2, '编辑', '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search');
INSERT INTO `logi_security_permission` (`id`, `permission_name`, `parent_id`, `leaf`, `level`, `description`, `create_time`, `update_time`, `is_delete`, `app_name`) VALUES (1923,'删除', 1883, 1, 2, '删除', '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search');

INSERT INTO `logi_security_permission` (`id`, `permission_name`, `parent_id`, `leaf`, `level`, `description`, `create_time`, `update_time`, `is_delete`, `app_name`) VALUES (1925,'查看软件中心列表', 1885, 1, 2, '查看软件中心列表', '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search');
INSERT INTO `logi_security_permission` (`id`, `permission_name`, `parent_id`, `leaf`, `level`, `description`, `create_time`, `update_time`, `is_delete`, `app_name`) VALUES (1927,'新建软件', 1885, 1, 2, '新建软件', '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search');
INSERT INTO `logi_security_permission` (`id`, `permission_name`, `parent_id`, `leaf`, `level`, `description`, `create_time`, `update_time`, `is_delete`, `app_name`) VALUES (1929,'编辑', 1885, 1, 2, '编辑', '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search');
INSERT INTO `logi_security_permission` (`id`, `permission_name`, `parent_id`, `leaf`, `level`, `description`, `create_time`, `update_time`, `is_delete`, `app_name`) VALUES (1931,'删除', 1885, 1, 2, '删除', '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search');

INSERT INTO `logi_security_permission` (`id`, `permission_name`, `parent_id`, `leaf`, `level`, `description`, `create_time`, `update_time`, `is_delete`, `app_name`) VALUES (1933,'复制', 1623, 1, 2, '复制', '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search');
INSERT INTO `logi_security_permission` (`id`, `permission_name`, `parent_id`, `leaf`, `level`, `description`, `create_time`, `update_time`, `is_delete`, `app_name`) VALUES (1935,'编辑', 1623, 1, 2, '编辑', '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search');


UPDATE logi_security_permission SET update_time = '2022-12-21 15:10:32', is_delete = 1 WHERE id = 1649;

UPDATE logi_security_permission SET update_time = '2022-12-21 15:10:32', is_delete = 1 WHERE id = 1597;
UPDATE logi_security_permission SET update_time = '2022-12-21 15:10:32', is_delete = 1 WHERE id = 1673;
UPDATE logi_security_permission SET update_time = '2022-12-21 15:10:32', is_delete = 1 WHERE id = 1675;
UPDATE logi_security_permission SET update_time = '2022-12-21 15:10:32', is_delete = 1 WHERE id = 1677;
UPDATE logi_security_permission SET update_time = '2022-12-21 15:10:32', is_delete = 1 WHERE id = 1679;

UPDATE logi_security_permission SET update_time = '2022-12-21 15:10:32', is_delete = 1 WHERE id = 1879;
UPDATE logi_security_permission SET update_time = '2022-12-21 15:10:32', is_delete = 1 WHERE id = 1855;

UPDATE logi_security_role_permission SET update_time = '2022-12-21 15:10:32', is_delete = 1 WHERE permission_id = 1649;
UPDATE logi_security_role_permission SET update_time = '2022-12-21 15:10:32', is_delete = 1 WHERE permission_id = 1597;
UPDATE logi_security_role_permission SET update_time = '2022-12-21 15:10:32', is_delete = 1 WHERE permission_id = 1673;
UPDATE logi_security_role_permission SET update_time = '2022-12-21 15:10:32', is_delete = 1 WHERE permission_id = 1675;
UPDATE logi_security_role_permission SET update_time = '2022-12-21 15:10:32', is_delete = 1 WHERE permission_id = 1677;
UPDATE logi_security_role_permission SET update_time = '2022-12-21 15:10:32', is_delete = 1 WHERE permission_id = 1679;

UPDATE logi_security_role_permission SET update_time = '2022-12-21 15:10:32', is_delete = 1 WHERE permission_id = 1879;
UPDATE logi_security_role_permission SET update_time = '2022-12-21 15:10:32', is_delete = 1 WHERE permission_id = 1855;


insert into logi_security_role_permission (id, role_id, permission_id, create_time, update_time, is_delete, app_name)
values (5601, 1, 1883, '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search'),
       (5603, 1, 1885, '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search'),
       (5605, 1, 1887, '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search'),
       (5607, 1, 1889, '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search'),
       (5609, 1, 1891, '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search'),
       (5611, 1, 1893, '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search'),
       (5613, 1, 1895, '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search'),
       (5615, 1, 1897, '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search'),
       (5617, 1, 1899, '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search'),
       (5619, 1, 1901, '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search'),
       (5621, 1, 1903, '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search'),
       (5623, 1, 1905, '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search'),
       (5625, 1, 1907, '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search'),
       (5627, 1, 1909, '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search'),
       (5629, 1, 1911, '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search'),
       (5631, 1, 1913, '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search'),
       (5633, 1, 1915, '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search'),
       (5635, 1, 1917, '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search'),
       (5637, 1, 1919, '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search'),
       (5639, 1, 1921, '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search'),
       (5641, 1, 1923, '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search'),
       (5643, 1, 1925, '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search'),
       (5645, 1, 1927, '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search'),
       (5647, 1, 1929, '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search'),
       (5649, 1, 1931, '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search'),
       (5651, 1, 1933, '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search'),
       (5653, 1, 1935, '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search'),
       (5655, 2, 1907, '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search'),
       (5657, 2, 1909, '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search'),
       (5659, 2, 1911, '2022-12-21 15:08:22', '2022-12-21 15:10:32', 0, 'know_search');




-- 新增平台配置初始化数据
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1686, 'arius.common.group', 'cluster.region.unsupported_divide_type', 'xpack.installed,zen1,ml.machine_memory,ml.max_open_jobs,ml.enabled', 1, -1, 1, '设置平台不支持的region划分方式', '2022-11-19 10:45:34', '2022-11-19 10:45:34', '2022-11-19 10:45:34');


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

# 权限点新增
INSERT INTO logi_security_permission ( id, permission_name, parent_id, leaf, level, description
                                     , is_delete, app_name)
VALUES (1882, '插件安装', 1593, 1, 2, '插件安装', 0, 'know_search');
INSERT INTO logi_security_permission (id, permission_name, parent_id, leaf, level, description,
                                      is_delete, app_name)
VALUES (1883, '数据迁移', 1593, 1, 2, '数据迁移', 0, 'know_search');
INSERT INTO logi_security_role_permission (role_id, permission_id, is_delete, app_name)
VALUES (1, 1882, 0, 'know_search');
INSERT INTO logi_security_role_permission (role_id, permission_id, is_delete, app_name)
VALUES (1, 1883, 0, 'know_search');
###gatea权限点新增
SELECT id
INTO @parent_id
FROM logi_security_permission
WHERE permission_name = 'Gateway管理';
INSERT INTO logi_security_permission (permission_name, parent_id, leaf, level,
                                      description, is_delete,
                                      app_name)
    VALUE ('新建Gateway', @parent_id, 1, 2, '新建Gateway', 0, 'know_search');

INSERT INTO logi_security_permission (permission_name, parent_id, leaf, level,
                                      description, is_delete,
                                      app_name)
    VALUE ('升级', @parent_id, 1, 2, '升级', 0, 'know_search');
INSERT INTO logi_security_permission (permission_name, parent_id, leaf, level,
                                      description, is_delete,
                                      app_name)
    VALUE ('重启', @parent_id, 1, 2, '重启', 0, 'know_search');
INSERT INTO logi_security_permission (permission_name, parent_id, leaf, level,
                                      description, is_delete,
                                      app_name)
    VALUE ('扩缩容', @parent_id, 1, 2, '扩缩容', 0, 'know_search');
INSERT INTO logi_security_permission (permission_name, parent_id, leaf, level,
                                      description, is_delete,
                                      app_name)
    VALUE ('回滚', @parent_id, 1, 2, '回滚', 0, 'know_search');
INSERT INTO logi_security_role_permission (role_id, permission_id, is_delete, app_name)
SELECT 1, id, 0, 'know_search'
FROM logi_security_permission
WHERE parent_id = @parent_id
  AND permission_name IN ('新建Gateway', '升级', '重启', '扩缩容', '回滚');

rename table logi_security_config to kf_security_config;
rename table logi_security_dept to kf_security_dept;
rename table logi_security_message to kf_security_message;
rename table logi_security_oplog to kf_security_oplog;
rename table logi_security_oplog_extra to kf_security_oplog_extra;
rename table logi_security_permission to kf_security_permission;
rename table logi_security_project to kf_security_project;
rename table logi_security_resource_type to kf_security_resource_type;
rename table logi_security_role to kf_security_role;
rename table logi_security_role_permission to kf_security_role_permission;
rename table logi_security_user to kf_security_user;
rename table logi_security_user_project to kf_security_user_project;
rename table logi_security_user_resource to kf_security_user_resource;
rename table logi_security_user_role to kf_security_user_role;

alter table logi_task
    add node_name_white_list_str varchar(3000) default '' not null comment '执行节点名对应白名单集';

alter table logi_worker
    add node_name varchar(100) default '' not null comment 'node 名';

# grafana 新增菜单sql
INSERT INTO `kf_security_permission` (`permission_name`, `parent_id`, `leaf`, `level`, `description`, `create_time`, `update_time`, `is_delete`, `app_name`) VALUES ('Grafana', 0, 0, 1, 'Grafana', '2022-05-24 18:08:26', '2022-12-22 15:16:17', 0, 'know_search');
INSERT INTO `kf_security_role_permission` (`role_id`, `permission_id`, `create_time`, `update_time`, `is_delete`, `app_name`) VALUES (1, (select id from kf_security_permission ksp where ksp.permission_name='Grafana' and ksp.app_name='know_search' and ksp.is_delete=0 ), '2022-06-01 21:19:42', '2022-08-25 10:31:42', 0, 'know_search');
INSERT INTO `kf_security_permission` (`permission_name`, `parent_id`, `leaf`, `level`, `description`, `create_time`, `update_time`, `is_delete`, `app_name`) VALUES ('查看Grafana', (select id from kf_security_permission ksp where ksp.permission_name='Grafana' and ksp.app_name='know_search' and ksp.is_delete=0 ), 1, 2, '查看Grafana', '2022-05-24 18:08:26', '2022-12-22 15:16:17', 0, 'know_search');
INSERT INTO `kf_security_role_permission` (`role_id`, `permission_id`, `create_time`, `update_time`, `is_delete`, `app_name`) VALUES (1, (select id from kf_security_permission ksp where ksp.permission_name='查看Grafana' and ksp.app_name='know_search' and ksp.is_delete=0 ), '2022-06-01 21:19:42', '2022-08-25 10:31:42', 0, 'know_search');

ALTER TABLE operate_record_info
    ADD operate_project_name VARCHAR(255) DEFAULT '' NULL COMMENT '用户操作的应用，即资源所属应用';
-- 新增普通用户侧的操作记录
INSERT INTO `kf_security_role_permission`
(`id`, `role_id`, `permission_id`, `create_time`, `update_time`, `is_delete`, `app_name`)
VALUES ('5661', '2', '1635', '2022-12-21 15:08:22', '2022-12-21 15:10:32', '0', 'know_search');
INSERT INTO `kf_security_role_permission`
(`id`, `role_id`, `permission_id`, `create_time`, `update_time`, `is_delete`, `app_name`)
VALUES ('5663', '2', '1853', '2022-12-21 15:08:22', '2022-12-21 15:10:32', '0', 'know_search');
ALTER TABLE arius_op_task
    MODIFY title VARCHAR(10100) DEFAULT '' NOT NULL COMMENT ' 标题 ';

ALTER TABLE gateway_cluster_node_info
    ADD CONSTRAINT uniq_ip_port
        UNIQUE (host_name, port, cluster_name);