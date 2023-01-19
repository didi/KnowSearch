/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.sql.plan.physical;

import java.util.List;
import java.util.Objects;

import org.elasticsearch.xpack.sql.expression.Attribute;
import org.elasticsearch.xpack.sql.expression.Expressions;
import org.elasticsearch.xpack.sql.expression.NamedExpression;
import org.elasticsearch.xpack.sql.tree.Source;
import org.elasticsearch.xpack.sql.tree.NodeInfo;

public class ProjectExec extends UnaryExec implements Unexecutable {

    private final List<? extends NamedExpression> projections;

    public ProjectExec(Source source, PhysicalPlan child, List<? extends NamedExpression> projections) {
        super(source, child);
        this.projections = projections;
    }

    @Override
    protected NodeInfo<ProjectExec> info() {
        return NodeInfo.create(this, ProjectExec::new, child(), projections);
    }

    @Override
    protected ProjectExec replaceChild(PhysicalPlan newChild) {
        return new ProjectExec(source(), newChild, projections);
    }

    public List<? extends NamedExpression> projections() {
        return projections;
    }

    @Override
    public List<Attribute> output() {
        return Expressions.asAttributes(projections);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projections, child());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        ProjectExec other = (ProjectExec) obj;

        return Objects.equals(projections, other.projections)
                && Objects.equals(child(), other.child());
    }
}
