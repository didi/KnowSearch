package com.didi.arius.gateway.dsl.dsl.parser.root;


import com.didi.arius.gateway.dsl.dsl.ast.common.KeyWord;
import com.didi.arius.gateway.dsl.dsl.ast.common.key.StringNode;
import com.didi.arius.gateway.dsl.dsl.ast.root.SearchType;
import com.didi.arius.gateway.dsl.dsl.parser.DslParser;
import com.didi.arius.gateway.dsl.dsl.parser.ParserType;

/**
 * @Author: D10865
 * @Description:
 * @Date: Create on 2018/9/27 下午3:08
 * @Modified By
 *
 * 解析search_type关键字
 *
 * {"index":["router_access_20180926"],"search_type":"count","ignore_unavailable":true}
 */
public class SearchTypeParser extends DslParser {

    public SearchTypeParser(ParserType type) {
        super(type);
    }

    @Override
    public KeyWord parse(String name, Object obj) throws Exception {
        SearchType node = new SearchType(name);
        node.n = new StringNode(obj);

        return node;
    }
}
