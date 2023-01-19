/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.sql.expression.predicate.conditional;

import org.elasticsearch.xpack.sql.expression.Expression;
import org.elasticsearch.xpack.sql.expression.Literal;
import org.elasticsearch.xpack.sql.expression.function.scalar.FunctionTestUtils;
import org.elasticsearch.xpack.sql.expression.predicate.operator.comparison.Equals;
import org.elasticsearch.xpack.sql.tree.AbstractNodeTestCase;
import org.elasticsearch.xpack.sql.tree.NodeSubclassTests;
import org.elasticsearch.xpack.sql.tree.Source;
import org.elasticsearch.xpack.sql.tree.SourceTests;
import org.elasticsearch.xpack.sql.type.DataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.elasticsearch.xpack.sql.expression.function.scalar.FunctionTestUtils.randomIntLiteral;
import static org.elasticsearch.xpack.sql.expression.function.scalar.FunctionTestUtils.randomStringLiteral;
import static org.elasticsearch.xpack.sql.tree.Source.EMPTY;
import static org.elasticsearch.xpack.sql.tree.SourceTests.randomSource;

/**
 * Needed to override tests in {@link NodeSubclassTests} as If is special since its children are not usual
 * expressions but {@link IfConditional}s.
 */
public class IifTests extends AbstractNodeTestCase<Iif, Expression> {

    public static Iif randomIif() {
        return new Iif(randomSource(), new Equals(randomSource(), randomStringLiteral(), randomStringLiteral()),
            randomIntLiteral(), randomIntLiteral());
    }

    @Override
    protected Iif randomInstance() {
        return randomIif();
    }

    @Override
    protected Iif mutate(Iif instance) {
        Iif iif = randomIif();
        List<Expression> mutatedChildren = mutateChildren(iif);
        return new Iif(iif.source(), mutatedChildren.get(0), mutatedChildren.get(1), mutatedChildren.get(2));
    }

    @Override
    protected Iif copy(Iif instance) {
        return new Iif(instance.source(), instance.conditions().get(0).condition(), instance.conditions().get(0).result(),
            instance.elseResult());
    }

    @Override
    public void testTransform() {
        Iif iif = randomIif();

        Source newSource = randomValueOtherThan(iif.source(), SourceTests::randomSource);
        assertEquals(new Iif(iif.source(), iif.conditions().get(0).condition(), iif.conditions().get(0).result(), iif.elseResult()),
            iif.transformPropertiesOnly(p -> Objects.equals(p, iif.source()) ? newSource: p, Object.class));
    }

    @Override
    public void testReplaceChildren() {
        Iif iif = randomIif();

        List<Expression> newChildren = mutateChildren(iif);
        assertEquals(new Iif(iif.source(), newChildren.get(0), newChildren.get(1), newChildren.get(2)),
            iif.replaceChildren(Arrays.asList(new IfConditional(iif.source(), newChildren.get(0), newChildren.get(1)),
                newChildren.get(2))));
    }

    public void testConditionFolded() {
        Iif iif = new Iif(EMPTY, Collections.singletonList(Literal.of(EMPTY, "foo")));
        assertEquals(DataType.KEYWORD, iif.dataType());
        assertEquals(Expression.TypeResolution.TYPE_RESOLVED, iif.typeResolved());
        assertNotNull(iif.info());
    }

    private List<Expression> mutateChildren(Iif iif) {
        List<Expression> expressions = new ArrayList<>(3);
        Equals eq = (Equals) iif.conditions().get(0).condition();
        expressions.add(new Equals(randomSource(),
            randomValueOtherThan(eq.left(), FunctionTestUtils::randomStringLiteral),
            randomValueOtherThan(eq.right(), FunctionTestUtils::randomStringLiteral)));
        expressions.add(randomValueOtherThan(iif.conditions().get(0).result(), FunctionTestUtils::randomIntLiteral));
        expressions.add(randomValueOtherThan(iif.elseResult(), FunctionTestUtils::randomIntLiteral));
        return expressions;
    }
}
