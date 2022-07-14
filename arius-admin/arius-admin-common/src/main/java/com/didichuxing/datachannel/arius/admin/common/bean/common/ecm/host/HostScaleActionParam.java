package com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.host;

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
public class HostScaleActionParam extends HostParamBase {
    private String action;
}