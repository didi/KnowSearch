/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.sql.expression.function.aggregate;

import org.elasticsearch.xpack.sql.expression.Expression;
import org.elasticsearch.xpack.sql.tree.NodeInfo;
import org.elasticsearch.xpack.sql.tree.Source;
import org.elasticsearch.xpack.sql.type.DataType;

import java.util.List;
import java.util.Objects;

/**
 * Count the number of documents matched ({@code COUNT})
 * <strong>OR</strong> count the number of distinct values
 * for a field that matched ({@code COUNT(DISTINCT}.
 */
public class Count extends AggregateFunction {

    private final boolean distinct;

    public Count(Source source, Expression field, boolean distinct) {
        super(source, field);
        this.distinct = distinct;
    }

    @Override
    protected NodeInfo<Count> info() {
        return NodeInfo.create(this, Count::new, field(), distinct);
    }

    @Override
    public Count replaceChildren(List<Expression> newChildren) {
        if (newChildren.size() != 1) {
            throw new IllegalArgumentException("expected [1] child but received [" + newChildren.size() + "]");
        }
        return new Count(source(), newChildren.get(0), distinct);
    }

    public boolean distinct() {
        return distinct;
    }

    @Override
    public DataType dataType() {
        return DataType.LONG;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), distinct());
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj) == true) {
            Count other = (Count) obj;
            return Objects.equals(other.distinct(), distinct());
        }
        return false;
    }
}
