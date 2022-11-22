#项目、用户、权限点
-- auto-generated definition
create table logi_security_config
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
    on logi_security_config (value_group, value_name);

-- auto-generated definition
create table logi_security_dept
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
create table logi_security_message
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
create table logi_security_oplog
(
    id                int auto_increment
        primary key,
    operator_ip       varchar(20)                            not null comment '操作者 ip',
    operator          varchar(20)                            null comment '操作者账号',
    operate_page      varchar(16)                            null comment '操作页面',
    operate_type      varchar(16)                            not null comment '操作类型',
    target_type       varchar(16)                            not null comment '对象分类',
    target            varchar(20)                            not null comment '操作对象',
    detail            text                                   null comment '日志详情',
    create_time       timestamp    default CURRENT_TIMESTAMP null,
    update_time       timestamp    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete         tinyint(1)   default 0                 not null comment '逻辑删除',
    app_name          varchar(16)                            null comment '应用名称',
    operation_methods varchar(255) default ''                null
)
    comment '操作日志' charset = utf8;

-- auto-generated definition
create table logi_security_oplog_extra
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
create table logi_security_permission
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
create table logi_security_project
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
create table logi_security_resource_type
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
create table logi_security_role
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
create table logi_security_role_permission
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
create table logi_security_user
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
create table logi_security_user_project
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
create table logi_security_user_resource
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
create table logi_security_user_role
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
    `desc`              varchar(500)                 NOT NULL DEFAULT '' COMMENT ' 索引描述 ',
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
insert into logi_security_role_permission (id, role_id, permission_id, create_time, update_time, is_delete, app_name)
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
insert into logi_security_permission (id, permission_name, parent_id, leaf, level, description, create_time, update_time, is_delete, app_name)
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
insert into logi_security_role (id, role_code, role_name, description, last_reviser, create_time, update_time,
                                is_delete, app_name)
values (1, 'r14715628', '管理员', '管理员', 'admin', '2022-06-01 21:19:42.0', '2022-07-06 22:23:59.0', 0,
        'know_search'),
       (2, 'r14481382', '资源 owner', '普通用户拥有的最大权限', 'admin', '2022-06-14 18:08:56.0',
        '2022-07-06 20:36:31.0', 0, 'know_search');
#初始化用户
insert into logi_security_user (id, user_name, pw, salt, real_name, phone, email, dept_id, is_delete,
                                create_time, update_time, app_name)
values (1, 'admin',
        'V1ZkU2RHRlhOSGhOYWs0M1VVWmFjVk5xVW1oaE0zUmlTVEJCZUZGRFRtUm1WVzh5VlcxNGMyRkZRamw3UUZacVNqUmhhM3RiSTBBeVFDTmRmVW8yVW14c2FFQjl7QFZqSjRha3tbI0AzQCNdfUo2UmxsaEB9Mv{#cdRgJ45Lqx}3IubEW87!==',
        '', 'admin', '18888888888', 'admin@12345.com', null, 0, '2022-05-26 05:46:12.0', '2022-08-26 09:06:19.0',
        'know_search');
#初始化用户和角色的关系
insert into logi_security_user_role (id, user_id, role_id, create_time, update_time, is_delete, app_name)
values (1, 1, 2, '2022-08-26 19:54:22.0', '2022-08-26 19:54:22.0', 0, 'know_search'),
       (2, 1, 1, '2022-08-30 21:05:17.0', '2022-08-30 21:05:17.0', 0, 'know_search');
#项目和项目配置、es user 的关系
insert into project_arius_config (project_id, analyze_response_enable, is_source_separated, aggr_analyze_enable,
                                  dsl_analyze_enable, slow_query_times, is_active, memo, create_time, update_time)
values (1, 1, 0, 1, 1, 1000, 1, '超级应用', '2022-06-14 18:52:08.0', '2022-08-27 23:13:14.0'),
       (2, 1, 0, 1, 1, 1000, 1, '元数据模版应用 不可以被删除', '2022-08-25 11:18:45.0', '2022-08-25 11:18:45.0');
insert into logi_security_project (id, project_code, project_name, description, dept_id, running, create_time,
                                   update_time, is_delete, app_name)
values (1, 'p14000143', 'SuperApp', '超级应用', 0, 1, '2022-05-26 05:49:08.0', '2022-08-24 11:09:49.0', 0,
        'know_search'),
       (2, 'p18461793', '元数据模版应用 _ 误删', '元数据模版应用 不可以被删除', 0, 1, '2022-08-25 11:06:04.0',
        '2022-08-25 11:18:45.0', 0, 'know_search');
insert into arius_es_user (id, index_exp, data_center, is_root, memo, ip, verify_code, is_active,
                           query_threshold, cluster, responsible, search_type, create_time, update_time,
                           project_id, is_default_display)
values (1, null, 'cn', 1, '管理员 APP', '', 'azAWiJhxkho33ac', 1, 100, 'logi-elasticsearch-7.6.0', 'admin', 1,
        '2022-05-26 09:35:38.0', '2022-06-23 00:16:47.0', 1, 1),
       (2, null, 'cn', 0, '元数据模版 APP', '', 'vkDgPEfD3jQJ1YY', 1, 1000, '', null, 0, '2022-07-05 08:16:17.0',
        '2022-08-25 21:48:58.0', 2, 0);


