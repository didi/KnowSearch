package com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.host;

import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.EcmParamBase;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.EsConfigAction;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.EsPluginAction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JSONType(serialzeFeatures = { SerializerFeature.WriteClassName })
public class HostsParamBase extends EcmParamBase {
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
     * 端口号, 同一ES实例角色的端口号相同
     */
    protected String         port;

    /**
     * master主机列表
     */
    protected List<String>   masterHostList;
}