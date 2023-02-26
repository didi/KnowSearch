# 前言

 + 本文以Centos7系统为例，系统基础配置要求：4核8G
 + 本文以0.3.2版本为例进行部署，按照本文可以快速部署一套单机模式的KnowSearch环境
 + 部署完成后可以通过浏览器输入IP:PORT进行访问，默认用户名密码: admin/admin123
 + shell部署方式支持分布式，具体方式可以参考安装包中的README文档
 + 安装完成后可以通过构造测试数据来体验产品功能（参考第三部分）

# 软件版本及依赖
| 软件名 | 版本要求 | 默认端口 |
| ------ | ------ | ------ |
| Mysql | v5.7+ | 3306 |
| Elasticsearch | 软件包中的固定版本 | 8060 |
| Grafana | v8.5.9 | 3000 |
| JDK | v8+ | - |
|  Centos| v6+ | - |
| Ubuntu| v16+ | - |
# 部署方式选择

+ SHELL方式部署

+ 根据文档手动部署

# 一、SHELL部署
## 1.1 快速体验单机版
### 1.1.1 在线方式安装
```
#在服务器中下载安装脚本,脚本中会重新安装Mysql
wget https://s3-gzpu.didistatic.com/pub/knowsearch/deploy_KnowSearch-0.3.2.sh

#执行脚本
sh deploy_KnowSearch-0.3.2.sh

#访问测试(默认用户名密码：admin/admin123)
127.0.0.1:8080
	
```

### 1.1.2 离线方式安装

```
#将安装包下载到本地且传输到目标服务器
wget https://s3-gzpu.didistatic.com/pub/knowsearch/KnowSearch-0.3.2_offline.tar.gz
	
#解压安装包
tar -zxf KnowSearch-0.3.2_offline.tar.gz
	
#执行安装脚本
sh deploy_KnowSearch-0.3.2_offline.sh
	
#访问测试(默认用户名密码：admin/admin123)
127.0.0.1:8080
	
```

## 1.2 分布式高可用版
### 1.2.1 安装ansible
```
#ansible安装方法 
yum -y install  ansible
 
#修改配置文件，避免首访问的机器信息没写入known_hosts文件
vim /etc/ansible/ansible.cfg
host_key_checking = False
```

### 1.2.2 操作步骤

```
#下载安装包
wget https://s3-gzpu.didistatic.com/pub/knowsearch/deploy_KnowSeach.tar.gz

#解压安装包
tar -zxf deploy_KnowSeach.tar.gz

#修改hosts.ini文件（根据注释和README文件修改）

#修改完成后执行安装脚本
sh install.sh

```

### 1.2.3 配置zeus服务

#### 1.2.3.1 通过浏览器访问zeus服务，生成token


#### 1.2.3.2 在KnowSearch服务端配置zeus相关信息

```
1. 修改admin服务模块的配置文件
vim ./KnowSearch-0.3.2/admin/application-full.yml
zeus.server: 127.0.0.1:8010 #请替换成真实的zeus服务端地址和端口
zeus.token: token #替换成在web端生产的token

2. 重启admin服务模块
sh control.sh restart

```


# 二、手动部署

## 1. 部署流程

基础依赖服务部署 ——>  KnowSearch各个模块部署

## 2. 基础依赖服务部署
 ###如已经部署过相关服务，可跳过对其的安装，但配置需要按照文档修改
 
### 2.1、安装Mysql服务
 ###版本需要5.7及以上
#### 2.1.1 yum方式安装
```
#配置yum源
wget https://dev.mysql.com/get/mysql57-community-release-el7-9.noarch.rpm
rpm -ivh mysql57-community-release-el7-9.noarch.rpm
	
#执行安装
yum -y install mysql-server mysql-client
	
#服务启动
systemctl start mysqld
	
#获取初始密码并修改
old_pass=`grep 'temporary password' /var/log/mysqld.log | awk '{print $NF}' | tail -n 1`
mysql -NBe "alter user USER() identified by 'Didi_am_678';" --connect-expired-password -uroot -p$old_pass
```
	
