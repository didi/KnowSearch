package com.didi.arius.gateway.dsl.dsl.parser.root;

import com.didi.arius.gateway.dsl.dsl.ast.common.KeyWord;
import com.didi.arius.gateway.dsl.dsl.ast.common.value.ObjectNode;
import com.didi.arius.gateway.dsl.dsl.ast.root.IgnoreUnavailable;
import com.didi.arius.gateway.dsl.dsl.parser.DslParser;
import com.didi.arius.gateway.dsl.dsl.parser.ParserType;

/**
 * @Author: D10865
 * @Description:
 * @Date: Create on 2018/9/21 下午12:56
 * @Modified By
 * 解析index关键字
 *
 * {"index":["arius_dsl_log_2018-09-20"],"ignore_unavailable":true}
 */
public class IgnoreUnavailableParser extends DslParser {

    public IgnoreUnavailableParser(ParserType type) {
        super(type);
    }

    @Override
    public KeyWord parse(String name, Object obj) throws Exception {
        IgnoreUnavailable node = new IgnoreUnavailable(name);
        node.n = new ObjectNode(obj);

        return node;
    }

}
