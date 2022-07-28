# 字段内容变更
alter table es_manager_test3.es_cluster_region
    modify racks varchar(2048) default '' null comment 'region的rack，逗号分隔';