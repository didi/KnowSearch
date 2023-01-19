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
 * Trims the leading whitespaces.
 */
public class LTrim extends UnaryStringFunction {

    public LTrim(Source source, Expression field) {
        super(source, field);
    }

    @Override
    protected NodeInfo<LTrim> info() {
        return NodeInfo.create(this, LTrim::new, field());
    }

    @Override
    protected LTrim replaceChild(Expression newChild) {
        return new LTrim(source(), newChild);
    }

    @Override
    protected StringOperation operation() {
        return StringOperation.LTRIM;
    }

    @Override
    public DataType dataType() {
        return DataType.KEYWORD;
    }

}
