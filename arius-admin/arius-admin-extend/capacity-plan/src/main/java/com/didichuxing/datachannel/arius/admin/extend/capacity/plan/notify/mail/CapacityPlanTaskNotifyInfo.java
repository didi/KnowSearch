package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.notify.mail;

import java.util.Date;

import com.didichuxing.datachannel.arius.admin.common.util.AriusDateUtils;
import com.didichuxing.datachannel.arius.admin.core.notify.NotifyInfo;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.common.CapacityPlanRegionContext;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.constant.CapacityPlanRegionTaskEnum;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.constant.CapacityPlanRegionTaskStatusEnum;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.constant.CapacityPlanRegionTaskTypeEnum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 * @date 2019-07-22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CapacityPlanTaskNotifyInfo implements NotifyInfo {

    private CapacityPlanRegionContext        regionPlanContext;

    private CapacityPlanRegionTaskTypeEnum   taskTypeEnum;

    private String                           deltaRacks;

    private CapacityPlanRegionTaskStatusEnum taskStatusEnum;

    @Override
    public String getBizId() {
        return String.valueOf(regionPlanContext.getRegion().getRegionId());
    }

    @Override
    public String getMailContent() {
        return getContent();
    }

    private String getContent() {
        StringBuilder msgBuilder = new StringBuilder("");
        if (CapacityPlanRegionTaskEnum.PLAN.getCode() == regionPlanContext.getTaskType()) {
            msgBuilder.append("【容量规划-region资源变更通知】\n");
        } else {
            msgBuilder.append("【容量检查-region资源变更通知】\n");
        }
        msgBuilder.append("cluster：" + regionPlanContext.getRegion().getClusterName() + "\n");
        msgBuilder.append("region：" + regionPlanContext.getRegion().getRacks() + "\n");
        msgBuilder.append("资源总量：" + regionPlanContext.getRegionMetric().getResource() + "\n");
        msgBuilder.append("资源消耗：磁盘" + regionPlanContext.getRegionCostDiskG() + "G, CPU"
                          + regionPlanContext.getRegionCostCpuCount() + "核\n");
        msgBuilder.append("任务类型：" + taskTypeEnum.getDesc() + "\n");
        msgBuilder.append("变更rack：" + deltaRacks + "\n");
        msgBuilder.append("任务状态：" + taskStatusEnum.getDesc() + "\n");
        msgBuilder.append("发送时间：" + AriusDateUtils.date2Str(new Date(), null) + "\n");
        return msgBuilder.toString();
    }
}
