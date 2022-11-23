# admin表修改
alter table es_cluster_phy_info
    add  kibana_address varchar(200) default '' null comment 'kibana外链地址';
alter table es_cluster_phy_info
    add  cerebro_address varchar(200) default '' null comment 'cerebro外链地址';