## 配置初始化数据
insert into arius_config_info (id, value_group, value_name, value, edit, dimension, status, memo,
                                               create_time, update_time, search_time)
values (187, 'arius.cache.switch', 'logic.template.cache.enable', 'true', 1, -1, -1, '逻辑模板缓存是否开启',
        '2021-09-01 20:37:47.0', '2021-11-29 14:57:47.0', '2021-09-01 20:37:47.0'),
       (189, 'arius.cache.switch', 'physical.template.cache.enable', 'true', 1, -1, -1,
        '获取物理模板列表是否开启全局缓存', '2021-09-01 20:41:22.0', '2021-11-29 14:57:45.0', '2021-09-01 20:41:22.0'),
       (191, 'arius.cache.switch', 'cluster.phy.cache.enable', 'true', 1, -1, -1, '获取物理集群列表是否开启全局缓存',
        '2021-09-01 20:42:31.0', '2021-11-29 14:57:42.0', '2021-09-01 20:42:31.0'),
       (193, 'arius.cache.switch', 'cluster.logic.cache.enable', 'true', 1, -1, -1, '获取逻辑集群列表是否开启全局缓存',
        '2021-09-01 20:43:08.0', '2021-11-29 14:57:39.0', '2021-09-01 20:43:08.0'),
       (1217, 'arius.meta.monitor', 'nodestat.collect.concurrent', 'true', 1, -1, -1, '', '2021-11-18 20:24:54.0',
        '2021-11-19 16:05:39.0', '2021-11-18 20:24:54.0'),
       (1223, 'arius.common.group', 'app.default.read.auth.indices', '""', 1, -1, 2, 'app 可读写的权限 ',
        '2021-12-15 20:17:06.0', '2021-12-16 11:17:26.0', '2021-12-15 20:17:06.0'),
       (1225, 'arius.common.group', 'delete.expire.index.ahead.clusters', '""', 1, -1, 2, ' 删除过期权限 ',
        '2021-12-15 20:17:48.0', '2021-12-16 11:17:24.0', '2021-12-15 20:17:48.0'),
       (1227, 'arius.common.group', 'operate.index.ahead.seconds', '2 * 60 * 60', 1, -1, 2, '索引操作提前时间',
        '2021-12-15 20:18:37.0', '2021-12-16 11:17:22.0', '2021-12-15 20:18:37.0'),
       (1229, 'arius.common.group', 'platform.govern.admin.hot.days', '-1', 1, -1, 2, '平台治理导入热存的天数',
        '2021-12-15 20:19:13.0', '2021-12-16 11:17:19.0', '2021-12-15 20:19:13.0'),
       (1231, 'arius.common.group', 'quota.dynamic.limit.black.appIds', 'none', 1, -1, 2, 'appid 黑名单控制',
        '2021-12-15 20:20:11.0', '2021-12-16 11:17:17.0', '2021-12-15 20:20:11.0'),
       (1233, 'arius.common.group', 'quota.dynamic.limit.black.cluster', '""', 1, -1, 2, 'cluster 黑名单控制 ',
        '2021-12-15 20:20:39.0', '2021-12-16 11:17:15.0', '2021-12-15 20:20:39.0'),
       (1235, 'arius.common.group', 'quota.dynamic.limit.black.logicId', 'none', 1, -1, 2, '模板黑名单控制',
        '2021-12-15 20:21:21.0', '2021-12-16 11:17:12.0', '2021-12-15 20:21:21.0'),
       (1237, 'arius.common.group', 'arius.wo.auto.process.create.template.disk.maxG', '10.0', 1, -1, 2,
        '模板创建时设置的磁盘空间最大值', '2021-12-15 20:21:49.0', '2021-12-16 11:15:12.0', '2021-12-15 20:21:49.0'),
       (1239, 'arius.common.group', 'request.interceptor.switch.open', 'true', 1, -1, 2, '请求拦截开关',
        '2021-12-15 20:22:14.0', '2021-12-16 11:15:10.0', '2021-12-15 20:22:14.0'),
       (1241, 'arius.common.group', 'arius.didi.t2.leader.mail', '""', 1, -1, 2, 'didi 领导者邮箱 ',
        '2021-12-15 20:22:40.0', '2021-12-16 11:15:07.0', '2021-12-15 20:22:40.0'),
       (1243, 'arius.common.group', 'defaultDay', '""', 1, -1, 2, ' 默认 hotDay 值 ', '2021-12-15 20:23:17.0',
        '2021-12-16 11:15:04.0', '2021-12-15 20:23:17.0'),
       (1245, 'arius.quota.config.group', 'arius.quota.config.tps.per.cpu.with.replica', '1000.0', 1, -1, 2,
        '资源管控 cpu 项', '2021-12-15 20:23:56.0', '2021-12-16 11:15:01.0', '2021-12-15 20:23:56.0'),
       (1247, 'arius.quota.config.group', 'arius.quota.config.tps.per.cpu.NO.replica', '2300.0', 1, -1, 2,
        '资源管控 cpu 项', '2021-12-15 20:24:27.0', '2021-12-16 11:14:58.0', '2021-12-15 20:24:27.0'),
       (1249, 'arius.quota.config.group', 'arius.quota.config.cost.per.g.per.month', '1.06', 1, -1, 2,
        '资源配置模板费用', '2021-12-15 20:24:59.0', '2021-12-16 11:14:56.0', '2021-12-15 20:24:59.0'),
       (1251, 'arius.meta.monitor.group', 'nodestat.collect.concurrent', 'fasle', 1, -1, 2, '节点状态信息是否并行采集',
        '2021-12-15 20:25:35.0', '2022-08-26 18:10:50.0', '2021-12-15 20:25:35.0'),
       (1253, 'arius.meta.monitor.group', 'indexstat.collect.concurrent', 'fasle', 1, -1, 2, '索引状态信息是否并行采集',
        '2021-12-15 20:26:00.0', '2022-08-26 18:10:45.0', '2021-12-15 20:26:00.0'),
       (1255, 'arius.common.group', 'indices.recovery.ceph_max_bytes_per_sec', '10MB', 1, -1, 2, '单节点分片恢复的速率',
        '2021-12-15 21:33:29.0', '2022-04-08 17:43:14.0', '2021-12-15 21:33:29.0'),
       (1257, 'arius.common.group', 'cluster.routing.allocation.node_concurrent_incoming_recoveries', '2', 1, -1, 2,
        '一个节点上允许多少并发的传入分片还原, 表示为传入还原', '2021-12-16 14:41:51.0', '2021-12-16 14:42:24.0',
        '2021-12-16 14:41:51.0'),
       (1259, 'arius.common', 'cluster.routing.allocation.node_concurrent_outgoing_recoveries', '2', 1, -1, 2,
        '一个节点上允许多少并发的传入分片还原, 传出还原', '2021-12-16 14:42:15.0', '2022-02-22 11:11:48.0',
        '2021-12-16 14:42:15.0'),
       (1585, 'test.test', 'testt', '21', 1, -1, -1, '请忽略 2221', '2022-01-13 14:25:40.0', '2022-01-15 16:27:05.0',
        '2022-01-13 14:25:40.0'),
       (1587, 'zptest', 'test', '<script>alert(1)</script>', 1, -1, -1, 'alert(1)', '2022-01-18 16:14:12.0',
        '2022-01-18 16:15:49.0', '2022-01-18 16:14:12.0'),
       (1589, 'test1ddd', 'dd ddd', 'dssdddd', 1, -1, -1, 'sddsdssd', '2022-01-26 11:39:23.0', '2022-01-26 11:39:42.0',
        '2022-01-26 11:39:23.0'),
       (1591, 'yyftemptest-01s', 'yyftemptest-01d', '', 1, -1, -1, '', '2022-03-01 16:44:12.0', '2022-03-01 16:44:39.0',
        '2022-03-01 16:44:12.0'),
       (1593, 'test1', 's', '', 1, -1, -1, '', '2022-03-07 11:37:39.0', '2022-03-07 11:37:43.0',
        '2022-03-07 11:37:39.0'),
       (1595, 'test1', '22',
        'm1qaz2wsx3edc4rfv5tgb6yhn7ujm1qaz2wsx3edc4rfv5tgb6yhn7ujm1qaz2wsx3edc4rfv5tgb6yhn7ujm1qaz2', 1, -1, -1, '',
        '2022-03-15 11:19:49.0', '2022-03-15 11:20:08.0', '2022-03-15 11:19:49.0'),
       (1597, 'r''r', '22', 'EE', 1, -1, -1, 'EEEE', '2022-03-15 12:19:26.0', '2022-08-01 08:52:43.0',
        '2022-03-15 12:19:26.0'),
       (1599, 'test1', 'testtemp1', 'uu''uuuu', 1, -1, -1, '那你能', '2022-03-29 15:30:59.0', '2022-03-29 15:31:26.0',
        '2022-03-29 15:30:59.0'),
       (1601, 'yyyYF223', 'WEFWfwef', '', 1, -1, -1, '', '2022-03-31 19:31:01.0', '2022-08-01 08:52:47.0',
        '2022-03-31 19:31:01.0'),
       (1603, 'yyftestYY0411-01', 'yyftestYY0411-01', 'sdsd', 1, -1, -1, 'sdsdcsg 参赛暗杀诉法设计风格！@',
        '2022-04-11 15:22:22.0', '2022-04-11 15:26:29.0', '2022-04-11 15:22:22.0'),
       (1607, 'arius.dashboard.threshold.group', 'index.segment.num_threshold',
        '{"name":" 索引 Segments 个数 ","metrics":"segmentNum","unit":" 个 ","compare":">","value":100}', 1, -1, 1,
        '索引 Segment 个数阈值定义', '2022-06-17 09:52:11.0', '2022-08-27 16:05:06.0', '2022-06-17 09:52:11.0'),
       (1609, 'arius.dashboard.threshold.group', 'index.template.segment_num_threshold',
        '{"name":" 模板 Segments 个数 ","metrics":"segmentNum","unit":" 个 ","compare":">","value":700}', 1, -1, 1,
        '索引模板 [Segment 个数阈值] 定义', '2022-06-17 09:53:34.0', '2022-08-27 19:01:57.0', '2022-06-17 09:53:34.0'),
       (1611, 'arius.dashboard.threshold.group', 'index.segment.memory_size_threshold',
        '{"name":" 索引 Segments 内存大小 ","metrics":"segmentMemSize","unit":"MB","compare":">","value":50}', 1, -1, 1,
        '索引 [Segment 内存大小阈值] 定义', '2022-06-17 09:54:20.0', '2022-08-27 22:24:52.0', '2022-06-17 09:54:20.0'),
       (1613, 'arius.dashboard.threshold.group', 'index.template.segment_memory_size_threshold',
        '{"name":" 模板 Segments 内存大小 ","metrics":"segmentMemSize","unit":"MB","compare":">","value":100}', 1, -1,
        1, '索引模板 [Segment 内存大小阈值] 定义', '2022-06-17 09:54:50.0', '2022-08-27 19:18:54.0',
        '2022-06-17 09:54:50.0'),
       (1617, 'arius.dashboard.threshold.group', 'node.shard.num_threshold',
        '{"name":" 节点分片个数 ","metrics":"shardNum","unit":" 个 ","compare":">","value":1000}', 1, -1, 1,
        '节点 [分片个数阈值] 定义', '2022-06-17 10:01:40.0', '2022-08-27 19:09:44.0', '2022-06-17 10:01:40.0'),
       (1619, 'arius.dashboard.threshold.group', 'index.shard.small_threshold',
        '{"name":" 小 shard 索引列表 ","metrics":"shardSize","unit":"MB","compare":"<","value":1000}', 1, -1, 1,
        '索引 [小 Shard 阈值] 定义', '2022-06-17 16:11:53.0', '2022-08-27 19:04:19.0', '2022-06-17 16:11:53.0'),
       (1623, 'settingGroup', 'name', 'value', 1, -1, -1, 'test', '2022-06-23 14:17:56.0', '2022-06-23 15:47:26.0',
        '2022-06-23 14:17:56.0'),
       (1625, 'group11', 'name1', 'value1', 1, -1, -1, 'des-edit', '2022-06-23 15:22:51.0', '2022-06-24 09:40:51.0',
        '2022-06-23 15:22:51.0'),
       (1627, 'arius.common.group', 'cluster.node.specification_list', '16c-64g-3072g,16c-48g-3071g,1c-48g-3071g,', 1,
        -1, 1, '节点规格列表，机型列表', '2022-07-05 14:10:27.0', '2022-07-18 15:01:29.0', '2022-07-05 14:10:27.0'),
       (1629, 'ccccccccccccccdcdccccccccccccccdcdccccccccccccccdb', 'dccccccccccccccdcdcccccccc', 'vjh', 1, -1, -1,
        'cdcdccccccccccccccdcdccccccccccccccdcdccccccccccccccdcdccccccccccccccdcdccccccccccccccdcdccccc',
        '2022-07-05 15:27:38.0', '2022-07-05 15:28:09.0', '2022-07-05 15:27:38.0'),
       (1631, '2', '3', '', 1, -1, -1, '', '2022-07-06 15:26:45.0', '2022-07-06 15:26:58.0', '2022-07-06 15:26:45.0'),
       (1633, 'arius.common.group', 'cluster.data.center_list', 'cn,en', 1, -1, 1, '数据中心列表',
        '2022-07-06 16:14:03.0', '2022-08-27 19:11:25.0', '2022-07-06 16:14:03.0'),
       (1635, 'arius.common.group', 'cluster.package.version_list', '7.6.1.1,6.6.6.6,7.6.1.2', 1, -1, 1,
        '系统预制支持的版本', '2022-07-06 16:17:25.0', '2022-07-06 16:17:25.0', '2022-07-06 16:17:25.0'),
       (1637, 'template.time.type', 'format', '[
  "yyyy-MM-dd HH:mm:ss",
  "yyyy-MM-dd HH:mm:ss.SSS",
  "yyyy-MM-dd''T''HH:mm:ss",
  "yyyy-MM-dd''T''HH:mm:ss.SSS",
  "yyyy-MM-dd HH:mm:ss.SSS Z",
  "yyyy/MM/dd HH:mm:ss",
  "epoch_seconds",
  "epoch_millis"
]', 1, -1, 1, ' 新建模版的时间格式 ', '2022-07-07 16:15:37.0', '2022-07-07 16:15:37.0', '2022-07-07 16:15:37.0'),
       (1639, 'arius.cluster.blacklist', 'cluster.phy.name', 'didi-cluster-test', 1, -1, 1,
        '滴滴内部测试环境集群, 禁止任何编辑删除新增操作', '2022-07-07 17:58:02.0', '2022-07-07 18:44:42.0',
        '2022-07-07 17:58:02.0'),
       (1641, 'arius.common.group', 'cluster.resource.type_list', '信创,acs,vmware', 1, -1, 1,
        '所属资源类型列表,IaaS 平台类型列表', '2022-07-07 19:13:13.0', '2022-08-27 19:11:50.0',
        '2022-07-07 19:13:13.0'),
       (1643, '55', '666', '1', 1, -1, -1, '143', '2022-07-13 16:59:41.0', '2022-07-13 17:01:48.0',
        '2022-07-13 16:59:41.0'),
       (1645, 'arius.common.group', 'index.rollover.threshold', '0.0001', 1, -1, 1, '主分片大小达到 1G 后升版本',
        '2022-07-15 21:03:12.0', '2022-07-22 16:53:26.0', '2022-07-15 21:03:12.0'),
       (1647, 'yyftemptest-01', 'yyf', 'sdv', 1, -1, -1, 'sdv', '2022-07-18 15:02:08.0', '2022-07-18 15:02:24.0',
        '2022-07-18 15:02:08.0'),
       (1649, 'arius.common.group', 'cluster.node.count_list', '2,4,6,10', 1, -1, 1, '集群节点个数列表',
        '2022-07-18 15:22:33.0', '2022-08-27 19:13:09.0', '2022-07-18 15:22:33.0'),
       (1651, '调度 yyfceshi', '是对的 s''d''c''d 测试', '等待的', 1, -1, -1, '的士速递', '2022-07-20 17:06:43.0',
        '2022-07-20 17:10:39.0', '2022-07-20 17:06:43.0'),
       (1653, 'arius.common.group', 'arius.system.template', '[
    "arius.dsl.analyze.result",
    "arius.dsl.metrics",
    "arius.dsl.template",
    "arius.gateway.join",
    "arius_stats_index_info",
    "arius_stats_node_info",
    "arius.template.access",
    "arius_cat_index_info",
    "arius_gateway_metrics",
    "arius_stats_cluster_info",
    "arius_stats_cluster_task_info",
    "arius_stats_dashboard_info",
    "arius.appid.template.access"
]', 1, -1, 1, ' 系统核心模版集合 ', '2022-07-21 12:25:48.0', '2022-07-21 12:30:06.0', '2022-07-21 12:25:48.0'),
       (1655, 'ds12', 'sd34', 'sdsddsd', 1, -1, -1, 'ds78', '2022-07-21 17:00:44.0', '2022-08-01 08:52:35.0',
        '2022-07-21 17:00:44.0'),
       (1656, 'arius.dashboard.threshold.group', 'index.mapping.num_threshold',
        '{"name":" 索引 Mapping 个数 ","metrics":"mappingNum","unit":" 个 ","compare":">","value":100}', 1, -1, 1,
        '索引 [Mapping 个数阈值] 定义', '2022-07-28 15:50:59.0', '2022-08-27 18:36:48.0', '2022-07-28 15:50:59.0'),
       (1657, 'arius.common.group', 'cluster.shard.big_threshold', '10', 1, -1, 1,
        '用于设置集群看板中的大 Shard 阈值，单位为 gb，大于这个值就认为是大 shard', '2022-07-28 17:49:59.0',
        '2022-08-26 18:08:56.0', '2022-07-28 17:49:59.0'),
       (1659, 'arius.dashboard.threshold.group', 'cluster.shard.num_threshold',
        '{"name":" 集群 shard 个数 ","metrics":"shardNum","unit":" 个 ","compare":">","value":2000}', 1, -1, 1,
        '集群 [Shard 个数阈值] 定义', '2022-08-05 15:58:22.0', '2022-08-27 18:31:22.0', '2022-08-05 15:58:22.0'),
       (1661, 'arius.dashboard.threshold.group', 'cluster.metric.collector.delayed_threshold',
        '{"name":"node_status 指标采集延时 ","metrics":"clusterElapsedTimeGte5Min","unit":"m","compare":">","value":5}',
        1, -1, 1, '集群 [指标采集延时阈值] 定义', '2022-08-10 14:10:47.0', '2022-08-27 18:28:59.0',
        '2022-08-10 14:10:47.0'),
       (1663, 'arius.dashboard.threshold.group', 'node.disk.used_percent_threshold',
        '{"name":" 磁盘利用率 ","metrics":"largeDiskUsage","unit":"%","compare":">","value":40}', 1, -1, 1,
        '节点 [磁盘利用率阈值] 定义', '2022-08-25 14:50:41.0', '2022-08-27 19:20:14.0', '2022-08-25 14:50:41.0'),
       (1665, 'arius.dashboard.threshold.group', 'node.jvm.heap.used_percent_threshold',
        '{"name":" 堆内存利用率 ","metrics":"largeHead","unit":"%","compare":">","value":35}', 1, -1, 1,
        '节点 [堆内存利用率阈值] 定义', '2022-08-25 16:45:33.0', '2022-08-27 19:20:20.0', '2022-08-25 16:45:33.0'),
       (1666, 'arius.dashboard.threshold.group', 'node.cpu.used_percent_threshold',
        '{"name":"CPU 利用率红线 ","metrics":"largeCpuUsage","unit":"%","compare":">","value":40}', 1, -1, 1,
        '节点 [CPU 利用率阈值] 定义', '2022-08-25 16:45:33.0', '2022-08-27 19:20:08.0', '2022-08-25 16:45:33.0'),
       (1667, 'arius.dashboard.threshold.group', 'node.jvm.heap.used_percent_time_duration_threshold',
        '{"name":"node.jvm.heap.used_percent_threshold_time_duration","metrics":"jvmHeapUsedPercentThresholdTimeDuration","unit":"m","compare":">","value":10}',
        1, -1, 1, '节点堆内存利用率阈值的 [持续时间]', '2022-08-25 16:45:33.0', '2022-08-27 15:44:06.0',
        '2022-08-25 16:45:33.0'),
       (1668, 'arius.dashboard.threshold.group', 'node.cpu.used_percent_threshold_time_duration_threshold',
        '{"name":"node.large.cpu.used.percent.time.threshold","metrics":"largeCpuUsage","unit":"s","compare":">","value":60}',
        1, -1, 1, '节点 CPU 利用率超阈值的 [持续时间]', '2022-08-25 16:45:33.0', '2022-08-27 19:28:08.0',
        '2022-08-25 16:45:33.0'),
       (1669, 'arius.dashboard.threshold.group', 'index.shard.big_threshold',
        '{"name":"index.shard.big_threshold","metrics":"shardSize","unit":"G","compare":">","value":20}', 1, -1, 1,
        '索引 [大 shard 阈值] 定义', '2022-08-26 15:25:07.0', '2022-08-29 10:28:24.0', '2022-08-26 15:25:07.0'),
       (1671, 'arius.template.group', 'logic.template.business_type',
        '[{"code":0,"desc":"系统日志","label":"system"},{"code":1,"desc":"日志数据","label":"log"},{"code":2,"desc":"用户上报数据","label":"olap"},{"code":3,"desc":"RDS数据","label":"binlog"},{"code":4,"desc":"离线导入数据","label":"offline"}]', 1, -1, 1, '模板业务类型',
        '2022-08-26 18:02:47.0', '2022-08-27 19:14:56.0', '2022-08-26 18:02:47.0'),
       (1673, 'arius.common.group', 'logic.template.time_format_list',
        'yyyy-MM-dd HH:mm:ss,yyyy-MM-dd HH:mm:ss.SSS,yyyy-MM-dd HH:mm:ss.SSS Z,yyyy-MM-dd''T''HH:mm:ss,yyyy-MM-dd''T''HH:mm:ss.SSS,yyyy-MM-dd''T''HH:mm:ssZ,yyyy-MM-dd''T''HH:mm:ss.SSSZ,yyyy/MM/dd HH:mm:ss,epoch_second,epoch_millis,yyyy-MM-dd',
        1, -1, 1, '模板时间格式列表', '2022-08-26 18:06:07.0', '2022-08-27 19:14:06.0', '2022-08-26 18:06:07.0'),
       (1675, 'arius.common.group', 'history.template.physic.indices.allocation.is_effective', 'ture', 1, -1, 1,
        '历史索引模板 shard 分配是否自动调整', '2022-08-26 18:07:53.0', '2022-08-26 18:07:53.0',
        '2022-08-26 18:07:53.0');

