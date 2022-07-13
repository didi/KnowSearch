package com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.dashboard;

import com.didichuxing.datachannel.arius.admin.common.util.CommonUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by linyunan on 3/14/22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClusterPhyHealthMetrics {
    /**
     * 当前时间
     */
    private Long    timestamp;

    /**
     * 总物理集群个数
     */
    private Integer totalNum;

    /**
     * green状态集群数
     */
    private Integer greenNum;

    /**
     * yellow状态集群数
     */
    private Integer yellowNum;

    /**
     * red状态集群数
     */
    private Integer redNum;

    /**
     * 未知状态集群数
     */
    private Integer unknownNum;

    /**
     * yellow状态集群名称列表
     */
    private String  yellowClusterListStr;

    /**
     * red状态集群名称列表
     */
    private String  redClusterListStr;

    /**
     * 未知状态集群名称列表
     */
    private String  unknownClusterListStr;

    /**
     * green状态集群百分比
     */
    private Double  greenPercent;

    /**
     * yellow状态集群百分比
     */
    private Double  yellowPercent;

    /**
     * red状态集群百分比
     */
    private Double  redPercent;

    /**
     * 未知状态集群百分比
     */
    private Double  unknownPercent;

    public void computePercent() {
        if (null == totalNum) {
            return;
        }

        this.greenPercent = CommonUtils.divideIntAndFormatDouble(greenNum, totalNum, 5, 100);
        this.yellowPercent = CommonUtils.divideIntAndFormatDouble(yellowNum, totalNum, 5, 100);
        this.redPercent = CommonUtils.divideIntAndFormatDouble(redNum, totalNum, 5, 100);
        this.unknownPercent = CommonUtils.divideIntAndFormatDouble(unknownNum, totalNum, 5, 100);
    }
}
