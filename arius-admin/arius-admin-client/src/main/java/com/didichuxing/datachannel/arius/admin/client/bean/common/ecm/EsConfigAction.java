package com.didichuxing.datachannel.arius.admin.client.bean.common.ecm;

import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.didichuxing.datachannel.arius.admin.client.constant.esconfig.EsConfigActionEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author lyn
 * @date 2021-01-21
 */
@Data
@AllArgsConstructor
public class EsConfigAction implements Serializable {

    /**
     * 配置操作类型
     * @see EsConfigActionEnum
     */
    private Integer    actionType;

    /**
     * 无效Es集群配置id
     */
    private List<Long> invalidEsConfigIds;

}