####
insert into es_package (id, url, es_version, creator, `release`, manifest, `desc`, create_time, update_time, delete_flag)
values  (1, 'registry.xiaojukeji.com/didibuild/elasticsearch-image.hnb-pre-v.arius.data-online.fd.didi.com.centos72:9721f7f4', '7.6.1.302', 'linyunan', 0, '3', '', '2021-03-30 20:35:03.0', '2021-10-11 16:07:51.0', 1),
        (3, 'registry.xiaojukeji.com/didibuild/elasticsearch-image.hnb-pre-v.arius.data-online.fd.didi.com.centos72:06b79e62', '7.6.0.1203', 'linyunan', 0, '3', '', '2021-04-01 14:56:42.0', '2021-04-01 14:56:42.0', 0),
        (15, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/elasticsearch.tar.gz', '7.6.0.1401', 'linyunan', 0, '4', '', '2021-06-21 14:56:09.0', '2021-09-14 15:33:36.0', 0),
        (17, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/elasticsearch.tar.gz', '7.6.0.1402', 'admin', 0, '4', 'fdsafsd2', '2021-06-21 14:57:53.0', '2021-10-27 10:34:09.0', 0),
        (23, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/7.6.0.13%404', '7.6.0.13', 'admin', 0, '4', 'ss', '2021-10-28 12:28:07.0', '2021-10-28 12:30:24.0', 1),
        (27, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/234.2.3%404', '234.2.3', 'admin', 0, '4', 'dd', '2021-10-28 19:47:08.0', '2021-10-28 19:55:01.0', 1),
        (29, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/234.2.4%404.tar.gz', '234.2.4', 'admin', 0, '4', 'dd', '2021-10-28 19:54:38.0', '2021-11-23 14:36:35.0', 1),
        (31, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/2.1.2.1%404.tar.gz', '2.1.2.1', 'admin', 0, '4', 'yyf测试', '2021-11-01 10:41:20.0', '2021-11-01 17:13:38.0', 1),
        (33, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/2.12.3.4%404.tar.gz', '2.12.3.4', 'admin', 0, '4', 'ceshi ', '2021-11-01 17:19:04.0', '2021-11-02 11:15:43.0', 1),
        (35, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/2.3.4.5%404.tar.gz', '2.3.4.5', 'admin', 0, '4', 'ceshi ', '2021-11-01 17:19:59.0', '2021-11-02 11:15:41.0', 1),
        (37, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/4.1.2.2%404.tar.gz', '4.1.2.2', 'admin', 0, '4', '测试沃尔特与会计银行股份大晚上是的法规环境股份的地方VG东风股份更好地发挥过', '2021-11-02 10:56:31.0', '2021-11-02 11:15:38.0', 1),
        (39, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/3.3.3.3%404.tar.gz', '3.3.3.3', 'admin', 0, '4', '测试', '2021-11-02 11:14:49.0', '2021-11-02 11:15:35.0', 1),
        (41, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/1.1.1.2%404.tar.gz', '1.1.1.2', 'admin', 0, '4', '测试测试', '2021-11-08 18:27:02.0', '2021-11-08 18:29:20.0', 1),
        (43, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/8.8.8.8%404.tar.gz', '8.8.8.8', 'admin', 0, '4', '测试请忽略、', '2021-11-17 14:47:20.0', '2021-11-23 14:36:33.0', 1),
        (45, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/7.7.7.7%404.tar.gz', '7.7.7.7', 'admin', 0, '4', '测试', '2021-11-18 14:48:38.0', '2021-11-23 14:36:30.0', 1),
        (47, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/1.2.3.4%404.tar.gz', '1.2.3.4', 'admin', 0, '4', '', '2021-11-19 19:08:06.0', '2021-11-23 14:36:27.0', 1),
        (49, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/1.9.89.1%404.tar.gz', '1.9.89.1', 'admin', 0, '4', '', '2021-11-19 19:08:55.0', '2021-11-23 14:36:23.0', 1),
        (51, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/2.2.2.2%404.tar.gz', '2.2.2.3', 'admin', 0, '4', '测试测试测试', '2021-11-25 18:00:47.0', '2021-11-25 20:21:44.0', 1),
        (53, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/2.2.2.5-4.tar.gz', '2.2.2.5', 'admin', 0, '4', null, '2021-11-25 20:16:51.0', '2021-11-25 20:17:23.0', 1),
        (55, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/6.6.6.6-4.tar.gz', '6.6.6.6', 'admin', 0, '4', '测试', '2021-11-26 10:39:19.0', '2021-11-26 10:39:45.0', 1),
        (57, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/6.6.6.6-4.tar.gz', '6.6.6.5', 'admin', 0, '4', '测试请忽略人', '2021-12-21 10:49:03.0', '2021-12-21 10:49:38.0', 0),
        (89, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/7.10.2.0-4.tar.gz', '7.10.2.0', 'admin', 0, '4', 'test', '2021-12-25 11:56:11.0', '2022-06-22 14:38:24.0', 0),
        (203, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/6.6.1.0-4.tar.gz', '6.6.1.0', 'admin', 0, '4', 'ssd', '2022-01-10 19:14:54.0', '2022-06-13 17:32:19.0', 1),
        (205, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/6.6.2.0-4.tar.gz', '6.6.2.0', 'admin', 0, '4', 'ss', '2022-01-10 20:23:07.0', '2022-01-10 20:23:07.0', 0),
        (211, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/2.3.3.5-4.tar.gz', '2.3.3.5', 'admin', 0, '4', 'ss', '2022-01-28 15:59:57.0', '2022-01-28 16:56:42.0', 1),
        (213, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/2.3.3.6-4.tar.gz', '2.3.3.6', 'admin', 0, '4', 'dd', '2022-01-28 16:58:46.0', '2022-01-28 16:58:46.0', 0),
        (215, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/1.1.1.1-4.tar.gz', '1.1.1.1', 'admin', 0, '4', '测试请忽略，使用完毕，QA会删除', '2022-02-18 10:49:07.0', '2022-03-23 19:55:04.0', 1),
        (217, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/2.2.2.2-4.tar.gz', '2.2.2.2', 'admin', 0, '4', '测试请忽略，使用完毕，QA会删除', '2022-02-18 10:49:37.0', '2022-05-17 17:38:47.0', 1),
        (219, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/3.3.3.3-4.tar.gz', '3.3.3.3', 'admin', 0, '4', '测试请忽略，使用完毕，QA会删除哦f', '2022-02-18 10:50:03.0', '2022-03-29 14:32:32.0', 1),
        (221, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/4.4.4.4-4.tar.gz', '4.4.4.4', 'admin', 0, '4', '测试', '2022-03-01 11:32:58.0', '2022-03-01 11:33:05.0', 1),
        (223, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/7.7.7.7-4.tar.gz', '7.7.7.7', 'admin', 0, '4', 'ceshi ', '2022-03-01 17:40:56.0', '2022-05-23 10:17:09.0', 1),
        (225, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/99.2.2.2222-4.tar.gz', '99.2.2.2222', 'admin', 0, '4', '测试请忽略', '2022-03-29 14:34:50.0', '2022-03-29 14:47:04.0', 1),
        (227, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/10.20.30.40-4.tar.gz', '10.20.30.40', 'admin', 0, '4', '测试请忽略', '2022-03-29 14:57:14.0', '2022-03-29 14:59:26.0', 1),
        (233, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/1.2.3.3333-4.tar.gz', '1.2.3.3333', 'yyfQA_admin', 0, '4', '测试请忽略', '2022-04-11 14:18:18.0', '2022-07-04 17:25:18.0', 0),
        (257, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/elasticsearch.tar.gz', '7.6.2.1', '', 0, '4', 'test', '2021-06-21 14:56:09.0', '2022-06-24 18:16:58.0', 1),
        (259, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/1.2.2.1-4.tar.gz', '1.2.2.1', '', 0, '4', 'test0624', '2022-06-24 18:07:38.0', '2022-06-24 18:16:53.0', 1),
        (261, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/9.9.9.9-4.tar.gz', '9.9.9.9', '', 0, '4', '111', '2022-06-24 18:08:22.0', '2022-06-24 18:16:50.0', 1),
        (263, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/1.1.1.3-4.tar.gz', '1.1.1.4', 'yyfQA_admin', 0, '4', 'test111服务范围框架发改委开复工文件和管控', '2022-06-24 18:17:43.0', '2022-07-05 10:10:46.0', 0),
        (265, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/9.9.9.9-4.tar.gz', '9.9.9.9', 'admin', 0, '4', 'test', '2022-06-27 10:11:50.0', '2022-08-07 10:45:40.0', 1),
        (267, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/7.6.2.1-4.tar.gz', '7.6.2.1', 'admin', 0, '4', 'test1', '2022-06-27 18:26:06.0', '2022-07-07 12:13:01.0', 0),
        (281, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/7.6.0.1403-4.tar.gz', '7.6.0.1403', 'admin', 0, '4', '7.6.0.1401', '2022-07-07 16:15:42.0', '2022-07-07 16:15:42.0', 0),
        (283, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/5.6.2.0-4.tar.gz', '5.6.2.0', 'yyfQA_admin', 0, '4', '开源版本', '2022-07-12 10:58:51.0', '2022-07-26 15:35:53.0', 1),
        (285, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/6.6.1.0-4.tar.gz', '6.6.1.0', 'yyfQA_admin', 0, '4', '开源版本', '2022-07-12 10:59:32.0', '2022-07-12 10:59:32.0', 0),
        (287, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/8.0.1.0-4.tar.gz', '8.0.1.0', 'admin', 0, '4', '开源版本', '2022-07-13 16:51:07.0', '2022-08-08 18:08:44.0', 0),
        (289, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/1.2.3.4-4.tar.gz', '1.2.3.4', 'admin', 0, '4', '的', '2022-07-21 20:30:03.0', '2022-07-21 20:30:16.0', 1),
        (291, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/1.2.1.3-null.tar.gz', '1.2.1.3', 'admin', 0, '4', '233', '2022-07-26 15:31:14.0', '2022-08-09 11:43:41.0', 1),
        (293, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/2.2.2.6-4.tar.gz', '2.2.2.6', 'admin', 0, '4', '9', '2022-07-28 16:06:11.0', '2022-08-07 10:44:22.0', 1),
        (295, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/1.5.6.1-4.tar.gz', '1.5.6.1', 'admin', 0, '4', '11', '2022-07-28 17:18:50.0', '2022-07-28 17:18:56.0', 1),
        (297, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/1.1.2.4-4.tar.gz', '1.1.2.4', 'admin', 0, '4', '123', '2022-07-28 20:31:10.0', '2022-08-07 10:44:18.0', 1),
        (299, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/8.8.1.8-4.tar.gz', '8.8.1.8', 'admin', 0, '4', '测试删除', '2022-08-08 10:29:18.0', '2022-08-08 10:29:23.0', 1),
        (301, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/7.6.2.0-4.tar.gz', '7.6.2.0', 'admin', 0, '4', '开源版本', '2022-08-08 18:08:30.0', '2022-08-08 18:08:30.0', 0),
        (303, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/6.8.20.0-4.tar.gz', '6.8.20.0', 'admin', 0, '4', '开源版本', '2022-08-08 18:10:12.0', '2022-08-08 18:10:12.0', 0),
        (305, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/5.6.2.0-4.tar.gz', '5.6.2.0', 'admin', 0, '4', '开源版本', '2022-08-08 18:11:17.0', '2022-08-08 18:11:17.0', 0),
        (307, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/2.1.1.2-4.tar.gz', '2.1.1.2', 'admin', 0, '4', 'hhh', '2022-08-08 18:16:44.0', '2022-08-08 18:16:51.0', 1),
        (309, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/1.2.3.1-4.tar.gz', '1.2.3.1', 'admin', 0, '4', 'lll', '2022-08-09 10:33:14.0', '2022-08-09 10:35:06.0', 1),
        (311, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/1.2.1.3-4.tar.gz', '1.2.1.3', 'admin', 0, '4', '123', '2022-08-09 11:44:32.0', '2022-08-09 11:45:03.0', 1),
        (313, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/1.2.1.3-4.tar.gz', '7.6.1.2', 'admin', 0, '4', '123123', '2022-08-09 11:50:30.0', '2022-08-25 20:29:25.0', 0),
        (317, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/4.3.2.2-4.tar.gz', '4.3.2.2', 'admin', 0, '4', 'temp特色图等待', '2022-08-16 11:12:39.0', '2022-08-16 11:14:00.0', 1),
        (319, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/6.6.6.3-4.tar.gz', '6.6.6.3', 'admin', 0, '4', '测试', '2022-08-25 21:45:02.0', '2022-08-25 21:45:17.0', 1),
        (321, 'https://s3-gzpu-inter.didistatic.com/logi-data-es/1.2.3.1-4.tar.gz', '6.6.6.6', 'admin', 0, '4', '1', '2022-08-26 15:47:48.0', '2022-08-27 14:55:35.0', 0);