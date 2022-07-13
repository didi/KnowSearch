package com.didichuxing.datachannel.arius.admin.common.bean.common;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NodeAllocationInfo {
    /**
     * 节点上的分片数目
     */
    @JSONField(name = "shards")
    private String shardsNumber;

    /**
     * 节点上索引index占用的空间大小
     */
    @JSONField(name = "disk.indices")
    private String diskIndicesSize;

    /**
     * 节点上已用磁盘空间
     */
    @JSONField(name = "disk.used")
    private String usedDiskSize;

    /**
     * 节点上可用磁盘空间
     */
    @JSONField(name = "disk.avail")
    private String canUseDiskSize;

    /**
     * 节点上磁盘空间总量
     */
    @JSONField(name = "disk.total")
    private String totalDiskSize;

    /**
     * 节点上磁盘已使用百分比
     */
    @JSONField(name = "disk.percent")
    private String usedDiskPercent;

    /**
     * 节点主机地址
     */
    @JSONField(name = "host")
    private String host;

    /**
     * 节点ip
     */
    @JSONField(name = "ip")
    private String ip;

    /**
     * 节点名称
     */
    @JSONField(name = "node")
    private String node;
}
