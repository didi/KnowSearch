# 变更：metrics_config -》user_metrics_config_info
create table user_metrics_config_info
(
    id          bigint auto_increment
        primary key,
    user_name   varchar(255)                        not null comment '用户账号',
    metric_info text                                null comment '指标看板的配置',
    create_time timestamp default CURRENT_TIMESTAMP null,
    update_time timestamp default CURRENT_TIMESTAMP null
) ENGINE = InnoDB
  AUTO_INCREMENT = 1592
  DEFAULT CHARSET = utf8 comment '用户关联到指标的配置信息表';


#变更index_template_info app_id /* appid */ 变更为 project_id
create table index_template_info2
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
    department_id     varchar(20)                  default ''                not null comment '部门id',
    department        varchar(100)                 default ''                not null comment '部门名称',
    responsible       varchar(500)                 default ''                not null comment '责任人',
    date_field        varchar(50)                  default ''                not null comment '时间字段',
    date_field_format varchar(128)                 default ''                not null comment '时间字段的格式',
    id_field          varchar(512)                 default ''                not null comment 'id字段',
    routing_field     varchar(512)                 default ''                not null comment 'routing字段',
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
    region_id         int(10)                      default -1                not null comment '模板关联的regionId',
    open_srv          varchar(255)                                           null comment '已开启的模板服务'
)
    comment '逻辑索引模板表' charset = utf8;
create index idx_project_id
    on index_template_info2 (project_id);
create index idx_data_center
    on index_template_info2 (data_center);

create index idx_is_active
    on index_template_info2 (is_active);

create index idx_name
    on index_template_info2 (name);

create index idx_region_id
    on index_template_info2 (region_id);


