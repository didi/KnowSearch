package com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.host;

import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.EcmParamBase;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.EsConfigAction;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.EsPluginAction;
import lombok.Data;

import java.util.List;

@Data
@JSONType(serialzeFeatures = { SerializerFeature.WriteClassName })
public class HostParamBase extends EcmParamBase {
    /**
     * 版本包地址
     */
    protected String         imageName;

    /**
     * 版本
     */
    protected String         esVersion;

    /**
     * 单节点实例数
     */
    protected Integer        pidCount;

    /**
     * Es集群配置相关
     */
    protected EsConfigAction esConfigAction;

    /**
     * Es集群plugin相关
     */
    protected EsPluginAction esPluginAction;

    /**
     * 主机列表
     */
    protected List<String>   hostList;

    /**
     * master主机列表
     */
    protected List<String>   masterHostList;
}