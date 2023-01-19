/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */

package org.elasticsearch.xpack.sql.querydsl.container;

import org.elasticsearch.xpack.sql.expression.function.aggregate.AggregateFunction;

import java.util.Objects;

public class AggregateSort extends Sort {

    private final AggregateFunction agg;

    public AggregateSort(AggregateFunction agg, Direction direction, Missing missing) {
        super(direction, missing);
        this.agg = agg;
    }

    public AggregateFunction agg() {
        return agg;
    }

    @Override
    public int hashCode() {
        return Objects.hash(agg, direction(), missing());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        
        AggregateSort other = (AggregateSort) obj;
        return Objects.equals(direction(), other.direction())
                && Objects.equals(missing(), other.missing())
                && Objects.equals(agg, other.agg);
    }
}