#appid_template_info /**appid模板信息**/ 变更为 project_template_info appid  /* 用户账号 */ 变更为 project_id
create table project_template_info
(
    id              bigint unsigned auto_increment comment '主键自增'
        primary key,
    project_id      int(10)      default -1                not null comment '项目id',
    template        varchar(100) default ''                not null comment '模板名称, 不能关联模板id 模板会跨集群迁移，id会变',
    type            int(10)      default -1                not null comment '项目id的权限 1 读写 2 读 -1 未知',
    responsible_ids varchar(100) default ''                not null comment '责任人id列表',
    status          int(10)      default 1                 not null comment '状态 1有效 0无效',
    create_time     timestamp    default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time     timestamp    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment 'project关联模板信息' charset = utf8;

create index idx_project_id
    on project_template_info (project_id);

create index idx_responsibleids
    on project_template_info (responsible_ids);

create index idx_status
    on project_template_info (status);

create index idx_template_id
    on project_template_info (template);

create index idx_type
    on project_template_info (type);


#app_logic_cluster_auth /**appid逻辑集群权限**/变更为project_logi_cluster_auth app_id变更为project_id
create table project_logi_cluster_auth
(
    id               bigint unsigned auto_increment comment '主键自增'
        primary key,
    project_id       int(10)         default -1                not null comment '用户账号',
    logic_cluster_id bigint unsigned default 0                 not null comment '逻辑集群id',
    type             int(10)         default -1                not null comment '权限类型，0-超管，1-配置管理，2-访问，-1-无权限',
    responsible      varchar(100)    default ''                not null comment '责任人id列表',
    status           int(10)         default 1                 not null comment '状态 1有效 0无效',
    create_time      timestamp       default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time      timestamp       default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment 'project_id逻辑集群权限' charset = utf8;

create index idx_project_id
    on project_logi_cluster_auth (project_id);

create index idx_logic_cluster_id
    on project_logi_cluster_auth (logic_cluster_id);

create index idx_responsible
    on project_logi_cluster_auth (responsible);

create index idx_status
    on project_logi_cluster_auth (status);

create index idx_type
    on project_logi_cluster_auth (type);

#arius_resource_logic变更project_arius_resource_logic app_id 变更为 project_id
create table project_arius_resource_logic
(
    id            bigint unsigned auto_increment comment '主键自增'
        primary key,
    name          varchar(128)  default ''                not null comment '资源名称',
    type          tinyint       default 2                 not null comment '资源类型 1 共享公共资源 2 独享资源',
    project_id    int(10)       default -1                not null comment '资源所属的project_id ',
    data_center   varchar(20)   default ''                not null comment '数据中心 cn/us01',
    responsible   varchar(128)  default ''                not null comment '资源责任人',
    memo          varchar(512)  default ''                not null comment '资源备注',
    quota         decimal(8, 2) default 1.00              not null comment '资源的大小',
    level         tinyint       default 1                 not null comment '服务等级 1 normal 2 important 3 vip ',
    config_json   varchar(1024) default ''                not null comment '集群配置',
    create_time   timestamp     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time   timestamp     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    health        tinyint       default 3                 not null comment '集群状态 1 green 2 yellow 3 red -1 未知'
)
    comment '逻辑资源信息' charset = utf8;

create index idx_name
    on project_arius_resource_logic (name);


#work_order 变更：approver_app_id 变更为 approver_project_id aapplicant_app_id 变更为
# applicant_project_id
create table arius_work_order_info
(
    id                   bigint unsigned auto_increment comment 'id'
        primary key,
    type                 varchar(25)  default 'unknown'         not null comment 'appcreate 创建app,clustercreate 创建集群,clusterindecrease 集群扩缩溶,clusteroffline 集群下线,clusterupdate 集群修改,templateauth 索引申请,templatecreate 索引创建,templateindecrease 索引扩容,templatequerydsl 查询语句创建,templatetransfer 索引转让,querydsllimitedit 查询语句编辑,responsiblegovern 员工离职,unhealthytemplategovern 不健康索引处理',
    title                varchar(64)  default ''                not null comment '标题',
    approver_project_id  int(16)      default -1                not null comment '审批人projectId',
    applicant            varchar(64)  default ''                not null comment '申请人用户id',
    extensions           text                                   null comment '拓展字段',
    description          text                                   null comment '备注信息',
    approver             varchar(64)  default ''                not null comment '审批人用户id',
    finish_time          timestamp    default CURRENT_TIMESTAMP not null comment '结束时间',
    opinion              varchar(256) default ''                not null comment '审批信息',
    status               int(16)      default 0                 not null comment '工单状态, 0:待审批, 1:通过, 2:拒绝, 3:取消',
    create_time          timestamp    default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time          timestamp    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '修
改时间',
    applicant_project_id int(16)      default -1                not null comment '申请人projectid'
)
    comment '工单表' charset = utf8;
#arius_es_user /**es user 用户表**/ 新增表
create table arius_es_user
(
    id                      bigint(10) unsigned auto_increment comment '主键 自增'
        primary key,
    index_exp               text                                    null comment '索引表达式',
    data_center             varchar(20)   default ''                not null comment '数据中心',
    is_root                 tinyint       default 0                 not null comment '是都是超级用户 超级用户具有所有索引的访问权限 0 不是 1是',
    memo                    varchar(1000) default ''                not null comment '备注',
    ip                      varchar(500)  default ''                not null comment '白名单ip地址',
    verify_code             varchar(50)   default ''                not null comment 'app验证码',
    is_active               tinyint(2)    default 1                 not null comment '1为可用，0不可用',
    query_threshold         int(10)       default 100               not null comment '限流值',
    cluster                 varchar(100)  default ''                not null comment '查询集群',
    responsible             varchar(500)  default ''                not null comment '责任人',
    dsl_analyze_enable      tinyint(2)    default 1                 not null comment '1为生效dsl分析查询限流值，0不生效dsl分析查询限流值',
    aggr_analyze_enable     tinyint       default 1                 not null comment '1 生效 0 不生效',
    is_source_separated     tinyint       default 0                 not null comment '是否是索引处分分离的 0 不是 1 是',
    analyze_response_enable tinyint       default 1                 not null comment '响应结果解析开关 默认是0：关闭，1：开启',
    search_type             tinyint       default 0                 not null comment '0表示app的查询请求需要app里配置的集群(一般配置的都是trib集群) 1表示app的查询请求必须只能访问一个模板',
    create_time             timestamp     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time             timestamp     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '修改时间',
    project_id              bigint(10)                              not null comment '项目id',
    is_default_display      tinyint(2)    default 0                 not null comment '1：项目默认的es user；0:项目新增的es user'
)
    comment 'es操作用户表' charset = utf8;

create table project_arius_es_config
(
    id bigint(10) unsigned auto_increment comment 'project id'
        primary key,

)