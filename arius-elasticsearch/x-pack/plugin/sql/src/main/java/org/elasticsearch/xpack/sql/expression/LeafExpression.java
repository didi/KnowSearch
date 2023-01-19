/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.sql.expression;

import org.elasticsearch.xpack.sql.tree.Source;

import java.util.List;

import static java.util.Collections.emptyList;

public abstract class LeafExpression extends Expression {

    protected LeafExpression(Source source) {
        super(source, emptyList());
    }

    @Override
    public final Expression replaceChildren(List<Expression> newChildren) {
        throw new UnsupportedOperationException("this type of node doesn't have any children to replace");
    }

    public AttributeSet references() {
        return AttributeSet.EMPTY;
    }
}
