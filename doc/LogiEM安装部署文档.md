# 1.部署架构说明

LogiEM 平台由两个后端应用，一个前端应用，一个kibana服务，一个ngnix服务构成，它们之间的部署架构如下图所属：

<img src="http://116.85.24.226/images/3001.png" alt="3001" style="zoom:50%;" />

# 2.物理机部署文档

部署流程：关系型数据库构建 ——> Elasticsearch元数据集群搭建 ——> Arius-admin 服务部署(包括设置服务基础配置、元数据集群的接入、元数据模板初始化等关键流程 )   ——> Arius-Gateway部署（包括设置服务基础配置）——> kibana部署 ——> 前端部署六个步骤。

## 2.1 关系数据库构建

默认使用关系型数据库Mysql，具体构建mysql操作如下： 

- CREATE DATABASE IF NOT EXISTS logi_em; 
- USE logi_em 
- 建表语句
  - [数据库创建DDL下载跳转链接](https://logi-em.s3.didiyunapi.com/logi-em-ddl.sql)
- 插入初始化数据
  - [数据库初始化DML下载跳转链接](https://logi-em.s3.didiyunapi.com/logi-em-init-dml.sql)

初始化dsl需要指定【元数据Elasticsearch集群名称】 ，即在初始化表query_app 时涉及一列cluster ，为元数据集群名称，例如 logi-elasticsearch-7.6.0

## 2.2 Elasticsearch元数据集群搭建

需要搭建一个Elasticsearch元数据集群来支持平台核心指标数据的存储，如集群维度指标、节点维度指标等等，可以参考如下 [Elasticsearch官方文档](https://www.elastic.co/guide/en/elasticsearch/reference/7.6/elasticsearch-intro.html)。

## 2.3.基础资源下载

1. 下载后端资源服务包（包括admin服务、gateway服务），[下载跳转链接](https://logi-em.s3.didiyunapi.com/logi-init.tar.gz)
2. 如需要二次开发，clone LogiEM到本地打开admin/gateway，进行打包，详见如下【服务打包】流程


## 2.4 Arius-admin服务部署

### 2.4.2 基础配置

下载相关资源，解压后进入admin目录，修改常用 application-full.yml 配置

```yaml
#################### 常用修改配置 begin #####################
# admin配置
admin:
  port:
    web: 8015                #admin端口号
  contextPath: /admin/api    #admin http请求前缀
  thread:
    size:                   
      op: 20                 #admin 内置线程池大小

# gateway相关配置
es.gateway.url: 127.0.0.1    #网关服务ip
es.gateway.port: 8200        #网关服务端口号

# ES元数据集群名称
es.update.cluster.name: logi-em-matedata-cluster
es.client.cluster.port: 9200

# spring 公共配置
spring:
  http:
    encoding:
      force: true
      charset: UTF-8
      enabled: true
  messages:
    encoding: UTF-8
  profiles:
    active: test
  datasource:
    name: data
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      driver-class-name: com.mysql.jdbc.Driver
      url: jdbc:mysql://127.0.0.1:3306/logi_em?useUnicode=true&characterEncoding=utf8&jdbcCompliantTruncation=true&allowMultiQueries=true&useSSL=false       #jdbc驱动相关信息
      username: root                     #mysql db  客户端用户名
      password: Logi_em123               #mysql db 客户端密码
      initialSize: 10
      validationQueryTimeout: 5000
      transactionQueryTimeout: 60000
      minIdle: 10
      maxActive: 30
      max-wait: 60000
      keepAlive: true
      time-between-eviction-runs-millis: 1000
      min-evictable-idle-time-millis: 300000
      defaultAutoCommit: true
      validationQuery: select 'x'
      testWhileIdle: true
      testOnReturn: false
      testOnBorrow: true
      logAbandoned: true
      pool-prepared-statements: true
      max-open-prepared-statements: 50
      filters: stat,wall
  servlet:
    multipart:
      max-file-size: 60MB
      max-request-size: 60MB
  logi-job:
    jdbc-url: jdbc:mysql://127.0.0.1:3306/logi_em?useUnicode=true&characterEncoding=utf8&jdbcCompliantTruncation=true&allowMultiQueries=true&useSSL=false             #jdbc驱动相关信息
    username: root                            #mysql db  客户端用户名
    password: Logi_em123                      #mysql db 客户端密码
    driver-class-name: com.mysql.jdbc.Driver
    max-lifetime: 60000
    init-sql: true
    init-thread-num: 10
    max-thread-num: 20
    log-exipre: 3                     #定时调度任务日志保存天数，以天为单位
    app_name: arius_test02       #环境名称，用于进行不同环境调度任务状态隔离
    claim-strategy: com.didiglobal.logi.job.core.consensual.RandomConsensual
################ 常用修改配置 end ########################
```

### 2.4.3 服务打包

使用maven打包生成jar包，具体指令如下：

```
mvn clean package -Dmaven.test.skip=true
```

### 2.4.4 服务启动

1. 部署环境要求
   1. 机器要求（单台机器4C 8G）
   2. JDK1.8 、操作系统不限

2. 进入arius-admin-rest.jar 所在目录，执行下面指令

```
java -jar -Xmx8g -Xms8g -Xmn3g -XX:MaxDirectMemorySize=2G -XX:MaxMetaspaceSize=256M -Djdk.nio.maxCachedBufferSize=262144 ./arius-admin-rest.jar --spring.config.location=./application-full.yml &
```

3. 校验服务是否正常

```
curl -X GET 'http://{host}:{port}/admin/api/v2/thirdpart/gateway/listApp' --header 'X-ARIUS-GATEWAY-TICKET: xTc59aY72'
```

### 2.4.5 初始化数据

首次部署需要导入系统初始化数据，下载以下包到包目录，运行以下shell脚本，[下载链接跳转](https://logi-em.s3.didiyunapi.com/logi-init.tar.gz)

```
sh init_logi_em_linux.sh 116.85.6.111 8015 116.85.6.111 8060
脚本入参说明：
$1 任意logi-em服务ip   
$2 任意logi-em服务port 
$3 元数据集群任意有效master节点ip
$4 元数据集群任意有效master节点port
$5 如果存在元数据信息是否需要删除 1为需要、0为不需要(不建议使用)
```

## 2.5 Arius-Gateway服务部署

### 2.5.1 基础配置

先部署启动Arius-Admin，再部署Arius-Gateway-v2，Arius-Gateway-v2 作为网关层，需配合 arius-admin-v2 使用。

下载 Logi-EM-Installation package.zip，解压，进入gateway目录，涉及以下常用 application-full.properties 文件配置修改 

```
gateway.cluster.name={metaCluster}
elasticsearch.admin.cluster.name={metaCluster}
arius.gateway.adminUrl=http://{ariusIp}:{ariusPort}/admin/api/
gateway.httpTransport.port=8200
```

### 2.5.2 服务打包

使用maven打包生成jar包，具体指令如下：

```
mvn clean package -Dmaven.test.skip=true
```

### 2.5.3 服务启动

1. 部署环境要求
   1. 机器要求（单台机器4C 内存8G）
   2. JDK1.8 、操作系统不限
2. 进入arius-gateway-rest.jar r 所在目录，执行下面指令

```
java -jar ./arius-gateway-rest.jar --spring.config.location=./application-full.properties &
```

3. 进入arius-gateway-rest.jar 所在目录，执行下面命令启动filebeats(metaCluster为元数据es集群)

```
sh filebeats_start.sh {metaCluster}
比如sh filebeats_start.sh 127.0.0.1:9200
```

4. 检查服务是否生效，检查gateway是否启动

```
curl -XGET "http://1:azAWiJhxkho33ac@{host}:{port}/_cluster/health"
```

检查filebeats是否启动

```
ps -ef | grep filebeat
```

**PS：**

Arius-Admin中访问Arius-Gateway服务地址检查
部署好Arius-Gateway服务后，回头确认Arius-Admin  application-full.yml中的配置，是否为Arius-Gateway的服务url和端口 ，检查样例如下：

```
es.gateway.url: 10.190.5.95
es.gateway.port: 8200
```

## 2.6 Kibana服务部署

### 2.6.1 部署目的

Arius用户控制台中内嵌了kibana应用，用于用户在控制台上直接进行kibana查询和dashboard的访问，并做了相应的权限管控和隔离。

### 2.6.2 部署前提

部署 arius-kibana 需要提前做好以下准备：

1. 部署好 arius-admin、arius-gateway、arius-console、nginx、ES元数据集群
2. 在 arius-admin 上申请一个超级用户，如：appid：1，appPassword：azAWiJhxkho33ac
3. 这里的超级用户主要是为了能够通过gateway的权限校验

### 2.6.3 开始部署 kibana

1. 地址相关包解压到用户目录，如：/home/ec2-user/arius-kibana-release   ，[下载跳转链接](https://logi-em.s3.didiyunapi.com/arius-kibana-release.tar.gz)
2. 设置kibana应用的配置文件，打开/home/ec2-user/arius-kibana-release/config/config.yaml，修改以下配置项

```
erver.basePath: "/console/arius/kibana7"
server.rewriteBasePath: true
kibana.index: ".kibana7_arius"
#arius gateway的地址
elasticsearch.hosts: ["http://10.161.98.190:8200"]     
#arius admin超级账号
elasticsearch.username: "1"   
#arius admin超级账号密码                                          
elasticsearch.password: "azAWiJhxkho33ac"                
server.port: 8061
```

启动kibana执行：sh /home/ec2-user/arius-kibana-release/control.sh start，若显示 "start ok" ，则表示启动成功。

也可以直接执行：nohup sh home/ec2-user/arius-kibana-release/bin/kibana serve -l home/ec2-user/output/logibana.log >> /dev/null 2>&1 &

成功启动之后，访问kibana会需要进行权限校验

## 2.7 前端部署

### 2.7.1 部署依赖

1. 资源：一台可用机器，用于部署前端资源及nginx
2. 环境：机器上安装 node（v12 及以上版本）、nginx

### 2.7.2 部署步骤

1. 获取前端资源文件
   1. 通过github 下载 release 资源包直接使用
   2. 编译前端源码生成 release 资源包使用，编译步骤详见前端工程readme
2. 上传前端资源文件至机器
   1. 可通过 scp 命令上传

```
# ./pub/es 本地前端资源所在路径
scp -r ./pub/es 账号@ip: 服务器文件路径
```

3. 配置 nginx
   1. 配置前端资源路径
   2. 接口请求代理
   3. kibana 服务代理

```
# 示例如下
# For more information on configuration, see:
#   * Official English Documentation: http://nginx.org/en/docs/
#   * Official Russian Documentation: http://nginx.org/ru/docs/


user root;
worker_processes auto;
error_log /var/log/nginx/error.log;
pid /run/nginx.pid;


# Load dynamic modules. See /usr/share/doc/nginx/README.dynamic.
include /usr/share/nginx/modules/*.conf;


events {
    worker_connections 1024;
}


http {
    log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
                      '$status $body_bytes_sent "$http_referer" '
                      '"$http_user_agent" "$http_x_forwarded_for"';


    access_log  /var/log/nginx/access.log  main;
    
    sendfile            on;
    tcp_nopush          on;
    tcp_nodelay         on;
    keepalive_timeout   65;
    types_hash_max_size 2048;


    include             /etc/nginx/mime.types;
    default_type        application/octet-stream;


    # Load modular configuration files from the /etc/nginx/conf.d directory.
    # See http://nginx.org/en/docs/ngx_core_module.html#include
    # for more information.
    include /etc/nginx/conf.d/*.conf;


    server {
      listen       80 default_server;
      server_name  _;
      client_max_body_size 600m; # 上传文件大小限制
      root        /root/es;


      # Load configuration files for the default server block.
      include /etc/nginx/default.d/*.conf;


    gzip on;
      gzip_buffers        16 8k;
      gzip_comp_level     4;
      gzip_http_version   1.0;
      gzip_min_length     1280;
      gzip_types          text/plain text/css text/xml application/x-javascript application/xml application/xml+rss application/json application/javascript text/*;
      gzip_vary           on; 



    location / {
      root /root/es; # /root/es 前端资源文件存放路径
      if ($request_filename ~* .*\.(?:htm|html|json)$) { # 对 htm|html|json 不做缓存
          add_header Cache-Control "private, no-store, no-cache, must-revalidate, proxy-revalidate";
      }
      try_files $uri /index.html;
    }


    location /es {
      root  /root;
      try_files $uri @fallback;
    }


    location @fallback {
      root /root/es/;
      rewrite .* /index.html break;
    }
        
    # 接口代理
    location ~ ^/api/es/admin/ { 
        rewrite ^/api/es/admin/(.*)$ /admin/api/$1 break; # 路径规则重写
        proxy_pass http://xxx.com; # 后端服务地址
    }


    location ~ ^/api/es/ams/ {
      proxy_pass http://xxx:8888;
    }


    location ^~ /console/arius/kibana7/ {
        if ($request_filename ~* .*\.(?:js|css)$) {
          add_header Cache-Control "max-age=604800000"; # 强缓存过期时间一周
        }
        
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-NginX-Proxy true;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header Authorization 'Basic $cookie_Authorization';
        proxy_pass_request_headers on;
        proxy_cache_bypass $http_upgrade $http_authorization;
        proxy_pass http://xxx:8061;  # kibana 服务地址
        rewrite ^/console/arius/kibana7/(.*)$ /console/arius/kibana7/$1 break;
    }


    error_page 404 /404.html;
    location = /404.html {}


    error_page 500 502 503 504 /50x.html;
    location = /50x.html {}
  }
}
```

### 2.7.3 服务启动

 启动 nginx

```
nginx -t # 检查 nginx 配置是否正确
systemctl start nginx    # 启动 Nginx
```

在浏览器输入 ip:port 访问前端页面