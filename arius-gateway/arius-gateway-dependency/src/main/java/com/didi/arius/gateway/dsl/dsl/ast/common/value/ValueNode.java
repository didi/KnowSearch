package com.didi.arius.gateway.dsl.dsl.ast.common.value;

import com.alibaba.fastjson.JSON;
import com.didi.arius.gateway.dsl.dsl.ast.common.Node;

public abstract class ValueNode extends Node {

    // 返回基本Node
    public static Node getValueNode(Object obj) {
        if (obj instanceof JSON) {
            return new JsonNode(obj);
        } else {
            return new ObjectNode(obj);
        }
    }
}
