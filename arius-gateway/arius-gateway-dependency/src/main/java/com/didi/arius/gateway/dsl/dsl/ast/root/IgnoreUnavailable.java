package com.didi.arius.gateway.dsl.dsl.ast.root;

import com.didi.arius.gateway.dsl.dsl.ast.common.KeyWord;
import com.didi.arius.gateway.dsl.dsl.ast.common.Node;
import com.didi.arius.gateway.dsl.dsl.visitor.basic.Visitor;

/**
 * @Author: D10865
 * @Description:
 * @Date: Create on 2018/9/21 下午12:58
 * @Modified By
 *
 * * 存储ignore_unavailable关键字的结果
 *  {"index":["arius_dsl_log_2018-09-20"],"ignore_unavailable":true}
 */
public class IgnoreUnavailable extends KeyWord {
    public Node n;

    public IgnoreUnavailable(String name) {
        super(name);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
