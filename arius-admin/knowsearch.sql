# 变更：metrics_config -》user_metrics_config_info
create table user_metrics_config_info
(
    id             bigint auto_increment
        primary key,
    user_name     varchar(255)                      not null comment '用户账号',
    metric_info    text                                null comment '指标看板的配置',
    create_time    timestamp default CURRENT_TIMESTAMP null,
    update_time    timestamp default CURRENT_TIMESTAMP null
)ENGINE=InnoDB AUTO_INCREMENT=1592 DEFAULT CHARSET=utf8 comment '用户关联到指标的配置信息表';