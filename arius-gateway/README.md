# 1.项目背景

> arius-gateway-v2, 提供了100%适配原生 ES RestClient 的接口功能，零侵入、插件化的进行了ES特性增强：查询/写入限流、权限校验、跨集群访问、DSL分析与管控。

# 2.模块划分

 >arius-gateway-common     存放公共常量, 公用方法, 异常类, 普通java bean等。
> 
 >arius-gateway-core       提供核心方法, 主要业务逻辑实现。
> 
>arius-gateway-remote     依赖第三方的接口。
>   
> arius-gateway-rest       提供rest和Tcp方式接口。
>   
> arius-gateway-task       执行程序定时任务。

# 3.如何使用

> arius-gateway-v2 作为网关层，需配合 arius-admin-v2 使用。
> 
1. 配置
   
   ```java
   配置环境文件：application-xxx.properties
   arius.gateway.adminUrl=arius.gateway.adminUrl=http://{host}:{port}/admin/api
   ```
2. 打包 
   
   ```java
   mvn clean package -Dmaven.test.skip=true
   ```
   
3. 运行

   ```java
   java -jar arius-gateway-rest-0.0.1-SNAPSHOT.jar --spring.profiles.active=test
   ```
