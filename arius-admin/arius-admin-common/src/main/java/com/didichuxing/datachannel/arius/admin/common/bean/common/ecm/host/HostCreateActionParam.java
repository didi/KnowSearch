package com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.host;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterResourceTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ResourceLogicLevelEnum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JSONType(serialzeFeatures = { SerializerFeature.WriteClassName })
public class HostCreateActionParam extends HostParamBase {

    /**
     * 机器规格
     */
    private String   machineSpec;

    /**
     * 数据中心
     */
    protected String dataCenter;

    /**
     * 机器节点
     */
    protected String nsTree;

    /**
     * 机房
     */
    protected String idc;

    /**
     * 插件包ID列表
     */
    protected String plugs;

    /**
     * 集群创建人
     */
    protected String creator;

    /**
     * 描述
     */
    protected String desc;

    /**
     * 服务等级
     * @see ResourceLogicLevelEnum
     */
    private Integer  level;

    private String   platformType;

    /**
     * @see ClusterResourceTypeEnum
     */
    private Integer  resourceType;

    public Result<Void> validateFiledIllegal() {
        if (StringUtils.isBlank(this.phyClusterName)) {
            return Result.buildParamIllegal("集群名称为空");
        }
        if (null == this.type) {
            return Result.buildParamIllegal("集群类型为空");
        }
        if (StringUtils.isBlank(this.esVersion)) {
            return Result.buildParamIllegal("ES版本为空");
        }
        return Result.buildSucc();
    }
}
