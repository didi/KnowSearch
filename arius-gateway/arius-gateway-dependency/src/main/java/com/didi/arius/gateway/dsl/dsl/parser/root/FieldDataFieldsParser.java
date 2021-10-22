package com.didi.arius.gateway.dsl.dsl.parser.root;

import com.alibaba.fastjson.JSONArray;
import com.didi.arius.gateway.dsl.dsl.ast.common.KeyWord;
import com.didi.arius.gateway.dsl.dsl.ast.common.key.StringNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.multi.NodeList;
import com.didi.arius.gateway.dsl.dsl.ast.root.FieldDataFields;
import com.didi.arius.gateway.dsl.dsl.parser.DslParser;
import com.didi.arius.gateway.dsl.dsl.parser.ParserType;

/**
 * @Author: D10865
 * @Description:
 * @Date: Create on 2018/9/21 下午3:21
 * @Modified By
 *
 * 解析fielddata_fields关键字
 *

"fielddata_fields": [
"sinkTime",
"collectTime",
"logTime",
"cleanTime"
]

 */
public class FieldDataFieldsParser extends DslParser {

    public FieldDataFieldsParser(ParserType type) {
        super(type);
    }

    @Override
    public KeyWord parse(String name, Object root) throws Exception {
        FieldDataFields node = new FieldDataFields(name);

        if(root instanceof JSONArray) {
            node.n = new NodeList();
            NodeList.toFieldList((JSONArray) root, (NodeList) node.n);

        } else if (root instanceof String) {
            node.n = new StringNode(root);
        }

        return node;
    }

}