####2.1.2 rpm包方式安装（推荐此方式安装） [Mysql安装包下载地址](https://s3-gzpu.didistatic.com/knowsearch/mysql5.7.tar.gz)
```
#解压到指定目录
tar -zxf mysql5.7.tar.gz -C /tmp/
	
#执行安装
yum -y localinstall /tmp/libaio-*.rpm /tmp/mysql-*.rpm
	
#服务启动
systemctl start mysqld
	
#获取初始密码并修改
old_pass=`grep 'temporary password' /var/log/mysqld.log | awk '{print $NF}' | tail -n 1`
mysql -NBe "alter user USER() identified by 'Didi_am_678';" --connect-expired-password -uroot -p$old_pass
```
### 2.2 安装Nginx服务
 ###版本无要求 [Nginx安装包下载地址](https://s3-gzpu.didistatic.com/knowsearch/nginx-1.8.1.rpm)

```
#yum方式安装
yum -y install nginx
	
#rpm包方式安装
rpm -ivh nginx-1.8.1.rpm
	
#服务启动
systemctl start nginx

```
### 2.3 配置JAVA环境
 ###版本要求11 [下载地址](https://s3-gzpu.didistatic.com/knowsearch/jdk11.tar.gz)
	
	#解压到指定目录
	tar -zxf jdk11.tar.gz -C /usr/local/
	
	#更改目录名
	mv  /usr/local/jdk-11.0.2 /usr/local/java11
	
	#添加到环境变量
	echo "export JAVA_HOME=/usr/local/java11" >> ~/.bashrc
    echo "export CLASSPATH=/usr/java/java11/lib" >> ~/.bashrc
    echo "export PATH=\$JAVA_HOME/bin:\$PATH:\$HOME/bin" >> ~/.bashrc
    source ~/.bashrc
    
### 2.4 Elasticsearch元数据实例搭建
 ###Elasticsearch元数据集群来支持平台核心指标数据的存储，如集群维度指标、节点维度指标等 [Elasticsearch安装包下载地址](https://s3-gzpu.didistatic.com/pub/knowsearch/elasticsearch-v7.6.0.1400.tar.gz)

```
#创建ES数据存储目录
mkdir -p /data/es_data
	
#创建ES所属用户
useradd arius
	
#配置用户的打开文件数
echo "arius soft nofile 655350" >>/etc/security/limits.conf
echo "arius hard nofile 655350" >>/etc/security/limits.conf
echo "vm.max_map_count = 655360" >>/etc/sysctl.conf
sysctl -p
	
#解压安装包
tar -zxf elasticsearch-v7.6.0.1400.tar.gz -C /data/
	
#更改目录所属组
chown -R arius:arius /data/
	
#修改配置文件(参考一下配置)
vim /data/elasticsearch-v7.6.0.1400/config/elasticsearch.yml
cluster.name: logi-elasticsearch-meta
node.name: es-node1
node.master: true
node.data: true
path.data: /data/es_data
http.port: 8060
discovery.seed_hosts: ["127.0.0.1:9300"]
	
#修改内存配置
vim /data/elasticsearch-v7.6.0.1400/config/jvm.options
-Xms2g
-Xmx2g
	
#启动服务
su - arius
export JAVA_HOME=/usr/local/java11
sh /data/elasticsearch-v7.6.0.1400/control.sh start
	
	
#确认状态
su - arius
export JAVA_HOME=/usr/local/java11
sh /data/elasticsearch-v7.6.0.1400/control.sh status
```

### 2.5 安装grafana服务
   ###[安装包下载地址](https://s3-gzpu.didistatic.com/pub/knowsearch/grafana-8.5.9.tar.gz)

