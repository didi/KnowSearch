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
 * 存储type关键字的结果
 *  {"index":"cll_test_binlog_kafka_2018-09-25","type":"cll_binlog_type"}
 *
 */
public class Type extends KeyWord {
    public Node n;

    public Type(String name) {
        super(name);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
