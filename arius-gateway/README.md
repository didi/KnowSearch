**项目背景**

> arius-gateway-v2, 是ES管控的重要组成部分, 负责处理API审核, 查询限流、优化, 权限控制等. 对外提供rest, tcp方式提供ES查询和写入服务, 屏蔽平台内部的细节, 起到了保护和管控ES集群的作用.

**工程模块组成和划分:**

 >arius-gateway-common     存放公共常量, 公用方法, 异常类, 普通java bean等.
> 
 >arius-gateway-core       提供核心方法, 主要业务逻辑实现.
> 
>arius-gateway-remote     依赖第三方的接口.
>   
> arius-gateway-rest       提供rest和Tcp方式接口.
>   
> arius-gateway-task       执行程序定时任务.

**如何使用**

> arius-gateway-v2 作为网关层，需配合 arius-admin-v2 使用。
> 
1. 配置
   
       配置环境文件：application-xxx.properties
       arius.gateway.adminUrl=arius.gateway.adminUrl=http://{host}:{port}/admin/api
2. 打包 
   
       mvn clean package -Dmaven.test.skip=true
   
3. 运行

       java -jar arius-gateway-rest-0.0.1-SNAPSHOT.jar --spring.profiles.active=test

   
**获取帮助**
> 如有使用疑问，及商业合作意向，可联系团队：dataprod.didicloud@didiglobal.com