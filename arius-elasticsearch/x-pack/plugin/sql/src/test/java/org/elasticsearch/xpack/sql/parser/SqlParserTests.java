/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.sql.parser;

import com.google.common.base.Joiner;

import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.xpack.sql.expression.Alias;
import org.elasticsearch.xpack.sql.expression.Literal;
import org.elasticsearch.xpack.sql.expression.NamedExpression;
import org.elasticsearch.xpack.sql.expression.Order;
import org.elasticsearch.xpack.sql.expression.UnresolvedAlias;
import org.elasticsearch.xpack.sql.expression.UnresolvedAttribute;
import org.elasticsearch.xpack.sql.expression.function.UnresolvedFunction;
import org.elasticsearch.xpack.sql.expression.predicate.fulltext.MatchQueryPredicate;
import org.elasticsearch.xpack.sql.expression.predicate.fulltext.MultiMatchQueryPredicate;
import org.elasticsearch.xpack.sql.expression.predicate.fulltext.StringQueryPredicate;
import org.elasticsearch.xpack.sql.plan.logical.Filter;
import org.elasticsearch.xpack.sql.plan.logical.LogicalPlan;
import org.elasticsearch.xpack.sql.plan.logical.OrderBy;
import org.elasticsearch.xpack.sql.plan.logical.Project;
import org.elasticsearch.xpack.sql.plan.logical.UnresolvedRelation;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import static java.util.Collections.nCopies;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.startsWith;

public class SqlParserTests extends ESTestCase {

    public void testSelectStar() {
        singleProjection(project(parseStatement("SELECT * FROM foo")), UnresolvedAlias.class);
    }

    private <T> T singleProjection(Project project, Class<T> type) {
        assertThat(project.projections(), hasSize(1));
        NamedExpression p = project.projections().get(0);
        assertThat(p, instanceOf(type));
        return type.cast(p);
    }

    public void testEscapeDoubleQuotes() {
        Project project = project(parseStatement("SELECT bar FROM \"fo\"\"o\""));
        assertTrue(project.child() instanceof UnresolvedRelation);
        assertEquals("fo\"o", ((UnresolvedRelation) project.child()).table().index());
    }

    public void testEscapeSingleQuotes() {
        Alias a = singleProjection(project(parseStatement("SELECT '''ab''c' AS \"escaped_text\"")), Alias.class);
        assertEquals("'ab'c", ((Literal) a.child()).value());
        assertEquals("escaped_text", a.name());
    }

    public void testEscapeSingleAndDoubleQuotes() {
        Alias a = singleProjection(project(parseStatement("SELECT 'ab''c' AS \"escaped\"\"text\"")), Alias.class);
        assertEquals("ab'c", ((Literal) a.child()).value());
        assertEquals("escaped\"text", a.name());
    }

    public void testSelectField() {
        UnresolvedAlias a = singleProjection(project(parseStatement("SELECT bar FROM foo")), UnresolvedAlias.class);
        assertEquals("bar", a.sourceText());
    }

    public void testSelectScore() {
        UnresolvedAlias f = singleProjection(project(parseStatement("SELECT SCORE() FROM foo")), UnresolvedAlias.class);
        assertEquals("SCORE()", f.sourceText());
    }

    public void testSelectCast() {
        UnresolvedAlias f = singleProjection(project(parseStatement("SELECT CAST(POWER(languages, 2) AS DOUBLE) FROM foo")),
                UnresolvedAlias.class);
        assertEquals("CAST(POWER(languages, 2) AS DOUBLE)", f.sourceText());
    }

    public void testSelectCastOperator() {
        UnresolvedAlias f = singleProjection(project(parseStatement("SELECT POWER(languages, 2)::DOUBLE FROM foo")), UnresolvedAlias.class);
        assertEquals("POWER(languages, 2)::DOUBLE", f.sourceText());
    }

    public void testSelectCastWithSQLOperator() {
        UnresolvedAlias f = singleProjection(project(parseStatement("SELECT CONVERT(POWER(languages, 2), SQL_DOUBLE) FROM foo")),
                UnresolvedAlias.class);
        assertEquals("CONVERT(POWER(languages, 2), SQL_DOUBLE)", f.sourceText());
    }

    public void testSelectCastToEsType() {
        UnresolvedAlias f = singleProjection(project(parseStatement("SELECT CAST('0.' AS SCALED_FLOAT)")), UnresolvedAlias.class);
        assertEquals("CAST('0.' AS SCALED_FLOAT)", f.sourceText());
    }

    public void testSelectAddWithParanthesis() {
        UnresolvedAlias f = singleProjection(project(parseStatement("SELECT (1 +  2)")), UnresolvedAlias.class);
        assertEquals("(1 +  2)", f.sourceText());
    }

    public void testSelectRightFunction() {
        UnresolvedAlias f = singleProjection(project(parseStatement("SELECT RIGHT()")), UnresolvedAlias.class);
        assertEquals("RIGHT()", f.sourceText());
    }

