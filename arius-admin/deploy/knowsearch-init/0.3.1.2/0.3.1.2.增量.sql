# admin表修改
alter table es_cluster_phy_info
    add  kibana_address varchar(200) default '' null comment 'kibana外链地址';
alter table es_cluster_phy_info
    add  cerebro_address varchar(200) default '' null comment 'cerebro外链地址';

# 可观测性组件:
# 1. 所有logi_security_*表名都改成了kf_security_*
# 2. logi_task表新增字段"node_name_white_list_str", logi_worker表新增字段"node_name"