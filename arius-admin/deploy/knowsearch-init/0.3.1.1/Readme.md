基于`0.3.1`的升级
- sql
  - 字段
  - 数据
- 脚本
- 修改user_metrics_config_info表名为user_config_info
- 修改metrics_info字段为config_info
- 增加项目id和config_type字段，增加项目id是为了满足用户+应用进行个性化配置的需求，增加config_type是为了可扩展性，目前1-dashboard和指标看板、2-查询模板
- 因为指标看板和dashboard入参名称发生改变，需要truncate user_config_info
- 修改deploy/zh_update/sql/table_rename.sql脚本中出现的es_manager_test3库的user_metrics_config_info表名为user_config_info