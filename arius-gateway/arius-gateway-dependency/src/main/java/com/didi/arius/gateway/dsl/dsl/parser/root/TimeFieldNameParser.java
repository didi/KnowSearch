package com.didi.arius.gateway.dsl.dsl.parser.root;

import com.didi.arius.gateway.dsl.dsl.ast.common.KeyWord;
import com.didi.arius.gateway.dsl.dsl.ast.common.value.ObjectNode;
import com.didi.arius.gateway.dsl.dsl.ast.root.TimeFieldName;
import com.didi.arius.gateway.dsl.dsl.parser.DslParser;
import com.didi.arius.gateway.dsl.dsl.parser.ParserType;

/**
 * @Author: D10865
 * @Description:
 * @Date: Create on 2018/9/17 上午11:57
 * @Modified By
 * <p>
 * 解析timeFieldName关键字
 * <p>
 * {"timeFieldName":"logTime","title":"heima_tcp.middleware_carreramessage.heima*","fields":"[]"}
 */
public class TimeFieldNameParser extends DslParser {

    public TimeFieldNameParser(ParserType type) {
        super(type);
    }

    @Override
    public KeyWord parse(String name, Object obj) throws Exception {
        TimeFieldName node = new TimeFieldName(name);
        node.n = new ObjectNode(obj);

        return node;
    }

}
