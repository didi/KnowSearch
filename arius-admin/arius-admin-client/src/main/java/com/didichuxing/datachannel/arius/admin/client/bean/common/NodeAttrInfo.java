package com.didichuxing.datachannel.arius.admin.client.bean.common;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NodeAttrInfo {
    /**
     * 物理集群节点名称
     */
    @JSONField(name = "node")
    private String node;

    /**
     * 节点属性
     */
    @JSONField(name = "attr")
    private String attribute;

    /**
     * 节点属性对应值
     */
    @JSONField(name = "value")
    private String value;
}
