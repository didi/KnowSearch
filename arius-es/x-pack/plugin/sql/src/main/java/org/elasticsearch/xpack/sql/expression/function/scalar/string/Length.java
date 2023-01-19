/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.sql.expression.function.scalar.string;

import org.elasticsearch.xpack.sql.expression.Expression;
import org.elasticsearch.xpack.sql.expression.function.scalar.string.StringProcessor.StringOperation;
import org.elasticsearch.xpack.sql.tree.Source;
import org.elasticsearch.xpack.sql.tree.NodeInfo;
import org.elasticsearch.xpack.sql.type.DataType;

/**
 * Returns the length (number of characters) in a string, excluding the trailing blanks.
 */
public class Length extends UnaryStringFunction {

    public Length(Source source, Expression field) {
        super(source, field);
    }

    @Override
    protected NodeInfo<Length> info() {
        return NodeInfo.create(this, Length::new, field());
    }

    @Override
    protected Length replaceChild(Expression newChild) {
        return new Length(source(), newChild);
    }

    @Override
    protected StringOperation operation() {
        return StringOperation.LENGTH;
    }

    @Override
    public DataType dataType() {
        return DataType.INTEGER;
    }

}
