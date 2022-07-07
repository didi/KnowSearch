package com.didichuxing.datachannel.arius.admin.common.constant;

import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import java.util.Arrays;
import java.util.Optional;

public enum ESClusterMethodNameEnum {
    /**
     * ES集群状态
     */
    ES_CLUSTER_status("集群状态",null),
    /**
     * ES集群开始
     */
    ES_CLUSTER_start("集群启动",OperateTypeEnum.PHYSICAL_CLUSTER_START),
    /**
     * ES集群能力
     */
    ES_CLUSTER_CAPACITY("集群扩缩容",OperateTypeEnum.PHYSICAL_CLUSTER_CAPACITY),
    /**
     * ES集群升级
     */
    ES_CLUSTER_UPGRADE("集群升级",OperateTypeEnum.PHYSICAL_CLUSTER_UPGRADE),
    /**
     * ES集群重新启动
     */
    ES_CLUSTER_RESTART("集群重启",OperateTypeEnum.PHYSICAL_CLUSTER_RESTART),
    /**
     * ES集群
     */
    ES_CLUSTER("集群",OperateTypeEnum.PHYSICAL_CLUSTER_START),
    /**
     * ES集群
     */
    ES_CLUSTER_OFFLINE("集群移除",OperateTypeEnum.PHYSICAL_CLUSTER_OFFLINE);
    
    private final String MethodName;
    private final OperateTypeEnum operateTypeEnum;
    
    ESClusterMethodNameEnum(String methodName, OperateTypeEnum operateTypeEnum) {
        MethodName = methodName;
        this.operateTypeEnum = operateTypeEnum;
    }
    
    public String getMethodName() {
        return MethodName;
    }
    
    public OperateTypeEnum getOperateTypeEnum() {
        return operateTypeEnum;
    }
    
    public static Optional<OperateTypeEnum> getOperateTypeEnum(String methodName) {
        return Arrays.stream(ESClusterMethodNameEnum.values())
                .filter(esClusterMethodNameEnum -> esClusterMethodNameEnum.getMethodName().equals(methodName))
                .findFirst().map(ESClusterMethodNameEnum::getOperateTypeEnum);
    }
}