```
#解压安装包
tar -zxf grafana-8.5.9.tar.gz -C /data/

#启动服务
cd  /data/grafana-8.5.9/bin/
nohup ./grafana-server &

#添加数据源（其中url参数需要修改成真实的elasticsearch服务地址）
curl -X POST -H "Content-Type: application/json" "http://127.0.0.1:3000/api/datasources" -d '{"name":"elasticsearch-observability","type":"elasticsearch","url":"http://127.0.0.1:8060","access":"proxy","basicAuth":false,"database":"index_observability","jsonData":{"esVersion":"7.0.0","includeFrozen":false,"logLevelField":"","logMessageField":"","maxConcurrentShardRequests":5,"timeField":"logMills"},"readOnly":false}}' 

#导入大盘模版(示例是其中之一，七个模版都要导入)
cd  /data/grafana-8.5.9/template/
curl  -H "Content-Type: application/json" -X POST "http://127.0.0.1:3000/api/dashboards/db" -d @metrics-process.json

```   

	
## 3、KnowSearch服务部署
 ###依赖JAVA11、服务器可用内存大于4G[安装包下载地址](https://s3-gzpu.didistatic.com/knowsearch/KnowSearch-0.3.2.tar.gz)
### 3.1 admin模块

```
#解压安装包到指定目录
tar -zxf KnowSearch-0.3.2.tar.gz -C /data/
	
#修改启动脚本并加入systemd管理
cd  /data/KnowSearch-0.3.2/admin/
sed -i '#dir_home#/data/KnowSearch-0.3.2#g' control.sh arius-admin.service
cp arius-admin.service /usr/lib/systemd/system/
	
#创建相应的库和导入初始化数据
mysql -uroot -p -e "CREATE DATABASE IF NOT EXISTS knowsearch;"
mysql -uroot -p knowsearch < /data/KnowSearch-0.3.2/admin/init/init.sql
	
	
#修改配置文件
vim application-full.yml
	
#admin相关配置
admin.port.web: 8015 #admin服务端口
admin.contextPath: /admin/api 

#gateway相关配置
es.gateway.url: 127.0.0.1
es.gateway.port: 8200

#ES元数据集群相关配置
es.update.cluster.name: logi-elasticsearch-meta

#ES客户端相关配置
es.client.cluster.port: 8060

#mysql服务相关配置(配置文件中有两部分mysql相关的，都需要修改)
 url: jdbc:mariadb://127.0.0.1:3306/knowsearch?useUnicode=true&characterEncoding=utf8&jdbcCompliantTruncation=true&allowMultiQueries=true&useSSL=false&alwaysAutoGeneratedKeys=true&serverTimezone=GMT%2B8
      username: root
      password: pwd
      
#监控大盘ES地址
elasticsearch-address: 127.0.0.1
elasticsearch-port: 8060	


#启动服务
systemctl daemon-reload
systemctl start arius-admin
	
#服务启动成功后执行元数据导入脚本
sh /data/KnowSearch-0.3.2/admin/init/init_knowsearch_liunx.sh


#重启服务
systemctl restart arius-admin

``` 

### 3.2 gateway模块

``` 
#修改启动脚本并加入systemd管理
cd  /data/KnowSearch-0.3.2/gateway/
sed -i '#dir_home#/data/KnowSearch-0.3.2#g' control.sh arius-gateway.service
cp arius-gateway.service /usr/lib/systemd/system/
	
#修改配置文件
gateway.cluster.name=logi-elasticsearch-meta #ES集群名称
elasticsearch.admin.cluster.name=logi-elasticsearch-meta	arius.gateway.adminUrl=http://127.0.0.1:8015/	admin/api/ #arius-admin模块服务地址和端口
gateway.httpTransport.port=8200 #gateway监听端口
	
#启动服务
systemctl daemon-reload
systemctl start arius-gateway
	
#启动filebeats服务(IP地址和端口为ES集群地址)
sh filebeats_start.sh 
```	
   
