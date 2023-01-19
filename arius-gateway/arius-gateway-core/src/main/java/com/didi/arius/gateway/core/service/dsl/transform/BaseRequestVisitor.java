package com.didi.arius.gateway.core.service.dsl.transform;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didiglobal.knowframework.dsl.parse.dsl.ast.DslNode;
import com.didiglobal.knowframework.dsl.parse.dsl.ast.aggr.AggrTerms;
import com.didiglobal.knowframework.dsl.parse.dsl.ast.aggr.SignificantTerms;
import com.didiglobal.knowframework.dsl.parse.dsl.ast.common.KeyWord;
import com.didiglobal.knowframework.dsl.parse.dsl.ast.common.Node;
import com.didiglobal.knowframework.dsl.parse.dsl.ast.common.key.KeyNode;
import com.didiglobal.knowframework.dsl.parse.dsl.ast.common.logic.*;
import com.didiglobal.knowframework.dsl.parse.dsl.ast.common.multi.NodeList;
import com.didiglobal.knowframework.dsl.parse.dsl.ast.common.multi.NodeMap;
import com.didiglobal.knowframework.dsl.parse.dsl.ast.common.script.Script;
import com.didiglobal.knowframework.dsl.parse.dsl.ast.common.value.ObjectNode;
import com.didiglobal.knowframework.dsl.parse.dsl.ast.query.*;
import com.didiglobal.knowframework.dsl.parse.dsl.ast.root.Sort;
import com.didiglobal.knowframework.dsl.parse.dsl.ast.root.Timeout;
import com.didiglobal.knowframework.dsl.parse.dsl.visitor.basic.OutputVisitor;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;

public class BaseRequestVisitor extends OutputVisitor {

    protected static final ILog logger = LogFactory.getLog(BaseRequestVisitor.class);

    protected static final String TYPE = "_type";

    protected static final String MATCH_ALL = "match_all";

    protected static final String ORDER = "order";

    protected static final String QUERY = "query";

    protected static final String QUERY_STRING = "query_string";


    /**
     * match不再支持type，旧版本的三种type中，phrase改为match_phrase查询，
     * phrase_prefix改为match_phrase_prefix查询
     * @param node
     */
    @Override
    public void visit(Match node) {
        super.visit(node);

        JSONObject obj = (JSONObject) this.ret;
        for (Map.Entry<String,Object> entry : obj.entrySet()) {
            Object value = entry.getValue();
            String key = entry.getKey();
            if (value instanceof JSONObject) {
                Object type = ((JSONObject) value).get("type");
                ((JSONObject) value).remove("type");
                if (type != null) {
                    if (type.equals("phrase")) {
                        node.setName("match_phrase");
                    } else if (type.equals("phrase_prefix") || type.equals("phrasePrefix")) {
                        node.setName("match_phrase_prefix");
                    }

                }

                if (key.equals(TYPE)) {
                    node.setName(MATCH_ALL);
                    obj.clear();
                    break;
                }
            }
        }
    }

    /**
     * 处理排序中兼容
     *
     * @param node
     */
    @Override
    public void visit(Sort node) {
        super.visit(node);

        if (this.ret instanceof JSONArray) {
            JSONArray arr = (JSONArray) this.ret;
            for (int i = 0 ; i < arr.size(); ++i) {
                Object obj = arr.get(i);
                if (obj instanceof JSONObject) {
                    JSONObject jObj = (JSONObject) obj;
                    transformSortItem(jObj);
                }
            }
        } else if (this.ret instanceof JSONObject) {
            transformSortItem((JSONObject) this.ret);
        }
    }

    private void transformSortItem(JSONObject sortItem) {
        for (Map.Entry<String,Object> entry : sortItem.entrySet()) {
            String key = entry.getKey();
            if (key.equals("_geo_distance") || key.equals("_geoDistance")) {
                continue;
            }

            Object sObj = sortItem.get(key);
            if (sObj instanceof JSONObject) {
                JSONObject iObj = (JSONObject) sObj;
                if (iObj.containsKey("ignore_unmapped")) {
                    iObj.remove("ignore_unmapped");
                }

                // 对排序字段没有指定排序方式时，在低版本中默认为降序，则添加desc
                if (iObj.containsKey(ORDER) && StringUtils.isBlank(iObj.getString(ORDER))) {
                    iObj.put(ORDER, "desc");
                }
            }

            // _score字段排序时去掉unmapped_type
            if ("_score".equals(key) && sObj instanceof JSONObject) {
                JSONObject iObj = (JSONObject) sObj;
                if (iObj.containsKey("unmapped_type")) {
                    iObj.remove("unmapped_type");
                }
            }
        }
    }

