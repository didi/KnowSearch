/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */

package org.elasticsearch.xpack.sql.expression.function.scalar.string;

import org.elasticsearch.xpack.sql.expression.Expression;
import org.elasticsearch.xpack.sql.expression.function.scalar.FunctionTestUtils.Combinations;
import org.elasticsearch.xpack.sql.expression.gen.pipeline.Pipe;
import org.elasticsearch.xpack.sql.tree.AbstractNodeTestCase;
import org.elasticsearch.xpack.sql.tree.Source;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static org.elasticsearch.xpack.sql.expression.Expressions.pipe;
import static org.elasticsearch.xpack.sql.expression.function.scalar.FunctionTestUtils.randomIntLiteral;
import static org.elasticsearch.xpack.sql.expression.function.scalar.FunctionTestUtils.randomStringLiteral;
import static org.elasticsearch.xpack.sql.tree.SourceTests.randomSource;

public class SubstringFunctionPipeTests
    extends AbstractNodeTestCase<SubstringFunctionPipe, Pipe> {

    @Override
    protected SubstringFunctionPipe randomInstance() {
        return randomSubstringFunctionPipe();
    }
    
    private Expression randomSubstringFunctionExpression() {
        return randomSubstringFunctionPipe().expression();
    }
    
    public static SubstringFunctionPipe randomSubstringFunctionPipe() {
        return (SubstringFunctionPipe) (new Substring(randomSource(),
                            randomStringLiteral(),
                            randomIntLiteral(),
                            randomIntLiteral())
                .makePipe());
    }

    @Override
    public void testTransform() {
        // test transforming only the properties (source, expression),
        // skipping the children (the two parameters of the binary function) which are tested separately
        SubstringFunctionPipe b1 = randomInstance();
        Expression newExpression = randomValueOtherThan(b1.expression(), () -> randomSubstringFunctionExpression());
        SubstringFunctionPipe newB = new SubstringFunctionPipe(
                b1.source(),
                newExpression,
                b1.src(),
                b1.start(),
                b1.length());
        assertEquals(newB, b1.transformPropertiesOnly(v -> Objects.equals(v, b1.expression()) ? newExpression : v, Expression.class));
        
        SubstringFunctionPipe b2 = randomInstance();
        Source newLoc = randomValueOtherThan(b2.source(), () -> randomSource());
        newB = new SubstringFunctionPipe(
                newLoc,
                b2.expression(),
                b2.src(),
                b2.start(),
                b2.length());
        assertEquals(newB,
                b2.transformPropertiesOnly(v -> Objects.equals(v, b2.source()) ? newLoc : v, Source.class));
    }

    @Override
    public void testReplaceChildren() {
        SubstringFunctionPipe b = randomInstance();
        Pipe newSource = pipe(((Expression) randomValueOtherThan(b.source(), () -> randomStringLiteral())));
        Pipe newStart = pipe(((Expression) randomValueOtherThan(b.start(), () -> randomIntLiteral())));
        Pipe newLength = pipe(((Expression) randomValueOtherThan(b.length(), () -> randomIntLiteral())));
        SubstringFunctionPipe newB =
                new SubstringFunctionPipe(b.source(), b.expression(), b.src(), b.start(), b.length());
        SubstringFunctionPipe transformed = null;
        
        // generate all the combinations of possible children modifications and test all of them
        for(int i = 1; i < 4; i++) {
            for(BitSet comb : new Combinations(3, i)) {
                transformed = (SubstringFunctionPipe) newB.replaceChildren(
                        comb.get(0) ? newSource : b.src(),
                        comb.get(1) ? newStart : b.start(),
                        comb.get(2) ? newLength : b.length());
                assertEquals(transformed.src(), comb.get(0) ? newSource : b.src());
                assertEquals(transformed.start(), comb.get(1) ? newStart : b.start());
                assertEquals(transformed.length(), comb.get(2) ? newLength : b.length());
                assertEquals(transformed.expression(), b.expression());
                assertEquals(transformed.source(), b.source());
            }
        }
    }

    @Override
    protected SubstringFunctionPipe mutate(SubstringFunctionPipe instance) {
        List<Function<SubstringFunctionPipe, SubstringFunctionPipe>> randoms = new ArrayList<>();
        
        for(int i = 1; i < 4; i++) {
            for(BitSet comb : new Combinations(3, i)) {
                randoms.add(f -> new SubstringFunctionPipe(
                        f.source(),
                        f.expression(),
                        comb.get(0) ? pipe(((Expression) randomValueOtherThan(f.src(),
                                () -> randomStringLiteral()))) : f.src(),
                        comb.get(1) ? pipe(((Expression) randomValueOtherThan(f.start(),
                                () -> randomIntLiteral()))) : f.start(),
                        comb.get(2) ? pipe(((Expression) randomValueOtherThan(f.length(),
                                () -> randomIntLiteral()))): f.length()));
            }
        }
        
        return randomFrom(randoms).apply(instance);
    }

    @Override
    protected SubstringFunctionPipe copy(SubstringFunctionPipe instance) {
        return new SubstringFunctionPipe(instance.source(),
                instance.expression(),
                instance.src(),
                instance.start(),
                instance.length());
    }
}