    public void testsSelectNonReservedKeywords() {
        String[] reserved = new String[] {
            "ANALYZE", "ANALYZED", "CATALOGS", "COLUMNS", "CURRENT", "DAY", "DEBUG", "EXECUTABLE", "EXPLAIN",
            "FIRST", "FORMAT", "FULL", "FUNCTIONS", "GRAPHVIZ", "HOUR", "INTERVAL", "LAST", "LIMIT",
            "MAPPED", "MINUTE", "MONTH", "OPTIMIZED", "PARSED", "PHYSICAL", "PLAN", "QUERY", "RLIKE",
            "SCHEMAS", "SECOND", "SHOW", "SYS", "TABLES", "TEXT", "TYPE", "TYPES", "VERIFY", "YEAR"};
        StringJoiner sj = new StringJoiner(",");
        for (String s : reserved) {
            sj.add(s);
        }

        Project project = project(parseStatement("SELECT " + sj.toString() + " FROM foo"));
        assertEquals(reserved.length, project.projections().size());

        for (int i = 0; i < project.projections().size(); i++) {
            NamedExpression ne = project.projections().get(i);
            assertEquals(UnresolvedAlias.class, ne.getClass());
            assertEquals(reserved[i], ne.sourceText());
        }
    }

    public void testOrderByField() {
        Order.OrderDirection dir = randomFrom(Order.OrderDirection.values());
        OrderBy ob = orderBy(parseStatement("SELECT * FROM foo ORDER BY bar" + stringForDirection(dir)));
        assertThat(ob.order(), hasSize(1));
        Order o = ob.order().get(0);
        assertEquals(dir, o.direction());
        assertThat(o.child(), instanceOf(UnresolvedAttribute.class));
        UnresolvedAttribute a = (UnresolvedAttribute) o.child();
        assertEquals("bar", a.name());
    }

    public void testOrderByScore() {
        Order.OrderDirection dir = randomFrom(Order.OrderDirection.values());
        OrderBy ob = orderBy(parseStatement("SELECT * FROM foo ORDER BY SCORE( )" + stringForDirection(dir)));
        assertThat(ob.order(), hasSize(1));
        Order o = ob.order().get(0);
        assertEquals(dir, o.direction());
        assertThat(o.child(), instanceOf(UnresolvedFunction.class));
        UnresolvedFunction f = (UnresolvedFunction) o.child();
        assertEquals("SCORE( )", f.sourceText());
    }

    public void testOrderByTwo() {
        Order.OrderDirection dir0 = randomFrom(Order.OrderDirection.values());
        Order.OrderDirection dir1 = randomFrom(Order.OrderDirection.values());
        OrderBy ob = orderBy(parseStatement(
            "     SELECT *"
            + "     FROM foo"
            + " ORDER BY bar" + stringForDirection(dir0) + ", baz" + stringForDirection(dir1)));
        assertThat(ob.order(), hasSize(2));
        Order o = ob.order().get(0);
        assertEquals(dir0, o.direction());
        assertThat(o.child(), instanceOf(UnresolvedAttribute.class));
        UnresolvedAttribute a = (UnresolvedAttribute) o.child();
        assertEquals("bar", a.name());
        o = ob.order().get(1);
        assertEquals(dir1, o.direction());
        assertThat(o.child(), instanceOf(UnresolvedAttribute.class));
        a = (UnresolvedAttribute) o.child();
        assertEquals("baz", a.name());
    }

    public void testStringQuery() {
        LogicalPlan plan =
            parseStatement("SELECT * FROM FOO WHERE " +
                "QUERY('foo', 'default_field=last_name;lenient=true', 'fuzzy_rewrite=scoring_boolean')");

        StringQueryPredicate sqp = (StringQueryPredicate) ((Filter) plan.children().get(0).children().get(0)).condition();
        assertEquals("foo", sqp.query());
        assertEquals(3, sqp.optionMap().size());
        assertThat(sqp.optionMap(), hasEntry("default_field", "last_name"));
        assertThat(sqp.optionMap(), hasEntry("lenient", "true"));
        assertThat(sqp.optionMap(), hasEntry("fuzzy_rewrite", "scoring_boolean"));
    }

    public void testMatchQuery() {
        LogicalPlan plan = parseStatement("SELECT * FROM FOO WHERE " +
                    "MATCH(first_name, 'foo', 'operator=AND;lenient=true', 'fuzzy_rewrite=scoring_boolean')");

        MatchQueryPredicate mqp = (MatchQueryPredicate) ((Filter) plan.children().get(0).children().get(0)).condition();
        assertEquals("foo", mqp.query());
        assertEquals("?first_name", mqp.field().toString());
        assertEquals(3, mqp.optionMap().size());
        assertThat(mqp.optionMap(), hasEntry("operator", "AND"));
        assertThat(mqp.optionMap(), hasEntry("lenient", "true"));
        assertThat(mqp.optionMap(), hasEntry("fuzzy_rewrite", "scoring_boolean"));
    }

