package com.didi.arius.gateway.dsl.dsl.parser.query;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.dsl.dsl.ast.common.KeyWord;
import com.didi.arius.gateway.dsl.dsl.ast.common.Node;
import com.didi.arius.gateway.dsl.dsl.ast.common.key.FieldNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.key.StringNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.multi.NodeList;
import com.didi.arius.gateway.dsl.dsl.ast.common.multi.NodeMap;
import com.didi.arius.gateway.dsl.dsl.ast.common.value.QueryStringValueNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.value.StringListNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.value.ValueNode;
import com.didi.arius.gateway.dsl.dsl.ast.query.QueryString;
import com.didi.arius.gateway.dsl.dsl.parser.DslParser;
import com.didi.arius.gateway.dsl.dsl.parser.ParserType;
import com.didi.arius.gateway.dsl.query_string.ast.QSNode;
import com.didi.arius.gateway.dsl.query_string.parser.QSParser;
import com.didi.arius.gateway.dsl.util.Utils;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;

import java.util.HashSet;
import java.util.Set;

public class QueryStringParser extends DslParser {

    private static final ILog LOGGER = LogFactory.getLog(QueryStringParser.class);


    public QueryStringParser(ParserType type) {
        super(type);
    }

    @Override
    public KeyWord parse(String name, Object root) throws Exception {
        QueryString node = new QueryString(name);


        NodeMap nm = new NodeMap();
        JSONObject jsonObject = (JSONObject) root;

        for (String key : jsonObject.keySet()) {
            Object obj = jsonObject.get(key);

            if (key.equalsIgnoreCase("fields")) {
                nm.m.put(new StringNode(key), toFieldList((JSONArray) obj));

            } else if (key.equalsIgnoreCase("default_field")) {
                nm.m.put(new StringNode(key), getFieldNode((String) obj));

            } else if (key.equalsIgnoreCase("query")) {
                try {
                    QSParser qsParser = new QSParser();
                    QSNode qsNode = qsParser.parse((String) obj);

                    QueryStringValueNode vn = new QueryStringValueNode();
                    vn.setQsNode(qsNode);

                    nm.m.put(new StringNode(key), vn);
                } catch (Exception e) {
                    // 如果QSParser解析失败，就使用原来解析方式
                    LOGGER.error("QueryStringParser QSParser error {}, {}", obj, Utils.logExceptionStack(e));
                    nm.m.put(new StringNode(key), parserQueryString((String) obj));
                }

            } else {
                nm.m.put(new StringNode(key), ValueNode.getValueNode(obj));
            }
        }

        node.n = nm;

        return node;
    }

    private Node toFieldList(JSONArray array) throws Exception {
        NodeList node = new NodeList();

        for (Object obj : array) {
            if (obj instanceof JSON) {
                throw new Exception("wrong json, json:" + array);
            }

            node.l.add(getFieldNode((String) obj));
        }

        return node;
    }

    private Node getFieldNode(String field) {
        if (!field.contains("^")) {
            return new FieldNode(field);
        } else {
            int index = field.indexOf("^");
            StringListNode ret = new StringListNode();
            ret.l.l.add(new FieldNode(field.substring(0, index)));
            ret.l.l.add(new StringNode(field.substring(index)));
            return ret;
        }
    }

    /**
     * 解析query string
     *
     * @param query
     * @return
     */
    private Node parserQueryString(String query) {
        if (query == null) {
            return new StringNode(query);
        }

        if (query.trim().equalsIgnoreCase("*")) {
            return new FieldNode(query);
        }

        // 不包含:
        if (!query.contains(":")) {
            return new StringNode(query);
        }

        StringListNode ret = new StringListNode();
        String[] subStrs = query.split(":");
        for (int i = 0; i < subStrs.length - 1; i++) {
            String str = subStrs[i].trim();

            String field = getField(str);

            str = subStrs[i];

            int fieldStart = str.lastIndexOf(field);

            // 添加字段之前的部分
            if (fieldStart != 0) {
                ret.l.l.add(new StringNode(str.substring(0, fieldStart)));
            }

            // 添加字段部分
            ret.l.l.add(new FieldNode(field));
            // 添加字段之后的部分和:
            ret.l.l.add(new StringNode(str.substring(fieldStart + field.length()) + ":"));
        }

        // 添加最后按照:分隔的字符串
        ret.l.l.add(new StringNode(subStrs[subStrs.length - 1]));

        return ret;
    }

    private static Set<String> KW = new HashSet<>();

    static {
        KW.add(" ");
        KW.add("&&");
        KW.add("||");
        KW.add("!");
        KW.add("+");
        KW.add("-");
        KW.add("=");
        KW.add(">");
        KW.add("<");
        KW.add("(");
        KW.add(")");
        KW.add("[");
        KW.add("]");
        KW.add("{");
        KW.add("}");
    }

    /**
     * 获取字段
     *
     * @param str
     * @return
     */
    private String getField(String str) {
        int index = -1;
        for (String k : KW) {
            int i = str.lastIndexOf(k);
            if (i < 0) {
                continue;
            }

            i = i + k.length() - 1;
            if (i > index) {
                index = i;
            }
        }

        return str.substring(index + 1);
    }

}
