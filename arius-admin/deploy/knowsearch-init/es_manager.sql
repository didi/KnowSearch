create table if not exists `arius_config_info`
(
    `id`          bigint(20) UNSIGNED                                      NOT NULL AUTO_INCREMENT COMMENT '主键自增',
    `value_group` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT '配置项组',
    `value_name`  varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT '配置项名字',
    `value`       text CHARACTER SET utf8 COLLATE utf8_general_ci          NULL COMMENT '配置项的值',
    `edit`        int(4)                                                   NOT NULL DEFAULT 1 COMMENT '是否可以编辑 1 不可编辑（程序获取） 2 可编辑',
    `dimension`   int(4)                                                   NOT NULL DEFAULT -1 COMMENT '配置项维度 1 集群 2 模板 ',
    `status`      int(4)                                                   NOT NULL DEFAULT 1 COMMENT '1 正常 2 禁用 -1 删除',
    `memo`        varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '备注',
    `create_time` timestamp                                                NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp                                                NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `search_time` timestamp                                                NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '配置查询时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_group_name` (`value_group`, `value_name`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1662
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = 'arius配置项'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for arius_es_user
-- ----------------------------
create table if not exists `arius_es_user`
(
    `id`                 bigint(10) UNSIGNED                                      NOT NULL AUTO_INCREMENT COMMENT '主键 自增',
    `index_exp`          text CHARACTER SET utf8 COLLATE utf8_general_ci          NULL COMMENT '索引表达式',
    `data_center`        varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci   NOT NULL DEFAULT '' COMMENT '数据中心',
    `is_root`            tinyint(4)                                               NOT NULL DEFAULT 0 COMMENT '是都是超级用户 超级用户具有所有索引的访问权限 0 不是 1是',
    `memo`               varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '备注',
    `ip`                 varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT '白名单ip地址',
    `verify_code`        varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci   NOT NULL DEFAULT '' COMMENT 'app验证码',
    `is_active`          tinyint(2)                                               NOT NULL DEFAULT 1 COMMENT '1为可用，0不可用',
    `query_threshold`    int(10)                                                  NOT NULL DEFAULT 100 COMMENT '限流值',
    `cluster`            varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT '查询集群',
    `responsible`        varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL     DEFAULT '' COMMENT '责任人',
    `search_type`        tinyint(4)                                               NOT NULL DEFAULT 0 COMMENT '0表示app的查询请求需要app里配置的集群(一般配置的都是trib集群) 1表示app的查询请求必须只能访问一个模板 2表示集群模式（可支持多模板查询）',
    `create_time`        timestamp                                                NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`        timestamp                                                NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `project_id`         bigint(10)                                               NOT NULL COMMENT '项目id',
    `is_default_display` tinyint(2)                                               NOT NULL DEFAULT 0 COMMENT '1：项目默认的es user；0:项目新增的es user',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 314
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = 'es操作用户表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for arius_meta_job_cluster_distribute
-- ----------------------------
create table if not exists `arius_meta_job_cluster_distribute`
(
    `id`           bigint(20) UNSIGNED                                     NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `cluster_id`   int(11)                                                 NOT NULL DEFAULT -1 COMMENT '集群id',
    `monitor_host` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '当前执行主机名',
    `monitor_time` timestamp                                               NOT NULL DEFAULT '2000-01-02 00:00:00' COMMENT '上一次监控时间',
    `gmt_create`   timestamp                                               NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modify`   timestamp                                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `cluster`      varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '集群名称',
    `dataCentre`   varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT 'cn' COMMENT '集群数据中心',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uniq_cluster_id` (`cluster_id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 433376
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = 'monitor任务分配'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for arius_op_task
-- ----------------------------
create table if not exists `arius_op_task`
(
    `id`           bigint(20) UNSIGNED                                      NOT NULL AUTO_INCREMENT COMMENT 'id主键自增',
    `title`        varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT '标题',
    `business_key` varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '0' COMMENT '业务数据主键',
    `status`       varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci   NOT NULL DEFAULT 'waiting' COMMENT '任务状态：success:成功 failed:失败 running:执行中 waiting:等待cancel:取消 pause:暂停',
    `creator`      varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT '创建人',
    `create_time`  timestamp                                                NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  timestamp                                                NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag`  tinyint(1)                                               NOT NULL DEFAULT 0 COMMENT '标记删除',
    `expand_data`  longtext CHARACTER SET utf8 COLLATE utf8_general_ci      NULL COMMENT '扩展数据',
    `task_type`    int(11)                                                  NOT NULL DEFAULT 0 COMMENT '任务类型1：集群新增，2：集群扩容，3：集群缩容，4：集群重，5：集群升级，6：集群插件操作，10：模版dcdr任务',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 2268
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = 'arius任务表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for arius_work_order_info
-- ----------------------------
create table if not exists `arius_work_order_info`
(
    `id`                   bigint(20) UNSIGNED                                     NOT NULL AUTO_INCREMENT COMMENT 'id',
    `type`                 varchar(25) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT 'unknown' COMMENT 'appcreate 创建app,clustercreate 创建集群,clusterindecrease 集群扩缩溶,clusteroffline 集群下线,clusterupdate 集群修改,templateauth 索引申请,templatecreate 索引创建,templateindecrease 索引扩容,templatequerydsl 查询语句创建,templatetransfer 索引转让,querydsllimitedit 查询语句编辑,responsiblegovern 员工离职,unhealthytemplategovern 不健康索引处理',
    `title`                varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT '标题',
    `approver_project_id`  int(16)                                                 NOT NULL DEFAULT -1 COMMENT '审批人projectid',
    `applicant`            varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT '申请人',
    `extensions`           text CHARACTER SET utf8 COLLATE utf8_general_ci         NULL COMMENT '拓展字段',
    `description`          text CHARACTER SET utf8 COLLATE utf8_general_ci         NULL COMMENT '备注信息',
    `approver`             varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT '审批人',
    `finish_time`          timestamp                                               NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '结束时间',
    `opinion`              varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '审批信息',
    `status`               int(16)                                                 NOT NULL DEFAULT 0 COMMENT '工单状态, 0:待审批, 1:通过, 2:拒绝, 3:取消',
    `create_time`          timestamp                                               NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`          timestamp                                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修\n改时间',
    `applicant_project_id` int(16)                                                 NOT NULL DEFAULT -1 COMMENT '申请人projectid',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 2460
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '工单表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for es_cluster_phy_info
-- ----------------------------
create table if not exists `es_cluster_phy_info`
(
    `id`                      bigint(20) UNSIGNED                                      NOT NULL AUTO_INCREMENT COMMENT '主键自增',
    `cluster`                 varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT 'es集群名',
    `read_address`            varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '读地址tcp',
    `write_address`           varchar(2000) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '写地址tcp',
    `http_address`            varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT 'http服务地址',
    `http_write_address`      varchar(8000) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT 'http写地址',
    `desc`                    varchar(2000) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '描述',
    `type`                    tinyint(4)                                               NOT NULL DEFAULT -1 COMMENT '集群类型，3-docker集群，4-host集群',
    `data_center`             varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci   NOT NULL DEFAULT 'cn' COMMENT '数据中心',
    `idc`                     varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci   NOT NULL DEFAULT '' COMMENT '机房信息',
    `es_version`              varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT 'es版本',
    `create_time`             timestamp                                                NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`             timestamp                                                NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `level`                   tinyint(4)                                               NOT NULL DEFAULT 1 COMMENT '服务等级',
    `password`                varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT '集群访问密码',
    `ecm_cluster_id`          int(11)                                                  NOT NULL DEFAULT -1 COMMENT 'ecm集群id',
    `cluster_config_template` text CHARACTER SET utf8 COLLATE utf8_general_ci          NULL COMMENT '集群安装模板',
    `image_name`              varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT '镜像名',
    `cfg_id`                  int(11)                                                  NOT NULL DEFAULT -1 COMMENT '配置包id',
    `package_id`              int(11)                                                  NOT NULL DEFAULT -1 COMMENT '程序包id',
    `plug_ids`                varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL     DEFAULT '' COMMENT '插件包id列表',
    `creator`                 varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT '集群创建人',
    `ns_tree`                 varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT '机器节点',
    `template_srvs`           varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL     DEFAULT '' COMMENT '集群的索引模板服务',
    `is_active`               tinyint(4)                                               NOT NULL DEFAULT 1 COMMENT '是否生效',
    `run_mode`                tinyint(255)                                             NOT NULL DEFAULT 0 COMMENT 'client运行模式，0读写共享，1读写分离',
    `write_action`            varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT NULL COMMENT '指定写client的action',
    `health`                  tinyint(2)                                               NOT NULL DEFAULT 3 COMMENT '集群状态 1 green 2 yellow 3 red',
    `active_shard_num`        bigint(25)                                               NOT NULL DEFAULT 0 COMMENT '有效shard总数量',
    `disk_total`              bigint(50)                                               NOT NULL DEFAULT 0 COMMENT '集群磁盘总量 单位byte',
    `disk_usage`              bigint(50)                                               NOT NULL DEFAULT 0 COMMENT '集群磁盘使用量 单位byte',
    `disk_usage_percent`      decimal(10, 5)                                           NOT NULL COMMENT '集群磁盘空闲率 单位 0 ~1',
    `tags`                    text CHARACTER SET utf8 COLLATE utf8_general_ci          NULL COMMENT '拓展字段,这里用于存放集群展示用属性标签，如「集群所属资源类型」等等',
    `platform_type`           varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT 'IaaS平台类型',
    `resource_type`           tinyint(4)                                               NOT NULL DEFAULT -1 COMMENT '集群资源类型，1-共享资源，2-独立资源，3-独享资源',
    `gateway_url`             varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT '集群gateway地址',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_cluster` (`cluster`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 4688
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '物理集群表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for es_cluster_region
-- ----------------------------
create table if not exists `es_cluster_region`
(
    `id`               bigint(20) UNSIGNED                                      NOT NULL AUTO_INCREMENT COMMENT '主键自增',
    `logic_cluster_id` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '-1' COMMENT '逻辑集群ID',
    `phy_cluster_name` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT '物理集群名',
    `racks`            varchar(2048) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT '' COMMENT 'region的rack，逗号分隔',
    `create_time`      timestamp                                                NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      timestamp                                                NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag`      tinyint(1)                                               NOT NULL DEFAULT 0 COMMENT '删除标记，1-已删除，0-未删除',
    `name`             varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT 'region名称',
    `config`           varchar(1024) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT '' COMMENT 'region配置项',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 3996
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = 'es集群region表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for es_cluster_role_host_info
-- ----------------------------
create table if not exists `es_cluster_role_host_info`
(
    `id`              bigint(20) UNSIGNED                                     NOT NULL AUTO_INCREMENT COMMENT 'id主键自增',
    `role_cluster_id` bigint(20) UNSIGNED                                     NOT NULL DEFAULT 0 COMMENT '关联集群角色表外键',
    `hostname`        varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '节点名称',
    `ip`              varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT '主机ip',
    `cluster`         varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT '集群',
    `port`            varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT '端口，如果是节点上启动了多个进程，可以是多个，用逗号隔开',
    `role`            tinyint(4)                                              NOT NULL DEFAULT -1 COMMENT '角色信息， 1data 2client 3master 4tribe',
    `status`          tinyint(4)                                              NOT NULL DEFAULT 1 COMMENT '节点状态，1 在线 2 离线',
    `rack`            varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL     DEFAULT '' COMMENT '节点rack信息',
    `node_set`        varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '节点set信息',
    `create_time`     timestamp                                               NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     timestamp                                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag`     tinyint(1)                                              NOT NULL DEFAULT 0 COMMENT '标记删除',
    `machine_spec`    varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT '',
    `region_id`       bigint(20)                                              NOT NULL DEFAULT -1 COMMENT '节点所属regionId',
    `attributes`      text CHARACTER SET utf8 COLLATE utf8_general_ci         NULL COMMENT 'es节点attributes信息 , 逗号分隔',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uniq_elastic_cluster_id_role_node_set` (`role_cluster_id`, `ip`, `port`, `node_set`) USING BTREE,
    INDEX `idx_cluster` (`cluster`) USING BTREE,
    INDEX `idx_hostname` (`hostname`) USING BTREE,
    INDEX `idx_rack` (`rack`) USING BTREE,
    INDEX `idx_region_id` (`region_id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 2262
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = 'es集群表对应各角色主机列表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for es_cluster_role_info
-- ----------------------------
create table if not exists `es_cluster_role_info`
(
    `id`                 bigint(20) UNSIGNED                                     NOT NULL AUTO_INCREMENT COMMENT 'id主键自增',
    `elastic_cluster_id` bigint(20) UNSIGNED                                     NOT NULL DEFAULT 0 COMMENT 'elastic_cluster外键id',
    `role_cluster_name`  varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT 'role集群名称',
    `role`               varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT '集群角色(masternode/datanode/clientnode)',
    `pod_number`         int(11)                                                 NOT NULL DEFAULT 0 COMMENT 'pod数量',
    `pid_count`          int(11)                                                 NOT NULL DEFAULT 1 COMMENT '单机实例数',
    `machine_spec`       varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT '' COMMENT '机器规格',
    `es_version`         varchar(150) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT 'es版本',
    `cfg_id`             int(11)                                                 NOT NULL DEFAULT -1 COMMENT '配置包id',
    `plug_ids`           varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '插件包id列表',
    `create_time`        timestamp                                               NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`        timestamp                                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag`        tinyint(1)                                              NOT NULL DEFAULT 0 COMMENT '标记删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uniq_elastic_cluster_id_ddcloud_cluster_name` (`elastic_cluster_id`, `role_cluster_name`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1100
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = 'es集群角色表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for es_config
-- ----------------------------
create table if not exists `es_config`
(
    `id`             bigint(20) UNSIGNED                                     NOT NULL AUTO_INCREMENT COMMENT 'id主键自增',
    `cluster_id`     bigint(20)                                              NOT NULL DEFAULT -1 COMMENT '集群id',
    `type_name`      varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '配置文件名称',
    `engin_name`     varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '组件名称',
    `config_data`    longtext CHARACTER SET utf8 COLLATE utf8_general_ci     NULL COMMENT '配置内容',
    `desc`           varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '配置描述',
    `version_tag`    varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '配置tag',
    `version_config` bigint(20)                                              NOT NULL DEFAULT -1 COMMENT '配置版本',
    `selected`       smallint(6)                                             NOT NULL DEFAULT 0 COMMENT '是否在使用',
    `create_time`    timestamp                                               NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    timestamp                                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag`    tinyint(1)                                              NOT NULL DEFAULT 0 COMMENT '标记删除',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1130
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = 'es配置表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for es_machine_norms
-- ----------------------------
create table if not exists `es_machine_norms`
(
    `id`          bigint(20) UNSIGNED                                    NOT NULL AUTO_INCREMENT COMMENT 'id主键自增',
    `role`        varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '角色(masternode/datanode/clientnode/datanode-ceph)',
    `spec`        varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '规格(16-48gi-100g)',
    `create_time` timestamp                                              NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `delete_flag` tinyint(1)                                             NOT NULL DEFAULT 0 COMMENT '标记删除',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 16
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '容器规格列表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for es_package
-- ----------------------------
create table if not exists `es_package`
(
    `id`          bigint(20) UNSIGNED                                     NOT NULL AUTO_INCREMENT COMMENT 'id主键自增',
    `url`         varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '镜像地址或包地址',
    `es_version`  varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '版本标识',
    `creator`     varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '包创建人',
    `release`     tinyint(1)                                              NOT NULL DEFAULT 0 COMMENT '是否为发布版本',
    `manifest`    varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT '类型(3 docker/4 host)',
    `desc`        varchar(384) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT '' COMMENT '备注',
    `create_time` timestamp                                               NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp                                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag` tinyint(1)                                              NOT NULL DEFAULT 0 COMMENT '标记删除 0 未删 1 已删',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 316
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '程序包版本管理'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for es_plugin
-- ----------------------------
create table if not exists `es_plugin`
(
    `id`                 bigint(20) UNSIGNED                                      NOT NULL AUTO_INCREMENT COMMENT 'id主键自增',
    `name`               varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci   NOT NULL DEFAULT '' COMMENT '插件名',
    `physic_cluster_ids` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT '物理集群id',
    `version`            varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci   NOT NULL DEFAULT '' COMMENT '插件版本',
    `url`                varchar(1024) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '插件存储地址',
    `md5`                varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT '插件文件md5',
    `desc`               varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT '插件描述',
    `creator`            varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT '插件创建人',
    `p_default`          tinyint(1)                                               NOT NULL DEFAULT 0 COMMENT '是否为系统默认：0 否 1 是',
    `create_time`        timestamp                                                NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `delete_flag`        tinyint(1)                                               NOT NULL DEFAULT 0 COMMENT '标记删除',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 416
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = 'es插件包管理'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for es_work_order_task
-- ----------------------------
create table if not exists `es_work_order_task`
(
    `id`                bigint(20) UNSIGNED                                     NOT NULL AUTO_INCREMENT COMMENT 'id主键自增',
    `title`             varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '标题',
    `work_order_id`     bigint(20)                                              NOT NULL DEFAULT -1 COMMENT '工单id',
    `physic_cluster_id` bigint(20)                                              NOT NULL DEFAULT -1 COMMENT '物理集群id',
    `cluster_node_role` varchar(512) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '-1' COMMENT '集群节点角色',
    `task_ids`          varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT ' 各角色任务ids',
    `type`              varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT '集群类型:3 docker容器云/ 4 host 物理机',
    `order_type`        varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT '工单类型 1 集群新增 2 集群扩容 3 集群缩容 4 集群重启 5 集群升级',
    `status`            varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT '任务状态',
    `creator`           varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '工单创建人',
    `create_time`       timestamp                                               NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       timestamp                                               NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag`       tinyint(1)                                              NOT NULL DEFAULT 0 COMMENT '标记删除',
    `handle_data`       longtext CHARACTER SET utf8 COLLATE utf8_general_ci     NULL COMMENT '工单数据',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1924
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = 'es工单任务表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for es_work_order_task_detail
-- ----------------------------
create table if not exists `es_work_order_task_detail`
(
    `id`                 bigint(20) UNSIGNED                                     NOT NULL AUTO_INCREMENT COMMENT 'id主键自增',
    `work_order_task_id` bigint(20)                                              NOT NULL DEFAULT -1 COMMENT '工单任务id',
    `role`               varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '所属角色',
    `hostname`           varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '主机名称/ip',
    `grp`                int(11)                                                 NOT NULL DEFAULT 0 COMMENT '机器的分组',
    `idx`                int(11)                                                 NOT NULL DEFAULT 0 COMMENT '机器在分组中的索引',
    `task_id`            bigint(20)                                              NOT NULL DEFAULT -1 COMMENT '容器云/物理机 接口返回任务id',
    `status`             varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT '任务状态',
    `create_time`        timestamp                                               NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`        timestamp                                               NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `delete_flag`        tinyint(1)                                              NOT NULL DEFAULT 0 COMMENT '标记删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uniq_work_order_task_id_role_hostname_delete_flag` (`work_order_task_id`, `role`, `hostname`, `delete_flag`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 6516
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = 'es工单任务详情表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for gateway_cluster_info
-- ----------------------------
create table if not exists `gateway_cluster_info`
(
    `id`           bigint(20) UNSIGNED                                    NOT NULL AUTO_INCREMENT COMMENT '主键 自增',
    `cluster_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '集群名称',
    `create_time`  timestamp                                              NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT ' 创建时间',
    `update_time`  timestamp                                              NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uniq_cluster_name` (`cluster_name`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 122
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = 'gateway集群信息'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for gateway_cluster_node_info
-- ----------------------------
create table if not exists `gateway_cluster_node_info`
(
    `id`             bigint(20) UNSIGNED                                    NOT NULL AUTO_INCREMENT COMMENT '主键 自增',
    `cluster_name`   varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '集群名称',
    `host_name`      varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '主机名',
    `port`           int(10)                                                NOT NULL DEFAULT -1 COMMENT '端口',
    `heartbeat_time` timestamp                                              NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '心跳时间',
    `create_time`    timestamp                                              NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT ' 创建时间',
    `update_time`    timestamp                                              NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uniq_ip_port` (`host_name`, `port`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 355678
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = 'gateway节点信息'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for index_template_alias
-- ----------------------------
create table if not exists `index_template_alias`
(
    `id`                bigint(10) UNSIGNED                                     NOT NULL AUTO_INCREMENT COMMENT '主键 自增',
    `index_template_id` int(10)                                                 NOT NULL DEFAULT -1 COMMENT '索引模板id',
    `name`              varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT '别名',
    `filterterm`        varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '过滤器',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 4
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '索引别名'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for index_template_config
-- ----------------------------
create table if not exists `index_template_config`
(
    `is_source_separated`      tinyint(4)          NOT NULL DEFAULT 0 COMMENT '是否是索引处分分离的 0 不是 1 是',
    `idc_flags`                tinyint(1)          NOT NULL DEFAULT 0 COMMENT 'idc标识',
    `adjust_rack_shard_factor` decimal(10, 2)      NOT NULL DEFAULT 1.00 COMMENT '模板shard的资源消耗因子',
    `mapping_improve_enable`   tinyint(4)          NOT NULL DEFAULT 1 COMMENT 'mapping优化开关 1 开 0 关',
    `pre_create_flags`         tinyint(1)          NOT NULL DEFAULT 1 COMMENT '预创建标识',
    `update_time`              timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `disable_source_flags`     tinyint(1)          NOT NULL DEFAULT 0 COMMENT '禁用source标识',
    `disable_index_rollover`   tinyint(1)          NOT NULL DEFAULT 1 COMMENT '禁用indexRollover功能',
    `dynamic_limit_enable`     tinyint(4)          NOT NULL DEFAULT 1 COMMENT '动态限流开关 1 开 0 关',
    `logic_id`                 int(10)             NOT NULL DEFAULT -1 COMMENT '逻辑模板id',
    `create_time`              timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `shard_num`                int(11)             NOT NULL DEFAULT 1 COMMENT 'shard数量',
    `adjust_rack_tps_factor`   decimal(10, 2)      NOT NULL DEFAULT 1.00 COMMENT '容量规划时，tps的系数 ',
    `id`                       bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_logic_id` (`logic_id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1466
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '模板配置信息'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for index_template_info
-- ----------------------------
create table if not exists `index_template_info`
(
    `id`                bigint(20) UNSIGNED                                     NOT NULL AUTO_INCREMENT COMMENT '主键自增',
    `name`              varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '名称',
    `data_type`         tinyint(4)                                              NOT NULL DEFAULT -1 COMMENT '数据类型',
    `date_format`       varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT '索引分区的时间后缀',
    `is_active`         tinyint(2)                                              NOT NULL DEFAULT 1 COMMENT '有效标记',
    `data_center`       varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT '数据中心',
    `expire_time`       bigint(20)                                              NOT NULL DEFAULT -1 COMMENT '保存时长',
    `hot_time`          int(10)                                                 NOT NULL DEFAULT -1 COMMENT '热数据保存时长',
    `responsible`       varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT '' COMMENT '责任人',
    `date_field`        varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT '时间字段',
    `date_field_format` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '时间字段的格式',
    `id_field`          varchar(512) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT '' COMMENT 'id字段',
    `routing_field`     varchar(512) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT '' COMMENT 'routing字段',
    `expression`        varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '索引表达式',
    `desc`              varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '索引描述',
    `quota`             decimal(10, 3)                                          NOT NULL DEFAULT -1.000 COMMENT '规格',
    `project_id`        int(10)                                                 NOT NULL DEFAULT -1 COMMENT 'project_id',
    `ingest_pipeline`   varchar(512) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT 'ingest_pipeline',
    `block_read`        tinyint(1) UNSIGNED ZEROFILL                            NOT NULL DEFAULT 0 COMMENT '是否禁读，0：否，1：是',
    `block_write`       tinyint(1) UNSIGNED ZEROFILL                            NOT NULL DEFAULT 0 COMMENT '是否禁写，0：否，1：是',
    `create_time`       timestamp                                               NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       timestamp                                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `write_rate_limit`  bigint(255)                                             NOT NULL DEFAULT -1 COMMENT '写入限流值',
    `resource_id`       bigint(20)                                              NOT NULL DEFAULT -1 COMMENT '逻辑集群id',
    `check_point_diff`  bigint(100)                                             NOT NULL DEFAULT 0 COMMENT 'dcdr位点差',
    `level`             tinyint(4)                                              NOT NULL DEFAULT 1 COMMENT '服务等级分为1,2,3',
    `has_dcdr`          tinyint(1) UNSIGNED ZEROFILL                            NOT NULL DEFAULT 0 COMMENT '是否开启dcdr',
    `open_srv`          varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT NULL COMMENT '已开启的模板服务',
    `disk_size`         decimal(10, 3)                                          NULL     DEFAULT -1.000 COMMENT '可用磁盘容量',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_data_center` (`data_center`) USING BTREE,
    INDEX `idx_is_active` (`is_active`) USING BTREE,
    INDEX `idx_name` (`name`) USING BTREE,
    INDEX `idx_project_id` (`project_id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 25882
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '逻辑索引模板表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for index_template_physical_info
-- ----------------------------
create table if not exists `index_template_physical_info`
(
    `id`            bigint(20) UNSIGNED                                     NOT NULL AUTO_INCREMENT COMMENT '主键自增',
    `logic_id`      int(10)                                                 NOT NULL DEFAULT -1 COMMENT '逻辑模板id',
    `name`          varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '模板名字',
    `expression`    varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '表达式',
    `cluster`       varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '集群名字',
    `rack`          varchar(512) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT 'rack',
    `shard`         int(10)                                                 NOT NULL DEFAULT 1 COMMENT 'shard个数',
    `shard_routing` int(10)                                                 NOT NULL DEFAULT 1 COMMENT '内核的逻辑shard',
    `version`       int(10)                                                 NOT NULL DEFAULT 0 COMMENT '版本',
    `role`          tinyint(4)                                              NOT NULL DEFAULT 1 COMMENT '角色 1master 2slave',
    `status`        tinyint(4)                                              NOT NULL DEFAULT 1 COMMENT '1 常规 -1 索引删除中 -2已删除',
    `config`        text CHARACTER SET utf8 COLLATE utf8_general_ci         NULL COMMENT '配置 json格式',
    `create_time`   timestamp                                               NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   timestamp                                               NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `region_id`     int(10)                                                 NOT NULL DEFAULT -1 COMMENT '模板关联的regionId',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_cluster_name_status` (`cluster`, `name`, `status`) USING BTREE,
    INDEX `idx_log_id_statud` (`logic_id`, `status`) USING BTREE,
    INDEX `idx_logic_id` (`logic_id`) USING BTREE,
    INDEX `idx_region_id` (`region_id`) USING BTREE,
    INDEX `idx_status` (`status`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 23542
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '物理模板信息'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for index_template_type
-- ----------------------------
create table if not exists `index_template_type`
(
    `id`                  bigint(10) UNSIGNED                                     NOT NULL AUTO_INCREMENT COMMENT '主键 自增',
    `index_template_id`   int(10)                                                 NOT NULL DEFAULT -1 COMMENT '索引模板id',
    `index_template_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '索引模板名称',
    `name`                varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT 'type名称',
    `id_field`            varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT 'id字段',
    `routing`             varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT 'routing字段',
    `source`              tinyint(4)                                              NOT NULL DEFAULT 1 COMMENT '0 不存source 1存source',
    `is_active`           tinyint(2)                                              NOT NULL DEFAULT 1 COMMENT '是否激活 1是 0否',
    `create_time`         timestamp                                               NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`         timestamp                                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '索引模板type'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for logi_job
-- ----------------------------
create table if not exists `logi_job`
(
    `id`          bigint(20)                                              NOT NULL AUTO_INCREMENT,
    `job_code`    varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT 'task taskCode',
    `task_code`   varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '任务code',
    `class_name`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '类的全限定名',
    `try_times`   int(10)                                                 NOT NULL DEFAULT 0 COMMENT '第几次重试',
    `worker_code` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '执行机器',
    `app_name`    varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '被调度的应用名称',
    `start_time`  datetime                                                NULL     DEFAULT '1971-01-01 00:00:00' COMMENT '开始时间',
    `create_time` datetime                                                NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime                                                NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `job_code` (`job_code`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1686882
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '正在执行的job信息'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for logi_job_log
-- ----------------------------
create table if not exists `logi_job_log`
(
    `id`          bigint(20)                                              NOT NULL AUTO_INCREMENT,
    `job_code`    varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT 'job taskCode',
    `task_code`   varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '任务code',
    `task_name`   varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '任务名称',
    `task_desc`   varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '任务描述',
    `task_id`     bigint(20)                                              NOT NULL DEFAULT 0 COMMENT '任务id',
    `class_name`  varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '类的全限定名',
    `try_times`   int(10)                                                 NOT NULL DEFAULT 0 COMMENT '第几次重试',
    `worker_code` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '执行机器',
    `worker_ip`   varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '执行机器ip',
    `start_time`  datetime                                                NULL     DEFAULT '1971-01-01 00:00:00' COMMENT '开始时间',
    `end_time`    datetime                                                NULL     DEFAULT '1971-01-01 00:00:00' COMMENT '结束时间',
    `status`      tinyint(4)                                              NOT NULL DEFAULT 0 COMMENT '执行结果 1成功 2失败 3取消',
    `error`       text CHARACTER SET utf8 COLLATE utf8_general_ci         NOT NULL COMMENT '错误信息',
    `result`      text CHARACTER SET utf8 COLLATE utf8_general_ci         NOT NULL COMMENT '执行结果',
    `app_name`    varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '被调度的应用名称',
    `create_time` datetime                                                NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime                                                NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_job_code` (`job_code`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1677760
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = 'job执行历史日志'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for logi_security_config
-- ----------------------------
create table if not exists `logi_security_config`
(
    `id`          bigint(20) UNSIGNED                                      NOT NULL AUTO_INCREMENT COMMENT '主键自增',
    `value_group` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT '配置项组',
    `value_name`  varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT '配置项名字',
    `value`       text CHARACTER SET utf8 COLLATE utf8_general_ci          NULL COMMENT '配置项的值',
    `edit`        int(4)                                                   NOT NULL DEFAULT 1 COMMENT '是否可以编辑 1 不可编辑（程序获取） 2 可编辑',
    `status`      int(4)                                                   NOT NULL DEFAULT 1 COMMENT '1 正常 2 禁用',
    `memo`        varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '备注',
    `create_time` timestamp                                                NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp                                                NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `is_delete`   tinyint(1)                                               NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `app_name`    varchar(16) CHARACTER SET utf8 COLLATE utf8_bin          NULL     DEFAULT NULL COMMENT '应用名称',
    `operator`    varchar(16) CHARACTER SET utf8 COLLATE utf8_bin          NULL     DEFAULT NULL COMMENT '操作者',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_group_name` (`value_group`, `value_name`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1592
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = 'logi配置项'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for logi_security_dept
-- ----------------------------
create table if not exists `logi_security_dept`
(
    `id`          int(11)                                                NOT NULL AUTO_INCREMENT,
    `dept_name`   varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '部门名',
    `parent_id`   int(11)                                                NOT NULL COMMENT '父部门id',
    `leaf`        tinyint(1)                                             NOT NULL COMMENT '是否叶子部门',
    `level`       tinyint(4)                                             NOT NULL COMMENT 'parentId为0的层级为1',
    `description` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '描述',
    `create_time` timestamp                                              NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp                                              NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete`   tinyint(1)                                             NULL DEFAULT 0 COMMENT '逻辑删除',
    `app_name`    varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '应用名称',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1592
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '部门信息表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for logi_security_message
-- ----------------------------
create table if not exists `logi_security_message`
(
    `id`          int(11)                                                 NOT NULL AUTO_INCREMENT,
    `title`       varchar(60) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL COMMENT '标题',
    `content`     varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '内容',
    `read_tag`    tinyint(1)                                              NULL DEFAULT 0 COMMENT '是否已读',
    `oplog_id`    int(11)                                                 NULL DEFAULT NULL COMMENT '操作日志id',
    `user_id`     int(11)                                                 NULL DEFAULT NULL COMMENT '这条消息属于哪个用户的，用户id',
    `create_time` timestamp                                               NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp                                               NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete`   tinyint(1)                                              NULL DEFAULT 0 COMMENT '逻辑删除',
    `app_name`    varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL DEFAULT NULL COMMENT '应用名称',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1848
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '消息中心'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for logi_security_oplog
-- ----------------------------
create table if not exists `logi_security_oplog`
(
    `id`                int(11)                                                 NOT NULL AUTO_INCREMENT,
    `operator_ip`       varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL COMMENT '操作者ip',
    `operator`          varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL     DEFAULT NULL COMMENT '操作者账号',
    `operate_page`      varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL     DEFAULT NULL COMMENT '操作页面',
    `operate_type`      varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL COMMENT '操作类型',
    `target_type`       varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL COMMENT '对象分类',
    `target`            varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL COMMENT '操作对象',
    `detail`            text CHARACTER SET utf8 COLLATE utf8_general_ci         NULL COMMENT '日志详情',
    `create_time`       timestamp                                               NULL     DEFAULT CURRENT_TIMESTAMP,
    `update_time`       timestamp                                               NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete`         tinyint(1)                                              NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `app_name`          varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL     DEFAULT NULL COMMENT '应用名称',
    `operation_methods` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT '',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 2762
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '操作日志'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for logi_security_oplog_extra
-- ----------------------------
create table if not exists `logi_security_oplog_extra`
(
    `id`          int(11)                                                NOT NULL AUTO_INCREMENT,
    `info`        varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '信息',
    `type`        tinyint(4)                                             NOT NULL COMMENT '哪种信息：1：操作页面;2：操作类型;3：对象分类',
    `create_time` timestamp                                              NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp                                              NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete`   tinyint(1)                                             NULL DEFAULT 0 COMMENT '逻辑删除',
    `app_name`    varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '应用名称',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1592
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '操作日志信息（操作页面、操作类型、对象分类）'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for logi_security_permission
-- ----------------------------
create table if not exists `logi_security_permission`
(
    `id`              int(11)                                                NOT NULL AUTO_INCREMENT,
    `permission_name` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '权限名字',
    `parent_id`       int(11)                                                NOT NULL COMMENT '父权限id',
    `leaf`            tinyint(1)                                             NOT NULL COMMENT '是否叶子权限点（具体的操作）',
    `level`           tinyint(4)                                             NOT NULL COMMENT '权限点的层级（parentId为0的层级为1）',
    `description`     varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '权限点描述',
    `create_time`     timestamp                                              NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     timestamp                                              NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete`       tinyint(1)                                             NULL DEFAULT 0 COMMENT '逻辑删除',
    `app_name`        varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '应用名称',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1876
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '权限表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for logi_security_project
-- ----------------------------
create table if not exists `logi_security_project`
(
    `id`           int(11)                                                 NOT NULL AUTO_INCREMENT COMMENT '项目id',
    `project_code` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '项目编号',
    `project_name` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '项目名',
    `description`  varchar(512) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '项目描述',
    `dept_id`      int(11)                                                 NOT NULL COMMENT '部门id',
    `running`      tinyint(1)                                              NOT NULL DEFAULT 1 COMMENT '启用 or 停用',
    `create_time`  timestamp                                               NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  timestamp                                               NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete`    tinyint(1)                                              NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `app_name`     varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL     DEFAULT NULL COMMENT '应用名称',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1830
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '项目表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for logi_security_resource_type
-- ----------------------------
create table if not exists `logi_security_resource_type`
(
    `id`          int(11)                                                NOT NULL AUTO_INCREMENT,
    `type_name`   varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT NULL COMMENT '资源类型名',
    `create_time` timestamp                                              NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp                                              NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete`   tinyint(1)                                             NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `app_name`    varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT NULL COMMENT '应用名称',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1592
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '资源类型表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for logi_security_role
-- ----------------------------
create table if not exists `logi_security_role`
(
    `id`           int(11)                                                 NOT NULL AUTO_INCREMENT,
    `role_code`    varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '角色编号',
    `role_name`    varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '名称',
    `description`  varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT NULL COMMENT '角色描述',
    `last_reviser` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL     DEFAULT NULL COMMENT '最后修改人',
    `create_time`  timestamp                                               NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  timestamp                                               NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete`    tinyint(1)                                              NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `app_name`     varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL     DEFAULT NULL COMMENT '应用名称',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1628
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '角色信息'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for logi_security_role_permission
-- ----------------------------
create table if not exists `logi_security_role_permission`
(
    `id`            int(11)                                                NOT NULL AUTO_INCREMENT,
    `role_id`       int(11)                                                NOT NULL COMMENT '角色id',
    `permission_id` int(11)                                                NOT NULL COMMENT '权限id',
    `create_time`   timestamp                                              NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   timestamp                                              NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete`     tinyint(1)                                             NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `app_name`      varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT NULL COMMENT '应用名称',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 5608
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '角色权限表（只保留叶子权限与角色关系）'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for logi_security_user
-- ----------------------------
create table if not exists `logi_security_user`
(
    `id`          int(11)                                                  NOT NULL AUTO_INCREMENT,
    `user_name`   varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci   NOT NULL COMMENT '用户账号',
    `pw`          varchar(2048) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户密码',
    `salt`        char(5) CHARACTER SET utf8 COLLATE utf8_general_ci       NOT NULL DEFAULT '' COMMENT '密码盐',
    `real_name`   varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT '真实姓名',
    `phone`       char(11) CHARACTER SET utf8 COLLATE utf8_general_ci      NOT NULL DEFAULT '' COMMENT 'mobile',
    `email`       varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci   NOT NULL DEFAULT '' COMMENT 'email',
    `dept_id`     int(11)                                                  NULL     DEFAULT NULL COMMENT '所属部门id',
    `is_delete`   tinyint(1)                                               NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time` timestamp                                                NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `update_time` timestamp                                                NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `app_name`    varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci   NULL     DEFAULT NULL COMMENT '应用名称',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1676
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '用户信息'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for logi_security_user_project
-- ----------------------------
create table if not exists `logi_security_user_project`
(
    `id`          int(11)                                                NOT NULL AUTO_INCREMENT,
    `user_id`     int(11)                                                NOT NULL COMMENT '用户id',
    `project_id`  int(11)                                                NOT NULL COMMENT '项目id',
    `create_time` timestamp                                              NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp                                              NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete`   tinyint(1)                                             NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `app_name`    varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT NULL COMMENT '应用名称',
    `user_type`   tinyint(10)                                            NOT NULL DEFAULT 0 COMMENT '用户类型：0：普通项目用户；1：项目owner',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 2632
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '用户项目关系表（项目负责人）'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for logi_security_user_resource
-- ----------------------------
create table if not exists `logi_security_user_resource`
(
    `id`               int(11)                                                NOT NULL AUTO_INCREMENT,
    `user_id`          int(11)                                                NOT NULL COMMENT '用户id',
    `project_id`       int(11)                                                NOT NULL COMMENT '资源所属项目id',
    `resource_type_id` int(11)                                                NOT NULL COMMENT '资源类别id',
    `resource_id`      int(11)                                                NOT NULL COMMENT '资源id',
    `control_level`    tinyint(4)                                             NOT NULL COMMENT '管理级别：1（查看权限）2（管理权限）',
    `create_time`      timestamp                                              NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      timestamp                                              NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete`        tinyint(1)                                             NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `app_name`         varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT NULL COMMENT '应用名称',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1592
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '用户和资源关系表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for logi_security_user_role
-- ----------------------------
create table if not exists `logi_security_user_role`
(
    `id`          int(11)                                                NOT NULL AUTO_INCREMENT,
    `user_id`     int(11)                                                NOT NULL COMMENT '用户id',
    `role_id`     int(11)                                                NOT NULL COMMENT '角色id',
    `create_time` timestamp                                              NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp                                              NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete`   tinyint(1)                                             NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `app_name`    varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT NULL COMMENT '应用名称',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 2404
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '用户角色表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for logi_task
-- ----------------------------
create table if not exists `logi_task`
(
    `id`              bigint(20)                                               NOT NULL AUTO_INCREMENT,
    `task_code`       varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT 'task taskCode',
    `task_name`       varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT '名称',
    `task_desc`       varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '任务描述',
    `cron`            varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT 'cron 表达式',
    `class_name`      varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT '类的全限定名',
    `params`          varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '执行参数 map 形式{key1:value1,key2:value2}',
    `retry_times`     int(10)                                                  NOT NULL DEFAULT 0 COMMENT '允许重试次数',
    `last_fire_time`  datetime                                                 NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '上次执行时间',
    `timeout`         bigint(20)                                               NOT NULL DEFAULT 0 COMMENT '超时 毫秒',
    `status`          tinyint(4)                                               NOT NULL DEFAULT 0 COMMENT '1等待 2运行中 3暂停',
    `sub_task_codes`  varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '子任务code列表,逗号分隔',
    `consensual`      varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT '执行策略',
    `owner`           varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT '责任人',
    `task_worker_str` varchar(3000) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '机器执行信息',
    `app_name`        varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT '被调度的应用名称',
    `create_time`     datetime                                                 NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     datetime                                                 NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `task_code` (`task_code`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 502
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '任务信息'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for logi_task_lock
-- ----------------------------
create table if not exists `logi_task_lock`
(
    `id`          bigint(20)                                              NOT NULL AUTO_INCREMENT,
    `task_code`   varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT 'task taskCode',
    `worker_code` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT 'worker taskCode',
    `app_name`    varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '被调度的应用名称',
    `expire_time` bigint(20)                                              NOT NULL DEFAULT 0 COMMENT '过期时间',
    `create_time` datetime                                                NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime                                                NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uni_task_app` (`task_code`, `app_name`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1332630
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '任务锁'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for logi_worker
-- ----------------------------
create table if not exists `logi_worker`
(
    `id`              bigint(20)                                              NOT NULL AUTO_INCREMENT,
    `worker_code`     varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT 'worker taskCode',
    `worker_name`     varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT 'worker名',
    `ip`              varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT 'worker的ip',
    `cpu`             int(11)                                                 NOT NULL DEFAULT 0 COMMENT 'cpu数量',
    `cpu_used`        double                                                  NOT NULL DEFAULT 0 COMMENT 'cpu使用率',
    `memory`          double                                                  NOT NULL DEFAULT 0 COMMENT '内存,以M为单位',
    `memory_used`     double                                                  NOT NULL DEFAULT 0 COMMENT '内存使用率',
    `jvm_memory`      double                                                  NOT NULL DEFAULT 0 COMMENT 'jvm堆大小，以M为单位',
    `jvm_memory_used` double                                                  NOT NULL DEFAULT 0 COMMENT 'jvm堆使用率',
    `job_num`         int(10)                                                 NOT NULL DEFAULT 0 COMMENT '正在执行job数',
    `heartbeat`       datetime                                                NULL     DEFAULT '1971-01-01 00:00:00' COMMENT '心跳时间',
    `app_name`        varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '被调度的应用名称',
    `create_time`     datetime                                                NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     datetime                                                NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `worker_code` (`worker_code`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 684766
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = 'worker信息'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for logi_worker_blacklist
-- ----------------------------
create table if not exists `logi_worker_blacklist`
(
    `id`          bigint(20)                                              NOT NULL AUTO_INCREMENT,
    `worker_code` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT 'worker taskCode',
    `create_time` datetime                                                NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime                                                NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `worker_code` (`worker_code`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = 'worker黑名单列表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for operate_record_info
-- ----------------------------
create table if not exists `operate_record_info`
(
    `id`             bigint(20) UNSIGNED                                     NOT NULL AUTO_INCREMENT COMMENT '主键 自增',
    `project_name`   varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT NULL COMMENT '应用',
    `module_id`      int(10)                                                 NOT NULL DEFAULT -1 COMMENT '模块id',
    `operate_id`     int(10)                                                 NOT NULL DEFAULT -1 COMMENT '操作id',
    `trigger_way_id` int(11)                                                 NULL     DEFAULT NULL COMMENT '触发方式',
    `user_operation` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT '操作人',
    `content`        longtext CHARACTER SET utf8 COLLATE utf8_general_ci     NULL COMMENT '操作内容',
    `operate_time`   timestamp                                               NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    `create_time`    timestamp                                               NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    timestamp                                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `biz_id`         varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 7248
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '操作记录表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for project_arius_config
-- ----------------------------
create table if not exists `project_arius_config`
(
    `project_id`              bigint(10) UNSIGNED                                      NOT NULL AUTO_INCREMENT COMMENT 'project id',
    `analyze_response_enable` tinyint(4)                                               NOT NULL DEFAULT 1 COMMENT '响应结果解析开关 默认是0：关闭，1：开启',
    `is_source_separated`     tinyint(4)                                               NOT NULL DEFAULT 0 COMMENT '是否是索引存储分离的 0 不是 1 是',
    `aggr_analyze_enable`     tinyint(4)                                               NOT NULL DEFAULT 1 COMMENT '1 生效 0 不生效',
    `dsl_analyze_enable`      tinyint(2)                                               NOT NULL DEFAULT 1 COMMENT '1为生效dsl分析查询限流值，0不生效dsl分析查询限流值',
    `slow_query_times`        int(10)                                                  NOT NULL DEFAULT 100 COMMENT '慢查询耗时',
    `is_active`               tinyint(2)                                               NOT NULL DEFAULT 1 COMMENT '1为可用，0不可用',
    `memo`                    varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '备注',
    `create_time`             timestamp                                                NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`             timestamp                                                NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`project_id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1830
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '项目配置'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for project_arius_resource_logic
-- ----------------------------
create table if not exists `project_arius_resource_logic`
(
    `id`                 bigint(20) UNSIGNED                                      NOT NULL AUTO_INCREMENT COMMENT '主键自增',
    `name`               varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT '资源名称',
    `type`               tinyint(4)                                               NOT NULL DEFAULT 2 COMMENT '资源类型 1 共享公共资源 2 独享资源',
    `project_id`         varchar(1024) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '-1' COMMENT '资源所属的project_id ',
    `data_center`        varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci   NOT NULL DEFAULT '' COMMENT '数据中心 cn/us01',
    `responsible`        varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL     DEFAULT '' COMMENT '资源责任人',
    `memo`               varchar(512) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL DEFAULT '' COMMENT '资源备注',
    `quota`              decimal(8, 2)                                            NOT NULL DEFAULT 1.00 COMMENT '资源的大小',
    `level`              tinyint(4)                                               NOT NULL DEFAULT 1 COMMENT '服务等级 1 normal 2 important 3 vip ',
    `config_json`        varchar(1024) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '集群配置',
    `create_time`        timestamp                                                NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`        timestamp                                                NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `health`             tinyint(4)                                               NOT NULL DEFAULT 3 COMMENT '集群状态 1 green 2 yellow 3 red -1 未知',
    `data_node_spec`     varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci   NOT NULL DEFAULT '' COMMENT '节点规格',
    `disk_total`         bigint(50)                                               NOT NULL DEFAULT 0 COMMENT '集群磁盘总量 单位byte',
    `disk_usage`         bigint(50)                                               NOT NULL DEFAULT 0 COMMENT '集群磁盘使用量 单位byte',
    `disk_usage_percent` decimal(10, 5)                                           NOT NULL COMMENT '集群磁盘空闲率 单位 0 ~1',
    `es_cluster_version` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci   NOT NULL COMMENT 'es集群版本',
    `node_num`           int(10)                                                  NOT NULL COMMENT '节点个数',
    `data_node_num`      int(10)                                                  NOT NULL DEFAULT 0 COMMENT '节点个数',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_name` (`name`) USING BTREE,
    INDEX `idx_project_id` (`project_id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 3908
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '逻辑资源信息'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for project_logi_cluster_auth
-- ----------------------------
create table if not exists `project_logi_cluster_auth`
(
    `id`               bigint(20) UNSIGNED                                     NOT NULL AUTO_INCREMENT COMMENT '主键自增',
    `project_id`       int(10)                                                 NOT NULL DEFAULT -1 COMMENT '项目id',
    `logic_cluster_id` bigint(20) UNSIGNED                                     NOT NULL DEFAULT 0 COMMENT '逻辑集群id',
    `type`             int(10)                                                 NOT NULL DEFAULT -1 COMMENT '权限类型，0-超管，1-配置管理，2-访问，-1-无权限',
    `responsible`      varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL     DEFAULT '' COMMENT '责任人id列表',
    `status`           int(10)                                                 NOT NULL DEFAULT 1 COMMENT '状态 1有效 0无效',
    `create_time`      timestamp                                               NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      timestamp                                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_project_id` (`project_id`) USING BTREE,
    INDEX `idx_logic_cluster_id` (`logic_cluster_id`) USING BTREE,
    INDEX `idx_status` (`status`) USING BTREE,
    INDEX `idx_type` (`type`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = 'project逻辑集群权限'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for project_template_info
-- ----------------------------
create table if not exists `project_template_info`
(
    `id`          bigint(20) UNSIGNED                                     NOT NULL AUTO_INCREMENT COMMENT '主键自增',
    `project_id`  int(10)                                                 NOT NULL DEFAULT -1 COMMENT '项目id',
    `template`    varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '模板名称, 不能关联模板id 模板会跨集群迁移，id会变',
    `type`        int(10)                                                 NOT NULL DEFAULT -1 COMMENT 'appid的权限 1 读写 2 读 -1 未知',
    `status`      int(10)                                                 NOT NULL DEFAULT 1 COMMENT '状态 1有效 0无效',
    `create_time` timestamp                                               NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp                                               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_project_id` (`project_id`) USING BTREE,
    INDEX `idx_status` (`status`) USING BTREE,
    INDEX `idx_template_id` (`template`) USING BTREE,
    INDEX `idx_type` (`type`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = 'project模板信息'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_metrics_config_info
-- ----------------------------
create table if not exists `user_metrics_config_info`
(
    `id`          bigint(20)                                              NOT NULL AUTO_INCREMENT,
    `user_name`   varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户账号',
    `metric_info` text CHARACTER SET utf8 COLLATE utf8_general_ci         NULL COMMENT '指标看板的配置',
    `create_time` timestamp                                               NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` timestamp                                               NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1602
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = '用户关联到指标的配置信息表'
  ROW_FORMAT = Dynamic;


/*其中cluster 为元数据集群名称 这里用的是 logi-elasticsearch-7.6.0 */

INSERT INTO `es_config` (`id`, `cluster_id`, `type_name`, `engin_name`, `config_data`, `desc`, `version_tag`,
                         `version_config`, `selected`, `create_time`, `update_time`, `delete_flag`)
VALUES (1, 1, 'elasticsearch.yml', 'template',
        'cluster.name: ${CLUSTERNAME}\nnode.name: ${HOSTNAME}\nnode.master: false\nnode.data: true\npath.data: /data1/es\nhttp.enabled: false\nnetwork.host: 0.0.0.0\nhttp.host: 0.0.0.0\ntransport.tcp.port: 9300\ndiscovery.zen.ping.unicast.hosts: [ ${MASTERNODES} ]\ndiscovery.zen.minimum_master_nodes: 2\ngateway.recover_after_nodes: 2\nnode.max_local_storage_nodes: 1\nindices.memory.index_buffer_size: 20%\nindices.queries.cache.size: 30%\ncluster.routing.allocation.same_shard.host: true\nprocessors: 16\nthread_pool.search.size: 16\nthread_pool.search.queue_size: 1000\nthread_pool.write.size: 16\nthread_pool.write.queue_size: 1000\nhttp.compression: true\ncluster.routing.allocation.type: group_balanced',
        'template', '1', 1, 1, '2020-10-15 17:11:31', '2021-01-06 14:26:36', 0);
INSERT INTO `es_config` (`id`, `cluster_id`, `type_name`, `engin_name`, `config_data`, `desc`, `version_tag`,
                         `version_config`, `selected`, `create_time`, `update_time`, `delete_flag`)
VALUES (6, 1, 'jvm.options', 'template',
        '## JVM configuration\n\n################################################################\n## IMPORTANT: JVM heap size\n################################################################\n##\n## You should always set the min and max JVM heap\n## size to the same value. For example, to set\n## the heap to 4 GB, set:\n##\n## -Xms4g\n## -Xmx4g\n##\n## See https://www.elastic.co/guide/en/elasticsearch/reference/current/heap-size.html\n## for more information\n##\n################################################################\n\n# Xms represents the initial size of total heap space\n# Xmx represents the maximum size of total heap space\n-Xms31g\n-Xmx31g\n\n################################################################\n## Expert settings\n################################################################\n##\n## All settings below this section are considered\n## expert settings. Don\'t tamper with them unless\n## you understand what you are doing\n##\n################################################################\n\n## GC configuration\n#-XX:+UseConcMarkSweepGC\n#-XX:CMSInitiatingOccupancyFraction=75\n#-XX:+UseCMSInitiatingOccupancyOnly\n\n-XX:+UseG1GC\n-XX:InitiatingHeapOccupancyPercent=70\n-XX:MaxGCPauseMillis=200\n\n## G1GC Configuration\n# NOTE: G1GC is only supported on JDK version 10 or later.\n# To use G1GC uncomment the lines below.\n# 10-:-XX:-UseConcMarkSweepGC\n# 10-:-XX:-UseCMSInitiatingOccupancyOnly\n# 10-:-XX:+UseG1GC\n# 10-:-XX:InitiatingHeapOccupancyPercent=75\n\n-Djava.io.tmpdir=${ES_TMPDIR}\n\n## heap dumps\n\n# generate a heap dump when an allocation from the Java heap fails\n# heap dumps are created in the working directory of the JVM\n#-XX:+HeapDumpOnOutOfMemoryError\n\n# specify an alternative path for heap dumps; ensure the directory exists and\n# has sufficient space\n#-XX:HeapDumpPath=data\n\n# specify an alternative path for JVM fatal error logs\n-XX:ErrorFile=hs_err_pid%p.log\n\n## JDK 8 GC logging\n\n8:-XX:+PrintGCDetails\n8:-XX:+PrintGCDateStamps\n8:-XX:+PrintTenuringDistribution\n8:-XX:+PrintGCApplicationStoppedTime\n8:-Xloggc:logs/gc.log\n8:-XX:+UseGCLogFileRotation\n8:-XX:NumberOfGCLogFiles=32\n8:-XX:GCLogFileSize=64m\n\n# JDK 9+ GC logging\n9-:-Xlog:gc*,gc+age=trace,safepoint:file=logs/gc.log:utctime,pid,tags:filecount=32,filesize=64m\n# due to internationalization enhancements in JDK 9 Elasticsearch need to set the provider to COMPAT otherwise\n# time/date parsing will break in an incompatible way for some date patterns and locals\n9-:-Djava.locale.providers=COMPAT\n\n# temporary workaround for C2 bug with JDK 10 on hardware with AVX-512\n10-:-XX:UseAVX=2\n\n-Djava.security.policy=config/java.policy\n-Dcsp.sentinel.log.output.type=console\n-XX:+ExitOnOutOfMemoryError',
        'template', '1', 1, 1, '2020-10-15 17:11:31', '2021-01-06 14:26:39', 0);
INSERT INTO `es_config` (`id`, `cluster_id`, `type_name`, `engin_name`, `config_data`, `desc`, `version_tag`,
                         `version_config`, `selected`, `create_time`, `update_time`, `delete_flag`)
VALUES (10, 1, 'filebeat.yml', 'template', 'template', 'template', '1', 1, 1, '2020-10-15 17:11:31',
        '2021-09-24 16:26:47', 0);

INSERT INTO `es_machine_norms` (`id`, `role`, `spec`, `create_time`, `delete_flag`)
VALUES (1, 'masternode', '8-48Gi-100g', '2021-03-30 11:52:45', 0);
INSERT INTO `es_machine_norms` (`id`, `role`, `spec`, `create_time`, `delete_flag`)
VALUES (3, 'clientnode', '8-48Gi-100g', '2021-03-30 11:52:45', 0);
INSERT INTO `es_machine_norms` (`id`, `role`, `spec`, `create_time`, `delete_flag`)
VALUES (5, 'clientnode', '16-48Gi-100g', '2021-03-30 11:52:45', 0);
INSERT INTO `es_machine_norms` (`id`, `role`, `spec`, `create_time`, `delete_flag`)
VALUES (7, 'datanode', '16-48Gi-2048g', '2021-03-30 11:52:45', 0);
INSERT INTO `es_machine_norms` (`id`, `role`, `spec`, `create_time`, `delete_flag`)
VALUES (9, 'datanode', '16-64Gi-3072g', '2021-03-30 11:52:45', 0);
INSERT INTO `es_machine_norms` (`id`, `role`, `spec`, `create_time`, `delete_flag`)
VALUES (11, 'datanode-ceph', '16-48Gi-3072g', '2021-03-30 11:52:45', 0);
INSERT INTO `es_machine_norms` (`id`, `role`, `spec`, `create_time`, `delete_flag`)
VALUES (13, 'datanode-ceph', '16-48Gi-5120g', '2021-03-30 11:52:45', 0);
INSERT INTO `es_machine_norms` (`id`, `role`, `spec`, `create_time`, `delete_flag`)
VALUES (15, 'masternode', '4-24Gi-100g', '2021-03-30 11:52:45', 0);