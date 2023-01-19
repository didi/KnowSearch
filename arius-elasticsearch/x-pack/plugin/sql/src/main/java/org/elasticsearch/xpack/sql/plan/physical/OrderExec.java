/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.sql.plan.physical;

import java.util.List;
import java.util.Objects;

import org.elasticsearch.xpack.sql.expression.Order;
import org.elasticsearch.xpack.sql.tree.Source;
import org.elasticsearch.xpack.sql.tree.NodeInfo;

public class OrderExec extends UnaryExec implements Unexecutable {

    private final List<Order> order;

    public OrderExec(Source source, PhysicalPlan child, List<Order> order) {
        super(source, child);
        this.order = order;
    }

    @Override
    protected NodeInfo<OrderExec> info() {
        return NodeInfo.create(this, OrderExec::new, child(), order);
    }

    @Override
    protected OrderExec replaceChild(PhysicalPlan newChild) {
        return new OrderExec(source(), newChild, order);
    }

    public List<Order> order() {
        return order;
    }

    @Override
    public int hashCode() {
        return Objects.hash(order, child());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        OrderExec other = (OrderExec) obj;

        return Objects.equals(order, other.order)
                && Objects.equals(child(), other.child());
    }
}
