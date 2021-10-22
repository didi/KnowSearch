package com.didi.arius.gateway.dsl.dsl.ast.root;

import com.didi.arius.gateway.dsl.dsl.ast.common.KeyWord;
import com.didi.arius.gateway.dsl.dsl.ast.common.Node;
import com.didi.arius.gateway.dsl.dsl.visitor.basic.Visitor;

/**
 * @Author: D10865
 * @Description:
 * @Date: Create on 2018/9/17 上午11:52
 * @Modified By
 *
 * 存储index关键字的结果
 *  {"index":["arius_dsl_log_2018-09-20"],"ignore_unavailable":true}
 *
 */
public class Index extends KeyWord {
    public Node n;

    public Index(String name) {
        super(name);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
