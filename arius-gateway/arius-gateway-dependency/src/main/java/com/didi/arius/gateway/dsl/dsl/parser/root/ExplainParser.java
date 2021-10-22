package com.didi.arius.gateway.dsl.dsl.parser.root;


import com.didi.arius.gateway.dsl.dsl.ast.common.KeyWord;
import com.didi.arius.gateway.dsl.dsl.ast.common.value.ObjectNode;
import com.didi.arius.gateway.dsl.dsl.ast.root.Explain;
import com.didi.arius.gateway.dsl.dsl.parser.DslParser;
import com.didi.arius.gateway.dsl.dsl.parser.ParserType;

/**
 * @Author: D10865
 * @Description:
 * @Date: Create on 2018/9/30 上午10:22
 * @Modified By
 *
 * 解析explain关键字
 *
 * {"from":0,"size":1,"query":{"ids":{"types":[],"values":["472083"]}},"explain":true,"sort":[{"alarmId":{"order":"desc"}}]}
 */
public class ExplainParser extends DslParser {

    public ExplainParser(ParserType type) {
        super(type);
    }

    @Override
    public KeyWord parse(String name, Object obj) throws Exception {
        Explain node = new Explain(name);
        node.n = new ObjectNode(obj);

        return node;
    }

}
