package com.didi.arius.gateway.dsl;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.alibaba.druid.sql.visitor.SQLASTOutputVisitor;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.didi.arius.gateway.dsl.bean.ExtractResult;
import com.didi.arius.gateway.dsl.dsl.ast.DslNode;
import com.didi.arius.gateway.dsl.dsl.parser.DslParser;
import com.didi.arius.gateway.dsl.dsl.visitor.DslDangerousTagVisitor;
import com.didi.arius.gateway.dsl.dsl.visitor.EsDslExportParameterVisitor;
import com.didi.arius.gateway.dsl.dsl.visitor.FormatVisitor;
import com.didi.arius.gateway.dsl.sql.EsExportParameterVisitor;
import com.didi.arius.gateway.dsl.sql.EsSqlFormatVisitor;
import com.didi.arius.gateway.dsl.sql.SqlDangerousTagVisitor;
import com.didi.arius.gateway.dsl.util.Utils;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.nlpcn.es4sql.parse.ElasticSqlExprParser;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @Author: D10865
 * @Description:
 * @Date: Create on 2018/5/29 下午3:53
 * @Modified By
 * <p>
 * 查询模板提取工具
 */
public class DslExtractionUtilV2 {

    private static final ILog LOGGER = LogFactory.getLog(DslExtractionUtilV2.class);

    /**
     * 版本标识符
     */
    private static final String VERSION_FLAG = "V2_";

    /**
     * 提取dsl语句成查询模板
     *
     * @param dslContent
     * @return
     */
    public static ExtractResult extractDsl(String dslContent) {
        String dslTemplate = "";
        ExtractResult extractResult = null;

        // 判断传入dsl语句是否为空
        if (StringUtils.isBlank(dslContent)) {
            dslTemplate = "{}";
            return new ExtractResult(dslTemplate, "", "dsl", Utils.getMD5WithVersion(dslTemplate, VERSION_FLAG), "{}", "normal",
                    "", "", "", "");
        }

        // 判断查询类型，dsl,sql
        String searchType = dslContent.startsWith("{") ? "dsl" : (dslContent.startsWith("select") || dslContent.startsWith("SELECT") ? "sql" : "unknown");

        // 由于http方式时，用户dsl语句含有空格，例如 {    \"size\":1, ，会误判为scroll方式
        if ("unknown".equals(searchType) && dslContent.contains("{") && dslContent.contains("}")) {
            searchType = "dsl";
        }

        if ("sql".equals(searchType)) {
            extractResult = formatSql(dslContent);

        } else if ("dsl".equals(searchType)) {
            extractResult = formatDsl(dslContent);

        } else {
            // 这里依赖查询方式，会误判为scroll方式
            extractResult = new ExtractResult();
            extractResult.buildScrollResult(dslContent, VERSION_FLAG);
        }

        return extractResult;
    }

    /**
     * 提取sql语句成查询模板
     *  查询语句，查询模板,索引名称,selectFields,whereFields,groupByFields,sortByFields
     *  6个元素
     * @param sql
     * @return
     */
    private static ExtractResult formatSql(String sql) {
        ExtractResult extractResult = new ExtractResult();

        try {
            extractResult.setSearchType("sql");
            extractResult.setDsl(sql);

            SQLExprParser parser = new ElasticSqlExprParser(sql);
            SQLExpr expr = parser.expr();
            SQLQueryExpr sqlExpr = (SQLQueryExpr) expr;

            // 提取查询模板
            SQLASTOutputVisitor esSqlOutputVisitor = new EsSqlFormatVisitor(new StringBuilder());
            esSqlOutputVisitor.setPrettyFormat(false);
            sqlExpr.accept(esSqlOutputVisitor);

            String sqlTemplate = esSqlOutputVisitor.getAppender().toString();
            extractResult.setDslTemplate(sqlTemplate);
            extractResult.setDslTemplateMd5(Utils.getMD5WithVersion(sqlTemplate, VERSION_FLAG));

            // 提取字段
            EsExportParameterVisitor esExportParameterVisitor = new EsExportParameterVisitor();
            sqlExpr.accept(esExportParameterVisitor);

            extractResult.setIndices(esExportParameterVisitor.getTableName());
            extractResult.setSelectFields(esExportParameterVisitor.getSelectFieldNames());
            extractResult.setWhereFields(esExportParameterVisitor.getWhereFieldsNames());
            extractResult.setGroupByFields(esExportParameterVisitor.getGroupByFieldNames());
            extractResult.setSortByFields(esExportParameterVisitor.getOrderByFieldNames());

            // 聚合使用的字段不为空 或者 含有GROUP BY 关键字
            if (StringUtils.isNotBlank(esExportParameterVisitor.getGroupByFieldNames())
                    || sqlTemplate.contains(" GROUP BY ")) {
                extractResult.setDslType("aggs");
            } else {
                //除了聚合之外都是普通查询
                extractResult.setDslType("normal");
            }

            // 危害dsl查询语句标签遍历器
            SqlDangerousTagVisitor sqlDangerousTagVisitor = new SqlDangerousTagVisitor(new StringBuilder());
            sqlExpr.accept(sqlDangerousTagVisitor);
            extractResult.setTags(sqlDangerousTagVisitor.getDangerousTags());

        } catch (Exception e) {
            LOGGER.error("formatSql {}, Exception {}", sql, Utils.logExceptionStack(e));
            extractResult.buildFailedResult(VERSION_FLAG);

        } catch (Throwable t) {
            LOGGER.error("formatSql {}, Throwable {}", sql, Utils.logExceptionStack(t));
            extractResult.buildFailedResult(VERSION_FLAG);
        }

        return extractResult;
    }

