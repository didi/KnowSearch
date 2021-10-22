package com.didi.arius.gateway.dsl.dsl.parser.root;

import com.didi.arius.gateway.dsl.dsl.ast.common.KeyWord;
import com.didi.arius.gateway.dsl.dsl.ast.common.value.ObjectNode;
import com.didi.arius.gateway.dsl.dsl.ast.root.Title;
import com.didi.arius.gateway.dsl.dsl.parser.DslParser;
import com.didi.arius.gateway.dsl.dsl.parser.ParserType;

/**
 * @Author: D10865
 * @Description:
 * @Date: Create on 2018/9/17 上午11:57
 * @Modified By
 * <p>
 * 解析title关键字
 * <p>
 * {"timeFieldName":"logTime","title":"heima_tcp.middleware_carreramessage.heima*","fields":"[]"}
 */
public class TitleParser extends DslParser {

    public TitleParser(ParserType type) {
        super(type);
    }

    @Override
    public KeyWord parse(String name, Object obj) throws Exception {
        Title node = new Title(name);
        node.n = new ObjectNode(obj);

        return node;
    }

}
