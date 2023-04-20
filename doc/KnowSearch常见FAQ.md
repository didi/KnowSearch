#  1.KnowSearch体验

# 1.1 如何零门槛体验KnowSearch观测能力？
<img src="./file/KnowSearch-ManagCluster.png"  style="zoom:50%;" />

# 1.2 如何零侵入体验GateWay的DSL查询模板的观测与管控能力

这里默认你已经是KnowSearch管理员，熟悉  [**应用、ES_USER、集群类型**] 平台基本概念，有疑问请查看[KnowSearch用户指南](./KnowSearch用户指南.md)中概念说明章节。

<img src="./file/KnowSearch-AppAcess.png"  style="zoom:50%;" />

这里默认你已经知悉GateWay的基本功能，熟读GateWay设计，熟悉其中的前置概念**[访问模式]**，零侵入体验GateWay，访问模式需要选择原生模式的ES_USER。

- 原生模式：会检验ES_USER，根据ES_USER指定的集群路由请求，所有请求都会转发给目标ES集群，除了要加上(ES_USER/校验码)外其他跟直接访问ES没区别

<img src="./file/KnowSearch-ESUser.png"  style="zoom:50%;" />

- 代码访问改造样例，在HTTP头域中增加ES_USER/ES_SECRET

```java
public void usingHttp() {
    String url = "http://10.88.128.149:30963/data-fd_es_gateway_stats_log/stats.log/_search?q=field:value";
    HttpGet get = new HttpGet(url);
    //在请求头中加入Authorization，并且将Token用base64加密 Token为ES_USER/ES_SECRET
    get.setHeader("Authorization", "Basic " + new BASE64Encoder().encode(String.format("%s:%s", ES_USER, ES_SECRET).getBytes("UTF-8")));
    CloseableHttpResponse response = null;
    try {
        response = httpClient.execute(get);//执行GET请求
    } catch (IOException e) {
        e.printStackTrace();
    }
    HttpEntity entity = response.getEntity();//获取结果
}
  
public void usingHttpSql() {
    String url = "http://10.88.128.149:30963/_sql";
    HttpPost httpPost = new HttpPost();
    //在请求头中加入Authorization，并且将Token用base64加密 Token为ES_USER/ES_SECRET
    httpPost.setHeader("Authorization", "Basic " + new BASE64Encoder().encode(String.format("%s:%s", ES_USER, ES_SECRET).getBytes("UTF-8")));
    httpPost.setURI(new URI(url)));
    StringEntity stringEntity = new StringEntity("select * from xxx", "UTF-8");
    httpPost.setEntity(stringEntity);
    CloseableHttpResponse response = null;
    try {
        response = httpClient.execute(httpPost);//执行POST请求
    } catch (IOException e) {
        e.printStackTrace();
    }
    HttpEntity entity = response.getEntity();//获取结果
    if (entity != null) {
       String result = EntityUtils.toString(entity);
       EntityUtils.consume(entity);
    }
}
```

- CURL调用样例

```java
curl -XGET "http://ES_USER:ES_SECRET@10.88.128.149:30963/{index}/_search?pretty"
curl -XPOST "http://ES_USER/ES_SECRET@10.88.128.149:30963/_sql" -d "select * from index limit 1"
curl -XPOST "http://ES_USER/ES_SECRET@10.88.128.149:30963/{index}/id/_update" -d'{"doc":{"phone":"18515010454", "send_time":"2020-02-05 01:00:29"}}'
```



# 2.索引写入

## 2.1 ES_USER级别限流和查询模板级别限流的是什么？

ES_USER级别限流是针对整个用户每秒最多能查询多少次，如果触发到ES_USER级别限流，会接收到 FlowLimitException 错误。查询模板级别限流是针对相同结构的查询语句每秒最多能查询多少次，如果触发到查询模板级别限流，会接收到 DslRateLimitException 错误，这里的限流值GateWay单实例生效。

## 2.2 mapping是什么？

- mapping是Lucence存储与索引字段的方式和规则，如某个字段的数据类型、默认值、分析器、是否被索引等。
- 参考官方文档：https://www.elastic.co/guide/en/elasticsearch/reference/master/mapping.html

## 2.3 数据已经接放ES，现在想增加一些新的字段 ，该怎么做？

ES 会动态判断该字段类型并建立索引，并且会按照该字段首次出现的类型来定义该字段。例如: 新增字段 {"name":"dididatabus"} es会判断为 string 类型。

## 2.4 时间字段起到什么作用？

时间字段主要用于时间分区的作用，为了防止索引膨胀，ES 会根据时间字段写入索引名称，分区主要有按天、按月两种，例如（indexname_2018-08-08、indexname_201808），默认情况下保存天数超过一个月会按月建立索引，其它时间会按天进行建立。



# 3.索引查询

## 3.1 DSL查询语法如何编写？

参考官网手册：https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl.html

## 3.2 为什么不能查询排序在10000条以后的数据？

很多用户都有疑问，ES 为什么不能查询 10000 条之后的数据。了解这个问题需要了解到es的分布式原理。ES 一个索引是对应到多个shard（数据分片），对于带size的查询，es是在每个shard上都会返回size条数据，如果索引有 100 个 shard，那就是得先汇聚 100*size 条数据，然后再排序选出size条数据。所以分页太深，ES 汇聚的数据量太大，会导致 ES 节点不堪重负。如果用户想查看深分页的数据，有两种方式，一种是带上 where 条件，让10000条之后的数据能通过查询条件，在前面被查出来。另一种是针对获取大量数据的场景，使用 ES 的 scroll 方式，可以批量捞取全量的数据。不过官方不再建议使用scroll API进行深度分页。如果要分页检索超过 Top 10,000+ 结果时，推荐使用：PIT + search_after。

## 3.3 关于分页查询

### 3.3.1 主要有三种分页查询方式

- From + Size 查询：优点是支持随机翻页，受制于 max_result_window 设置，不能无限制翻页，存在深度翻页问题，越往后翻页越慢，深分页不推荐使用；
- Search After 查询：查询本质是使用前一页中的一组排序值来检索匹配的下一页，并且不严格受制于 max_result_window，可以无限制往后翻页，但是不支持随机翻页；
- Scroll 查询：相比于 From + Size 和 search_after 返回一页数据，Scroll API 可用于从单个搜索请求中检索大量结果（甚至所有结果），其方式与传统数据库中游标（cursor）类似，支持全量遍历，但是响应时间是非实时 ，并且保留上下文需要足够的堆内存空间。

### 3.3.2 分别的使用场景

- From + Size 查询：非常适合小型数据集或者大数据集返回 Top N（N <= 10000）结果集的业务场景，支持随机跳转分页的业务场景；

- Search After 查询：不支持随机翻页，更适合手机端应用的场景（一直往下滑动刷新数据的场景）；

- Scroll 查询：全量或数据量很大时遍历结果数据，而非分页查询，并且对实时性要求不高的场景。关于具体的使用方式请见官方文档。

# 4.DSL无法切换集群
修改nginx.conf配置
```java
   location ~ ^/api/es/gateway/ {
   
      rewrite ^/api/es/gateway/(.*)$ /$1 break; # 路径规则重写
      
      proxy_pass http://127.0.0.1:8060;  #这个地址需要改成网关地址
      
  }
  ```