    /**
     * 提取dsl语句成查询模板
     *
     * @param dsl
     * @return
     */
    private static ExtractResult formatDsl(String dsl) {
        ExtractResult extractResult = new ExtractResult();
        extractResult.setSearchType("dsl");

        String orginalDsl = new String(dsl);
        //需要对多个查询模板进行去重
        Set<String> dslTemplateSet = Sets.newLinkedHashSet();
        Set<String> selectFieldsSet = Sets.newHashSet();
        Set<String> whereFieldsSet = Sets.newHashSet();
        Set<String> groupByFieldsSet = Sets.newHashSet();
        Set<String> sortByFieldsSet = Sets.newHashSet();
        List<String> dslTemplateMd5List = Lists.newLinkedList();
        Set<String> tags = Sets.newHashSet();

        try {
            // 由于fastjson JSON.parseObject {"size":0,"query":{"match":{"$ref":"@"}},"aggs":{"ip":{"terms":{"field":"ipsrc"}}}} 中$ref重新指向match
            dsl = dsl.replaceAll("\\\"\\$ref\\\"", "\"ref\"");
            List<String> dslList = Lists.newArrayList();

            DefaultJSONParser parser = null;
            Object obj = null;

            // 解析多个json，直到pos为0
            for (;;) {
                try {
                    // 这里需要Feature.OrderedField.getMask()保持有序
                    parser = new DefaultJSONParser(dsl, ParserConfig.getGlobalInstance(), JSON.DEFAULT_PARSER_FEATURE | Feature.OrderedField.getMask());
                    obj = parser.parse();
                } catch (Throwable t) {
                    LOGGER.error("formatDsl [{}], [{}], Throwable {}", dsl, orginalDsl, Utils.logExceptionStack(t));
                }
                if (obj == null) {
                    break;
                }

                if (obj instanceof JSONObject) {
                    dslList.add(JSONObject.toJSONString(obj, SerializerFeature.WriteMapNullValue));
                    int pos = parser.getLexer().pos();
                    if (pos <= 0) {
                        break;
                    }
                    dsl = dsl.substring(pos);
                    parser.getLexer().close();
                } else {
                    parser.getLexer().close();
                    break;
                }
            }

            // 遍历dsl查询语句进行提取
            for (String dslContent : dslList) {

                JSONObject jsonObject = JSON.parseObject(dslContent);
                DslNode node = DslParser.parse(jsonObject);
                FormatVisitor formatVisitor = new FormatVisitor();
                node.accept(formatVisitor);

                EsDslExportParameterVisitor esDslExportParameterVisitor = new EsDslExportParameterVisitor();
                node.accept(esDslExportParameterVisitor);

                String dslTemplate = formatVisitor.ret.toString();
                dslTemplateSet.add(dslTemplate);
                dslTemplateMd5List.add(Utils.getMD5WithVersion(dslTemplate, VERSION_FLAG));
                Utils.addSetItemWithCommSplit(selectFieldsSet, esDslExportParameterVisitor.getSelectFieldNames());
                Utils.addSetItemWithCommSplit(whereFieldsSet, esDslExportParameterVisitor.getWhereFieldsNames());
                Utils.addSetItemWithCommSplit(groupByFieldsSet, esDslExportParameterVisitor.getGroupByFieldNames());
                Utils.addSetItemWithCommSplit(sortByFieldsSet, esDslExportParameterVisitor.getOrderByFieldNames());

                // 危害dsl查询语句标签遍历器
                DslDangerousTagVisitor dslDangerousTagVisitor = new DslDangerousTagVisitor();
                node.accept(dslDangerousTagVisitor);
                tags.addAll(dslDangerousTagVisitor.getDangerousTags());
            }

            extractResult.setDsl(StringUtils.join(dslList, ""));
            extractResult.setDslTemplate(StringUtils.join(dslTemplateSet, ""));
            extractResult.setIndices("");   // 索引名称为空
            extractResult.setSelectFields(StringUtils.join(selectFieldsSet, ","));
            extractResult.setWhereFields(StringUtils.join(whereFieldsSet, ","));
            extractResult.setGroupByFields(StringUtils.join(groupByFieldsSet, ","));
            extractResult.setSortByFields(StringUtils.join(sortByFieldsSet, ","));
            // 设置md5
            extractResult.setDslTemplateMd5(Utils.getMD5WithVersion(extractResult.getDslTemplate(), VERSION_FLAG));
            extractResult.setTags(tags);

            // 判断dsl中是否有聚合字段aggs, aggregations
            if (extractResult.getDslTemplate().contains("\"aggregations\"")
                    || extractResult.getDslTemplate().contains("\"aggs\"")) {
                extractResult.setDslType("aggs");
            } else {
                //除了聚合之外都是普通查询
                extractResult.setDslType("normal");
            }

            // 查询语句个数大于1，是msearch
            if (dslTemplateMd5List.size() > 1) {
                // 提取msearch查询语句的模板列表
                extractResult.setDslTemplateMd5List(dslTemplateMd5List);
                // msearch查询语句列表
                extractResult.setDslList(dslList);
                // msearch查询语句的模板列表
                extractResult.setDslTemplateList(Arrays.asList(dslTemplateSet.toArray(new String[]{})));
            }

        } catch (Exception e) {
            LOGGER.error("formatDsl {}, Exception {}", orginalDsl, Utils.logExceptionStack(e));
            extractResult.buildFailedResult(VERSION_FLAG);
            extractResult.setDsl(orginalDsl);

        } catch (Throwable t) {
            LOGGER.error("formatDsl {}, Throwable {}", orginalDsl, Utils.logExceptionStack(t));
            extractResult.buildFailedResult(VERSION_FLAG);
            extractResult.setDsl(orginalDsl);
        }

        return extractResult;
    }

}
