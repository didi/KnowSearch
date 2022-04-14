package com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.elasticcloud;

import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zengqiao
 * @date 20/10/20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JSONType(serialzeFeatures = { SerializerFeature.WriteClassName })
public class ElasticCloudScaleActionParam extends ElasticCloudCommonActionParam {
    private Integer podNum;
}