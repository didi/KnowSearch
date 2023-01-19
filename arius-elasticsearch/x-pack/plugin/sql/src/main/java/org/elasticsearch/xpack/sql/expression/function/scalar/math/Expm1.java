/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.sql.expression.function.scalar.math;

import org.elasticsearch.xpack.sql.expression.Expression;
import org.elasticsearch.xpack.sql.expression.function.scalar.math.MathProcessor.MathOperation;
import org.elasticsearch.xpack.sql.tree.Source;
import org.elasticsearch.xpack.sql.tree.NodeInfo;

/**
 * <a href="https://docs.oracle.com/javase/8/docs/api/java/lang/Math.html#expm1-double-">e<sup>x</sup> + 1</a>
 * function.
 */
public class Expm1 extends MathFunction {
    public Expm1(Source source, Expression field) {
        super(source, field);
    }

    @Override
    protected NodeInfo<Expm1> info() {
        return NodeInfo.create(this, Expm1::new, field());
    }

    @Override
    protected Expm1 replaceChild(Expression newChild) {
        return new Expm1(source(), newChild);
    }

    @Override
    protected MathOperation operation() {
        return MathOperation.EXPM1;
    }
}