    public void testMultiMatchQuery() {
        LogicalPlan plan = parseStatement("SELECT * FROM FOO WHERE " +
                "MATCH('first_name,last_name', 'foo', 'operator=AND;type=best_fields', 'fuzzy_rewrite=scoring_boolean')");

        MultiMatchQueryPredicate mmqp = (MultiMatchQueryPredicate) ((Filter) plan.children().get(0).children().get(0)).condition();
        assertEquals("foo", mmqp.query());
        assertEquals("first_name,last_name", mmqp.fieldString());
        assertEquals(3, mmqp.optionMap().size());
        assertThat(mmqp.optionMap(), hasEntry("operator", "AND"));
        assertThat(mmqp.optionMap(), hasEntry("type", "best_fields"));
        assertThat(mmqp.optionMap(), hasEntry("fuzzy_rewrite", "scoring_boolean"));
    }

    public void testLimitToPreventStackOverflowFromLargeBinaryBooleanExpression() {
        // Create expression in the form of a = b OR a = b OR ... a = b

        // 1000 elements is ok
        new SqlParser().createExpression(Joiner.on(" OR ").join(nCopies(1000, "a = b")));

        // 5000 elements cause stack overflow
        ParsingException e = expectThrows(ParsingException.class, () ->
            new SqlParser().createExpression(Joiner.on(" OR ").join(nCopies(5000, "a = b"))));
        assertThat(e.getMessage(),
            startsWith("line -1:0: SQL statement is too large, causing stack overflow when generating the parsing tree: ["));
    }

    public void testLimitToPreventStackOverflowFromLargeUnaryArithmeticExpression() {
        // Create expression in the form of abs(abs(abs ... (i) ...)

        // 200 elements is ok
        new SqlParser().createExpression(
            Joiner.on("").join(nCopies(200, "abs(")).concat("i").concat(Joiner.on("").join(nCopies(200, ")"))));

        // 5000 elements cause stack overflow
        ParsingException e = expectThrows(ParsingException.class, () -> new SqlParser().createExpression(
            Joiner.on("").join(nCopies(1000, "abs(")).concat("i").concat(Joiner.on("").join(nCopies(1000, ")")))));
        assertThat(e.getMessage(),
            startsWith("line -1:0: SQL statement is too large, causing stack overflow when generating the parsing tree: ["));
    }

    public void testLimitToPreventStackOverflowFromLargeBinaryArithmeticExpression() {
        // Create expression in the form of a + a + a + ... + a

        // 1000 elements is ok
        new SqlParser().createExpression(Joiner.on(" + ").join(nCopies(1000, "a")));

        // 5000 elements cause stack overflow
        ParsingException e = expectThrows(ParsingException.class, () ->
            new SqlParser().createExpression(Joiner.on(" + ").join(nCopies(5000, "a"))));
        assertThat(e.getMessage(),
            startsWith("line -1:0: SQL statement is too large, causing stack overflow when generating the parsing tree: ["));
    }

    public void testLimitToPreventStackOverflowFromLargeSubselectTree() {
        // Test with queries in the form of `SELECT * FROM (SELECT * FROM (... t) ...)

        // 200 elements is ok
        new SqlParser().createStatement(
            Joiner.on(" (").join(nCopies(200, "SELECT * FROM"))
                .concat("t")
                .concat(Joiner.on("").join(nCopies(199, ")"))));

        // 500 elements cause stack overflow
        ParsingException e = expectThrows(ParsingException.class, () -> new SqlParser().createStatement(
            Joiner.on(" (").join(nCopies(500, "SELECT * FROM"))
                .concat("t")
                .concat(Joiner.on("").join(nCopies(499, ")")))));
        assertThat(e.getMessage(),
            startsWith("line -1:0: SQL statement is too large, causing stack overflow when generating the parsing tree: ["));
    }

    private LogicalPlan parseStatement(String sql) {
        return new SqlParser().createStatement(sql);
    }

    private Project project(LogicalPlan plan) {
        List<Project> sync = new ArrayList<>(1);
        projectRecur(plan, sync);
        assertThat("expected only one SELECT", sync, hasSize(1));
        return sync.get(0);
    }

    private void projectRecur(LogicalPlan plan, List<Project> sync) {
        if (plan instanceof Project) {
            sync.add((Project) plan);
            return;
        }
        for (LogicalPlan child : plan.children()) {
            projectRecur(child, sync);
        }
    }

    /**
     * Find the one and only {@code ORDER BY} in a plan.
     */
    private OrderBy orderBy(LogicalPlan plan) {
        List<LogicalPlan> l = plan.children().stream()
            .filter(c -> c instanceof OrderBy)
            .collect(toList());
        assertThat("expected only one ORDER BY", l, hasSize(1));
        return (OrderBy) l.get(0);
    }

    /**
     * Convert a direction into a string that represents that parses to
     * that direction.
     */
    private String stringForDirection(Order.OrderDirection dir) {
        String dirStr = dir.toString();
        return randomBoolean() && dirStr.equals("ASC") ? "" : " " + dirStr;
    }
}
