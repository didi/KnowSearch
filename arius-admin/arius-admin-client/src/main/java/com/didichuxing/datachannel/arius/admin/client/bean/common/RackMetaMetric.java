package com.didichuxing.datachannel.arius.admin.client.bean.common;

import lombok.Data;
import lombok.ToString;

/**
 * @author d06679
 * @date 2019-06-24
 */
@Data
@ToString
public class RackMetaMetric {

    /***************************************** ADMIN指标 ****************************************************/
    /**
     * 集群
     */
    private String  cluster;

    /**
     * rack名字
     */
    private String  name;

    /***************************************** ECM指标 ****************************************************/

    /**
     * 节点个数
     */
    private Integer nodeCount;

    /**
     * cpu个数
     */
    private Integer cpuCount;

    /**
     * 磁盘空间
     */
    private Double  totalDiskG;

    /***************************************** AMS指标 ****************************************************/

    /**
     * 磁盘空闲空间
     */
    private Double  diskFreeG;

    /**
     * cpu使用率
     */
    private Double  cpuUsedPercent;

}