### 3.3 前端代码部署及配置
 ###[Nginx配置文件下载地址](https://s3-gzpu.didistatic.com/knowsearch/nginx.conf)

```
#将配置文件放到etc目录
mv knowsearch_nginx.conf /etc/nginx/conf.d/knowsearch_nginx.conf
	
#修改配置文件(data目录是前端文件跟目录)
sed -i 's#c_path#data/KnowSearch-0.3.2#g' /etc/nginx/conf.d/knowsearch_nginx.conf
sed -i 's#ups_admin#127.0.0.1:8015#' /etc/nginx/conf.d/knowsearch_nginx.conf

#重启Nginx服务
systemctl restart nginx
```
 ###打开浏览器输入NginxIP地址测试，用户名密码（admin/admin123）

# 三、扩展服务部署

## 1、Fastdump部署（按需部署）
  ### fastdump提供数据迁移能力,需要将fastdump安装到一组ES集群中的所有节点，请求发送至安装有fastdump进程的master（暂不支持高可用）
  ### 准备一台已部署好Ansible的客户端机器（Centos7）,ansible安装方式可以参考分布式安装中的内容
### 1.1 下载安装包并解压

```
wget https://s3-gzpu.didistatic.com/pub/fastdump.tar.gz
tar -zxf fastdump.tar.gz 

```  

### 1.2 修改hosts文件

```
vim ./hosts.ini
[fastdump]
#第一例：替换成源ES实例IP地址、第二例：用户名、第三例：密码
#如果密码中有特殊符号请使用双引号将密码引起来
127.0.0.1 ansible_ssh_user= ansible_ssh_pass=
127.0.0.2 ansible_ssh_user= ansible_ssh_pass=
127.0.0.3 ansible_ssh_user= ansible_ssh_pass=
......

[fastdump:vars]
#安装目录，不需要带/号
install_path=home
#fastdump服务端口
port=8300
```

### 1.3 执行安装

```
ansible-playbook -i hosts.ini deploy_fastdump.yml

```

### 1.4 使用fastdump服务接口
 ###支持的接口：迁移任务提交、状态查询、读取速率修改、终止任务
 ###fastdump服务地址是安装在ES集群中Master节点上的fastdump进程是服务节点，访问该节点的  IP:PORT即可
 
#### 1.4.1 任务提交

