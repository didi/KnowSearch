package com.didi.arius.gateway.dsl.dsl.ast.query;

import com.didi.arius.gateway.dsl.dsl.ast.common.KeyWord;
import com.didi.arius.gateway.dsl.dsl.ast.common.Node;
import com.didi.arius.gateway.dsl.dsl.visitor.basic.Visitor;

/**
 * @Author: D10865
 * @Description:
 * @Date: Create on 2018/9/17 上午11:30
 * @Modified By
 *
 * 存储dis_max查询子句里的元素
 */
public class DisMax extends KeyWord {

    public Node n;

    public DisMax(String name) {
        super(name);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

}
