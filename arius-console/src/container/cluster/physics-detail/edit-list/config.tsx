import { number } from "echarts";

const percent = "%";
const input = "input";
const select = "select";

const isInt = (val: string) => {
  // 整数
  const intReg = /^\d{1,}$/;
  return intReg.test(val);
};

const isTwoDecimal = (val: string) => {
  // 两位小数
  const twoDecimalReg = /^\d{1,}\.\d{1,2}$/;
  return twoDecimalReg.test(val);
};

const maxEqual0AndMinEqual100 = (val: string) => {
  const num = Number(val);
  return num >= 0 && num <= 100;
};

const max1AndMin100 = (val: string) => {
  const num = Number(val);
  return num > 1 && num < 100;
};

// node_concurrent_incoming_recoveries
const maxEqual0AndMin2147483647 = (val: string) => {
  const num = Number(val);
  return num >= 0 && num < 2147483647;
};

const maxEqualNegative1AndMin2147483647 = (val: string) => {
  const num = Number(val);
  return num >= -1 && num < 2147483647;
};

const max0 = (val: string) => {
  const num = Number(val);
  return num > 0;
};

const maxEqual0 = (val: string) => {
  const num = Number(val);
  return num >= 0;
};
const maxEqual0AndMinEqual120 = (val: string) => {
  const num = Number(val);
  return num >= 0 && num <= 120;
};

const max1000 = (val: string) => {
  const num = Number(val);
  return num > 1000;
};

// 百分比
const checkPercent = (val) => {
  if ((isInt(val) || isTwoDecimal(val)) && maxEqual0AndMinEqual100(val)) {
    return "";
  }
  return "请输入两位小数以内的数字且大于等于0小于等于100";
};

// 效验 overhead
const checkIntTwoDecimalMax1AndMin100 = (val) => {
  if ((isInt(val) || isTwoDecimal(val)) && max1AndMin100(val)) {
    return "";
  }
  return "请输入两位小数以内的数字且大于1小于等于100";
};

// node_concurrent_incoming_recoveries  效验
const checkIntMaxEqual0AndMin2147483647 = (val) => {
  if (isInt(val) && maxEqual0AndMin2147483647(val)) {
    return "";
  }
  return "请输入一个整数且大于等于0小于2147483647";
};

// rebalance
const checkIntMaxEqualNegative1AndMin2147483647 = (val) => {
  if (isInt(val) && maxEqualNegative1AndMin2147483647(val)) {
    return "";
  }
  return "请输入两位小数以内的数字且大于等于-1小于2147483647";
};

// balance
const checkIntTwoDecimalMaxEqual0 = (val) => {
  if ((isInt(val) || isTwoDecimal(val)) && maxEqual0(val)) {
    return "";
  }
  return "请输入两位小数以内的数字且大于等于0";
};

const checkMax1000 = (val) => {
  if (isInt(val) && max1000(val)) {
    return "";
  }
  return "请输入一个整数且大于1000";
};

const checkInt = (val) => {
  if (isInt(val)) {
    return "";
  }
  return "请输入一个整数";
};

// 秒
const checkS = (val) => {
  if (isInt(val) && maxEqual0AndMinEqual120(val)) {
    return "";
  }
  return "请输入一个整数大于等于0且小于等于120";
};

// enable 下拉列表
const enableSelectList = ["all", "primaries", "new_primaries", "none"].map((item) => ({ name: item, value: item }));

// rebalance
const rebalanceSelectList = ["all", "primaries", "replicas", "none"].map((item) => ({ name: item, value: item }));

// boolean 下拉列表
const booleanSelectList = ["true", "false"].map((item) => ({
  name: item,
  value: item,
}));

// all write 下拉列表
const allWriteList = ["all", "write"].map((item) => ({
  name: item,
  value: item,
}));

// rebalanceList
const allow_rebalanceSelectList = ["always", "indices_primaries_active", "indices_all_active"].map((item) => ({ name: item, value: item }));

