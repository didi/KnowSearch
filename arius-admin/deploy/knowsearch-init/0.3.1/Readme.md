# `kndowersearch 0.3.1`升级方案
1. 导入权限和项目的sql
2. 配置`cluster-phy-join.json`中
   1. 其中`clusterRegionDTOS`中需要根据有几个ip和port进行进行对应的匹配
   2. 其中`cluster`需要和脚本中的几个`cluster`名称相同，最好不要改
3. 配置`init_knowersearch.sh`
   1. 其中`url_prefix`是需要配置的，这个是部署之后请求的`admin`的地址
   2. 其余的默认就可以了