package com.didichuxing.datachannel.arius.admin.common.bean.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ClusterLogicStats {
    //文档数量
    private Long docCount;
    //已使用存储大小
    private Long usageFsBytes;
    //剩余存储大小
    private Long freeInBytes;
    //总存储大小
    private Long totalFsBytes;
    // cpu使用率
    private Double cpuUsedPresent;
    //节点主机地址
    private String host;
    //节点名称
    private String nodeName;
}
