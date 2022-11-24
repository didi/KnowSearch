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

ALTER TABLE gateway_cluster_node_info
    ADD machine_spec VARCHAR(255) DEFAULT '' NULL COMMENT '机器规格\n';


-- 新增平台配置初始化数据
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1686, 'arius.common.group', 'cluster.region.unsupported_divide_type', 'xpack.installed,zen1,ml.machine_memory,ml.max_open_jobs,ml.enabled', 1, -1, 1, '设置平台不支持的region划分方式', '2022-11-19 10:45:34', '2022-11-19 10:45:34', '2022-11-19 10:45:34');
