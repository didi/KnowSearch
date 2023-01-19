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
 * Returns the leftmost count characters of a string.
 */
public class Left extends BinaryStringNumericFunction {

    public Left(Source source, Expression left, Expression right) {
        super(source, left, right);
    }

    @Override
    protected BinaryStringNumericOperation operation() {
        return BinaryStringNumericOperation.LEFT;
    }

    @Override
    protected Left replaceChildren(Expression newLeft, Expression newRight) {
        return new Left(source(), newLeft, newRight);
    }

    @Override
    protected NodeInfo<Left> info() {
        return NodeInfo.create(this, Left::new, left(), right());
    }

}
