/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.sql.expression.gen.script;

import org.elasticsearch.xpack.sql.expression.Expressions;
import org.elasticsearch.xpack.sql.expression.function.aggregate.AggregateFunction;
import org.elasticsearch.xpack.sql.expression.function.aggregate.Count;
import org.elasticsearch.xpack.sql.expression.function.aggregate.InnerAggregate;

class Agg extends Param<AggregateFunction> {

    private static final String COUNT_PATH = "_count";

    Agg(AggregateFunction aggRef) {
        super(aggRef);
    }

    String aggName() {
        return Expressions.id(value());
    }

    public String aggProperty() {
        AggregateFunction agg = value();

        if (agg instanceof InnerAggregate) {
            InnerAggregate inner = (InnerAggregate) agg;
            return Expressions.id(inner.outer()) + "." + inner.innerName();
        }
        // Count needs special handling since in most cases it is not a dedicated aggregation
        else if (agg instanceof Count) {
            Count c = (Count) agg;
            // for literals get the last count
            if (c.field().foldable() == true) {
                return COUNT_PATH;
            }
            // when dealing with fields, check whether there's a single-metric (distinct -> cardinality)
            // or a bucket (non-distinct - filter agg)
            else {
                if (c.distinct() == true) {
                    return Expressions.id(c);
                } else {
                    return Expressions.id(c) + "." + COUNT_PATH;
                }
            }
        }
        return null;
    }

    @Override
    public String prefix() {
        return "a";
    }
}