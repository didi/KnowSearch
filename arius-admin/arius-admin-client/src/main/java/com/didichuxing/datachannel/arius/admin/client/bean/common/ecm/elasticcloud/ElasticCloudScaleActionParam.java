package com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.elasticcloud;

import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.Data;

/**
 * @author zengqiao
 * @date 20/10/20
 */
@Data
@JSONType(serialzeFeatures = { SerializerFeature.WriteClassName })
public class ElasticCloudScaleActionParam extends ElasticCloudCommonActionParam {
    private Integer podNum;
}