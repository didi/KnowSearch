KnowSearch团队结合滴滴内部对ES的使用经验，总结了一些最佳实践。主要分为索引生成、mapping设置、查询和写入优化等。

# 1.索引生成

目前 KnowSearch 上创建的索引模板建议采用时间进行分区， 一般有按天、按月、按年创建，用户申请的索引为逻辑索引，实际存储的为物理索引。
如：abc 为用户申请时的索引模板名称，选择按月保存，实际存储的索引为 abc_201812、abc_201901。
按时间分区创建可以避免单个索引过大、提供索引快速恢复、历史索引删除等功能。

## 1.1 禁止自带后缀索引写入

ES 写入文档的过程中，如果写入的索引在集群中没有创建，则会在写入的时候创建该索引。如果每次写入的索引都不一样，则会频繁创建索引，索引过多会导致 ES 集群元数据膨胀，内存消耗大，恢复速度慢等一系列问题，频繁创建索引还会阻塞 master 节点，带来稳定性和性能问题。

## 1.2 禁止按天创建长期保存的索引

KnowSearch 对于保存周期在90天以上的索引，建议不按天分区创建，必须按月分区创建。长期保存索引，按天创建会导致集群中索引数量膨胀，间接导致集群 shard 过多，元数据膨胀，影响集群稳定性，拖慢集群重启恢复速度。

## 1.3 不建议索引不分区

KnowSearch 建议索引实际保存时按照业务时间进行分区，不建议不分区。不分区索引随着数据写入的增加，超过预估容量之后会导致写入变慢，索引扩容迁移恢复均有很多问题，影响业务使用。

# 2.Mapping设置

## 2.1 禁止日志场景下对message做全量清洗

日志场景下 message 用于保存日志的原文，在标准kv格式 message 场景下，一般可以对message做特定字段的清洗用于检索，如：traceId、actionName。

禁止对 message 不指定字段清洗而是全量清洗，即对 message 的所有字段都清洗之后在 ES 中建检索，由于 message 字段的不确定性，全量清洗情况下会导致相应的 mapping 膨胀，导致性能和稳定性问题。

## 2.2 不建议日志场景下对message做分词检索

日志场景下 message 用于保存日志的原文，一般不会对 message 在 ES 中做分词检索，即便是非标准kv格式的 message，不能进行清洗也不建议对 message 做分词检索。

由于 message 字段可能很长，内容也不固定，对 message 做分词检索会耗费大量存储空间。
针对这种场景，建议打印日志时遵循滴滴日志规范，之后才通过清洗所需要的字段来检索。

## 2.3 不建议随意增加mapping字段

ES 的free schema 特性支持动态修改mapping的能力，用户数据写入到 ES 场景下，将数据以json 的格式写入 ES 中，可以更加json的属性自动的设置 mapping 信息。但是如果写入的json的属性不固定，随意增加，会导致 ES 中索引 mapping 的属性字段越来越多。

建议用户在自己写入数据到ES中时，要定义好自己的 mapping 信息，在动态 mapping 特性下，管理好写入ES的数据类型。

## 2.4 建议对字段按需做分词检索

写入 ES 的数据，建议用户根据实际需要对字段做分词检索，对于没有检索需要的字段建议在mappinbg 不设置分词和全文检索，对应字段的”index“设置为no，这样可以节省大量成本。

## 2.5 建议对字段按需做聚合
写入 ES 的数据，建议用户根据实际需要对字段做聚合，对于没有聚合需要的字段建议在mapping 中 "doc_values" 设置为false，这样可以节省大量成本。

## 2.6 字段值为数值时索引类型的选择
ES的动态映射是根据字段值内容进行映射，例如status=1会自动映射为long类型，name="es"会自动映射为keyword类型。

ES中不同的索引类型底层使用不同的数据结构。数值类型long，integer使用BKD作为底层存储数据结构，数值类型适合范围查询但不适合key=value的精确查询，如果对数值类型的字段进行精确查询会**消耗大量CPU进行bitset构建**。字符串类型keyword使用FST和SkipList作为底层存储数据结构，字符串类型适合key=value的精确查询，符合过滤条件的文档id集合已经存储到倒排链表中不需要实时构建bitset。

