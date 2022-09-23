alter table user_metrics_config_info rename to user_config_info;
alter table user_config_info COMMENT '用户和应用配置信息表';
alter table `user_config_info` change COLUMN metric_info config_info text COMMENT '用户下某个应用的配置';
alter table `user_config_info` add column project_id int(10) NOT NULL DEFAULT '-1' COMMENT '项目id' after user_name;
alter table `user_config_info` add column config_type int(10) NOT NULL DEFAULT '1' COMMENT '配置类型,1-指标看板和dashboard，2-查询模板列表' after project_id;
truncate table user_config_info;
# user_metrics_config_info-》user_config_info
ALTER TABLE es_manager_test3.user_metrics_config_info RENAME TO es_manager_test3.user_config_info;

INSERT INTO `es_manager_test`.`user_config_info`(`user_name`, `project_id`, `config_type`, `config_info`)
select DISTINCT t1.user_name,t2.project_id,2,
                concat('[{\"firstUserConfigType\":\"searchQuery\",\"projectId\":',t2.project_id,',\"secondUserConfigType\":\"searchTemplate\",\"userConfigTypes\":[\"totalCostAvg\",\"totalShardsAvg\"],\"userName\":\"',t1.user_name,'\"}]') as config_info
from logi_security_user t1 inner join
     logi_security_user_project t2 on t1.id=t2.user_id and t2.is_delete='0'
where  t1.is_delete='0';