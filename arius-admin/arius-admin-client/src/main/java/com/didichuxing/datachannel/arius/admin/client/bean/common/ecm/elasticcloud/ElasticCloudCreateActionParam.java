package com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.elasticcloud;

import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.EcmParamBase;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ResourceLogicLevelEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JSONType(serialzeFeatures = { SerializerFeature.WriteClassName })
public class ElasticCloudCreateActionParam extends EcmParamBase {
    /**
     * 机器规格
     */
    private String   machineSpec;

    /**
     * role信息
     */
    private Long     roleId;

    private String   roleClusterName;

    private Integer  podNum;

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
     * es版本
     */
    protected String esVersion;

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
     * 镜像地址
     */
    protected String imageName;

    /**
     * 服务等级
     * @see ResourceLogicLevelEnum
     */
    private Integer  level;

    public Result validateFiledIllegal() {
        if (StringUtils.isBlank(this.phyClusterName)) {
            return Result.buildParamIllegal("集群名称为空");
        }
        if (null == this.type) {
            return Result.buildParamIllegal("集群类型为空");
        }
        if (StringUtils.isBlank(this.dataCenter)) {
            return Result.buildParamIllegal("数据中心为空");
        }
        if (StringUtils.isBlank(this.idc)) {
            return Result.buildParamIllegal("机房名称为空");
        }
        if (StringUtils.isBlank(this.esVersion)) {
            return Result.buildParamIllegal("ES版本为空");
        }
        return Result.buildSucc();
    }

    public String namespace() {
        return String.format("%s.%s.%s", this.roleName, this.phyClusterName, this.nsTree);
    }

}
