package com.didi.arius.gateway.core.service.dsl.transform;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.dsl.dsl.ast.DslNode;
import com.didi.arius.gateway.dsl.dsl.ast.aggr.AggrTerms;
import com.didi.arius.gateway.dsl.dsl.ast.aggr.SignificantTerms;
import com.didi.arius.gateway.dsl.dsl.ast.common.KeyWord;
import com.didi.arius.gateway.dsl.dsl.ast.common.Node;
import com.didi.arius.gateway.dsl.dsl.ast.common.key.KeyNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.logic.*;
import com.didi.arius.gateway.dsl.dsl.ast.common.multi.NodeList;
import com.didi.arius.gateway.dsl.dsl.ast.common.multi.NodeMap;
import com.didi.arius.gateway.dsl.dsl.ast.common.script.Script;
import com.didi.arius.gateway.dsl.dsl.ast.common.value.ObjectNode;
import com.didi.arius.gateway.dsl.dsl.ast.query.*;
import com.didi.arius.gateway.dsl.dsl.ast.root.Sort;
import com.didi.arius.gateway.dsl.dsl.ast.root.Timeout;
import com.didi.arius.gateway.dsl.dsl.visitor.basic.OutputVisitor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class BaseRequestVisitor extends OutputVisitor {

    protected static final Logger logger = LoggerFactory.getLogger(BaseRequestVisitor.class);

    /**
     * match不再支持type，旧版本的三种type中，phrase改为match_phrase查询，
     * phrase_prefix改为match_phrase_prefix查询
     * @param node
     */
    @Override
    public void visit(Match node) {
        super.visit(node);

        JSONObject obj = (JSONObject) this.ret;
        for (String key : obj.keySet()) {
            Object value = obj.get(key);
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

                if (key.equals("_type")) {
                    node.setName("match_all");
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
        for (String key : sortItem.keySet()) {
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
                if (iObj.containsKey("order") && StringUtils.isBlank(iObj.getString("order"))) {
                    iObj.put("order", "desc");
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


        if (obj.containsKey("query")) {
            Object query = obj.remove("query");
            obj.put("must", query);
        } else if (obj.containsKey("query_string")) {
            Object queryString = obj.remove("query_string");
            JSONObject query = new JSONObject();
            query.put("query_string", queryString);
            obj.put("must", query);
        }

//            this.ret = new JSONObject();
//
//        if (obj.size() != 1 || ((obj.size() == 1 && !obj.containsKey("filter")))) {
//            ((JSONObject) this.ret).put("must", obj);
//        }
//
//        if (obj.containsKey("filter")) {
//            Object filter = obj.remove("filter");
//            ((JSONObject) this.ret).put("filter", filter);
//        }
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
            if (key.equals("_type")) {
                node.setName("match_all");
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
            if (key.equals("_type")) {
                node.setName("match_all");
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
            } else if (obj.containsKey("query")) {
                obj = obj.getJSONObject("query");
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

        node.setName("bool");
        if (this.ret instanceof JSONArray) {
            JSONObject boolNode = new JSONObject();
            boolNode.put("must", this.ret);
            this.ret = boolNode;
        } else if (this.ret instanceof JSONObject){
            JSONObject obj = (JSONObject) this.ret;

            JSONArray allArr = new JSONArray();
            for (String key : obj.keySet()) {
                if (obj.get(key) instanceof  JSONArray) {
                    JSONArray arr = obj.getJSONArray(key);
                    allArr.addAll(arr);
                }
            }

            JSONObject boolNode = new JSONObject();
            boolNode.put("must", allArr);
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

        node.setName("bool");
        if (this.ret instanceof JSONArray) {
            JSONObject boolNode = new JSONObject();
            boolNode.put("should", this.ret);
            this.ret = boolNode;
        } else if (this.ret instanceof JSONObject){
            JSONObject obj = (JSONObject) this.ret;

            JSONArray allArr = new JSONArray();
            for (String key : obj.keySet()) {
                if (obj.get(key) instanceof  JSONArray) {
                    JSONArray arr = obj.getJSONArray(key);
                    allArr.addAll(arr);
                }
            }

            JSONObject boolNode = new JSONObject();
            boolNode.put("should", allArr);
            this.ret = boolNode;
        }
    }

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

    public void visit(Queryquery node) {
        super.visit(node);

        if (this.ret instanceof JSONObject) {
            JSONObject obj = (JSONObject) this.ret;
            if (obj.size() == 1 && obj.containsKey("query_string")) {
                this.ret = obj.get("query_string");
                node.setName("query_string");
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
        } else if (n instanceof ObjectNode) {
            if (((ObjectNode) n).value == null) {
                this.ret = null;
            }
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

            if (kn.getValue().equalsIgnoreCase("query")) {
                valueNode.accept(this);

                JSONObject obj = (JSONObject) this.ret;

                if (valueNode instanceof KeyWord
                        && ((KeyWord) valueNode).getName().equals("query_string")) {
                    jsonMap.put("query_string", obj);
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