export const clusterSetting = {
  BREAKER: {
    "indices.breaker.total.limit": {
      info: "基础熔断器内存限制。",
      unit: percent,
      type: input,
      check: checkPercent,
      confirmMessage: "限制值不应该设置过低，有可能对程序处理性能造成影响，建议设置在40%",
    },
    "indices.breaker.fielddata.limit": {
      info: "fielddata熔断器内存限制。",
      unit: percent,
      type: input,
      check: checkPercent,
    },
    "indices.breaker.fielddata.overhead": {
      info: "fielddata内存限制系数（估算值：系数乘以真实值）。",
      type: input,
      check: checkIntTwoDecimalMax1AndMin100,
      confirmMessage: "限制值不应设置过低，有可能造成集群熔断，请谨慎操作，建议采用默认值",
    },
    "indices.breaker.request.limit": {
      info: "request熔断器的限制。",
      unit: percent,
      type: input,
      check: checkPercent,
    },
    "indices.breaker.request.overhead": {
      info: "request内存限制系数（估算值：系数乘以真实值）。",
      type: input,
      check: checkIntTwoDecimalMax1AndMin100,
    },
    "network.breaker.inflight_requests.limit": {
      info: "inflight_requests请求熔断器内存限制。",
      unit: percent,
      type: input,
      check: checkPercent,
    },
    "network.breaker.inflight_requests.overhead": {
      info: "inflight_requests内存限制系数。",
      type: input,
      check: checkIntTwoDecimalMax1AndMin100,
    },
  },
  ROUTING: {
    "cluster.routing.allocation.enable": {
      info: "为特定类型的分片启用或禁用分配。",
      type: select,
      selectList: enableSelectList,
      confirmMessage: "请谨慎修改，否则可能导致索引分片无法分配",
    },
    "cluster.routing.allocation.node_concurrent_incoming_recoveries": {
      info: "一个节点上允许发生多少并发传入分片恢复。",
      type: input,
      check: checkIntMaxEqual0AndMin2147483647,
    },
    "cluster.routing.allocation.node_concurrent_outgoing_recoveries": {
      info: "一个节点上允许发生多少并发传出分片恢复。",
      type: input,
      check: checkIntMaxEqual0AndMin2147483647,
    },
    "cluster.routing.allocation.node_concurrent_recoveries": {
      info: "设置 cluster.routing.allocation.node_concurrent_incoming_recoveries 和 cluster.routing.allocation.node_concurrent_outgoing_recoveries 的快捷方式。",
      type: input,
      check: checkInt,
    },
    "cluster.routing.allocation.node_initial_primaries_recoveries": {
      info: "初始化主分配恢复并发度。",
      type: input,
      check: checkIntMaxEqual0AndMin2147483647,
    },
    "cluster.routing.allocation.same_shard.host": {
      info: "是否允许在单个主机上分配同一分片的多个副本。",
      type: select,
      selectList: booleanSelectList,
    },
    "cluster.routing.rebalance.enable": {
      info: "启用或禁用特定类型分片的重新平衡。",
      type: select,
      selectList: rebalanceSelectList,
    },
    "cluster.routing.allocation.allow_rebalance": {
      info: "指定何时允许分片重新平衡。\nalways：一直允许重新平衡；\nindices_primaries_active：只有当集群中的所有的 primaries 被分配；\nindices_all_active：只有当集群中的所有分片（primaries and replicas）被分配（默认）。",
      type: select,
      selectList: allow_rebalanceSelectList,
    },
    "cluster.routing.allocation.cluster_concurrent_rebalance": {
      info: "允许控制多个并发分片重新平衡在所允许的集群范围，默认为 2 。",
      type: input,
      check: checkIntMaxEqualNegative1AndMin2147483647,
    },
    "cluster.routing.allocation.balance.shard": {
      info: "定义节点上分配的分片总数的权重因子（float），默认为 0.45f ，提高这个值会增加集群中所有节点的分片数量均衡的趋势。",
      type: input,
      check: checkIntTwoDecimalMaxEqual0,
    },
    "cluster.routing.allocation.balance.index": {
      info: "定义在特定节点上分配的每个索引的分片数的权重因子（float），默认为 0.55f ，提高这个值会增加集群中所有节点每个索引的分片数量均衡的趋势。",
      type: input,
      check: checkIntTwoDecimalMaxEqual0,
    },
    "cluster.routing.allocation.balance.threshold": {
      info: "应执行的操作的最小优化值（非负浮点），默认为 1.0f，提高这个值将导致集群在优化分片平衡方面不那么积极。",
      type: input,
      check: checkIntTwoDecimalMaxEqual0,
    },
    "cluster.routing.allocation.disk.threshold_enabled": {
      info: "分片分配时是否考虑磁盘因素。",
      type: select,
      selectList: booleanSelectList,
    },
    "cluster.routing.allocation.disk.watermark.low": {
      info: "磁盘使用的限制（low），默认值是85%，意味着当一个node的磁盘使用率达到了85%，那么就不会再往这个node上面分配shard了。",
      unit: percent,
      type: input,
      check: checkPercent,
    },
    "cluster.routing.allocation.disk.watermark.high": {
      info: "磁盘使用的限制（high），默认是90%，当某个node的磁盘使用率达到90%的时候，elasticsearch就会考虑将一部分shard从这个node上面迁移到别的node上面。",
      unit: percent,
      type: input,
      check: checkPercent,
    },
    "cluster.routing.allocation.disk.watermark.flood_stage": {
      info: "磁盘使用的限制（danger），默认值是95%，当某个node的磁盘使用达到这个水平以后，这个node上的shard对应的index都会被设置为 index.blocks.read_only_allow_delete，也就是只允许读操作和删除操作，这是es为了应对集群崩溃不得不采取的一个操作，而且在cluster中的node解除磁盘风险后需要手动进行只读设置的解除。",
      unit: percent,
      type: input,
      check: checkPercent,
    },
    "cluster.routing.allocation.total_shards_per_node": {
      info: "控制一个节点上最多可以分配多少个分片。",
      type: input,
      check: checkMax1000,
      confirmMessage: "当集群索引内容较多时，建议不要减少每个节点上的总分片数目，建议保持在1000以上，-1 表示不限制",
    },
    "cluster.routing.allocation.awareness.attributes": {
      info: "机架感知，主副分片不同机架分布。",
      type: select,
      mode: "multiple",
      selectList: [],
      confirmMessage: "设置之后对于集群主副分片会分配到对应属性下的节点上，请谨慎操作",
    },
    "indices.recovery.max_bytes_per_sec": {
      info: "每个节点分片迁移的带宽限制",
      type: input,
      mode: "multiple",
    },
  },
  ZEN: {
    "discovery.zen.commit_timeout": {
      info: "元数据预更新超时时间。",
      type: input,
      unit: "s",
      check: checkS,
    },
    "discovery.zen.minimum_master_nodes": {
      info: "设置了最少有多少个备选主节点才能开始选举。",
      type: input,
      check: checkInt,
    },
    "discovery.zen.no_master_block": {
      info: "设置没有主节点时限制的操作。all：所有操作均不可进行，读写、包括集群状态的读写api，例如获得索引配置（index settings），putMapping，和集群状态（cluster state）api；write：默认，写操作被拒绝执行，基于最后一次已知的正常的集群状态可读，这也许会读取到已过时的数据。",
      type: select,
      selectList: allWriteList,
    },
    "discovery.zen.publish_diff.enable": {
      info: "是否启用集群状态增量更新。",
      type: select,
      selectList: booleanSelectList,
    },
    "discovery.zen.publish_timeout": {
      info: "元数据更新超时时间。",
      type: input,
      unit: "s",
      check: checkS,
    },
  },
};