    /**
     * 不再支持Filtered关键字，改成bool查询
     * @param node
     */
    @Override
    public void visit(Filtered node) {
        super.visit(node);

        JSONObject obj = (JSONObject) this.ret;
        node.setName("bool");


        if (obj.containsKey(QUERY)) {
            Object query = obj.remove(QUERY);
            obj.put("must", query);
        } else if (obj.containsKey(QUERY_STRING)) {
            Object queryString = obj.remove(QUERY_STRING);
            JSONObject query = new JSONObject();
            query.put(QUERY_STRING, queryString);
            obj.put("must", query);
        }
    }

    /**
     * 不再支持fields和fielddata_fields关键字，直接过滤
     * @param node
     */
    @Override
    public void visit(DslNode node) {
        super.visit(node);
        JSONObject obj = (JSONObject) this.ret;

        if (obj.containsKey("fields")) {
            obj.remove("fields");
        }

        if (obj.containsKey("fielddata_fields")) {
            obj.remove("fielddata_fields");
        }
    }

    private static final int DEFAULT_TERMS_SIZE = 1000;

    /**
     * aggs的terms不再支持size=0，改成默认1000
     * @param node
     */
    @Override
    public void visit(AggrTerms node) {
        super.visit(node);

        JSONObject obj = (JSONObject) this.ret;
        if (obj.containsKey("size")) {
            int size = obj.getIntValue("size");
            if (size == 0) {
                obj.put("size", DEFAULT_TERMS_SIZE);
            }
        }
    }

    /**
     * aggs的SignificantTerms不再支持size=0，改成默认1000
     *
     * @param node
     */
    @Override
    public void visit(SignificantTerms node) {
        super.visit(node);

        JSONObject obj = (JSONObject) this.ret;
        if (obj.containsKey("size")) {
            int size = obj.getIntValue("size");
            if (size == 0) {
                obj.put("size", DEFAULT_TERMS_SIZE);
            }
        }
    }

    /**
     * 不再支持missing，改成must_not和exists的组合
     * @param node
     */
    @Override
    public void visit(Missing node) {
        super.visit(node);

        JSONObject obj = (JSONObject) this.ret;
        String value = obj.getString("field");
        if (value == null) {
             return ;
        }

        node.setName("bool");
        this.ret = JSON.parseObject(String.format("{\"must_not\":[{\"exists\":{\"field\":\"%s\"}}]}", value));
    }

    /**
     * 对于dsl语句中timeout没有加单位的在6.x查询出错场景，加上单位ms
     *
     * @param node
     */
    @Override
    public void visit(Timeout node) {
        super.visit(node);

        try {
            // 为数值类型时，添加单位ms
            if (this.ret instanceof Short || this.ret instanceof Integer || this.ret instanceof Long) {
                String timeOutValue = String.format("%dms", this.ret);
                node.v = new ObjectNode(timeOutValue);
                this.ret = timeOutValue;
            }
        } catch (Exception e) {
            logger.warn("parse timeout exception", e);
        }
    }

    @Override
    public void visit(Term node) {
        super.visit(node);
        JSONObject obj = (JSONObject) this.ret;
        for (String key : obj.keySet()) {
            if (key.equals(TYPE)) {
                node.setName(MATCH_ALL);
                obj.clear();
                break;
            }
        }
    }

    @Override
    public void visit(Terms node) {
        super.visit(node);
        JSONObject obj = (JSONObject) this.ret;
        for (String key : obj.keySet()) {
            if (key.equals(TYPE)) {
                node.setName(MATCH_ALL);
                obj.clear();
                break;
            }
        }
    }


    /**
     * 高版本不再支持not，改成must_not
     * @param node
     */
    @Override
    public void visit(Not node) {
        super.visit(node);

        JSONObject obj = (JSONObject) this.ret;
        node.setName("bool");
        JSONObject boolNode = new JSONObject();
        JSONArray mustNotNode = new JSONArray();
        boolNode.put("must_not", mustNotNode);

        if (obj.size() == 1) {
            if (obj.containsKey("filter")) {
                obj = obj.getJSONObject("filter");
            } else if (obj.containsKey(QUERY)) {
                obj = obj.getJSONObject(QUERY);
            }
        }

        mustNotNode.add(obj);
        this.ret = boolNode;
    }

