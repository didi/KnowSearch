package com.didichuxing.datachannel.arius.admin.client.bean.common.ecm;

import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JSONType(serialzeFeatures = { SerializerFeature.WriteClassName })
public class EcmParamBase implements Serializable {
    /**
     * 工单Id
     */
    protected Long    workOrderId;

    /**
     * 集群ID
     */
    protected Long    phyClusterId;

    /**
     * 集群名字
     */
    protected String  phyClusterName;

    /**
     * 角色名称
     */
    protected String  roleName;

    /**
     * 集群类型(3：容器云集群；4：物理机集群 ）)
     * @see ESClusterTypeEnum
     */
    protected Integer type;

    /**
     * 任务ID
     */
    protected Integer taskId;

    /**
     * 节点数(pod数量)
     */
    protected Integer nodeNumber;
}
