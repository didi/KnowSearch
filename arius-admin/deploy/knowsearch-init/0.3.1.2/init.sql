#项目、用户、权限点
-- auto-generated definition
create table kf_security_config
(
    id          bigint unsigned auto_increment comment '主键自增'
        primary key,
    value_group varchar(100)  default ''                not null comment ' 配置项组 ',
    value_name  varchar(100)  default ''                not null comment ' 配置项名字 ',
    value       text                                    null comment '配置项的值',
    edit        int(4)        default 1                 not null comment '是否可以编辑 1 不可编辑（程序获取） 2 可编辑',
    status      int(4)        default 1                 not null comment '1 正常 2 禁用',
    memo        varchar(1000) default ''                not null comment ' 备注 ',
    create_time timestamp     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time timestamp     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '修改时间',
    is_delete   tinyint(1)    default 0                 not null comment '逻辑删除',
    app_name    varchar(16) collate utf8_bin            null comment '应用名称',
    operator    varchar(16) collate utf8_bin            null comment '操作者'
)
    comment 'logi 配置项' charset = utf8;

create index idx_group_name
    on kf_security_config (value_group, value_name);

-- auto-generated definition
create table kf_security_dept
(
    id          int auto_increment
        primary key,
    dept_name   varchar(10)                          not null comment '部门名',
    parent_id   int                                  not null comment '父部门 id',
    leaf        tinyint(1)                           not null comment '是否叶子部门',
    level       tinyint                              not null comment 'parentId 为 0 的层级为 1',
    description varchar(20)                          null comment '描述',
    create_time timestamp  default CURRENT_TIMESTAMP null comment '创建时间',
    update_time timestamp  default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete   tinyint(1) default 0                 null comment '逻辑删除',
    app_name    varchar(16)                          null comment '应用名称'
)
    comment '部门信息表' charset = utf8;

-- auto-generated definition
create table kf_security_message
(
    id          int auto_increment
        primary key,
    title       varchar(60)                          not null comment '标题',
    content     varchar(256)                         null comment '内容',
    read_tag    tinyint(1) default 0                 null comment '是否已读',
    oplog_id    int                                  null comment '操作日志 id',
    user_id     int                                  null comment '这条消息属于哪个用户的，用户 id',
    create_time timestamp  default CURRENT_TIMESTAMP null comment '创建时间',
    update_time timestamp  default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete   tinyint(1) default 0                 null comment '逻辑删除',
    app_name    varchar(16)                          null comment '应用名称'
)
    comment '消息中心' charset = utf8;

-- auto-generated definition
create table kf_security_oplog
(
    id                int auto_increment
        primary key,
    operator_ip       varchar(20)                            not null comment '操作者 ip',
    operator          varchar(20)                            null comment '操作者账号',
    operate_page      varchar(16)                            null comment '操作页面',
    operate_type      varchar(16)                            not null comment '操作类型',
    target_type       varchar(16)                            not null comment '对象分类',
    target            varchar(255)                            not null comment '操作对象',
    detail            text                                   null comment '日志详情',
    create_time       timestamp    default CURRENT_TIMESTAMP null,
    update_time       timestamp    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete         tinyint(1)   default 0                 not null comment '逻辑删除',
    app_name          varchar(16)                            null comment '应用名称',
    operation_methods varchar(255) default ''                null
)
    comment '操作日志' charset = utf8;

-- auto-generated definition
create table kf_security_oplog_extra
(
    id          int auto_increment
        primary key,
    info        varchar(16)                          null comment '信息',
    type        tinyint                              not null comment '哪种信息：1：操作页面;2：操作类型;3：对象分类',
    create_time timestamp  default CURRENT_TIMESTAMP null comment '创建时间',
    update_time timestamp  default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete   tinyint(1) default 0                 null comment '逻辑删除',
    app_name    varchar(16)                          null comment '应用名称'
)
    comment '操作日志信息（操作页面、操作类型、对象分类）' charset = utf8;

-- auto-generated definition
create table kf_security_permission
(
    id              int auto_increment
        primary key,
    permission_name varchar(40)                          not null comment '权限名字',
    parent_id       int                                  not null comment '父权限 id',
    leaf            tinyint(1)                           not null comment '是否叶子权限点（具体的操作）',
    level           tinyint                              not null comment '权限点的层级（parentId 为 0 的层级为 1）',
    description     varchar(64)                          null comment '权限点描述',
    create_time     timestamp  default CURRENT_TIMESTAMP null comment '创建时间',
    update_time     timestamp  default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete       tinyint(1) default 0                 null comment '逻辑删除',
    app_name        varchar(16)                          null comment '应用名称'
)
    comment '权限表' charset = utf8;

-- auto-generated definition
create table kf_security_project
(
    id           int auto_increment comment '项目 id'
        primary key,
    project_code varchar(128)                           not null comment '项目编号',
    project_name varchar(128)                           not null comment '项目名',
    description  varchar(512) default ''                not null comment ' 项目描述 ',
    dept_id      int                                    not null comment '部门 id',
    running      tinyint(1)   default 1                 not null comment '启用 or 停用',
    create_time  timestamp    default CURRENT_TIMESTAMP null comment '创建时间',
    update_time  timestamp    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete    tinyint(1)   default 0                 not null comment '逻辑删除',
    app_name     varchar(16)                            null comment '应用名称'
)
    comment '项目表' charset = utf8;

-- auto-generated definition
create table kf_security_resource_type
(
    id          int auto_increment
        primary key,
    type_name   varchar(16)                          null comment '资源类型名',
    create_time timestamp  default CURRENT_TIMESTAMP null comment '创建时间',
    update_time timestamp  default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete   tinyint(1) default 0                 not null comment '逻辑删除',
    app_name    varchar(16)                          null comment '应用名称'
)
    comment '资源类型表' charset = utf8;

-- auto-generated definition
create table kf_security_role
(
    id           int auto_increment
        primary key,
    role_code    varchar(128)                         not null comment '角色编号',
    role_name    varchar(128)                         not null comment '名称',
    description  varchar(128)                         null comment '角色描述',
    last_reviser varchar(30)                          null comment '最后修改人',
    create_time  timestamp  default CURRENT_TIMESTAMP null comment '创建时间',
    update_time  timestamp  default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete    tinyint(1) default 0                 not null comment '逻辑删除',
    app_name     varchar(16)                          null comment '应用名称'
)
    comment '角色信息' charset = utf8;

-- auto-generated definition
create table kf_security_role_permission
(
    id            int auto_increment
        primary key,
    role_id       int                                  not null comment '角色 id',
    permission_id int                                  not null comment '权限 id',
    create_time   timestamp  default CURRENT_TIMESTAMP null comment '创建时间',
    update_time   timestamp  default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete     tinyint(1) default 0                 not null comment '逻辑删除',
    app_name      varchar(16)                          null comment '应用名称'
)
    comment '角色权限表（只保留叶子权限与角色关系）' charset = utf8;

-- auto-generated definition
create table kf_security_user
(
    id          int auto_increment
        primary key,
    user_name   varchar(64)                            not null comment '用户账号',
    pw          varchar(2048)                          not null comment '用户密码',
    salt        char(5)      default ''                not null comment ' 密码盐 ',
    real_name   varchar(128) default ''                not null comment ' 真实姓名 ',
    phone       char(11)     default ''                not null comment 'mobile',
    email       varchar(30)  default ''                not null comment 'email',
    dept_id     int                                    null comment '所属部门 id',
    is_delete   tinyint(1)   default 0                 not null comment '逻辑删除',
    create_time timestamp    default CURRENT_TIMESTAMP null comment '注册时间',
    update_time timestamp    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    app_name    varchar(16)                            null comment '应用名称'
)
    comment '用户信息' charset = utf8;

-- auto-generated definition
create table kf_security_user_project
(
    id          int auto_increment
        primary key,
    user_id     int                                   not null comment '用户 id',
    project_id  int                                   not null comment '项目 id',
    create_time timestamp   default CURRENT_TIMESTAMP null comment '创建时间',
    update_time timestamp   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete   tinyint(1)  default 0                 not null comment '逻辑删除',
    app_name    varchar(16)                           null comment '应用名称',
    user_type   tinyint(10) default 0                 not null comment '用户类型：0：普通项目用户；1：项目 owner'
)
    comment '用户项目关系表（项目负责人）' charset = utf8;

-- auto-generated definition
create table kf_security_user_resource
(
    id               int auto_increment
        primary key,
    user_id          int                                  not null comment '用户 id',
    project_id       int                                  not null comment '资源所属项目 id',
    resource_type_id int                                  not null comment '资源类别 id',
    resource_id      int                                  not null comment '资源 id',
    control_level    tinyint                              not null comment '管理级别：1（查看权限）2（管理权限）',
    create_time      timestamp  default CURRENT_TIMESTAMP null comment '创建时间',
    update_time      timestamp  default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete        tinyint(1) default 0                 not null comment '逻辑删除',
    app_name         varchar(16)                          null comment '应用名称'
)
    comment '用户和资源关系表' charset = utf8;

-- auto-generated definition
create table kf_security_user_role
(
    id          int auto_increment
        primary key,
    user_id     int                                  not null comment '用户 id',
    role_id     int                                  not null comment '角色 id',
    create_time timestamp  default CURRENT_TIMESTAMP null comment '创建时间',
    update_time timestamp  default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete   tinyint(1) default 0                 not null comment '逻辑删除',
    app_name    varchar(16)                          null comment '应用名称'
)
    comment '用户角色表' charset = utf8;

-- auto-generated definition
create table arius_es_user
(
    id                 bigint(10) unsigned auto_increment comment '主键 自增'
        primary key,
    index_exp          text                                    null comment '索引表达式',
    data_center        varchar(20)   default ''                not null comment ' 数据中心 ',
    is_root            tinyint       default 0                 not null comment '是都是超级用户 超级用户具有所有索引的访问权限 0 不是 1 是',
    memo               varchar(1000) default ''                not null comment ' 备注 ',
    ip                 varchar(500)  default ''                not null comment ' 白名单 ip 地址 ',
    verify_code        varchar(50)   default ''                not null comment 'app 验证码 ',
    is_active          tinyint(2)    default 1                 not null comment '1 为可用，0 不可用',
    query_threshold    int(10)       default 100               not null comment '限流值',
    cluster            varchar(100)  default ''                not null comment ' 查询集群 ',
    responsible        varchar(500)  default ''                null comment ' 责任人 ',
    search_type        tinyint       default 0                 not null comment '0 表示 app 的查询请求需要 app 里配置的集群 (一般配置的都是 trib 集群) 1 表示 app 的查询请求必须只能访问一个模板 2 表示集群模式（可支持多模板查询）',
    create_time        timestamp     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time        timestamp     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '修改时间',
    project_id         bigint(10)                              not null comment '项目 id',
    is_default_display tinyint(2)    default 0                 not null comment '1：项目默认的 es user；0: 项目新增的 es user'
)
    comment 'es 操作用户表' charset = utf8;
create table project_arius_config
(
    project_id              bigint(10) unsigned auto_increment comment 'project id'
        primary key,
    analyze_response_enable tinyint       default 1                 not null comment '响应结果解析开关 默认是 0：关闭，1：开启',
    is_source_separated     tinyint       default 0                 not null comment '是否是索引存储分离的 0 不是 1 是',
    aggr_analyze_enable     tinyint       default 1                 not null comment '1 生效 0 不生效',
    dsl_analyze_enable      tinyint(2)    default 1                 not null comment '1 为生效 dsl 分析查询限流值，0 不生效 dsl 分析查询限流值',
    slow_query_times        int(10)       default 100               not null comment '慢查询耗时',
    is_active               tinyint(2)    default 1                 not null comment '1 为可用，0 不可用',
    memo                    varchar(1000) default ''                not null comment ' 备注 ',
    create_time             timestamp     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time             timestamp     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '修改时间'
)
    comment '项目配置' charset = utf8;

-- auto-generated definition


#### 核心表结构
CREATE TABLE `arius_config_info`
(
    `id`          bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键自增',
    `value_group` varchar(100)        NOT NULL DEFAULT '' COMMENT ' 配置项组 ',
    `value_name`  varchar(100)        NOT NULL DEFAULT '' COMMENT ' 配置项名字 ',
    `value`       text COMMENT '配置项的值',
    `edit`        int(4)              NOT NULL DEFAULT '1' COMMENT '是否可以编辑 1 不可编辑（程序获取） 2 可编辑',
    `dimension`   int(4)              NOT NULL DEFAULT '-1' COMMENT '配置项维度 1 集群 2 模板',
    `status`      int(4)              NOT NULL DEFAULT '1' COMMENT '1 正常 2 禁用 -1 删除',
    `memo`        varchar(1000)       NOT NULL DEFAULT '' COMMENT ' 备注 ',
    `create_time` timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `search_time` timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '配置查询时间',
    PRIMARY KEY (`id`),
    KEY `idx_group_name` (`value_group`, `value_name`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1662
  DEFAULT CHARSET = utf8 COMMENT ='arius 配置项';



-- ----------------------------
-- Table structure for arius_meta_job_cluster_distribute
-- ----------------------------
CREATE TABLE `arius_meta_job_cluster_distribute`
(
    `id`           bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键 id',
    `cluster_id`   int(11)             NOT NULL DEFAULT '-1' COMMENT '集群 id',
    `monitor_host` varchar(128)        NOT NULL DEFAULT '' COMMENT ' 当前执行主机名 ',
    `monitor_time` timestamp           NOT NULL DEFAULT '2000-01-02 00:00:00' COMMENT '上一次监控时间',
    `gmt_create`   timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modify`   timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `cluster`      varchar(128)        NOT NULL DEFAULT '' COMMENT ' 集群名称 ',
    `dataCentre`   varchar(16)         NOT NULL DEFAULT 'cn' COMMENT '集群数据中心',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_cluster_id` (`cluster_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 435089
  DEFAULT CHARSET = utf8 COMMENT ='monitor 任务分配';

-- ----------------------------
-- Table structure for arius_op_task
-- ----------------------------
CREATE TABLE `arius_op_task`
(
    `id`           bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id 主键自增',
    `title`        varchar(100)        NOT NULL DEFAULT '' COMMENT ' 标题 ',
    `business_key` varchar(1000)       NOT NULL DEFAULT '0' COMMENT '业务数据主键',
    `status`       varchar(20)         NOT NULL DEFAULT 'waiting' COMMENT '任务状态：success: 成功 failed: 失败 running: 执行中 waiting: 等待 cancel: 取消 pause: 暂停',
    `creator`      varchar(100)        NOT NULL DEFAULT '' COMMENT ' 创建人 ',
    `create_time`  timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag`  tinyint(1)          NOT NULL DEFAULT '0' COMMENT '标记删除',
    `expand_data`  longtext COMMENT '扩展数据',
    `task_type`    int(11)             NOT NULL DEFAULT '0' COMMENT '任务类型 1：集群新增，2：集群扩容，3：集群缩容，4：集群重，5：集群升级，6：集群插件操作，10：模版 dcdr 任务',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 2302
  DEFAULT CHARSET = utf8 COMMENT ='arius 任务表';

-- ----------------------------
-- Table structure for arius_work_order_info
-- ----------------------------
CREATE TABLE `arius_work_order_info`
(
    `id`                   bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
    `type`                 varchar(25)         NOT NULL DEFAULT 'unknown' COMMENT 'appcreate 创建 app,clustercreate 创建集群,clusterindecrease 集群扩缩溶,clusteroffline 集群下线,clusterupdate 集群修改,templateauth 索引申请,templatecreate 索引创建,templateindecrease 索引扩容,templatequerydsl 查询语句创建,templatetransfer 索引转让,querydsllimitedit 查询语句编辑,responsiblegovern 员工离职,unhealthytemplategovern 不健康索引处理',
    `title`                varchar(64)         NOT NULL DEFAULT '' COMMENT ' 标题 ',
    `approver_project_id`  int(16)             NOT NULL DEFAULT '-1' COMMENT '审批人 projectid',
    `applicant`            varchar(64)         NOT NULL DEFAULT '' COMMENT ' 申请人 ',
    `extensions`           text COMMENT '拓展字段',
    `description`          text COMMENT '备注信息',
    `approver`             varchar(64)         NOT NULL DEFAULT '' COMMENT ' 审批人 ',
    `finish_time`          timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '结束时间',
    `opinion`              varchar(256)        NOT NULL DEFAULT '' COMMENT ' 审批信息 ',
    `status`               int(16)             NOT NULL DEFAULT '0' COMMENT '工单状态, 0: 待审批, 1: 通过, 2: 拒绝, 3: 取消',
    `create_time`          timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`          timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修 \n 改时间',
    `applicant_project_id` int(16)             NOT NULL DEFAULT '-1' COMMENT '申请人 projectid',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 2522
  DEFAULT CHARSET = utf8 COMMENT ='工单表';

-- ----------------------------
-- Table structure for es_cluster_phy_info
-- ----------------------------
CREATE TABLE `es_cluster_phy_info`
(
    `id`                      bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键自增',
    `cluster`                 varchar(128)        NOT NULL DEFAULT '' COMMENT 'es 集群名 ',
    `read_address`            varchar(1000)       NOT NULL DEFAULT '' COMMENT ' 读地址 tcp',
    `write_address`           varchar(2000)       NOT NULL DEFAULT '' COMMENT ' 写地址 tcp',
    `http_address`            varchar(1000)       NOT NULL DEFAULT '' COMMENT 'http 服务地址 ',
    `http_write_address`      varchar(8000)       NOT NULL DEFAULT '' COMMENT 'http 写地址 ',
    `desc`                    varchar(2000)       NOT NULL DEFAULT '' COMMENT ' 描述 ',
    `type`                    tinyint(4)          NOT NULL DEFAULT '-1' COMMENT '集群类型，3-docker 集群，4-host 集群',
    `data_center`             varchar(10)         NOT NULL DEFAULT 'cn' COMMENT '数据中心',
    `idc`                     varchar(10)         NOT NULL DEFAULT '' COMMENT ' 机房信息 ',
    `es_version`              varchar(100)        NOT NULL DEFAULT '' COMMENT 'es 版本 ',
    `create_time`             timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`             timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `level`                   tinyint(4)          NOT NULL DEFAULT '1' COMMENT '服务等级',
    `password`                varchar(255)        NOT NULL DEFAULT '' COMMENT ' 集群访问密码 ',
    `ecm_cluster_id`          int(11)             NOT NULL DEFAULT '-1' COMMENT 'ecm 集群 id',
    `cluster_config_template` text COMMENT '集群安装模板',
    `image_name`              varchar(500)        NOT NULL DEFAULT '' COMMENT ' 镜像名 ',
    `cfg_id`                  int(11)             NOT NULL DEFAULT '-1' COMMENT '配置包 id',
    `package_id`              int(11)             NOT NULL DEFAULT '-1' COMMENT '程序包 id',
    `plug_ids`                varchar(100)                 DEFAULT '' COMMENT ' 插件包 id 列表 ',
    `creator`                 varchar(255)        NOT NULL DEFAULT '' COMMENT ' 集群创建人 ',
    `ns_tree`                 varchar(100)        NOT NULL DEFAULT '' COMMENT ' 机器节点 ',
    `template_srvs`           varchar(255)                 DEFAULT '' COMMENT ' 集群的索引模板服务 ',
    `is_active`               tinyint(4)          NOT NULL DEFAULT '1' COMMENT '是否生效',
    `run_mode`                tinyint(255)        NOT NULL DEFAULT '0' COMMENT 'client 运行模式，0 读写共享，1 读写分离',
    `write_action`            varchar(1000)                DEFAULT NULL COMMENT '指定写 client 的 action',
    `health`                  tinyint(2)          NOT NULL DEFAULT '3' COMMENT '集群状态 1 green 2 yellow 3 red',
    `active_shard_num`        bigint(25)          NOT NULL DEFAULT '0' COMMENT '有效 shard 总数量',
    `disk_total`              bigint(50)          NOT NULL DEFAULT '0' COMMENT '集群磁盘总量 单位 byte',
    `disk_usage`              bigint(50)          NOT NULL DEFAULT '0' COMMENT '集群磁盘使用量 单位 byte',
    `disk_usage_percent`      decimal(10, 5)      NOT NULL COMMENT '集群磁盘空闲率 单位 0 ~1',
    `tags`                    text COMMENT '拓展字段, 这里用于存放集群展示用属性标签，如「集群所属资源类型」等等',
    `platform_type`           varchar(100)        NOT NULL DEFAULT '' COMMENT 'IaaS 平台类型 ',
    `resource_type`           tinyint(4)          NOT NULL DEFAULT '-1' COMMENT '集群资源类型，1- 共享资源，2- 独立资源，3- 独享资源',
    `gateway_url`             varchar(200)        NOT NULL DEFAULT '' COMMENT ' 集群 gateway 地址 ',
    `kibana_address`          varchar(200)                 DEFAULT '' COMMENT 'kibana外链地址',
    `cerebro_address`         varchar(200)                 DEFAULT '' COMMENT 'cerebro外链地址',
    PRIMARY KEY (`id`),
    KEY `idx_cluster` (`cluster`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 4710
  DEFAULT CHARSET = utf8 COMMENT ='物理集群表';

-- ----------------------------
-- Table structure for es_cluster_region
-- ----------------------------
CREATE TABLE `es_cluster_region`
(
    `id`               bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键自增',
    `logic_cluster_id` varchar(200)        NOT NULL DEFAULT '-1' COMMENT '逻辑集群 ID',
    `phy_cluster_name` varchar(128)        NOT NULL DEFAULT '' COMMENT ' 物理集群名 ',
    `racks`            varchar(2048)                DEFAULT '' COMMENT 'region 的 rack，逗号分隔 ',
    `create_time`      timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag`      tinyint(1)          NOT NULL DEFAULT '0' COMMENT '删除标记，1- 已删除，0- 未删除',
    `name`             varchar(100)        NOT NULL DEFAULT '' COMMENT 'region 名称 ',
    `config`           varchar(1024)                DEFAULT '' COMMENT 'region 配置项 ',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 4008
  DEFAULT CHARSET = utf8 COMMENT ='es 集群 region 表';

-- ----------------------------
-- Table structure for es_cluster_role_host_info
-- ----------------------------
CREATE TABLE `es_cluster_role_host_info`
(
    `id`              bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id 主键自增',
    `role_cluster_id` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '关联集群角色表外键',
    `hostname`        varchar(100)        NOT NULL DEFAULT '' COMMENT ' 节点名称 ',
    `ip`              varchar(50)         NOT NULL DEFAULT '' COMMENT ' 主机 ip',
    `cluster`         varchar(50)         NOT NULL DEFAULT '' COMMENT ' 集群 ',
    `port`            varchar(20)         NOT NULL DEFAULT '' COMMENT ' 端口，如果是节点上启动了多个进程，可以是多个，用逗号隔开 ',
    `role`            tinyint(4)          NOT NULL DEFAULT '-1' COMMENT '角色信息， 1data 2client 3master 4tribe',
    `status`          tinyint(4)          NOT NULL DEFAULT '1' COMMENT '节点状态，1 在线 2 离线',
    `rack`            varchar(30)                  DEFAULT '' COMMENT ' 节点 rack 信息 ',
    `node_set`        varchar(500)        NOT NULL DEFAULT '' COMMENT ' 节点 set 信息 ',
    `create_time`     timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag`     tinyint(1)          NOT NULL DEFAULT '0' COMMENT '标记删除',
    `machine_spec`    varchar(100)                 DEFAULT '',
    `region_id`       bigint(20)          NOT NULL DEFAULT '-1' COMMENT '节点所属 regionId',
    `attributes`      text COMMENT 'es 节点 attributes 信息 , 逗号分隔',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_elastic_cluster_id_role_node_set` (`role_cluster_id`, `ip`, `port`, `node_set`),
    KEY `idx_cluster` (`cluster`),
    KEY `idx_hostname` (`hostname`),
    KEY `idx_rack` (`rack`),
    KEY `idx_region_id` (`region_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 2274
  DEFAULT CHARSET = utf8 COMMENT ='es 集群表对应各角色主机列表';

-- ----------------------------
-- Table structure for es_cluster_role_info
-- ----------------------------
CREATE TABLE `es_cluster_role_info`
(
    `id`                 bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id 主键自增',
    `elastic_cluster_id` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT 'elastic_cluster 外键 id',
    `role_cluster_name`  varchar(256)        NOT NULL DEFAULT '' COMMENT 'role 集群名称 ',
    `role`               varchar(20)         NOT NULL DEFAULT '' COMMENT ' 集群角色 (masternode/datanode/clientnode)',
    `pod_number`         int(11)             NOT NULL DEFAULT '0' COMMENT 'pod 数量',
    `pid_count`          int(11)             NOT NULL DEFAULT '1' COMMENT '单机实例数',
    `machine_spec`       varchar(100)                 DEFAULT '' COMMENT ' 机器规格 ',
    `es_version`         varchar(150)        NOT NULL DEFAULT '' COMMENT 'es 版本 ',
    `cfg_id`             int(11)             NOT NULL DEFAULT '-1' COMMENT '配置包 id',
    `plug_ids`           varchar(100)        NOT NULL DEFAULT '' COMMENT ' 插件包 id 列表 ',
    `create_time`        timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`        timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag`        tinyint(1)          NOT NULL DEFAULT '0' COMMENT '标记删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_elastic_cluster_id_ddcloud_cluster_name` (`elastic_cluster_id`, `role_cluster_name`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1138
  DEFAULT CHARSET = utf8 COMMENT ='es 集群角色表';

-- ----------------------------
-- Table structure for es_config
-- ----------------------------
CREATE TABLE `es_config`
(
    `id`             bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id 主键自增',
    `cluster_id`     bigint(20)          NOT NULL DEFAULT '-1' COMMENT '集群 id',
    `type_name`      varchar(255)        NOT NULL DEFAULT '' COMMENT ' 配置文件名称 ',
    `engin_name`     varchar(100)        NOT NULL DEFAULT '' COMMENT ' 组件名称 ',
    `config_data`    longtext COMMENT '配置内容',
    `desc`           varchar(255)        NOT NULL DEFAULT '' COMMENT ' 配置描述 ',
    `version_tag`    varchar(100)        NOT NULL DEFAULT '' COMMENT ' 配置 tag',
    `version_config` bigint(20)          NOT NULL DEFAULT '-1' COMMENT '配置版本',
    `selected`       smallint(6)         NOT NULL DEFAULT '0' COMMENT '是否在使用',
    `create_time`    timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag`    tinyint(1)          NOT NULL DEFAULT '0' COMMENT '标记删除',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1130
  DEFAULT CHARSET = utf8 COMMENT ='es 配置表';

-- ----------------------------
-- Table structure for es_machine_norms
-- ----------------------------
CREATE TABLE `es_machine_norms`
(
    `id`          bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id 主键自增',
    `role`        varchar(20)         NOT NULL DEFAULT '' COMMENT ' 角色 (masternode/datanode/clientnode/datanode-ceph)',
    `spec`        varchar(32)         NOT NULL DEFAULT '' COMMENT ' 规格 (16-48gi-100g)',
    `create_time` timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `delete_flag` tinyint(1)          NOT NULL DEFAULT '0' COMMENT '标记删除',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 16
  DEFAULT CHARSET = utf8 COMMENT ='容器规格列表';

-- ----------------------------
-- Table structure for es_package
-- ----------------------------
CREATE TABLE `es_package`
(
    `id`          bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id 主键自增',
    `url`         varchar(255)        NOT NULL DEFAULT '' COMMENT ' 镜像地址或包地址 ',
    `es_version`  varchar(100)        NOT NULL DEFAULT '' COMMENT ' 版本标识 ',
    `creator`     varchar(100)        NOT NULL DEFAULT '' COMMENT ' 包创建人 ',
    `release`     tinyint(1)          NOT NULL DEFAULT '0' COMMENT '是否为发布版本',
    `manifest`    varchar(32)         NOT NULL DEFAULT '' COMMENT ' 类型 (3 docker/4 host)',
    `desc`        varchar(384)                 DEFAULT '' COMMENT ' 备注 ',
    `create_time` timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` tinyint(1)          NOT NULL DEFAULT '0' COMMENT '标记删除 0 未删 1 已删',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 318
  DEFAULT CHARSET = utf8 COMMENT ='程序包版本管理';

-- ----------------------------
-- Table structure for es_plugin
-- ----------------------------
CREATE TABLE `es_plugin`
(
    `id`                 bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id 主键自增',
    `name`               varchar(50)         NOT NULL DEFAULT '' COMMENT ' 插件名 ',
    `physic_cluster_ids` varchar(100)        NOT NULL DEFAULT '' COMMENT ' 物理集群 id',
    `version`            varchar(50)         NOT NULL DEFAULT '' COMMENT ' 插件版本 ',
    `url`                varchar(1024)       NOT NULL DEFAULT '' COMMENT ' 插件存储地址 ',
    `md5`                varchar(100)        NOT NULL DEFAULT '' COMMENT ' 插件文件 md5',
    `desc`               varchar(255)        NOT NULL DEFAULT '' COMMENT ' 插件描述 ',
    `creator`            varchar(100)        NOT NULL DEFAULT '' COMMENT ' 插件创建人 ',
    `p_default`          tinyint(1)          NOT NULL DEFAULT '0' COMMENT '是否为系统默认：0 否 1 是',
    `create_time`        timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `delete_flag`        tinyint(1)          NOT NULL DEFAULT '0' COMMENT '标记删除',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 416
  DEFAULT CHARSET = utf8 COMMENT ='es 插件包管理';

-- ----------------------------
-- Table structure for es_work_order_task
-- ----------------------------
CREATE TABLE `es_work_order_task`
(
    `id`                bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id 主键自增',
    `title`             varchar(100)        NOT NULL DEFAULT '' COMMENT ' 标题 ',
    `work_order_id`     bigint(20)          NOT NULL DEFAULT '-1' COMMENT '工单 id',
    `physic_cluster_id` bigint(20)          NOT NULL DEFAULT '-1' COMMENT '物理集群 id',
    `cluster_node_role` varchar(512)        NOT NULL DEFAULT '-1' COMMENT '集群节点角色',
    `task_ids`          varchar(128)        NOT NULL DEFAULT '' COMMENT ' 各角色任务 ids',
    `type`              varchar(50)         NOT NULL DEFAULT '' COMMENT ' 集群类型:3 docker 容器云 / 4 host 物理机 ',
    `order_type`        varchar(50)         NOT NULL DEFAULT '' COMMENT ' 工单类型 1 集群新增 2 集群扩容 3 集群缩容 4 集群重启 5 集群升级 ',
    `status`            varchar(20)         NOT NULL DEFAULT '' COMMENT ' 任务状态 ',
    `creator`           varchar(100)        NOT NULL DEFAULT '' COMMENT ' 工单创建人 ',
    `create_time`       timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag`       tinyint(1)          NOT NULL DEFAULT '0' COMMENT '标记删除',
    `handle_data`       longtext COMMENT '工单数据',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1958
  DEFAULT CHARSET = utf8 COMMENT ='es 工单任务表';

-- ----------------------------
-- Table structure for es_work_order_task_detail
-- ----------------------------
CREATE TABLE `es_work_order_task_detail`
(
    `id`                 bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id 主键自增',
    `work_order_task_id` bigint(20)          NOT NULL DEFAULT '-1' COMMENT '工单任务 id',
    `role`               varchar(100)        NOT NULL DEFAULT '' COMMENT ' 所属角色 ',
    `hostname`           varchar(100)        NOT NULL DEFAULT '' COMMENT ' 主机名称 /ip',
    `grp`                int(11)             NOT NULL DEFAULT '0' COMMENT '机器的分组',
    `idx`                int(11)             NOT NULL DEFAULT '0' COMMENT '机器在分组中的索引',
    `task_id`            bigint(20)          NOT NULL DEFAULT '-1' COMMENT '容器云 / 物理机 接口返回任务 id',
    `status`             varchar(20)         NOT NULL DEFAULT '' COMMENT ' 任务状态 ',
    `create_time`        timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`        timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag`        tinyint(1)          NOT NULL DEFAULT '0' COMMENT '标记删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_work_order_task_id_role_hostname_delete_flag` (`work_order_task_id`, `role`, `hostname`, `delete_flag`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 6592
  DEFAULT CHARSET = utf8 COMMENT ='es 工单任务详情表';

-- ----------------------------
-- Table structure for gateway_cluster_info
-- ----------------------------
CREATE TABLE `gateway_cluster_info`
(
    `id`           bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键 自增',
    `cluster_name` varchar(50)         NOT NULL DEFAULT '' COMMENT ' 集群名称 ',
    `create_time`  timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_cluster_name` (`cluster_name`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 188
  DEFAULT CHARSET = utf8 COMMENT ='gateway 集群信息';

-- ----------------------------
-- Table structure for gateway_cluster_node_info
-- ----------------------------
CREATE TABLE `gateway_cluster_node_info`
(
    `id`             bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键 自增',
    `cluster_name`   varchar(50)         NOT NULL DEFAULT '' COMMENT ' 集群名称 ',
    `host_name`      varchar(50)         NOT NULL DEFAULT '' COMMENT ' 主机名 ',
    `port`           int(10)             NOT NULL DEFAULT '-1' COMMENT '端口',
    `heartbeat_time` timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '心跳时间',
    `create_time`    timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_ip_port` (`host_name`, `port`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 490264
  DEFAULT CHARSET = utf8 COMMENT ='gateway 节点信息';

-- ----------------------------
-- Table structure for index_template_alias
-- ----------------------------
CREATE TABLE `index_template_alias`
(
    `id`                bigint(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键 自增',
    `index_template_id` int(10)             NOT NULL DEFAULT '-1' COMMENT '索引模板 id',
    `name`              varchar(50)         NOT NULL DEFAULT '' COMMENT ' 别名 ',
    `filterterm`        varchar(255)        NOT NULL DEFAULT '' COMMENT ' 过滤器 ',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 4
  DEFAULT CHARSET = utf8 COMMENT ='索引别名';

-- ----------------------------
-- Table structure for index_template_config
-- ----------------------------
CREATE TABLE `index_template_config`
(
    `is_source_separated`      tinyint(4)          NOT NULL DEFAULT '0' COMMENT '是否是索引处分分离的 0 不是 1 是',
    `idc_flags`                tinyint(1)          NOT NULL DEFAULT '0' COMMENT 'idc 标识',
    `adjust_rack_shard_factor` decimal(10, 2)      NOT NULL DEFAULT '1.00' COMMENT '模板 shard 的资源消耗因子',
    `mapping_improve_enable`   tinyint(4)          NOT NULL DEFAULT '1' COMMENT 'mapping 优化开关 1 开 0 关',
    `pre_create_flags`         tinyint(1)          NOT NULL DEFAULT '1' COMMENT '预创建标识',
    `update_time`              timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `disable_source_flags`     tinyint(1)          NOT NULL DEFAULT '0' COMMENT '禁用 source 标识',
    `disable_index_rollover`   tinyint(1)          NOT NULL DEFAULT '1' COMMENT '禁用 indexRollover 功能',
    `dynamic_limit_enable`     tinyint(4)          NOT NULL DEFAULT '1' COMMENT '动态限流开关 1 开 0 关',
    `logic_id`                 int(10)             NOT NULL DEFAULT '-1' COMMENT '逻辑模板 id',
    `create_time`              timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `shard_num`                int(11)             NOT NULL DEFAULT '1' COMMENT 'shard 数量',
    `adjust_rack_tps_factor`   decimal(10, 2)      NOT NULL DEFAULT '1.00' COMMENT '容量规划时，tps 的系数',
    `id`                       bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    PRIMARY KEY (`id`),
    KEY `idx_logic_id` (`logic_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1546
  DEFAULT CHARSET = utf8 COMMENT ='模板配置信息';

-- ----------------------------
-- Table structure for index_template_info
-- ----------------------------
CREATE TABLE `index_template_info`
(
    `id`                bigint(20) unsigned          NOT NULL AUTO_INCREMENT COMMENT '主键自增',
    `name`              varchar(128)                 NOT NULL DEFAULT '' COMMENT ' 名称 ',
    `data_type`         tinyint(4)                   NOT NULL DEFAULT '-1' COMMENT '数据类型',
    `date_format`       varchar(50)                  NOT NULL DEFAULT '' COMMENT ' 索引分区的时间后缀 ',
    `is_active`         tinyint(2)                   NOT NULL DEFAULT '1' COMMENT '有效标记',
    `data_center`       varchar(20)                  NOT NULL DEFAULT '' COMMENT ' 数据中心 ',
    `expire_time`       bigint(20)                   NOT NULL DEFAULT '-1' COMMENT '保存时长',
    `hot_time`          int(10)                      NOT NULL DEFAULT '-1' COMMENT '热数据保存时长',
    `responsible`       varchar(500)                          DEFAULT '' COMMENT ' 责任人 ',
    `date_field`        varchar(50)                  NOT NULL DEFAULT '' COMMENT ' 时间字段 ',
    `date_field_format` varchar(128)                 NOT NULL DEFAULT '' COMMENT ' 时间字段的格式 ',
    `id_field`          varchar(512)                          DEFAULT '' COMMENT 'id 字段 ',
    `routing_field`     varchar(512)                          DEFAULT '' COMMENT 'routing 字段 ',
    `expression`        varchar(100)                 NOT NULL DEFAULT '' COMMENT ' 索引表达式 ',
    `desc`              varchar(1000)                NOT NULL DEFAULT '' COMMENT ' 索引描述 ',
    `quota`             decimal(10, 3)               NOT NULL DEFAULT '-1.000' COMMENT '规格',
    `project_id`        int(10)                      NOT NULL DEFAULT '-1' COMMENT 'project_id',
    `ingest_pipeline`   varchar(512)                 NOT NULL DEFAULT '' COMMENT 'ingest_pipeline',
    `block_read`        tinyint(1) unsigned zerofill NOT NULL DEFAULT '0' COMMENT '是否禁读，0：否，1：是',
    `block_write`       tinyint(1) unsigned zerofill NOT NULL DEFAULT '0' COMMENT '是否禁写，0：否，1：是',
    `create_time`       timestamp                    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       timestamp                    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `write_rate_limit`  bigint(255)                  NOT NULL DEFAULT '-1' COMMENT '写入限流值',
    `resource_id`       bigint(20)                   NOT NULL DEFAULT '-1' COMMENT '逻辑集群 id',
    `check_point_diff`  bigint(100)                  NOT NULL DEFAULT '0' COMMENT 'dcdr 位点差',
    `level`             tinyint(4)                   NOT NULL DEFAULT '1' COMMENT '服务等级分为 1,2,3',
    `has_dcdr`          tinyint(1) unsigned zerofill NOT NULL DEFAULT '0' COMMENT '是否开启 dcdr',
    `open_srv`          varchar(255)                          DEFAULT NULL COMMENT '已开启的模板服务',
    `disk_size`         decimal(10, 3)                        DEFAULT '-1.000' COMMENT '可用磁盘容量',
    `health`            int(11)                               DEFAULT '-1' COMMENT '模版健康；-1 是 UNKNOW',
    PRIMARY KEY (`id`),
    KEY `idx_data_center` (`data_center`),
    KEY `idx_is_active` (`is_active`),
    KEY `idx_name` (`name`),
    KEY `idx_project_id` (`project_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 25998
  DEFAULT CHARSET = utf8 COMMENT ='逻辑索引模板表';

-- ----------------------------
-- Table structure for index_template_physical_info
-- ----------------------------
CREATE TABLE `index_template_physical_info`
(
    `id`            bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键自增',
    `logic_id`      int(10)             NOT NULL DEFAULT '-1' COMMENT '逻辑模板 id',
    `name`          varchar(128)        NOT NULL DEFAULT '' COMMENT ' 模板名字 ',
    `expression`    varchar(128)        NOT NULL DEFAULT '' COMMENT ' 表达式 ',
    `cluster`       varchar(128)        NOT NULL DEFAULT '' COMMENT ' 集群名字 ',
    `rack`          varchar(512)        NOT NULL DEFAULT '' COMMENT 'rack',
    `shard`         int(10)             NOT NULL DEFAULT '1' COMMENT 'shard 个数',
    `shard_routing` int(10)             NOT NULL DEFAULT '1' COMMENT '内核的逻辑 shard',
    `version`       int(10)             NOT NULL DEFAULT '0' COMMENT '版本',
    `role`          tinyint(4)          NOT NULL DEFAULT '1' COMMENT '角色 1master 2slave',
    `status`        tinyint(4)          NOT NULL DEFAULT '1' COMMENT '1 常规 -1 索引删除中 -2 已删除',
    `config`        text COMMENT '配置 json 格式',
    `create_time`   timestamp           NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   timestamp           NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `region_id`     int(10)             NOT NULL DEFAULT '-1' COMMENT '模板关联的 regionId',
    PRIMARY KEY (`id`),
    KEY `idx_cluster_name_status` (`cluster`, `name`, `status`),
    KEY `idx_log_id_statud` (`logic_id`, `status`),
    KEY `idx_logic_id` (`logic_id`),
    KEY `idx_region_id` (`region_id`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 23700
  DEFAULT CHARSET = utf8 COMMENT ='物理模板信息';

-- ----------------------------
-- Table structure for index_template_type
-- ----------------------------
CREATE TABLE `index_template_type`
(
    `id`                  bigint(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键 自增',
    `index_template_id`   int(10)             NOT NULL DEFAULT '-1' COMMENT '索引模板 id',
    `index_template_name` varchar(100)        NOT NULL DEFAULT '' COMMENT ' 索引模板名称 ',
    `name`                varchar(100)        NOT NULL DEFAULT '' COMMENT 'type 名称 ',
    `id_field`            varchar(128)        NOT NULL DEFAULT '' COMMENT 'id 字段 ',
    `routing`             varchar(100)        NOT NULL DEFAULT '' COMMENT 'routing 字段 ',
    `source`              tinyint(4)          NOT NULL DEFAULT '1' COMMENT '0 不存 source 1 存 source',
    `is_active`           tinyint(2)          NOT NULL DEFAULT '1' COMMENT '是否激活 1 是 0 否',
    `create_time`         timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`         timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='索引模板 type';

-- ----------------------------
-- Table structure for logi_job
-- ----------------------------
CREATE TABLE `logi_job`
(
    `id`          bigint(20)   NOT NULL AUTO_INCREMENT,
    `job_code`    varchar(100) NOT NULL DEFAULT '' COMMENT 'task taskCode',
    `task_code`   varchar(255) NOT NULL DEFAULT '' COMMENT ' 任务 code',
    `class_name`  varchar(255) NOT NULL DEFAULT '' COMMENT ' 类的全限定名 ',
    `try_times`   int(10)      NOT NULL DEFAULT '0' COMMENT '第几次重试',
    `worker_code` varchar(200) NOT NULL DEFAULT '' COMMENT ' 执行机器 ',
    `app_name`    varchar(100) NOT NULL DEFAULT '' COMMENT ' 被调度的应用名称 ',
    `start_time`  datetime              DEFAULT '1971-01-01 00:00:00' COMMENT '开始时间',
    `create_time` datetime              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `job_code` (`job_code`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 381677
  DEFAULT CHARSET = utf8 COMMENT ='正在执行的 job 信息';

-- ----------------------------
-- Table structure for logi_job_log
-- ----------------------------
CREATE TABLE `logi_job_log`
(
    `id`          bigint(20)   NOT NULL AUTO_INCREMENT,
    `job_code`    varchar(100) NOT NULL DEFAULT '' COMMENT 'job taskCode',
    `task_code`   varchar(255) NOT NULL DEFAULT '' COMMENT ' 任务 code',
    `task_name`   varchar(255) NOT NULL DEFAULT '' COMMENT ' 任务名称 ',
    `task_desc`   varchar(255) NOT NULL DEFAULT '' COMMENT ' 任务描述 ',
    `task_id`     bigint(20)   NOT NULL DEFAULT '0' COMMENT '任务 id',
    `class_name`  varchar(255) NOT NULL DEFAULT '' COMMENT ' 类的全限定名 ',
    `try_times`   int(10)      NOT NULL DEFAULT '0' COMMENT '第几次重试',
    `worker_code` varchar(200) NOT NULL DEFAULT '' COMMENT ' 执行机器 ',
    `worker_ip`   varchar(200) NOT NULL DEFAULT '' COMMENT ' 执行机器 ip',
    `start_time`  datetime              DEFAULT '1971-01-01 00:00:00' COMMENT '开始时间',
    `end_time`    datetime              DEFAULT '1971-01-01 00:00:00' COMMENT '结束时间',
    `status`      tinyint(4)   NOT NULL DEFAULT '0' COMMENT '执行结果 1 成功 2 失败 3 取消',
    `error`       text         NOT NULL COMMENT '错误信息',
    `result`      text         NOT NULL COMMENT '执行结果',
    `app_name`    varchar(100) NOT NULL DEFAULT '' COMMENT ' 被调度的应用名称 ',
    `create_time` datetime              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_job_code` (`job_code`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 381395
  DEFAULT CHARSET = utf8 COMMENT ='job 执行历史日志';



-- ----------------------------
-- Table structure for logi_task
-- ----------------------------
CREATE TABLE `logi_task`
(
    `id`              bigint(20)    NOT NULL AUTO_INCREMENT,
    `task_code`       varchar(100)  NOT NULL DEFAULT '' COMMENT 'task taskCode',
    `task_name`       varchar(255)  NOT NULL DEFAULT '' COMMENT ' 名称 ',
    `task_desc`       varchar(1000) NOT NULL DEFAULT '' COMMENT ' 任务描述 ',
    `cron`            varchar(100)  NOT NULL DEFAULT '' COMMENT 'cron 表达式 ',
    `class_name`      varchar(255)  NOT NULL DEFAULT '' COMMENT ' 类的全限定名 ',
    `params`          varchar(1000) NOT NULL DEFAULT '' COMMENT ' 执行参数 map 形式 {key1:value1,key2:value2}',
    `retry_times`     int(10)       NOT NULL DEFAULT '0' COMMENT '允许重试次数',
    `last_fire_time`  datetime               DEFAULT CURRENT_TIMESTAMP COMMENT '上次执行时间',
    `timeout`         bigint(20)    NOT NULL DEFAULT '0' COMMENT '超时 毫秒',
    `status`          tinyint(4)    NOT NULL DEFAULT '0' COMMENT '1 等待 2 运行中 3 暂停',
    `sub_task_codes`  varchar(1000) NOT NULL DEFAULT '' COMMENT ' 子任务 code 列表, 逗号分隔 ',
    `consensual`      varchar(200)  NOT NULL DEFAULT '' COMMENT ' 执行策略 ',
    `owner`           varchar(200)  NOT NULL DEFAULT '' COMMENT ' 责任人 ',
    `task_worker_str` varchar(3000) NOT NULL DEFAULT '' COMMENT ' 机器执行信息 ',
    `app_name`        varchar(100)  NOT NULL DEFAULT '' COMMENT ' 被调度的应用名称 ',
    `node_name_white_list_str` VARCHAR(3000) DEFAULT '' NOT NULL COMMENT '执行节点名对应白名单集',
    `create_time`     datetime               DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     datetime               DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `task_code` (`task_code`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 548
  DEFAULT CHARSET = utf8 COMMENT ='任务信息';

-- ----------------------------
-- Table structure for logi_task_lock
-- ----------------------------
CREATE TABLE `logi_task_lock`
(
    `id`          bigint(20)   NOT NULL AUTO_INCREMENT,
    `task_code`   varchar(100) NOT NULL DEFAULT '' COMMENT 'task taskCode',
    `worker_code` varchar(100) NOT NULL DEFAULT '' COMMENT 'worker taskCode',
    `app_name`    varchar(100) NOT NULL DEFAULT '' COMMENT ' 被调度的应用名称 ',
    `expire_time` bigint(20)   NOT NULL DEFAULT '0' COMMENT '过期时间',
    `create_time` datetime              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uni_task_app` (`task_code`, `app_name`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 569
  DEFAULT CHARSET = utf8 COMMENT ='任务锁';

-- ----------------------------
-- Table structure for logi_worker
-- ----------------------------
CREATE TABLE `logi_worker`
(
    `id`              bigint(20)   NOT NULL AUTO_INCREMENT,
    `worker_code`     varchar(100) NOT NULL DEFAULT '' COMMENT 'worker taskCode',
    `worker_name`     varchar(100) NOT NULL DEFAULT '' COMMENT 'worker 名 ',
    `ip`              varchar(100) NOT NULL DEFAULT '' COMMENT 'worker 的 ip',
    `cpu`             int(11)      NOT NULL DEFAULT '0' COMMENT 'cpu 数量',
    `cpu_used`        double       NOT NULL DEFAULT '0' COMMENT 'cpu 使用率',
    `memory`          double       NOT NULL DEFAULT '0' COMMENT '内存, 以 M 为单位',
    `memory_used`     double       NOT NULL DEFAULT '0' COMMENT '内存使用率',
    `jvm_memory`      double       NOT NULL DEFAULT '0' COMMENT 'jvm 堆大小，以 M 为单位',
    `jvm_memory_used` double       NOT NULL DEFAULT '0' COMMENT 'jvm 堆使用率',
    `job_num`         int(10)      NOT NULL DEFAULT '0' COMMENT '正在执行 job 数',
    `heartbeat`       datetime              DEFAULT '1971-01-01 00:00:00' COMMENT '心跳时间',
    `app_name`        varchar(100) NOT NULL DEFAULT '' COMMENT ' 被调度的应用名称 ',
    `create_time`     datetime              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     datetime              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `node_name`     VARCHAR(100) DEFAULT '' NOT NULL COMMENT 'node 名',
    PRIMARY KEY (`id`),
    UNIQUE KEY `worker_code` (`worker_code`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 8
  DEFAULT CHARSET = utf8 COMMENT ='worker 信息';

-- ----------------------------
-- Table structure for logi_worker_blacklist
-- ----------------------------
CREATE TABLE `logi_worker_blacklist`
(
    `id`          bigint(20)   NOT NULL AUTO_INCREMENT,
    `worker_code` varchar(100) NOT NULL DEFAULT '' COMMENT 'worker taskCode',
    `create_time` datetime              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `worker_code` (`worker_code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='worker 黑名单列表';

-- ----------------------------
-- Table structure for operate_record_info
-- ----------------------------
CREATE TABLE `operate_record_info`
(
    `id`             bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键 自增',
    `project_name`   varchar(255)                 DEFAULT NULL COMMENT '应用',
    `module_id`      int(10)             NOT NULL DEFAULT '-1' COMMENT '模块 id',
    `operate_id`     int(10)             NOT NULL DEFAULT '-1' COMMENT '操作 id',
    `trigger_way_id` int(11)                      DEFAULT NULL COMMENT '触发方式',
    `user_operation` varchar(50)         NOT NULL DEFAULT '' COMMENT ' 操作人 ',
    `content`        longtext COMMENT '操作内容',
    `operate_time`   timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    `create_time`    timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `biz_id`         varchar(255)                 DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 8218
  DEFAULT CHARSET = utf8 COMMENT ='操作记录表';


-- ----------------------------
-- Table structure for project_arius_resource_logic
-- ----------------------------
CREATE TABLE `project_arius_resource_logic`
(
    `id`                 bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键自增',
    `name`               varchar(128)        NOT NULL DEFAULT '' COMMENT ' 资源名称 ',
    `type`               tinyint(4)          NOT NULL DEFAULT '2' COMMENT '资源类型 1 共享公共资源 2 独享资源',
    `project_id`         varchar(1024)       NOT NULL DEFAULT '-1' COMMENT '资源所属的 project_id',
    `data_center`        varchar(20)         NOT NULL DEFAULT '' COMMENT ' 数据中心 cn/us01',
    `responsible`        varchar(128)                 DEFAULT '' COMMENT ' 资源责任人 ',
    `memo`               varchar(512)        NOT NULL DEFAULT '' COMMENT ' 资源备注 ',
    `quota`              decimal(8, 2)       NOT NULL DEFAULT '1.00' COMMENT '资源的大小',
    `level`              tinyint(4)          NOT NULL DEFAULT '1' COMMENT '服务等级 1 normal 2 important 3 vip',
    `config_json`        varchar(1024)       NOT NULL DEFAULT '' COMMENT ' 集群配置 ',
    `create_time`        timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`        timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `health`             tinyint(4)          NOT NULL DEFAULT '3' COMMENT '集群状态 1 green 2 yellow 3 red -1 未知',
    `data_node_spec`     varchar(20)         NOT NULL DEFAULT '' COMMENT ' 节点规格 ',
    `disk_total`         bigint(50)          NOT NULL DEFAULT '0' COMMENT '集群磁盘总量 单位 byte',
    `disk_usage`         bigint(50)          NOT NULL DEFAULT '0' COMMENT '集群磁盘使用量 单位 byte',
    `disk_usage_percent` decimal(10, 5)      default NULL COMMENT '集群磁盘空闲率 单位 0 ~1',
    `es_cluster_version` varchar(20)         default NULL COMMENT 'es 集群版本',
    `node_num`           int(10)             NOT NULL DEFAULT '0' COMMENT '节点个数',
    `data_node_num`      int(10)             NOT NULL DEFAULT '0' COMMENT '节点个数',
    PRIMARY KEY (`id`),
    KEY `idx_name` (`name`),
    KEY `idx_project_id` (`project_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 3922
  DEFAULT CHARSET = utf8 COMMENT ='逻辑资源信息';

-- ----------------------------
-- Table structure for project_logi_cluster_auth
-- ----------------------------
CREATE TABLE `project_logi_cluster_auth`
(
    `id`               bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键自增',
    `project_id`       int(10)             NOT NULL DEFAULT '-1' COMMENT '项目 id',
    `logic_cluster_id` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '逻辑集群 id',
    `type`             int(10)             NOT NULL DEFAULT '-1' COMMENT '权限类型，0- 超管，1- 配置管理，2- 访问，-1- 无权限',
    `responsible`      varchar(100)                 DEFAULT '' COMMENT ' 责任人 id 列表 ',
    `status`           int(10)             NOT NULL DEFAULT '1' COMMENT '状态 1 有效 0 无效',
    `create_time`      timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_project_id` (`project_id`),
    KEY `idx_logic_cluster_id` (`logic_cluster_id`),
    KEY `idx_status` (`status`),
    KEY `idx_type` (`type`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='project 逻辑集群权限';

-- ----------------------------
-- Table structure for project_template_info
-- ----------------------------
CREATE TABLE `project_template_info`
(
    `id`          bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键自增',
    `project_id`  int(10)             NOT NULL DEFAULT '-1' COMMENT '项目 id',
    `template`    varchar(100)        NOT NULL DEFAULT '' COMMENT ' 模板名称, 不能关联模板 id 模板会跨集群迁移，id 会变 ',
    `type`        int(10)             NOT NULL DEFAULT '-1' COMMENT 'appid 的权限 1 读写 2 读 -1 未知',
    `status`      int(10)             NOT NULL DEFAULT '1' COMMENT '状态 1 有效 0 无效',
    `create_time` timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_project_id` (`project_id`),
    KEY `idx_status` (`status`),
    KEY `idx_template_id` (`template`),
    KEY `idx_type` (`type`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='project 模板信息';

-- ----------------------------
-- Table structure for user_metrics_config_info
-- ----------------------------
CREATE TABLE `user_metrics_config_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_name` varchar(255) NOT NULL COMMENT '用户账号',
  `metric_info` text COMMENT '指标看板的配置',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1602 DEFAULT CHARSET=utf8 COMMENT='用户关联到指标的配置信息表';


#权限点和角色的初始化数据
insert into kf_security_role_permission (id, role_id, permission_id, create_time, update_time, is_delete, app_name)
values (1597, 1, 0, '2022-06-01 21:19:42.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1935, 1, 1593, '2022-06-14 17:41:03.0', '2022-08-27 17:36:58.0', 0, 'know_search'),
       (1937, 1, 1637, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1939, 1, 1639, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1941, 1, 1641, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1943, 1, 1643, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1945, 1, 1645, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1947, 1, 1647, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1949, 1, 1649, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1951, 1, 1651, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1953, 1, 1653, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1955, 1, 1655, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1957, 1, 1657, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1959, 1, 1659, '2022-06-14 17:41:03.0', '2022-08-25 10:33:59.0', 1, 'know_search'),
       (1961, 1, 1661, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1963, 1, 1597, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1965, 1, 1673, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1967, 1, 1675, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1969, 1, 1677, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1971, 1, 1679, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1973, 1, 1599, '2022-06-14 17:41:03.0', '2022-08-25 10:36:08.0', 1, 'know_search'),
       (1975, 1, 1681, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1977, 1, 1683, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1979, 1, 1685, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1981, 1, 1687, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1983, 1, 1601, '2022-06-14 17:41:03.0', '2022-08-25 10:36:44.0', 1, 'know_search'),
       (1985, 1, 1689, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1987, 1, 1691, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1989, 1, 1693, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1991, 1, 1695, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1993, 1, 1697, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1995, 1, 1699, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1997, 1, 1603, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1999, 1, 1701, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2001, 1, 1703, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2003, 1, 1705, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2005, 1, 1707, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2007, 1, 1709, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2009, 1, 1711, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2011, 1, 1713, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2013, 1, 1715, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2015, 1, 1717, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2017, 1, 1719, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2019, 1, 1721, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2021, 1, 1723, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2023, 1, 1605, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2025, 1, 1725, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2027, 1, 1727, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2029, 1, 1729, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2031, 1, 1731, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2033, 1, 1733, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2035, 1, 1735, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2037, 1, 1737, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2039, 1, 1739, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2041, 1, 1741, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2043, 1, 1743, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2045, 1, 1607, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2047, 1, 1745, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2049, 1, 1747, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2051, 1, 1749, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2053, 1, 1751, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2055, 1, 1753, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2057, 1, 1755, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2059, 1, 1609, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2061, 1, 1757, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2063, 1, 1855, '2022-06-14 17:41:03.0', '2022-08-25 10:34:13.0', 0, 'know_search'),
       (2065, 1, 1857, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2067, 1, 1611, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2069, 1, 1759, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2071, 1, 1859, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2073, 1, 1861, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2075, 1, 1863, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2077, 1, 1865, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2079, 1, 1867, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2081, 1, 1613, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2083, 1, 1761, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2085, 1, 1615, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2087, 1, 1763, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2089, 1, 1619, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2091, 1, 1769, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2093, 1, 1771, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2095, 1, 1773, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2097, 1, 1621, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2099, 1, 1775, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2101, 1, 1777, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2103, 1, 1779, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2105, 1, 1781, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2107, 1, 1783, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2109, 1, 1785, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2111, 1, 1787, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2113, 1, 1789, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2115, 1, 1791, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2117, 1, 1793, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2119, 1, 1795, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2121, 1, 1797, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2123, 1, 1799, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2125, 1, 1801, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2127, 1, 1623, '2022-06-14 17:41:03.0', '2022-08-27 17:34:08.0', 0, 'know_search'),
       (2129, 1, 1803, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2131, 1, 1805, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2133, 1, 1807, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2135, 1, 1809, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2137, 1, 1625, '2022-06-14 17:41:03.0', '2022-08-27 17:34:08.0', 0, 'know_search'),
       (2139, 1, 1811, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2141, 1, 1813, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2143, 1, 1815, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2145, 1, 1817, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2147, 1, 1627, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2149, 1, 1819, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2151, 1, 1821, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2153, 1, 1629, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2155, 1, 1823, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2157, 1, 1825, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2159, 1, 1827, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2161, 1, 1829, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2163, 1, 1831, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2165, 1, 1631, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2167, 1, 1833, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2169, 1, 1835, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2171, 1, 1837, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2173, 1, 1839, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2175, 1, 1841, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2177, 1, 1633, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2179, 1, 1843, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2181, 1, 1845, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2183, 1, 1847, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2185, 1, 1849, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2187, 1, 1851, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2189, 1, 1635, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2191, 1, 1853, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2193, 2, 1595, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2195, 2, 1663, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2197, 2, 1665, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2199, 2, 1667, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2201, 2, 1669, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2203, 2, 1671, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2205, 2, 1601, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2207, 2, 1689, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2209, 2, 1691, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2211, 2, 1693, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2213, 2, 1695, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2215, 2, 1697, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2217, 2, 1699, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2219, 2, 1605, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2221, 2, 1725, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2223, 2, 1727, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2225, 2, 1729, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2227, 2, 1731, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2229, 2, 1733, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2231, 2, 1735, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2233, 2, 1737, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2235, 2, 1739, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2237, 2, 1741, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2239, 2, 1743, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2241, 2, 1609, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2243, 2, 1757, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2245, 2, 1855, '2022-06-14 18:08:56.0', '2022-08-25 10:33:12.0', 1, 'know_search'),
       (2247, 2, 1857, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2249, 2, 1611, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2251, 2, 1759, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2253, 2, 1859, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2255, 2, 1861, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2257, 2, 1863, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2259, 2, 1865, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2261, 2, 1867, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2263, 2, 1613, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2265, 2, 1761, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2267, 2, 1615, '2022-06-14 18:08:56.0', '2022-08-25 20:27:55.0', 1, 'know_search'),
       (2269, 2, 1763, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2271, 2, 1617, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2273, 2, 1765, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2275, 2, 1767, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2277, 2, 1631, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2279, 2, 1833, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2281, 2, 1835, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2283, 2, 1837, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2285, 2, 1839, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2287, 2, 1841, '2022-06-14 18:08:56.0', '2022-08-26 17:59:49.0', 1, 'know_search'),
       (2643, 1, 1595, '2022-06-17 16:39:23.0', '2022-08-25 10:35:06.0', 1, 'know_search'),
       (4505, 1, 1869, '2022-07-04 15:45:59.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (4507, 1, 1871, '2022-07-04 15:46:56.0', '2022-08-27 17:37:22.0', 0, 'know_search'),
       (5275, 1, 1873, '2022-06-17 15:53:54.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (5277, 2, 1873, '2022-06-17 15:53:54.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (5349, 1, 1875, '2022-06-17 15:53:54.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (5591, 1, 1759, '2022-08-11 10:39:01.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (5593, 2, 1759, '2022-08-11 10:39:59.0', '2022-08-25 10:31:42.0', 0, 'know_search');
#权限点初始化数据
insert into kf_security_permission (id, permission_name, parent_id, leaf, level, description, create_time, update_time, is_delete, app_name)
values  (1593, '物理集群', 0, 0, 1, '物理集群', '2022-05-24 18:08:22.0', '2022-08-24 20:07:31.0', 0, 'know_search'),
        (1595, '我的集群', 0, 0, 1, '我的集群', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1597, '集群版本', 0, 0, 1, '集群版本', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1599, 'Gateway管理', 0, 0, 1, 'Gateway管理', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1601, '模板管理', 0, 0, 1, '模板管理', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1603, '模板服务', 0, 0, 1, '模板服务', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1605, '索引管理', 0, 0, 1, '索引管理', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1607, '索引服务', 0, 0, 1, '索引服务', '2022-05-24 18:08:22.0', '2022-05-24 18:24:16.0', 0, 'know_search'),
        (1609, '索引查询', 0, 0, 1, '索引查询', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1611, '查询诊断', 0, 0, 1, '查询诊断', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1613, '集群看板', 0, 0, 1, '集群看板', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1615, '网关看板', 0, 0, 1, '网关看板', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1617, '我的申请', 0, 0, 1, '我的申请', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1619, '我的审批', 0, 0, 1, '我的审批', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1621, '任务列表', 0, 0, 1, '任务列表', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1623, '调度任务列表', 0, 0, 1, '调度任务列表', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1625, '调度日志', 0, 0, 1, '调度日志', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1627, '用户管理', 0, 0, 1, '用户管理', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1629, '角色管理', 0, 0, 1, '角色管理', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1631, '应用管理', 0, 0, 1, '应用管理', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1633, '平台配置', 0, 0, 1, '平台配置', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1635, '操作记录', 0, 0, 1, '操作记录', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1637, '查看集群列表及详情', 1593, 1, 2, '查看集群列表及详情', '2022-05-24 18:08:22.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1639, '接入集群', 1593, 1, 2, '接入集群', '2022-05-24 18:08:22.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1641, '新建集群', 1593, 1, 2, '新建集群', '2022-05-24 18:08:22.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1643, '扩缩容', 1593, 1, 2, '扩缩容', '2022-05-24 18:08:22.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1645, '升级', 1593, 1, 2, '升级', '2022-05-24 18:08:22.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1647, '重启', 1593, 1, 2, '重启', '2022-05-24 18:08:23.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1649, '配置变更', 1593, 1, 2, '配置变更', '2022-05-24 18:08:23.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1651, 'Region划分', 1593, 1, 2, 'Region划分', '2022-05-24 18:08:23.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1653, 'Region管理', 1593, 1, 2, 'Region管理', '2022-05-24 18:08:23.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1655, '快捷命令', 1593, 1, 2, '快捷命令', '2022-05-24 18:08:23.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1657, '编辑', 1593, 1, 2, '编辑', '2022-05-24 18:08:23.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1659, '绑定Gateway', 1593, 1, 2, '绑定Gateway', '2022-05-24 18:08:23.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1661, '下线', 1593, 1, 2, '下线', '2022-05-24 18:08:23.0', '2022-05-24 18:10:52.0', 0, 'know_search'),
        (1663, '查看集群列表及详情', 1595, 1, 2, '查看集群列表及详情', '2022-05-24 18:08:23.0', '2022-05-24 18:10:52.0', 0, 'know_search'),
        (1665, '申请集群', 1595, 1, 2, '申请集群', '2022-05-24 18:08:23.0', '2022-05-24 18:10:52.0', 0, 'know_search'),
        (1667, '编辑', 1595, 1, 2, '编辑', '2022-05-24 18:08:23.0', '2022-05-24 18:10:52.0', 0, 'know_search'),
        (1669, '扩缩容', 1595, 1, 2, '扩缩容', '2022-05-24 18:08:23.0', '2022-05-24 18:10:52.0', 0, 'know_search'),
        (1671, '下线', 1595, 1, 2, '下线', '2022-05-24 18:08:23.0', '2022-05-24 18:20:44.0', 0, 'know_search'),
        (1673, '查看版本列表', 1597, 1, 2, '查看版本列表', '2022-05-24 18:08:23.0', '2022-05-24 18:20:44.0', 0, 'know_search'),
        (1675, '新增版本', 1597, 1, 2, '新增版本', '2022-05-24 18:08:23.0', '2022-05-24 18:20:44.0', 0, 'know_search'),
        (1677, '编辑', 1597, 1, 2, '编辑', '2022-05-24 18:08:23.0', '2022-05-24 18:20:44.0', 0, 'know_search'),
        (1679, '删除', 1597, 1, 2, '删除', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1681, '查看Gateway 集群列表', 1599, 1, 2, '查看Gateway 集群列表', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1683, '接入gateway', 1599, 1, 2, '接入gateway', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1685, '编辑', 1599, 1, 2, '编辑', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1687, '下线', 1599, 1, 2, '下线', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1689, '查看模板列表及详情', 1601, 1, 2, '查看模板列表及详情', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1691, '申请模板', 1601, 1, 2, '申请模板', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1693, '编辑', 1601, 1, 2, '编辑', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1695, '下线', 1601, 1, 2, '下线', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1697, '编辑Mapping', 1601, 1, 2, '编辑Mapping', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1699, '编辑Setting', 1601, 1, 2, '编辑Setting', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1701, '查看模板列表', 1603, 1, 2, '查看模板列表', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1703, '开关：预创建', 1603, 1, 2, '开关：预创建', '2022-05-24 18:08:23.0', '2022-06-14 16:49:48.0', 0, 'know_search'),
        (1705, '开关：过期删除', 1603, 1, 2, '开关：过期删除', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1707, '开关：冷热分离', 1603, 1, 2, '开关：冷热分离', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1709, '开关：pipeline', 1603, 1, 2, '开关：写入限流', '2022-05-24 18:08:23.0', '2022-06-14 16:49:49.0', 0, 'know_search'),
        (1711, '开关：Rollover', 1603, 1, 2, '开关：Rollover', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1713, '查看DCDR链路', 1603, 1, 2, '查看DCDR链路', '2022-05-24 18:08:23.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1715, '创建DCDR链路', 1603, 1, 2, '创建DCDR链路', '2022-05-24 18:08:24.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1717, '清理', 1603, 1, 2, '清理', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1719, '扩缩容', 1603, 1, 2, '扩缩容', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1721, '升版本', 1603, 1, 2, '升版本', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1723, '批量操作', 1603, 1, 2, '批量操作', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1725, '查看索引列表及详情', 1605, 1, 2, '查看索引列表及详情', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1727, '编辑Mapping', 1605, 1, 2, '编辑Mapping', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1729, '编辑Setting', 1605, 1, 2, '编辑Setting', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1731, '禁用读', 1607, 1, 2, '禁用读', '2022-05-24 18:08:24.0', '2022-07-15 08:50:56.0', 0, 'know_search'),
        (1733, '禁用写', 1607, 1, 2, '禁用写', '2022-05-24 18:08:24.0', '2022-07-15 08:50:56.0', 0, 'know_search'),
        (1735, '设置别名', 1605, 1, 2, '设置别名', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1737, '删除别名', 1605, 1, 2, '删除别名', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1739, '关闭索引', 1607, 1, 2, '关闭索引', '2022-05-24 18:08:24.0', '2022-07-15 09:52:25.0', 0, 'know_search'),
        (1741, '下线', 1605, 1, 2, '下线', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1743, '批量删除', 1605, 1, 2, '批量删除', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1745, '查看列表', 1607, 1, 2, '查看列表', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1747, '执行Rollover', 1607, 1, 2, '执行Rollover', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1749, '执行shrink', 1607, 1, 2, '执行shrink', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1751, '执行split', 1607, 1, 2, '执行split', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1753, '执行ForceMerge', 1607, 1, 2, '执行ForceMerge', '2022-05-24 18:08:24.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1755, '批量执行', 1607, 1, 2, '批量执行', '2022-05-24 18:08:24.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1757, 'DSL查询', 1609, 1, 2, 'DSL查询', '2022-05-24 18:08:24.0', '2022-06-14 16:39:48.0', 0, 'know_search'),
        (1759, '查询模板', 0, 0, 1, '查看查询模板列表', '2022-05-24 18:08:24.0', '2022-08-11 10:37:43.0', 0, 'know_search'),
        (1761, '查看集群看板', 1613, 1, 2, '查看集群看板', '2022-05-24 18:08:24.0', '2022-06-14 16:37:54.0', 0, 'know_search'),
        (1763, '查看网关看板', 1615, 1, 2, '查看网关看板', '2022-05-24 18:08:24.0', '2022-06-14 16:38:14.0', 0, 'know_search'),
        (1765, '查看我的申请列表', 1617, 1, 2, '查看我的申请列表', '2022-05-24 18:08:24.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1767, '撤回', 1617, 1, 2, '撤回', '2022-05-24 18:08:24.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1769, '查看我的审批列表', 1619, 1, 2, '查看我的审批列表', '2022-05-24 18:08:24.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1771, '驳回', 1619, 1, 2, '撤回', '2022-05-24 18:08:24.0', '2022-07-18 20:57:33.0', 0, 'know_search'),
        (1773, '通过', 1619, 1, 2, '通过', '2022-05-24 18:08:24.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1775, '查看任务列表', 1621, 1, 2, '查看任务列表', '2022-05-24 18:08:24.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1777, '查看进度', 1621, 1, 2, '查看进度', '2022-05-24 18:08:24.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1779, '执行', 1621, 1, 2, '执行', '2022-05-24 18:08:24.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1781, '暂停', 1621, 1, 2, '暂停', '2022-05-24 18:08:24.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1783, '重试', 1621, 1, 2, '重试', '2022-05-24 18:08:25.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1785, '取消', 1621, 1, 2, '取消', '2022-05-24 18:08:25.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1787, '查看日志（子任务）', 1621, 1, 2, '查看日志（子任务）', '2022-05-24 18:08:25.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1789, '重试（子任务）', 1621, 1, 2, '重试（子任务）', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1791, '忽略（子任务）', 1621, 1, 2, '忽略（子任务）', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1793, '查看详情（DCDR）', 1621, 1, 2, '查看详情（DCDR）', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1795, '取消（DCDR）', 1621, 1, 2, '取消（DCDR）', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1797, '重试（DCDR）', 1621, 1, 2, '重试（DCDR）', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1799, '强切（DCDR）', 1621, 1, 2, '强切（DCDR）', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1801, '返回（DCDR）', 1621, 1, 2, '返回（DCDR）', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1803, '查看任务列表', 1623, 1, 2, '查看任务列表', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1805, '查看日志', 1623, 1, 2, '查看日志', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1807, '执行', 1623, 1, 2, '执行', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1809, '暂停', 1623, 1, 2, '暂停', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1811, '查看调度日志列表', 1625, 1, 2, '查看调度日志列表', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1813, '调度详情', 1625, 1, 2, '调度详情', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1815, '执行日志', 1625, 1, 2, '执行日志', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1817, '终止任务', 1625, 1, 2, '终止任务', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1819, '查看用户列表', 1627, 1, 2, '查看用户列表', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1821, '分配角色', 1627, 1, 2, '分配角色', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1823, '查看角色列表', 1629, 1, 2, '查看角色列表', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1825, '编辑', 1629, 1, 2, '编辑', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1827, '绑定用户', 1629, 1, 2, '绑定用户', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1829, '回收用户', 1629, 1, 2, '回收用户', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1831, '删除角色', 1629, 1, 2, '删除角色', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1833, '查看应用列表', 1631, 1, 2, '查看应用列表', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1835, '新建应用', 1631, 1, 2, '新建应用', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1837, '编辑', 1631, 1, 2, '编辑', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1839, '删除', 1631, 1, 2, '删除', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1841, '访问设置', 1631, 1, 2, '访问设置', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1843, '查看平台配置列表', 1633, 1, 2, '查看平台配置列表', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1845, '新增平台配置', 1633, 1, 2, '新增平台配置', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1847, '禁用平台配置', 1633, 1, 2, '禁用平台配置', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1849, '编辑平台配置', 1633, 1, 2, '编辑平台配置', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1851, '删除平台配置', 1633, 1, 2, '删除平台配置', '2022-05-24 18:08:26.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1853, '查看操作记录列表', 1635, 1, 2, '查看操作记录列表', '2022-05-24 18:08:26.0', '2022-05-24 18:23:34.0', 0, 'know_search'),
        (1855, 'Kibana', 1609, 1, 2, 'Kibana', '2022-05-24 18:08:26.0', '2022-06-14 16:44:02.0', 0, 'know_search'),
        (1857, 'SQL查询', 1609, 1, 2, 'SQL查询', '2022-05-24 18:08:26.0', '2022-06-14 16:44:02.0', 0, 'know_search'),
        (1859, '批量修改限流值', 1759, 1, 2, '批量修改限流值', '2022-05-24 18:08:26.0', '2022-08-11 10:37:13.0', 0, 'know_search'),
        (1861, '禁用', 1759, 1, 2, '禁用', '2022-05-24 18:08:26.0', '2022-08-11 10:37:13.0', 0, 'know_search'),
        (1863, '修改限流值', 1759, 1, 2, '修改限流值', '2022-05-24 18:08:26.0', '2022-08-11 10:37:13.0', 0, 'know_search'),
        (1865, '查看异常查询列表', 1611, 1, 2, '查看异常查询列表', '2022-05-24 18:08:26.0', '2022-06-14 16:44:02.0', 0, 'know_search'),
        (1867, '查看慢查询列表', 1611, 1, 2, '查看慢查询列表', '2022-05-24 18:08:26.0', '2022-06-14 16:44:21.0', 0, 'know_search'),
        (1869, '新增角色', 1629, 1, 2, '新增角色', '2022-05-24 18:08:26.0', '2022-05-24 18:23:34.0', 0, 'know_search'),
        (1871, 'Dashboard', 0, 0, 1, '查看dashboard', '2022-05-24 18:08:26.0', '2022-08-27 17:35:50.0', 0, 'know_search'),
        (1873, '新建索引', 1605, 1, 2, '新建索引', '2022-05-24 18:08:26.0', '2022-05-24 18:23:34.0', 0, 'know_search'),
        (1875, '查看dashboard', 1871, 1, 2, '查看dashboard', '2022-05-24 18:08:24.0', '2022-08-27 17:35:50.0', 0,
         'know_search');
#角色初始化数据
insert into kf_security_role (id, role_code, role_name, description, last_reviser, create_time, update_time,
                                is_delete, app_name)
values (1, 'r14715628', '管理员', '管理员', 'admin', '2022-06-01 21:19:42.0', '2022-07-06 22:23:59.0', 0,
        'know_search'),
       (2, 'r14481382', '资源 owner', '普通用户拥有的最大权限', 'admin', '2022-06-14 18:08:56.0',
        '2022-07-06 20:36:31.0', 0, 'know_search');
#初始化用户
insert into kf_security_user (id, user_name, pw, salt, real_name, phone, email, dept_id, is_delete,
                                create_time, update_time, app_name)
values (1, 'admin',
        'V1ZkU2RHRlhOSGhOYWs0M1VVWmFjVk5xVW1oaE0zUmlTVEJCZUZGRFRtUm1WVzh5VlcxNGMyRkZRamw3UUZacVNqUmhhM3RiSTBBeVFDTmRmVW8yVW14c2FFQjl7QFZqSjRha3tbI0AzQCNdfUo2UmxsaEB9Mv{#cdRgJ45Lqx}3IubEW87!==',
        '', 'admin', '18888888888', 'admin@12345.com', null, 0, '2022-05-26 05:46:12.0', '2022-08-26 09:06:19.0',
        'know_search');
#初始化用户和角色的关系
insert into kf_security_user_role (id, user_id, role_id, create_time, update_time, is_delete, app_name)
values (1, 1, 2, '2022-08-26 19:54:22.0', '2022-08-26 19:54:22.0', 0, 'know_search'),
       (2, 1, 1, '2022-08-30 21:05:17.0', '2022-08-30 21:05:17.0', 0, 'know_search');
#项目和项目配置、es user 的关系
insert into project_arius_config (project_id, analyze_response_enable, is_source_separated, aggr_analyze_enable,
                                  dsl_analyze_enable, slow_query_times, is_active, memo, create_time, update_time)
values (1, 1, 0, 1, 1, 1000, 1, '超级应用', '2022-06-14 18:52:08.0', '2022-08-27 23:13:14.0'),
       (2, 1, 0, 1, 1, 1000, 1, '元数据模版应用 不可以被删除', '2022-08-25 11:18:45.0', '2022-08-25 11:18:45.0');
insert into kf_security_project (id, project_code, project_name, description, dept_id, running, create_time,
                                   update_time, is_delete, app_name)
values (1, 'p14000143', 'SuperApp', '超级应用', 0, 1, '2022-05-26 05:49:08.0', '2022-08-24 11:09:49.0', 0,
        'know_search'),
       (2, 'p18461793', '元数据模版应用_误删', '元数据模版应用 不可以被删除', 0, 1, '2022-08-25 11:06:04.0',
        '2022-08-25 11:18:45.0', 0, 'know_search');
insert into arius_es_user (id, index_exp, data_center, is_root, memo, ip, verify_code, is_active,
                           query_threshold, cluster, responsible, search_type, create_time, update_time,
                           project_id, is_default_display)
values (1, null, 'cn', 1, '管理员 APP', '', 'azAWiJhxkho33ac', 1, 100, '',
        'admin', 1,
        '2022-05-26 09:35:38.0', '2022-06-23 00:16:47.0', 1, 1),
       (2, null, 'cn', 0, '元数据模版 APP', '', 'vkDgPEfD3jQJ1YY', 1, 1000, '', 'admin', 1, '2022-07-05 08:16:17.0',
        '2022-08-25 21:48:58.0', 2, 1);


## 配置初始化数据
TRUNCATE  table `arius_config_info`;
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (187, 'arius.cache.switch', 'logic.template.cache.enable', 'true', 1, -1, -1, '逻辑模板缓存是否开启', '2021-09-01 20:37:47', '2021-11-29 14:57:47', '2021-09-01 20:37:47');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (189, 'arius.cache.switch', 'physical.template.cache.enable', 'true', 1, -1, -1, '获取物理模板列表是否开启全局缓存', '2021-09-01 20:41:22', '2021-11-29 14:57:45', '2021-09-01 20:41:22');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (191, 'arius.cache.switch', 'cluster.phy.cache.enable', 'true', 1, -1, -1, '获取物理集群列表是否开启全局缓存', '2021-09-01 20:42:31', '2021-11-29 14:57:42', '2021-09-01 20:42:31');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (193, 'arius.cache.switch', 'cluster.logic.cache.enable', 'true', 1, -1, -1, '获取逻辑集群列表是否开启全局缓存', '2021-09-01 20:43:08', '2021-11-29 14:57:39', '2021-09-01 20:43:08');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1217, 'arius.meta.monitor', 'nodestat.collect.concurrent', 'true', 1, -1, -1, '', '2021-11-18 20:24:54', '2021-11-19 16:05:39', '2021-11-18 20:24:54');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1223, 'arius.common.group', 'app.default.read.auth.indices', '\"\"', 1, -1, 2, 'app可读写的权限', '2021-12-15 20:17:06', '2021-12-16 11:17:26', '2021-12-15 20:17:06');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1225, 'arius.common.group', 'delete.expire.index.ahead.clusters', '\"\"', 1, -1, 2, '删除过期权限', '2021-12-15 20:17:48', '2021-12-16 11:17:24', '2021-12-15 20:17:48');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1227, 'arius.common.group', 'operate.index.ahead.seconds', '2 * 60 * 60', 1, -1, 2, '索引操作提前时间', '2021-12-15 20:18:37', '2021-12-16 11:17:22', '2021-12-15 20:18:37');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1229, 'arius.common.group', 'platform.govern.admin.hot.days', '-1', 1, -1, 2, '平台治理导入热存的天数', '2021-12-15 20:19:13', '2021-12-16 11:17:19', '2021-12-15 20:19:13');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1231, 'arius.common.group', 'quota.dynamic.limit.black.appIds', 'none', 1, -1, 2, 'appid黑名单控制', '2021-12-15 20:20:11', '2021-12-16 11:17:17', '2021-12-15 20:20:11');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1233, 'arius.common.group', 'quota.dynamic.limit.black.cluster', '\"\"', 1, -1, 2, 'cluster黑名单控制', '2021-12-15 20:20:39', '2021-12-16 11:17:15', '2021-12-15 20:20:39');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1235, 'arius.common.group', 'quota.dynamic.limit.black.logicId', 'none', 1, -1, 2, '模板黑名单控制', '2021-12-15 20:21:21', '2021-12-16 11:17:12', '2021-12-15 20:21:21');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1237, 'arius.common.group', 'arius.wo.auto.process.create.template.disk.maxG', '10.0', 1, -1, 2, '模板创建时设置的磁盘空间最大值', '2021-12-15 20:21:49', '2021-12-16 11:15:12', '2021-12-15 20:21:49');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1239, 'arius.common.group', 'request.interceptor.switch.open', 'true', 1, -1, 2, '请求拦截开关', '2021-12-15 20:22:14', '2021-12-16 11:15:10', '2021-12-15 20:22:14');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1241, 'arius.common.group', 'arius.didi.t2.leader.mail', '\"\"', 1, -1, 2, 'didi领导者邮箱', '2021-12-15 20:22:40', '2021-12-16 11:15:07', '2021-12-15 20:22:40');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1243, 'arius.common.group', 'defaultDay', '\"\"', 1, -1, 2, '默认hotDay值', '2021-12-15 20:23:17', '2021-12-16 11:15:04', '2021-12-15 20:23:17');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1245, 'arius.quota.config.group', 'arius.quota.config.tps.per.cpu.with.replica', '1000.0', 1, -1, 2, '资源管控cpu项', '2021-12-15 20:23:56', '2021-12-16 11:15:01', '2021-12-15 20:23:56');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1247, 'arius.quota.config.group', 'arius.quota.config.tps.per.cpu.NO.replica', '2300.0', 1, -1, 2, '资源管控cpu项', '2021-12-15 20:24:27', '2021-12-16 11:14:58', '2021-12-15 20:24:27');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1249, 'arius.quota.config.group', 'arius.quota.config.cost.per.g.per.month', '1.06', 1, -1, 2, '资源配置模板费用', '2021-12-15 20:24:59', '2021-12-16 11:14:56', '2021-12-15 20:24:59');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1251, 'arius.meta.monitor.group', 'nodestat.collect.concurrent', 'fasle', 1, -1, 2, '节点状态信息是否并行采集', '2021-12-15 20:25:35', '2022-08-26 18:10:50', '2021-12-15 20:25:35');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1253, 'arius.meta.monitor.group', 'indexstat.collect.concurrent', 'fasle', 1, -1, 2, '索引状态信息是否并行采集', '2021-12-15 20:26:00', '2022-08-26 18:10:45', '2021-12-15 20:26:00');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1255, 'arius.common.group', 'indices.recovery.ceph_max_bytes_per_sec', '10MB', 1, -1, 2, '单节点分片恢复的速率', '2021-12-15 21:33:29', '2022-04-08 17:43:14', '2021-12-15 21:33:29');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1257, 'arius.common.group', 'cluster.routing.allocation.node_concurrent_incoming_recoveries', '2', 1, -1, 2, '一个节点上允许多少并发的传入分片还原,表示为传入还原', '2021-12-16 14:41:51', '2021-12-16 14:42:24', '2021-12-16 14:41:51');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1259, 'arius.common', 'cluster.routing.allocation.node_concurrent_outgoing_recoveries', '2', 1, -1, 2, '一个节点上允许多少并发的传入分片还原,传出还原', '2021-12-16 14:42:15', '2022-02-22 11:11:48', '2021-12-16 14:42:15');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1585, 'test.test', 'testt', '21', 1, -1, -1, '请忽略2221', '2022-01-13 14:25:40', '2022-01-15 16:27:05', '2022-01-13 14:25:40');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1587, 'zptest', 'test', '<script>alert(1)</script>', 1, -1, -1, 'alert(1)', '2022-01-18 16:14:12', '2022-01-18 16:15:49', '2022-01-18 16:14:12');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1589, 'test1ddd', 'dd ddd', 'dssdddd', 1, -1, -1, 'sddsdssd', '2022-01-26 11:39:23', '2022-01-26 11:39:42', '2022-01-26 11:39:23');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1591, 'yyftemptest-01s', 'yyftemptest-01d', '', 1, -1, -1, '', '2022-03-01 16:44:12', '2022-03-01 16:44:39', '2022-03-01 16:44:12');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1593, 'test1', 's', '', 1, -1, -1, '', '2022-03-07 11:37:39', '2022-03-07 11:37:43', '2022-03-07 11:37:39');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1595, 'test1', '22', 'm1qaz2wsx3edc4rfv5tgb6yhn7ujm1qaz2wsx3edc4rfv5tgb6yhn7ujm1qaz2wsx3edc4rfv5tgb6yhn7ujm1qaz2', 1, -1, -1, '', '2022-03-15 11:19:49', '2022-03-15 11:20:08', '2022-03-15 11:19:49');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1623, 'settingGroup', 'name', 'value', 1, -1, -1, 'test', '2022-06-23 14:17:56', '2022-06-23 15:47:26', '2022-06-23 14:17:56');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1625, 'group11', 'name1', 'value1', 1, -1, -1, 'des-edit', '2022-06-23 15:22:51', '2022-06-24 09:40:51', '2022-06-23 15:22:51');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1627, 'arius.common.group', 'cluster.node.specification_list', '16c-64g-3072g,16c-48g-3071g,1c-48g-3071g,', 1, -1, 1, '节点规格列表，机型列表', '2022-07-05 14:10:27', '2022-07-18 15:01:29', '2022-07-05 14:10:27');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1629, 'ccccccccccccccdcdccccccccccccccdcdccccccccccccccdb', 'dccccccccccccccdcdcccccccc', 'vjh', 1, -1, -1, 'cdcdccccccccccccccdcdccccccccccccccdcdccccccccccccccdcdccccccccccccccdcdccccccccccccccdcdccccc', '2022-07-05 15:27:38', '2022-07-05 15:28:09', '2022-07-05 15:27:38');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1631, '2', '3', '', 1, -1, -1, '', '2022-07-06 15:26:45', '2022-07-06 15:26:58', '2022-07-06 15:26:45');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1633, 'arius.common.group', 'cluster.data.center_list', 'cn,en', 1, -1, 1, '数据中心列表', '2022-07-06 16:14:03', '2022-08-27 19:11:25', '2022-07-06 16:14:03');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1635, 'arius.common.group', 'cluster.package.version_list', '7.6.1.1,6.6.6.6,7.6.1.2', 1, -1, 1, '系统预制支持的版本', '2022-07-06 16:17:25', '2022-07-06 16:17:25', '2022-07-06 16:17:25');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1637, 'template.time.type', 'format', '[\n  \"yyyy-MM-dd HH:mm:ss\",\n  \"yyyy-MM-dd HH:mm:ss.SSS\",\n  \"yyyy-MM-dd\'T\'HH:mm:ss\",\n  \"yyyy-MM-dd\'T\'HH:mm:ss.SSS\",\n  \"yyyy-MM-dd HH:mm:ss.SSS Z\",\n  \"yyyy/MM/dd HH:mm:ss\",\n  \"epoch_seconds\",\n  \"epoch_millis\"\n]', 1, -1, 1, '新建模版的时间格式', '2022-07-07 16:15:37', '2022-07-07 16:15:37', '2022-07-07 16:15:37');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1639, 'arius.cluster.blacklist', 'cluster.phy.name', 'didi-cluster-test', 1, -1, 1, '滴滴内部测试环境集群, 禁止任何编辑删除新增操作', '2022-07-07 17:58:02', '2022-07-07 18:44:42', '2022-07-07 17:58:02');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1641, 'arius.common.group', 'cluster.resource.type_list', '信创,acs,vmware', 1, -1, 1, '所属资源类型列表,IaaS平台类型列表', '2022-07-07 19:13:13', '2022-08-31 16:38:37', '2022-07-07 19:13:13');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1643, '55', '666', '1', 1, -1, -1, '143', '2022-07-13 16:59:41', '2022-07-13 17:01:48', '2022-07-13 16:59:41');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1645, 'arius.common.group', 'index.rollover.threshold', '0.00001', 1, -1, 1, '主分片大小达到1G后升版本', '2022-07-15 21:03:12', '2022-09-22 15:28:54', '2022-07-15 21:03:12');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1647, 'yyftemptest-01', 'yyf', 'sdv', 1, -1, -1, 'sdv', '2022-07-18 15:02:08', '2022-07-18 15:02:24', '2022-07-18 15:02:08');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1649, 'arius.common.group', 'cluster.node.count_list', '2,4,6,10', 1, -1, 1, '集群节点个数列表', '2022-07-18 15:22:33', '2022-08-27 19:13:09', '2022-07-18 15:22:33');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1653, 'arius.common.group', 'arius.system.template', '[\n    \"arius.dsl.analyze.result\",\n    \"arius.dsl.metrics\",\n    \"arius.dsl.template\",\n    \"arius.gateway.join\",\n    \"arius_stats_index_info\",\n    \"arius_stats_node_info\",\n    \"arius.template.access\",\n    \"arius_cat_index_info\",\n    \"arius_gateway_metrics\",\n    \"arius_stats_cluster_info\",\n    \"arius_stats_cluster_task_info\",\n    \"arius_stats_dashboard_info\",\n    \"arius.appid.template.access\"\n]', 1, -1, 1, '系统核心模版集合', '2022-07-21 12:25:48', '2022-07-21 12:30:06', '2022-07-21 12:25:48');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1655, 'ds12', 'sd34', 'sdsddsd', 1, -1, -1, 'ds78', '2022-07-21 17:00:44', '2022-08-01 08:52:35', '2022-07-21 17:00:44');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1657, 'arius.common.group', 'cluster.shard.big_threshold', '10', 1, -1, 1, '用于设置集群看板中的大Shard阈值，单位为gb，大于这个值就认为是大shard', '2022-07-28 17:49:59', '2022-08-26 18:08:56', '2022-07-28 17:49:59');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1671, 'arius.template.group', 'logic.template.business_type', '系统数据,日志数据,业务上报数据,test_businesss_type1,RDS数据,离线导入数据,testset,123,test_businesss_type1', 1, -1, 1, '模板业务类型', '2022-08-26 18:02:47', '2022-09-01 15:16:11', '2022-08-26 18:02:47');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1673, 'arius.template.group', 'logic.template.time_format_list', 'yyyy-MM-dd HH:mm:ss,yyyy-MM-dd HH:mm:ss.SSS,yyyy-MM-dd HH:mm:ss.SSS Z,yyyy-MM-dd\'T\'HH:mm:ss,yyyy-MM-dd\'T\'HH:mm:ss.SSS,yyyy-MM-dd\'T\'HH:mm:ssZ,yyyy-MM-dd\'T\'HH:mm:ss.SSSZ,yyyy/MM/dd HH:mm:ss,epoch_second,epoch_millis,yyyy-MM-dd', 1, -1, 1, '模板时间格式列表', '2022-08-26 18:06:07', '2022-08-31 17:16:03', '2022-08-26 18:06:07');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1675, 'arius.template.group', 'history.template.physic.indices.allocation.is_effective', 'ture', 1, -1, 1, '历史索引模板shard分配是否自动调整', '2022-08-26 18:07:53', '2022-08-31 17:07:02', '2022-08-26 18:07:53');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1677, 'arius.common.group', 'operate.record.save.time', '29', 1, -1, -1, '操作记录的保存时间', '2022-09-01 16:44:03', '2022-09-01 17:23:48', '2022-09-01 16:44:03');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1679, 'arius.common.group', 'operate.record.save_time', '25', 1, -1, 1, '操作记录的保存时间(天)', '2022-09-01 19:34:33', '2022-09-19 15:14:59', '2022-09-01 19:34:33');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1681, 'arius.common.group', 'super_app.default.dsl.command', '#获取节点状态\nGET _nodes/stats\n\n#获取集群信息\nGET _cluster/stats\n\n#获取集群健康信息\nGET _cluster/health?v\n\n#查看当前集群的热点线程\nGET _nodes/hot_threads\n\n#查看当前集群运行中的任务信息\nGET _tasks?actions=*&detailed\n\n#shard分配说明，会在分片未分配的事后去通过这个命令查看下具体原因\nGET /_cluster/allocation/explain\n\n#异常shard分配重试，当集群red有shard未分配的情况下会通过这个命令来重试分配\nPOST /_cluster/reroute?retry_failed=true\n\n#清除fielddata内存，当集群因为fileddata太大导致熔断或占用很多内存，可以通过此命令释放内存\nPOST _cache/clear?fielddata=true\n', 1, -1, 1, '超级应用默认就有的命令', '2022-09-20 10:26:08', '2022-09-26 11:34:14', '2022-09-20 10:26:08');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1683, 'arius.common.group', 'operate.record.save.num', '30', 1, -1, -1, 'DSL和kibana操作记录保存条数', '2022-09-20 10:45:31', '2022-09-22 16:49:16', '2022-09-20 10:45:31');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1685, 'ddd', 'sdd', 'sd', 1, -1, -1, 'ds', '2022-09-21 15:22:31', '2022-09-21 15:23:00', '2022-09-21 15:22:31');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1607, 'arius.dashboard.threshold.group', 'index.segment.num_threshold', '{\"name\":\" 索引 Segments 个数 \",\"metrics\":\"segmentNum\",\"unit\":\" 个 \",\"compare\":\">\",\"value\":100}', 1, -1, 1, '索引 Segment 个数阈值定义', '2022-06-17 09:52:11', '2022-08-27 16:05:06', '2022-06-17 09:52:11');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1609, 'arius.dashboard.threshold.group', 'index.template.segment_num_threshold', '{\"name\":\" 模板 Segments 个数 \",\"metrics\":\"segmentNum\",\"unit\":\" 个 \",\"compare\":\">\",\"value\":700}', 1, -1, 1, '索引模板 [Segment 个数阈值] 定义', '2022-06-17 09:53:34', '2022-08-27 19:01:57', '2022-06-17 09:53:34');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1611, 'arius.dashboard.threshold.group', 'index.segment.memory_size_threshold', '{\"name\":\" 索引 Segments 内存大小 \",\"metrics\":\"segmentMemSize\",\"unit\":\"MB\",\"compare\":\">\",\"value\":500}', 1, -1, 1, '索引 [Segment 内存大小阈值] 定义', '2022-06-17 09:54:20', '2022-10-26 18:50:50', '2022-06-17 09:54:20');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1613, 'arius.dashboard.threshold.group', 'index.template.segment_memory_size_threshold', '{\"name\":\" 模板 Segments 内存大小 \",\"metrics\":\"segmentMemSize\",\"unit\":\"MB\",\"compare\":\">\",\"value\":3000}', 1, -1, 1, '索引模板 [Segment 内存大小阈值] 定义', '2022-06-17 09:54:50', '2022-10-26 18:50:27', '2022-06-17 09:54:50');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1617, 'arius.dashboard.threshold.group', 'node.shard.num_threshold', '{\"name\":\" 节点分片个数 \",\"metrics\":\"shardNum\",\"unit\":\" 个 \",\"compare\":\">\",\"value\":1000}', 1, -1, 1, '节点 [分片个数阈值] 定义', '2022-06-17 10:01:40', '2022-08-27 19:09:44', '2022-06-17 10:01:40');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1619, 'arius.dashboard.threshold.group', 'index.shard.small_threshold', '{\"name\":\" 小 shard 索引列表 \",\"metrics\":\"shardSize\",\"unit\":\"MB\",\"compare\":\"<\",\"value\":1000}', 1, -1, 1, '索引 [小 Shard 阈值] 定义', '2022-06-17 16:11:53', '2022-08-27 19:04:19', '2022-06-17 16:11:53');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1656, 'arius.dashboard.threshold.group', 'index.mapping.num_threshold', '{\"name\":\" 索引 Mapping 个数 \",\"metrics\":\"mappingNum\",\"unit\":\" 个 \",\"compare\":\">\",\"value\":100}', 1, -1, 1, '索引 [Mapping 个数阈值] 定义', '2022-07-28 15:50:59', '2022-08-27 18:36:48', '2022-07-28 15:50:59');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1659, 'arius.dashboard.threshold.group', 'cluster.shard.num_threshold', '{\"name\":\" 集群 shard 个数 \",\"metrics\":\"shardNum\",\"unit\":\" 个 \",\"compare\":\">\",\"value\":10000}', 1, -1, 1, '集群 [Shard 个数阈值] 定义', '2022-08-05 15:58:22', '2022-10-27 12:00:25', '2022-08-05 15:58:22');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1661, 'arius.dashboard.threshold.group', 'cluster.metric.collector.delayed_threshold', '{\"name\":\"node_status 指标采集延时 \",\"metrics\":\"clusterElapsedTimeGte5Min\",\"unit\":\"MIN\",\"compare\":\">\",\"value\":5}', 1, -1, 1, '集群 [指标采集延时阈值] 定义', '2022-08-10 14:10:47', '2022-10-26 16:20:17', '2022-08-10 14:10:47');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1663, 'arius.dashboard.threshold.group', 'node.disk.used_percent_threshold', '{\"name\":\" 磁盘利用率 \",\"metrics\":\"largeDiskUsage\",\"unit\":\"%\",\"compare\":\">\",\"value\":80}', 1, -1, 1, '节点 [磁盘利用率阈值] 定义', '2022-08-25 14:50:41', '2022-10-26 18:48:54', '2022-08-25 14:50:41');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1665, 'arius.dashboard.threshold.group', 'node.jvm.heap.used_percent_threshold', '{\"name\":\" 堆内存利用率 \",\"metrics\":\"largeHead\",\"unit\":\"%\",\"compare\":\">\",\"value\":75}', 1, -1, 1, '节点 [堆内存利用率阈值] 定义', '2022-08-25 16:45:33', '2022-10-26 18:48:40', '2022-08-25 16:45:33');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1666, 'arius.dashboard.threshold.group', 'node.cpu.used_percent_threshold', '{\"name\":\"CPU 利用率红线 \",\"metrics\":\"largeCpuUsage\",\"unit\":\"%\",\"compare\":\">\",\"value\":60}', 1, -1, 1, '节点 [CPU 利用率阈值] 定义', '2022-08-25 16:45:33', '2022-10-26 18:48:18', '2022-08-25 16:45:33');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1667, 'arius.dashboard.threshold.group', 'node.jvm.heap.used_percent_time_duration_threshold', '{\"name\":\"node.jvm.heap.used_percent_threshold_time_duration\",\"metrics\":\"jvmHeapUsedPercentThresholdTimeDuration\",\"unit\":\"MIN\",\"compare\":\">\",\"value\":10}', 1, -1, 1, '节点堆内存利用率阈值的 [持续时间]', '2022-08-25 16:45:33', '2022-10-26 18:47:52', '2022-08-25 16:45:33');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1668, 'arius.dashboard.threshold.group', 'node.cpu.used_percent_threshold_time_duration_threshold', '{\"name\":\"node.large.cpu.used.percent.time.threshold\",\"metrics\":\"largeCpuUsage\",\"unit\":\"MIN\",\"compare\":\">\",\"value\":5}', 1, -1, 1, '节点 CPU 利用率超阈值的 [持续时间]', '2022-08-25 16:45:33', '2022-10-26 18:47:21', '2022-08-25 16:45:33');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1669, 'arius.dashboard.threshold.group', 'index.shard.big_threshold', '{\"name\":\"index.shard.big_threshold\",\"metrics\":\"shardSize\",\"unit\":\"G\",\"compare\":\">\",\"value\":20}', 1, -1, 1, '索引 [大 shard 阈值] 定义', '2022-08-26 15:25:07', '2022-08-29 10:28:24', '2022-08-26 15:25:07');



INSERT INTO `es_package` (`id`, `url`, `es_version`, `creator`, `release`, `manifest`, `desc`, `create_time`, `update_time`, `delete_flag`) VALUES (1, 'https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-5.6.16.tar.gz', '5.X', 'admin', 0, '4', '5.X社区开源版本', '2022-07-12 10:59:32', '2022-07-12 10:59:32', 0);
INSERT INTO `es_package` (`id`, `url`, `es_version`, `creator`, `release`, `manifest`, `desc`, `create_time`, `update_time`, `delete_flag`) VALUES (2, 'https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-6.8.23.tar.gz', '6.X', 'admin', 0, '4', '6.X社区开源版本', '2022-07-12 10:59:32', '2022-12-27 16:06:30', 0);
INSERT INTO `es_package` (`id`, `url`, `es_version`, `creator`, `release`, `manifest`, `desc`, `create_time`, `update_time`, `delete_flag`) VALUES (3, 'https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-7.17.8-linux-x86_64.tar.gz', '7.X', 'admin', 0, '4', '7.X社区开源版本', '2022-07-12 10:59:32', '2022-12-27 16:06:29', 0);
INSERT INTO `es_package` (`id`, `url`, `es_version`, `creator`, `release`, `manifest`, `desc`, `create_time`, `update_time`, `delete_flag`) VALUES (4, 'https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-8.5.3-linux-x86_64.tar.gz', '8.X', 'admin', 0, '4', '8.X社区开源版本', '2022-07-12 10:59:32', '2022-12-27 15:50:37', 0);
INSERT INTO `es_package` (`id`, `url`, `es_version`, `creator`, `release`, `manifest`, `desc`, `create_time`, `update_time`, `delete_flag`) VALUES (5, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/6.6.1.0-4.tar.gz', '6.6.1.903', 'admin', 0, '4', '6.6.1.903滴滴内部版本', '2022-07-12 10:59:32', '2022-12-27 15:48:23', 0);
INSERT INTO `es_package` (`id`, `url`, `es_version`, `creator`, `release`, `manifest`, `desc`, `create_time`, `update_time`, `delete_flag`) VALUES (6, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/elasticsearch.tar.gz', '7.6.0.1401', 'admin', 0, '4', '7.6.0.1401滴滴内部版本', '2022-07-12 10:59:32', '2022-07-12 10:59:32', 0);

/*
0.3.1原始sql
UPDATE kf_security_permission SET permission_name = 'Kibana', parent_id = 1609, leaf = 1, level = 2, description = 'Kibana', create_time = '2022-05-24 18:08:26.0', update_time = '2022-06-14 16:44:02.0', is_delete = 0, app_name = 'know_search' WHERE id = 1855;
UPDATE kf_security_permission SET permission_name = 'SQL查询', parent_id = 1609, leaf = 1, level = 2, description = 'SQL查询', create_time = '2022-05-24 18:08:26.0', update_time = '2022-06-14 16:44:02.0', is_delete = 0, app_name = 'know_search' WHERE id = 1857;
UPDATE kf_security_permission SET permission_name = 'DSL查询', parent_id = 1609, leaf = 1, level = 2, description = 'DSL查询', create_time = '2022-05-24 18:08:24.0', update_time = '2022-06-14 16:39:48.0', is_delete = 0, app_name = 'know_search' WHERE id = 1757;
UPDATE kf_security_role_permission SET role_id = 1, permission_id = 1609, create_time = '2022-06-14 17:41:03.0', update_time = '2022-08-25 10:31:42.0', is_delete = 0, app_name = 'know_search' WHERE id = 2059;
UPDATE kf_security_role_permission SET role_id = 1, permission_id = 1757, create_time = '2022-06-14 17:41:03.0', update_time = '2022-08-25 10:31:42.0', is_delete = 0, app_name = 'know_search' WHERE id = 2061;
UPDATE kf_security_role_permission SET role_id = 1, permission_id = 1855, create_time = '2022-06-14 17:41:03.0', update_time = '2022-08-25 10:34:13.0', is_delete = 0, app_name = 'know_search' WHERE id = 2063;
UPDATE kf_security_role_permission SET role_id = 1, permission_id = 1857, create_time = '2022-06-14 17:41:03.0', update_time = '2022-08-25 10:31:42.0', is_delete = 0, app_name = 'know_search' WHERE id = 2065;
UPDATE kf_security_role_permission SET role_id = 2, permission_id = 1609, create_time = '2022-06-14 18:08:56.0', update_time = '2022-08-25 10:31:42.0', is_delete = 0, app_name = 'know_search' WHERE id = 2241;
UPDATE kf_security_role_permission SET role_id = 2, permission_id = 1757, create_time = '2022-06-14 18:08:56.0', update_time = '2022-08-25 10:31:42.0', is_delete = 0, app_name = 'know_search' WHERE id = 2243;
UPDATE kf_security_role_permission SET role_id = 2, permission_id = 1855, create_time = '2022-06-14 18:08:56.0', update_time = '2022-08-25 10:33:12.0', is_delete = 1, app_name = 'know_search' WHERE id = 2245;
UPDATE kf_security_role_permission SET role_id = 2, permission_id = 1857, create_time = '2022-06-14 18:08:56.0', update_time = '2022-08-25 10:31:42.0', is_delete = 0, app_name = 'know_search' WHERE id = 2247;
*/
# 0.3.1.1变更sql
# 1.更新level和leaf
UPDATE kf_security_permission SET permission_name = 'DSL查询', parent_id = 0, leaf = 0, level = 1, description = 'DSL查询', create_time = '2022-05-24 18:08:24.0', update_time = '2022-09-02 19:01:17.0', is_delete = 0, app_name = 'know_search' WHERE id = 1757;
UPDATE kf_security_permission SET permission_name = 'Kibana', parent_id = 0, leaf = 0, level = 1, description = 'Kibana', create_time = '2022-05-24 18:08:26.0', update_time = '2022-09-02 19:01:17.0', is_delete = 0, app_name = 'know_search' WHERE id = 1855;
UPDATE kf_security_permission SET permission_name = 'SQL查询', parent_id = 0, leaf = 0, level = 1, description = 'SQL查询', create_time = '2022-05-24 18:08:26.0', update_time = '2022-09-02 19:01:17.0', is_delete = 0, app_name = 'know_search' WHERE id = 1857;
#1.1修改kf_security_role_permission
UPDATE kf_security_role_permission SET role_id = 1, permission_id = 1609, create_time = '2022-06-14 17:41:03.0', update_time = '2022-09-02 19:04:07.0', is_delete = 1, app_name = 'know_search' WHERE id = 2059;
UPDATE kf_security_role_permission SET role_id = 1, permission_id = 1757, create_time = '2022-06-14 17:41:03.0', update_time = '2022-08-25 10:31:42.0', is_delete = 0, app_name = 'know_search' WHERE id = 2061;
UPDATE kf_security_role_permission SET role_id = 1, permission_id = 1855, create_time = '2022-06-14 17:41:03.0', update_time = '2022-08-25 10:34:13.0', is_delete = 0, app_name = 'know_search' WHERE id = 2063;
UPDATE kf_security_role_permission SET role_id = 1, permission_id = 1857, create_time = '2022-06-14 17:41:03.0', update_time = '2022-08-25 10:31:42.0', is_delete = 0, app_name = 'know_search' WHERE id = 2065;
UPDATE kf_security_role_permission SET role_id = 2, permission_id = 1609, create_time = '2022-06-14 18:08:56.0', update_time = '2022-09-02 19:04:07.0', is_delete = 1, app_name = 'know_search' WHERE id = 2241;
UPDATE kf_security_role_permission SET role_id = 2, permission_id = 1757, create_time = '2022-06-14 18:08:56.0', update_time = '2022-08-25 10:31:42.0', is_delete = 0, app_name = 'know_search' WHERE id = 2243;
UPDATE kf_security_role_permission SET role_id = 2, permission_id = 1855, create_time = '2022-06-14 18:08:56.0', update_time = '2022-08-25 10:33:12.0', is_delete = 1, app_name = 'know_search' WHERE id = 2245;
UPDATE kf_security_role_permission SET role_id = 2, permission_id = 1857, create_time = '2022-06-14 18:08:56.0', update_time = '2022-08-25 10:31:42.0', is_delete = 0, app_name = 'know_search' WHERE id = 2247;
#2.更新name
UPDATE kf_security_permission SET permission_name = 'Kibana查询', parent_id = 0, leaf = 0, level = 1, description = 'Kibana查询', create_time = '2022-05-24 18:08:26.0', update_time = '2022-09-05 14:19:29.0', is_delete = 0, app_name = 'know_search' WHERE id = 1855;
#3.新增3个权限点
INSERT INTO kf_security_permission (permission_name, parent_id, leaf, level, description, create_time, update_time, is_delete, app_name) VALUES ('DSL', 0, 0, 1, 'DSL', '2022-05-24 18:08:24.0', '2022-09-02 19:01:17.0', 0, 'know_search');
INSERT INTO kf_security_permission (permission_name, parent_id, leaf, level, description, create_time, update_time, is_delete, app_name) VALUES ('Kibana', 0, 0, 1, 'Kibana', '2022-05-24 18:08:26.0', '2022-09-02 19:01:17.0', 0, 'know_search');
INSERT INTO kf_security_permission (permission_name, parent_id, leaf, level, description, create_time, update_time, is_delete, app_name) VALUES ('SQL', 0, 0, 1, 'SQL', '2022-05-24 18:08:26.0', '2022-09-02 19:01:17.0', 0, 'know_search');
#3.1 新增kf_security_role_permission
insert into kf_security_role_permission(role_id, permission_id, is_delete, app_name)
values (1, 1877, 0, 'know_search'),
       (1, 1879, 1, 'know_search'),
       (1, 1881, 0, 'know_search'),
       (2, 1877, 0, 'know_search'),
       (2, 1879, 1, 'know_search'),
       (2, 1881, 0, 'know_search');

#4.再次更新level和leaf
UPDATE kf_security_permission SET permission_name = 'DSL查询', parent_id = 1877, leaf = 1, level = 2, description = 'DSL查询' WHERE id = 1757;
UPDATE kf_security_permission SET permission_name = 'Kibana查询', parent_id = 1879, leaf = 1, level = 2, description = 'Kibana查询' WHERE id = 1855;
UPDATE kf_security_permission SET permission_name = 'SQL查询', parent_id = 1881, leaf = 1, level = 2, description = 'SQL查询' WHERE id = 1857;

#5.用户和应用配置信息表
alter table user_metrics_config_info rename to user_config_info;
alter table user_config_info COMMENT '用户和应用配置信息表';
alter table `user_config_info` change COLUMN metric_info config_info text COMMENT '用户下某个应用的配置';
alter table `user_config_info` add column project_id int(10) NOT NULL DEFAULT '-1' COMMENT '项目 id' after user_name;
alter table `user_config_info` add column config_type int(10) NOT NULL DEFAULT '1' COMMENT '配置类型,1- 指标看板和 dashboard，2- 查询模板列表' after project_id;
truncate table user_config_info;

INSERT INTO `user_config_info`(`user_name`, `project_id`, `config_type`, `config_info`)
select DISTINCT t1.user_name,t2.project_id,2,
                concat('[{\"firstUserConfigType\":\"searchQuery\",\"projectId\":',t2.project_id,',\"secondUserConfigType\":\"searchTemplate\",\"userConfigTypes\":[\"totalCostAvg\",\"totalShardsAvg\"],\"userName\":\"',t1.user_name,'\"}]') as config_info
from kf_security_user t1 inner join
     kf_security_user_project t2 on t1.id=t2.user_id and t2.is_delete='0'
where  t1.is_delete='0';
-- ----------------------------
-- Table structure for metric_dictionary_info
-- ----------------------------
DROP TABLE IF EXISTS `metric_dictionary_info`;
CREATE TABLE `metric_dictionary_info`  (
                                           `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT,
                                           `type` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '指标分类',
                                           `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '指标名称',
                                           `price` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '-1' COMMENT '指标价值',
                                           `interval` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '1' COMMENT '计算间隔',
                                           `current_cal_logic` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '当前计算逻辑',
                                           `is_gold` tinyint(1) UNSIGNED ZEROFILL NULL DEFAULT 0 COMMENT '是否黄金指标(0否1是)',
                                           `unit` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '单位',
                                           `interactive_form` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '交互形式',
                                           `is_warning` tinyint(1) UNSIGNED ZEROFILL NULL DEFAULT 0 COMMENT '告警指标(0否1是)',
                                           `source` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '指标来源',
                                           `tags` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '指标标签',
                                           `model` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '模块',
                                           `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                           `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                           `is_active` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否生效',
                                           `is_threshold` tinyint(1) UNSIGNED ZEROFILL NOT NULL DEFAULT 0 COMMENT '是否有阈值',
                                           `threshold` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '阈值',
                                           `metric_type` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '阈值信息',
                                           PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5754 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '用户和应用配置信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of metric_dictionary_info
-- ----------------------------
INSERT INTO `metric_dictionary_info` VALUES (4915, '集群统计', '集群状态、shard总数、索引模板总数、文档总数、索引数、节点分配（Master节点数/Data节点数/Client节点数）、堆内存总量（已用内存、空闲内存）、磁盘总量（已用磁盘、空闲磁盘）、节点总数（活跃节点数、死亡节点数）、集群索引存储量、未分配Shard数', '集群当前运行状态概览信息', '当前值', '索引模板总数: /_template命令获取的数组大小\n通过GET _cluster/stats命令直接获取\n  集群状态：status\n  shard总数：shards.total\n  文档总数：indices.docs.count\n  索引数:indices.count\n  Master节点数:nodes.count.master\n  Data节点数:nodes.count.data \n  Client节点数:nodes.count.total - nodes.count.master-nodes.count.data\n  堆内存总量:nodes.os.mem.total_in_bytes\n  已用内存:nodes.os.mem.used_in_bytes\n  空闲内存:nodes.os.mem.free_percent\n  磁盘总量:nodes.fs.total_in_bytes\n  已用磁盘:nodes.fs.total_in_bytes - nodes.fs.free_in_bytes\n  空闲磁盘:nodes.fs.free_in_bytes\n  节点总数:nodes.count.total\n  活跃节点数，死亡节点数:查询配置的集群节点列表后和ES集群节点ip列表进行匹配，可以匹配上的为活跃节点 \n  集群索引存储量：indices.store.size_in_bytes  ->  新增字段indicesStoreSize\n未分配Shard数：通过 _cat/health?format=json 获取unassign字段 -> 新增字段unassignedShardNum', 1, NULL, '状态栏', 1, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-09-29 09:20:01', 1, 0, NULL, 'basic');
INSERT INTO `metric_dictionary_info` VALUES (4917, '系统指标', 'CPU使用率（平均分位值、99分位值、95分位值、75分位值、55分位值）', '集群CPU使用率与均衡情况观察', '当前值', '[当前值] 集群下所有节点，通过GET _nodes/stats命令获取nodes.{nodeName}.os.cpu.percent ，根据分位进行聚合', 1, '%', '折线图', 1, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-10-24 09:49:28', 1, 0, NULL, 'cpuUsage');
INSERT INTO `metric_dictionary_info` VALUES (4919, '系统指标', 'CPU 1分钟负载（平均分位值、99分位值、95分位值、75分位值、55分位值）', '集群1分钟负载与均衡情况观察', '当前值', '[当前值] 集群下所有节点，通过GET _nodes/stats命令获取nodes.{nodeName}.os.cpu.load_average.1m，根据分位进行聚合', 0, NULL, '折线图', 0, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-10-24 09:49:30', 1, 0, NULL, 'cpuLoad1M');
INSERT INTO `metric_dictionary_info` VALUES (4921, '系统指标', '磁盘使用率（平均分位值、99分位值、95分位值、75分位值、55分位值）', '集群磁盘利用率与均衡情况观察', '当前值', '[当前值]集群下所有节点，通过GET _nodes/stats命令获取（nodes.{nodeName}.fs.total.total_in_bytes - nodes.{nodeName}.fs.total.free_in_bytes）/nodes.{nodeName}.fs.total.total_in_bytes，根据分位进行聚合', 0, '%', '折线图', 1, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-09-29 09:20:16', 1, 0, NULL, 'diskUsage');
INSERT INTO `metric_dictionary_info` VALUES (4923, '系统指标', '磁盘使用情况（磁盘空闲量/磁盘使用量/磁盘总量）', '集群磁盘使用情况概览', '当前值', '通过GET _cluster/stats命令获取\n磁盘总量 : nodes.{nodeName}.fs.total_in_bytes\n磁盘使用量 : nodes.{nodeName}.fs.total_in_bytes - nodes.fs.free_in_bytes\n磁盘空闲量 : nodes.{nodeName}.fs.free_in_bytes', 0, 'GB', '折线图', 0, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-09-29 11:06:06', 1, 0, NULL, 'diskInfo');
INSERT INTO `metric_dictionary_info` VALUES (4925, '系统指标', '网络出口流量', '集群网络出口流量/网络入口流量走势', '当前值', '[当前值] 集群下的所有节点，通过GET _nodes/stats命令获取nodes.{nodeName}.transport.rx_size_in_bytes、nodes.{nodeName}.transport.tx_size_in_bytes（接收、发送）的累加值', 0, 'MB/S', '折线图', 0, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-10-24 09:47:08', 1, 0, NULL, 'sendTransSize');
INSERT INTO `metric_dictionary_info` VALUES (4927, '系统指标', '指标采集失败率', '集群指标采集失败率（采集失败次数/采集窗口大小）', '60S', NULL, 1, '%', '折线图', 1, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-09-28 11:31:36', 1, 0, NULL, NULL);
INSERT INTO `metric_dictionary_info` VALUES (4929, '性能指标', '执行任务耗时（平均分位值、99分位值、95分位值、75分位值、55分位值）', '集群节点任务执行耗时与均衡情况观察', '当前值', '[当前值] _cat/tasks?v&detailed&format=json命令获取结果集中running_time字段的分位值', 0, 'S', '折线图', 0, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-10-10 16:00:21', 1, 0, NULL, 'taskCost');
INSERT INTO `metric_dictionary_info` VALUES (4931, '性能指标', '执行任务数量', '集群全部节点每秒执行任务数走势', '当前值', '[当前值] _cat/tasks?v&detailed&format=json命令获取结果集的大小', 0, '个/S', '折线图', 0, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-09-29 11:06:07', 1, 0, NULL, 'taskCount');
INSERT INTO `metric_dictionary_info` VALUES (4932, '系统指标', '网络入口流量', '集群网络出口流量/网络入口流量走势', '当前值', '[当前值] 集群下的所有节点，通过GET _nodes/stats命令获取nodes.{nodeName}.transport.rx_size_in_bytes、nodes.{nodeName}.transport.tx_size_in_bytes（接收、发送）的累加值', 0, 'MB/S', '折线图', 0, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-10-24 09:47:11', 1, 0, NULL, 'recvTransSize');
INSERT INTO `metric_dictionary_info` VALUES (4933, '性能指标', '查询QPS', '集群Shard级别查询并发量概览', '60S', '[累加值]  集群下的所有节点，间隔时间内通过GET _nodes/stats命令获取nodes.{nodeName}.indices.search.query_total的差值累加值/间隔时间', 1, '次/S', '折线图', 0, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-11-02 19:17:58', 1, 0, NULL, 'readTps');
INSERT INTO `metric_dictionary_info` VALUES (4935, '性能指标', '写入TPS', '集群Shard级别写入并发量概览', '60S', '[累加值] 集群下的所有节点，间隔时间内通过GET _nodes/stats命令获取nodes.{nodeName}.indices.indexing.index_total的差值累加值/间隔时间', 1, '个/S', '折线图', 0, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-10-25 15:47:20', 1, 0, NULL, 'writeTps');
INSERT INTO `metric_dictionary_info` VALUES (4937, '性能指标', '查询耗时', '集群Shard级别查询耗时概览', '60S', '[最大值] 集群下的所有节点,间隔时间内通过_node/stats命令获取nodes.{nodeName}.indices.search.query_time_in_millis差值累加值/节点间隔时间内nodes.{nodeName}.indices.search.query_total差值累加值', 1, 'MS', '折线图', 0, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-11-02 19:25:44', 1, 0, NULL, 'searchLatency');
INSERT INTO `metric_dictionary_info` VALUES (4939, '性能指标', '写入耗时', '集群文档级别写入耗时概览', '60S', '[最大值] 集群下的所有节点,间隔时间内通过_node/stats命令获取nodes.{nodeName}.indices.indexing.index_time_in_millis差值累加值/节点间隔时间内nodes.{nodeName}.indices.indexing.index_total差值累加值', 1, 'MS', '折线图', 0, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-11-02 19:25:17', 1, 0, NULL, 'indexingLatency');
INSERT INTO `metric_dictionary_info` VALUES (4941, '性能指标', '网关写入TPS', '通过网关每秒写入集群的写入请求次数', '60S', NULL, 0, '次/S', '折线图', 0, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-09-29 11:06:08', 1, 0, NULL, NULL);
INSERT INTO `metric_dictionary_info` VALUES (4943, '性能指标', '网关写入吞吐量', '通过网关每秒写入集群的文档字节数', '60S', NULL, 1, 'MB/S', '折线图', 0, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-09-29 11:06:09', 1, 0, NULL, NULL);
INSERT INTO `metric_dictionary_info` VALUES (4945, '性能指标', '网关写入请求耗时', '通过网关写入请求的平均耗时', '60S', NULL, 1, 'MS', '折线图', 0, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-09-29 11:06:09', 1, 0, NULL, NULL);
INSERT INTO `metric_dictionary_info` VALUES (4947, '性能指标', '网关写入请求响应体大小', '通过网关写入请求的响应体平均大小', '60S', NULL, 0, 'B', '折线图', 0, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-09-29 11:06:09', 1, 0, NULL, NULL);
INSERT INTO `metric_dictionary_info` VALUES (4949, '性能指标', '网关查询QPS', '通过网关每秒查询的请求次数', '60S', NULL, 1, '次/S', '折线图', 0, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-09-29 11:06:09', 1, 0, NULL, NULL);
INSERT INTO `metric_dictionary_info` VALUES (4951, '性能指标', '网关查询请求耗时', '通过网关查询请求的平均耗时', '60S', NULL, 1, 'MS', '折线图', 0, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-09-29 11:06:10', 1, 0, NULL, NULL);
INSERT INTO `metric_dictionary_info` VALUES (4953, '性能指标', '网关单次查询命中Shard数', '通过网关查询请求的命中Shard平均数', '60S', NULL, 0, '个', '折线图', 0, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-09-29 11:06:10', 1, 0, NULL, NULL);
INSERT INTO `metric_dictionary_info` VALUES (4955, '状态指标', '迁移中shard列表', '集群Shard迁移感知', '当前值', '[当前值] 通过GET _cat/recovery?v&h=i,s,t,st,shost,thost&active_only=true命令获取的集合详情', 0, NULL, '列表展示（index,source_host,target_host,bytes_recovered bytes_percen,translog_ops_percentt）', 0, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-09-29 11:06:10', 1, 0, NULL, 'movingShards');
INSERT INTO `metric_dictionary_info` VALUES (4957, '状态指标', '未分配Shard列表', '集群节点掉线感知', '当前值', '[当前值]通过GET _cat/shards?format=json命令获取state=UNASSIGN的shard列表', 0, NULL, '列表项（index，shard，prirep，state）', 0, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-09-30 08:56:42', 1, 0, NULL, 'unAssignShards');
INSERT INTO `metric_dictionary_info` VALUES (4959, '状态指标', 'Dead节点列表', '', '当前值', '[当前值] 通过GET _nodes命令获取集群节点信息，与平台集群节点列表对比缺失的节点', 0, NULL, '列表项（节点IP、主机名、实例名）', 1, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-09-29 09:42:21', 1, 0, NULL, 'invalidNodes');
INSERT INTO `metric_dictionary_info` VALUES (4961, '状态指标', 'PendingTask列表', '集群PengingTask感知', '当前值', '[当前值] 通过/_cluster/pending_tasks命令获取的集合', 0, NULL, '列表项（插入顺序、优先级、任务来源、执行任务前等待时间）', 1, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-09-29 09:42:38', 1, 0, NULL, 'pendingTasks');
INSERT INTO `metric_dictionary_info` VALUES (4963, '系统指标', 'CPU利用率', 'CPU使用率，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取nodes.{nodeName}.os.cpu.percent字段', 1, '%', '折线图', 1, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:42:08', 1, 0, NULL, 'os-cpu-percent');
INSERT INTO `metric_dictionary_info` VALUES (4965, '系统指标', '磁盘空闲率', '磁盘空闲率，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取nodes.{nodeName}.fs.total.free_in_bytes/nodes.{nodeName}.fs.total.total_in_bytes', 1, '%', '折线图', 1, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:36:11', 1, 0, NULL, 'fs-total-disk_free_percent');
INSERT INTO `metric_dictionary_info` VALUES (4967, '系统指标', '网络发送流量', '网络包为单位的每秒发送流量，Top节点趋势', '当前值', '[平均值]，间隔时间内通过GET _nodes/stats命令获取nodes.{nodeName}.transport.tx_size_in_bytes的差值/时间间隔', 0, 'MB/S', '折线图', 1, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-24 09:53:56', 1, 0, NULL, 'transport-tx_size_in_bytes_rate');
INSERT INTO `metric_dictionary_info` VALUES (4969, '系统指标', '网络接收流量', '网络包为单位的接收流量，Top节点趋势', '当前值', '[平均值]，间隔时间内通过GET _nodes/stats命令获取nodes.{nodeName}.transport.rx_size_in_bytes的差值/时间间隔', 0, 'MB/S', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-24 09:54:17', 1, 0, NULL, 'transport-rx_size_in_bytes_rate');
INSERT INTO `metric_dictionary_info` VALUES (4971, '系统指标', 'CPU近1分钟负载', 'CPU近1分钟负载，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取nodes.{nodeName}.os.cpu.load_average.1m', 0, NULL, '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:36:49', 1, 0, NULL, 'os-cpu-load_average-1m');
INSERT INTO `metric_dictionary_info` VALUES (4973, '基本性能指标', '写入TPS', '节点索引写入速率平均值，Top节点趋势', '60S', '[平均值] (当前时刻减去上一时刻通过GET _nodes/stats命令获取nodes.{nodeName}.indices.indexing.index_total的差值)/时间间隔(S)', 1, '个/S', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-29 09:45:25', 1, 0, NULL, 'indices-indexing-index_total_rate');
INSERT INTO `metric_dictionary_info` VALUES (4975, '基本性能指标', '网关查询QPS', '网关通过ClientNode节点每秒查询的请求数，Top节点趋势', '60S', NULL, 0, '次/S', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-28 11:31:36', 1, 0, NULL, NULL);
INSERT INTO `metric_dictionary_info` VALUES (4977, '基本性能指标', '网关写入TPS', '网关通过ClientNode节点每秒写入的请求数，Top节点趋势', '60S', NULL, 0, '次/S', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-28 11:31:36', 1, 0, NULL, NULL);
INSERT INTO `metric_dictionary_info` VALUES (4979, '基本性能指标', '网关写入吞吐量', '网关通过ClientNode节点每秒写入的吞吐量，Top节点趋势', '60S', NULL, 0, 'MB/S', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 11:10:44', 1, 0, NULL, 'collectorDelayed');
INSERT INTO `metric_dictionary_info` VALUES (4981, '基本性能指标', '写入耗时', '节点索引写入耗时平均值，Top节点趋势', '60S', '[平均值],间隔时间内通过GET _nodes/stats命令获取nodes.{nodeName}.indices.indexing.index_time_in_millis的差值/,间隔时间内nodes.{nodeName}.indices.docs.count的差值', 1, 'MS', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 11:06:05', 1, 0, NULL, 'indices-indexing-index_time_per_doc');
INSERT INTO `metric_dictionary_info` VALUES (4983, '基本性能指标', 'Query QPS', '节点索引Query速率平均值，Top节点趋势', '60S', '[平均值],间隔时间内通过GET _nodes/stats命令获取nodes.{nodeName}.indices.search.query_total的差值/时间间隔', 1, '次/S', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-29 09:45:49', 1, 0, NULL, 'indices-search-query_total_rate');
INSERT INTO `metric_dictionary_info` VALUES (4985, '基本性能指标', 'Fetch QPS', '节点索引Fetch速率平均值，Top节点趋势', '60S', '[平均值] ,间隔时间内通过GET _nodes/stats命令获取nodes.{nodeName}.indices.search.fetch_total的差值/时间间隔', 0, '次/S', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-29 09:46:00', 1, 0, NULL, 'indices-search-fetch_total_rate');
INSERT INTO `metric_dictionary_info` VALUES (4987, '基本性能指标', 'Query耗时', '节点索引Query耗时平均值，Top节点趋势', '60S', '[平均值],间隔时间内通过GET _nodes/stats命令获取nodes.{nodeName}.indices.search.query_time_in_millis的差值/,间隔时间内nodes.{nodeName}.indices.search.query_total的差值', 1, 'MS', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:37:36', 1, 0, NULL, 'indices-search-query_time_per_query');
INSERT INTO `metric_dictionary_info` VALUES (4989, '基本性能指标', 'Fetch耗时', '节点索引Fetch耗时平均值，Top节点趋势', '60S', '[平均值] ,间隔时间内通过GET _nodes/stats命令获取nodes.{nodeName}.indices.search.fetch_time_in_millis的差值/,间隔时间内nodes.{nodeName}.indices.search.fetch_total的差值', 0, 'MS', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:37:46', 1, 0, NULL, 'indices-search-fetch_time_per_fetch');
INSERT INTO `metric_dictionary_info` VALUES (4991, '基本性能指标', 'Scroll当下请求量', '节点索引Scroll请求量，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取nodes.{nodeName}.indices.search.scroll_current的值', 0, '个', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:37:56', 1, 0, NULL, 'indices-search-scroll_current');
INSERT INTO `metric_dictionary_info` VALUES (4993, '基本性能指标', 'Scroll请求耗时', '节点Scroll耗时平均值，Top节点趋势', '60S', '[平均值] ,间隔时间内通过GET _nodes/stats命令获取nodes.{nodeName}.indices.search.scroll_time_in_millis的差值/,间隔时间内nodes.{nodeName}.indices.search.scroll_total的差值', 0, 'MS', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:38:05', 1, 0, NULL, 'indices-search-scroll_avg_time');
INSERT INTO `metric_dictionary_info` VALUES (4995, '基本性能指标', 'Merge操作耗时', '节点Merge耗时平均值，Top节点趋势', '60S', '[平均值] 间隔时间内通过GET _nodes/stats命令获取nodes.{nodeName}.indices.merges.total_time_in_millis的差值/间隔时间内nodes.{nodeName}.indices.merges.total的差值', 1, 'MS', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:42:21', 1, 0, NULL, 'indices-merges_avg_time');
INSERT INTO `metric_dictionary_info` VALUES (4997, '基本性能指标', 'Refresh操作耗时', '节点Refresh耗时平均值，Top节点趋势', '60S', '[平均值]间隔时间内通过GET _nodes/stats命令获取nodes.{nodeName}.indices.refresh.total_time_in_millis的差值/间隔时间内nodes.{nodeName}.indices.refresh.total的差值', 1, 'MS', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:42:29', 1, 0, NULL, 'indices-refresh_avg_time');
INSERT INTO `metric_dictionary_info` VALUES (4999, '基本性能指标', 'Flush操作耗时', '节点Flush耗时平均值，Top节点趋势', '60S', '[平均值]间隔时间内通过GET _nodes/stats命令获取nodes.{nodeName}.indices.flush.total_time_in_millis的差值/间隔时间内nodes.{nodeName}.indices.flush.total的差值', 1, 'MS', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:45:15', 1, 0, NULL, 'indices-flush_avg_time');
INSERT INTO `metric_dictionary_info` VALUES (5000, '基本性能指标', 'request Cache eviction', '节点Request Cache缓存驱逐数，Top节点趋势', '当前值', '[差值] 间隔时间内通过GET _nodes/stats命令获取nodes.{nodeName}.indices.request_cache.evictions的差值/时间间隔(S)', 0, '次/MIN', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-24 09:57:08', 1, 0, NULL, 'indices-request_cache-evictions');
INSERT INTO `metric_dictionary_info` VALUES (5001, '基本性能指标', 'Write Rejected个数', '节点写入拒绝数，Top节点趋势', '60S', '[平均值]间隔时间内通过GET _nodes/stats命令获取nodes.{nodeName}.thread_pool.bulk.rejected的差值/时间间隔', 1, '个/MIN', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-29 10:00:54', 1, 0, NULL, 'thread_pool-bulk-rejected');
INSERT INTO `metric_dictionary_info` VALUES (5003, '基本性能指标', 'Write Queue个数', '节点写入队列堆积数，Top节点趋势', '当前值', '[当前值]  _nodes/stats命令获取nodes.{nodeName}.thread_pool.bulk.queue', 1, '个', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 11:05:43', 1, 0, NULL, 'thread_pool-bulk-queue');
INSERT INTO `metric_dictionary_info` VALUES (5005, '基本性能指标', 'Search Queue个数', '节点查询队列堆积数，Top节点趋势', '当前值', '[当前值]  _nodes/stats命令获取nodes.{nodeName}.thread_pool.search.queue', 1, '个', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:45:52', 1, 0, NULL, 'indices-search-query_total');
INSERT INTO `metric_dictionary_info` VALUES (5007, '基本性能指标', 'Search Rejected个数', '节点查询拒绝数，Top节点趋势', '60S', '[差值] 间隔时间内通过GET _nodes/stats命令获取nodes.{nodeName}.thread_pool.search.rejected的差值/时间间隔(MIN)', 1, '个/MIN', '折线图', 1, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-29 10:01:05', 1, 0, NULL, 'thread_pool-search-rejected');
INSERT INTO `metric_dictionary_info` VALUES (5009, '基本性能指标', 'Merge次数', '节点Merge次数，Top节点趋势', '60S', '[差值] 间隔时间内通过GET _nodes/stats命令获取nodes.{nodeName}.indices.merges.total的差值/时间间隔(MIN)', 0, '次/MIN', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:46:27', 1, 0, NULL, 'indices-merges-total');
INSERT INTO `metric_dictionary_info` VALUES (5011, '基本性能指标', 'Refresh次数', '节点Refresh次数，Top节点趋势', '60S', '[差值] 间隔时间内通过GET _nodes/stats命令获取nodes.{nodeName}.indices.refresh.total的差值/时间间隔(MIN)', 0, '次/MIN', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:46:40', 1, 0, NULL, 'indices-refresh-total');
INSERT INTO `metric_dictionary_info` VALUES (5013, '基本性能指标', 'Flush次数', '节点Flush次数，Top节点趋势', '60S', '[差值] 间隔时间内通过GET _nodes/stats命令获取nodes.{nodeName}.indices.flush.total的差值/时间间隔(MIN)', 0, '次/MIN', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:46:56', 1, 0, NULL, 'indices-flush-total');
INSERT INTO `metric_dictionary_info` VALUES (5015, '基本性能指标', 'Query Cache内存命中率', '节点Query Cache内存命中率，Top节点趋势', '', '[差值] 通过GET _nodes/stats命令获取nodes.{nodeName}.indices.query_cache.hit_count/nodes.{nodeName}.indices.query_cache.total_count', 0, '%', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-28 16:31:04', 1, 0, NULL, 'indices-query_cache-hit_rate');
INSERT INTO `metric_dictionary_info` VALUES (5017, '基本性能指标', 'Reques Cache内存命中率', '节点Reques Cache内存命中率，Top节点趋势', '', '[差值] 通过GET _nodes/stats命令获取nodes.{nodeName}.indices.request_cache.hit_count/(nodes.{nodeName}.indices.request_cache.hit_count+nodes.{nodeName}.indices.request_cache.miss_count)', 0, '%', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-28 16:31:09', 1, 0, NULL, 'indices-request_cache-hit_rate');
INSERT INTO `metric_dictionary_info` VALUES (5019, '内存大小指标', 'Query Cache内存大小', '节点所有Shard Query Cache(Cached Filters/...)堆内存汇总占用，不在Lucene内存占用统计范围内，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取nodes.{nodeName}.indices.query_cache.memory_size_in_bytes', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-10 10:54:22', 1, 0, NULL, 'indices-query_cache-memory_size_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5021, '内存大小指标', 'Request Cache内存大小', '节点所有Shard Request Cache(Cached Aggregation Results/...)堆内存汇总占用，不在Lucene内存占用统计范围内，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取nodes.{nodeName}.indices.request_cache.memory_size_in_bytes', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-10 10:54:40', 1, 0, NULL, 'indices-request_cache-memory_size_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5023, '高级性能指标', '未提交的Translog大小', '节点所有Shard未提交Translog的大小累加值，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取nodes.{nodeName}.indices.translog.uncommitted_size_in_bytes', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:47:50', 1, 0, NULL, 'indices-translog-uncommitted_size_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5025, '高级性能指标', 'Http活跃连接数', '节点的Http活跃连接数，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取nodes.{nodeName}.http.current_open', 1, '个', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:47:57', 1, 0, NULL, 'http-current_open');
INSERT INTO `metric_dictionary_info` VALUES (5027, '高级性能指标', 'Segement数 ', '节点所有Shard的Segment汇总数，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取nodes.{nodeName}.indices.segments.count', 1, '个', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:48:05', 1, 0, NULL, 'indices-segments-count');
INSERT INTO `metric_dictionary_info` VALUES (5029, '高级性能指标', 'Segement内存大小', '节点所有Shard的Segment底层Lucene内存汇总占用，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取nodes.{nodeName}.indices.segments.memory_in_bytes', 1, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:48:13', 1, 0, NULL, 'indices-segments-memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5031, '内存大小指标', 'Terms内存大小', '节点所有Shard的Segment底层Terms(Text/Keyword/...)内存汇总占用，是底层Lucene内存汇总占用的一个子项，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取nodes.{nodeName}.indices.segments.term_vectors_memory_in_bytes', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-10 10:53:27', 1, 0, NULL, 'indices-segments-term_vectors_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5033, '内存大小指标', 'Points内存大小', '节点所有Shard的Segment底层Points(Numbers/IPs/Geo/...)内存汇总占用，是底层Lucene内存汇总占用的一个子项，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取nodes.{nodeName}.indices.segments.points_memory_in_bytes', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-10 10:53:41', 1, 0, NULL, 'indices-segments-points_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5035, '内存大小指标', 'Doc Values内存大小', '节点所有Shard的Doc Values内存大小累加值，是底层Lucene内存汇总占用的一个子项，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取nodes.{nodeName}.indices.segments.doc_values_memory_in_bytes', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-10 10:53:57', 1, 0, NULL, 'indices-segments-doc_values_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5037, '内存大小指标', 'Index Writer内存大小', '节点所有Shard的Index Writer内存大小累加值，不在Lucene内存占用统计范围内,Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取nodes.{nodeName}.indices.segments.index_writer_memory_in_bytes', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-10 10:54:04', 1, 0, NULL, 'indices-segments-index_writer_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5039, '高级性能指标', '文档总数', '节点所有Shard索引文档数累加值，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取nodes.{nodeName}.indices.docs.count', 1, '个', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:49:31', 1, 0, NULL, 'indices-docs-count');
INSERT INTO `metric_dictionary_info` VALUES (5041, '高级性能指标', '总存储大小', '节点所有Shard索引存储大小累加值，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取nodes.{nodeName}.indices.store.size_in_bytes', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:49:38', 1, 0, NULL, 'indices-store-size_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5043, '高级性能指标', '执行任务耗时', '节点执行任务平均耗时，Top节点趋势', '当前值', '[平均值] _cat/tasks?v&detailed&format=json命令根据node筛选并获取running_time的平均值', 0, 'S', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:50:50', 1, 0, NULL, 'runningTime');
INSERT INTO `metric_dictionary_info` VALUES (5045, '高级性能指标', '执行任务数量', '节点执行任务总数量，Top节点趋势', '当前值', '[当前值] _cat/tasks?v&detailed&format=json命令根据node筛选并获取到的集合大小', 0, '个', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 11:05:00', 1, 0, NULL, 'taskId');
INSERT INTO `metric_dictionary_info` VALUES (5047, '内存大小指标', 'Stored Fields大小', '节点所有Shard Stored Fields(_source/...)内存大小累加值，是底层Lucene内存汇总占用的一个子项，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取nodes.{nodeName}.indices.segments.stored_fields_memory_in_bytes', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-10 10:54:50', 1, 0, NULL, 'indices-segments-stored_fields_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5049, '内存大小指标', 'Norms内存大小', '节点所有Shard Norms(normalization factors for query time/text scoring)内存大小累加值，是底层Lucene内存汇总占用的一个子项，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取indices.segments.norms_memory_in_bytes', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-10 10:54:59', 1, 0, NULL, 'indices-segments-norms_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5051, '内存大小指标', 'Version Map内存大小', '节点所有Shard Version Map(update/delete)内存大小累加值，不在Lucene内存占用统计范围内，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取indices.segments.version_map_memory_in_bytes', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-10 10:55:09', 1, 0, NULL, 'indices-segments-version_map_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5053, '内存大小指标', 'Fixed Bitsets内存大小', '节点所有Shard Fixed Bitsets(deeply nested object/...)内存大小累加值，是底层Lucene内存汇总占用的一个子项，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取indices.segments.fixed_bit_set_memory_in_bytes', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-10 10:55:20', 1, 0, NULL, 'indices-segments-fixed_bit_set_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5055, '内存大小指标', 'Fielddata内存大小', '节点所有Shard的Fielddata(global ordinals /enable fielddata on text field/...)内存汇总占用，不在Lucene内存占用统计范围内，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取indices.segments.breakers.fielddata.estimated_size_in_bytes', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-11-01 09:13:09', 1, 0, NULL, 'breakers-fielddata-estimated_size_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5057, '高级性能指标', '写入线程池queue数', '节点写入线程池队列当前值', '当前值', '[当前值] GET _nodes/stats命令获取thread_pool.write.queue', 0, '个', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-29 11:05:01', 1, 0, NULL, 'thread_pool-write-queue');
INSERT INTO `metric_dictionary_info` VALUES (5059, '高级性能指标', '查询线程池queue数', '节点查询线程池池队列当前值', '当前值', '[当前值] GET _nodes/stats命令获取thread_pool.search.queue', 0, '个', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-08 18:39:32', 1, 0, NULL, 'thread_pool-search-queue');
INSERT INTO `metric_dictionary_info` VALUES (5061, '高级性能指标', '刷新线程池queue数', '节点刷新线程池队列当前值', '当前值', '[当前值] GET _nodes/stats命令获取thread_pool.refresh.queue', 0, '个', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-29 11:05:01', 1, 0, NULL, 'thread_pool-refresh-queue');
INSERT INTO `metric_dictionary_info` VALUES (5063, '高级性能指标', '落盘线程池queue数', '节点落盘线程池队列当前值', '当前值', '[当前值] GET _nodes/stats命令获取thread_pool.rollup_indexing.queue', 0, '个', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-29 11:05:01', 1, 0, NULL, 'thread_pool-rollup_indexing-queue');
INSERT INTO `metric_dictionary_info` VALUES (5065, '高级性能指标', '管理线程池queue数', '节点管理线程池队列当前值', '当前值', '[当前值] GET _nodes/stats命令获取thread_pool.management.queue', 0, '个', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-29 11:05:01', 1, 0, NULL, 'thread_pool-management-queue');
INSERT INTO `metric_dictionary_info` VALUES (5067, '高级性能指标', '合并线程池queue数', '节点合并线程池队列当前值', '当前值', '[当前值] GET _nodes/stats命令获取thread_pool.force_merge.queue', 0, '个', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-29 11:05:01', 1, 0, NULL, 'thread_pool-force_merge-queue');
INSERT INTO `metric_dictionary_info` VALUES (5069, 'JVM指标', 'Young GC次数', '节点Young GC次数，Top节点趋势', '60S', '[平均值]当前时刻减去上一时刻通过GET _nodes/stats命令获取nodes.{nodeName}.jvm.gc.collectors.young.collection_count的值/时间间隔', 1, '次/S', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:41:40', 1, 0, NULL, 'jvm-gc-young-collection_count_rate');
INSERT INTO `metric_dictionary_info` VALUES (5071, 'JVM指标', 'Old GC次数', '节点Old GC次数，Top节点趋势', '60S', '[平均值]当前时刻减去上一时刻通过GET _nodes/stats命令获取nodes.{nodeName}.jvm.gc.collectors.old.collection_count的值/时间间隔', 1, '次/S', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:41:33', 1, 0, NULL, 'jvm-gc-old-collection_count_rate');
INSERT INTO `metric_dictionary_info` VALUES (5073, 'JVM指标', 'Young GC耗时', '节点Young GC平均耗时，Top节点趋势', '60S', '[差值]  间隔时间内通过GET _nodes/stats命令获取nodes.{nodeName}.jvm.gc.collectors.young.collection_time_in_millis的差值/ 间隔时间内nodes.{nodeName}.jvm.gc.collectors.young.collection_count的差值', 0, 'MS', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:41:26', 1, 0, NULL, 'jvm-gc-young-collection_avg_time');
INSERT INTO `metric_dictionary_info` VALUES (5075, 'JVM指标', 'Old GC耗时', '节点Old GC平均耗时，Top节点趋势', '60S', '[差值]  间隔时间内通过GET _nodes/stats命令获取nodes.{nodeName}.jvm.gc.collectors.old.collection_time_in_millis的差值/ 间隔时间内nodes.{nodeName}.jvm.gc.collectors.old.collection_count的差值', 0, 'MS', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:41:17', 1, 0, NULL, 'jvm-gc-old-collection_avg_time');
INSERT INTO `metric_dictionary_info` VALUES (5077, 'JVM指标', 'JVM堆内存使用量', '节点JVM堆内存使用量，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取nodes.{nodeName}.jvm.mem.heap_used_in_bytes', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:40:52', 1, 0, NULL, 'jvm-mem-heap_used_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5079, 'JVM指标', 'JVM堆外存使用量', '节点JVM堆外存使用量，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取nodes.{nodeName}.jvm.mem.non_heap_used_in_bytes', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:40:44', 1, 0, NULL, 'jvm-mem-non_heap_used_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5081, 'JVM指标', 'JVM堆使用率', '节点JVM堆使用率，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取nodes.{nodeName}.jvm.mem.heap_used_percent', 1, '%', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:40:18', 1, 0, NULL, 'jvm-mem-heap_used_percent');
INSERT INTO `metric_dictionary_info` VALUES (5083, 'JVM指标', '堆内存young区使用空间', '节点年轻代堆内存使用空间，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取nodes.{nodeName}.jvm.mem.pools.young.used_in_bytes', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:40:10', 1, 0, NULL, 'jvm-mem-pools-young-used_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5085, 'JVM指标', '堆内存old区使用空间', '节点老年代堆内存使用空间，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取nodes.{nodeName}.jvm.mem.pools.old.used_in_bytes', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:40:03', 1, 0, NULL, 'jvm-mem-pools-old-used_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5087, 'breaker指标', 'Field data circuit breaker 内存占用', '统计当前fielddata占用内存总大小，与阀值比较，超过则熔断请求', '当前值', '[当前值] GET _nodes/stats命令获取breakers.fielddata.limit_size_in_bytes[阈值]，breakers.fielddata.estimated_size_in_bytes[当前值]', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-18 14:16:52', 1, 0, NULL, 'breakers-fielddata-limit_size_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5089, 'breaker指标', 'Request circuit breaker 内存占用', '统计当前请求(比如聚合请求临时内存构建）占用内存总大小，与阀值比较，超过则熔断请求', '当前值', '[当前值] GET _nodes/stats命令获取breakers.request.limit_size_in_bytes[阈值]，breakers.request.estimated_size_in_bytes[当前值]', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-18 14:17:03', 1, 0, NULL, 'breakers-request-limit_size_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5090, '基本性能指标', 'query Cache evictions', '节点Query Cache缓存驱逐数，Top节点趋势', '当前值', '[差值] 间隔时间内通过GET _nodes/stats命令获取nodes.{nodeName}.indices.query_cache.evictions的差值/时间间隔(S)', 0, '次/MIN', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-24 09:56:51', 1, 0, NULL, 'indices-query_cache-evictions');
INSERT INTO `metric_dictionary_info` VALUES (5091, 'breaker指标', 'inflight requests circuit breaker 内存占用', '统计当前请求body占用内存总大小，与阀值比较，超过则熔断请求', '当前值', '[当前值] GET _nodes/stats命令获取breakers.in_flight_requests.limit_size_in_bytes[阈值]，breakers.in_flight_requests.estimated_size_in_bytes[当前值]', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-18 14:17:04', 1, 0, NULL, 'breakers-in_flight_requests-limit_size_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5093, 'breaker指标', 'Accounting requests circuit breaker 内存占用', '统计当前请求结束后不能释放的对象(例如segment常驻的内存占用)所占用内存总大小，与阀值比较，超过则熔断请求', '当前值', '[当前值] GET _nodes/stats命令获取breakers.accounting.limit_size_in_bytes[阈值]，breakers.accounting.estimated_size_in_bytes[当前值]', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-18 14:17:29', 1, 0, NULL, 'breakers-accounting-limit_size_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5095, 'breaker指标', 'Script compilation circuit breaker 编译次数', '统计一段时间内脚本编译次数，与阀值比较，超过则熔断请求', '当前值', '[平均值] 当前时刻减去上个时刻通过GET _nodes/stats命令获取script.compilations的数量差值/时间间隔MIN', 0, '次/MIN', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-11 14:58:57', 1, 0, NULL, 'script-compilations');
INSERT INTO `metric_dictionary_info` VALUES (5097, 'breaker指标', 'Parent circuit breaker JVM真实内存占用', '统计JVM真实内存占用，与阀值比较，超过则熔断请求', '当前值', '[当前值] GET _nodes/stats命令获取breakers.parent.limit_size_in_bytes[阈值]，breakers.parent.estimated_size_in_bytes[当前值]', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-18 14:17:33', 1, 0, NULL, 'breakers-parent-limit_size_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5099, '索引基础指标', '索引Shard数', '索引Shard个数，Top节点趋势', '当前值', '[当前值] {indexName}/_stats?level=shards命令获取_shards.total', 1, '个', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-09-29 11:06:16', 1, 0, NULL, 'shardNu');
INSERT INTO `metric_dictionary_info` VALUES (5101, '索引基础指标', '索引存储大小', '索引存储总大小，Top节点趋势', '当前值', '[当前值] {indexName}/_stats?level=shards命令获取_all.total.store.size_in_bytes', 0, 'GB', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-09-29 11:06:16', 1, 0, NULL, 'store-size_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5103, '索引基础指标', '文档总数', '索引的文档总数，Top节点趋势', '当前值', '[当前值] {indexName}/_stats?level=shards命令获取_all.total.docs.count', 1, '个', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-09-29 11:06:17', 1, 0, NULL, 'docs-count');
INSERT INTO `metric_dictionary_info` VALUES (5105, '索引性能指标', '写入TPS', '索引写入速率平均值，Top节点趋势', '60S', '[平均值] (间隔时间内通过{indexName}/_stats?level=shards命令获取_all.total.docs.count的差值)/时间间隔(S)', 1, '个/S', '折线', 1, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-09-29 09:23:43', 1, 0, NULL, 'indexing-index_total_rate');
INSERT INTO `metric_dictionary_info` VALUES (5107, '索引性能指标', '写入耗时', '索引写入耗时平均值，Top节点趋势', '60S', '[平均值] (间隔时间内通过{indexName}/_stats?level=shards命令获取_all.total.indexing.index_time_in_millis的差值)/ 间隔时间内_all.total.indexing.index_total的差值', 1, 'MS', '折线', 1, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-11-02 16:55:05', 1, 0, NULL, 'indices-indexing-index_time_per_doc');
INSERT INTO `metric_dictionary_info` VALUES (5109, '索引性能指标', '网关写入TPS', 'Index通过网关的每秒写入请求数', '60S', '[平均值] (间隔时间内通过查询网关索引获取命中写入条件总数的差值)/时间间隔(S)', 0, '次/S', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-09-29 11:06:17', 1, 0, NULL, NULL);
INSERT INTO `metric_dictionary_info` VALUES (5111, '索引性能指标', '网关写入耗时', 'Index通过网关的写入平均耗时', '60S', '[平均值]  间隔时间内通过查询网关索引获取命中写入条件耗时的平均值', 0, 'MS', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-09-29 11:06:17', 1, 0, NULL, NULL);
INSERT INTO `metric_dictionary_info` VALUES (5113, '索引性能指标', '网关查询QPS', 'Index通过网关的每秒查询请求量', '60S', '[平均值] (间隔时间内通过查询网关索引获取命中查询总数的差值)/时间间隔(S)', 0, '次/S', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-09-29 11:06:17', 1, 0, NULL, NULL);
INSERT INTO `metric_dictionary_info` VALUES (5115, '索引性能指标', '网关查询耗时', 'Index通过网关的查询平均耗时', '60S', '[平均值]  间隔时间内通过查询网关索引获取命中查询耗时的平均值', 0, 'MS', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-09-29 11:06:17', 1, 0, NULL, NULL);
INSERT INTO `metric_dictionary_info` VALUES (5117, '索引性能指标', '查询Query QPS', '索引Query速率平均值，Top节点趋势', '60S', '[平均值] (间隔时间内通过{indexName}/_stats?level=shards命令获取_all.total.search.query_total的差值)/时间间隔(S)', 1, '次/S', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-09-29 11:06:17', 1, 0, NULL, 'search-query_total_rate');
INSERT INTO `metric_dictionary_info` VALUES (5119, '索引性能指标', 'Fetch QPS', '索引Fetch速率平均值，Top节点趋势', '60S', '[平均值] (间隔时间内通过{indexName}/_stats?level=shards命令获取_all.total.search.fetch_total的差值)/时间间隔(S)', 0, '次/S', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-10-11 11:23:19', 1, 0, NULL, 'search-fetch_total_rate');
INSERT INTO `metric_dictionary_info` VALUES (5121, '索引性能指标', '查询Query耗时', '索引Query耗时平均值，Top节点趋势', '60S', '[平均值]  间隔时间内通过{indexName}/_stats?level=shards命令获取_all.total.search.query_time_in_millis的差值/ 间隔时间内_all.total.search.query_total的差值', 0, 'MS', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-09-30 11:14:56', 1, 0, NULL, 'cost-query_time_in_millis');
INSERT INTO `metric_dictionary_info` VALUES (5123, '索引性能指标', '查询Fetch耗时', '索引Fetch耗时平均值，Top节点趋势', '60S', '[平均值] 间隔时间内通过{indexName}/_stats?level=shards命令获取_all.total.search.fetch_time_in_millis的差值/ 间隔时间内_all.total.search.fetch_total的差值', 0, 'MS', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-09-30 11:15:06', 1, 0, NULL, 'cost-fetch_time_in_millis');
INSERT INTO `metric_dictionary_info` VALUES (5125, '索引性能指标', '查询Scroll量', '索引间隔时间内所有Shard Scroll请求量，Top节点趋势', '60S', '[平均值] (间隔时间内通过{indexName}/_stats?level=shards命令获取_all.total.search.scroll_total的差值)/时间间隔(S)', 0, '个', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-09-29 11:06:02', 1, 0, NULL, 'search-scroll_total_rate');
INSERT INTO `metric_dictionary_info` VALUES (5127, '索引性能指标', '查询Scroll耗时', '索引Scorll耗时平均值，Top节点趋势', '60S', '[平均值]  间隔时间内通过{indexName}/_stats?level=shards命令获取_all.total.search.scroll_time_in_millis的差值/ 间隔时间内_all.total.search.scroll_total的差值', 0, 'MS', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-09-30 11:19:31', 1, 0, NULL, 'cost-scroll_time_in_millis');
INSERT INTO `metric_dictionary_info` VALUES (5129, '索引性能指标', 'Merge耗时', '索引Merge耗时平均值，Top节点趋势', '60S', '[平均值]  间隔时间内通过{indexName}/_stats?level=shards命令获取_all.total.merges.total_time_in_millis的差值/ 间隔时间内_all.total.merges.total的差值', 0, 'MS', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-09-30 11:17:24', 1, 0, NULL, 'cost-merges-total_time_in_millis');
INSERT INTO `metric_dictionary_info` VALUES (5131, '索引性能指标', 'Refresh耗时', '索引Refresh耗时平均值，Top节点趋势', '60S', '[平均值]  间隔时间内通过{indexName}/_stats?level=shards命令获取_all.total.refresh.total_time_in_millis的差值/ 间隔时间内_all.total.refresh.total的差值', 0, 'MS', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-09-30 11:17:39', 1, 0, NULL, 'cost-refresh-total_time_in_millis');
INSERT INTO `metric_dictionary_info` VALUES (5133, '索引性能指标', 'Flush耗时', '索引Flush耗时平均值，Top节点趋势', '60S', '[平均值]  间隔时间内通过{indexName}/_stats?level=shards命令获取_all.total.indices.flush.total_time_in_millis的差值/ 间隔时间内_all.total.indices.flush.total的差值', 0, 'MS', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-09-30 11:18:08', 1, 0, NULL, 'cost-flush-total_time_in_millis');
INSERT INTO `metric_dictionary_info` VALUES (5135, '索引性能指标', 'Merge次数', '索引Merge次数，Top节点趋势', '60S', '[平均值] (间隔时间内通过{indexName}/_stats?level=shards命令获取_all.total.merges.total的差值)/时间间隔(MIN)', 0, '次/MIN', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-09-29 11:06:02', 1, 0, NULL, 'merges-total_rate');
INSERT INTO `metric_dictionary_info` VALUES (5137, '索引性能指标', 'Refresh次数', '索引Refresh次数，Top节点趋势', '60S', '[平均值] (间隔时间内通过{indexName}/_stats?level=shards命令获取_all.total.refresh.total的差值)/时间间隔(MIN)', 0, '次/MIN', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-09-29 11:06:02', 1, 0, NULL, 'refresh-total_rate');
INSERT INTO `metric_dictionary_info` VALUES (5139, '索引性能指标', 'Flush次数', '索引Flush次数，Top节点趋势', '60S', '[平均值] (间隔时间内通过{indexName}/_stats?level=shards命令获取flush.total的差值)/时间间隔(MIN)', 0, '次/MIN', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-09-29 11:06:03', 1, 0, NULL, 'flush-total_rate');
INSERT INTO `metric_dictionary_info` VALUES (5141, '索引内存指标', 'Segements大小', '索引所有Shard的Segment底层Lucene内存汇总占用，Top节点趋势', '当前值', '[当前值] {indexName}/_stats?level=shards命令获取_all.total.segments.memory_in_bytes', 1, 'MB', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-10-27 11:13:35', 1, 0, NULL, 'segments-memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5143, '索引内存指标', 'Terms内存大小', '索引所有Shard的Segment底层Terms(Text/Keyword/...)内存汇总占用，是底层Lucene内存汇总占用的一个子项，Top节点趋势', '当前值', '[当前值] {indexName}/_stats?level=shards命令获取_all.total.segments.term_vectors_memory_in_bytes', 0, 'MB', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-10-27 11:13:40', 1, 0, NULL, 'segments-term_vectors_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5145, '索引内存指标', 'Points内存大小', '索引所有Shard的Segment底层Points(Numbers/IPs/Geo/...)内存汇总占用，是底层Lucene内存汇总占用的一个子项，Top节点趋势', '当前值', '[当前值] {indexName}/_stats?level=shards命令获取_all.total.segments.points_memory_in_bytes', 0, 'MB', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-10-27 11:14:44', 1, 0, NULL, 'segments-points_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5147, '索引内存指标', 'Doc Values内存大小', '索引所有Shard的Doc Values内存大小，是底层Lucene内存汇总占用的一个子项，Top节点趋势', '当前值', '[当前值] {indexName}/_stats?level=shards命令获取_all.total.segments.doc_values_memory_in_bytes', 0, 'MB', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-10-27 11:15:01', 1, 0, NULL, 'segments-doc_values_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5149, '索引内存指标', 'Index Writer内存大小', '索引所有Shard的Index Writer内存大小，不在Lucene内存占用统计范围内,Top节点趋势', '当前值', '[当前值] {indexName}/_stats?level=shards命令获取_all.total.segments.index_writer_memory_in_bytes', 0, 'MB', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-10-27 11:15:06', 1, 0, NULL, 'segments-index_writer_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5151, '索引内存指标', '未提交的Translog大小', '索引所有Shard的未提交Translog的大小累加值，Top节点趋势', '当前值', '[当前值] {indexName}/_stats?level=shards命令获取_all.total.translog.uncommitted_size_in_bytes', 0, 'MB', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-10-27 11:15:10', 1, 0, NULL, 'translog-size_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5153, '索引内存指标', 'Query Cache内存大小', '索引所有Shard Query Cache(Cached Filters/...)堆内存汇总占用，不在Lucene内存占用统计范围内，Top节点趋势', '当前值', '[当前值] GET {indexName}_/stats?level=shards命令获取_all.total.query_cache.memory_size_in_bytes', 1, 'MB', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-11-02 20:54:35', 1, 0, NULL, 'query_cache-memory_size_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5155, '索引内存指标', 'Stored Fields大小', '索引stored_fields_memory内存大小', '当前值', '[当前值] GET {indexName}_/stats?level=shards命令获取_all.total.segments.stored_fields_memory_in_bytes', 0, 'MB', NULL, 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-11-02 20:54:40', 1, 0, NULL, 'segments-stored_fields_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5157, '索引内存指标', 'Norms内存大小', '索引所有Shard Norms(normalization factors for query time/text scoring)内存大小累加值，是底层Lucene内存汇总占用的一个子项，Top节点趋势', '当前值', '[当前值] GET {indexName}_/stats?level=shards命令获取_all.total.segments.norms_memory_in_bytes', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-11-02 20:54:42', 1, 0, NULL, 'segments-norms_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5159, '索引内存指标', 'Version Map内存大小', '索引所有Shard Version Map(update/delete)内存大小累加值，不在Lucene内存占用统计范围内，Top节点趋势', '当前值', '[当前值] GET {indexName}_/stats?level=shards命令获取_all.total.segments.version_map_memory_in_bytes', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-11-02 20:54:45', 1, 0, NULL, 'segments-version_map_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5161, '索引内存指标', 'Fixed Bitsets内存大小', '索引所有Shard Fixed Bitsets(deeply nested object/...)内存大小累加值，是底层Lucene内存汇总占用的一个子项，Top节点趋势', '当前值', '[当前值] GET {indexName}_/stats?level=shards命令获取_all.total.segments.fixed_bit_set_memory_in_bytes', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-11-02 20:54:49', 1, 0, NULL, 'segments-fixed_bit_set_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5163, '索引内存指标', 'Fielddata内存大小', '索引所有Shard Fielddata(global ordinals /enable fielddata on text field/...)内存汇总占用，不在Lucene内存占用统计范围内，Top节点趋势', '当前值', '[当前值] GET {indexName}_/stats?level=shards命令获取_all.total.fielddata.memory_size_in_bytes', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-11-02 20:54:54', 1, 0, NULL, 'fielddata-memory_size_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5165, '索引内存指标', 'Request Cache内存大小', '索引所有Shard Request Cache(Cached Aggregation Results/...)堆内存汇总占用，不在Lucene内存占用统计范围内，Top节点趋势', '当前值', '[当前值] GET {indexName}_/stats?level=shards命令获取_all.total.request_cache.memory_size_in_bytes', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-11-02 20:54:58', 1, 0, NULL, 'segments-request_cache-memory_size_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5167, '索引模板基础指标', '索引Shard数', '索引模板下索引Shard个数，Top节点趋势', '当前值', '[当前值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取shards数量之和', 1, '个', '折线', 0, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-09-29 11:06:05', 1, 0, NULL, 'shardNu');
INSERT INTO `metric_dictionary_info` VALUES (5169, '索引模板基础指标', '索引存储大小', '索引模板下索引Shard存储总大小，Top节点趋势', '当前值', '[当前值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.store.size_in_bytes之和', 0, 'GB', '折线', 0, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-10-27 11:15:43', 1, 0, NULL, 'store-size_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5171, '索引模板基础指标', '文档总数', '索引模板下索引的文档数，Top节点趋势', '当前值', '[当前值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.docs.count之和', 1, '个', '折线', 0, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-10-27 11:15:48', 1, 0, NULL, 'docs-count');
INSERT INTO `metric_dictionary_info` VALUES (5173, '索引模板性能指标', '写入TPS', '索引模板写入速率平均值，Top节点趋势', '60S', '[平均值] 间隔时间内模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.docs.count的差值累加值/间隔时间', 1, '个/S', '折线', 1, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-10-27 11:15:53', 1, 0, NULL, 'indexing-index_total_rate');
INSERT INTO `metric_dictionary_info` VALUES (5175, '索引模板性能指标', '写入耗时', '索引模板写入耗时平均值，Top节点趋势', '60S', '[平均值] 间隔时间内模板下所有的索引通过GET {indexName}/_stats?level=shards命令获取_all.total.indexing.index_time_in_millis的差值)/ 间隔时间内_all.total.indexing.index_total的差值', 1, 'MS', '折线', 1, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-11-02 16:58:37', 1, 0, NULL, 'indices-indexing-index_time_per_doc');
INSERT INTO `metric_dictionary_info` VALUES (5177, '索引模板性能指标', '查询Query QPS', '索引模板Query速率平均值，Top节点趋势', '60S', '[平均值] 间隔时间内模板下所有的索引通过{indexName}/_stats?level=shards命令获取 all.total.search.query_total的差值累加值/间隔时间', 1, '次/S', '折线', 0, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-10-27 11:16:07', 1, 0, NULL, 'search-query_total_rate');
INSERT INTO `metric_dictionary_info` VALUES (5179, '索引模板性能指标', '网关写入TPS', 'IndexTemplate所属Index通过网关的每秒写入请求数', '60S', NULL, 0, '次/S', '折线', 0, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-09-29 11:06:06', 1, 0, NULL, NULL);
INSERT INTO `metric_dictionary_info` VALUES (5181, '索引模板性能指标', '网关写入耗时', 'IndexTemplate所属Index通过网关的写入平均耗时', '60S', NULL, 0, 'MS', '折线', 0, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-09-29 11:06:06', 1, 0, NULL, NULL);
INSERT INTO `metric_dictionary_info` VALUES (5183, '索引模板性能指标', '网关查询QPS', 'IndexTemplate所属Index通过网关的每秒查询请求量', '60S', NULL, 0, '次/S', '折线', 0, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-09-29 11:06:06', 1, 0, NULL, NULL);
INSERT INTO `metric_dictionary_info` VALUES (5185, '索引模板性能指标', '网关查询耗时', 'IndexTemplate所属Index通过网关的查询平均耗时', '60S', NULL, 0, 'MS', '折线', 0, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-09-29 11:06:07', 1, 0, NULL, NULL);
INSERT INTO `metric_dictionary_info` VALUES (5187, '索引模板性能指标', '查询Fetch QPS', '索引模板Fetch速率平均值，Top节点趋势', '60S', '[平均值]  间隔时间内获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.search.fetch_total的差值累加值/时间间隔', 0, '次/S', '折线', 0, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-10-24 10:58:35', 1, 0, NULL, 'search-fetch_total_rate');
INSERT INTO `metric_dictionary_info` VALUES (5189, '索引模板性能指标', '查询Query耗时', '索引模板Query耗时平均值，Top节点趋势', '60S', '[平均值] 间隔时间内模板下所有的索引通过{indexName}/_stats?level=shards命令_all.total.search.query_time_in_millis的差值累加值/_all.total.search.query_total的差值累加值', 0, 'MS', '折线', 0, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-10-24 10:57:12', 1, 0, NULL, 'cost-query_time_in_millis');
INSERT INTO `metric_dictionary_info` VALUES (5191, '索引模板性能指标', '查询Fetch耗时', '索引模板Fetch耗时平均值，Top节点趋势', '60S', '[平均值] 间隔时间内获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.search.fetch_time_in_millis的差值累加值/_all.total.search.fetch_total的差值累加值', 0, 'MS', '折线', 0, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-10-24 10:59:22', 1, 0, NULL, 'cost-fetch_time_in_millis');
INSERT INTO `metric_dictionary_info` VALUES (5193, '索引模板性能指标', '查询Scroll量', '索引模板下索引Shard 级别 Scroll请求量，Top节点趋势', '60S', '[平均值] 获取间隔时间内模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.search.scroll_total的差值累加值/时间间隔', 0, '个', '折线', 0, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-10-24 10:44:38', 1, 0, NULL, 'search-scroll_total_rate');
INSERT INTO `metric_dictionary_info` VALUES (5195, '索引模板性能指标', '查询Scroll耗时', '索引模板Scorll耗时平均值，Top节点趋势', '60S', '[平均值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.search.scroll_time_in_millis的差值累加值/_all.total.search.scroll_total的差值累加值', 0, 'MS', '折线', 0, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-09-30 11:19:09', 1, 0, NULL, 'cost-scroll_time_in_millis');
INSERT INTO `metric_dictionary_info` VALUES (5197, '索引模板性能指标', 'Merge耗时', '索引模板Merge耗时平均值，Top节点趋势', '60S', '[平均值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.merges.total_time_in_millis的差值累加值/_all.total.merges.total的差值累加值', 0, 'MS', '折线', 0, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-09-29 11:06:08', 1, 0, NULL, 'cost-merges-total_time_in_millis');
INSERT INTO `metric_dictionary_info` VALUES (5199, '索引模板性能指标', 'Refresh耗时', '索引模板Refresh耗时平均值，Top节点趋势', '60S', '[平均值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.refresh.total_time_in_millis的差值累加值/_all.total.refresh.total的差值累加值', 0, 'MS', '折线', 0, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-09-29 11:06:09', 1, 0, NULL, 'cost-refresh-total_time_in_millis');
INSERT INTO `metric_dictionary_info` VALUES (5201, '索引模板性能指标', 'Flush耗时', '索引模板Flush耗时平均值，Top节点趋势', '60S', '[平均值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.flush.total_time_in_millis的差值累加值/all.total.flush.total的差值累加值', 0, 'MS', '折线', 0, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-09-29 11:06:09', 1, 0, NULL, 'cost-flush-total_time_in_millis');
INSERT INTO `metric_dictionary_info` VALUES (5203, '索引模板性能指标', 'Merge次数', '索引模板Merge次数，Top节点趋势', '60S', '[平均值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.merges.total的差值累加值/时间间隔', 0, '次/MIN', '折线', 0, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-10-24 10:59:59', 1, 0, NULL, 'merges-total_rate');
INSERT INTO `metric_dictionary_info` VALUES (5205, '索引模板性能指标', 'Refresh次数', '索引模板Refresh次数，Top节点趋势', '60S', '[平均值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.refresh.total的差值累加值/时间间隔', 0, '次/MIN', '折线', 0, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-10-24 10:57:58', 1, 0, NULL, 'refresh-total_rate');
INSERT INTO `metric_dictionary_info` VALUES (5207, '索引模板性能指标', 'Flush次数', '索引模板Flush次数，Top节点趋势', '60S', '[平均值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.flush-total_rate的累加值差值/时间间隔', 0, '次/MIN', '折线', 0, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-09-29 11:06:10', 1, 0, NULL, 'flush-total_rate');
INSERT INTO `metric_dictionary_info` VALUES (5209, '索引模板内存指标', 'Segements大小', '索引模板下索引所有Shard的Segment底层Lucene内存汇总占用，Top节点趋势', '当前值', '[当前值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.segments.memory_in_bytes的总和', 1, 'MB', '折线', 0, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-09-29 11:06:10', 1, 0, NULL, 'segments-memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5211, '索引模板内存指标', 'Terms内存大小', '索引模板下索引所有Shard的Segment底层Terms(Text/Keyword/...)内存汇总占用，是底层Lucene内存汇总占用的一个子项，Top节点趋势', '当前值', '[当前值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.segments.term_vectors_memory_in_bytes的总和', 0, 'MB', '折线', 0, NULL, NULL, 'Index_template', '2022-09-28 11:31:36', '2022-09-29 11:06:10', 1, 0, NULL, 'segments-term_vectors_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5213, '索引模板内存指标', 'Points内存大小', '索引模板下索引所有Shard的Segment底层Points(Numbers/IPs/Geo/...)内存汇总占用，是底层Lucene内存汇总占用的一个子项，Top节点趋势', '当前值', '[当前值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.segments.points_memory_in_bytes的总和', 0, 'MB', '折线', 0, NULL, NULL, 'Index_template', '2022-09-28 11:31:36', '2022-09-29 11:06:11', 1, 0, NULL, 'segments-points_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5215, '索引模板内存指标', 'Doc Values内存大小', '索引模板下索引所有Shard的Doc Values内存大小，是底层Lucene内存汇总占用的一个子项，Top节点趋势', '当前值', '[当前值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.segments.doc_values_memory_in_bytes的总和', 0, 'MB', '折线', 0, NULL, NULL, 'Index_template', '2022-09-28 11:31:36', '2022-09-29 11:06:11', 1, 0, NULL, 'segments-doc_values_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5217, '索引模板内存指标', 'Index Writer内存大小', '索引模板下索引所有Shard的Index Writer内存大小，不在Lucene内存占用统计范围内,Top节点趋势', '当前值', '[当前值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.segments.index_writer_memory_in_bytes的总和', 0, 'MB', '折线', 0, NULL, NULL, 'Index_template', '2022-09-28 11:31:36', '2022-09-29 11:06:11', 1, 0, NULL, 'segments-index_writer_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5219, '索引模板内存指标', '未提交的Translog大小', '索引模板下索引所有Shard的未提交Translog的大小累加值，Top节点趋势', '当前值', '[当前值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.translog.uncommitted_size_in_bytes的总和', 0, 'MB', '折线', 0, NULL, NULL, 'Index_template', '2022-09-28 11:31:36', '2022-09-29 11:06:11', 1, 0, NULL, 'translog-size_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5221, '索引模板内存指标', 'Query Cache内存大小', '索引模板下索引所有Shard Query Cache(Cached Filters/...)堆内存汇总占用，不在Lucene内存占用统计范围内，Top节点趋势', '当前值', '[当前值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.query_cache.memory_size_in_bytes的总和', 1, 'MB', '折线', 0, NULL, NULL, 'Index_template', '2022-09-28 11:31:36', '2022-09-29 11:06:11', 1, 0, NULL, 'query_cache-memory_size_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5223, '索引模板内存指标', 'Stored Fields大小', '索引模板下索引stored_fields_memory内存大小', '当前值', '[当前值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.segments.stored_fields_memory_in_bytes的总和', 0, NULL, NULL, 0, NULL, NULL, 'Index_template', '2022-09-28 11:31:36', '2022-09-29 11:06:12', 1, 0, NULL, 'segments-stored_fields_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5225, '索引模板内存指标', 'Norms内存大小', '索引模板下索引所有Shard Norms(normalization factors for query time/text scoring)内存大小累加值，是底层Lucene内存汇总占用的一个子项，Top节点趋势', '当前值', '[当前值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.segments-norms_memory_in_bytes的总和', 0, 'MB', '折线图', 0, NULL, NULL, 'Index_template', '2022-09-28 11:31:36', '2022-11-02 19:22:53', 1, 0, NULL, 'segments-norms_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5227, '索引模板内存指标', 'Version Map内存大小', '索引模板下索引所有Shard Version Map(update/delete)内存大小累加值，不在Lucene内存占用统计范围内，Top节点趋势', '当前值', '[当前值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.segments.version_map_memory_in_bytes的总和', 0, 'MB', '折线图', 0, NULL, NULL, 'Index_template', '2022-09-28 11:31:36', '2022-11-02 19:23:20', 1, 0, NULL, 'segments-version_map_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5229, '索引模板内存指标', 'Fixed Bitsets内存大小', '索引模板下索引所有Shard Fixed Bitsets(deeply nested object/...)内存大小累加值，是底层Lucene内存汇总占用的一个子项，Top节点趋势', '当前值', '[当前值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.segments.fixed_bit_set_memory_in_bytes的总和', 0, 'MB', '折线图', 0, NULL, NULL, 'Index_template', '2022-09-28 11:31:36', '2022-11-02 19:23:39', 1, 0, NULL, 'segments-fixed_bit_set_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5231, '索引模板内存指标', 'Fielddata内存大小', '索引模板下索引所有Shard Fielddata(global ordinals /enable fielddata on text field/...)内存汇总占用，不在Lucene内存占用统计范围内，Top节点趋势', '当前值', '[当前值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.fielddata.memory_size_in_bytes的总和', 0, 'MB', '折线图', 0, NULL, NULL, 'Index_template', '2022-09-28 11:31:36', '2022-11-02 19:23:49', 1, 0, NULL, 'fielddata-memory_size_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5233, '索引模板内存指标', 'Request Cache内存大小', '索引模板下索引所有Shard Request Cache(Cached Aggregation Results/...)堆内存汇总占用，不在Lucene内存占用统计范围内，Top节点趋势', '当前值', '[当前值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.request_cache.memory_size_in_bytes的总和', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-11-02 19:20:19', 1, 0, NULL, 'segments-request_cache-memory_size_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5235, '集群', '集群健康状态', '不同健康状态集群分布感知，快速定位故障集群', '当前值', '[当前值] 通过GET _cluster/health命令获取status', 0, NULL, '状态栏', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-10-08 15:18:40', 1, 0, NULL, 'health');
INSERT INTO `metric_dictionary_info` VALUES (5237, '集群', '指标采集延时', '指标数据质量风险集群预警', '当前值', '[当前值] 采集数据最近一个时间点和当前时间点的差值', 0, 'S', '列表', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-09-30 11:22:01', 1, 0, NULL, 'clusterElapsedTimeGte5Min');
INSERT INTO `metric_dictionary_info` VALUES (5239, '集群', 'shard个数大于10000集群', 'Shard膨胀风险集群预警', '当前值', '[当前值] 通过GET _cat/health?format=json获取shards总数量(包括unassign状态)', 0, NULL, '列表', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-10-10 15:49:42', 1, 0, NULL, 'shardNum');
INSERT INTO `metric_dictionary_info` VALUES (5241, '集群', '写入耗时', '索引写入性能对比分析，性能不足集群预警', '5*60S', '[最大值] 集群下的所有节点,间隔时间内通过_node/stats命令获取nodes.{nodeName}.indices.indexing.index_time_in_millis差值累加值/节点间隔时间内nodes.{nodeName}.indices.indexing.index_total差值累加值', 0, 'S', '折线图，可选Top5-Top50', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-11-02 19:51:22', 1, 0, NULL, 'indexingLatency');
INSERT INTO `metric_dictionary_info` VALUES (5243, '集群', 'node_stats接口平均采集耗时', 'Master指标采集性能问题集群预警', '当前值', '[当前值] 调用一次_nodes/stats命令所消耗的时间', 0, 'S', '折线图，可选Top5-Top50', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-10-10 15:50:23', 1, 0, NULL, 'nodeElapsedTime');
INSERT INTO `metric_dictionary_info` VALUES (5245, '集群', '集群pending task数', 'pending task持续堆积，Master元数据处理性能问题集群预警', '当前值', '[当前值] 通过 _cluster/health命令获取number_of_pending_tasks', 0, '个', '折线图，可选Top5-Top50', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-09-30 11:22:23', 1, 0, NULL, 'pendingTaskNum');
INSERT INTO `metric_dictionary_info` VALUES (5247, '集群', '网关失败率', '各组网关业务查询健康预警', '5*60S', '5分钟执行一次，获取近一分钟内的网关失败率', 0, '%', '折线图，可选Top5-Top50', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-09-30 11:22:26', 1, 0, NULL, 'gatewayFailedPer');
INSERT INTO `metric_dictionary_info` VALUES (5249, '集群', '查询耗时', '索引查询性能对比分析，查询性能不足集群预警', '5*60S', '[最大值] 集群下的所有节点,间隔时间内通过_node/stats命令获取nodes.{nodeName}.indices.search.query_time_in_millis差值累加值/节点间隔时间内nodes.{nodeName}.indices.search.query_total差值累加值', 0, 'S', '折线图，可选Top5-Top50', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-11-02 19:51:08', 1, 0, NULL, 'searchLatency');
INSERT INTO `metric_dictionary_info` VALUES (5251, '节点', '节点执行任务耗时', '节点执行任务平均耗时高', '5*60S', '[平均值]根据_cat/tasks?v&detailed&format=json命令获取到当前时间的各节点任务执行总和/节点执行的次数', 0, 'S', '折线图，可选Top5-Top50', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-09-30 11:22:56', 1, 0, NULL, 'taskConsuming');
INSERT INTO `metric_dictionary_info` VALUES (5253, '节点', '磁盘利用率超红线节点', '磁盘利用率超安全水位节点预警', '当前值', '[当前值]根据GET _nodes/stats命令获取到(nodes.{nodeName}.fs.total-nodes.{nodeName}.fs.free_in_bytes)/nodes.{nodeName}.fs.total大于阀值的节点', 0, NULL, '列表', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-09-30 11:23:34', 1, 0, NULL, 'largeDiskUsage');
INSERT INTO `metric_dictionary_info` VALUES (5255, '节点', '分片个数大于500节点', '分片数超安全水位节点预警', '当前值', '[当前值]根据_cat/shards?v&h=node命令获取到结果个数大于阀值的节点', 0, NULL, '列表', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-10-08 16:55:51', 1, 0, NULL, 'shardNum');
INSERT INTO `metric_dictionary_info` VALUES (5257, '节点', '堆内存利用率超红线节点', '堆内存利用率超红线节点预警', '当前值', '[当前值]根据GET _nodes/stats命令获取nodes.{nodeName}.jvm.mem.heap_used_percent大于阀值的节点', 0, NULL, '列表', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-09-30 11:23:28', 1, 0, NULL, 'largeHead');
INSERT INTO `metric_dictionary_info` VALUES (5259, '节点', 'CPU利用率超红线节点', 'CPU利用率超红线节点预警', '当前值', '[当前值]根据GET _nodes/stats命令获取到nodes.{nodeName}.os.cpu.percent大于阀值的节点', 0, NULL, '列表', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-09-30 11:23:32', 1, 0, NULL, 'largeCpuUsage');
INSERT INTO `metric_dictionary_info` VALUES (5261, '节点', 'SearchRejected节点', 'SearchRejected节点预警', '5*60S', '[当前值]当前时间和上次时间通过GET _nodes/stats命令nodes.{nodeName}.thread_pool.search.rejected差值不为0的节点', 0, NULL, '列表', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-09-30 11:23:41', 1, 0, NULL, 'searchRejectedNum');
INSERT INTO `metric_dictionary_info` VALUES (5263, '节点', 'WriteRejected节点', 'WriteRejected节点预警', '5*60S', '[当前值]当前时间和上次时间通过GET _nodes/stats命令nodes.{nodeName}.thread_pool.write.rejected差值不为0的节点', 0, NULL, '列表', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-09-30 11:23:43', 1, 0, NULL, 'writeRejectedNum');
INSERT INTO `metric_dictionary_info` VALUES (5265, '索引', 'segments内存大于1MB索引模板', '索引模板超大内存占用风险预警', '当前值', '[当前值]根据_cat/segments/命令获取size字段大于阀值的索引', 0, NULL, '列表', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-10-08 16:59:56', 1, 1, 'segments内存大于1MB索引模板', 'tplSegmentMemSize');
INSERT INTO `metric_dictionary_info` VALUES (5267, '索引', 'segments个数大于20索引模板', '索引模板Segements数超红线预警', '当前值', '[当前值]根据_cat/segments/命令获取segment个数大于阀值的索引', 0, NULL, '列表', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-10-08 16:59:58', 1, 0, NULL, 'tplSegmentNum');
INSERT INTO `metric_dictionary_info` VALUES (5269, '索引', '未分配shard索引', 'shard未分配索引预警', '当前值', '[当前值]根据GET {indexName}/_stats命令获取索引状态不等于green的索引', 0, NULL, '列表', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-09-30 11:24:09', 1, 0, NULL, 'unassignedShard');
INSERT INTO `metric_dictionary_info` VALUES (5271, '索引', 'mapping字段个数大于100索引', '索引mapping字段膨胀预警', '当前值', '[当前值]根据GET {indexName}命令mapping的字段个数大于阀值的索引', 0, NULL, '列表', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-09-30 11:24:13', 1, 0, NULL, 'mappingNum');
INSERT INTO `metric_dictionary_info` VALUES (5273, '索引', 'segments内存大于100B索引', '索引超大内存占用风险预警', '当前值', '[当前值]根据_cat/segments/命令获取segment内存（size）大于阀值的索引', 0, NULL, '列表', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-09-30 11:24:20', 1, 0, NULL, 'segmentMemSize');
INSERT INTO `metric_dictionary_info` VALUES (5275, '索引', 'segments个数大于100索引', '索引Segements数超红线预警', '当前值', '[当前值]根据_cat/segments/命令获取segment个数大于阀值的索引', 0, NULL, '列表', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-09-30 11:24:24', 1, 0, NULL, 'segmentNum');
INSERT INTO `metric_dictionary_info` VALUES (5277, '索引', 'RED索引', 'RED索引预警', '当前值', '[当前值]根据_cat/indices?format=json命令获取健康状态(health)等于red的索引', 0, NULL, '列表', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-09-30 11:24:59', 1, 0, NULL, 'red');
INSERT INTO `metric_dictionary_info` VALUES (5279, '索引', '单个shard大于500MB索引', '单Shard过大索引预警', '当前值', '[当前值]根据_cat/shards命令获取大小大于指定的阀值的索引', 0, NULL, '列表', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-10-08 16:57:51', 1, 0, NULL, 'bigShard');
INSERT INTO `metric_dictionary_info` VALUES (5281, '索引', '无副本索引', '无副本索引稳定性预警', '当前值', '[当前值]根据_cat/indices命令获取副本数等于0的索引', 0, NULL, '列表', 0, NULL, NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-09-30 11:25:12', 1, 0, NULL, 'singReplicate');
INSERT INTO `metric_dictionary_info` VALUES (5283, '索引', '单个shard小于500MB索引', '索引Shard数分配不合理预警', '当前值', '[当前值]根据_cat/shards命令获取大小(store)小于指定的阀值并且shard数量大于1的索引', 0, NULL, '列表', 0, NULL, NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-10-08 16:58:06', 1, 0, NULL, 'smallShard');



#重新全量导入权限点表
truncate table kf_security_permission;
insert into kf_security_permission (id, permission_name, parent_id, leaf, level, description, create_time, update_time, is_delete, app_name)
values  (1593, '物理集群', 0, 0, 1, '物理集群', '2022-05-24 18:08:22.0', '2022-08-24 20:07:31.0', 0, 'know_search'),
        (1595, '我的集群', 0, 0, 1, '我的集群', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1597, '集群版本', 0, 0, 1, '集群版本', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1599, 'Gateway管理', 0, 0, 1, 'Gateway管理', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1601, '模板管理', 0, 0, 1, '模板管理', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1603, '模板服务', 0, 0, 1, '模板服务', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1605, '索引管理', 0, 0, 1, '索引管理', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1607, '索引服务', 0, 0, 1, '索引服务', '2022-05-24 18:08:22.0', '2022-05-24 18:24:16.0', 0, 'know_search'),
        (1609, '索引查询', 0, 0, 1, '索引查询', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1611, '查询诊断', 0, 0, 1, '查询诊断', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1613, '集群看板', 0, 0, 1, '集群看板', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1615, '网关看板', 0, 0, 1, '网关看板', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1617, '我的申请', 0, 0, 1, '我的申请', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1619, '我的审批', 0, 0, 1, '我的审批', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1621, '任务列表', 0, 0, 1, '任务列表', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1623, '调度任务列表', 0, 0, 1, '调度任务列表', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1625, '调度日志', 0, 0, 1, '调度日志', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1627, '用户管理', 0, 0, 1, '用户管理', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1629, '角色管理', 0, 0, 1, '角色管理', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1631, '应用管理', 0, 0, 1, '应用管理', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1633, '平台配置', 0, 0, 1, '平台配置', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1635, '操作记录', 0, 0, 1, '操作记录', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1637, '查看集群列表及详情', 1593, 1, 2, '查看集群列表及详情', '2022-05-24 18:08:22.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1639, '接入集群', 1593, 1, 2, '接入集群', '2022-05-24 18:08:22.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1641, '新建集群', 1593, 1, 2, '新建集群', '2022-05-24 18:08:22.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1643, '扩缩容', 1593, 1, 2, '扩缩容', '2022-05-24 18:08:22.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1645, '升级', 1593, 1, 2, '升级', '2022-05-24 18:08:22.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1647, '重启', 1593, 1, 2, '重启', '2022-05-24 18:08:23.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1649, '配置变更', 1593, 1, 2, '配置变更', '2022-05-24 18:08:23.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1651, 'Region划分', 1593, 1, 2, 'Region划分', '2022-05-24 18:08:23.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1653, 'Region管理', 1593, 1, 2, 'Region管理', '2022-05-24 18:08:23.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1655, '快捷命令', 1593, 1, 2, '快捷命令', '2022-05-24 18:08:23.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1657, '编辑', 1593, 1, 2, '编辑', '2022-05-24 18:08:23.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1659, '绑定Gateway', 1593, 1, 2, '绑定Gateway', '2022-05-24 18:08:23.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1661, '下线', 1593, 1, 2, '下线', '2022-05-24 18:08:23.0', '2022-05-24 18:10:52.0', 0, 'know_search'),
        (1663, '查看集群列表及详情', 1595, 1, 2, '查看集群列表及详情', '2022-05-24 18:08:23.0', '2022-05-24 18:10:52.0', 0, 'know_search'),
        (1665, '申请集群', 1595, 1, 2, '申请集群', '2022-05-24 18:08:23.0', '2022-05-24 18:10:52.0', 0, 'know_search'),
        (1667, '编辑', 1595, 1, 2, '编辑', '2022-05-24 18:08:23.0', '2022-05-24 18:10:52.0', 0, 'know_search'),
        (1669, '扩缩容', 1595, 1, 2, '扩缩容', '2022-05-24 18:08:23.0', '2022-05-24 18:10:52.0', 0, 'know_search'),
        (1671, '下线', 1595, 1, 2, '下线', '2022-05-24 18:08:23.0', '2022-05-24 18:20:44.0', 0, 'know_search'),
        (1673, '查看版本列表', 1597, 1, 2, '查看版本列表', '2022-05-24 18:08:23.0', '2022-05-24 18:20:44.0', 0, 'know_search'),
        (1675, '新增版本', 1597, 1, 2, '新增版本', '2022-05-24 18:08:23.0', '2022-05-24 18:20:44.0', 0, 'know_search'),
        (1677, '编辑', 1597, 1, 2, '编辑', '2022-05-24 18:08:23.0', '2022-05-24 18:20:44.0', 0, 'know_search'),
        (1679, '删除', 1597, 1, 2, '删除', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1681, '查看Gateway 集群列表', 1599, 1, 2, '查看Gateway 集群列表', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1683, '接入gateway', 1599, 1, 2, '接入gateway', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1685, '编辑', 1599, 1, 2, '编辑', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1687, '下线', 1599, 1, 2, '下线', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1689, '查看模板列表及详情', 1601, 1, 2, '查看模板列表及详情', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1691, '申请模板', 1601, 1, 2, '申请模板', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1693, '编辑', 1601, 1, 2, '编辑', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1695, '下线', 1601, 1, 2, '下线', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1697, '编辑Mapping', 1601, 1, 2, '编辑Mapping', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1699, '编辑Setting', 1601, 1, 2, '编辑Setting', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1701, '查看模板列表', 1603, 1, 2, '查看模板列表', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1703, '开关：预创建', 1603, 1, 2, '开关：预创建', '2022-05-24 18:08:23.0', '2022-06-14 16:49:48.0', 0, 'know_search'),
        (1705, '开关：过期删除', 1603, 1, 2, '开关：过期删除', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1707, '开关：冷热分离', 1603, 1, 2, '开关：冷热分离', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1709, '开关：pipeline', 1603, 1, 2, '开关：写入限流', '2022-05-24 18:08:23.0', '2022-06-14 16:49:49.0', 0, 'know_search'),
        (1711, '开关：Rollover', 1603, 1, 2, '开关：Rollover', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1713, '查看DCDR链路', 1603, 1, 2, '查看DCDR链路', '2022-05-24 18:08:23.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1715, '创建DCDR链路', 1603, 1, 2, '创建DCDR链路', '2022-05-24 18:08:24.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1717, '清理', 1603, 1, 2, '清理', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1719, '扩缩容', 1603, 1, 2, '扩缩容', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1721, '升版本', 1603, 1, 2, '升版本', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1723, '批量操作', 1603, 1, 2, '批量操作', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1725, '查看索引列表及详情', 1605, 1, 2, '查看索引列表及详情', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1727, '编辑Mapping', 1605, 1, 2, '编辑Mapping', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1729, '编辑Setting', 1605, 1, 2, '编辑Setting', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1731, '禁用读', 1607, 1, 2, '禁用读', '2022-05-24 18:08:24.0', '2022-07-15 08:50:56.0', 0, 'know_search'),
        (1733, '禁用写', 1607, 1, 2, '禁用写', '2022-05-24 18:08:24.0', '2022-07-15 08:50:56.0', 0, 'know_search'),
        (1735, '设置别名', 1605, 1, 2, '设置别名', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1737, '删除别名', 1605, 1, 2, '删除别名', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1739, '关闭索引', 1607, 1, 2, '关闭索引', '2022-05-24 18:08:24.0', '2022-07-15 09:52:25.0', 0, 'know_search'),
        (1741, '下线', 1605, 1, 2, '下线', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1743, '批量删除', 1605, 1, 2, '批量删除', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1745, '查看列表', 1607, 1, 2, '查看列表', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1747, '执行Rollover', 1607, 1, 2, '执行Rollover', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1749, '执行shrink', 1607, 1, 2, '执行shrink', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1751, '执行split', 1607, 1, 2, '执行split', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1753, '执行ForceMerge', 1607, 1, 2, '执行ForceMerge', '2022-05-24 18:08:24.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1755, '批量执行', 1607, 1, 2, '批量执行', '2022-05-24 18:08:24.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1757, 'DSL查询', 1877, 1, 2, 'DSL查询', '2022-05-24 18:08:24.0', '2022-09-05 14:24:00.0', 0, 'know_search'),
        (1759, '查询模板', 0, 0, 1, '查看查询模板列表', '2022-05-24 18:08:24.0', '2022-08-11 10:37:43.0', 0, 'know_search'),
        (1761, '查看集群看板', 1613, 1, 2, '查看集群看板', '2022-05-24 18:08:24.0', '2022-06-14 16:37:54.0', 0, 'know_search'),
        (1763, '查看网关看板', 1615, 1, 2, '查看网关看板', '2022-05-24 18:08:24.0', '2022-06-14 16:38:14.0', 0, 'know_search'),
        (1765, '查看我的申请列表', 1617, 1, 2, '查看我的申请列表', '2022-05-24 18:08:24.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1767, '撤回', 1617, 1, 2, '撤回', '2022-05-24 18:08:24.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1769, '查看我的审批列表', 1619, 1, 2, '查看我的审批列表', '2022-05-24 18:08:24.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1771, '驳回', 1619, 1, 2, '撤回', '2022-05-24 18:08:24.0', '2022-07-18 20:57:33.0', 0, 'know_search'),
        (1773, '通过', 1619, 1, 2, '通过', '2022-05-24 18:08:24.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1775, '查看任务列表', 1621, 1, 2, '查看任务列表', '2022-05-24 18:08:24.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1777, '查看进度', 1621, 1, 2, '查看进度', '2022-05-24 18:08:24.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1779, '执行', 1621, 1, 2, '执行', '2022-05-24 18:08:24.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1781, '暂停', 1621, 1, 2, '暂停', '2022-05-24 18:08:24.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1783, '重试', 1621, 1, 2, '重试', '2022-05-24 18:08:25.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1785, '取消', 1621, 1, 2, '取消', '2022-05-24 18:08:25.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1787, '查看日志（子任务）', 1621, 1, 2, '查看日志（子任务）', '2022-05-24 18:08:25.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1789, '重试（子任务）', 1621, 1, 2, '重试（子任务）', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1791, '忽略（子任务）', 1621, 1, 2, '忽略（子任务）', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1793, '查看详情（DCDR）', 1621, 1, 2, '查看详情（DCDR）', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1795, '取消（DCDR）', 1621, 1, 2, '取消（DCDR）', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1797, '重试（DCDR）', 1621, 1, 2, '重试（DCDR）', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1799, '强切（DCDR）', 1621, 1, 2, '强切（DCDR）', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1801, '返回（DCDR）', 1621, 1, 2, '返回（DCDR）', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1803, '查看任务列表', 1623, 1, 2, '查看任务列表', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1805, '查看日志', 1623, 1, 2, '查看日志', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1807, '执行', 1623, 1, 2, '执行', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1809, '暂停', 1623, 1, 2, '暂停', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1811, '查看调度日志列表', 1625, 1, 2, '查看调度日志列表', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1813, '调度详情', 1625, 1, 2, '调度详情', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1815, '执行日志', 1625, 1, 2, '执行日志', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1817, '终止任务', 1625, 1, 2, '终止任务', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1819, '查看用户列表', 1627, 1, 2, '查看用户列表', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1821, '分配角色', 1627, 1, 2, '分配角色', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1823, '查看角色列表', 1629, 1, 2, '查看角色列表', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1825, '编辑', 1629, 1, 2, '编辑', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1827, '绑定用户', 1629, 1, 2, '绑定用户', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1829, '回收用户', 1629, 1, 2, '回收用户', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1831, '删除角色', 1629, 1, 2, '删除角色', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1833, '查看应用列表', 1631, 1, 2, '查看应用列表', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1835, '新建应用', 1631, 1, 2, '新建应用', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1837, '编辑', 1631, 1, 2, '编辑', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1839, '删除', 1631, 1, 2, '删除', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1841, '访问设置', 1631, 1, 2, '访问设置', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1843, '查看平台配置列表', 1633, 1, 2, '查看平台配置列表', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1845, '新增平台配置', 1633, 1, 2, '新增平台配置', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1847, '禁用平台配置', 1633, 1, 2, '禁用平台配置', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1849, '编辑平台配置', 1633, 1, 2, '编辑平台配置', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1851, '删除平台配置', 1633, 1, 2, '删除平台配置', '2022-05-24 18:08:26.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1853, '查看操作记录列表', 1635, 1, 2, '查看操作记录列表', '2022-05-24 18:08:26.0', '2022-05-24 18:23:34.0', 0, 'know_search'),
        (1855, 'Kibana查询', 1879, 1, 2, 'Kibana查询', '2022-05-24 18:08:26.0', '2022-09-05 14:24:00.0', 0, 'know_search'),
        (1857, 'SQL查询', 1881, 1, 2, 'SQL查询', '2022-05-24 18:08:26.0', '2022-09-05 14:24:00.0', 0, 'know_search'),
        (1859, '批量修改限流值', 1759, 1, 2, '批量修改限流值', '2022-05-24 18:08:26.0', '2022-08-11 10:37:13.0', 0, 'know_search'),
        (1861, '禁用', 1759, 1, 2, '禁用', '2022-05-24 18:08:26.0', '2022-08-11 10:37:13.0', 0, 'know_search'),
        (1863, '修改限流值', 1759, 1, 2, '修改限流值', '2022-05-24 18:08:26.0', '2022-08-11 10:37:13.0', 0, 'know_search'),
        (1865, '查看异常查询列表', 1611, 1, 2, '查看异常查询列表', '2022-05-24 18:08:26.0', '2022-06-14 16:44:02.0', 0, 'know_search'),
        (1867, '查看慢查询列表', 1611, 1, 2, '查看慢查询列表', '2022-05-24 18:08:26.0', '2022-06-14 16:44:21.0', 0, 'know_search'),
        (1869, '新增角色', 1629, 1, 2, '新增角色', '2022-05-24 18:08:26.0', '2022-05-24 18:23:34.0', 0, 'know_search'),
        (1871, 'Dashboard', 0, 0, 1, '查看dashboard', '2022-05-24 18:08:26.0', '2022-08-27 17:35:50.0', 0, 'know_search'),
        (1873, '新建索引', 1605, 1, 2, '新建索引', '2022-05-24 18:08:26.0', '2022-05-24 18:23:34.0', 0, 'know_search'),
        (1875, '查看dashboard', 1871, 1, 2, '查看dashboard', '2022-05-24 18:08:24.0', '2022-08-27 17:35:50.0', 0, 'know_search'),
        (1877, 'DSL', 0, 0, 1, 'DSL', '2022-05-24 18:08:24.0', '2022-09-02 19:01:17.0', 0, 'know_search'),
        (1879, 'Kibana', 0, 0, 1, 'Kibana', '2022-05-24 18:08:26.0', '2022-09-02 19:01:17.0', 0, 'know_search'),
        (1881, 'SQL', 0, 0, 1, 'SQL', '2022-05-24 18:08:26.0', '2022-09-02 19:01:17.0', 0, 'know_search');

alter table kf_security_oplog
    modify target varchar(225) not null comment '操作对象';

INSERT INTO `kf_security_permission` (`permission_name`, `parent_id`, `leaf`, `level`, `description`, `create_time`, `update_time`, `is_delete`, `app_name`) VALUES ('Grafana', 0, 0, 1, 'Grafana', '2022-05-24 18:08:26', '2022-12-22 15:16:17', 0, 'know_search');
INSERT INTO `kf_security_role_permission` (`role_id`, `permission_id`, `create_time`, `update_time`, `is_delete`, `app_name`) VALUES (1, (select id from kf_security_permission ksp where ksp.permission_name='Grafana' and ksp.app_name='know_search' and ksp.is_delete=0 ), '2022-06-01 21:19:42', '2022-08-25 10:31:42', 0, 'know_search');
INSERT INTO `kf_security_permission` (`permission_name`, `parent_id`, `leaf`, `level`, `description`, `create_time`, `update_time`, `is_delete`, `app_name`) VALUES ('查看Grafana', (select id from kf_security_permission ksp where ksp.permission_name='Grafana' and ksp.app_name='know_search' and ksp.is_delete=0 ), 1, 2, '查看Grafana', '2022-05-24 18:08:26', '2022-12-22 15:16:17', 0, 'know_search');
INSERT INTO `kf_security_role_permission` (`role_id`, `permission_id`, `create_time`, `update_time`, `is_delete`, `app_name`) VALUES (1, (select id from kf_security_permission ksp where ksp.permission_name='查看Grafana' and ksp.app_name='know_search' and ksp.is_delete=0 ), '2022-06-01 21:19:42', '2022-08-25 10:31:42', 0, 'know_search');


ALTER TABLE es_cluster_phy_info
    ADD proxy_address VARCHAR(255) DEFAULT '' NULL COMMENT ' 代理地址 ';

#项目、用户、权限点
-- auto-generated definition
create table kf_security_config
(
    id          bigint unsigned auto_increment comment '主键自增'
        primary key,
    value_group varchar(100)  default ''                not null comment ' 配置项组 ',
    value_name  varchar(100)  default ''                not null comment ' 配置项名字 ',
    value       text                                    null comment '配置项的值',
    edit        int(4)        default 1                 not null comment '是否可以编辑 1 不可编辑（程序获取） 2 可编辑',
    status      int(4)        default 1                 not null comment '1 正常 2 禁用',
    memo        varchar(1000) default ''                not null comment ' 备注 ',
    create_time timestamp     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time timestamp     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '修改时间',
    is_delete   tinyint(1)    default 0                 not null comment '逻辑删除',
    app_name    varchar(16) collate utf8_bin            null comment '应用名称',
    operator    varchar(16) collate utf8_bin            null comment '操作者'
)
    comment 'logi 配置项' charset = utf8;

create index idx_group_name
    on kf_security_config (value_group, value_name);

-- auto-generated definition
create table kf_security_dept
(
    id          int auto_increment
        primary key,
    dept_name   varchar(10)                          not null comment '部门名',
    parent_id   int                                  not null comment '父部门 id',
    leaf        tinyint(1)                           not null comment '是否叶子部门',
    level       tinyint                              not null comment 'parentId 为 0 的层级为 1',
    description varchar(20)                          null comment '描述',
    create_time timestamp  default CURRENT_TIMESTAMP null comment '创建时间',
    update_time timestamp  default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete   tinyint(1) default 0                 null comment '逻辑删除',
    app_name    varchar(16)                          null comment '应用名称'
)
    comment '部门信息表' charset = utf8;

-- auto-generated definition
create table kf_security_message
(
    id          int auto_increment
        primary key,
    title       varchar(60)                          not null comment '标题',
    content     varchar(256)                         null comment '内容',
    read_tag    tinyint(1) default 0                 null comment '是否已读',
    oplog_id    int                                  null comment '操作日志 id',
    user_id     int                                  null comment '这条消息属于哪个用户的，用户 id',
    create_time timestamp  default CURRENT_TIMESTAMP null comment '创建时间',
    update_time timestamp  default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete   tinyint(1) default 0                 null comment '逻辑删除',
    app_name    varchar(16)                          null comment '应用名称'
)
    comment '消息中心' charset = utf8;

-- auto-generated definition
create table kf_security_oplog
(
    id                int auto_increment
        primary key,
    operator_ip       varchar(20)                            not null comment '操作者 ip',
    operator          varchar(20)                            null comment '操作者账号',
    operate_page      varchar(16)                            null comment '操作页面',
    operate_type      varchar(16)                            not null comment '操作类型',
    target_type       varchar(16)                            not null comment '对象分类',
    target            varchar(255)                            not null comment '操作对象',
    detail            text                                   null comment '日志详情',
    create_time       timestamp    default CURRENT_TIMESTAMP null,
    update_time       timestamp    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete         tinyint(1)   default 0                 not null comment '逻辑删除',
    app_name          varchar(16)                            null comment '应用名称',
    operation_methods varchar(255) default ''                null
)
    comment '操作日志' charset = utf8;

-- auto-generated definition
create table kf_security_oplog_extra
(
    id          int auto_increment
        primary key,
    info        varchar(16)                          null comment '信息',
    type        tinyint                              not null comment '哪种信息：1：操作页面;2：操作类型;3：对象分类',
    create_time timestamp  default CURRENT_TIMESTAMP null comment '创建时间',
    update_time timestamp  default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete   tinyint(1) default 0                 null comment '逻辑删除',
    app_name    varchar(16)                          null comment '应用名称'
)
    comment '操作日志信息（操作页面、操作类型、对象分类）' charset = utf8;

-- auto-generated definition
create table kf_security_permission
(
    id              int auto_increment
        primary key,
    permission_name varchar(40)                          not null comment '权限名字',
    parent_id       int                                  not null comment '父权限 id',
    leaf            tinyint(1)                           not null comment '是否叶子权限点（具体的操作）',
    level           tinyint                              not null comment '权限点的层级（parentId 为 0 的层级为 1）',
    description     varchar(64)                          null comment '权限点描述',
    create_time     timestamp  default CURRENT_TIMESTAMP null comment '创建时间',
    update_time     timestamp  default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete       tinyint(1) default 0                 null comment '逻辑删除',
    app_name        varchar(16)                          null comment '应用名称'
)
    comment '权限表' charset = utf8;

-- auto-generated definition
create table kf_security_project
(
    id           int auto_increment comment '项目 id'
        primary key,
    project_code varchar(128)                           not null comment '项目编号',
    project_name varchar(128)                           not null comment '项目名',
    description  varchar(512) default ''                not null comment ' 项目描述 ',
    dept_id      int                                    not null comment '部门 id',
    running      tinyint(1)   default 1                 not null comment '启用 or 停用',
    create_time  timestamp    default CURRENT_TIMESTAMP null comment '创建时间',
    update_time  timestamp    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete    tinyint(1)   default 0                 not null comment '逻辑删除',
    app_name     varchar(16)                            null comment '应用名称'
)
    comment '项目表' charset = utf8;

-- auto-generated definition
create table kf_security_resource_type
(
    id          int auto_increment
        primary key,
    type_name   varchar(16)                          null comment '资源类型名',
    create_time timestamp  default CURRENT_TIMESTAMP null comment '创建时间',
    update_time timestamp  default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete   tinyint(1) default 0                 not null comment '逻辑删除',
    app_name    varchar(16)                          null comment '应用名称'
)
    comment '资源类型表' charset = utf8;

-- auto-generated definition
create table kf_security_role
(
    id           int auto_increment
        primary key,
    role_code    varchar(128)                         not null comment '角色编号',
    role_name    varchar(128)                         not null comment '名称',
    description  varchar(128)                         null comment '角色描述',
    last_reviser varchar(30)                          null comment '最后修改人',
    create_time  timestamp  default CURRENT_TIMESTAMP null comment '创建时间',
    update_time  timestamp  default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete    tinyint(1) default 0                 not null comment '逻辑删除',
    app_name     varchar(16)                          null comment '应用名称'
)
    comment '角色信息' charset = utf8;

-- auto-generated definition
create table kf_security_role_permission
(
    id            int auto_increment
        primary key,
    role_id       int                                  not null comment '角色 id',
    permission_id int                                  not null comment '权限 id',
    create_time   timestamp  default CURRENT_TIMESTAMP null comment '创建时间',
    update_time   timestamp  default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete     tinyint(1) default 0                 not null comment '逻辑删除',
    app_name      varchar(16)                          null comment '应用名称'
)
    comment '角色权限表（只保留叶子权限与角色关系）' charset = utf8;

-- auto-generated definition
create table kf_security_user
(
    id          int auto_increment
        primary key,
    user_name   varchar(64)                            not null comment '用户账号',
    pw          varchar(2048)                          not null comment '用户密码',
    salt        char(5)      default ''                not null comment ' 密码盐 ',
    real_name   varchar(128) default ''                not null comment ' 真实姓名 ',
    phone       char(11)     default ''                not null comment 'mobile',
    email       varchar(30)  default ''                not null comment 'email',
    dept_id     int                                    null comment '所属部门 id',
    is_delete   tinyint(1)   default 0                 not null comment '逻辑删除',
    create_time timestamp    default CURRENT_TIMESTAMP null comment '注册时间',
    update_time timestamp    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    app_name    varchar(16)                            null comment '应用名称'
)
    comment '用户信息' charset = utf8;

-- auto-generated definition
create table kf_security_user_project
(
    id          int auto_increment
        primary key,
    user_id     int                                   not null comment '用户 id',
    project_id  int                                   not null comment '项目 id',
    create_time timestamp   default CURRENT_TIMESTAMP null comment '创建时间',
    update_time timestamp   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete   tinyint(1)  default 0                 not null comment '逻辑删除',
    app_name    varchar(16)                           null comment '应用名称',
    user_type   tinyint(10) default 0                 not null comment '用户类型：0：普通项目用户；1：项目 owner'
)
    comment '用户项目关系表（项目负责人）' charset = utf8;

-- auto-generated definition
create table kf_security_user_resource
(
    id               int auto_increment
        primary key,
    user_id          int                                  not null comment '用户 id',
    project_id       int                                  not null comment '资源所属项目 id',
    resource_type_id int                                  not null comment '资源类别 id',
    resource_id      int                                  not null comment '资源 id',
    control_level    tinyint                              not null comment '管理级别：1（查看权限）2（管理权限）',
    create_time      timestamp  default CURRENT_TIMESTAMP null comment '创建时间',
    update_time      timestamp  default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete        tinyint(1) default 0                 not null comment '逻辑删除',
    app_name         varchar(16)                          null comment '应用名称'
)
    comment '用户和资源关系表' charset = utf8;

-- auto-generated definition
create table kf_security_user_role
(
    id          int auto_increment
        primary key,
    user_id     int                                  not null comment '用户 id',
    role_id     int                                  not null comment '角色 id',
    create_time timestamp  default CURRENT_TIMESTAMP null comment '创建时间',
    update_time timestamp  default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete   tinyint(1) default 0                 not null comment '逻辑删除',
    app_name    varchar(16)                          null comment '应用名称'
)
    comment '用户角色表' charset = utf8;

-- auto-generated definition
create table arius_es_user
(
    id                 bigint(10) unsigned auto_increment comment '主键 自增'
        primary key,
    index_exp          text                                    null comment '索引表达式',
    data_center        varchar(20)   default ''                not null comment ' 数据中心 ',
    is_root            tinyint       default 0                 not null comment '是都是超级用户 超级用户具有所有索引的访问权限 0 不是 1 是',
    memo               varchar(1000) default ''                not null comment ' 备注 ',
    ip                 varchar(500)  default ''                not null comment ' 白名单 ip 地址 ',
    verify_code        varchar(50)   default ''                not null comment 'app 验证码 ',
    is_active          tinyint(2)    default 1                 not null comment '1 为可用，0 不可用',
    query_threshold    int(10)       default 100               not null comment '限流值',
    cluster            varchar(100)  default ''                not null comment ' 查询集群 ',
    responsible        varchar(500)  default ''                null comment ' 责任人 ',
    search_type        tinyint       default 0                 not null comment '0 表示 app 的查询请求需要 app 里配置的集群 (一般配置的都是 trib 集群) 1 表示 app 的查询请求必须只能访问一个模板 2 表示集群模式（可支持多模板查询）',
    create_time        timestamp     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time        timestamp     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '修改时间',
    project_id         bigint(10)                              not null comment '项目 id',
    is_default_display tinyint(2)    default 0                 not null comment '1：项目默认的 es user；0: 项目新增的 es user'
)
    comment 'es 操作用户表' charset = utf8;
create table project_arius_config
(
    project_id              bigint(10) unsigned auto_increment comment 'project id'
        primary key,
    analyze_response_enable tinyint       default 1                 not null comment '响应结果解析开关 默认是 0：关闭，1：开启',
    is_source_separated     tinyint       default 0                 not null comment '是否是索引存储分离的 0 不是 1 是',
    aggr_analyze_enable     tinyint       default 1                 not null comment '1 生效 0 不生效',
    dsl_analyze_enable      tinyint(2)    default 1                 not null comment '1 为生效 dsl 分析查询限流值，0 不生效 dsl 分析查询限流值',
    slow_query_times        int(10)       default 100               not null comment '慢查询耗时',
    is_active               tinyint(2)    default 1                 not null comment '1 为可用，0 不可用',
    memo                    varchar(1000) default ''                not null comment ' 备注 ',
    create_time             timestamp     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time             timestamp     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '修改时间'
)
    comment '项目配置' charset = utf8;

-- auto-generated definition


#### 核心表结构
CREATE TABLE `arius_config_info`
(
    `id`          bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键自增',
    `value_group` varchar(100)        NOT NULL DEFAULT '' COMMENT ' 配置项组 ',
    `value_name`  varchar(100)        NOT NULL DEFAULT '' COMMENT ' 配置项名字 ',
    `value`       text COMMENT '配置项的值',
    `edit`        int(4)              NOT NULL DEFAULT '1' COMMENT '是否可以编辑 1 不可编辑（程序获取） 2 可编辑',
    `dimension`   int(4)              NOT NULL DEFAULT '-1' COMMENT '配置项维度 1 集群 2 模板',
    `status`      int(4)              NOT NULL DEFAULT '1' COMMENT '1 正常 2 禁用 -1 删除',
    `memo`        varchar(1000)       NOT NULL DEFAULT '' COMMENT ' 备注 ',
    `create_time` timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `search_time` timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '配置查询时间',
    PRIMARY KEY (`id`),
    KEY `idx_group_name` (`value_group`, `value_name`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1662
  DEFAULT CHARSET = utf8 COMMENT ='arius 配置项';



-- ----------------------------
-- Table structure for arius_meta_job_cluster_distribute
-- ----------------------------
CREATE TABLE `arius_meta_job_cluster_distribute`
(
    `id`           bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键 id',
    `cluster_id`   int(11)             NOT NULL DEFAULT '-1' COMMENT '集群 id',
    `monitor_host` varchar(128)        NOT NULL DEFAULT '' COMMENT ' 当前执行主机名 ',
    `monitor_time` timestamp           NOT NULL DEFAULT '2000-01-02 00:00:00' COMMENT '上一次监控时间',
    `gmt_create`   timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modify`   timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `cluster`      varchar(128)        NOT NULL DEFAULT '' COMMENT ' 集群名称 ',
    `dataCentre`   varchar(16)         NOT NULL DEFAULT 'cn' COMMENT '集群数据中心',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_cluster_id` (`cluster_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 435089
  DEFAULT CHARSET = utf8 COMMENT ='monitor 任务分配';

-- ----------------------------
-- Table structure for arius_op_task
-- ----------------------------
CREATE TABLE `arius_op_task`
(
    `id`           bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id 主键自增',
    `title`        varchar(100)        NOT NULL DEFAULT '' COMMENT ' 标题 ',
    `business_key` varchar(1000)       NOT NULL DEFAULT '0' COMMENT '业务数据主键',
    `status`       varchar(20)         NOT NULL DEFAULT 'waiting' COMMENT '任务状态：success: 成功 failed: 失败 running: 执行中 waiting: 等待 cancel: 取消 pause: 暂停',
    `creator`      varchar(100)        NOT NULL DEFAULT '' COMMENT ' 创建人 ',
    `create_time`  timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag`  tinyint(1)          NOT NULL DEFAULT '0' COMMENT '标记删除',
    `expand_data`  longtext COMMENT '扩展数据',
    `task_type`    int(11)             NOT NULL DEFAULT '0' COMMENT '任务类型 1：集群新增，2：集群扩容，3：集群缩容，4：集群重，5：集群升级，6：集群插件操作，10：模版 dcdr 任务',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 2302
  DEFAULT CHARSET = utf8 COMMENT ='arius 任务表';

-- ----------------------------
-- Table structure for arius_work_order_info
-- ----------------------------
CREATE TABLE `arius_work_order_info`
(
    `id`                   bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
    `type`                 varchar(25)         NOT NULL DEFAULT 'unknown' COMMENT 'appcreate 创建 app,clustercreate 创建集群,clusterindecrease 集群扩缩溶,clusteroffline 集群下线,clusterupdate 集群修改,templateauth 索引申请,templatecreate 索引创建,templateindecrease 索引扩容,templatequerydsl 查询语句创建,templatetransfer 索引转让,querydsllimitedit 查询语句编辑,responsiblegovern 员工离职,unhealthytemplategovern 不健康索引处理',
    `title`                varchar(64)         NOT NULL DEFAULT '' COMMENT ' 标题 ',
    `approver_project_id`  int(16)             NOT NULL DEFAULT '-1' COMMENT '审批人 projectid',
    `applicant`            varchar(64)         NOT NULL DEFAULT '' COMMENT ' 申请人 ',
    `extensions`           text COMMENT '拓展字段',
    `description`          text COMMENT '备注信息',
    `approver`             varchar(64)         NOT NULL DEFAULT '' COMMENT ' 审批人 ',
    `finish_time`          timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '结束时间',
    `opinion`              varchar(256)        NOT NULL DEFAULT '' COMMENT ' 审批信息 ',
    `status`               int(16)             NOT NULL DEFAULT '0' COMMENT '工单状态, 0: 待审批, 1: 通过, 2: 拒绝, 3: 取消',
    `create_time`          timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`          timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修 \n 改时间',
    `applicant_project_id` int(16)             NOT NULL DEFAULT '-1' COMMENT '申请人 projectid',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 2522
  DEFAULT CHARSET = utf8 COMMENT ='工单表';

-- ----------------------------
-- Table structure for es_cluster_phy_info
-- ----------------------------
CREATE TABLE `es_cluster_phy_info`
(
    `id`                      bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键自增',
    `cluster`                 varchar(128)        NOT NULL DEFAULT '' COMMENT 'es 集群名 ',
    `read_address`            varchar(1000)       NOT NULL DEFAULT '' COMMENT ' 读地址 tcp',
    `write_address`           varchar(2000)       NOT NULL DEFAULT '' COMMENT ' 写地址 tcp',
    `http_address`            varchar(1000)       NOT NULL DEFAULT '' COMMENT 'http 服务地址 ',
    `http_write_address`      varchar(8000)       NOT NULL DEFAULT '' COMMENT 'http 写地址 ',
    `desc`                    varchar(2000)       NOT NULL DEFAULT '' COMMENT ' 描述 ',
    `type`                    tinyint(4)          NOT NULL DEFAULT '-1' COMMENT '集群类型，3-docker 集群，4-host 集群',
    `data_center`             varchar(10)         NOT NULL DEFAULT 'cn' COMMENT '数据中心',
    `idc`                     varchar(10)         NOT NULL DEFAULT '' COMMENT ' 机房信息 ',
    `es_version`              varchar(100)        NOT NULL DEFAULT '' COMMENT 'es 版本 ',
    `create_time`             timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`             timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `level`                   tinyint(4)          NOT NULL DEFAULT '1' COMMENT '服务等级',
    `password`                varchar(255)        NOT NULL DEFAULT '' COMMENT ' 集群访问密码 ',
    `ecm_cluster_id`          int(11)             NOT NULL DEFAULT '-1' COMMENT 'ecm 集群 id',
    `cluster_config_template` text COMMENT '集群安装模板',
    `image_name`              varchar(500)        NOT NULL DEFAULT '' COMMENT ' 镜像名 ',
    `cfg_id`                  int(11)             NOT NULL DEFAULT '-1' COMMENT '配置包 id',
    `package_id`              int(11)             NOT NULL DEFAULT '-1' COMMENT '程序包 id',
    `plug_ids`                varchar(100)                 DEFAULT '' COMMENT ' 插件包 id 列表 ',
    `creator`                 varchar(255)        NOT NULL DEFAULT '' COMMENT ' 集群创建人 ',
    `ns_tree`                 varchar(100)        NOT NULL DEFAULT '' COMMENT ' 机器节点 ',
    `template_srvs`           varchar(255)                 DEFAULT '' COMMENT ' 集群的索引模板服务 ',
    `is_active`               tinyint(4)          NOT NULL DEFAULT '1' COMMENT '是否生效',
    `run_mode`                tinyint(255)        NOT NULL DEFAULT '0' COMMENT 'client 运行模式，0 读写共享，1 读写分离',
    `write_action`            varchar(1000)                DEFAULT NULL COMMENT '指定写 client 的 action',
    `health`                  tinyint(2)          NOT NULL DEFAULT '3' COMMENT '集群状态 1 green 2 yellow 3 red',
    `active_shard_num`        bigint(25)          NOT NULL DEFAULT '0' COMMENT '有效 shard 总数量',
    `disk_total`              bigint(50)          NOT NULL DEFAULT '0' COMMENT '集群磁盘总量 单位 byte',
    `disk_usage`              bigint(50)          NOT NULL DEFAULT '0' COMMENT '集群磁盘使用量 单位 byte',
    `disk_usage_percent`      decimal(10, 5)      NOT NULL COMMENT '集群磁盘空闲率 单位 0 ~1',
    `tags`                    text COMMENT '拓展字段, 这里用于存放集群展示用属性标签，如「集群所属资源类型」等等',
    `platform_type`           varchar(100)        NOT NULL DEFAULT '' COMMENT 'IaaS 平台类型 ',
    `resource_type`           tinyint(4)          NOT NULL DEFAULT '-1' COMMENT '集群资源类型，1- 共享资源，2- 独立资源，3- 独享资源',
    `gateway_url`             varchar(200)        NOT NULL DEFAULT '' COMMENT ' 集群 gateway 地址 ',
    `kibana_address`          varchar(200)                 DEFAULT '' COMMENT 'kibana外链地址',
    `cerebro_address`         varchar(200)                 DEFAULT '' COMMENT 'cerebro外链地址',
    PRIMARY KEY (`id`),
    KEY `idx_cluster` (`cluster`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 4710
  DEFAULT CHARSET = utf8 COMMENT ='物理集群表';

-- ----------------------------
-- Table structure for es_cluster_region
-- ----------------------------
CREATE TABLE `es_cluster_region`
(
    `id`               bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键自增',
    `logic_cluster_id` varchar(200)        NOT NULL DEFAULT '-1' COMMENT '逻辑集群 ID',
    `phy_cluster_name` varchar(128)        NOT NULL DEFAULT '' COMMENT ' 物理集群名 ',
    `racks`            varchar(2048)                DEFAULT '' COMMENT 'region 的 rack，逗号分隔 ',
    `create_time`      timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag`      tinyint(1)          NOT NULL DEFAULT '0' COMMENT '删除标记，1- 已删除，0- 未删除',
    `name`             varchar(100)        NOT NULL DEFAULT '' COMMENT 'region 名称 ',
    `config`           varchar(1024)                DEFAULT '' COMMENT 'region 配置项 ',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 4008
  DEFAULT CHARSET = utf8 COMMENT ='es 集群 region 表';

-- ----------------------------
-- Table structure for es_cluster_role_host_info
-- ----------------------------
CREATE TABLE `es_cluster_role_host_info`
(
    `id`              bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id 主键自增',
    `role_cluster_id` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '关联集群角色表外键',
    `hostname`        varchar(100)        NOT NULL DEFAULT '' COMMENT ' 节点名称 ',
    `ip`              varchar(50)         NOT NULL DEFAULT '' COMMENT ' 主机 ip',
    `cluster`         varchar(50)         NOT NULL DEFAULT '' COMMENT ' 集群 ',
    `port`            varchar(20)         NOT NULL DEFAULT '' COMMENT ' 端口，如果是节点上启动了多个进程，可以是多个，用逗号隔开 ',
    `role`            tinyint(4)          NOT NULL DEFAULT '-1' COMMENT '角色信息， 1data 2client 3master 4tribe',
    `status`          tinyint(4)          NOT NULL DEFAULT '1' COMMENT '节点状态，1 在线 2 离线',
    `rack`            varchar(30)                  DEFAULT '' COMMENT ' 节点 rack 信息 ',
    `node_set`        varchar(500)        NOT NULL DEFAULT '' COMMENT ' 节点 set 信息 ',
    `create_time`     timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag`     tinyint(1)          NOT NULL DEFAULT '0' COMMENT '标记删除',
    `machine_spec`    varchar(100)                 DEFAULT '',
    `region_id`       bigint(20)          NOT NULL DEFAULT '-1' COMMENT '节点所属 regionId',
    `attributes`      text COMMENT 'es 节点 attributes 信息 , 逗号分隔',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_elastic_cluster_id_role_node_set` (`role_cluster_id`, `ip`, `port`, `node_set`),
    KEY `idx_cluster` (`cluster`),
    KEY `idx_hostname` (`hostname`),
    KEY `idx_rack` (`rack`),
    KEY `idx_region_id` (`region_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 2274
  DEFAULT CHARSET = utf8 COMMENT ='es 集群表对应各角色主机列表';

-- ----------------------------
-- Table structure for es_cluster_role_info
-- ----------------------------
CREATE TABLE `es_cluster_role_info`
(
    `id`                 bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id 主键自增',
    `elastic_cluster_id` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT 'elastic_cluster 外键 id',
    `role_cluster_name`  varchar(256)        NOT NULL DEFAULT '' COMMENT 'role 集群名称 ',
    `role`               varchar(20)         NOT NULL DEFAULT '' COMMENT ' 集群角色 (masternode/datanode/clientnode)',
    `pod_number`         int(11)             NOT NULL DEFAULT '0' COMMENT 'pod 数量',
    `pid_count`          int(11)             NOT NULL DEFAULT '1' COMMENT '单机实例数',
    `machine_spec`       varchar(100)                 DEFAULT '' COMMENT ' 机器规格 ',
    `es_version`         varchar(150)        NOT NULL DEFAULT '' COMMENT 'es 版本 ',
    `cfg_id`             int(11)             NOT NULL DEFAULT '-1' COMMENT '配置包 id',
    `plug_ids`           varchar(100)        NOT NULL DEFAULT '' COMMENT ' 插件包 id 列表 ',
    `create_time`        timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`        timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag`        tinyint(1)          NOT NULL DEFAULT '0' COMMENT '标记删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_elastic_cluster_id_ddcloud_cluster_name` (`elastic_cluster_id`, `role_cluster_name`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1138
  DEFAULT CHARSET = utf8 COMMENT ='es 集群角色表';

-- ----------------------------
-- Table structure for es_config
-- ----------------------------
CREATE TABLE `es_config`
(
    `id`             bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id 主键自增',
    `cluster_id`     bigint(20)          NOT NULL DEFAULT '-1' COMMENT '集群 id',
    `type_name`      varchar(255)        NOT NULL DEFAULT '' COMMENT ' 配置文件名称 ',
    `engin_name`     varchar(100)        NOT NULL DEFAULT '' COMMENT ' 组件名称 ',
    `config_data`    longtext COMMENT '配置内容',
    `desc`           varchar(255)        NOT NULL DEFAULT '' COMMENT ' 配置描述 ',
    `version_tag`    varchar(100)        NOT NULL DEFAULT '' COMMENT ' 配置 tag',
    `version_config` bigint(20)          NOT NULL DEFAULT '-1' COMMENT '配置版本',
    `selected`       smallint(6)         NOT NULL DEFAULT '0' COMMENT '是否在使用',
    `create_time`    timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag`    tinyint(1)          NOT NULL DEFAULT '0' COMMENT '标记删除',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1130
  DEFAULT CHARSET = utf8 COMMENT ='es 配置表';

-- ----------------------------
-- Table structure for es_machine_norms
-- ----------------------------
CREATE TABLE `es_machine_norms`
(
    `id`          bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id 主键自增',
    `role`        varchar(20)         NOT NULL DEFAULT '' COMMENT ' 角色 (masternode/datanode/clientnode/datanode-ceph)',
    `spec`        varchar(32)         NOT NULL DEFAULT '' COMMENT ' 规格 (16-48gi-100g)',
    `create_time` timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `delete_flag` tinyint(1)          NOT NULL DEFAULT '0' COMMENT '标记删除',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 16
  DEFAULT CHARSET = utf8 COMMENT ='容器规格列表';

-- ----------------------------
-- Table structure for es_package
-- ----------------------------
CREATE TABLE `es_package`
(
    `id`          bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id 主键自增',
    `url`         varchar(255)        NOT NULL DEFAULT '' COMMENT ' 镜像地址或包地址 ',
    `es_version`  varchar(100)        NOT NULL DEFAULT '' COMMENT ' 版本标识 ',
    `creator`     varchar(100)        NOT NULL DEFAULT '' COMMENT ' 包创建人 ',
    `release`     tinyint(1)          NOT NULL DEFAULT '0' COMMENT '是否为发布版本',
    `manifest`    varchar(32)         NOT NULL DEFAULT '' COMMENT ' 类型 (3 docker/4 host)',
    `desc`        varchar(384)                 DEFAULT '' COMMENT ' 备注 ',
    `create_time` timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` tinyint(1)          NOT NULL DEFAULT '0' COMMENT '标记删除 0 未删 1 已删',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 318
  DEFAULT CHARSET = utf8 COMMENT ='程序包版本管理';

-- ----------------------------
-- Table structure for es_plugin
-- ----------------------------
CREATE TABLE `es_plugin`
(
    `id`                 bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id 主键自增',
    `name`               varchar(50)         NOT NULL DEFAULT '' COMMENT ' 插件名 ',
    `physic_cluster_ids` varchar(100)        NOT NULL DEFAULT '' COMMENT ' 物理集群 id',
    `version`            varchar(50)         NOT NULL DEFAULT '' COMMENT ' 插件版本 ',
    `url`                varchar(1024)       NOT NULL DEFAULT '' COMMENT ' 插件存储地址 ',
    `md5`                varchar(100)        NOT NULL DEFAULT '' COMMENT ' 插件文件 md5',
    `desc`               varchar(255)        NOT NULL DEFAULT '' COMMENT ' 插件描述 ',
    `creator`            varchar(100)        NOT NULL DEFAULT '' COMMENT ' 插件创建人 ',
    `p_default`          tinyint(1)          NOT NULL DEFAULT '0' COMMENT '是否为系统默认：0 否 1 是',
    `create_time`        timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `delete_flag`        tinyint(1)          NOT NULL DEFAULT '0' COMMENT '标记删除',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 416
  DEFAULT CHARSET = utf8 COMMENT ='es 插件包管理';

-- ----------------------------
-- Table structure for es_work_order_task
-- ----------------------------
CREATE TABLE `es_work_order_task`
(
    `id`                bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id 主键自增',
    `title`             varchar(100)        NOT NULL DEFAULT '' COMMENT ' 标题 ',
    `work_order_id`     bigint(20)          NOT NULL DEFAULT '-1' COMMENT '工单 id',
    `physic_cluster_id` bigint(20)          NOT NULL DEFAULT '-1' COMMENT '物理集群 id',
    `cluster_node_role` varchar(512)        NOT NULL DEFAULT '-1' COMMENT '集群节点角色',
    `task_ids`          varchar(128)        NOT NULL DEFAULT '' COMMENT ' 各角色任务 ids',
    `type`              varchar(50)         NOT NULL DEFAULT '' COMMENT ' 集群类型:3 docker 容器云 / 4 host 物理机 ',
    `order_type`        varchar(50)         NOT NULL DEFAULT '' COMMENT ' 工单类型 1 集群新增 2 集群扩容 3 集群缩容 4 集群重启 5 集群升级 ',
    `status`            varchar(20)         NOT NULL DEFAULT '' COMMENT ' 任务状态 ',
    `creator`           varchar(100)        NOT NULL DEFAULT '' COMMENT ' 工单创建人 ',
    `create_time`       timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag`       tinyint(1)          NOT NULL DEFAULT '0' COMMENT '标记删除',
    `handle_data`       longtext COMMENT '工单数据',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1958
  DEFAULT CHARSET = utf8 COMMENT ='es 工单任务表';

-- ----------------------------
-- Table structure for es_work_order_task_detail
-- ----------------------------
CREATE TABLE `es_work_order_task_detail`
(
    `id`                 bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id 主键自增',
    `work_order_task_id` bigint(20)          NOT NULL DEFAULT '-1' COMMENT '工单任务 id',
    `role`               varchar(100)        NOT NULL DEFAULT '' COMMENT ' 所属角色 ',
    `hostname`           varchar(100)        NOT NULL DEFAULT '' COMMENT ' 主机名称 /ip',
    `grp`                int(11)             NOT NULL DEFAULT '0' COMMENT '机器的分组',
    `idx`                int(11)             NOT NULL DEFAULT '0' COMMENT '机器在分组中的索引',
    `task_id`            bigint(20)          NOT NULL DEFAULT '-1' COMMENT '容器云 / 物理机 接口返回任务 id',
    `status`             varchar(20)         NOT NULL DEFAULT '' COMMENT ' 任务状态 ',
    `create_time`        timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`        timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag`        tinyint(1)          NOT NULL DEFAULT '0' COMMENT '标记删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_work_order_task_id_role_hostname_delete_flag` (`work_order_task_id`, `role`, `hostname`, `delete_flag`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 6592
  DEFAULT CHARSET = utf8 COMMENT ='es 工单任务详情表';

-- ----------------------------
-- Table structure for gateway_cluster_info
-- ----------------------------
CREATE TABLE `gateway_cluster_info`
(
    `id`           bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键 自增',
    `cluster_name` varchar(50)         NOT NULL DEFAULT '' COMMENT ' 集群名称 ',
    `create_time`  timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_cluster_name` (`cluster_name`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 188
  DEFAULT CHARSET = utf8 COMMENT ='gateway 集群信息';

-- ----------------------------
-- Table structure for gateway_cluster_node_info
-- ----------------------------
CREATE TABLE `gateway_cluster_node_info`
(
    `id`             bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键 自增',
    `cluster_name`   varchar(50)         NOT NULL DEFAULT '' COMMENT ' 集群名称 ',
    `host_name`      varchar(50)         NOT NULL DEFAULT '' COMMENT ' 主机名 ',
    `port`           int(10)             NOT NULL DEFAULT '-1' COMMENT '端口',
    `heartbeat_time` timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '心跳时间',
    `create_time`    timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_ip_port` (`host_name`, `port`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 490264
  DEFAULT CHARSET = utf8 COMMENT ='gateway 节点信息';

-- ----------------------------
-- Table structure for index_template_alias
-- ----------------------------
CREATE TABLE `index_template_alias`
(
    `id`                bigint(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键 自增',
    `index_template_id` int(10)             NOT NULL DEFAULT '-1' COMMENT '索引模板 id',
    `name`              varchar(50)         NOT NULL DEFAULT '' COMMENT ' 别名 ',
    `filterterm`        varchar(255)        NOT NULL DEFAULT '' COMMENT ' 过滤器 ',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 4
  DEFAULT CHARSET = utf8 COMMENT ='索引别名';

-- ----------------------------
-- Table structure for index_template_config
-- ----------------------------
CREATE TABLE `index_template_config`
(
    `is_source_separated`      tinyint(4)          NOT NULL DEFAULT '0' COMMENT '是否是索引处分分离的 0 不是 1 是',
    `idc_flags`                tinyint(1)          NOT NULL DEFAULT '0' COMMENT 'idc 标识',
    `adjust_rack_shard_factor` decimal(10, 2)      NOT NULL DEFAULT '1.00' COMMENT '模板 shard 的资源消耗因子',
    `mapping_improve_enable`   tinyint(4)          NOT NULL DEFAULT '1' COMMENT 'mapping 优化开关 1 开 0 关',
    `pre_create_flags`         tinyint(1)          NOT NULL DEFAULT '1' COMMENT '预创建标识',
    `update_time`              timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `disable_source_flags`     tinyint(1)          NOT NULL DEFAULT '0' COMMENT '禁用 source 标识',
    `disable_index_rollover`   tinyint(1)          NOT NULL DEFAULT '1' COMMENT '禁用 indexRollover 功能',
    `dynamic_limit_enable`     tinyint(4)          NOT NULL DEFAULT '1' COMMENT '动态限流开关 1 开 0 关',
    `logic_id`                 int(10)             NOT NULL DEFAULT '-1' COMMENT '逻辑模板 id',
    `create_time`              timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `shard_num`                int(11)             NOT NULL DEFAULT '1' COMMENT 'shard 数量',
    `adjust_rack_tps_factor`   decimal(10, 2)      NOT NULL DEFAULT '1.00' COMMENT '容量规划时，tps 的系数',
    `id`                       bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    PRIMARY KEY (`id`),
    KEY `idx_logic_id` (`logic_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1546
  DEFAULT CHARSET = utf8 COMMENT ='模板配置信息';

-- ----------------------------
-- Table structure for index_template_info
-- ----------------------------
CREATE TABLE `index_template_info`
(
    `id`                bigint(20) unsigned          NOT NULL AUTO_INCREMENT COMMENT '主键自增',
    `name`              varchar(128)                 NOT NULL DEFAULT '' COMMENT ' 名称 ',
    `data_type`         tinyint(4)                   NOT NULL DEFAULT '-1' COMMENT '数据类型',
    `date_format`       varchar(50)                  NOT NULL DEFAULT '' COMMENT ' 索引分区的时间后缀 ',
    `is_active`         tinyint(2)                   NOT NULL DEFAULT '1' COMMENT '有效标记',
    `data_center`       varchar(20)                  NOT NULL DEFAULT '' COMMENT ' 数据中心 ',
    `expire_time`       bigint(20)                   NOT NULL DEFAULT '-1' COMMENT '保存时长',
    `hot_time`          int(10)                      NOT NULL DEFAULT '-1' COMMENT '热数据保存时长',
    `responsible`       varchar(500)                          DEFAULT '' COMMENT ' 责任人 ',
    `date_field`        varchar(50)                  NOT NULL DEFAULT '' COMMENT ' 时间字段 ',
    `date_field_format` varchar(128)                 NOT NULL DEFAULT '' COMMENT ' 时间字段的格式 ',
    `id_field`          varchar(512)                          DEFAULT '' COMMENT 'id 字段 ',
    `routing_field`     varchar(512)                          DEFAULT '' COMMENT 'routing 字段 ',
    `expression`        varchar(100)                 NOT NULL DEFAULT '' COMMENT ' 索引表达式 ',
    `desc`              varchar(1000)                NOT NULL DEFAULT '' COMMENT ' 索引描述 ',
    `quota`             decimal(10, 3)               NOT NULL DEFAULT '-1.000' COMMENT '规格',
    `project_id`        int(10)                      NOT NULL DEFAULT '-1' COMMENT 'project_id',
    `ingest_pipeline`   varchar(512)                 NOT NULL DEFAULT '' COMMENT 'ingest_pipeline',
    `block_read`        tinyint(1) unsigned zerofill NOT NULL DEFAULT '0' COMMENT '是否禁读，0：否，1：是',
    `block_write`       tinyint(1) unsigned zerofill NOT NULL DEFAULT '0' COMMENT '是否禁写，0：否，1：是',
    `create_time`       timestamp                    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       timestamp                    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `write_rate_limit`  bigint(255)                  NOT NULL DEFAULT '-1' COMMENT '写入限流值',
    `resource_id`       bigint(20)                   NOT NULL DEFAULT '-1' COMMENT '逻辑集群 id',
    `check_point_diff`  bigint(100)                  NOT NULL DEFAULT '0' COMMENT 'dcdr 位点差',
    `level`             tinyint(4)                   NOT NULL DEFAULT '1' COMMENT '服务等级分为 1,2,3',
    `has_dcdr`          tinyint(1) unsigned zerofill NOT NULL DEFAULT '0' COMMENT '是否开启 dcdr',
    `open_srv`          varchar(255)                          DEFAULT NULL COMMENT '已开启的模板服务',
    `disk_size`         decimal(10, 3)                        DEFAULT '-1.000' COMMENT '可用磁盘容量',
    `health`            int(11)                               DEFAULT '-1' COMMENT '模版健康；-1 是 UNKNOW',
    PRIMARY KEY (`id`),
    KEY `idx_data_center` (`data_center`),
    KEY `idx_is_active` (`is_active`),
    KEY `idx_name` (`name`),
    KEY `idx_project_id` (`project_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 25998
  DEFAULT CHARSET = utf8 COMMENT ='逻辑索引模板表';

-- ----------------------------
-- Table structure for index_template_physical_info
-- ----------------------------
CREATE TABLE `index_template_physical_info`
(
    `id`            bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键自增',
    `logic_id`      int(10)             NOT NULL DEFAULT '-1' COMMENT '逻辑模板 id',
    `name`          varchar(128)        NOT NULL DEFAULT '' COMMENT ' 模板名字 ',
    `expression`    varchar(128)        NOT NULL DEFAULT '' COMMENT ' 表达式 ',
    `cluster`       varchar(128)        NOT NULL DEFAULT '' COMMENT ' 集群名字 ',
    `rack`          varchar(512)        NOT NULL DEFAULT '' COMMENT 'rack',
    `shard`         int(10)             NOT NULL DEFAULT '1' COMMENT 'shard 个数',
    `shard_routing` int(10)             NOT NULL DEFAULT '1' COMMENT '内核的逻辑 shard',
    `version`       int(10)             NOT NULL DEFAULT '0' COMMENT '版本',
    `role`          tinyint(4)          NOT NULL DEFAULT '1' COMMENT '角色 1master 2slave',
    `status`        tinyint(4)          NOT NULL DEFAULT '1' COMMENT '1 常规 -1 索引删除中 -2 已删除',
    `config`        text COMMENT '配置 json 格式',
    `create_time`   timestamp           NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   timestamp           NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `region_id`     int(10)             NOT NULL DEFAULT '-1' COMMENT '模板关联的 regionId',
    PRIMARY KEY (`id`),
    KEY `idx_cluster_name_status` (`cluster`, `name`, `status`),
    KEY `idx_log_id_statud` (`logic_id`, `status`),
    KEY `idx_logic_id` (`logic_id`),
    KEY `idx_region_id` (`region_id`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 23700
  DEFAULT CHARSET = utf8 COMMENT ='物理模板信息';

-- ----------------------------
-- Table structure for index_template_type
-- ----------------------------
CREATE TABLE `index_template_type`
(
    `id`                  bigint(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键 自增',
    `index_template_id`   int(10)             NOT NULL DEFAULT '-1' COMMENT '索引模板 id',
    `index_template_name` varchar(100)        NOT NULL DEFAULT '' COMMENT ' 索引模板名称 ',
    `name`                varchar(100)        NOT NULL DEFAULT '' COMMENT 'type 名称 ',
    `id_field`            varchar(128)        NOT NULL DEFAULT '' COMMENT 'id 字段 ',
    `routing`             varchar(100)        NOT NULL DEFAULT '' COMMENT 'routing 字段 ',
    `source`              tinyint(4)          NOT NULL DEFAULT '1' COMMENT '0 不存 source 1 存 source',
    `is_active`           tinyint(2)          NOT NULL DEFAULT '1' COMMENT '是否激活 1 是 0 否',
    `create_time`         timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`         timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='索引模板 type';

-- ----------------------------
-- Table structure for logi_job
-- ----------------------------
CREATE TABLE `logi_job`
(
    `id`          bigint(20)   NOT NULL AUTO_INCREMENT,
    `job_code`    varchar(100) NOT NULL DEFAULT '' COMMENT 'task taskCode',
    `task_code`   varchar(255) NOT NULL DEFAULT '' COMMENT ' 任务 code',
    `class_name`  varchar(255) NOT NULL DEFAULT '' COMMENT ' 类的全限定名 ',
    `try_times`   int(10)      NOT NULL DEFAULT '0' COMMENT '第几次重试',
    `worker_code` varchar(200) NOT NULL DEFAULT '' COMMENT ' 执行机器 ',
    `app_name`    varchar(100) NOT NULL DEFAULT '' COMMENT ' 被调度的应用名称 ',
    `start_time`  datetime              DEFAULT '1971-01-01 00:00:00' COMMENT '开始时间',
    `create_time` datetime              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `job_code` (`job_code`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 381677
  DEFAULT CHARSET = utf8 COMMENT ='正在执行的 job 信息';

-- ----------------------------
-- Table structure for logi_job_log
-- ----------------------------
CREATE TABLE `logi_job_log`
(
    `id`          bigint(20)   NOT NULL AUTO_INCREMENT,
    `job_code`    varchar(100) NOT NULL DEFAULT '' COMMENT 'job taskCode',
    `task_code`   varchar(255) NOT NULL DEFAULT '' COMMENT ' 任务 code',
    `task_name`   varchar(255) NOT NULL DEFAULT '' COMMENT ' 任务名称 ',
    `task_desc`   varchar(255) NOT NULL DEFAULT '' COMMENT ' 任务描述 ',
    `task_id`     bigint(20)   NOT NULL DEFAULT '0' COMMENT '任务 id',
    `class_name`  varchar(255) NOT NULL DEFAULT '' COMMENT ' 类的全限定名 ',
    `try_times`   int(10)      NOT NULL DEFAULT '0' COMMENT '第几次重试',
    `worker_code` varchar(200) NOT NULL DEFAULT '' COMMENT ' 执行机器 ',
    `worker_ip`   varchar(200) NOT NULL DEFAULT '' COMMENT ' 执行机器 ip',
    `start_time`  datetime              DEFAULT '1971-01-01 00:00:00' COMMENT '开始时间',
    `end_time`    datetime              DEFAULT '1971-01-01 00:00:00' COMMENT '结束时间',
    `status`      tinyint(4)   NOT NULL DEFAULT '0' COMMENT '执行结果 1 成功 2 失败 3 取消',
    `error`       text         NOT NULL COMMENT '错误信息',
    `result`      text         NOT NULL COMMENT '执行结果',
    `app_name`    varchar(100) NOT NULL DEFAULT '' COMMENT ' 被调度的应用名称 ',
    `create_time` datetime              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_job_code` (`job_code`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 381395
  DEFAULT CHARSET = utf8 COMMENT ='job 执行历史日志';



-- ----------------------------
-- Table structure for logi_task
-- ----------------------------
CREATE TABLE `logi_task`
(
    `id`              bigint(20)    NOT NULL AUTO_INCREMENT,
    `task_code`       varchar(100)  NOT NULL DEFAULT '' COMMENT 'task taskCode',
    `task_name`       varchar(255)  NOT NULL DEFAULT '' COMMENT ' 名称 ',
    `task_desc`       varchar(1000) NOT NULL DEFAULT '' COMMENT ' 任务描述 ',
    `cron`            varchar(100)  NOT NULL DEFAULT '' COMMENT 'cron 表达式 ',
    `class_name`      varchar(255)  NOT NULL DEFAULT '' COMMENT ' 类的全限定名 ',
    `params`          varchar(1000) NOT NULL DEFAULT '' COMMENT ' 执行参数 map 形式 {key1:value1,key2:value2}',
    `retry_times`     int(10)       NOT NULL DEFAULT '0' COMMENT '允许重试次数',
    `last_fire_time`  datetime               DEFAULT CURRENT_TIMESTAMP COMMENT '上次执行时间',
    `timeout`         bigint(20)    NOT NULL DEFAULT '0' COMMENT '超时 毫秒',
    `status`          tinyint(4)    NOT NULL DEFAULT '0' COMMENT '1 等待 2 运行中 3 暂停',
    `sub_task_codes`  varchar(1000) NOT NULL DEFAULT '' COMMENT ' 子任务 code 列表, 逗号分隔 ',
    `consensual`      varchar(200)  NOT NULL DEFAULT '' COMMENT ' 执行策略 ',
    `owner`           varchar(200)  NOT NULL DEFAULT '' COMMENT ' 责任人 ',
    `task_worker_str` varchar(3000) NOT NULL DEFAULT '' COMMENT ' 机器执行信息 ',
    `app_name`        varchar(100)  NOT NULL DEFAULT '' COMMENT ' 被调度的应用名称 ',
    `node_name_white_list_str` VARCHAR(3000) DEFAULT '' NOT NULL COMMENT '执行节点名对应白名单集',
    `create_time`     datetime               DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     datetime               DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `task_code` (`task_code`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 548
  DEFAULT CHARSET = utf8 COMMENT ='任务信息';

-- ----------------------------
-- Table structure for logi_task_lock
-- ----------------------------
CREATE TABLE `logi_task_lock`
(
    `id`          bigint(20)   NOT NULL AUTO_INCREMENT,
    `task_code`   varchar(100) NOT NULL DEFAULT '' COMMENT 'task taskCode',
    `worker_code` varchar(100) NOT NULL DEFAULT '' COMMENT 'worker taskCode',
    `app_name`    varchar(100) NOT NULL DEFAULT '' COMMENT ' 被调度的应用名称 ',
    `expire_time` bigint(20)   NOT NULL DEFAULT '0' COMMENT '过期时间',
    `create_time` datetime              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uni_task_app` (`task_code`, `app_name`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 569
  DEFAULT CHARSET = utf8 COMMENT ='任务锁';

-- ----------------------------
-- Table structure for logi_worker
-- ----------------------------
CREATE TABLE `logi_worker`
(
    `id`              bigint(20)   NOT NULL AUTO_INCREMENT,
    `worker_code`     varchar(100) NOT NULL DEFAULT '' COMMENT 'worker taskCode',
    `worker_name`     varchar(100) NOT NULL DEFAULT '' COMMENT 'worker 名 ',
    `ip`              varchar(100) NOT NULL DEFAULT '' COMMENT 'worker 的 ip',
    `cpu`             int(11)      NOT NULL DEFAULT '0' COMMENT 'cpu 数量',
    `cpu_used`        double       NOT NULL DEFAULT '0' COMMENT 'cpu 使用率',
    `memory`          double       NOT NULL DEFAULT '0' COMMENT '内存, 以 M 为单位',
    `memory_used`     double       NOT NULL DEFAULT '0' COMMENT '内存使用率',
    `jvm_memory`      double       NOT NULL DEFAULT '0' COMMENT 'jvm 堆大小，以 M 为单位',
    `jvm_memory_used` double       NOT NULL DEFAULT '0' COMMENT 'jvm 堆使用率',
    `job_num`         int(10)      NOT NULL DEFAULT '0' COMMENT '正在执行 job 数',
    `heartbeat`       datetime              DEFAULT '1971-01-01 00:00:00' COMMENT '心跳时间',
    `app_name`        varchar(100) NOT NULL DEFAULT '' COMMENT ' 被调度的应用名称 ',
    `create_time`     datetime              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     datetime              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `node_name`     VARCHAR(100) DEFAULT '' NOT NULL COMMENT 'node 名',
    PRIMARY KEY (`id`),
    UNIQUE KEY `worker_code` (`worker_code`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 8
  DEFAULT CHARSET = utf8 COMMENT ='worker 信息';

-- ----------------------------
-- Table structure for logi_worker_blacklist
-- ----------------------------
CREATE TABLE `logi_worker_blacklist`
(
    `id`          bigint(20)   NOT NULL AUTO_INCREMENT,
    `worker_code` varchar(100) NOT NULL DEFAULT '' COMMENT 'worker taskCode',
    `create_time` datetime              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `worker_code` (`worker_code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='worker 黑名单列表';

-- ----------------------------
-- Table structure for operate_record_info
-- ----------------------------
CREATE TABLE `operate_record_info`
(
    `id`             bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键 自增',
    `project_name`   varchar(255)                 DEFAULT NULL COMMENT '应用',
    `module_id`      int(10)             NOT NULL DEFAULT '-1' COMMENT '模块 id',
    `operate_id`     int(10)             NOT NULL DEFAULT '-1' COMMENT '操作 id',
    `trigger_way_id` int(11)                      DEFAULT NULL COMMENT '触发方式',
    `user_operation` varchar(50)         NOT NULL DEFAULT '' COMMENT ' 操作人 ',
    `content`        longtext COMMENT '操作内容',
    `operate_time`   timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    `create_time`    timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `biz_id`         varchar(255)                 DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 8218
  DEFAULT CHARSET = utf8 COMMENT ='操作记录表';


-- ----------------------------
-- Table structure for project_arius_resource_logic
-- ----------------------------
CREATE TABLE `project_arius_resource_logic`
(
    `id`                 bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键自增',
    `name`               varchar(128)        NOT NULL DEFAULT '' COMMENT ' 资源名称 ',
    `type`               tinyint(4)          NOT NULL DEFAULT '2' COMMENT '资源类型 1 共享公共资源 2 独享资源',
    `project_id`         varchar(1024)       NOT NULL DEFAULT '-1' COMMENT '资源所属的 project_id',
    `data_center`        varchar(20)         NOT NULL DEFAULT '' COMMENT ' 数据中心 cn/us01',
    `responsible`        varchar(128)                 DEFAULT '' COMMENT ' 资源责任人 ',
    `memo`               varchar(512)        NOT NULL DEFAULT '' COMMENT ' 资源备注 ',
    `quota`              decimal(8, 2)       NOT NULL DEFAULT '1.00' COMMENT '资源的大小',
    `level`              tinyint(4)          NOT NULL DEFAULT '1' COMMENT '服务等级 1 normal 2 important 3 vip',
    `config_json`        varchar(1024)       NOT NULL DEFAULT '' COMMENT ' 集群配置 ',
    `create_time`        timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`        timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `health`             tinyint(4)          NOT NULL DEFAULT '3' COMMENT '集群状态 1 green 2 yellow 3 red -1 未知',
    `data_node_spec`     varchar(20)         NOT NULL DEFAULT '' COMMENT ' 节点规格 ',
    `disk_total`         bigint(50)          NOT NULL DEFAULT '0' COMMENT '集群磁盘总量 单位 byte',
    `disk_usage`         bigint(50)          NOT NULL DEFAULT '0' COMMENT '集群磁盘使用量 单位 byte',
    `disk_usage_percent` decimal(10, 5)      default NULL COMMENT '集群磁盘空闲率 单位 0 ~1',
    `es_cluster_version` varchar(20)         default NULL COMMENT 'es 集群版本',
    `node_num`           int(10)             NOT NULL DEFAULT '0' COMMENT '节点个数',
    `data_node_num`      int(10)             NOT NULL DEFAULT '0' COMMENT '节点个数',
    PRIMARY KEY (`id`),
    KEY `idx_name` (`name`),
    KEY `idx_project_id` (`project_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 3922
  DEFAULT CHARSET = utf8 COMMENT ='逻辑资源信息';

-- ----------------------------
-- Table structure for project_logi_cluster_auth
-- ----------------------------
CREATE TABLE `project_logi_cluster_auth`
(
    `id`               bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键自增',
    `project_id`       int(10)             NOT NULL DEFAULT '-1' COMMENT '项目 id',
    `logic_cluster_id` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '逻辑集群 id',
    `type`             int(10)             NOT NULL DEFAULT '-1' COMMENT '权限类型，0- 超管，1- 配置管理，2- 访问，-1- 无权限',
    `responsible`      varchar(100)                 DEFAULT '' COMMENT ' 责任人 id 列表 ',
    `status`           int(10)             NOT NULL DEFAULT '1' COMMENT '状态 1 有效 0 无效',
    `create_time`      timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_project_id` (`project_id`),
    KEY `idx_logic_cluster_id` (`logic_cluster_id`),
    KEY `idx_status` (`status`),
    KEY `idx_type` (`type`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='project 逻辑集群权限';

-- ----------------------------
-- Table structure for project_template_info
-- ----------------------------
CREATE TABLE `project_template_info`
(
    `id`          bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键自增',
    `project_id`  int(10)             NOT NULL DEFAULT '-1' COMMENT '项目 id',
    `template`    varchar(100)        NOT NULL DEFAULT '' COMMENT ' 模板名称, 不能关联模板 id 模板会跨集群迁移，id 会变 ',
    `type`        int(10)             NOT NULL DEFAULT '-1' COMMENT 'appid 的权限 1 读写 2 读 -1 未知',
    `status`      int(10)             NOT NULL DEFAULT '1' COMMENT '状态 1 有效 0 无效',
    `create_time` timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_project_id` (`project_id`),
    KEY `idx_status` (`status`),
    KEY `idx_template_id` (`template`),
    KEY `idx_type` (`type`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='project 模板信息';

-- ----------------------------
-- Table structure for user_metrics_config_info
-- ----------------------------
CREATE TABLE `user_metrics_config_info` (
                                            `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                            `user_name` varchar(255) NOT NULL COMMENT '用户账号',
                                            `metric_info` text COMMENT '指标看板的配置',
                                            `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                                            `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                                            PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1602 DEFAULT CHARSET=utf8 COMMENT='用户关联到指标的配置信息表';


#权限点和角色的初始化数据
insert into kf_security_role_permission (id, role_id, permission_id, create_time, update_time, is_delete, app_name)
values (1597, 1, 0, '2022-06-01 21:19:42.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1935, 1, 1593, '2022-06-14 17:41:03.0', '2022-08-27 17:36:58.0', 0, 'know_search'),
       (1937, 1, 1637, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1939, 1, 1639, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1941, 1, 1641, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1943, 1, 1643, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1945, 1, 1645, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1947, 1, 1647, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1949, 1, 1649, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1951, 1, 1651, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1953, 1, 1653, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1955, 1, 1655, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1957, 1, 1657, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1959, 1, 1659, '2022-06-14 17:41:03.0', '2022-08-25 10:33:59.0', 1, 'know_search'),
       (1961, 1, 1661, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1963, 1, 1597, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1965, 1, 1673, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1967, 1, 1675, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1969, 1, 1677, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1971, 1, 1679, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1973, 1, 1599, '2022-06-14 17:41:03.0', '2022-08-25 10:36:08.0', 1, 'know_search'),
       (1975, 1, 1681, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1977, 1, 1683, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1979, 1, 1685, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1981, 1, 1687, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1983, 1, 1601, '2022-06-14 17:41:03.0', '2022-08-25 10:36:44.0', 1, 'know_search'),
       (1985, 1, 1689, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1987, 1, 1691, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1989, 1, 1693, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1991, 1, 1695, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1993, 1, 1697, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1995, 1, 1699, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1997, 1, 1603, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (1999, 1, 1701, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2001, 1, 1703, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2003, 1, 1705, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2005, 1, 1707, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2007, 1, 1709, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2009, 1, 1711, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2011, 1, 1713, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2013, 1, 1715, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2015, 1, 1717, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2017, 1, 1719, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2019, 1, 1721, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2021, 1, 1723, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2023, 1, 1605, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2025, 1, 1725, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2027, 1, 1727, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2029, 1, 1729, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2031, 1, 1731, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2033, 1, 1733, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2035, 1, 1735, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2037, 1, 1737, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2039, 1, 1739, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2041, 1, 1741, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2043, 1, 1743, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2045, 1, 1607, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2047, 1, 1745, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2049, 1, 1747, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2051, 1, 1749, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2053, 1, 1751, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2055, 1, 1753, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2057, 1, 1755, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2059, 1, 1609, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2061, 1, 1757, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2063, 1, 1855, '2022-06-14 17:41:03.0', '2022-08-25 10:34:13.0', 0, 'know_search'),
       (2065, 1, 1857, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2067, 1, 1611, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2069, 1, 1759, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2071, 1, 1859, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2073, 1, 1861, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2075, 1, 1863, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2077, 1, 1865, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2079, 1, 1867, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2081, 1, 1613, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2083, 1, 1761, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2085, 1, 1615, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2087, 1, 1763, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2089, 1, 1619, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2091, 1, 1769, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2093, 1, 1771, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2095, 1, 1773, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2097, 1, 1621, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2099, 1, 1775, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2101, 1, 1777, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2103, 1, 1779, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2105, 1, 1781, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2107, 1, 1783, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2109, 1, 1785, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2111, 1, 1787, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2113, 1, 1789, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2115, 1, 1791, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2117, 1, 1793, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2119, 1, 1795, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2121, 1, 1797, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2123, 1, 1799, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2125, 1, 1801, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2127, 1, 1623, '2022-06-14 17:41:03.0', '2022-08-27 17:34:08.0', 0, 'know_search'),
       (2129, 1, 1803, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2131, 1, 1805, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2133, 1, 1807, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2135, 1, 1809, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2137, 1, 1625, '2022-06-14 17:41:03.0', '2022-08-27 17:34:08.0', 0, 'know_search'),
       (2139, 1, 1811, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2141, 1, 1813, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2143, 1, 1815, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2145, 1, 1817, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2147, 1, 1627, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2149, 1, 1819, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2151, 1, 1821, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2153, 1, 1629, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2155, 1, 1823, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2157, 1, 1825, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2159, 1, 1827, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2161, 1, 1829, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2163, 1, 1831, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2165, 1, 1631, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2167, 1, 1833, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2169, 1, 1835, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2171, 1, 1837, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2173, 1, 1839, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2175, 1, 1841, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2177, 1, 1633, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2179, 1, 1843, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2181, 1, 1845, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2183, 1, 1847, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2185, 1, 1849, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2187, 1, 1851, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2189, 1, 1635, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2191, 1, 1853, '2022-06-14 17:41:03.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2193, 2, 1595, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2195, 2, 1663, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2197, 2, 1665, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2199, 2, 1667, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2201, 2, 1669, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2203, 2, 1671, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2205, 2, 1601, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2207, 2, 1689, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2209, 2, 1691, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2211, 2, 1693, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2213, 2, 1695, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2215, 2, 1697, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2217, 2, 1699, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2219, 2, 1605, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2221, 2, 1725, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2223, 2, 1727, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2225, 2, 1729, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2227, 2, 1731, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2229, 2, 1733, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2231, 2, 1735, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2233, 2, 1737, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2235, 2, 1739, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2237, 2, 1741, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2239, 2, 1743, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2241, 2, 1609, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2243, 2, 1757, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2245, 2, 1855, '2022-06-14 18:08:56.0', '2022-08-25 10:33:12.0', 1, 'know_search'),
       (2247, 2, 1857, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2249, 2, 1611, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2251, 2, 1759, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2253, 2, 1859, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2255, 2, 1861, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2257, 2, 1863, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2259, 2, 1865, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2261, 2, 1867, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2263, 2, 1613, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2265, 2, 1761, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2267, 2, 1615, '2022-06-14 18:08:56.0', '2022-08-25 20:27:55.0', 1, 'know_search'),
       (2269, 2, 1763, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2271, 2, 1617, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2273, 2, 1765, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2275, 2, 1767, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2277, 2, 1631, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2279, 2, 1833, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2281, 2, 1835, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2283, 2, 1837, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2285, 2, 1839, '2022-06-14 18:08:56.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (2287, 2, 1841, '2022-06-14 18:08:56.0', '2022-08-26 17:59:49.0', 1, 'know_search'),
       (2643, 1, 1595, '2022-06-17 16:39:23.0', '2022-08-25 10:35:06.0', 1, 'know_search'),
       (4505, 1, 1869, '2022-07-04 15:45:59.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (4507, 1, 1871, '2022-07-04 15:46:56.0', '2022-08-27 17:37:22.0', 0, 'know_search'),
       (5275, 1, 1873, '2022-06-17 15:53:54.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (5277, 2, 1873, '2022-06-17 15:53:54.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (5349, 1, 1875, '2022-06-17 15:53:54.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (5591, 1, 1759, '2022-08-11 10:39:01.0', '2022-08-25 10:31:42.0', 0, 'know_search'),
       (5593, 2, 1759, '2022-08-11 10:39:59.0', '2022-08-25 10:31:42.0', 0, 'know_search');
#权限点初始化数据
insert into kf_security_permission (id, permission_name, parent_id, leaf, level, description, create_time, update_time, is_delete, app_name)
values  (1593, '物理集群', 0, 0, 1, '物理集群', '2022-05-24 18:08:22.0', '2022-08-24 20:07:31.0', 0, 'know_search'),
        (1595, '我的集群', 0, 0, 1, '我的集群', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1597, '集群版本', 0, 0, 1, '集群版本', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1599, 'Gateway管理', 0, 0, 1, 'Gateway管理', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1601, '模板管理', 0, 0, 1, '模板管理', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1603, '模板服务', 0, 0, 1, '模板服务', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1605, '索引管理', 0, 0, 1, '索引管理', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1607, '索引服务', 0, 0, 1, '索引服务', '2022-05-24 18:08:22.0', '2022-05-24 18:24:16.0', 0, 'know_search'),
        (1609, '索引查询', 0, 0, 1, '索引查询', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1611, '查询诊断', 0, 0, 1, '查询诊断', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1613, '集群看板', 0, 0, 1, '集群看板', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1615, '网关看板', 0, 0, 1, '网关看板', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1617, '我的申请', 0, 0, 1, '我的申请', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1619, '我的审批', 0, 0, 1, '我的审批', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1621, '任务列表', 0, 0, 1, '任务列表', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1623, '调度任务列表', 0, 0, 1, '调度任务列表', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1625, '调度日志', 0, 0, 1, '调度日志', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1627, '用户管理', 0, 0, 1, '用户管理', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1629, '角色管理', 0, 0, 1, '角色管理', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1631, '应用管理', 0, 0, 1, '应用管理', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1633, '平台配置', 0, 0, 1, '平台配置', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1635, '操作记录', 0, 0, 1, '操作记录', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1637, '查看集群列表及详情', 1593, 1, 2, '查看集群列表及详情', '2022-05-24 18:08:22.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1639, '接入集群', 1593, 1, 2, '接入集群', '2022-05-24 18:08:22.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1641, '新建集群', 1593, 1, 2, '新建集群', '2022-05-24 18:08:22.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1643, '扩缩容', 1593, 1, 2, '扩缩容', '2022-05-24 18:08:22.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1645, '升级', 1593, 1, 2, '升级', '2022-05-24 18:08:22.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1647, '重启', 1593, 1, 2, '重启', '2022-05-24 18:08:23.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1649, '配置变更', 1593, 1, 2, '配置变更', '2022-05-24 18:08:23.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1651, 'Region划分', 1593, 1, 2, 'Region划分', '2022-05-24 18:08:23.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1653, 'Region管理', 1593, 1, 2, 'Region管理', '2022-05-24 18:08:23.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1655, '快捷命令', 1593, 1, 2, '快捷命令', '2022-05-24 18:08:23.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1657, '编辑', 1593, 1, 2, '编辑', '2022-05-24 18:08:23.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1659, '绑定Gateway', 1593, 1, 2, '绑定Gateway', '2022-05-24 18:08:23.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1661, '下线', 1593, 1, 2, '下线', '2022-05-24 18:08:23.0', '2022-05-24 18:10:52.0', 0, 'know_search'),
        (1663, '查看集群列表及详情', 1595, 1, 2, '查看集群列表及详情', '2022-05-24 18:08:23.0', '2022-05-24 18:10:52.0', 0, 'know_search'),
        (1665, '申请集群', 1595, 1, 2, '申请集群', '2022-05-24 18:08:23.0', '2022-05-24 18:10:52.0', 0, 'know_search'),
        (1667, '编辑', 1595, 1, 2, '编辑', '2022-05-24 18:08:23.0', '2022-05-24 18:10:52.0', 0, 'know_search'),
        (1669, '扩缩容', 1595, 1, 2, '扩缩容', '2022-05-24 18:08:23.0', '2022-05-24 18:10:52.0', 0, 'know_search'),
        (1671, '下线', 1595, 1, 2, '下线', '2022-05-24 18:08:23.0', '2022-05-24 18:20:44.0', 0, 'know_search'),
        (1673, '查看版本列表', 1597, 1, 2, '查看版本列表', '2022-05-24 18:08:23.0', '2022-05-24 18:20:44.0', 0, 'know_search'),
        (1675, '新增版本', 1597, 1, 2, '新增版本', '2022-05-24 18:08:23.0', '2022-05-24 18:20:44.0', 0, 'know_search'),
        (1677, '编辑', 1597, 1, 2, '编辑', '2022-05-24 18:08:23.0', '2022-05-24 18:20:44.0', 0, 'know_search'),
        (1679, '删除', 1597, 1, 2, '删除', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1681, '查看Gateway 集群列表', 1599, 1, 2, '查看Gateway 集群列表', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1683, '接入gateway', 1599, 1, 2, '接入gateway', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1685, '编辑', 1599, 1, 2, '编辑', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1687, '下线', 1599, 1, 2, '下线', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1689, '查看模板列表及详情', 1601, 1, 2, '查看模板列表及详情', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1691, '申请模板', 1601, 1, 2, '申请模板', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1693, '编辑', 1601, 1, 2, '编辑', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1695, '下线', 1601, 1, 2, '下线', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1697, '编辑Mapping', 1601, 1, 2, '编辑Mapping', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1699, '编辑Setting', 1601, 1, 2, '编辑Setting', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1701, '查看模板列表', 1603, 1, 2, '查看模板列表', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1703, '开关：预创建', 1603, 1, 2, '开关：预创建', '2022-05-24 18:08:23.0', '2022-06-14 16:49:48.0', 0, 'know_search'),
        (1705, '开关：过期删除', 1603, 1, 2, '开关：过期删除', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1707, '开关：冷热分离', 1603, 1, 2, '开关：冷热分离', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1709, '开关：pipeline', 1603, 1, 2, '开关：写入限流', '2022-05-24 18:08:23.0', '2022-06-14 16:49:49.0', 0, 'know_search'),
        (1711, '开关：Rollover', 1603, 1, 2, '开关：Rollover', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1713, '查看DCDR链路', 1603, 1, 2, '查看DCDR链路', '2022-05-24 18:08:23.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1715, '创建DCDR链路', 1603, 1, 2, '创建DCDR链路', '2022-05-24 18:08:24.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1717, '清理', 1603, 1, 2, '清理', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1719, '扩缩容', 1603, 1, 2, '扩缩容', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1721, '升版本', 1603, 1, 2, '升版本', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1723, '批量操作', 1603, 1, 2, '批量操作', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1725, '查看索引列表及详情', 1605, 1, 2, '查看索引列表及详情', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1727, '编辑Mapping', 1605, 1, 2, '编辑Mapping', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1729, '编辑Setting', 1605, 1, 2, '编辑Setting', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1731, '禁用读', 1607, 1, 2, '禁用读', '2022-05-24 18:08:24.0', '2022-07-15 08:50:56.0', 0, 'know_search'),
        (1733, '禁用写', 1607, 1, 2, '禁用写', '2022-05-24 18:08:24.0', '2022-07-15 08:50:56.0', 0, 'know_search'),
        (1735, '设置别名', 1605, 1, 2, '设置别名', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1737, '删除别名', 1605, 1, 2, '删除别名', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1739, '关闭索引', 1607, 1, 2, '关闭索引', '2022-05-24 18:08:24.0', '2022-07-15 09:52:25.0', 0, 'know_search'),
        (1741, '下线', 1605, 1, 2, '下线', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1743, '批量删除', 1605, 1, 2, '批量删除', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1745, '查看列表', 1607, 1, 2, '查看列表', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1747, '执行Rollover', 1607, 1, 2, '执行Rollover', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1749, '执行shrink', 1607, 1, 2, '执行shrink', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1751, '执行split', 1607, 1, 2, '执行split', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1753, '执行ForceMerge', 1607, 1, 2, '执行ForceMerge', '2022-05-24 18:08:24.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1755, '批量执行', 1607, 1, 2, '批量执行', '2022-05-24 18:08:24.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1757, 'DSL查询', 1609, 1, 2, 'DSL查询', '2022-05-24 18:08:24.0', '2022-06-14 16:39:48.0', 0, 'know_search'),
        (1759, '查询模板', 0, 0, 1, '查看查询模板列表', '2022-05-24 18:08:24.0', '2022-08-11 10:37:43.0', 0, 'know_search'),
        (1761, '查看集群看板', 1613, 1, 2, '查看集群看板', '2022-05-24 18:08:24.0', '2022-06-14 16:37:54.0', 0, 'know_search'),
        (1763, '查看网关看板', 1615, 1, 2, '查看网关看板', '2022-05-24 18:08:24.0', '2022-06-14 16:38:14.0', 0, 'know_search'),
        (1765, '查看我的申请列表', 1617, 1, 2, '查看我的申请列表', '2022-05-24 18:08:24.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1767, '撤回', 1617, 1, 2, '撤回', '2022-05-24 18:08:24.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1769, '查看我的审批列表', 1619, 1, 2, '查看我的审批列表', '2022-05-24 18:08:24.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1771, '驳回', 1619, 1, 2, '撤回', '2022-05-24 18:08:24.0', '2022-07-18 20:57:33.0', 0, 'know_search'),
        (1773, '通过', 1619, 1, 2, '通过', '2022-05-24 18:08:24.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1775, '查看任务列表', 1621, 1, 2, '查看任务列表', '2022-05-24 18:08:24.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1777, '查看进度', 1621, 1, 2, '查看进度', '2022-05-24 18:08:24.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1779, '执行', 1621, 1, 2, '执行', '2022-05-24 18:08:24.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1781, '暂停', 1621, 1, 2, '暂停', '2022-05-24 18:08:24.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1783, '重试', 1621, 1, 2, '重试', '2022-05-24 18:08:25.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1785, '取消', 1621, 1, 2, '取消', '2022-05-24 18:08:25.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1787, '查看日志（子任务）', 1621, 1, 2, '查看日志（子任务）', '2022-05-24 18:08:25.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1789, '重试（子任务）', 1621, 1, 2, '重试（子任务）', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1791, '忽略（子任务）', 1621, 1, 2, '忽略（子任务）', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1793, '查看详情（DCDR）', 1621, 1, 2, '查看详情（DCDR）', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1795, '取消（DCDR）', 1621, 1, 2, '取消（DCDR）', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1797, '重试（DCDR）', 1621, 1, 2, '重试（DCDR）', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1799, '强切（DCDR）', 1621, 1, 2, '强切（DCDR）', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1801, '返回（DCDR）', 1621, 1, 2, '返回（DCDR）', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1803, '查看任务列表', 1623, 1, 2, '查看任务列表', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1805, '查看日志', 1623, 1, 2, '查看日志', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1807, '执行', 1623, 1, 2, '执行', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1809, '暂停', 1623, 1, 2, '暂停', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1811, '查看调度日志列表', 1625, 1, 2, '查看调度日志列表', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1813, '调度详情', 1625, 1, 2, '调度详情', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1815, '执行日志', 1625, 1, 2, '执行日志', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1817, '终止任务', 1625, 1, 2, '终止任务', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1819, '查看用户列表', 1627, 1, 2, '查看用户列表', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1821, '分配角色', 1627, 1, 2, '分配角色', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1823, '查看角色列表', 1629, 1, 2, '查看角色列表', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1825, '编辑', 1629, 1, 2, '编辑', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1827, '绑定用户', 1629, 1, 2, '绑定用户', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1829, '回收用户', 1629, 1, 2, '回收用户', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1831, '删除角色', 1629, 1, 2, '删除角色', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1833, '查看应用列表', 1631, 1, 2, '查看应用列表', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1835, '新建应用', 1631, 1, 2, '新建应用', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1837, '编辑', 1631, 1, 2, '编辑', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1839, '删除', 1631, 1, 2, '删除', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1841, '访问设置', 1631, 1, 2, '访问设置', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1843, '查看平台配置列表', 1633, 1, 2, '查看平台配置列表', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1845, '新增平台配置', 1633, 1, 2, '新增平台配置', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1847, '禁用平台配置', 1633, 1, 2, '禁用平台配置', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1849, '编辑平台配置', 1633, 1, 2, '编辑平台配置', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1851, '删除平台配置', 1633, 1, 2, '删除平台配置', '2022-05-24 18:08:26.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1853, '查看操作记录列表', 1635, 1, 2, '查看操作记录列表', '2022-05-24 18:08:26.0', '2022-05-24 18:23:34.0', 0, 'know_search'),
        (1855, 'Kibana', 1609, 1, 2, 'Kibana', '2022-05-24 18:08:26.0', '2022-06-14 16:44:02.0', 0, 'know_search'),
        (1857, 'SQL查询', 1609, 1, 2, 'SQL查询', '2022-05-24 18:08:26.0', '2022-06-14 16:44:02.0', 0, 'know_search'),
        (1859, '批量修改限流值', 1759, 1, 2, '批量修改限流值', '2022-05-24 18:08:26.0', '2022-08-11 10:37:13.0', 0, 'know_search'),
        (1861, '禁用', 1759, 1, 2, '禁用', '2022-05-24 18:08:26.0', '2022-08-11 10:37:13.0', 0, 'know_search'),
        (1863, '修改限流值', 1759, 1, 2, '修改限流值', '2022-05-24 18:08:26.0', '2022-08-11 10:37:13.0', 0, 'know_search'),
        (1865, '查看异常查询列表', 1611, 1, 2, '查看异常查询列表', '2022-05-24 18:08:26.0', '2022-06-14 16:44:02.0', 0, 'know_search'),
        (1867, '查看慢查询列表', 1611, 1, 2, '查看慢查询列表', '2022-05-24 18:08:26.0', '2022-06-14 16:44:21.0', 0, 'know_search'),
        (1869, '新增角色', 1629, 1, 2, '新增角色', '2022-05-24 18:08:26.0', '2022-05-24 18:23:34.0', 0, 'know_search'),
        (1871, 'Dashboard', 0, 0, 1, '查看dashboard', '2022-05-24 18:08:26.0', '2022-08-27 17:35:50.0', 0, 'know_search'),
        (1873, '新建索引', 1605, 1, 2, '新建索引', '2022-05-24 18:08:26.0', '2022-05-24 18:23:34.0', 0, 'know_search'),
        (1875, '查看dashboard', 1871, 1, 2, '查看dashboard', '2022-05-24 18:08:24.0', '2022-08-27 17:35:50.0', 0,
         'know_search');
#角色初始化数据
insert into kf_security_role (id, role_code, role_name, description, last_reviser, create_time, update_time,
                                is_delete, app_name)
values (1, 'r14715628', '管理员', '管理员', 'admin', '2022-06-01 21:19:42.0', '2022-07-06 22:23:59.0', 0,
        'know_search'),
       (2, 'r14481382', '资源 owner', '普通用户拥有的最大权限', 'admin', '2022-06-14 18:08:56.0',
        '2022-07-06 20:36:31.0', 0, 'know_search');
#初始化用户
insert into kf_security_user (id, user_name, pw, salt, real_name, phone, email, dept_id, is_delete,
                                create_time, update_time, app_name)
values (1, 'admin',
        'V1ZkU2RHRlhOSGhOYWs0M1VVWmFjVk5xVW1oaE0zUmlTVEJCZUZGRFRtUm1WVzh5VlcxNGMyRkZRamw3UUZacVNqUmhhM3RiSTBBeVFDTmRmVW8yVW14c2FFQjl7QFZqSjRha3tbI0AzQCNdfUo2UmxsaEB9Mv{#cdRgJ45Lqx}3IubEW87!==',
        '', 'admin', '18888888888', 'admin@12345.com', null, 0, '2022-05-26 05:46:12.0', '2022-08-26 09:06:19.0',
        'know_search');
#初始化用户和角色的关系
insert into kf_security_user_role (id, user_id, role_id, create_time, update_time, is_delete, app_name)
values (1, 1, 2, '2022-08-26 19:54:22.0', '2022-08-26 19:54:22.0', 0, 'know_search'),
       (2, 1, 1, '2022-08-30 21:05:17.0', '2022-08-30 21:05:17.0', 0, 'know_search');
#项目和项目配置、es user 的关系
insert into project_arius_config (project_id, analyze_response_enable, is_source_separated, aggr_analyze_enable,
                                  dsl_analyze_enable, slow_query_times, is_active, memo, create_time, update_time)
values (1, 1, 0, 1, 1, 1000, 1, '超级应用', '2022-06-14 18:52:08.0', '2022-08-27 23:13:14.0'),
       (2, 1, 0, 1, 1, 1000, 1, '元数据模版应用 不可以被删除', '2022-08-25 11:18:45.0', '2022-08-25 11:18:45.0');
insert into kf_security_project (id, project_code, project_name, description, dept_id, running, create_time,
                                 update_time, is_delete, app_name)
values (1, 'p14000143', 'SuperApp', '超级应用', 0, 1, '2022-05-26 05:49:08.0', '2022-08-24 11:09:49.0', 0,
        'know_search'),
       (2, 'p18461793', '元数据模版应用_误删', '元数据模版应用 不可以被删除', 0, 1, '2022-08-25 11:06:04.0',
        '2022-08-25 11:18:45.0', 0, 'know_search');
insert into arius_es_user (id, index_exp, data_center, is_root, memo, ip, verify_code, is_active,
                           query_threshold, cluster, responsible, search_type, create_time, update_time,
                           project_id, is_default_display)
values (1, null, 'cn', 1, '管理员 APP', '', 'azAWiJhxkho33ac', 1, 100, '',
        'admin', 1,
        '2022-05-26 09:35:38.0', '2022-06-23 00:16:47.0', 1, 1),
       (2, null, 'cn', 0, '元数据模版 APP', '', 'vkDgPEfD3jQJ1YY', 1, 1000, '', 'admin', 1, '2022-07-05 08:16:17.0',
        '2022-08-25 21:48:58.0', 2, 1);


## 配置初始化数据
TRUNCATE  table `arius_config_info`;
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (187, 'arius.cache.switch', 'logic.template.cache.enable', 'true', 1, -1, -1, '逻辑模板缓存是否开启', '2021-09-01 20:37:47', '2021-11-29 14:57:47', '2021-09-01 20:37:47');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (189, 'arius.cache.switch', 'physical.template.cache.enable', 'true', 1, -1, -1, '获取物理模板列表是否开启全局缓存', '2021-09-01 20:41:22', '2021-11-29 14:57:45', '2021-09-01 20:41:22');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (191, 'arius.cache.switch', 'cluster.phy.cache.enable', 'true', 1, -1, -1, '获取物理集群列表是否开启全局缓存', '2021-09-01 20:42:31', '2021-11-29 14:57:42', '2021-09-01 20:42:31');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (193, 'arius.cache.switch', 'cluster.logic.cache.enable', 'true', 1, -1, -1, '获取逻辑集群列表是否开启全局缓存', '2021-09-01 20:43:08', '2021-11-29 14:57:39', '2021-09-01 20:43:08');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1217, 'arius.meta.monitor', 'nodestat.collect.concurrent', 'true', 1, -1, -1, '', '2021-11-18 20:24:54', '2021-11-19 16:05:39', '2021-11-18 20:24:54');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1223, 'arius.common.group', 'app.default.read.auth.indices', '\"\"', 1, -1, 2, 'app可读写的权限', '2021-12-15 20:17:06', '2021-12-16 11:17:26', '2021-12-15 20:17:06');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1225, 'arius.common.group', 'delete.expire.index.ahead.clusters', '\"\"', 1, -1, 2, '删除过期权限', '2021-12-15 20:17:48', '2021-12-16 11:17:24', '2021-12-15 20:17:48');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1227, 'arius.common.group', 'operate.index.ahead.seconds', '2 * 60 * 60', 1, -1, 2, '索引操作提前时间', '2021-12-15 20:18:37', '2021-12-16 11:17:22', '2021-12-15 20:18:37');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1229, 'arius.common.group', 'platform.govern.admin.hot.days', '-1', 1, -1, 2, '平台治理导入热存的天数', '2021-12-15 20:19:13', '2021-12-16 11:17:19', '2021-12-15 20:19:13');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1231, 'arius.common.group', 'quota.dynamic.limit.black.appIds', 'none', 1, -1, 2, 'appid黑名单控制', '2021-12-15 20:20:11', '2021-12-16 11:17:17', '2021-12-15 20:20:11');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1233, 'arius.common.group', 'quota.dynamic.limit.black.cluster', '\"\"', 1, -1, 2, 'cluster黑名单控制', '2021-12-15 20:20:39', '2021-12-16 11:17:15', '2021-12-15 20:20:39');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1235, 'arius.common.group', 'quota.dynamic.limit.black.logicId', 'none', 1, -1, 2, '模板黑名单控制', '2021-12-15 20:21:21', '2021-12-16 11:17:12', '2021-12-15 20:21:21');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1237, 'arius.common.group', 'arius.wo.auto.process.create.template.disk.maxG', '10.0', 1, -1, 2, '模板创建时设置的磁盘空间最大值', '2021-12-15 20:21:49', '2021-12-16 11:15:12', '2021-12-15 20:21:49');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1239, 'arius.common.group', 'request.interceptor.switch.open', 'true', 1, -1, 2, '请求拦截开关', '2021-12-15 20:22:14', '2021-12-16 11:15:10', '2021-12-15 20:22:14');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1241, 'arius.common.group', 'arius.didi.t2.leader.mail', '\"\"', 1, -1, 2, 'didi领导者邮箱', '2021-12-15 20:22:40', '2021-12-16 11:15:07', '2021-12-15 20:22:40');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1243, 'arius.common.group', 'defaultDay', '\"\"', 1, -1, 2, '默认hotDay值', '2021-12-15 20:23:17', '2021-12-16 11:15:04', '2021-12-15 20:23:17');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1245, 'arius.quota.config.group', 'arius.quota.config.tps.per.cpu.with.replica', '1000.0', 1, -1, 2, '资源管控cpu项', '2021-12-15 20:23:56', '2021-12-16 11:15:01', '2021-12-15 20:23:56');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1247, 'arius.quota.config.group', 'arius.quota.config.tps.per.cpu.NO.replica', '2300.0', 1, -1, 2, '资源管控cpu项', '2021-12-15 20:24:27', '2021-12-16 11:14:58', '2021-12-15 20:24:27');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1249, 'arius.quota.config.group', 'arius.quota.config.cost.per.g.per.month', '1.06', 1, -1, 2, '资源配置模板费用', '2021-12-15 20:24:59', '2021-12-16 11:14:56', '2021-12-15 20:24:59');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1251, 'arius.meta.monitor.group', 'nodestat.collect.concurrent', 'fasle', 1, -1, 2, '节点状态信息是否并行采集', '2021-12-15 20:25:35', '2022-08-26 18:10:50', '2021-12-15 20:25:35');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1253, 'arius.meta.monitor.group', 'indexstat.collect.concurrent', 'fasle', 1, -1, 2, '索引状态信息是否并行采集', '2021-12-15 20:26:00', '2022-08-26 18:10:45', '2021-12-15 20:26:00');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1255, 'arius.common.group', 'indices.recovery.ceph_max_bytes_per_sec', '10MB', 1, -1, 2, '单节点分片恢复的速率', '2021-12-15 21:33:29', '2022-04-08 17:43:14', '2021-12-15 21:33:29');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1257, 'arius.common.group', 'cluster.routing.allocation.node_concurrent_incoming_recoveries', '2', 1, -1, 2, '一个节点上允许多少并发的传入分片还原,表示为传入还原', '2021-12-16 14:41:51', '2021-12-16 14:42:24', '2021-12-16 14:41:51');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1259, 'arius.common', 'cluster.routing.allocation.node_concurrent_outgoing_recoveries', '2', 1, -1, 2, '一个节点上允许多少并发的传入分片还原,传出还原', '2021-12-16 14:42:15', '2022-02-22 11:11:48', '2021-12-16 14:42:15');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1585, 'test.test', 'testt', '21', 1, -1, -1, '请忽略2221', '2022-01-13 14:25:40', '2022-01-15 16:27:05', '2022-01-13 14:25:40');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1587, 'zptest', 'test', '<script>alert(1)</script>', 1, -1, -1, 'alert(1)', '2022-01-18 16:14:12', '2022-01-18 16:15:49', '2022-01-18 16:14:12');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1589, 'test1ddd', 'dd ddd', 'dssdddd', 1, -1, -1, 'sddsdssd', '2022-01-26 11:39:23', '2022-01-26 11:39:42', '2022-01-26 11:39:23');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1591, 'yyftemptest-01s', 'yyftemptest-01d', '', 1, -1, -1, '', '2022-03-01 16:44:12', '2022-03-01 16:44:39', '2022-03-01 16:44:12');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1593, 'test1', 's', '', 1, -1, -1, '', '2022-03-07 11:37:39', '2022-03-07 11:37:43', '2022-03-07 11:37:39');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1595, 'test1', '22', 'm1qaz2wsx3edc4rfv5tgb6yhn7ujm1qaz2wsx3edc4rfv5tgb6yhn7ujm1qaz2wsx3edc4rfv5tgb6yhn7ujm1qaz2', 1, -1, -1, '', '2022-03-15 11:19:49', '2022-03-15 11:20:08', '2022-03-15 11:19:49');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1623, 'settingGroup', 'name', 'value', 1, -1, -1, 'test', '2022-06-23 14:17:56', '2022-06-23 15:47:26', '2022-06-23 14:17:56');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1625, 'group11', 'name1', 'value1', 1, -1, -1, 'des-edit', '2022-06-23 15:22:51', '2022-06-24 09:40:51', '2022-06-23 15:22:51');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1627, 'arius.common.group', 'cluster.node.specification_list', '16c-64g-3072g,16c-48g-3071g,1c-48g-3071g,', 1, -1, 1, '节点规格列表，机型列表', '2022-07-05 14:10:27', '2022-07-18 15:01:29', '2022-07-05 14:10:27');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1629, 'ccccccccccccccdcdccccccccccccccdcdccccccccccccccdb', 'dccccccccccccccdcdcccccccc', 'vjh', 1, -1, -1, 'cdcdccccccccccccccdcdccccccccccccccdcdccccccccccccccdcdccccccccccccccdcdccccccccccccccdcdccccc', '2022-07-05 15:27:38', '2022-07-05 15:28:09', '2022-07-05 15:27:38');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1631, '2', '3', '', 1, -1, -1, '', '2022-07-06 15:26:45', '2022-07-06 15:26:58', '2022-07-06 15:26:45');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1633, 'arius.common.group', 'cluster.data.center_list', 'cn,en', 1, -1, 1, '数据中心列表', '2022-07-06 16:14:03', '2022-08-27 19:11:25', '2022-07-06 16:14:03');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1635, 'arius.common.group', 'cluster.package.version_list', '7.6.1.1,6.6.6.6,7.6.1.2', 1, -1, 1, '系统预制支持的版本', '2022-07-06 16:17:25', '2022-07-06 16:17:25', '2022-07-06 16:17:25');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1637, 'template.time.type', 'format', '[\n  \"yyyy-MM-dd HH:mm:ss\",\n  \"yyyy-MM-dd HH:mm:ss.SSS\",\n  \"yyyy-MM-dd\'T\'HH:mm:ss\",\n  \"yyyy-MM-dd\'T\'HH:mm:ss.SSS\",\n  \"yyyy-MM-dd HH:mm:ss.SSS Z\",\n  \"yyyy/MM/dd HH:mm:ss\",\n  \"epoch_seconds\",\n  \"epoch_millis\"\n]', 1, -1, 1, '新建模版的时间格式', '2022-07-07 16:15:37', '2022-07-07 16:15:37', '2022-07-07 16:15:37');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1639, 'arius.cluster.blacklist', 'cluster.phy.name', 'didi-cluster-test', 1, -1, 1, '滴滴内部测试环境集群, 禁止任何编辑删除新增操作', '2022-07-07 17:58:02', '2022-07-07 18:44:42', '2022-07-07 17:58:02');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1641, 'arius.common.group', 'cluster.resource.type_list', '信创,acs,vmware', 1, -1, 1, '所属资源类型列表,IaaS平台类型列表', '2022-07-07 19:13:13', '2022-08-31 16:38:37', '2022-07-07 19:13:13');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1643, '55', '666', '1', 1, -1, -1, '143', '2022-07-13 16:59:41', '2022-07-13 17:01:48', '2022-07-13 16:59:41');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1645, 'arius.common.group', 'index.rollover.threshold', '0.00001', 1, -1, 1, '主分片大小达到1G后升版本', '2022-07-15 21:03:12', '2022-09-22 15:28:54', '2022-07-15 21:03:12');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1647, 'yyftemptest-01', 'yyf', 'sdv', 1, -1, -1, 'sdv', '2022-07-18 15:02:08', '2022-07-18 15:02:24', '2022-07-18 15:02:08');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1649, 'arius.common.group', 'cluster.node.count_list', '2,4,6,10', 1, -1, 1, '集群节点个数列表', '2022-07-18 15:22:33', '2022-08-27 19:13:09', '2022-07-18 15:22:33');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1653, 'arius.common.group', 'arius.system.template', '[\n    \"arius.dsl.analyze.result\",\n    \"arius.dsl.metrics\",\n    \"arius.dsl.template\",\n    \"arius.gateway.join\",\n    \"arius_stats_index_info\",\n    \"arius_stats_node_info\",\n    \"arius.template.access\",\n    \"arius_cat_index_info\",\n    \"arius_gateway_metrics\",\n    \"arius_stats_cluster_info\",\n    \"arius_stats_cluster_task_info\",\n    \"arius_stats_dashboard_info\",\n    \"arius.appid.template.access\"\n]', 1, -1, 1, '系统核心模版集合', '2022-07-21 12:25:48', '2022-07-21 12:30:06', '2022-07-21 12:25:48');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1655, 'ds12', 'sd34', 'sdsddsd', 1, -1, -1, 'ds78', '2022-07-21 17:00:44', '2022-08-01 08:52:35', '2022-07-21 17:00:44');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1657, 'arius.common.group', 'cluster.shard.big_threshold', '10', 1, -1, 1, '用于设置集群看板中的大Shard阈值，单位为gb，大于这个值就认为是大shard', '2022-07-28 17:49:59', '2022-08-26 18:08:56', '2022-07-28 17:49:59');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1671, 'arius.template.group', 'logic.template.business_type', '系统数据,日志数据,业务上报数据,test_businesss_type1,RDS数据,离线导入数据,testset,123,test_businesss_type1', 1, -1, 1, '模板业务类型', '2022-08-26 18:02:47', '2022-09-01 15:16:11', '2022-08-26 18:02:47');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1673, 'arius.template.group', 'logic.template.time_format_list', 'yyyy-MM-dd HH:mm:ss,yyyy-MM-dd HH:mm:ss.SSS,yyyy-MM-dd HH:mm:ss.SSS Z,yyyy-MM-dd\'T\'HH:mm:ss,yyyy-MM-dd\'T\'HH:mm:ss.SSS,yyyy-MM-dd\'T\'HH:mm:ssZ,yyyy-MM-dd\'T\'HH:mm:ss.SSSZ,yyyy/MM/dd HH:mm:ss,epoch_second,epoch_millis,yyyy-MM-dd', 1, -1, 1, '模板时间格式列表', '2022-08-26 18:06:07', '2022-08-31 17:16:03', '2022-08-26 18:06:07');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1675, 'arius.template.group', 'history.template.physic.indices.allocation.is_effective', 'ture', 1, -1, 1, '历史索引模板shard分配是否自动调整', '2022-08-26 18:07:53', '2022-08-31 17:07:02', '2022-08-26 18:07:53');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1677, 'arius.common.group', 'operate.record.save.time', '29', 1, -1, -1, '操作记录的保存时间', '2022-09-01 16:44:03', '2022-09-01 17:23:48', '2022-09-01 16:44:03');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1679, 'arius.common.group', 'operate.record.save_time', '25', 1, -1, 1, '操作记录的保存时间(天)', '2022-09-01 19:34:33', '2022-09-19 15:14:59', '2022-09-01 19:34:33');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1681, 'arius.common.group', 'super_app.default.dsl.command', '#获取节点状态\nGET _nodes/stats\n\n#获取集群信息\nGET _cluster/stats\n\n#获取集群健康信息\nGET _cluster/health?v\n\n#查看当前集群的热点线程\nGET _nodes/hot_threads\n\n#查看当前集群运行中的任务信息\nGET _tasks?actions=*&detailed\n\n#shard分配说明，会在分片未分配的事后去通过这个命令查看下具体原因\nGET /_cluster/allocation/explain\n\n#异常shard分配重试，当集群red有shard未分配的情况下会通过这个命令来重试分配\nPOST /_cluster/reroute?retry_failed=true\n\n#清除fielddata内存，当集群因为fileddata太大导致熔断或占用很多内存，可以通过此命令释放内存\nPOST _cache/clear?fielddata=true\n', 1, -1, 1, '超级应用默认就有的命令', '2022-09-20 10:26:08', '2022-09-26 11:34:14', '2022-09-20 10:26:08');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1683, 'arius.common.group', 'operate.record.save.num', '30', 1, -1, -1, 'DSL和kibana操作记录保存条数', '2022-09-20 10:45:31', '2022-09-22 16:49:16', '2022-09-20 10:45:31');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1685, 'ddd', 'sdd', 'sd', 1, -1, -1, 'ds', '2022-09-21 15:22:31', '2022-09-21 15:23:00', '2022-09-21 15:22:31');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1607, 'arius.dashboard.threshold.group', 'index.segment.num_threshold', '{\"name\":\" 索引 Segments 个数 \",\"metrics\":\"segmentNum\",\"unit\":\" 个 \",\"compare\":\">\",\"value\":100}', 1, -1, 1, '索引 Segment 个数阈值定义', '2022-06-17 09:52:11', '2022-08-27 16:05:06', '2022-06-17 09:52:11');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1609, 'arius.dashboard.threshold.group', 'index.template.segment_num_threshold', '{\"name\":\" 模板 Segments 个数 \",\"metrics\":\"segmentNum\",\"unit\":\" 个 \",\"compare\":\">\",\"value\":700}', 1, -1, 1, '索引模板 [Segment 个数阈值] 定义', '2022-06-17 09:53:34', '2022-08-27 19:01:57', '2022-06-17 09:53:34');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1611, 'arius.dashboard.threshold.group', 'index.segment.memory_size_threshold', '{\"name\":\" 索引 Segments 内存大小 \",\"metrics\":\"segmentMemSize\",\"unit\":\"MB\",\"compare\":\">\",\"value\":500}', 1, -1, 1, '索引 [Segment 内存大小阈值] 定义', '2022-06-17 09:54:20', '2022-10-26 18:50:50', '2022-06-17 09:54:20');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1613, 'arius.dashboard.threshold.group', 'index.template.segment_memory_size_threshold', '{\"name\":\" 模板 Segments 内存大小 \",\"metrics\":\"segmentMemSize\",\"unit\":\"MB\",\"compare\":\">\",\"value\":3000}', 1, -1, 1, '索引模板 [Segment 内存大小阈值] 定义', '2022-06-17 09:54:50', '2022-10-26 18:50:27', '2022-06-17 09:54:50');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1617, 'arius.dashboard.threshold.group', 'node.shard.num_threshold', '{\"name\":\" 节点分片个数 \",\"metrics\":\"shardNum\",\"unit\":\" 个 \",\"compare\":\">\",\"value\":1000}', 1, -1, 1, '节点 [分片个数阈值] 定义', '2022-06-17 10:01:40', '2022-08-27 19:09:44', '2022-06-17 10:01:40');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1619, 'arius.dashboard.threshold.group', 'index.shard.small_threshold', '{\"name\":\" 小 shard 索引列表 \",\"metrics\":\"shardSize\",\"unit\":\"MB\",\"compare\":\"<\",\"value\":1000}', 1, -1, 1, '索引 [小 Shard 阈值] 定义', '2022-06-17 16:11:53', '2022-08-27 19:04:19', '2022-06-17 16:11:53');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1656, 'arius.dashboard.threshold.group', 'index.mapping.num_threshold', '{\"name\":\" 索引 Mapping 个数 \",\"metrics\":\"mappingNum\",\"unit\":\" 个 \",\"compare\":\">\",\"value\":100}', 1, -1, 1, '索引 [Mapping 个数阈值] 定义', '2022-07-28 15:50:59', '2022-08-27 18:36:48', '2022-07-28 15:50:59');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1659, 'arius.dashboard.threshold.group', 'cluster.shard.num_threshold', '{\"name\":\" 集群 shard 个数 \",\"metrics\":\"shardNum\",\"unit\":\" 个 \",\"compare\":\">\",\"value\":10000}', 1, -1, 1, '集群 [Shard 个数阈值] 定义', '2022-08-05 15:58:22', '2022-10-27 12:00:25', '2022-08-05 15:58:22');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1661, 'arius.dashboard.threshold.group', 'cluster.metric.collector.delayed_threshold', '{\"name\":\"node_status 指标采集延时 \",\"metrics\":\"clusterElapsedTimeGte5Min\",\"unit\":\"MIN\",\"compare\":\">\",\"value\":5}', 1, -1, 1, '集群 [指标采集延时阈值] 定义', '2022-08-10 14:10:47', '2022-10-26 16:20:17', '2022-08-10 14:10:47');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1663, 'arius.dashboard.threshold.group', 'node.disk.used_percent_threshold', '{\"name\":\" 磁盘利用率 \",\"metrics\":\"largeDiskUsage\",\"unit\":\"%\",\"compare\":\">\",\"value\":80}', 1, -1, 1, '节点 [磁盘利用率阈值] 定义', '2022-08-25 14:50:41', '2022-10-26 18:48:54', '2022-08-25 14:50:41');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1665, 'arius.dashboard.threshold.group', 'node.jvm.heap.used_percent_threshold', '{\"name\":\" 堆内存利用率 \",\"metrics\":\"largeHead\",\"unit\":\"%\",\"compare\":\">\",\"value\":75}', 1, -1, 1, '节点 [堆内存利用率阈值] 定义', '2022-08-25 16:45:33', '2022-10-26 18:48:40', '2022-08-25 16:45:33');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1666, 'arius.dashboard.threshold.group', 'node.cpu.used_percent_threshold', '{\"name\":\"CPU 利用率红线 \",\"metrics\":\"largeCpuUsage\",\"unit\":\"%\",\"compare\":\">\",\"value\":60}', 1, -1, 1, '节点 [CPU 利用率阈值] 定义', '2022-08-25 16:45:33', '2022-10-26 18:48:18', '2022-08-25 16:45:33');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1667, 'arius.dashboard.threshold.group', 'node.jvm.heap.used_percent_time_duration_threshold', '{\"name\":\"node.jvm.heap.used_percent_threshold_time_duration\",\"metrics\":\"jvmHeapUsedPercentThresholdTimeDuration\",\"unit\":\"MIN\",\"compare\":\">\",\"value\":10}', 1, -1, 1, '节点堆内存利用率阈值的 [持续时间]', '2022-08-25 16:45:33', '2022-10-26 18:47:52', '2022-08-25 16:45:33');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1668, 'arius.dashboard.threshold.group', 'node.cpu.used_percent_threshold_time_duration_threshold', '{\"name\":\"node.large.cpu.used.percent.time.threshold\",\"metrics\":\"largeCpuUsage\",\"unit\":\"MIN\",\"compare\":\">\",\"value\":5}', 1, -1, 1, '节点 CPU 利用率超阈值的 [持续时间]', '2022-08-25 16:45:33', '2022-10-26 18:47:21', '2022-08-25 16:45:33');
INSERT INTO `arius_config_info`(`id`, `value_group`, `value_name`, `value`, `edit`, `dimension`, `status`, `memo`, `create_time`, `update_time`, `search_time`) VALUES (1669, 'arius.dashboard.threshold.group', 'index.shard.big_threshold', '{\"name\":\"index.shard.big_threshold\",\"metrics\":\"shardSize\",\"unit\":\"G\",\"compare\":\">\",\"value\":20}', 1, -1, 1, '索引 [大 shard 阈值] 定义', '2022-08-26 15:25:07', '2022-08-29 10:28:24', '2022-08-26 15:25:07');



INSERT INTO `es_package` (`id`, `url`, `es_version`, `creator`, `release`, `manifest`, `desc`, `create_time`, `update_time`, `delete_flag`) VALUES (1, 'https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-5.6.16.tar.gz', '5.X', 'admin', 0, '4', '5.X社区开源版本', '2022-07-12 10:59:32', '2022-07-12 10:59:32', 0);
INSERT INTO `es_package` (`id`, `url`, `es_version`, `creator`, `release`, `manifest`, `desc`, `create_time`, `update_time`, `delete_flag`) VALUES (2, 'https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-6.8.23.tar.gz', '6.X', 'admin', 0, '4', '6.X社区开源版本', '2022-07-12 10:59:32', '2022-12-27 16:06:30', 0);
INSERT INTO `es_package` (`id`, `url`, `es_version`, `creator`, `release`, `manifest`, `desc`, `create_time`, `update_time`, `delete_flag`) VALUES (3, 'https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-7.17.8-linux-x86_64.tar.gz', '7.X', 'admin', 0, '4', '7.X社区开源版本', '2022-07-12 10:59:32', '2022-12-27 16:06:29', 0);
INSERT INTO `es_package` (`id`, `url`, `es_version`, `creator`, `release`, `manifest`, `desc`, `create_time`, `update_time`, `delete_flag`) VALUES (4, 'https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-8.5.3-linux-x86_64.tar.gz', '8.X', 'admin', 0, '4', '8.X社区开源版本', '2022-07-12 10:59:32', '2022-12-27 15:50:37', 0);
INSERT INTO `es_package` (`id`, `url`, `es_version`, `creator`, `release`, `manifest`, `desc`, `create_time`, `update_time`, `delete_flag`) VALUES (5, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/6.6.1.0-4.tar.gz', '6.6.1.903', 'admin', 0, '4', '6.6.1.903滴滴内部版本', '2022-07-12 10:59:32', '2022-12-27 15:48:23', 0);
INSERT INTO `es_package` (`id`, `url`, `es_version`, `creator`, `release`, `manifest`, `desc`, `create_time`, `update_time`, `delete_flag`) VALUES (6, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/elasticsearch.tar.gz', '7.6.0.1401', 'admin', 0, '4', '7.6.0.1401滴滴内部版本', '2022-07-12 10:59:32', '2022-07-12 10:59:32', 0);

/*
0.3.1原始sql
UPDATE kf_security_permission SET permission_name = 'Kibana', parent_id = 1609, leaf = 1, level = 2, description = 'Kibana', create_time = '2022-05-24 18:08:26.0', update_time = '2022-06-14 16:44:02.0', is_delete = 0, app_name = 'know_search' WHERE id = 1855;
UPDATE kf_security_permission SET permission_name = 'SQL查询', parent_id = 1609, leaf = 1, level = 2, description = 'SQL查询', create_time = '2022-05-24 18:08:26.0', update_time = '2022-06-14 16:44:02.0', is_delete = 0, app_name = 'know_search' WHERE id = 1857;
UPDATE kf_security_permission SET permission_name = 'DSL查询', parent_id = 1609, leaf = 1, level = 2, description = 'DSL查询', create_time = '2022-05-24 18:08:24.0', update_time = '2022-06-14 16:39:48.0', is_delete = 0, app_name = 'know_search' WHERE id = 1757;
UPDATE kf_security_role_permission SET role_id = 1, permission_id = 1609, create_time = '2022-06-14 17:41:03.0', update_time = '2022-08-25 10:31:42.0', is_delete = 0, app_name = 'know_search' WHERE id = 2059;
UPDATE kf_security_role_permission SET role_id = 1, permission_id = 1757, create_time = '2022-06-14 17:41:03.0', update_time = '2022-08-25 10:31:42.0', is_delete = 0, app_name = 'know_search' WHERE id = 2061;
UPDATE kf_security_role_permission SET role_id = 1, permission_id = 1855, create_time = '2022-06-14 17:41:03.0', update_time = '2022-08-25 10:34:13.0', is_delete = 0, app_name = 'know_search' WHERE id = 2063;
UPDATE kf_security_role_permission SET role_id = 1, permission_id = 1857, create_time = '2022-06-14 17:41:03.0', update_time = '2022-08-25 10:31:42.0', is_delete = 0, app_name = 'know_search' WHERE id = 2065;
UPDATE kf_security_role_permission SET role_id = 2, permission_id = 1609, create_time = '2022-06-14 18:08:56.0', update_time = '2022-08-25 10:31:42.0', is_delete = 0, app_name = 'know_search' WHERE id = 2241;
UPDATE kf_security_role_permission SET role_id = 2, permission_id = 1757, create_time = '2022-06-14 18:08:56.0', update_time = '2022-08-25 10:31:42.0', is_delete = 0, app_name = 'know_search' WHERE id = 2243;
UPDATE kf_security_role_permission SET role_id = 2, permission_id = 1855, create_time = '2022-06-14 18:08:56.0', update_time = '2022-08-25 10:33:12.0', is_delete = 1, app_name = 'know_search' WHERE id = 2245;
UPDATE kf_security_role_permission SET role_id = 2, permission_id = 1857, create_time = '2022-06-14 18:08:56.0', update_time = '2022-08-25 10:31:42.0', is_delete = 0, app_name = 'know_search' WHERE id = 2247;
*/
# 0.3.1.1变更sql
# 1.更新level和leaf
UPDATE kf_security_permission SET permission_name = 'DSL查询', parent_id = 0, leaf = 0, level = 1, description = 'DSL查询', create_time = '2022-05-24 18:08:24.0', update_time = '2022-09-02 19:01:17.0', is_delete = 0, app_name = 'know_search' WHERE id = 1757;
UPDATE kf_security_permission SET permission_name = 'Kibana', parent_id = 0, leaf = 0, level = 1, description = 'Kibana', create_time = '2022-05-24 18:08:26.0', update_time = '2022-09-02 19:01:17.0', is_delete = 0, app_name = 'know_search' WHERE id = 1855;
UPDATE kf_security_permission SET permission_name = 'SQL查询', parent_id = 0, leaf = 0, level = 1, description = 'SQL查询', create_time = '2022-05-24 18:08:26.0', update_time = '2022-09-02 19:01:17.0', is_delete = 0, app_name = 'know_search' WHERE id = 1857;
#1.1修改kf_security_role_permission
UPDATE kf_security_role_permission SET role_id = 1, permission_id = 1609, create_time = '2022-06-14 17:41:03.0', update_time = '2022-09-02 19:04:07.0', is_delete = 1, app_name = 'know_search' WHERE id = 2059;
UPDATE kf_security_role_permission SET role_id = 1, permission_id = 1757, create_time = '2022-06-14 17:41:03.0', update_time = '2022-08-25 10:31:42.0', is_delete = 0, app_name = 'know_search' WHERE id = 2061;
UPDATE kf_security_role_permission SET role_id = 1, permission_id = 1855, create_time = '2022-06-14 17:41:03.0', update_time = '2022-08-25 10:34:13.0', is_delete = 0, app_name = 'know_search' WHERE id = 2063;
UPDATE kf_security_role_permission SET role_id = 1, permission_id = 1857, create_time = '2022-06-14 17:41:03.0', update_time = '2022-08-25 10:31:42.0', is_delete = 0, app_name = 'know_search' WHERE id = 2065;
UPDATE kf_security_role_permission SET role_id = 2, permission_id = 1609, create_time = '2022-06-14 18:08:56.0', update_time = '2022-09-02 19:04:07.0', is_delete = 1, app_name = 'know_search' WHERE id = 2241;
UPDATE kf_security_role_permission SET role_id = 2, permission_id = 1757, create_time = '2022-06-14 18:08:56.0', update_time = '2022-08-25 10:31:42.0', is_delete = 0, app_name = 'know_search' WHERE id = 2243;
UPDATE kf_security_role_permission SET role_id = 2, permission_id = 1855, create_time = '2022-06-14 18:08:56.0', update_time = '2022-08-25 10:33:12.0', is_delete = 1, app_name = 'know_search' WHERE id = 2245;
UPDATE kf_security_role_permission SET role_id = 2, permission_id = 1857, create_time = '2022-06-14 18:08:56.0', update_time = '2022-08-25 10:31:42.0', is_delete = 0, app_name = 'know_search' WHERE id = 2247;
#2.更新name
UPDATE kf_security_permission SET permission_name = 'Kibana查询', parent_id = 0, leaf = 0, level = 1, description = 'Kibana查询', create_time = '2022-05-24 18:08:26.0', update_time = '2022-09-05 14:19:29.0', is_delete = 0, app_name = 'know_search' WHERE id = 1855;
#3.新增3个权限点
INSERT INTO kf_security_permission (permission_name, parent_id, leaf, level, description, create_time, update_time, is_delete, app_name) VALUES ('DSL', 0, 0, 1, 'DSL', '2022-05-24 18:08:24.0', '2022-09-02 19:01:17.0', 0, 'know_search');
INSERT INTO kf_security_permission (permission_name, parent_id, leaf, level, description, create_time, update_time, is_delete, app_name) VALUES ('Kibana', 0, 0, 1, 'Kibana', '2022-05-24 18:08:26.0', '2022-09-02 19:01:17.0', 0, 'know_search');
INSERT INTO kf_security_permission (permission_name, parent_id, leaf, level, description, create_time, update_time, is_delete, app_name) VALUES ('SQL', 0, 0, 1, 'SQL', '2022-05-24 18:08:26.0', '2022-09-02 19:01:17.0', 0, 'know_search');
#3.1 新增kf_security_role_permission
insert into kf_security_role_permission(role_id, permission_id, is_delete, app_name)
values (1, 1877, 0, 'know_search'),
       (1, 1879, 1, 'know_search'),
       (1, 1881, 0, 'know_search'),
       (2, 1877, 0, 'know_search'),
       (2, 1879, 1, 'know_search'),
       (2, 1881, 0, 'know_search');

#4.再次更新level和leaf
UPDATE kf_security_permission SET permission_name = 'DSL查询', parent_id = 1877, leaf = 1, level = 2, description = 'DSL查询' WHERE id = 1757;
UPDATE kf_security_permission SET permission_name = 'Kibana查询', parent_id = 1879, leaf = 1, level = 2, description = 'Kibana查询' WHERE id = 1855;
UPDATE kf_security_permission SET permission_name = 'SQL查询', parent_id = 1881, leaf = 1, level = 2, description = 'SQL查询' WHERE id = 1857;

#5.用户和应用配置信息表
alter table user_metrics_config_info rename to user_config_info;
alter table user_config_info COMMENT '用户和应用配置信息表';
alter table `user_config_info` change COLUMN metric_info config_info text COMMENT '用户下某个应用的配置';
alter table `user_config_info` add column project_id int(10) NOT NULL DEFAULT '-1' COMMENT '项目 id' after user_name;
alter table `user_config_info` add column config_type int(10) NOT NULL DEFAULT '1' COMMENT '配置类型,1- 指标看板和 dashboard，2- 查询模板列表' after project_id;
truncate table user_config_info;

INSERT INTO `user_config_info`(`user_name`, `project_id`, `config_type`, `config_info`)
select DISTINCT t1.user_name,t2.project_id,2,
                concat('[{\"firstUserConfigType\":\"searchQuery\",\"projectId\":',t2.project_id,',\"secondUserConfigType\":\"searchTemplate\",\"userConfigTypes\":[\"totalCostAvg\",\"totalShardsAvg\"],\"userName\":\"',t1.user_name,'\"}]') as config_info
from kf_security_user t1 inner join
     kf_security_user_project t2 on t1.id=t2.user_id and t2.is_delete='0'
where  t1.is_delete='0';
-- ----------------------------
-- Table structure for metric_dictionary_info
-- ----------------------------
DROP TABLE IF EXISTS `metric_dictionary_info`;
CREATE TABLE `metric_dictionary_info`  (
                                           `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT,
                                           `type` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '指标分类',
                                           `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '指标名称',
                                           `price` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '-1' COMMENT '指标价值',
                                           `interval` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '1' COMMENT '计算间隔',
                                           `current_cal_logic` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '当前计算逻辑',
                                           `is_gold` tinyint(1) UNSIGNED ZEROFILL NULL DEFAULT 0 COMMENT '是否黄金指标(0否1是)',
                                           `unit` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '单位',
                                           `interactive_form` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '交互形式',
                                           `is_warning` tinyint(1) UNSIGNED ZEROFILL NULL DEFAULT 0 COMMENT '告警指标(0否1是)',
                                           `source` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '指标来源',
                                           `tags` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '指标标签',
                                           `model` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '模块',
                                           `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                           `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                           `is_active` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否生效',
                                           `is_threshold` tinyint(1) UNSIGNED ZEROFILL NOT NULL DEFAULT 0 COMMENT '是否有阈值',
                                           `threshold` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '阈值',
                                           `metric_type` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '阈值信息',
                                           PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5754 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '用户和应用配置信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of metric_dictionary_info
-- ----------------------------
INSERT INTO `metric_dictionary_info` VALUES (4915, '集群统计', '集群状态、shard总数、索引模板总数、文档总数、索引数、节点分配（Master节点数/Data节点数/Client节点数）、堆内存总量（已用内存、空闲内存）、磁盘总量（已用磁盘、空闲磁盘）、节点总数（活跃节点数、死亡节点数）、集群索引存储量、未分配Shard数', '集群当前运行状态概览信息', '当前值', '索引模板总数: /_template命令获取的数组大小\n通过GET _cluster/stats命令直接获取\n  集群状态：status\n  shard总数：shards.total\n  文档总数：indices.docs.count\n  索引数:indices.count\n  Master节点数:nodes.count.master\n  Data节点数:nodes.count.data \n  Client节点数:nodes.count.total - nodes.count.master-nodes.count.data\n  堆内存总量:nodes.os.mem.total_in_bytes\n  已用内存:nodes.os.mem.used_in_bytes\n  空闲内存:nodes.os.mem.free_percent\n  磁盘总量:nodes.fs.total_in_bytes\n  已用磁盘:nodes.fs.total_in_bytes - nodes.fs.free_in_bytes\n  空闲磁盘:nodes.fs.free_in_bytes\n  节点总数:nodes.count.total\n  活跃节点数，死亡节点数:查询配置的集群节点列表后和ES集群节点ip列表进行匹配，可以匹配上的为活跃节点 \n  集群索引存储量：indices.store.size_in_bytes  ->  新增字段indicesStoreSize\n未分配Shard数：通过 _cat/health?format=json 获取unassign字段 -> 新增字段unassignedShardNum', 1, NULL, '状态栏', 1, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-09-29 09:20:01', 1, 0, NULL, 'basic');
INSERT INTO `metric_dictionary_info` VALUES (4917, '系统指标', 'CPU使用率（平均分位值、99分位值、95分位值、75分位值、55分位值）', '集群CPU使用率与均衡情况观察', '当前值', '[当前值] 集群下所有节点，通过GET _nodes/stats命令获取nodes.{nodeName}.os.cpu.percent ，根据分位进行聚合', 1, '%', '折线图', 1, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-10-24 09:49:28', 1, 0, NULL, 'cpuUsage');
INSERT INTO `metric_dictionary_info` VALUES (4919, '系统指标', 'CPU 1分钟负载（平均分位值、99分位值、95分位值、75分位值、55分位值）', '集群1分钟负载与均衡情况观察', '当前值', '[当前值] 集群下所有节点，通过GET _nodes/stats命令获取nodes.{nodeName}.os.cpu.load_average.1m，根据分位进行聚合', 0, NULL, '折线图', 0, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-10-24 09:49:30', 1, 0, NULL, 'cpuLoad1M');
INSERT INTO `metric_dictionary_info` VALUES (4921, '系统指标', '磁盘使用率（平均分位值、99分位值、95分位值、75分位值、55分位值）', '集群磁盘利用率与均衡情况观察', '当前值', '[当前值]集群下所有节点，通过GET _nodes/stats命令获取（nodes.{nodeName}.fs.total.total_in_bytes - nodes.{nodeName}.fs.total.free_in_bytes）/nodes.{nodeName}.fs.total.total_in_bytes，根据分位进行聚合', 0, '%', '折线图', 1, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-09-29 09:20:16', 1, 0, NULL, 'diskUsage');
INSERT INTO `metric_dictionary_info` VALUES (4923, '系统指标', '磁盘使用情况（磁盘空闲量/磁盘使用量/磁盘总量）', '集群磁盘使用情况概览', '当前值', '通过GET _cluster/stats命令获取\n磁盘总量 : nodes.{nodeName}.fs.total_in_bytes\n磁盘使用量 : nodes.{nodeName}.fs.total_in_bytes - nodes.fs.free_in_bytes\n磁盘空闲量 : nodes.{nodeName}.fs.free_in_bytes', 0, 'GB', '折线图', 0, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-09-29 11:06:06', 1, 0, NULL, 'diskInfo');
INSERT INTO `metric_dictionary_info` VALUES (4925, '系统指标', '网络出口流量', '集群网络出口流量/网络入口流量走势', '当前值', '[当前值] 集群下的所有节点，通过GET _nodes/stats命令获取nodes.{nodeName}.transport.rx_size_in_bytes、nodes.{nodeName}.transport.tx_size_in_bytes（接收、发送）的累加值', 0, 'MB/S', '折线图', 0, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-10-24 09:47:08', 1, 0, NULL, 'sendTransSize');
INSERT INTO `metric_dictionary_info` VALUES (4927, '系统指标', '指标采集失败率', '集群指标采集失败率（采集失败次数/采集窗口大小）', '60S', NULL, 1, '%', '折线图', 1, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-09-28 11:31:36', 1, 0, NULL, NULL);
INSERT INTO `metric_dictionary_info` VALUES (4929, '性能指标', '执行任务耗时（平均分位值、99分位值、95分位值、75分位值、55分位值）', '集群节点任务执行耗时与均衡情况观察', '当前值', '[当前值] _cat/tasks?v&detailed&format=json命令获取结果集中running_time字段的分位值', 0, 'S', '折线图', 0, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-10-10 16:00:21', 1, 0, NULL, 'taskCost');
INSERT INTO `metric_dictionary_info` VALUES (4931, '性能指标', '执行任务数量', '集群全部节点每秒执行任务数走势', '当前值', '[当前值] _cat/tasks?v&detailed&format=json命令获取结果集的大小', 0, '个/S', '折线图', 0, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-09-29 11:06:07', 1, 0, NULL, 'taskCount');
INSERT INTO `metric_dictionary_info` VALUES (4932, '系统指标', '网络入口流量', '集群网络出口流量/网络入口流量走势', '当前值', '[当前值] 集群下的所有节点，通过GET _nodes/stats命令获取nodes.{nodeName}.transport.rx_size_in_bytes、nodes.{nodeName}.transport.tx_size_in_bytes（接收、发送）的累加值', 0, 'MB/S', '折线图', 0, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-10-24 09:47:11', 1, 0, NULL, 'recvTransSize');
INSERT INTO `metric_dictionary_info` VALUES (4933, '性能指标', '查询QPS', '集群Shard级别查询并发量概览', '60S', '[累加值]  集群下的所有节点，间隔时间内通过GET _nodes/stats命令获取nodes.{nodeName}.indices.search.query_total的差值累加值/间隔时间', 1, '次/S', '折线图', 0, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-11-02 19:17:58', 1, 0, NULL, 'readTps');
INSERT INTO `metric_dictionary_info` VALUES (4935, '性能指标', '写入TPS', '集群Shard级别写入并发量概览', '60S', '[累加值] 集群下的所有节点，间隔时间内通过GET _nodes/stats命令获取nodes.{nodeName}.indices.indexing.index_total的差值累加值/间隔时间', 1, '个/S', '折线图', 0, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-10-25 15:47:20', 1, 0, NULL, 'writeTps');
INSERT INTO `metric_dictionary_info` VALUES (4937, '性能指标', '查询耗时', '集群Shard级别查询耗时概览', '60S', '[最大值] 集群下的所有节点,间隔时间内通过_node/stats命令获取nodes.{nodeName}.indices.search.query_time_in_millis差值累加值/节点间隔时间内nodes.{nodeName}.indices.search.query_total差值累加值', 1, 'MS', '折线图', 0, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-11-02 19:25:44', 1, 0, NULL, 'searchLatency');
INSERT INTO `metric_dictionary_info` VALUES (4939, '性能指标', '写入耗时', '集群文档级别写入耗时概览', '60S', '[最大值] 集群下的所有节点,间隔时间内通过_node/stats命令获取nodes.{nodeName}.indices.indexing.index_time_in_millis差值累加值/节点间隔时间内nodes.{nodeName}.indices.indexing.index_total差值累加值', 1, 'MS', '折线图', 0, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-11-02 19:25:17', 1, 0, NULL, 'indexingLatency');
INSERT INTO `metric_dictionary_info` VALUES (4941, '性能指标', '网关写入TPS', '通过网关每秒写入集群的写入请求次数', '60S', NULL, 0, '次/S', '折线图', 0, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-09-29 11:06:08', 1, 0, NULL, NULL);
INSERT INTO `metric_dictionary_info` VALUES (4943, '性能指标', '网关写入吞吐量', '通过网关每秒写入集群的文档字节数', '60S', NULL, 1, 'MB/S', '折线图', 0, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-09-29 11:06:09', 1, 0, NULL, NULL);
INSERT INTO `metric_dictionary_info` VALUES (4945, '性能指标', '网关写入请求耗时', '通过网关写入请求的平均耗时', '60S', NULL, 1, 'MS', '折线图', 0, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-09-29 11:06:09', 1, 0, NULL, NULL);
INSERT INTO `metric_dictionary_info` VALUES (4947, '性能指标', '网关写入请求响应体大小', '通过网关写入请求的响应体平均大小', '60S', NULL, 0, 'B', '折线图', 0, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-09-29 11:06:09', 1, 0, NULL, NULL);
INSERT INTO `metric_dictionary_info` VALUES (4949, '性能指标', '网关查询QPS', '通过网关每秒查询的请求次数', '60S', NULL, 1, '次/S', '折线图', 0, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-09-29 11:06:09', 1, 0, NULL, NULL);
INSERT INTO `metric_dictionary_info` VALUES (4951, '性能指标', '网关查询请求耗时', '通过网关查询请求的平均耗时', '60S', NULL, 1, 'MS', '折线图', 0, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-09-29 11:06:10', 1, 0, NULL, NULL);
INSERT INTO `metric_dictionary_info` VALUES (4953, '性能指标', '网关单次查询命中Shard数', '通过网关查询请求的命中Shard平均数', '60S', NULL, 0, '个', '折线图', 0, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-09-29 11:06:10', 1, 0, NULL, NULL);
INSERT INTO `metric_dictionary_info` VALUES (4955, '状态指标', '迁移中shard列表', '集群Shard迁移感知', '当前值', '[当前值] 通过GET _cat/recovery?v&h=i,s,t,st,shost,thost&active_only=true命令获取的集合详情', 0, NULL, '列表展示（index,source_host,target_host,bytes_recovered bytes_percen,translog_ops_percentt）', 0, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-09-29 11:06:10', 1, 0, NULL, 'movingShards');
INSERT INTO `metric_dictionary_info` VALUES (4957, '状态指标', '未分配Shard列表', '集群节点掉线感知', '当前值', '[当前值]通过GET _cat/shards?format=json命令获取state=UNASSIGN的shard列表', 0, NULL, '列表项（index，shard，prirep，state）', 0, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-09-30 08:56:42', 1, 0, NULL, 'unAssignShards');
INSERT INTO `metric_dictionary_info` VALUES (4959, '状态指标', 'Dead节点列表', '', '当前值', '[当前值] 通过GET _nodes命令获取集群节点信息，与平台集群节点列表对比缺失的节点', 0, NULL, '列表项（节点IP、主机名、实例名）', 1, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-09-29 09:42:21', 1, 0, NULL, 'invalidNodes');
INSERT INTO `metric_dictionary_info` VALUES (4961, '状态指标', 'PendingTask列表', '集群PengingTask感知', '当前值', '[当前值] 通过/_cluster/pending_tasks命令获取的集合', 0, NULL, '列表项（插入顺序、优先级、任务来源、执行任务前等待时间）', 1, 'ES引擎', NULL, 'OverView', '2022-09-28 11:31:36', '2022-09-29 09:42:38', 1, 0, NULL, 'pendingTasks');
INSERT INTO `metric_dictionary_info` VALUES (4963, '系统指标', 'CPU利用率', 'CPU使用率，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取nodes.{nodeName}.os.cpu.percent字段', 1, '%', '折线图', 1, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:42:08', 1, 0, NULL, 'os-cpu-percent');
INSERT INTO `metric_dictionary_info` VALUES (4965, '系统指标', '磁盘空闲率', '磁盘空闲率，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取nodes.{nodeName}.fs.total.free_in_bytes/nodes.{nodeName}.fs.total.total_in_bytes', 1, '%', '折线图', 1, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:36:11', 1, 0, NULL, 'fs-total-disk_free_percent');
INSERT INTO `metric_dictionary_info` VALUES (4967, '系统指标', '网络发送流量', '网络包为单位的每秒发送流量，Top节点趋势', '当前值', '[平均值]，间隔时间内通过GET _nodes/stats命令获取nodes.{nodeName}.transport.tx_size_in_bytes的差值/时间间隔', 0, 'MB/S', '折线图', 1, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-24 09:53:56', 1, 0, NULL, 'transport-tx_size_in_bytes_rate');
INSERT INTO `metric_dictionary_info` VALUES (4969, '系统指标', '网络接收流量', '网络包为单位的接收流量，Top节点趋势', '当前值', '[平均值]，间隔时间内通过GET _nodes/stats命令获取nodes.{nodeName}.transport.rx_size_in_bytes的差值/时间间隔', 0, 'MB/S', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-24 09:54:17', 1, 0, NULL, 'transport-rx_size_in_bytes_rate');
INSERT INTO `metric_dictionary_info` VALUES (4971, '系统指标', 'CPU近1分钟负载', 'CPU近1分钟负载，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取nodes.{nodeName}.os.cpu.load_average.1m', 0, NULL, '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:36:49', 1, 0, NULL, 'os-cpu-load_average-1m');
INSERT INTO `metric_dictionary_info` VALUES (4973, '基本性能指标', '写入TPS', '节点索引写入速率平均值，Top节点趋势', '60S', '[平均值] (当前时刻减去上一时刻通过GET _nodes/stats命令获取nodes.{nodeName}.indices.indexing.index_total的差值)/时间间隔(S)', 1, '个/S', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-29 09:45:25', 1, 0, NULL, 'indices-indexing-index_total_rate');
INSERT INTO `metric_dictionary_info` VALUES (4975, '基本性能指标', '网关查询QPS', '网关通过ClientNode节点每秒查询的请求数，Top节点趋势', '60S', NULL, 0, '次/S', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-28 11:31:36', 1, 0, NULL, NULL);
INSERT INTO `metric_dictionary_info` VALUES (4977, '基本性能指标', '网关写入TPS', '网关通过ClientNode节点每秒写入的请求数，Top节点趋势', '60S', NULL, 0, '次/S', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-28 11:31:36', 1, 0, NULL, NULL);
INSERT INTO `metric_dictionary_info` VALUES (4979, '基本性能指标', '网关写入吞吐量', '网关通过ClientNode节点每秒写入的吞吐量，Top节点趋势', '60S', NULL, 0, 'MB/S', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 11:10:44', 1, 0, NULL, 'collectorDelayed');
INSERT INTO `metric_dictionary_info` VALUES (4981, '基本性能指标', '写入耗时', '节点索引写入耗时平均值，Top节点趋势', '60S', '[平均值],间隔时间内通过GET _nodes/stats命令获取nodes.{nodeName}.indices.indexing.index_time_in_millis的差值/,间隔时间内nodes.{nodeName}.indices.docs.count的差值', 1, 'MS', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 11:06:05', 1, 0, NULL, 'indices-indexing-index_time_per_doc');
INSERT INTO `metric_dictionary_info` VALUES (4983, '基本性能指标', 'Query QPS', '节点索引Query速率平均值，Top节点趋势', '60S', '[平均值],间隔时间内通过GET _nodes/stats命令获取nodes.{nodeName}.indices.search.query_total的差值/时间间隔', 1, '次/S', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-29 09:45:49', 1, 0, NULL, 'indices-search-query_total_rate');
INSERT INTO `metric_dictionary_info` VALUES (4985, '基本性能指标', 'Fetch QPS', '节点索引Fetch速率平均值，Top节点趋势', '60S', '[平均值] ,间隔时间内通过GET _nodes/stats命令获取nodes.{nodeName}.indices.search.fetch_total的差值/时间间隔', 0, '次/S', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-29 09:46:00', 1, 0, NULL, 'indices-search-fetch_total_rate');
INSERT INTO `metric_dictionary_info` VALUES (4987, '基本性能指标', 'Query耗时', '节点索引Query耗时平均值，Top节点趋势', '60S', '[平均值],间隔时间内通过GET _nodes/stats命令获取nodes.{nodeName}.indices.search.query_time_in_millis的差值/,间隔时间内nodes.{nodeName}.indices.search.query_total的差值', 1, 'MS', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:37:36', 1, 0, NULL, 'indices-search-query_time_per_query');
INSERT INTO `metric_dictionary_info` VALUES (4989, '基本性能指标', 'Fetch耗时', '节点索引Fetch耗时平均值，Top节点趋势', '60S', '[平均值] ,间隔时间内通过GET _nodes/stats命令获取nodes.{nodeName}.indices.search.fetch_time_in_millis的差值/,间隔时间内nodes.{nodeName}.indices.search.fetch_total的差值', 0, 'MS', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:37:46', 1, 0, NULL, 'indices-search-fetch_time_per_fetch');
INSERT INTO `metric_dictionary_info` VALUES (4991, '基本性能指标', 'Scroll当下请求量', '节点索引Scroll请求量，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取nodes.{nodeName}.indices.search.scroll_current的值', 0, '个', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:37:56', 1, 0, NULL, 'indices-search-scroll_current');
INSERT INTO `metric_dictionary_info` VALUES (4993, '基本性能指标', 'Scroll请求耗时', '节点Scroll耗时平均值，Top节点趋势', '60S', '[平均值] ,间隔时间内通过GET _nodes/stats命令获取nodes.{nodeName}.indices.search.scroll_time_in_millis的差值/,间隔时间内nodes.{nodeName}.indices.search.scroll_total的差值', 0, 'MS', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:38:05', 1, 0, NULL, 'indices-search-scroll_avg_time');
INSERT INTO `metric_dictionary_info` VALUES (4995, '基本性能指标', 'Merge操作耗时', '节点Merge耗时平均值，Top节点趋势', '60S', '[平均值] 间隔时间内通过GET _nodes/stats命令获取nodes.{nodeName}.indices.merges.total_time_in_millis的差值/间隔时间内nodes.{nodeName}.indices.merges.total的差值', 1, 'MS', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:42:21', 1, 0, NULL, 'indices-merges_avg_time');
INSERT INTO `metric_dictionary_info` VALUES (4997, '基本性能指标', 'Refresh操作耗时', '节点Refresh耗时平均值，Top节点趋势', '60S', '[平均值]间隔时间内通过GET _nodes/stats命令获取nodes.{nodeName}.indices.refresh.total_time_in_millis的差值/间隔时间内nodes.{nodeName}.indices.refresh.total的差值', 1, 'MS', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:42:29', 1, 0, NULL, 'indices-refresh_avg_time');
INSERT INTO `metric_dictionary_info` VALUES (4999, '基本性能指标', 'Flush操作耗时', '节点Flush耗时平均值，Top节点趋势', '60S', '[平均值]间隔时间内通过GET _nodes/stats命令获取nodes.{nodeName}.indices.flush.total_time_in_millis的差值/间隔时间内nodes.{nodeName}.indices.flush.total的差值', 1, 'MS', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:45:15', 1, 0, NULL, 'indices-flush_avg_time');
INSERT INTO `metric_dictionary_info` VALUES (5000, '基本性能指标', 'request Cache eviction', '节点Request Cache缓存驱逐数，Top节点趋势', '当前值', '[差值] 间隔时间内通过GET _nodes/stats命令获取nodes.{nodeName}.indices.request_cache.evictions的差值/时间间隔(S)', 0, '次/MIN', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-24 09:57:08', 1, 0, NULL, 'indices-request_cache-evictions');
INSERT INTO `metric_dictionary_info` VALUES (5001, '基本性能指标', 'Write Rejected个数', '节点写入拒绝数，Top节点趋势', '60S', '[平均值]间隔时间内通过GET _nodes/stats命令获取nodes.{nodeName}.thread_pool.bulk.rejected的差值/时间间隔', 1, '个/MIN', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-29 10:00:54', 1, 0, NULL, 'thread_pool-bulk-rejected');
INSERT INTO `metric_dictionary_info` VALUES (5003, '基本性能指标', 'Write Queue个数', '节点写入队列堆积数，Top节点趋势', '当前值', '[当前值]  _nodes/stats命令获取nodes.{nodeName}.thread_pool.bulk.queue', 1, '个', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 11:05:43', 1, 0, NULL, 'thread_pool-bulk-queue');
INSERT INTO `metric_dictionary_info` VALUES (5005, '基本性能指标', 'Search Queue个数', '节点查询队列堆积数，Top节点趋势', '当前值', '[当前值]  _nodes/stats命令获取nodes.{nodeName}.thread_pool.search.queue', 1, '个', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:45:52', 1, 0, NULL, 'indices-search-query_total');
INSERT INTO `metric_dictionary_info` VALUES (5007, '基本性能指标', 'Search Rejected个数', '节点查询拒绝数，Top节点趋势', '60S', '[差值] 间隔时间内通过GET _nodes/stats命令获取nodes.{nodeName}.thread_pool.search.rejected的差值/时间间隔(MIN)', 1, '个/MIN', '折线图', 1, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-29 10:01:05', 1, 0, NULL, 'thread_pool-search-rejected');
INSERT INTO `metric_dictionary_info` VALUES (5009, '基本性能指标', 'Merge次数', '节点Merge次数，Top节点趋势', '60S', '[差值] 间隔时间内通过GET _nodes/stats命令获取nodes.{nodeName}.indices.merges.total的差值/时间间隔(MIN)', 0, '次/MIN', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:46:27', 1, 0, NULL, 'indices-merges-total');
INSERT INTO `metric_dictionary_info` VALUES (5011, '基本性能指标', 'Refresh次数', '节点Refresh次数，Top节点趋势', '60S', '[差值] 间隔时间内通过GET _nodes/stats命令获取nodes.{nodeName}.indices.refresh.total的差值/时间间隔(MIN)', 0, '次/MIN', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:46:40', 1, 0, NULL, 'indices-refresh-total');
INSERT INTO `metric_dictionary_info` VALUES (5013, '基本性能指标', 'Flush次数', '节点Flush次数，Top节点趋势', '60S', '[差值] 间隔时间内通过GET _nodes/stats命令获取nodes.{nodeName}.indices.flush.total的差值/时间间隔(MIN)', 0, '次/MIN', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:46:56', 1, 0, NULL, 'indices-flush-total');
INSERT INTO `metric_dictionary_info` VALUES (5015, '基本性能指标', 'Query Cache内存命中率', '节点Query Cache内存命中率，Top节点趋势', '', '[差值] 通过GET _nodes/stats命令获取nodes.{nodeName}.indices.query_cache.hit_count/nodes.{nodeName}.indices.query_cache.total_count', 0, '%', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-28 16:31:04', 1, 0, NULL, 'indices-query_cache-hit_rate');
INSERT INTO `metric_dictionary_info` VALUES (5017, '基本性能指标', 'Reques Cache内存命中率', '节点Reques Cache内存命中率，Top节点趋势', '', '[差值] 通过GET _nodes/stats命令获取nodes.{nodeName}.indices.request_cache.hit_count/(nodes.{nodeName}.indices.request_cache.hit_count+nodes.{nodeName}.indices.request_cache.miss_count)', 0, '%', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-28 16:31:09', 1, 0, NULL, 'indices-request_cache-hit_rate');
INSERT INTO `metric_dictionary_info` VALUES (5019, '内存大小指标', 'Query Cache内存大小', '节点所有Shard Query Cache(Cached Filters/...)堆内存汇总占用，不在Lucene内存占用统计范围内，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取nodes.{nodeName}.indices.query_cache.memory_size_in_bytes', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-10 10:54:22', 1, 0, NULL, 'indices-query_cache-memory_size_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5021, '内存大小指标', 'Request Cache内存大小', '节点所有Shard Request Cache(Cached Aggregation Results/...)堆内存汇总占用，不在Lucene内存占用统计范围内，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取nodes.{nodeName}.indices.request_cache.memory_size_in_bytes', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-10 10:54:40', 1, 0, NULL, 'indices-request_cache-memory_size_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5023, '高级性能指标', '未提交的Translog大小', '节点所有Shard未提交Translog的大小累加值，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取nodes.{nodeName}.indices.translog.uncommitted_size_in_bytes', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:47:50', 1, 0, NULL, 'indices-translog-uncommitted_size_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5025, '高级性能指标', 'Http活跃连接数', '节点的Http活跃连接数，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取nodes.{nodeName}.http.current_open', 1, '个', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:47:57', 1, 0, NULL, 'http-current_open');
INSERT INTO `metric_dictionary_info` VALUES (5027, '高级性能指标', 'Segement数 ', '节点所有Shard的Segment汇总数，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取nodes.{nodeName}.indices.segments.count', 1, '个', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:48:05', 1, 0, NULL, 'indices-segments-count');
INSERT INTO `metric_dictionary_info` VALUES (5029, '高级性能指标', 'Segement内存大小', '节点所有Shard的Segment底层Lucene内存汇总占用，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取nodes.{nodeName}.indices.segments.memory_in_bytes', 1, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:48:13', 1, 0, NULL, 'indices-segments-memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5031, '内存大小指标', 'Terms内存大小', '节点所有Shard的Segment底层Terms(Text/Keyword/...)内存汇总占用，是底层Lucene内存汇总占用的一个子项，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取nodes.{nodeName}.indices.segments.term_vectors_memory_in_bytes', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-10 10:53:27', 1, 0, NULL, 'indices-segments-term_vectors_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5033, '内存大小指标', 'Points内存大小', '节点所有Shard的Segment底层Points(Numbers/IPs/Geo/...)内存汇总占用，是底层Lucene内存汇总占用的一个子项，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取nodes.{nodeName}.indices.segments.points_memory_in_bytes', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-10 10:53:41', 1, 0, NULL, 'indices-segments-points_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5035, '内存大小指标', 'Doc Values内存大小', '节点所有Shard的Doc Values内存大小累加值，是底层Lucene内存汇总占用的一个子项，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取nodes.{nodeName}.indices.segments.doc_values_memory_in_bytes', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-10 10:53:57', 1, 0, NULL, 'indices-segments-doc_values_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5037, '内存大小指标', 'Index Writer内存大小', '节点所有Shard的Index Writer内存大小累加值，不在Lucene内存占用统计范围内,Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取nodes.{nodeName}.indices.segments.index_writer_memory_in_bytes', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-10 10:54:04', 1, 0, NULL, 'indices-segments-index_writer_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5039, '高级性能指标', '文档总数', '节点所有Shard索引文档数累加值，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取nodes.{nodeName}.indices.docs.count', 1, '个', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:49:31', 1, 0, NULL, 'indices-docs-count');
INSERT INTO `metric_dictionary_info` VALUES (5041, '高级性能指标', '总存储大小', '节点所有Shard索引存储大小累加值，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取nodes.{nodeName}.indices.store.size_in_bytes', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:49:38', 1, 0, NULL, 'indices-store-size_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5043, '高级性能指标', '执行任务耗时', '节点执行任务平均耗时，Top节点趋势', '当前值', '[平均值] _cat/tasks?v&detailed&format=json命令根据node筛选并获取running_time的平均值', 0, 'S', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:50:50', 1, 0, NULL, 'runningTime');
INSERT INTO `metric_dictionary_info` VALUES (5045, '高级性能指标', '执行任务数量', '节点执行任务总数量，Top节点趋势', '当前值', '[当前值] _cat/tasks?v&detailed&format=json命令根据node筛选并获取到的集合大小', 0, '个', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 11:05:00', 1, 0, NULL, 'taskId');
INSERT INTO `metric_dictionary_info` VALUES (5047, '内存大小指标', 'Stored Fields大小', '节点所有Shard Stored Fields(_source/...)内存大小累加值，是底层Lucene内存汇总占用的一个子项，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取nodes.{nodeName}.indices.segments.stored_fields_memory_in_bytes', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-10 10:54:50', 1, 0, NULL, 'indices-segments-stored_fields_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5049, '内存大小指标', 'Norms内存大小', '节点所有Shard Norms(normalization factors for query time/text scoring)内存大小累加值，是底层Lucene内存汇总占用的一个子项，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取indices.segments.norms_memory_in_bytes', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-10 10:54:59', 1, 0, NULL, 'indices-segments-norms_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5051, '内存大小指标', 'Version Map内存大小', '节点所有Shard Version Map(update/delete)内存大小累加值，不在Lucene内存占用统计范围内，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取indices.segments.version_map_memory_in_bytes', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-10 10:55:09', 1, 0, NULL, 'indices-segments-version_map_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5053, '内存大小指标', 'Fixed Bitsets内存大小', '节点所有Shard Fixed Bitsets(deeply nested object/...)内存大小累加值，是底层Lucene内存汇总占用的一个子项，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取indices.segments.fixed_bit_set_memory_in_bytes', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-10 10:55:20', 1, 0, NULL, 'indices-segments-fixed_bit_set_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5055, '内存大小指标', 'Fielddata内存大小', '节点所有Shard的Fielddata(global ordinals /enable fielddata on text field/...)内存汇总占用，不在Lucene内存占用统计范围内，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取indices.segments.breakers.fielddata.estimated_size_in_bytes', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-11-01 09:13:09', 1, 0, NULL, 'breakers-fielddata-estimated_size_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5057, '高级性能指标', '写入线程池queue数', '节点写入线程池队列当前值', '当前值', '[当前值] GET _nodes/stats命令获取thread_pool.write.queue', 0, '个', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-29 11:05:01', 1, 0, NULL, 'thread_pool-write-queue');
INSERT INTO `metric_dictionary_info` VALUES (5059, '高级性能指标', '查询线程池queue数', '节点查询线程池池队列当前值', '当前值', '[当前值] GET _nodes/stats命令获取thread_pool.search.queue', 0, '个', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-08 18:39:32', 1, 0, NULL, 'thread_pool-search-queue');
INSERT INTO `metric_dictionary_info` VALUES (5061, '高级性能指标', '刷新线程池queue数', '节点刷新线程池队列当前值', '当前值', '[当前值] GET _nodes/stats命令获取thread_pool.refresh.queue', 0, '个', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-29 11:05:01', 1, 0, NULL, 'thread_pool-refresh-queue');
INSERT INTO `metric_dictionary_info` VALUES (5063, '高级性能指标', '落盘线程池queue数', '节点落盘线程池队列当前值', '当前值', '[当前值] GET _nodes/stats命令获取thread_pool.rollup_indexing.queue', 0, '个', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-29 11:05:01', 1, 0, NULL, 'thread_pool-rollup_indexing-queue');
INSERT INTO `metric_dictionary_info` VALUES (5065, '高级性能指标', '管理线程池queue数', '节点管理线程池队列当前值', '当前值', '[当前值] GET _nodes/stats命令获取thread_pool.management.queue', 0, '个', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-29 11:05:01', 1, 0, NULL, 'thread_pool-management-queue');
INSERT INTO `metric_dictionary_info` VALUES (5067, '高级性能指标', '合并线程池queue数', '节点合并线程池队列当前值', '当前值', '[当前值] GET _nodes/stats命令获取thread_pool.force_merge.queue', 0, '个', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-29 11:05:01', 1, 0, NULL, 'thread_pool-force_merge-queue');
INSERT INTO `metric_dictionary_info` VALUES (5069, 'JVM指标', 'Young GC次数', '节点Young GC次数，Top节点趋势', '60S', '[平均值]当前时刻减去上一时刻通过GET _nodes/stats命令获取nodes.{nodeName}.jvm.gc.collectors.young.collection_count的值/时间间隔', 1, '次/S', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:41:40', 1, 0, NULL, 'jvm-gc-young-collection_count_rate');
INSERT INTO `metric_dictionary_info` VALUES (5071, 'JVM指标', 'Old GC次数', '节点Old GC次数，Top节点趋势', '60S', '[平均值]当前时刻减去上一时刻通过GET _nodes/stats命令获取nodes.{nodeName}.jvm.gc.collectors.old.collection_count的值/时间间隔', 1, '次/S', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:41:33', 1, 0, NULL, 'jvm-gc-old-collection_count_rate');
INSERT INTO `metric_dictionary_info` VALUES (5073, 'JVM指标', 'Young GC耗时', '节点Young GC平均耗时，Top节点趋势', '60S', '[差值]  间隔时间内通过GET _nodes/stats命令获取nodes.{nodeName}.jvm.gc.collectors.young.collection_time_in_millis的差值/ 间隔时间内nodes.{nodeName}.jvm.gc.collectors.young.collection_count的差值', 0, 'MS', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:41:26', 1, 0, NULL, 'jvm-gc-young-collection_avg_time');
INSERT INTO `metric_dictionary_info` VALUES (5075, 'JVM指标', 'Old GC耗时', '节点Old GC平均耗时，Top节点趋势', '60S', '[差值]  间隔时间内通过GET _nodes/stats命令获取nodes.{nodeName}.jvm.gc.collectors.old.collection_time_in_millis的差值/ 间隔时间内nodes.{nodeName}.jvm.gc.collectors.old.collection_count的差值', 0, 'MS', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:41:17', 1, 0, NULL, 'jvm-gc-old-collection_avg_time');
INSERT INTO `metric_dictionary_info` VALUES (5077, 'JVM指标', 'JVM堆内存使用量', '节点JVM堆内存使用量，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取nodes.{nodeName}.jvm.mem.heap_used_in_bytes', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:40:52', 1, 0, NULL, 'jvm-mem-heap_used_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5079, 'JVM指标', 'JVM堆外存使用量', '节点JVM堆外存使用量，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取nodes.{nodeName}.jvm.mem.non_heap_used_in_bytes', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:40:44', 1, 0, NULL, 'jvm-mem-non_heap_used_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5081, 'JVM指标', 'JVM堆使用率', '节点JVM堆使用率，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取nodes.{nodeName}.jvm.mem.heap_used_percent', 1, '%', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:40:18', 1, 0, NULL, 'jvm-mem-heap_used_percent');
INSERT INTO `metric_dictionary_info` VALUES (5083, 'JVM指标', '堆内存young区使用空间', '节点年轻代堆内存使用空间，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取nodes.{nodeName}.jvm.mem.pools.young.used_in_bytes', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:40:10', 1, 0, NULL, 'jvm-mem-pools-young-used_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5085, 'JVM指标', '堆内存old区使用空间', '节点老年代堆内存使用空间，Top节点趋势', '当前值', '[当前值] GET _nodes/stats命令获取nodes.{nodeName}.jvm.mem.pools.old.used_in_bytes', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-09-30 10:40:03', 1, 0, NULL, 'jvm-mem-pools-old-used_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5087, 'breaker指标', 'Field data circuit breaker 内存占用', '统计当前fielddata占用内存总大小，与阀值比较，超过则熔断请求', '当前值', '[当前值] GET _nodes/stats命令获取breakers.fielddata.limit_size_in_bytes[阈值]，breakers.fielddata.estimated_size_in_bytes[当前值]', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-18 14:16:52', 1, 0, NULL, 'breakers-fielddata-limit_size_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5089, 'breaker指标', 'Request circuit breaker 内存占用', '统计当前请求(比如聚合请求临时内存构建）占用内存总大小，与阀值比较，超过则熔断请求', '当前值', '[当前值] GET _nodes/stats命令获取breakers.request.limit_size_in_bytes[阈值]，breakers.request.estimated_size_in_bytes[当前值]', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-18 14:17:03', 1, 0, NULL, 'breakers-request-limit_size_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5090, '基本性能指标', 'query Cache evictions', '节点Query Cache缓存驱逐数，Top节点趋势', '当前值', '[差值] 间隔时间内通过GET _nodes/stats命令获取nodes.{nodeName}.indices.query_cache.evictions的差值/时间间隔(S)', 0, '次/MIN', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-24 09:56:51', 1, 0, NULL, 'indices-query_cache-evictions');
INSERT INTO `metric_dictionary_info` VALUES (5091, 'breaker指标', 'inflight requests circuit breaker 内存占用', '统计当前请求body占用内存总大小，与阀值比较，超过则熔断请求', '当前值', '[当前值] GET _nodes/stats命令获取breakers.in_flight_requests.limit_size_in_bytes[阈值]，breakers.in_flight_requests.estimated_size_in_bytes[当前值]', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-18 14:17:04', 1, 0, NULL, 'breakers-in_flight_requests-limit_size_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5093, 'breaker指标', 'Accounting requests circuit breaker 内存占用', '统计当前请求结束后不能释放的对象(例如segment常驻的内存占用)所占用内存总大小，与阀值比较，超过则熔断请求', '当前值', '[当前值] GET _nodes/stats命令获取breakers.accounting.limit_size_in_bytes[阈值]，breakers.accounting.estimated_size_in_bytes[当前值]', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-18 14:17:29', 1, 0, NULL, 'breakers-accounting-limit_size_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5095, 'breaker指标', 'Script compilation circuit breaker 编译次数', '统计一段时间内脚本编译次数，与阀值比较，超过则熔断请求', '当前值', '[平均值] 当前时刻减去上个时刻通过GET _nodes/stats命令获取script.compilations的数量差值/时间间隔MIN', 0, '次/MIN', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-11 14:58:57', 1, 0, NULL, 'script-compilations');
INSERT INTO `metric_dictionary_info` VALUES (5097, 'breaker指标', 'Parent circuit breaker JVM真实内存占用', '统计JVM真实内存占用，与阀值比较，超过则熔断请求', '当前值', '[当前值] GET _nodes/stats命令获取breakers.parent.limit_size_in_bytes[阈值]，breakers.parent.estimated_size_in_bytes[当前值]', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Node', '2022-09-28 11:31:36', '2022-10-18 14:17:33', 1, 0, NULL, 'breakers-parent-limit_size_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5099, '索引基础指标', '索引Shard数', '索引Shard个数，Top节点趋势', '当前值', '[当前值] {indexName}/_stats?level=shards命令获取_shards.total', 1, '个', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-09-29 11:06:16', 1, 0, NULL, 'shardNu');
INSERT INTO `metric_dictionary_info` VALUES (5101, '索引基础指标', '索引存储大小', '索引存储总大小，Top节点趋势', '当前值', '[当前值] {indexName}/_stats?level=shards命令获取_all.total.store.size_in_bytes', 0, 'GB', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-09-29 11:06:16', 1, 0, NULL, 'store-size_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5103, '索引基础指标', '文档总数', '索引的文档总数，Top节点趋势', '当前值', '[当前值] {indexName}/_stats?level=shards命令获取_all.total.docs.count', 1, '个', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-09-29 11:06:17', 1, 0, NULL, 'docs-count');
INSERT INTO `metric_dictionary_info` VALUES (5105, '索引性能指标', '写入TPS', '索引写入速率平均值，Top节点趋势', '60S', '[平均值] (间隔时间内通过{indexName}/_stats?level=shards命令获取_all.total.docs.count的差值)/时间间隔(S)', 1, '个/S', '折线', 1, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-09-29 09:23:43', 1, 0, NULL, 'indexing-index_total_rate');
INSERT INTO `metric_dictionary_info` VALUES (5107, '索引性能指标', '写入耗时', '索引写入耗时平均值，Top节点趋势', '60S', '[平均值] (间隔时间内通过{indexName}/_stats?level=shards命令获取_all.total.indexing.index_time_in_millis的差值)/ 间隔时间内_all.total.indexing.index_total的差值', 1, 'MS', '折线', 1, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-11-02 16:55:05', 1, 0, NULL, 'indices-indexing-index_time_per_doc');
INSERT INTO `metric_dictionary_info` VALUES (5109, '索引性能指标', '网关写入TPS', 'Index通过网关的每秒写入请求数', '60S', '[平均值] (间隔时间内通过查询网关索引获取命中写入条件总数的差值)/时间间隔(S)', 0, '次/S', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-09-29 11:06:17', 1, 0, NULL, NULL);
INSERT INTO `metric_dictionary_info` VALUES (5111, '索引性能指标', '网关写入耗时', 'Index通过网关的写入平均耗时', '60S', '[平均值]  间隔时间内通过查询网关索引获取命中写入条件耗时的平均值', 0, 'MS', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-09-29 11:06:17', 1, 0, NULL, NULL);
INSERT INTO `metric_dictionary_info` VALUES (5113, '索引性能指标', '网关查询QPS', 'Index通过网关的每秒查询请求量', '60S', '[平均值] (间隔时间内通过查询网关索引获取命中查询总数的差值)/时间间隔(S)', 0, '次/S', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-09-29 11:06:17', 1, 0, NULL, NULL);
INSERT INTO `metric_dictionary_info` VALUES (5115, '索引性能指标', '网关查询耗时', 'Index通过网关的查询平均耗时', '60S', '[平均值]  间隔时间内通过查询网关索引获取命中查询耗时的平均值', 0, 'MS', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-09-29 11:06:17', 1, 0, NULL, NULL);
INSERT INTO `metric_dictionary_info` VALUES (5117, '索引性能指标', '查询Query QPS', '索引Query速率平均值，Top节点趋势', '60S', '[平均值] (间隔时间内通过{indexName}/_stats?level=shards命令获取_all.total.search.query_total的差值)/时间间隔(S)', 1, '次/S', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-09-29 11:06:17', 1, 0, NULL, 'search-query_total_rate');
INSERT INTO `metric_dictionary_info` VALUES (5119, '索引性能指标', 'Fetch QPS', '索引Fetch速率平均值，Top节点趋势', '60S', '[平均值] (间隔时间内通过{indexName}/_stats?level=shards命令获取_all.total.search.fetch_total的差值)/时间间隔(S)', 0, '次/S', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-10-11 11:23:19', 1, 0, NULL, 'search-fetch_total_rate');
INSERT INTO `metric_dictionary_info` VALUES (5121, '索引性能指标', '查询Query耗时', '索引Query耗时平均值，Top节点趋势', '60S', '[平均值]  间隔时间内通过{indexName}/_stats?level=shards命令获取_all.total.search.query_time_in_millis的差值/ 间隔时间内_all.total.search.query_total的差值', 0, 'MS', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-09-30 11:14:56', 1, 0, NULL, 'cost-query_time_in_millis');
INSERT INTO `metric_dictionary_info` VALUES (5123, '索引性能指标', '查询Fetch耗时', '索引Fetch耗时平均值，Top节点趋势', '60S', '[平均值] 间隔时间内通过{indexName}/_stats?level=shards命令获取_all.total.search.fetch_time_in_millis的差值/ 间隔时间内_all.total.search.fetch_total的差值', 0, 'MS', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-09-30 11:15:06', 1, 0, NULL, 'cost-fetch_time_in_millis');
INSERT INTO `metric_dictionary_info` VALUES (5125, '索引性能指标', '查询Scroll量', '索引间隔时间内所有Shard Scroll请求量，Top节点趋势', '60S', '[平均值] (间隔时间内通过{indexName}/_stats?level=shards命令获取_all.total.search.scroll_total的差值)/时间间隔(S)', 0, '个', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-09-29 11:06:02', 1, 0, NULL, 'search-scroll_total_rate');
INSERT INTO `metric_dictionary_info` VALUES (5127, '索引性能指标', '查询Scroll耗时', '索引Scorll耗时平均值，Top节点趋势', '60S', '[平均值]  间隔时间内通过{indexName}/_stats?level=shards命令获取_all.total.search.scroll_time_in_millis的差值/ 间隔时间内_all.total.search.scroll_total的差值', 0, 'MS', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-09-30 11:19:31', 1, 0, NULL, 'cost-scroll_time_in_millis');
INSERT INTO `metric_dictionary_info` VALUES (5129, '索引性能指标', 'Merge耗时', '索引Merge耗时平均值，Top节点趋势', '60S', '[平均值]  间隔时间内通过{indexName}/_stats?level=shards命令获取_all.total.merges.total_time_in_millis的差值/ 间隔时间内_all.total.merges.total的差值', 0, 'MS', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-09-30 11:17:24', 1, 0, NULL, 'cost-merges-total_time_in_millis');
INSERT INTO `metric_dictionary_info` VALUES (5131, '索引性能指标', 'Refresh耗时', '索引Refresh耗时平均值，Top节点趋势', '60S', '[平均值]  间隔时间内通过{indexName}/_stats?level=shards命令获取_all.total.refresh.total_time_in_millis的差值/ 间隔时间内_all.total.refresh.total的差值', 0, 'MS', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-09-30 11:17:39', 1, 0, NULL, 'cost-refresh-total_time_in_millis');
INSERT INTO `metric_dictionary_info` VALUES (5133, '索引性能指标', 'Flush耗时', '索引Flush耗时平均值，Top节点趋势', '60S', '[平均值]  间隔时间内通过{indexName}/_stats?level=shards命令获取_all.total.indices.flush.total_time_in_millis的差值/ 间隔时间内_all.total.indices.flush.total的差值', 0, 'MS', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-09-30 11:18:08', 1, 0, NULL, 'cost-flush-total_time_in_millis');
INSERT INTO `metric_dictionary_info` VALUES (5135, '索引性能指标', 'Merge次数', '索引Merge次数，Top节点趋势', '60S', '[平均值] (间隔时间内通过{indexName}/_stats?level=shards命令获取_all.total.merges.total的差值)/时间间隔(MIN)', 0, '次/MIN', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-09-29 11:06:02', 1, 0, NULL, 'merges-total_rate');
INSERT INTO `metric_dictionary_info` VALUES (5137, '索引性能指标', 'Refresh次数', '索引Refresh次数，Top节点趋势', '60S', '[平均值] (间隔时间内通过{indexName}/_stats?level=shards命令获取_all.total.refresh.total的差值)/时间间隔(MIN)', 0, '次/MIN', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-09-29 11:06:02', 1, 0, NULL, 'refresh-total_rate');
INSERT INTO `metric_dictionary_info` VALUES (5139, '索引性能指标', 'Flush次数', '索引Flush次数，Top节点趋势', '60S', '[平均值] (间隔时间内通过{indexName}/_stats?level=shards命令获取flush.total的差值)/时间间隔(MIN)', 0, '次/MIN', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-09-29 11:06:03', 1, 0, NULL, 'flush-total_rate');
INSERT INTO `metric_dictionary_info` VALUES (5141, '索引内存指标', 'Segements大小', '索引所有Shard的Segment底层Lucene内存汇总占用，Top节点趋势', '当前值', '[当前值] {indexName}/_stats?level=shards命令获取_all.total.segments.memory_in_bytes', 1, 'MB', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-10-27 11:13:35', 1, 0, NULL, 'segments-memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5143, '索引内存指标', 'Terms内存大小', '索引所有Shard的Segment底层Terms(Text/Keyword/...)内存汇总占用，是底层Lucene内存汇总占用的一个子项，Top节点趋势', '当前值', '[当前值] {indexName}/_stats?level=shards命令获取_all.total.segments.term_vectors_memory_in_bytes', 0, 'MB', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-10-27 11:13:40', 1, 0, NULL, 'segments-term_vectors_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5145, '索引内存指标', 'Points内存大小', '索引所有Shard的Segment底层Points(Numbers/IPs/Geo/...)内存汇总占用，是底层Lucene内存汇总占用的一个子项，Top节点趋势', '当前值', '[当前值] {indexName}/_stats?level=shards命令获取_all.total.segments.points_memory_in_bytes', 0, 'MB', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-10-27 11:14:44', 1, 0, NULL, 'segments-points_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5147, '索引内存指标', 'Doc Values内存大小', '索引所有Shard的Doc Values内存大小，是底层Lucene内存汇总占用的一个子项，Top节点趋势', '当前值', '[当前值] {indexName}/_stats?level=shards命令获取_all.total.segments.doc_values_memory_in_bytes', 0, 'MB', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-10-27 11:15:01', 1, 0, NULL, 'segments-doc_values_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5149, '索引内存指标', 'Index Writer内存大小', '索引所有Shard的Index Writer内存大小，不在Lucene内存占用统计范围内,Top节点趋势', '当前值', '[当前值] {indexName}/_stats?level=shards命令获取_all.total.segments.index_writer_memory_in_bytes', 0, 'MB', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-10-27 11:15:06', 1, 0, NULL, 'segments-index_writer_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5151, '索引内存指标', '未提交的Translog大小', '索引所有Shard的未提交Translog的大小累加值，Top节点趋势', '当前值', '[当前值] {indexName}/_stats?level=shards命令获取_all.total.translog.uncommitted_size_in_bytes', 0, 'MB', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-10-27 11:15:10', 1, 0, NULL, 'translog-size_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5153, '索引内存指标', 'Query Cache内存大小', '索引所有Shard Query Cache(Cached Filters/...)堆内存汇总占用，不在Lucene内存占用统计范围内，Top节点趋势', '当前值', '[当前值] GET {indexName}_/stats?level=shards命令获取_all.total.query_cache.memory_size_in_bytes', 1, 'MB', '折线', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-11-02 20:54:35', 1, 0, NULL, 'query_cache-memory_size_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5155, '索引内存指标', 'Stored Fields大小', '索引stored_fields_memory内存大小', '当前值', '[当前值] GET {indexName}_/stats?level=shards命令获取_all.total.segments.stored_fields_memory_in_bytes', 0, 'MB', NULL, 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-11-02 20:54:40', 1, 0, NULL, 'segments-stored_fields_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5157, '索引内存指标', 'Norms内存大小', '索引所有Shard Norms(normalization factors for query time/text scoring)内存大小累加值，是底层Lucene内存汇总占用的一个子项，Top节点趋势', '当前值', '[当前值] GET {indexName}_/stats?level=shards命令获取_all.total.segments.norms_memory_in_bytes', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-11-02 20:54:42', 1, 0, NULL, 'segments-norms_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5159, '索引内存指标', 'Version Map内存大小', '索引所有Shard Version Map(update/delete)内存大小累加值，不在Lucene内存占用统计范围内，Top节点趋势', '当前值', '[当前值] GET {indexName}_/stats?level=shards命令获取_all.total.segments.version_map_memory_in_bytes', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-11-02 20:54:45', 1, 0, NULL, 'segments-version_map_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5161, '索引内存指标', 'Fixed Bitsets内存大小', '索引所有Shard Fixed Bitsets(deeply nested object/...)内存大小累加值，是底层Lucene内存汇总占用的一个子项，Top节点趋势', '当前值', '[当前值] GET {indexName}_/stats?level=shards命令获取_all.total.segments.fixed_bit_set_memory_in_bytes', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-11-02 20:54:49', 1, 0, NULL, 'segments-fixed_bit_set_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5163, '索引内存指标', 'Fielddata内存大小', '索引所有Shard Fielddata(global ordinals /enable fielddata on text field/...)内存汇总占用，不在Lucene内存占用统计范围内，Top节点趋势', '当前值', '[当前值] GET {indexName}_/stats?level=shards命令获取_all.total.fielddata.memory_size_in_bytes', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-11-02 20:54:54', 1, 0, NULL, 'fielddata-memory_size_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5165, '索引内存指标', 'Request Cache内存大小', '索引所有Shard Request Cache(Cached Aggregation Results/...)堆内存汇总占用，不在Lucene内存占用统计范围内，Top节点趋势', '当前值', '[当前值] GET {indexName}_/stats?level=shards命令获取_all.total.request_cache.memory_size_in_bytes', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Index', '2022-09-28 11:31:36', '2022-11-02 20:54:58', 1, 0, NULL, 'segments-request_cache-memory_size_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5167, '索引模板基础指标', '索引Shard数', '索引模板下索引Shard个数，Top节点趋势', '当前值', '[当前值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取shards数量之和', 1, '个', '折线', 0, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-09-29 11:06:05', 1, 0, NULL, 'shardNu');
INSERT INTO `metric_dictionary_info` VALUES (5169, '索引模板基础指标', '索引存储大小', '索引模板下索引Shard存储总大小，Top节点趋势', '当前值', '[当前值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.store.size_in_bytes之和', 0, 'GB', '折线', 0, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-10-27 11:15:43', 1, 0, NULL, 'store-size_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5171, '索引模板基础指标', '文档总数', '索引模板下索引的文档数，Top节点趋势', '当前值', '[当前值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.docs.count之和', 1, '个', '折线', 0, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-10-27 11:15:48', 1, 0, NULL, 'docs-count');
INSERT INTO `metric_dictionary_info` VALUES (5173, '索引模板性能指标', '写入TPS', '索引模板写入速率平均值，Top节点趋势', '60S', '[平均值] 间隔时间内模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.docs.count的差值累加值/间隔时间', 1, '个/S', '折线', 1, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-10-27 11:15:53', 1, 0, NULL, 'indexing-index_total_rate');
INSERT INTO `metric_dictionary_info` VALUES (5175, '索引模板性能指标', '写入耗时', '索引模板写入耗时平均值，Top节点趋势', '60S', '[平均值] 间隔时间内模板下所有的索引通过GET {indexName}/_stats?level=shards命令获取_all.total.indexing.index_time_in_millis的差值)/ 间隔时间内_all.total.indexing.index_total的差值', 1, 'MS', '折线', 1, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-11-02 16:58:37', 1, 0, NULL, 'indices-indexing-index_time_per_doc');
INSERT INTO `metric_dictionary_info` VALUES (5177, '索引模板性能指标', '查询Query QPS', '索引模板Query速率平均值，Top节点趋势', '60S', '[平均值] 间隔时间内模板下所有的索引通过{indexName}/_stats?level=shards命令获取 all.total.search.query_total的差值累加值/间隔时间', 1, '次/S', '折线', 0, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-10-27 11:16:07', 1, 0, NULL, 'search-query_total_rate');
INSERT INTO `metric_dictionary_info` VALUES (5179, '索引模板性能指标', '网关写入TPS', 'IndexTemplate所属Index通过网关的每秒写入请求数', '60S', NULL, 0, '次/S', '折线', 0, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-09-29 11:06:06', 1, 0, NULL, NULL);
INSERT INTO `metric_dictionary_info` VALUES (5181, '索引模板性能指标', '网关写入耗时', 'IndexTemplate所属Index通过网关的写入平均耗时', '60S', NULL, 0, 'MS', '折线', 0, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-09-29 11:06:06', 1, 0, NULL, NULL);
INSERT INTO `metric_dictionary_info` VALUES (5183, '索引模板性能指标', '网关查询QPS', 'IndexTemplate所属Index通过网关的每秒查询请求量', '60S', NULL, 0, '次/S', '折线', 0, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-09-29 11:06:06', 1, 0, NULL, NULL);
INSERT INTO `metric_dictionary_info` VALUES (5185, '索引模板性能指标', '网关查询耗时', 'IndexTemplate所属Index通过网关的查询平均耗时', '60S', NULL, 0, 'MS', '折线', 0, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-09-29 11:06:07', 1, 0, NULL, NULL);
INSERT INTO `metric_dictionary_info` VALUES (5187, '索引模板性能指标', '查询Fetch QPS', '索引模板Fetch速率平均值，Top节点趋势', '60S', '[平均值]  间隔时间内获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.search.fetch_total的差值累加值/时间间隔', 0, '次/S', '折线', 0, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-10-24 10:58:35', 1, 0, NULL, 'search-fetch_total_rate');
INSERT INTO `metric_dictionary_info` VALUES (5189, '索引模板性能指标', '查询Query耗时', '索引模板Query耗时平均值，Top节点趋势', '60S', '[平均值] 间隔时间内模板下所有的索引通过{indexName}/_stats?level=shards命令_all.total.search.query_time_in_millis的差值累加值/_all.total.search.query_total的差值累加值', 0, 'MS', '折线', 0, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-10-24 10:57:12', 1, 0, NULL, 'cost-query_time_in_millis');
INSERT INTO `metric_dictionary_info` VALUES (5191, '索引模板性能指标', '查询Fetch耗时', '索引模板Fetch耗时平均值，Top节点趋势', '60S', '[平均值] 间隔时间内获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.search.fetch_time_in_millis的差值累加值/_all.total.search.fetch_total的差值累加值', 0, 'MS', '折线', 0, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-10-24 10:59:22', 1, 0, NULL, 'cost-fetch_time_in_millis');
INSERT INTO `metric_dictionary_info` VALUES (5193, '索引模板性能指标', '查询Scroll量', '索引模板下索引Shard 级别 Scroll请求量，Top节点趋势', '60S', '[平均值] 获取间隔时间内模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.search.scroll_total的差值累加值/时间间隔', 0, '个', '折线', 0, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-10-24 10:44:38', 1, 0, NULL, 'search-scroll_total_rate');
INSERT INTO `metric_dictionary_info` VALUES (5195, '索引模板性能指标', '查询Scroll耗时', '索引模板Scorll耗时平均值，Top节点趋势', '60S', '[平均值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.search.scroll_time_in_millis的差值累加值/_all.total.search.scroll_total的差值累加值', 0, 'MS', '折线', 0, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-09-30 11:19:09', 1, 0, NULL, 'cost-scroll_time_in_millis');
INSERT INTO `metric_dictionary_info` VALUES (5197, '索引模板性能指标', 'Merge耗时', '索引模板Merge耗时平均值，Top节点趋势', '60S', '[平均值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.merges.total_time_in_millis的差值累加值/_all.total.merges.total的差值累加值', 0, 'MS', '折线', 0, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-09-29 11:06:08', 1, 0, NULL, 'cost-merges-total_time_in_millis');
INSERT INTO `metric_dictionary_info` VALUES (5199, '索引模板性能指标', 'Refresh耗时', '索引模板Refresh耗时平均值，Top节点趋势', '60S', '[平均值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.refresh.total_time_in_millis的差值累加值/_all.total.refresh.total的差值累加值', 0, 'MS', '折线', 0, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-09-29 11:06:09', 1, 0, NULL, 'cost-refresh-total_time_in_millis');
INSERT INTO `metric_dictionary_info` VALUES (5201, '索引模板性能指标', 'Flush耗时', '索引模板Flush耗时平均值，Top节点趋势', '60S', '[平均值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.flush.total_time_in_millis的差值累加值/all.total.flush.total的差值累加值', 0, 'MS', '折线', 0, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-09-29 11:06:09', 1, 0, NULL, 'cost-flush-total_time_in_millis');
INSERT INTO `metric_dictionary_info` VALUES (5203, '索引模板性能指标', 'Merge次数', '索引模板Merge次数，Top节点趋势', '60S', '[平均值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.merges.total的差值累加值/时间间隔', 0, '次/MIN', '折线', 0, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-10-24 10:59:59', 1, 0, NULL, 'merges-total_rate');
INSERT INTO `metric_dictionary_info` VALUES (5205, '索引模板性能指标', 'Refresh次数', '索引模板Refresh次数，Top节点趋势', '60S', '[平均值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.refresh.total的差值累加值/时间间隔', 0, '次/MIN', '折线', 0, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-10-24 10:57:58', 1, 0, NULL, 'refresh-total_rate');
INSERT INTO `metric_dictionary_info` VALUES (5207, '索引模板性能指标', 'Flush次数', '索引模板Flush次数，Top节点趋势', '60S', '[平均值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.flush-total_rate的累加值差值/时间间隔', 0, '次/MIN', '折线', 0, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-09-29 11:06:10', 1, 0, NULL, 'flush-total_rate');
INSERT INTO `metric_dictionary_info` VALUES (5209, '索引模板内存指标', 'Segements大小', '索引模板下索引所有Shard的Segment底层Lucene内存汇总占用，Top节点趋势', '当前值', '[当前值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.segments.memory_in_bytes的总和', 1, 'MB', '折线', 0, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-09-29 11:06:10', 1, 0, NULL, 'segments-memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5211, '索引模板内存指标', 'Terms内存大小', '索引模板下索引所有Shard的Segment底层Terms(Text/Keyword/...)内存汇总占用，是底层Lucene内存汇总占用的一个子项，Top节点趋势', '当前值', '[当前值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.segments.term_vectors_memory_in_bytes的总和', 0, 'MB', '折线', 0, NULL, NULL, 'Index_template', '2022-09-28 11:31:36', '2022-09-29 11:06:10', 1, 0, NULL, 'segments-term_vectors_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5213, '索引模板内存指标', 'Points内存大小', '索引模板下索引所有Shard的Segment底层Points(Numbers/IPs/Geo/...)内存汇总占用，是底层Lucene内存汇总占用的一个子项，Top节点趋势', '当前值', '[当前值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.segments.points_memory_in_bytes的总和', 0, 'MB', '折线', 0, NULL, NULL, 'Index_template', '2022-09-28 11:31:36', '2022-09-29 11:06:11', 1, 0, NULL, 'segments-points_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5215, '索引模板内存指标', 'Doc Values内存大小', '索引模板下索引所有Shard的Doc Values内存大小，是底层Lucene内存汇总占用的一个子项，Top节点趋势', '当前值', '[当前值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.segments.doc_values_memory_in_bytes的总和', 0, 'MB', '折线', 0, NULL, NULL, 'Index_template', '2022-09-28 11:31:36', '2022-09-29 11:06:11', 1, 0, NULL, 'segments-doc_values_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5217, '索引模板内存指标', 'Index Writer内存大小', '索引模板下索引所有Shard的Index Writer内存大小，不在Lucene内存占用统计范围内,Top节点趋势', '当前值', '[当前值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.segments.index_writer_memory_in_bytes的总和', 0, 'MB', '折线', 0, NULL, NULL, 'Index_template', '2022-09-28 11:31:36', '2022-09-29 11:06:11', 1, 0, NULL, 'segments-index_writer_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5219, '索引模板内存指标', '未提交的Translog大小', '索引模板下索引所有Shard的未提交Translog的大小累加值，Top节点趋势', '当前值', '[当前值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.translog.uncommitted_size_in_bytes的总和', 0, 'MB', '折线', 0, NULL, NULL, 'Index_template', '2022-09-28 11:31:36', '2022-09-29 11:06:11', 1, 0, NULL, 'translog-size_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5221, '索引模板内存指标', 'Query Cache内存大小', '索引模板下索引所有Shard Query Cache(Cached Filters/...)堆内存汇总占用，不在Lucene内存占用统计范围内，Top节点趋势', '当前值', '[当前值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.query_cache.memory_size_in_bytes的总和', 1, 'MB', '折线', 0, NULL, NULL, 'Index_template', '2022-09-28 11:31:36', '2022-09-29 11:06:11', 1, 0, NULL, 'query_cache-memory_size_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5223, '索引模板内存指标', 'Stored Fields大小', '索引模板下索引stored_fields_memory内存大小', '当前值', '[当前值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.segments.stored_fields_memory_in_bytes的总和', 0, NULL, NULL, 0, NULL, NULL, 'Index_template', '2022-09-28 11:31:36', '2022-09-29 11:06:12', 1, 0, NULL, 'segments-stored_fields_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5225, '索引模板内存指标', 'Norms内存大小', '索引模板下索引所有Shard Norms(normalization factors for query time/text scoring)内存大小累加值，是底层Lucene内存汇总占用的一个子项，Top节点趋势', '当前值', '[当前值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.segments-norms_memory_in_bytes的总和', 0, 'MB', '折线图', 0, NULL, NULL, 'Index_template', '2022-09-28 11:31:36', '2022-11-02 19:22:53', 1, 0, NULL, 'segments-norms_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5227, '索引模板内存指标', 'Version Map内存大小', '索引模板下索引所有Shard Version Map(update/delete)内存大小累加值，不在Lucene内存占用统计范围内，Top节点趋势', '当前值', '[当前值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.segments.version_map_memory_in_bytes的总和', 0, 'MB', '折线图', 0, NULL, NULL, 'Index_template', '2022-09-28 11:31:36', '2022-11-02 19:23:20', 1, 0, NULL, 'segments-version_map_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5229, '索引模板内存指标', 'Fixed Bitsets内存大小', '索引模板下索引所有Shard Fixed Bitsets(deeply nested object/...)内存大小累加值，是底层Lucene内存汇总占用的一个子项，Top节点趋势', '当前值', '[当前值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.segments.fixed_bit_set_memory_in_bytes的总和', 0, 'MB', '折线图', 0, NULL, NULL, 'Index_template', '2022-09-28 11:31:36', '2022-11-02 19:23:39', 1, 0, NULL, 'segments-fixed_bit_set_memory_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5231, '索引模板内存指标', 'Fielddata内存大小', '索引模板下索引所有Shard Fielddata(global ordinals /enable fielddata on text field/...)内存汇总占用，不在Lucene内存占用统计范围内，Top节点趋势', '当前值', '[当前值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.fielddata.memory_size_in_bytes的总和', 0, 'MB', '折线图', 0, NULL, NULL, 'Index_template', '2022-09-28 11:31:36', '2022-11-02 19:23:49', 1, 0, NULL, 'fielddata-memory_size_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5233, '索引模板内存指标', 'Request Cache内存大小', '索引模板下索引所有Shard Request Cache(Cached Aggregation Results/...)堆内存汇总占用，不在Lucene内存占用统计范围内，Top节点趋势', '当前值', '[当前值] 获取模板下所有的索引通过{indexName}/_stats?level=shards命令获取_all.total.request_cache.memory_size_in_bytes的总和', 0, 'MB', '折线图', 0, 'ES引擎', NULL, 'Index_template', '2022-09-28 11:31:36', '2022-11-02 19:20:19', 1, 0, NULL, 'segments-request_cache-memory_size_in_bytes');
INSERT INTO `metric_dictionary_info` VALUES (5235, '集群', '集群健康状态', '不同健康状态集群分布感知，快速定位故障集群', '当前值', '[当前值] 通过GET _cluster/health命令获取status', 0, NULL, '状态栏', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-10-08 15:18:40', 1, 0, NULL, 'health');
INSERT INTO `metric_dictionary_info` VALUES (5237, '集群', '指标采集延时', '指标数据质量风险集群预警', '当前值', '[当前值] 采集数据最近一个时间点和当前时间点的差值', 0, 'S', '列表', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-09-30 11:22:01', 1, 0, NULL, 'clusterElapsedTimeGte5Min');
INSERT INTO `metric_dictionary_info` VALUES (5239, '集群', 'shard个数大于10000集群', 'Shard膨胀风险集群预警', '当前值', '[当前值] 通过GET _cat/health?format=json获取shards总数量(包括unassign状态)', 0, NULL, '列表', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-10-10 15:49:42', 1, 0, NULL, 'shardNum');
INSERT INTO `metric_dictionary_info` VALUES (5241, '集群', '写入耗时', '索引写入性能对比分析，性能不足集群预警', '5*60S', '[最大值] 集群下的所有节点,间隔时间内通过_node/stats命令获取nodes.{nodeName}.indices.indexing.index_time_in_millis差值累加值/节点间隔时间内nodes.{nodeName}.indices.indexing.index_total差值累加值', 0, 'S', '折线图，可选Top5-Top50', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-11-02 19:51:22', 1, 0, NULL, 'indexingLatency');
INSERT INTO `metric_dictionary_info` VALUES (5243, '集群', 'node_stats接口平均采集耗时', 'Master指标采集性能问题集群预警', '当前值', '[当前值] 调用一次_nodes/stats命令所消耗的时间', 0, 'S', '折线图，可选Top5-Top50', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-10-10 15:50:23', 1, 0, NULL, 'nodeElapsedTime');
INSERT INTO `metric_dictionary_info` VALUES (5245, '集群', '集群pending task数', 'pending task持续堆积，Master元数据处理性能问题集群预警', '当前值', '[当前值] 通过 _cluster/health命令获取number_of_pending_tasks', 0, '个', '折线图，可选Top5-Top50', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-09-30 11:22:23', 1, 0, NULL, 'pendingTaskNum');
INSERT INTO `metric_dictionary_info` VALUES (5247, '集群', '网关失败率', '各组网关业务查询健康预警', '5*60S', '5分钟执行一次，获取近一分钟内的网关失败率', 0, '%', '折线图，可选Top5-Top50', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-09-30 11:22:26', 1, 0, NULL, 'gatewayFailedPer');
INSERT INTO `metric_dictionary_info` VALUES (5249, '集群', '查询耗时', '索引查询性能对比分析，查询性能不足集群预警', '5*60S', '[最大值] 集群下的所有节点,间隔时间内通过_node/stats命令获取nodes.{nodeName}.indices.search.query_time_in_millis差值累加值/节点间隔时间内nodes.{nodeName}.indices.search.query_total差值累加值', 0, 'S', '折线图，可选Top5-Top50', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-11-02 19:51:08', 1, 0, NULL, 'searchLatency');
INSERT INTO `metric_dictionary_info` VALUES (5251, '节点', '节点执行任务耗时', '节点执行任务平均耗时高', '5*60S', '[平均值]根据_cat/tasks?v&detailed&format=json命令获取到当前时间的各节点任务执行总和/节点执行的次数', 0, 'S', '折线图，可选Top5-Top50', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-09-30 11:22:56', 1, 0, NULL, 'taskConsuming');
INSERT INTO `metric_dictionary_info` VALUES (5253, '节点', '磁盘利用率超红线节点', '磁盘利用率超安全水位节点预警', '当前值', '[当前值]根据GET _nodes/stats命令获取到(nodes.{nodeName}.fs.total-nodes.{nodeName}.fs.free_in_bytes)/nodes.{nodeName}.fs.total大于阀值的节点', 0, NULL, '列表', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-09-30 11:23:34', 1, 0, NULL, 'largeDiskUsage');
INSERT INTO `metric_dictionary_info` VALUES (5255, '节点', '分片个数大于500节点', '分片数超安全水位节点预警', '当前值', '[当前值]根据_cat/shards?v&h=node命令获取到结果个数大于阀值的节点', 0, NULL, '列表', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-10-08 16:55:51', 1, 0, NULL, 'shardNum');
INSERT INTO `metric_dictionary_info` VALUES (5257, '节点', '堆内存利用率超红线节点', '堆内存利用率超红线节点预警', '当前值', '[当前值]根据GET _nodes/stats命令获取nodes.{nodeName}.jvm.mem.heap_used_percent大于阀值的节点', 0, NULL, '列表', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-09-30 11:23:28', 1, 0, NULL, 'largeHead');
INSERT INTO `metric_dictionary_info` VALUES (5259, '节点', 'CPU利用率超红线节点', 'CPU利用率超红线节点预警', '当前值', '[当前值]根据GET _nodes/stats命令获取到nodes.{nodeName}.os.cpu.percent大于阀值的节点', 0, NULL, '列表', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-09-30 11:23:32', 1, 0, NULL, 'largeCpuUsage');
INSERT INTO `metric_dictionary_info` VALUES (5261, '节点', 'SearchRejected节点', 'SearchRejected节点预警', '5*60S', '[当前值]当前时间和上次时间通过GET _nodes/stats命令nodes.{nodeName}.thread_pool.search.rejected差值不为0的节点', 0, NULL, '列表', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-09-30 11:23:41', 1, 0, NULL, 'searchRejectedNum');
INSERT INTO `metric_dictionary_info` VALUES (5263, '节点', 'WriteRejected节点', 'WriteRejected节点预警', '5*60S', '[当前值]当前时间和上次时间通过GET _nodes/stats命令nodes.{nodeName}.thread_pool.write.rejected差值不为0的节点', 0, NULL, '列表', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-09-30 11:23:43', 1, 0, NULL, 'writeRejectedNum');
INSERT INTO `metric_dictionary_info` VALUES (5265, '索引', 'segments内存大于1MB索引模板', '索引模板超大内存占用风险预警', '当前值', '[当前值]根据_cat/segments/命令获取size字段大于阀值的索引', 0, NULL, '列表', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-10-08 16:59:56', 1, 1, 'segments内存大于1MB索引模板', 'tplSegmentMemSize');
INSERT INTO `metric_dictionary_info` VALUES (5267, '索引', 'segments个数大于20索引模板', '索引模板Segements数超红线预警', '当前值', '[当前值]根据_cat/segments/命令获取segment个数大于阀值的索引', 0, NULL, '列表', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-10-08 16:59:58', 1, 0, NULL, 'tplSegmentNum');
INSERT INTO `metric_dictionary_info` VALUES (5269, '索引', '未分配shard索引', 'shard未分配索引预警', '当前值', '[当前值]根据GET {indexName}/_stats命令获取索引状态不等于green的索引', 0, NULL, '列表', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-09-30 11:24:09', 1, 0, NULL, 'unassignedShard');
INSERT INTO `metric_dictionary_info` VALUES (5271, '索引', 'mapping字段个数大于100索引', '索引mapping字段膨胀预警', '当前值', '[当前值]根据GET {indexName}命令mapping的字段个数大于阀值的索引', 0, NULL, '列表', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-09-30 11:24:13', 1, 0, NULL, 'mappingNum');
INSERT INTO `metric_dictionary_info` VALUES (5273, '索引', 'segments内存大于100B索引', '索引超大内存占用风险预警', '当前值', '[当前值]根据_cat/segments/命令获取segment内存（size）大于阀值的索引', 0, NULL, '列表', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-09-30 11:24:20', 1, 0, NULL, 'segmentMemSize');
INSERT INTO `metric_dictionary_info` VALUES (5275, '索引', 'segments个数大于100索引', '索引Segements数超红线预警', '当前值', '[当前值]根据_cat/segments/命令获取segment个数大于阀值的索引', 0, NULL, '列表', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-09-30 11:24:24', 1, 0, NULL, 'segmentNum');
INSERT INTO `metric_dictionary_info` VALUES (5277, '索引', 'RED索引', 'RED索引预警', '当前值', '[当前值]根据_cat/indices?format=json命令获取健康状态(health)等于red的索引', 0, NULL, '列表', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-09-30 11:24:59', 1, 0, NULL, 'red');
INSERT INTO `metric_dictionary_info` VALUES (5279, '索引', '单个shard大于500MB索引', '单Shard过大索引预警', '当前值', '[当前值]根据_cat/shards命令获取大小大于指定的阀值的索引', 0, NULL, '列表', 0, 'ES引擎', NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-10-08 16:57:51', 1, 0, NULL, 'bigShard');
INSERT INTO `metric_dictionary_info` VALUES (5281, '索引', '无副本索引', '无副本索引稳定性预警', '当前值', '[当前值]根据_cat/indices命令获取副本数等于0的索引', 0, NULL, '列表', 0, NULL, NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-09-30 11:25:12', 1, 0, NULL, 'singReplicate');
INSERT INTO `metric_dictionary_info` VALUES (5283, '索引', '单个shard小于500MB索引', '索引Shard数分配不合理预警', '当前值', '[当前值]根据_cat/shards命令获取大小(store)小于指定的阀值并且shard数量大于1的索引', 0, NULL, '列表', 0, NULL, NULL, 'Dashboard', '2022-09-28 11:31:36', '2022-10-08 16:58:06', 1, 0, NULL, 'smallShard');



#重新全量导入权限点表
truncate table kf_security_permission;
insert into kf_security_permission (id, permission_name, parent_id, leaf, level, description, create_time, update_time, is_delete, app_name)
values  (1593, '物理集群', 0, 0, 1, '物理集群', '2022-05-24 18:08:22.0', '2022-08-24 20:07:31.0', 0, 'know_search'),
        (1595, '我的集群', 0, 0, 1, '我的集群', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1597, '集群版本', 0, 0, 1, '集群版本', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1599, 'Gateway管理', 0, 0, 1, 'Gateway管理', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1601, '模板管理', 0, 0, 1, '模板管理', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1603, '模板服务', 0, 0, 1, '模板服务', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1605, '索引管理', 0, 0, 1, '索引管理', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1607, '索引服务', 0, 0, 1, '索引服务', '2022-05-24 18:08:22.0', '2022-05-24 18:24:16.0', 0, 'know_search'),
        (1609, '索引查询', 0, 0, 1, '索引查询', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1611, '查询诊断', 0, 0, 1, '查询诊断', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1613, '集群看板', 0, 0, 1, '集群看板', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1615, '网关看板', 0, 0, 1, '网关看板', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1617, '我的申请', 0, 0, 1, '我的申请', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1619, '我的审批', 0, 0, 1, '我的审批', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1621, '任务列表', 0, 0, 1, '任务列表', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1623, '调度任务列表', 0, 0, 1, '调度任务列表', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1625, '调度日志', 0, 0, 1, '调度日志', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1627, '用户管理', 0, 0, 1, '用户管理', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1629, '角色管理', 0, 0, 1, '角色管理', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1631, '应用管理', 0, 0, 1, '应用管理', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1633, '平台配置', 0, 0, 1, '平台配置', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1635, '操作记录', 0, 0, 1, '操作记录', '2022-05-24 18:08:22.0', '2022-05-24 18:08:22.0', 0, 'know_search'),
        (1637, '查看集群列表及详情', 1593, 1, 2, '查看集群列表及详情', '2022-05-24 18:08:22.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1639, '接入集群', 1593, 1, 2, '接入集群', '2022-05-24 18:08:22.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1641, '新建集群', 1593, 1, 2, '新建集群', '2022-05-24 18:08:22.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1643, '扩缩容', 1593, 1, 2, '扩缩容', '2022-05-24 18:08:22.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1645, '升级', 1593, 1, 2, '升级', '2022-05-24 18:08:22.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1647, '重启', 1593, 1, 2, '重启', '2022-05-24 18:08:23.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1649, '配置变更', 1593, 1, 2, '配置变更', '2022-05-24 18:08:23.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1651, 'Region划分', 1593, 1, 2, 'Region划分', '2022-05-24 18:08:23.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1653, 'Region管理', 1593, 1, 2, 'Region管理', '2022-05-24 18:08:23.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1655, '快捷命令', 1593, 1, 2, '快捷命令', '2022-05-24 18:08:23.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1657, '编辑', 1593, 1, 2, '编辑', '2022-05-24 18:08:23.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1659, '绑定Gateway', 1593, 1, 2, '绑定Gateway', '2022-05-24 18:08:23.0', '2022-05-24 18:10:32.0', 0, 'know_search'),
        (1661, '下线', 1593, 1, 2, '下线', '2022-05-24 18:08:23.0', '2022-05-24 18:10:52.0', 0, 'know_search'),
        (1663, '查看集群列表及详情', 1595, 1, 2, '查看集群列表及详情', '2022-05-24 18:08:23.0', '2022-05-24 18:10:52.0', 0, 'know_search'),
        (1665, '申请集群', 1595, 1, 2, '申请集群', '2022-05-24 18:08:23.0', '2022-05-24 18:10:52.0', 0, 'know_search'),
        (1667, '编辑', 1595, 1, 2, '编辑', '2022-05-24 18:08:23.0', '2022-05-24 18:10:52.0', 0, 'know_search'),
        (1669, '扩缩容', 1595, 1, 2, '扩缩容', '2022-05-24 18:08:23.0', '2022-05-24 18:10:52.0', 0, 'know_search'),
        (1671, '下线', 1595, 1, 2, '下线', '2022-05-24 18:08:23.0', '2022-05-24 18:20:44.0', 0, 'know_search'),
        (1673, '查看版本列表', 1597, 1, 2, '查看版本列表', '2022-05-24 18:08:23.0', '2022-05-24 18:20:44.0', 0, 'know_search'),
        (1675, '新增版本', 1597, 1, 2, '新增版本', '2022-05-24 18:08:23.0', '2022-05-24 18:20:44.0', 0, 'know_search'),
        (1677, '编辑', 1597, 1, 2, '编辑', '2022-05-24 18:08:23.0', '2022-05-24 18:20:44.0', 0, 'know_search'),
        (1679, '删除', 1597, 1, 2, '删除', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1681, '查看Gateway 集群列表', 1599, 1, 2, '查看Gateway 集群列表', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1683, '接入gateway', 1599, 1, 2, '接入gateway', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1685, '编辑', 1599, 1, 2, '编辑', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1687, '下线', 1599, 1, 2, '下线', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1689, '查看模板列表及详情', 1601, 1, 2, '查看模板列表及详情', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1691, '申请模板', 1601, 1, 2, '申请模板', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1693, '编辑', 1601, 1, 2, '编辑', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1695, '下线', 1601, 1, 2, '下线', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1697, '编辑Mapping', 1601, 1, 2, '编辑Mapping', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1699, '编辑Setting', 1601, 1, 2, '编辑Setting', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1701, '查看模板列表', 1603, 1, 2, '查看模板列表', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1703, '开关：预创建', 1603, 1, 2, '开关：预创建', '2022-05-24 18:08:23.0', '2022-06-14 16:49:48.0', 0, 'know_search'),
        (1705, '开关：过期删除', 1603, 1, 2, '开关：过期删除', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1707, '开关：冷热分离', 1603, 1, 2, '开关：冷热分离', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1709, '开关：pipeline', 1603, 1, 2, '开关：写入限流', '2022-05-24 18:08:23.0', '2022-06-14 16:49:49.0', 0, 'know_search'),
        (1711, '开关：Rollover', 1603, 1, 2, '开关：Rollover', '2022-05-24 18:08:23.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1713, '查看DCDR链路', 1603, 1, 2, '查看DCDR链路', '2022-05-24 18:08:23.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1715, '创建DCDR链路', 1603, 1, 2, '创建DCDR链路', '2022-05-24 18:08:24.0', '2022-05-24 18:20:45.0', 0, 'know_search'),
        (1717, '清理', 1603, 1, 2, '清理', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1719, '扩缩容', 1603, 1, 2, '扩缩容', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1721, '升版本', 1603, 1, 2, '升版本', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1723, '批量操作', 1603, 1, 2, '批量操作', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1725, '查看索引列表及详情', 1605, 1, 2, '查看索引列表及详情', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1727, '编辑Mapping', 1605, 1, 2, '编辑Mapping', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1729, '编辑Setting', 1605, 1, 2, '编辑Setting', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1731, '禁用读', 1607, 1, 2, '禁用读', '2022-05-24 18:08:24.0', '2022-07-15 08:50:56.0', 0, 'know_search'),
        (1733, '禁用写', 1607, 1, 2, '禁用写', '2022-05-24 18:08:24.0', '2022-07-15 08:50:56.0', 0, 'know_search'),
        (1735, '设置别名', 1605, 1, 2, '设置别名', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1737, '删除别名', 1605, 1, 2, '删除别名', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1739, '关闭索引', 1607, 1, 2, '关闭索引', '2022-05-24 18:08:24.0', '2022-07-15 09:52:25.0', 0, 'know_search'),
        (1741, '下线', 1605, 1, 2, '下线', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1743, '批量删除', 1605, 1, 2, '批量删除', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1745, '查看列表', 1607, 1, 2, '查看列表', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1747, '执行Rollover', 1607, 1, 2, '执行Rollover', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1749, '执行shrink', 1607, 1, 2, '执行shrink', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1751, '执行split', 1607, 1, 2, '执行split', '2022-05-24 18:08:24.0', '2022-05-24 18:20:46.0', 0, 'know_search'),
        (1753, '执行ForceMerge', 1607, 1, 2, '执行ForceMerge', '2022-05-24 18:08:24.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1755, '批量执行', 1607, 1, 2, '批量执行', '2022-05-24 18:08:24.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1757, 'DSL查询', 1877, 1, 2, 'DSL查询', '2022-05-24 18:08:24.0', '2022-09-05 14:24:00.0', 0, 'know_search'),
        (1759, '查询模板', 0, 0, 1, '查看查询模板列表', '2022-05-24 18:08:24.0', '2022-08-11 10:37:43.0', 0, 'know_search'),
        (1761, '查看集群看板', 1613, 1, 2, '查看集群看板', '2022-05-24 18:08:24.0', '2022-06-14 16:37:54.0', 0, 'know_search'),
        (1763, '查看网关看板', 1615, 1, 2, '查看网关看板', '2022-05-24 18:08:24.0', '2022-06-14 16:38:14.0', 0, 'know_search'),
        (1765, '查看我的申请列表', 1617, 1, 2, '查看我的申请列表', '2022-05-24 18:08:24.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1767, '撤回', 1617, 1, 2, '撤回', '2022-05-24 18:08:24.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1769, '查看我的审批列表', 1619, 1, 2, '查看我的审批列表', '2022-05-24 18:08:24.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1771, '驳回', 1619, 1, 2, '撤回', '2022-05-24 18:08:24.0', '2022-07-18 20:57:33.0', 0, 'know_search'),
        (1773, '通过', 1619, 1, 2, '通过', '2022-05-24 18:08:24.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1775, '查看任务列表', 1621, 1, 2, '查看任务列表', '2022-05-24 18:08:24.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1777, '查看进度', 1621, 1, 2, '查看进度', '2022-05-24 18:08:24.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1779, '执行', 1621, 1, 2, '执行', '2022-05-24 18:08:24.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1781, '暂停', 1621, 1, 2, '暂停', '2022-05-24 18:08:24.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1783, '重试', 1621, 1, 2, '重试', '2022-05-24 18:08:25.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1785, '取消', 1621, 1, 2, '取消', '2022-05-24 18:08:25.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1787, '查看日志（子任务）', 1621, 1, 2, '查看日志（子任务）', '2022-05-24 18:08:25.0', '2022-05-24 18:20:47.0', 0, 'know_search'),
        (1789, '重试（子任务）', 1621, 1, 2, '重试（子任务）', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1791, '忽略（子任务）', 1621, 1, 2, '忽略（子任务）', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1793, '查看详情（DCDR）', 1621, 1, 2, '查看详情（DCDR）', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1795, '取消（DCDR）', 1621, 1, 2, '取消（DCDR）', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1797, '重试（DCDR）', 1621, 1, 2, '重试（DCDR）', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1799, '强切（DCDR）', 1621, 1, 2, '强切（DCDR）', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1801, '返回（DCDR）', 1621, 1, 2, '返回（DCDR）', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1803, '查看任务列表', 1623, 1, 2, '查看任务列表', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1805, '查看日志', 1623, 1, 2, '查看日志', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1807, '执行', 1623, 1, 2, '执行', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1809, '暂停', 1623, 1, 2, '暂停', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1811, '查看调度日志列表', 1625, 1, 2, '查看调度日志列表', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1813, '调度详情', 1625, 1, 2, '调度详情', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1815, '执行日志', 1625, 1, 2, '执行日志', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1817, '终止任务', 1625, 1, 2, '终止任务', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1819, '查看用户列表', 1627, 1, 2, '查看用户列表', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1821, '分配角色', 1627, 1, 2, '分配角色', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1823, '查看角色列表', 1629, 1, 2, '查看角色列表', '2022-05-24 18:08:25.0', '2022-05-24 18:20:48.0', 0, 'know_search'),
        (1825, '编辑', 1629, 1, 2, '编辑', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1827, '绑定用户', 1629, 1, 2, '绑定用户', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1829, '回收用户', 1629, 1, 2, '回收用户', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1831, '删除角色', 1629, 1, 2, '删除角色', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1833, '查看应用列表', 1631, 1, 2, '查看应用列表', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1835, '新建应用', 1631, 1, 2, '新建应用', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1837, '编辑', 1631, 1, 2, '编辑', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1839, '删除', 1631, 1, 2, '删除', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1841, '访问设置', 1631, 1, 2, '访问设置', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1843, '查看平台配置列表', 1633, 1, 2, '查看平台配置列表', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1845, '新增平台配置', 1633, 1, 2, '新增平台配置', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1847, '禁用平台配置', 1633, 1, 2, '禁用平台配置', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1849, '编辑平台配置', 1633, 1, 2, '编辑平台配置', '2022-05-24 18:08:25.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1851, '删除平台配置', 1633, 1, 2, '删除平台配置', '2022-05-24 18:08:26.0', '2022-05-24 18:20:49.0', 0, 'know_search'),
        (1853, '查看操作记录列表', 1635, 1, 2, '查看操作记录列表', '2022-05-24 18:08:26.0', '2022-05-24 18:23:34.0', 0, 'know_search'),
        (1855, 'Kibana查询', 1879, 1, 2, 'Kibana查询', '2022-05-24 18:08:26.0', '2022-09-05 14:24:00.0', 0, 'know_search'),
        (1857, 'SQL查询', 1881, 1, 2, 'SQL查询', '2022-05-24 18:08:26.0', '2022-09-05 14:24:00.0', 0, 'know_search'),
        (1859, '批量修改限流值', 1759, 1, 2, '批量修改限流值', '2022-05-24 18:08:26.0', '2022-08-11 10:37:13.0', 0, 'know_search'),
        (1861, '禁用', 1759, 1, 2, '禁用', '2022-05-24 18:08:26.0', '2022-08-11 10:37:13.0', 0, 'know_search'),
        (1863, '修改限流值', 1759, 1, 2, '修改限流值', '2022-05-24 18:08:26.0', '2022-08-11 10:37:13.0', 0, 'know_search'),
        (1865, '查看异常查询列表', 1611, 1, 2, '查看异常查询列表', '2022-05-24 18:08:26.0', '2022-06-14 16:44:02.0', 0, 'know_search'),
        (1867, '查看慢查询列表', 1611, 1, 2, '查看慢查询列表', '2022-05-24 18:08:26.0', '2022-06-14 16:44:21.0', 0, 'know_search'),
        (1869, '新增角色', 1629, 1, 2, '新增角色', '2022-05-24 18:08:26.0', '2022-05-24 18:23:34.0', 0, 'know_search'),
        (1871, 'Dashboard', 0, 0, 1, '查看dashboard', '2022-05-24 18:08:26.0', '2022-08-27 17:35:50.0', 0, 'know_search'),
        (1873, '新建索引', 1605, 1, 2, '新建索引', '2022-05-24 18:08:26.0', '2022-05-24 18:23:34.0', 0, 'know_search'),
        (1875, '查看dashboard', 1871, 1, 2, '查看dashboard', '2022-05-24 18:08:24.0', '2022-08-27 17:35:50.0', 0, 'know_search'),
        (1877, 'DSL', 0, 0, 1, 'DSL', '2022-05-24 18:08:24.0', '2022-09-02 19:01:17.0', 0, 'know_search'),
        (1879, 'Kibana', 0, 0, 1, 'Kibana', '2022-05-24 18:08:26.0', '2022-09-02 19:01:17.0', 0, 'know_search'),
        (1881, 'SQL', 0, 0, 1, 'SQL', '2022-05-24 18:08:26.0', '2022-09-02 19:01:17.0', 0, 'know_search');

alter table kf_security_oplog
    modify target varchar(225) not null comment '操作对象';

INSERT INTO `kf_security_permission` (`permission_name`, `parent_id`, `leaf`, `level`, `description`, `create_time`, `update_time`, `is_delete`, `app_name`) VALUES ('Grafana', 0, 0, 1, 'Grafana', '2022-05-24 18:08:26', '2022-12-22 15:16:17', 0, 'know_search');
INSERT INTO `kf_security_role_permission` (`role_id`, `permission_id`, `create_time`, `update_time`, `is_delete`, `app_name`) VALUES (1, (select id from kf_security_permission ksp where ksp.permission_name='Grafana' and ksp.app_name='know_search' and ksp.is_delete=0 ), '2022-06-01 21:19:42', '2022-08-25 10:31:42', 0, 'know_search');
INSERT INTO `kf_security_permission` (`permission_name`, `parent_id`, `leaf`, `level`, `description`, `create_time`, `update_time`, `is_delete`, `app_name`) VALUES ('查看Grafana', (select id from kf_security_permission ksp where ksp.permission_name='Grafana' and ksp.app_name='know_search' and ksp.is_delete=0 ), 1, 2, '查看Grafana', '2022-05-24 18:08:26', '2022-12-22 15:16:17', 0, 'know_search');
INSERT INTO `kf_security_role_permission` (`role_id`, `permission_id`, `create_time`, `update_time`, `is_delete`, `app_name`) VALUES (1, (select id from kf_security_permission ksp where ksp.permission_name='查看Grafana' and ksp.app_name='know_search' and ksp.is_delete=0 ), '2022-06-01 21:19:42', '2022-08-25 10:31:42', 0, 'know_search');


ALTER TABLE es_cluster_phy_info
    ADD proxy_address VARCHAR(255) DEFAULT '' NULL COMMENT ' 代理地址 ';

-- 设置supperapp为索引模式
update `arius_es_user` set cluster='' , search_type=1  where id=1;