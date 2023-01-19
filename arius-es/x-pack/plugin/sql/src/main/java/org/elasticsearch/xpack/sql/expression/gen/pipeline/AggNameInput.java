/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.sql.expression.gen.pipeline;

import org.elasticsearch.xpack.sql.expression.Expression;
import org.elasticsearch.xpack.sql.tree.Source;
import org.elasticsearch.xpack.sql.tree.NodeInfo;

public class AggNameInput extends CommonNonExecutableInput<String> {
    public AggNameInput(Source source, Expression expression, String context) {
        super(source, expression, context);
    }

    @Override
    protected NodeInfo<AggNameInput> info() {
        return NodeInfo.create(this, AggNameInput::new, expression(), context());
    }

    @Override
    public final boolean supportedByAggsOnlyQuery() {
        return true;
    }

    @Override
    public final boolean resolved() {
        return false;
    }
}
