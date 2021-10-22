package com.didi.arius.gateway.dsl.dsl.parser.root;

import com.didi.arius.gateway.dsl.dsl.ast.common.KeyWord;
import com.didi.arius.gateway.dsl.dsl.ast.common.value.ObjectNode;
import com.didi.arius.gateway.dsl.dsl.ast.root.ScrollId;
import com.didi.arius.gateway.dsl.dsl.parser.DslParser;
import com.didi.arius.gateway.dsl.dsl.parser.ParserType;

/**
 * @Author: D10865
 * @Description:
 * @Date: Create on 2018/9/17 上午11:57
 * @Modified By
 * <p>
 * 解析scroll_id关键字  或 scrollId关键字
 * <p>
 * {"scroll":"60s","scroll_id":"cXVlcnlBbmRGZXRjaDsxOzEyNjEzOTpXYW9YS2dlQVM1YU9hZFJXVFNZa2x3OzA7"}
 * {"scroll":"1m","scrollId":"cXVlcnlUaGVuRmV0Y2g7ODsyMjcwNTg5MzoyWF"}
 */
public class ScrollIdParser extends DslParser {

    public ScrollIdParser(ParserType type) {
        super(type);
    }

    @Override
    public KeyWord parse(String name, Object obj) throws Exception {
        ScrollId node = new ScrollId(name);
        node.n = new ObjectNode(obj);

        return node;
    }

}
