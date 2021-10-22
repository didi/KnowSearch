package com.didi.arius.gateway.dsl.dsl.parser.root;

import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.dsl.dsl.ast.common.KeyWord;
import com.didi.arius.gateway.dsl.dsl.ast.common.key.FieldNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.multi.NodeMap;
import com.didi.arius.gateway.dsl.dsl.ast.common.value.ValueNode;
import com.didi.arius.gateway.dsl.dsl.ast.root.IndexConstraints;
import com.didi.arius.gateway.dsl.dsl.parser.DslParser;
import com.didi.arius.gateway.dsl.dsl.parser.ParserType;

/**
 * @Author: D10865
 * @Description:
 * @Date: Create on 2018/9/6 下午12:47
 * @Modified By
 *
 * fields 解析器
 */
public class IndexConstraintsParser extends DslParser {

    public IndexConstraintsParser(ParserType type) {
        super(type);
    }

    @Override
    public KeyWord parse(String name, Object root) throws Exception {
        IndexConstraints node = new IndexConstraints(name);
        NodeMap nm = new NodeMap();

        JSONObject jsonObject = (JSONObject) root;
        for(String key : jsonObject.keySet()) {
            FieldNode fieldNode = new FieldNode(key);
            nm.m.put(fieldNode, ValueNode.getValueNode(jsonObject.get(key)));
        }

        node.n = nm;
        return node;
    }


}