**特别注意：mysql字段中用于存储可枚举值的数值类型，像字段名为status、xxx_type应该使用keyword作为ES的索引类型，而不是long类型**。

# 3.查询优化

查询可以先参考ES官网上的查询优化建议： https://www.elastic.co/guide/en/elasticsearch/reference/current/tune-for-search-speed.html

## 3.1 不建议带\*查询

ES 搜索时，支持索引的前缀带\*匹配，如：abc\*，会匹配的所有 abc 开头的索引，abc_201501~ abc_201901都会被查询到，如果再带上复杂查询条件，则会进行全表扫描，很容易把 ES 查挂。

所以在查询的过程中不建议带\*查询，直接\*查询更是被 KnowSearch 网关直接禁止，建议在查询的过程中使用实际索引名称，如：select * from abc_201901; select * from abc_201901, abc_201902;

## 3.2 不建议复杂的聚合查询

ES 的聚合查询需要在内存中将符合条件的文档进行排序或者聚合。在数据量非常大，聚合查询又很复杂的情况下，需要耗费大量内存，很容易直接把 ES 的内存撑爆。

KnowSearch Gateway 和高版本的ES对聚合查询的内存都会做监控，如果发现耗费内存过大会直接禁止查询。

请谨慎使用 ES 的 aggs(aggregations) 查询，对应sql是group by关键字，尤其是cardinality和script查询，对内存消耗大，容易出现性能问题。

## 3.3 不建议查询命中过多的数据
ES 每次查询都会返回该次查询的全部命中结果，这会导致需要命中全部的数据，有些情况下还要对这些数据进行打分排序，造成整体性能缓慢。

比如不带过滤条件的查询，查询列表等，如何查询列表还带上排序条件，性能会下降的更加严重。这些查询即使加上 limit 条件，也会很慢，limit 只是返回的数据加了限制，并不影响查询过程。

## 3.4 建议查询的条件中带上路由字段
ES 文档在写入的时候可以指定 routing 字段，查询的时候在查询条件中带上 routing，提升查询速度。
如：可以使用 abc.id 作为 abc 索引的 routing 字段，查询条件中带 abc.id，select * from abc_201901 where abc.id=123 and abc.pasanger_id=123 group by abc.driver_id;

ES 在查询时会根据 routing 字段先定位到具体的 shard，然后在该 shard 上做具体的过滤和聚合，避免遍历索引所在的所有 shard，提升查询效率。

## 3.5 不建议在大容量索引上进行复杂查询
目前有些索引容量特别大，甚至超过了千亿条记录，在这样的索引上查询时，需要特别注意查询的复杂程度，过滤条件命中的数据很多时，会查询的特别慢，甚至超过默认超时时间，而且这样的索引shard会非常多，一个查询会需要很多资源开销。

大容量索引的查询请注意查询语句的优化，选择最合理的查询方式，主要原则是尽量缩小查询范围。

## 3.6 查询语句建议选择合适的排序方式，默认建议按_doc排序
ES 默认按照 score 排序，会对每条记录计算分数，按分数从高到底排序。如果对排序没有依赖的用户，可以使用按内部 _doc 顺序排序，可以避免打分环节。按其他字段排序的话，查询会更慢，每条记录会去 DocValues 中获取记录对应的排序字段值，该次查询可能触发 IO 操作，造成更慢的性能。

按照_doc排序的DSL和SQL语法如下：

DSL：

```
"sort": [
  {
   "_doc": {
    "order": "asc"
   }
  }
 ]
```

SQL：ORDER BY _doc

## 3.7 请谨慎使用 post_filter 关键字

ES 支持 post_filter 查询，是为了将 query 和 aggs 的结果分开，实际使用的需求并不大，很多人把它误用为跟 query 一样表现，在2.3.3版本，最外层的 filter 关键字还映射成 post_filter 查询，可能对性能造成很严重的影响。

post_filter相关内容可参见文档：https://www.elastic.co/guide/en/elasticsearch/reference/current/search-request-post-filter.html

## 3.8 关于timeout的设置

