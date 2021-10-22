package com.didi.arius.gateway.dsl.dsl.ast.root;


import com.didi.arius.gateway.dsl.dsl.ast.common.KeyWord;
import com.didi.arius.gateway.dsl.dsl.ast.common.Node;
import com.didi.arius.gateway.dsl.dsl.visitor.basic.Visitor;

/**
 * @Author: D10865
 * @Description:
 * @Date: Create on 2018/9/27 下午3:09
 * @Modified By
 *
 *  存储search_type关键字的结果
 *
 *  {"index":["router_access_20180926"],"search_type":"count","ignore_unavailable":true}
 */
public class SearchType extends KeyWord {

    public Node n;

    public SearchType(String name) {
        super(name);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

}
