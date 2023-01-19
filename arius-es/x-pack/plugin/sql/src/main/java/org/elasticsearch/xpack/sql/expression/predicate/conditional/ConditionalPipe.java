/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */

package org.elasticsearch.xpack.sql.expression.predicate.conditional;

import org.elasticsearch.xpack.sql.expression.Expression;
import org.elasticsearch.xpack.sql.expression.gen.pipeline.MultiPipe;
import org.elasticsearch.xpack.sql.expression.gen.pipeline.Pipe;
import org.elasticsearch.xpack.sql.expression.gen.processor.Processor;
import org.elasticsearch.xpack.sql.expression.predicate.conditional.ConditionalProcessor.ConditionalOperation;
import org.elasticsearch.xpack.sql.tree.Source;
import org.elasticsearch.xpack.sql.tree.NodeInfo;

import java.util.List;
import java.util.Objects;

public class ConditionalPipe extends MultiPipe {

    private final ConditionalOperation operation;

    public ConditionalPipe(Source source, Expression expression, List<Pipe> children, ConditionalOperation operation) {
        super(source, expression, children);
        this.operation = operation;
    }

    @Override
    protected NodeInfo<ConditionalPipe> info() {
        return NodeInfo.create(this, ConditionalPipe::new, expression(), children(), operation);
    }

    @Override
    public Pipe replaceChildren(List<Pipe> newChildren) {
        return new ConditionalPipe(source(), expression(), newChildren, operation);
    }

    @Override
    public Processor asProcessor(List<Processor> procs) {
        return new ConditionalProcessor(procs, operation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), operation);
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            ConditionalPipe other = (ConditionalPipe) obj;
            return Objects.equals(operation, other.operation);
        }
        return false;
    }
}
