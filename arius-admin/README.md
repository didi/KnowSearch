## 1. 项目背景
    arius-admin-v2 致力于实现一套于对Elasticsearch元数据的管控平台，负责维护项目（APPID）、索引、Elasticsearch集群资源等信息的管理，并维护三者之前的关系；对外暴露restFul接口，以提供查询管控元数据的能力。
## 2. 工程模块组成和划分
arius-admin-v2由10个主要的工程和扩展增值服务组成:

    arius-admin-rest            表现层1，可以直接封装Manager接口暴露成restful接口 
    arius-admin-task            表现层2，提供auv-job定时调度任务入口, 任务实现在arius-admin-biz/arius-admin-core中 
    arius-admin-common          基础组件层1，存放业务需要数据结构, 如Java POJO(entity、po)、公共工具方法、事件等 
    arius-admin-client          基础组件层2，存放业务需要的POJO(vo、dto)、枚举等, 并且提供客户端请求响应的数据实体。 
    arius-admin-biz             业务层1，负责对arius-admin-core中各种的业务逻辑进行聚合处理，提供表现层所需要的数据实体 
    arius-admin-extend          业务层2，扩展服务, 如容量规划、fast-index等 
    arius-admin-core            核心层1，提供核心的管控能力, 主要的业务逻辑实现, 如集群、索引、项目等 
    arius-admin-metadata        核心层2，负责对Elasticsearch的元数据采集，如索引指标统计、节点指标统计, 提供的能力包括但不限于DSL分析、健康检查等 
    arius-admin-persistence     数据层1，负责对MySQL和Elasticsearch中的数据进行操作 
    arius-admin-remote          数据层2，获取第三方服务数据, 如访问文件存储系统、企业部门系统等

名词定义：

    resource:       逻辑资源
    cluster:        物理es集群
    area:           容量规划的执行单元,由多个region组成
    region:         容量规划的规划单元,由多个rack组成
 
    
## 3. 如何使用
    可单独部署，也可配合网关 arius-gateway-v2 一同使用，致力于更高效的管控 Elasticsearch 元数据，由网关提供API审核，查询限流、优化，权限控制等能力，对外提供 rest、tcp 方式，提供 Elasticsearch 查询和写入服务，屏蔽平台内部的细节，更高效的保护和管控ES集群的作用。

### a. 配置
    配置环境文件：application-xxx.properties
#### 更新ES数据集群名称
    es.update.cluster.name: {name}

    es.client.cluster.port: {port}

#### 网关配置
    es.gateway.url: {host}

    es.gateway.port: {port}

    es.appid: {appId1},{appId2},{appId3}

    es.password: {passwd},{passwd},{passwd}

#### 数据源配置
    datasource:
        name: data
        type: com.alibaba.druid.pool.DruidDataSource 
        druid:
          driver-class-name: com.mysql.jdbc.Driver
          url: jdbc:mysql://{host}:{port}/{dbName}?useUnicode=true&characterEncoding=utf8&jdbcCompliantTruncation=true&allowMultiQueries=true&useSSL=false
        username: {username}
        password: {password}
      
    auv-job:
        jdbc-url: jdbc:mysql://{host}:{port}/{dbName}?useUnicode=true&characterEncoding=utf8&jdbcCompliantTruncation=true&allowMultiQueries=true&useSSL=false
        username: {username}
        password: {password}

### b.打包
    mvn clean package -Dmaven.test.skip=true

### c.运行
    java -jar arius-admin-rest.jar --spring.profiles.active=xxx (test、dev)
## 4.获取帮助
    如有使用疑问，及商业合作意向，可联系团队：dataprod.didicloud@didiglobal.com