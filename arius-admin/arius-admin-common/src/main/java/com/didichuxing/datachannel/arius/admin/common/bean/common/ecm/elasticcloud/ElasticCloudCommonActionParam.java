package com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.elasticcloud;

import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.EcmParamBase;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.EsConfigAction;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.EsPluginAction;
import lombok.Data;

@Data
@JSONType(serialzeFeatures = { SerializerFeature.WriteClassName })
public class ElasticCloudCommonActionParam extends EcmParamBase {
    protected String         nsTree;

    protected String         machineRoom;

    protected String         imageName;

    protected String         esVersion;

    protected EsConfigAction esConfigActions;

    protected EsPluginAction esPluginAction;

    public ElasticCloudCommonActionParam() {
    }

    public ElasticCloudCommonActionParam(Long clusterId, String roleName) {
        this.phyClusterId = clusterId;
        this.roleName = roleName;
    }

    public String namespace() {
        return String.format("%s.%s.%s", this.roleName, this.phyClusterName, this.nsTree);
    }
}