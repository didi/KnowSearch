package com.didi.arius.gateway.dsl.dsl.visitor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.didi.arius.gateway.dsl.dsl.ast.common.KeyWord;
import com.didi.arius.gateway.dsl.dsl.ast.common.Node;
import com.didi.arius.gateway.dsl.dsl.ast.common.key.KeyNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.multi.NodeList;
import com.didi.arius.gateway.dsl.dsl.ast.common.multi.NodeMap;
import com.didi.arius.gateway.dsl.dsl.ast.common.value.JsonNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.value.ObjectNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.value.QueryStringValueNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.value.StringListNode;
import com.didi.arius.gateway.dsl.dsl.ast.query.QueryString;
import com.didi.arius.gateway.dsl.dsl.ast.root.FieldDataFields;
import com.didi.arius.gateway.dsl.dsl.ast.root.Fields;
import com.didi.arius.gateway.dsl.dsl.ast.root.Source;
import com.didi.arius.gateway.dsl.dsl.visitor.basic.OutputVisitor;
import com.didi.arius.gateway.dsl.query_string.visitor.QSFormatVisitor;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class FormatVisitor extends OutputVisitor {

    private static final ILog LOGGER = LogFactory.getLog(FormatVisitor.class);

    @Override
    public void visit(Source node) {
        this.ret = "[?]";
    }

    @Override
    public void visit(Fields node) {
        this.ret = "?";
    }

    @Override
    public void visit(ObjectNode node) {
        ret = "?";
    }

    @Override
    public void visit(FieldDataFields node) {
        ret = "[?]";
    }

    @Override
    public void visit(QueryString node) {
        // 内部使用LinkedHashMap<String, Object>
        JSONObject root = new JSONObject(true);
        Map<String, Object> sortedMap = new TreeMap<>();

        // QueryString 中定义的node是NodeMap
        if (node.n instanceof NodeMap) {
            Node valueNode = null;
            NodeMap nodeMap = (NodeMap)node.n;

            for (KeyNode n : nodeMap.m.keySet()) {
                n.accept(this);
                String key = (String) ret;
                valueNode = nodeMap.m.get(n);

                // 如果query string解析失败，就使用原来的解析方式，结果是StringListNode类型
                if ("query".equalsIgnoreCase(key) && valueNode instanceof StringListNode) {
                    // 对query中的查询条件，简单用？替换
                    sortedMap.put(key, "?");

                } else if ("fields".equalsIgnoreCase(key)) {
                    sortedMap.put(key, "[?]");

                } else if ("default_field".equalsIgnoreCase(key)) {
                    sortedMap.put(key, "?");

                } else {
                    valueNode.accept(this);
                    Object value = ret;
                    sortedMap.put(key, value);
                }
            }
        }

        root.putAll(sortedMap);
        this.ret = root;
    }

    @Override
    public void visit(QueryStringValueNode node) {
        QSFormatVisitor formatVisitor = new QSFormatVisitor();
        node.getQsNode().accept(formatVisitor);
        ret = formatVisitor.output();
    }

    /**
     * node map 排序
     *
     * @param node
     */
    @Override
    public void visit(NodeMap node) {
        // 内部使用LinkedHashMap<String, Object>
        JSONObject root = new JSONObject(true);

        Map<String, Object> sortedMap = new TreeMap<>();
        for (KeyNode n : node.m.keySet()) {
            n.accept(this);
            String key = (String) ret;

            node.m.get(n).accept(this);
            Object value = ret;
            sortedMap.put(key, value);
        }

        root.putAll(sortedMap);
        this.ret = root;
    }

    /**
     * node 集合格式化并排序
     *
     * @param node
     */
    @Override
    public void visit(NodeList node) {
        List<Object> l = new ArrayList<>();

        for (Node n : node.l) {
            if (n instanceof KeyWord) {
                JSONObject obj = new JSONObject();
                n.accept(this);
                obj.put(((KeyWord) n).getName(), ret);
            } else {
                n.accept(this);
            }

            l.add(ret);
        }

        // 对list中完全相同的项去重
        List<Object> tmp = new ArrayList<>();
        Set<String> exist = new HashSet<>();
        for (Object obj : l) {
            String str = JSON.toJSONString(obj);
            if (exist.contains(str)) {
                continue;
            } else {
                tmp.add(obj);
                exist.add(str);
            }
        }

        l = tmp;
        Collections.sort(l, new Comparator<Object>() {

            @Override
            public int compare(Object o1, Object o2) {
                if (o1 == o2) {
                    return 0;
                }

                if (o1 == null) {
                    return -1;
                }

                if (o2 == null) {
                    return 1;
                }

                return o1.toString().compareTo(o2.toString());
            }
        });

        JSONArray array = new JSONArray();
        for (Object o : l) {
            array.add(o);
        }

        ret = array;
    }

    public void visit(JsonNode node) {
        JSON json = clone(node.json);

        if (json instanceof JSONObject) {
            doJsonObject((JSONObject) json);
        }

        if (node.json instanceof JSONArray) {
            doJsonArray((JSONArray) json);
        }

        ret = json;
    }

    public void doJsonObject(JSONObject object) {
        for (String key : object.keySet()) {
            Object o = object.get(key);

            if (o instanceof JSONObject) {
                doJsonObject((JSONObject) o);
                continue;
            }

            if (o instanceof JSONArray) {
                doJsonArray((JSONArray) o);
                continue;
            }

            object.put(key, "?");
        }
    }

    public void doJsonArray(JSONArray array) {
        List<Object> l = new ArrayList<>();

        Set<String> exist = new HashSet<>();
        for (Object o : array) {
            if (o instanceof JSONObject) {
                doJsonObject((JSONObject) o);

            } else if (o instanceof JSONArray) {
                doJsonArray((JSONArray) o);

            } else {
                o = "?";
            }

            String str = JSON.toJSONString(o);
            if (exist.contains(str)) {
                continue;
            }

            exist.add(str);
            l.add(o);
        }

        // 排序
        Collections.sort(l, new Comparator<Object>() {

            @Override
            public int compare(Object o1, Object o2) {
                if (o1 == o2) {
                    return 0;
                }

                if (o1 == null) {
                    return -1;
                }

                if (o2 == null) {
                    return 1;
                }

                return o1.toString().compareTo(o2.toString());
            }
        });

        array.clear();
        for (Object o : l) {
            array.add(o);
        }
    }

    public JSON clone(JSON json) {
        return (JSON) JSON.parse(JSON.toJSONString(json, SerializerFeature.WriteMapNullValue));
    }

}
