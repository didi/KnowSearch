## 1. 项目背景
    随着Elasticsearch版本的高速迭代, 且各Elasticsearch企业内核版本繁多(2.x ~ 8.x), 运维的复杂度极高, 统一版本带来的升级工作显得尤为迫切, 
    Fastdump作为轻量级、易扩展、极快的离线迁移的工具应运而生，目前支持2.x 5.x 6.x 7.x 到 5.x 6.x 7.x 8.x 任意版本间的离线数据迁移。
    此外，针对Elasticsearch日常的运维能力，如Shard变更，离线数据快速迁移，数据压测等使用频次较高的能力，Fastdump一并支持。
## 2. 工程模块组成和划分
fastdump由5个主要的工程组成:

    fastdump-rest:        表现层，提供restful接口
    fastdump-common:      基础组件层，存放业务需要数据结构, 如Java POJO、公共工具方法、事件等
    fastdump-core         核心层，提供核心的迁移能力, 主要的业务逻辑实现, 如索引迁移、模板迁移等
    fastdump-persistence  数据层，负责对Lucene中的数据进行读写操作
    fastdump-adapter      适配层，提供多版本Lucene读写适配能力

名词定义:

    XXXSource             元数据解析端
    XXXReader             数据读取端
    XXXSinker             数据写入端
    
## 3. 如何使用
    
### a. 配置

    fastdump.httpTransport.port={}

    node.concurrent.Handle.shard.num={}

    index.bulk.thread.pool.size={}
    index.bulk.thread.pool.queue={}

    jvm.heap.mem.protect.percent.threshold={}
    single.bulk.max.doc.num={}

    min.limit.read.rate.percent={}

### b.打包
    mvn clean package -Dmaven.test.skip=true

### c.运行
    nohup java -Xmx1g -Xms1g -Xmn400m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./gc.hprof -jar fastdump-rest.jar --spring.config.location=./application.properties &
## 4.获取帮助
    邮箱至信到 linyunan_it@163.com