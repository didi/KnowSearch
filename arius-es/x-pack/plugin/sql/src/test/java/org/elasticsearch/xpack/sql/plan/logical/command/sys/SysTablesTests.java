/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.sql.plan.logical.command.sys;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.xpack.sql.TestUtils;
import org.elasticsearch.xpack.sql.analysis.analyzer.Analyzer;
import org.elasticsearch.xpack.sql.analysis.analyzer.Verifier;
import org.elasticsearch.xpack.sql.analysis.index.EsIndex;
import org.elasticsearch.xpack.sql.analysis.index.IndexResolution;
import org.elasticsearch.xpack.sql.analysis.index.IndexResolver;
import org.elasticsearch.xpack.sql.analysis.index.IndexResolver.IndexInfo;
import org.elasticsearch.xpack.sql.analysis.index.IndexResolver.IndexType;
import org.elasticsearch.xpack.sql.expression.function.FunctionRegistry;
import org.elasticsearch.xpack.sql.parser.SqlParser;
import org.elasticsearch.xpack.sql.plan.logical.command.Command;
import org.elasticsearch.xpack.sql.proto.Mode;
import org.elasticsearch.xpack.sql.proto.Protocol;
import org.elasticsearch.xpack.sql.proto.SqlTypedParamValue;
import org.elasticsearch.xpack.sql.session.Configuration;
import org.elasticsearch.xpack.sql.session.SchemaRowSet;
import org.elasticsearch.xpack.sql.session.SqlSession;
import org.elasticsearch.xpack.sql.stats.Metrics;
import org.elasticsearch.xpack.sql.type.DataTypes;
import org.elasticsearch.xpack.sql.type.EsField;
import org.elasticsearch.xpack.sql.type.TypesTests;
import org.elasticsearch.xpack.sql.util.DateUtils;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.elasticsearch.action.ActionListener.wrap;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SysTablesTests extends ESTestCase {

    private static final String CLUSTER_NAME = "cluster";

    private final SqlParser parser = new SqlParser();
    private final Map<String, EsField> mapping = TypesTests.loadMapping("mapping-multi-field-with-nested.json", true);
    private final IndexInfo index = new IndexInfo("test", IndexType.STANDARD_INDEX);
    private final IndexInfo alias = new IndexInfo("alias", IndexType.ALIAS);
    private final IndexInfo frozen = new IndexInfo("frozen", IndexType.FROZEN_INDEX);

    private final Configuration FROZEN_CFG = new Configuration(DateUtils.UTC, Protocol.FETCH_SIZE, Protocol.REQUEST_TIMEOUT,
            Protocol.PAGE_TIMEOUT, null, Mode.PLAIN, null, null, null, false, true);

    //
    // catalog enumeration
    //
    public void testSysTablesCatalogEnumerationWithEmptyType() throws Exception {
        executeCommand("SYS TABLES CATALOG LIKE '%' LIKE '' TYPE ''", r -> {
            assertEquals(1, r.size());
            assertEquals(CLUSTER_NAME, r.column(0));
            // everything else should be null
            for (int i = 1; i < 10; i++) {
                assertNull(r.column(i));
            }
        }, index);
    }

    public void testSysTablesCatalogAllTypes() throws Exception {
        executeCommand("SYS TABLES CATALOG LIKE '%' LIKE '' TYPE '%'", r -> {
            assertEquals(1, r.size());
            assertEquals(CLUSTER_NAME, r.column(0));
            // everything else should be null
            for (int i = 1; i < 10; i++) {
                assertNull(r.column(i));
            }
        }, new IndexInfo[0]);
    }

    // when types are null, consider them equivalent to '' for compatibility reasons
    public void testSysTablesCatalogNoTypes() throws Exception {
        executeCommand("SYS TABLES CATALOG LIKE '%' LIKE ''", r -> {
            assertEquals(1, r.size());
            assertEquals(CLUSTER_NAME, r.column(0));
            // everything else should be null
            for (int i = 1; i < 10; i++) {
                assertNull(r.column(i));
            }
        }, index);
    }


    //
    // table types enumeration
    //

    // missing type means pattern
    public void testSysTablesTypesEnumerationWoString() throws Exception {
        executeCommand("SYS TABLES CATALOG LIKE '' LIKE '' ", r -> {
            assertEquals(2, r.size());
            assertEquals("BASE TABLE", r.column(3));
            assertTrue(r.advanceRow());
            assertEquals("VIEW", r.column(3));
        }, alias, index);
    }

    public void testSysTablesTypesEnumeration() throws Exception {
        executeCommand("SYS TABLES CATALOG LIKE '' LIKE '' TYPE '%'", r -> {
            assertEquals(2, r.size());

            Iterator<IndexType> it = IndexType.VALID_REGULAR.stream().sorted(Comparator.comparing(IndexType::toSql)).iterator();

            for (int t = 0; t < r.size(); t++) {
                assertEquals(it.next().toSql(), r.column(3));

                // everything else should be null
                for (int i = 0; i < 10; i++) {
                    if (i != 3) {
                        assertNull(r.column(i));
                    }
                }

                r.advanceRow();
            }
        }, new IndexInfo[0]);
    }

    // when a type is specified, apply filtering
    public void testSysTablesTypesEnumerationAllCatalogsAndSpecifiedView() throws Exception {
        executeCommand("SYS TABLES CATALOG LIKE '%' LIKE '' TYPE 'VIEW'", r -> {
            assertEquals(0, r.size());
        }, new IndexInfo[0]);
    }

    public void testSysTablesDifferentCatalog() throws Exception {
        executeCommand("SYS TABLES CATALOG LIKE 'foo'", r -> {
            assertEquals(0, r.size());
            assertFalse(r.hasCurrentRow());
        });
    }

    public void testSysTablesNoTypes() throws Exception {
        executeCommand("SYS TABLES", r -> {
            assertEquals(2, r.size());
            assertEquals("test", r.column(2));
            assertEquals("BASE TABLE", r.column(3));
            assertTrue(r.advanceRow());
            assertEquals("alias", r.column(2));
            assertEquals("VIEW", r.column(3));
        }, index, alias);
    }

    public void testSysTablesNoTypesAndFrozen() throws Exception {
        executeCommand("SYS TABLES", r -> {
            assertEquals(3, r.size());
            assertEquals("frozen", r.column(2));
            assertEquals("BASE TABLE", r.column(3));
            assertTrue(r.advanceRow());
            assertEquals("test", r.column(2));
            assertEquals("BASE TABLE", r.column(3));
            assertTrue(r.advanceRow());
            assertEquals("alias", r.column(2));
            assertEquals("VIEW", r.column(3));
        }, FROZEN_CFG, index, alias, frozen);
    }

    public void testSysTablesWithLegacyTypes() throws Exception {
        executeCommand("SYS TABLES TYPE 'TABLE', 'ALIAS'", r -> {
            assertEquals(2, r.size());
            assertEquals("test", r.column(2));
            assertEquals("TABLE", r.column(3));
            assertTrue(r.advanceRow());
            assertEquals("alias", r.column(2));
            assertEquals("VIEW", r.column(3));
        }, index, alias);
    }

    public void testSysTablesWithProperTypes() throws Exception {
        executeCommand("SYS TABLES TYPE 'BASE TABLE', 'ALIAS'", r -> {
            assertEquals(2, r.size());
            assertEquals("test", r.column(2));
            assertEquals("BASE TABLE", r.column(3));
            assertTrue(r.advanceRow());
            assertEquals("alias", r.column(2));
            assertEquals("VIEW", r.column(3));
        }, index, alias);
    }

    public void testSysTablesWithProperTypesAndFrozen() throws Exception {
        executeCommand("SYS TABLES TYPE 'BASE TABLE', 'ALIAS'", r -> {
            assertEquals(3, r.size());
            assertEquals("frozen", r.column(2));
            assertEquals("BASE TABLE", r.column(3));
            assertTrue(r.advanceRow());
            assertEquals("test", r.column(2));
            assertEquals("BASE TABLE", r.column(3));
            assertTrue(r.advanceRow());
            assertEquals("alias", r.column(2));
            assertEquals("VIEW", r.column(3));
        }, index, frozen, alias);
    }

    public void testSysTablesPattern() throws Exception {
        executeCommand("SYS TABLES LIKE '%'", r -> {
            assertEquals(2, r.size());
            assertEquals("test", r.column(2));
            assertEquals("BASE TABLE", r.column(3));
            assertTrue(r.advanceRow());
            assertEquals("alias", r.column(2));
        }, index, alias);
    }

    public void testSysTablesPatternParameterized() throws Exception {
        List<SqlTypedParamValue> params = asList(param("%"));
        executeCommand("SYS TABLES LIKE ?", params, r -> {
            assertEquals("test", r.column(2));
            assertTrue(r.advanceRow());
            assertEquals(2, r.size());
            assertEquals("alias", r.column(2));
        }, alias, index);
    }

    public void testSysTablesOnlyAliases() throws Exception {
        executeCommand("SYS TABLES LIKE 'test' TYPE 'ALIAS'", r -> {
            assertEquals(1, r.size());
            assertEquals("alias", r.column(2));
        }, alias);
    }

    public void testSysTablesOnlyAliasesParameterized() throws Exception {
        List<SqlTypedParamValue> params = asList(param("ALIAS"));
        executeCommand("SYS TABLES LIKE 'test' TYPE ?", params, r -> {
            assertEquals(1, r.size());
            assertEquals("alias", r.column(2));
        }, alias);
    }

    public void testSysTablesOnlyIndices() throws Exception {
        executeCommand("SYS TABLES LIKE 'test' TYPE 'BASE TABLE'", r -> {
            assertEquals(1, r.size());
            assertEquals("test", r.column(2));
        }, index);
    }

    public void testSysTablesOnlyIndicesWithFrozen() throws Exception {
        executeCommand("SYS TABLES LIKE 'test' TYPE 'BASE TABLE'", r -> {
            assertEquals(2, r.size());
            assertEquals("frozen", r.column(2));
            assertTrue(r.advanceRow());
            assertEquals("test", r.column(2));
        }, index, frozen);
    }

    public void testSysTablesOnlyIndicesInLegacyMode() throws Exception {
        executeCommand("SYS TABLES LIKE 'test' TYPE 'TABLE'", r -> {
            assertEquals(1, r.size());
            assertEquals("test", r.column(2));
            assertEquals("TABLE", r.column(3));
        }, index);
    }

    public void testSysTablesNoPatternWithTypesSpecifiedInLegacyMode() throws Exception {
        executeCommand("SYS TABLES TYPE 'TABLE','VIEW'", r -> {
            assertEquals(2, r.size());
            assertEquals("test", r.column(2));
            assertEquals("TABLE", r.column(3));
            assertTrue(r.advanceRow());
            assertEquals("alias", r.column(2));
            assertEquals("VIEW", r.column(3));

        }, index, alias);
    }

    public void testSysTablesOnlyIndicesLegacyModeParameterized() throws Exception {
        executeCommand("SYS TABLES LIKE 'test' TYPE ?", asList(param("TABLE")), r -> {
            assertEquals(1, r.size());
            assertEquals("test", r.column(2));
            assertEquals("TABLE", r.column(3));
        }, index);
    }

    public void testSysTablesOnlyIndicesParameterized() throws Exception {
        executeCommand("SYS TABLES LIKE 'test' TYPE ?", asList(param("ALIAS")), r -> {
            assertEquals(1, r.size());
            assertEquals("test", r.column(2));
        }, index);
    }

    public void testSysTablesOnlyIndicesAndAliases() throws Exception {
        executeCommand("SYS TABLES LIKE 'test' TYPE 'VIEW', 'BASE TABLE'", r -> {
            assertEquals(2, r.size());
            assertEquals("test", r.column(2));
            assertTrue(r.advanceRow());
            assertEquals("alias", r.column(2));
        }, index, alias);
    }

    public void testSysTablesOnlyIndicesAndAliasesParameterized() throws Exception {
        List<SqlTypedParamValue> params = asList(param("VIEW"), param("BASE TABLE"));
        executeCommand("SYS TABLES LIKE 'test' TYPE ?, ?", params, r -> {
            assertEquals(2, r.size());
            assertEquals("test", r.column(2));
            assertTrue(r.advanceRow());
            assertEquals("alias", r.column(2));
        }, index, alias);
    }

    public void testSysTablesOnlyIndicesLegacyAndAliasesParameterized() throws Exception {
        List<SqlTypedParamValue> params = asList(param("VIEW"), param("TABLE"));
        executeCommand("SYS TABLES LIKE 'test' TYPE ?, ?", params, r -> {
            assertEquals(2, r.size());
            assertEquals("test", r.column(2));
            assertEquals("TABLE", r.column(3));
            assertTrue(r.advanceRow());
            assertEquals("alias", r.column(2));
            assertEquals("VIEW", r.column(3));
        }, index, alias);
    }

    public void testSysTablesWithCatalogOnlyAliases() throws Exception {
        executeCommand("SYS TABLES CATALOG LIKE '%' LIKE 'test' TYPE 'VIEW'", r -> {
            assertEquals(1, r.size());
            assertEquals("alias", r.column(2));
        }, alias);
    }

    public void testSysTablesWithEmptyCatalogOnlyAliases() throws Exception {
        executeCommand("SYS TABLES CATALOG LIKE '' LIKE 'test' TYPE 'VIEW'", r -> {
            assertEquals(0, r.size());
        }, alias);
    }

    public void testSysTablesWithInvalidType() throws Exception {
        executeCommand("SYS TABLES LIKE 'test' TYPE 'QUE HORA ES'", r -> {
            assertEquals(0, r.size());
        }, new IndexInfo[0]);
    }


    private SqlTypedParamValue param(Object value) {
        return new SqlTypedParamValue(DataTypes.fromJava(value).typeName, value);
    }

    private Tuple<Command, SqlSession> sql(String sql, List<SqlTypedParamValue> params, Configuration cfg) {
        EsIndex test = new EsIndex("test", mapping);
        Analyzer analyzer = new Analyzer(TestUtils.TEST_CFG, new FunctionRegistry(), IndexResolution.valid(test),
                                         new Verifier(new Metrics()));
        Command cmd = (Command) analyzer.analyze(parser.createStatement(sql, params), true);

        IndexResolver resolver = mock(IndexResolver.class);
        when(resolver.clusterName()).thenReturn(CLUSTER_NAME);

        SqlSession session = new SqlSession(cfg, null, null, resolver, null, null, null, null, null);
        return new Tuple<>(cmd, session);
    }

    private void executeCommand(String sql, Consumer<SchemaRowSet> consumer, IndexInfo... infos) throws Exception {
        executeCommand(sql, emptyList(), consumer, infos);
    }

    private void executeCommand(String sql, Consumer<SchemaRowSet> consumer, Configuration cfg, IndexInfo... infos) throws Exception {
        executeCommand(sql, emptyList(), consumer, cfg, infos);
    }

    private void executeCommand(String sql, List<SqlTypedParamValue> params, Consumer<SchemaRowSet> consumer, IndexInfo... infos)
            throws Exception {
        executeCommand(sql, params, consumer, TestUtils.TEST_CFG, infos);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void executeCommand(String sql, List<SqlTypedParamValue> params, Consumer<SchemaRowSet> consumer, Configuration cfg,
            IndexInfo... infos) throws Exception {
        Tuple<Command, SqlSession> tuple = sql(sql, params, cfg);

        IndexResolver resolver = tuple.v2().indexResolver();

        doAnswer(invocation -> {
            ((ActionListener) invocation.getArguments()[3]).onResponse(new LinkedHashSet<>(asList(infos)));
            return Void.TYPE;
        }).when(resolver).resolveNames(any(), any(), any(), any());

        tuple.v1().execute(tuple.v2(), wrap(p -> consumer.accept((SchemaRowSet) p.rowSet()), ex -> fail(ex.getMessage())));
    }
}
