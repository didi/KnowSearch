package com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.host;

import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.Data;

import java.util.List;

/**
 * @author zengqiao
 * @date 20/10/20
 */
@Data
@JSONType(serialzeFeatures = { SerializerFeature.WriteClassName })
public class HostScaleActionParam extends HostParamBase {
    private List<String> hostList;

    private String       action;
}