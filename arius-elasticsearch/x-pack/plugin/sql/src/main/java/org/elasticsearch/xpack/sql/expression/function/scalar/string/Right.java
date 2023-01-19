/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.sql.expression.function.scalar.string;

import org.elasticsearch.xpack.sql.expression.Expression;
import org.elasticsearch.xpack.sql.expression.function.scalar.string.BinaryStringNumericProcessor.BinaryStringNumericOperation;
import org.elasticsearch.xpack.sql.tree.Source;
import org.elasticsearch.xpack.sql.tree.NodeInfo;

/**
 * Returns the rightmost count characters of a string.
 */
public class Right extends BinaryStringNumericFunction {

    public Right(Source source, Expression left, Expression right) {
        super(source, left, right);
    }

    @Override
    protected BinaryStringNumericOperation operation() {
        return BinaryStringNumericOperation.RIGHT;
    }

    @Override
    protected Right replaceChildren(Expression newLeft, Expression newRight) {
        return new Right(source(), newLeft, newRight);
    }

    @Override
    protected NodeInfo<Right> info() {
        return NodeInfo.create(this, Right::new, left(), right());
    }

}
