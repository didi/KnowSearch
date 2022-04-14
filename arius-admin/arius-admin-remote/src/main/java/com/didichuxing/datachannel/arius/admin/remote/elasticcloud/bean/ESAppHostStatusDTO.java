package com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean;

import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.response.EcmTaskStatus;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;
import com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean.bizenum.ElasticCloudHostStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ESAppHostStatusDTO extends BaseDTO {

    private static final long serialVersionUID = -6512263309120853724L;
    /**
     * 分组
     */
    private String  host;

    /**
     * 分组
     */
    private Integer group;

    /**
     * 下标
     */
    private Integer idx;

    /**
     * 状态
     */
    private String  status;

    /**
     * 阶段
     */
    private String  phase;

    /**
     * 转化成 EcmTaskStatus
     * @see EcmTaskStatus
     */
    public EcmTaskStatus convert2EcmTaskStatus(Integer taskId, String clusterStatus) {
        EcmTaskStatus ecmTaskStatus = new EcmTaskStatus();
        ecmTaskStatus.setTaskId(taskId);
        ecmTaskStatus.setHostname(host);
        ecmTaskStatus.setGroup(group);
        ecmTaskStatus.setPodIndex(idx);
        ecmTaskStatus.setStatusEnum(ElasticCloudHostStatusEnum.getEcmHostStatus(this.status, clusterStatus));
        return ecmTaskStatus;
    }

}
