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