```
#切换到api目录
cd ./api
#如果需要提交任务，需要先修改submit.json文件(根据文件注释修改)
vim submit.json
参考图1
#提交迁移任务
sh index_job.sh submit
参考图2
#执行成功后会在同级目录下生成一个taskid.txt文件来记录提交的任务ID(其他操作会用到)
```
图-1
![](https://s3-gzpu.didistatic.com/pub/knowsearch/image/image-1.png)
图-2
![](https://s3-gzpu.didistatic.com/pub/knowsearch/image/image-2.png)

#### 1.4.2 状态查询

```
sh index.job.sh status
```
![](https://s3-gzpu.didistatic.com/pub/knowsearch/image/image-3.png)

#### 1.4.3 修改读取速率

```
sh index_job.sh readrate

```
![](https://s3-gzpu.didistatic.com/pub/knowsearch/image/image-4.png)

#### 1.4.4 终止任务

```
sh index_job.sh stop
```
![](https://s3-gzpu.didistatic.com/pub/knowsearch/image/image-5.png)


## 2、Zeus部署(按需部署)
 ###zeus提供运维管控能力，如果不需要时可以不安装
 ###zeus-agent属于客户端，其他属于服务端

### 2.1 下载安装包并解压
```
wget https://s3-gzpu.didistatic.com/pub/knowsearch/zeus.tar.gz
tar -zxf zeus.tar.gz
```
### 2.2 导入初始化SQL

```
mysql -uroot -p -e "create database zeus"
mysql -uroot -p zeus < ./common/schema-all-new.sql

```

### 2.3 安装并启动各个模块

```
1. 安装zeus-web
#修改数据库地址和用户/密码
vim ./web/cfg.yml
mysql:
  addr: "user:passsword@tcp(127.0.0.1:3306)
 
#启动zeus-web服务
nohup ./web/zeus-web &

2. 安装zeus-scheduler
#修改数据库地址和用户/密码
vim ./scheduler/cfg.yml
mysql:
  addr: "user:passsword@tcp(127.0.0.1:3306)
#启动zeus-web服务
nohup ./scheduler/zeus-scheduler &

3. 安装zeus-exec
#启动zeus-exec服务
nohup ./exec/zeus-exec &

4. 安装zeus-agent
#修改数据库地址和用户/密码
vim ./web/cfg.yml
mysql:
  addr: "user:passsword@tcp(127.0.0.1:3306)
  
#启动zeus-ageng服务
nohup ./agent/zeus-agent &

5. 访问测试
打开浏览器访问IP:8010 ，用户名密码：root/Work5834 

```

 
# 四、构造测试数据
 ###脚本[下载地址](https://s3-gzpu.didistatic.com/pub/knowsearch/IndexTestTools.tar.gz)
## 1. 目录结构
  ```
  IndexTestTools
├── cluster_info.list
├── index_name.list
├── init_data #二级目录
│ ├── create_index.json
│ ├── create_template.json
│ ├── delete_index.json
│ ├── get_templateID.json
│ ├── index_data.json
│ ├── join_cluster.json
│ ├── query.json
│ └── template_data.json
├── IndexTestTools.sh
  ```
## 2. 文件作用说明(附json压缩转义方法)
 
![](https://s3-gzpu.didistatic.com/pub/knowsearch/image/文件作用.png)
附：转义工具：https://www.bejson.com/

压缩json
![](https://s3-gzpu.didistatic.com/pub/knowsearch/image/压缩前.png)
![](https://s3-gzpu.didistatic.com/pub/knowsearch/image/压缩后.png)
转义json
![](https://s3-gzpu-inter.didistatic.com/pub/knowsearch/image/转义.png)

## 3. 脚本使用流程
### 3.1 根据注释提示修改IndexTestTools.sh中的参数
![](https://s3-gzpu.didistatic.com/pub/knowsearch/image/参数.png)

### 3.2 自定义mapping或者setting 
 + 无特殊需求可以使用默认,即不需修改
 + 修改init_data目录下的对应的json文件（参考文件作用说明）

### 3.3 执行IndexTestTools.sh 

```
sh IndexTestTools.sh -h 获取使用方法
```
![](https://s3-gzpu.didistatic.com/pub/knowsearch/image/使用方法.png)

+ action参数释义：join 接入集群 ，create创建模版或索引，write写入数据，query查询数据，delete下线模版或索引，其他参考组合用法
+ type参数释义：操作类型，索引或模版
count参数释义：写入或查询时指定的次数，不指定时默认为1，-1表示持续
+ name参数为空时需要在IndexTestTools.sh脚本中指定cluster_name参数
+ action参数为write/query可以指定次数（不指定默认为1次）

### 3.4其他

```
sh IndexTestTools.sh -a=write -t=index -c=10 #表示往每个索引写入10次，指定为-1时 代表持续写入，在持续写入时可以使用nohup sh IndexTestTools.sh -a=write -c=-1 & 进行后台运行
sh IndexTestTools.sh -a=query -t=index -c=10 #表示索引查询10次，指定为-1时 代表持续查询，在持续查询时可以使用 nohup sh IndexTestTools.sh -a=query -c=-1 & 进行后台运行

组合用法1：接入集群+创建模版/索引+写入数据
sh IndexTestTools.sh -a=joinANDcreateANDwrite -t=index

组合用法2: 创建模版/索引+写入数据（集群需要已接入）
sh IndexTestTools.sh -a=createANDwrite -t=index
```