1、ES 查询参数中的 timeout 关键字，作用于单个 shard 的超时。即单个 shard 的查询超过timeout 时间，则直接返回该时间段内的查询结果。这时候的结果可能是不完整的，用户需要关心查询是否触发了timeout。在查询的返回结果中，timed_out 告知了用户是否超时，false表示没有超时。true表示超时，此时需要注意查询结果是否不完整。如下示例，timed_out=false，表示查询没有超时。

```
{
  "took": 9,
  "timed_out": false,
  "_shards": {
    "total": 12,
    "successful": 12,
    "failed": 0
  },
  "hits": {
	...
  }
}
```

ES 默认的 timeout 一般是5s-10s，在进行大规模查询的时候，可能触发 timeout，用户可以在查询参数中指定 timeout，具体见：https://www.elastic.co/guide/en/elasticsearch/reference/current/search-uri-request.html ，sql接口也支持timeout设置。

2、KnowSearch 的 Gateway支持 timeout 设置，该 timeout 参数表示请求ES的超时时间，超过timeout 值，则直接返回客户端 timeout。Gateway 的具体参数名称为：socket_timeout ，最大为120s。

3、客户端自行设置的 timeout，这个根据每个语言的sdk，用户可以自行设置，该 timeout 只作用于客户端，但是 Gateway 以及 ES 内部可能还未超时，还会继续计算，所以客户端超时请谨慎设置。建议根据查询复杂程度设置相应的超时时间。

## 3.9 关于search结果是否完整的判断

ES search返回结果如下：

```
{
  "took": 9,
  "timed_out": false,
  "_shards": {
    "total": 12,
    "successful": 12,
    "failed": 0
  },
  "hits": {
	...
  }
}
```

要确认search结果正常，除了返回如上的查询结果，还需要确认以下两点：
1、timed_out是否为false，关于timed_out说明，请参见：3.8 关于timeout的设置。
2、_shards里结果的failed是否为0，如果不为0，说明有部分shard查询失败，不为0时，会附带上异常说明。

## 3.10 关于terms查询个数

建议terms查询语法如下，其中Value的个数建议在100个以内。

## 3.11 关于wildcard查询建议

1、限制 wildcard 字符串长度
建议 wildcard 查询的字符串字符串长度进行限制，不能超过20个字。

2、限制 wildcard 不要前缀传递\*号。
前缀传递\*号，ES 会遍历全部索引匹配是否命中，这种方式效率非常低，消耗资源非常大，难以利用ES的高效索引查询，建议不要前缀带*查询。

3、考虑用全文检索替代 wildcard 查询
ES支持全文检索，查询效率会 wildcard 查询效率高出非常多。全文检索的字段类型介绍见：https://www.elastic.co/guide/en/elasticsearch/reference/current/text.html

## 3.12 关于滚动(scroll)查询建议

由于滚动 (scroll) 查询时查询上下文 (SearchContext) 需要等到 scroll 完成或 scroll 超时才释放查询上下文占用 ES 内存资源，因此禁止将 scroll 超时时间设置得非常大(大于5分钟)，建议在1分钟左右或者在 scroll 用完后主动调用 clear 接口释放占用资源。

## 3.13 精确匹配字段类型的建议
精确匹配字段类型的建议设置成 keyword，范围查询字段类型设置成number(integer/long/double/float等)。

# 4.写入优化

查询可以先参考ES官网上的写入优化建议： https://www.elastic.co/guide/en/elasticsearch/reference/current/tune-for-indexing-speed.html

## 4.1 用bulk写入
建议用bulk写入，一批bulk的数量不易太多，需要根据服务器配置以及doc大小给出一个合理的值，如不清楚可以暂时给个1000或者doc到2MB

## 4.2 多线程写入
多线程能提高写入的性能，但线程不易太多

## 4.3 唯一id
不需要唯一id的，写入不要指定唯一id，让es自动生成id，这样可以避免version和id的检查，提升性能

## 4.4 translog
极端情况下比如断电允许丢点数据的，建议translog同步刷磁盘改异步

## 4.5 刷新时间和写入buffer
适当可以调大refresh_time和写入buffer