    /**
     * 高版本不再支持not，改成must
     * @param node
     */
    @Override
    public void visit(And node) {
        super.visit(node);
        visitByConditionType(node, "must");
    }

    private void visitByConditionType(KeyWord node, String conditionType) {
        node.setName("bool");
        if (this.ret instanceof JSONArray) {
            JSONObject boolNode = new JSONObject();
            boolNode.put(conditionType, this.ret);
            this.ret = boolNode;
        } else if (this.ret instanceof JSONObject){
            JSONObject obj = (JSONObject) this.ret;

            JSONArray allArr = new JSONArray();
            for (Map.Entry<String,Object> entry : obj.entrySet()) {
                if (entry.getValue() instanceof  JSONArray) {
                    JSONArray arr = obj.getJSONArray(entry.getKey());
                    allArr.addAll(arr);
                }
            }

            JSONObject boolNode = new JSONObject();
            boolNode.put(conditionType, allArr);
            this.ret = boolNode;
        }
    }

    /**
     * 高版本不再支持not，改成should
     * @param node
     */
    @Override
    public void visit(Or node) {
        super.visit(node);
        visitByConditionType(node, "should");
    }

    @Override
    public void visit(Script node) {
        super.visit(node);

        if (this.ret instanceof JSONObject) {
            JSONObject obj = (JSONObject) this.ret;
            if (obj.containsKey("script")) {
                JSONObject script = obj.getJSONObject("script");
                transformScript(script);
            } else {
                transformScript(obj);
            }


        }
    }

    @Override
    public void visit(Queryquery node) {
        super.visit(node);

        if (this.ret instanceof JSONObject) {
            JSONObject obj = (JSONObject) this.ret;
            if (obj.size() == 1 && obj.containsKey(QUERY_STRING)) {
                this.ret = obj.get(QUERY_STRING);
                node.setName(QUERY_STRING);
            }
        }
    }

    @Override
    public void visit(Must node) {
        visitBoolLogic(node.n);
    }

    @Override
    public void visit(MustNot node) {
        visitBoolLogic(node.n);
    }

    @Override
    public void visit(Should node) {
        visitBoolLogic(node.n);
    }

    @Override
    public void visit(Filter node) {
        visitBoolLogic(node.n);
    }

    private void visitBoolLogic(Node n) {
        if (n instanceof NodeList) {
            JSONArray array = new JSONArray();
            for (Node k : ((NodeList) n).l) {
                if (k instanceof NodeMap) {
                    NodeMap map = (NodeMap) k;
                    array.add(parseBoolItem(map));
                } else if (k instanceof NodeList) {
                    NodeList nl = (NodeList)k;
                    for (Node node : nl.l) {
                        if (node instanceof NodeMap) {
                            NodeMap map = (NodeMap) node;
                            array.add(parseBoolItem(map));
                        }
                    }
                }
            }

            this.ret = array;
        } else if (n instanceof NodeMap) {
            this.ret = parseBoolItem((NodeMap) n);
        } else if (n instanceof ObjectNode && ((ObjectNode) n).value == null) {
            this.ret = null;
        }
    }

    /**
     * bool item里出现query关键字，则直接去掉query，将query内容上提
     * @param map
     * @return
     */
    private JSONObject parseBoolItem(NodeMap map) {
        JSONObject jsonMap = new JSONObject();
        for (KeyNode kn : map.m.keySet()) {
            Node valueNode = map.m.get(kn);

            if (kn.getValue().equalsIgnoreCase(QUERY)) {
                valueNode.accept(this);

                JSONObject obj = (JSONObject) this.ret;

                if (valueNode instanceof KeyWord
                        && ((KeyWord) valueNode).getName().equals(QUERY_STRING)) {
                    jsonMap.put(QUERY_STRING, obj);
                } else if (obj.size() == 1) {
                    Map.Entry<String, Object> entry = obj.entrySet().iterator().next();
                    String key = entry.getKey();
                    Object value = entry.getValue();

                    jsonMap.put(key, value);
                }
            } else {
                valueNode.accept(this);

                if (valueNode instanceof KeyWord) {
                    jsonMap.put(((KeyWord)valueNode).getName(), this.ret);
                } else {
                    jsonMap.put(kn.getValue(), this.ret);
                }

            }
        }

        return jsonMap;
    }


    private void transformScript(JSONObject script) {
        if (script.containsKey("lang")) {
            String lang = script.getString("lang");
            if (lang.equalsIgnoreCase("inline")
                    || lang.equalsIgnoreCase("groovy")) {
                script.put("lang", "painless");
            }
        }
    }
}
