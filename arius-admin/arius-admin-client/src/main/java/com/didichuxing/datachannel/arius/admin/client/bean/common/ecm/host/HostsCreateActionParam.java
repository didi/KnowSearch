package com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.host;

import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ResourceLogicLevelEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JSONType(serialzeFeatures = { SerializerFeature.WriteClassName })
public class HostsCreateActionParam extends HostsParamBase {

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
