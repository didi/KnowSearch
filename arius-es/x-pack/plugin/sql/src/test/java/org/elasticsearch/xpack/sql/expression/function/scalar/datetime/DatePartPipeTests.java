/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */

package org.elasticsearch.xpack.sql.expression.function.scalar.datetime;

import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.xpack.sql.expression.Expression;
import org.elasticsearch.xpack.sql.expression.function.scalar.FunctionTestUtils;
import org.elasticsearch.xpack.sql.expression.gen.pipeline.BinaryPipe;
import org.elasticsearch.xpack.sql.expression.gen.pipeline.Pipe;
import org.elasticsearch.xpack.sql.tree.AbstractNodeTestCase;
import org.elasticsearch.xpack.sql.tree.Source;
import org.elasticsearch.xpack.sql.tree.SourceTests;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static org.elasticsearch.xpack.sql.expression.Expressions.pipe;
import static org.elasticsearch.xpack.sql.expression.function.scalar.FunctionTestUtils.randomDatetimeLiteral;
import static org.elasticsearch.xpack.sql.expression.function.scalar.FunctionTestUtils.randomStringLiteral;
import static org.elasticsearch.xpack.sql.tree.SourceTests.randomSource;

public class DatePartPipeTests extends AbstractNodeTestCase<DatePartPipe, Pipe> {

    @Override
    protected DatePartPipe randomInstance() {
        return randomDatePartPipe();
    }

    private Expression randomDatePartPipeExpression() {
        return randomDatePartPipe().expression();
    }

    public static DatePartPipe randomDatePartPipe() {
        return (DatePartPipe) new DatePart(
            randomSource(),
            randomStringLiteral(),
            randomDatetimeLiteral(),
            randomZone())
            .makePipe();
    }

    @Override
    public void testTransform() {
        // test transforming only the properties (source, expression),
        // skipping the children (the two parameters of the binary function) which are tested separately
        DatePartPipe b1 = randomInstance();

        Expression newExpression = randomValueOtherThan(b1.expression(), this::randomDatePartPipeExpression);
        DatePartPipe newB = new DatePartPipe(
            b1.source(),
            newExpression,
            b1.left(),
            b1.right(),
            b1.zoneId());
        assertEquals(newB, b1.transformPropertiesOnly(v -> Objects.equals(v, b1.expression()) ? newExpression : v, Expression.class));

        DatePartPipe b2 = randomInstance();
        Source newLoc = randomValueOtherThan(b2.source(), SourceTests::randomSource);
        newB = new DatePartPipe(
            newLoc,
            b2.expression(),
            b2.left(),
            b2.right(),
            b2.zoneId());
        assertEquals(newB,
            b2.transformPropertiesOnly(v -> Objects.equals(v, b2.source()) ? newLoc : v, Source.class));
    }

    @Override
    public void testReplaceChildren() {
        DatePartPipe b = randomInstance();
        Pipe newLeft = pipe(((Expression) randomValueOtherThan(b.left(), FunctionTestUtils::randomStringLiteral)));
        Pipe newRight = pipe(((Expression) randomValueOtherThan(b.right(), FunctionTestUtils::randomDatetimeLiteral)));
        ZoneId newZoneId = randomValueOtherThan(b.zoneId(), ESTestCase::randomZone);
        DatePartPipe newB = new DatePartPipe( b.source(), b.expression(), b.left(), b.right(), newZoneId);
        BinaryPipe transformed = newB.replaceChildren(newLeft, b.right());

        assertEquals(transformed.left(), newLeft);
        assertEquals(transformed.source(), b.source());
        assertEquals(transformed.expression(), b.expression());
        assertEquals(transformed.right(), b.right());

        transformed = newB.replaceChildren(b.left(), newRight);
        assertEquals(transformed.left(), b.left());
        assertEquals(transformed.source(), b.source());
        assertEquals(transformed.expression(), b.expression());
        assertEquals(transformed.right(), newRight);

        transformed = newB.replaceChildren(newLeft, newRight);
        assertEquals(transformed.left(), newLeft);
        assertEquals(transformed.source(), b.source());
        assertEquals(transformed.expression(), b.expression());
        assertEquals(transformed.right(), newRight);
    }

    @Override
    protected DatePartPipe mutate(DatePartPipe instance) {
        List<Function<DatePartPipe, DatePartPipe>> randoms = new ArrayList<>();
        randoms.add(f -> new DatePartPipe(f.source(), f.expression(),
            pipe(((Expression) randomValueOtherThan(f.left(), FunctionTestUtils::randomStringLiteral))),
            f.right(),
            randomValueOtherThan(f.zoneId(), ESTestCase::randomZone)));
        randoms.add(f -> new DatePartPipe(f.source(), f.expression(),
            f.left(),
            pipe(((Expression) randomValueOtherThan(f.right(), FunctionTestUtils::randomDatetimeLiteral))),
            randomValueOtherThan(f.zoneId(), ESTestCase::randomZone)));
        randoms.add(f -> new DatePartPipe(f.source(), f.expression(),
            pipe(((Expression) randomValueOtherThan(f.left(), FunctionTestUtils::randomStringLiteral))),
            pipe(((Expression) randomValueOtherThan(f.right(), FunctionTestUtils::randomDatetimeLiteral))),
            randomValueOtherThan(f.zoneId(), ESTestCase::randomZone)));

        return randomFrom(randoms).apply(instance);
    }

    @Override
    protected DatePartPipe copy(DatePartPipe instance) {
        return new DatePartPipe(
            instance.source(),
            instance.expression(),
            instance.left(),
            instance.right(),
            instance.zoneId());
    }
}
