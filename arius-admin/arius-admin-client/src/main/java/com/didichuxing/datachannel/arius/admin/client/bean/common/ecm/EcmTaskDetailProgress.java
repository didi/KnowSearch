package com.didichuxing.datachannel.arius.admin.client.bean.common.ecm;

import com.google.common.collect.Maps;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EcmTaskDetailProgress {
    /**
     * 执行进度
     */
    private Long                             percent;

    /**
     * 成功的节点个数
     */
    private Long                             success;

    /**
     * 失败的节点个数
     */
    private Long                             failed;

    /**
     * 执行中的节点个数
     */
    private Long                             creating;

    /**
     * 等待中的节点个数
     */
    private Long                             waiting;

    /**
     * 忽略的节点个数
     */
    private Long                             ignore;

    /**
     * 取消的节点个数
     */
    private Long                             cancel;

    /**
     * 节点总个数
     */
    private Long                             sum;

    /**
     * 任务状态
     */
    @ApiModelProperty("任务状态")
    private String                           status;

    /**
     * 工单类型
     */
    @ApiModelProperty("工单类型  1 集群新增  2 集群扩容 3 集群缩容 4 集群重启 5 集群升级")
    private Integer                          orderType;

    /**
     * 全部节点
     */
    private Map<String, List<EcmTaskDetail>> roleNameTaskDetailMap;

    public Long updatePercent() {
        if (this.sum == null || this.sum.equals(0L)) {
            this.percent = null;
            return this.percent;
        }

        Long finished = 0L;
        if (this.failed != null) {
            finished += this.failed;
        }
        if (this.success != null) {
            finished += this.success;
        }
        if (this.ignore != null) {
            finished += this.ignore;
        }

        this.percent = finished * 10000 / this.sum;
        return this.percent;
    }

    public static EcmTaskDetailProgress newFieldInitializedInstance() {
        EcmTaskDetailProgress ecmTaskDetailProgress = new EcmTaskDetailProgress();
        ecmTaskDetailProgress.setPercent(0L);
        ecmTaskDetailProgress.setSuccess(0L);
        ecmTaskDetailProgress.setFailed(0L);
        ecmTaskDetailProgress.setCreating(0L);
        ecmTaskDetailProgress.setWaiting(0L);
        ecmTaskDetailProgress.setIgnore(0L);
        ecmTaskDetailProgress.setCancel(0L);
        ecmTaskDetailProgress.setSum(0L);
        ecmTaskDetailProgress.setStatus("");
        ecmTaskDetailProgress.setOrderType(null);
        ecmTaskDetailProgress.setRoleNameTaskDetailMap(Maps.newHashMap());
        return ecmTaskDetailProgress;
    }
}
