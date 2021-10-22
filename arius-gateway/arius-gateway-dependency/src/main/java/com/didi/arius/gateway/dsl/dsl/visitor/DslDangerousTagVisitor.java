package com.didi.arius.gateway.dsl.dsl.visitor;

import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.dsl.bean.DangerousDslTagEnum;
import com.didi.arius.gateway.dsl.dsl.ast.aggr.Aggs;
import com.didi.arius.gateway.dsl.dsl.ast.aggr.Cardinality;
import com.didi.arius.gateway.dsl.dsl.ast.aggr.SignificantTerms;
import com.didi.arius.gateway.dsl.dsl.ast.common.Node;
import com.didi.arius.gateway.dsl.dsl.ast.common.key.KeyNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.script.Script;
import com.didi.arius.gateway.dsl.dsl.ast.common.value.JsonNode;
import com.didi.arius.gateway.dsl.dsl.ast.query.Regexp;
import com.didi.arius.gateway.dsl.dsl.ast.query.Wildcard;
import com.didi.arius.gateway.dsl.dsl.visitor.basic.OutputVisitor;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;

import java.util.Set;

/**
 * @Author: D10865
 * @Description:
 * @Date: Create on 2019/1/5 下午9:05
 * @Modified By
 * <p>
 * 危害dsl查询语句标签遍历器
 */
public class DslDangerousTagVisitor extends OutputVisitor {

    private static final ILog LOGGER = LogFactory.getLog(DslDangerousTagVisitor.class);
    // 标签集合
    private Set<String> tags = Sets.newHashSet();
    // aggs次数
    private int aggsLevel = 0;
    // 最大aggs次数
    private int maxAggsLevel = 0;

    /**
     * DSL中带了script
     *
     * @param node
     */
    @Override
    public void visit(Script node) {
        super.visit(node);
        this.tags.add(DangerousDslTagEnum.WITH_SCRIPT.getTag());
    }

    /**
     * query中带了Wildcard，且前缀*号
     *
     * @param node
     */
    @Override
    public void visit(Wildcard node) {
        // visit 节点输出查询语句，为了判断查询语句长度
        super.visit(node);

        // 得到Wildcard的内容是否以*开头
        Node valueNode = null;
        String value = null;
        for (KeyNode n : node.m.m.keySet()) {

            valueNode = node.m.m.get(n);

            // 如果valueNode节点是关键字，则从关键字获取修改后key的值，否则遍历valueNode得到key
            if (valueNode != null && valueNode instanceof JsonNode) {

                JSONObject jsonObject = (JSONObject)(((JsonNode)valueNode).json);
                // 先从value节点取值，如果没有则从wildcard节点取值
                value = jsonObject.getString("value");
                if (StringUtils.isBlank(value)) {
                    value = jsonObject.getString("wildcard");
                }

                if (StringUtils.isNotBlank(value) && value.startsWith("*")) {
                    this.tags.add(DangerousDslTagEnum.WITH_WILDCARD_PRE.getTag());
                    return;
                }
            }
        }
    }

    /**
     * query中带了Regexp
     *
     * @param node
     */
    @Override
    public void visit(Regexp node) {
        super.visit(node);
        this.tags.add(DangerousDslTagEnum.WITH_REGEXP.getTag());
    }

    /**
     * aggs中带了cardinality
     *
     * @param node
     */
    @Override
    public void visit(Cardinality node) {
        super.visit(node);
        this.tags.add(DangerousDslTagEnum.AGGS_CARDINALITY.getTag());
    }

    /**
     * aggs中带了significant_terms
     *
     * @param node
     */
    @Override
    public void visit(SignificantTerms node) {
        super.visit(node);
        this.tags.add(DangerousDslTagEnum.AGGS_SIGNIFICANT_TERMS.getTag());
    }

    /**
     * aggs 嵌套层数过深
     *
     * @param node
     */
    @Override
    public void visit(Aggs node) {
        ++this.aggsLevel;
        // aggsLevel来保存当前节点高度
        // maxAggsLevel来保存最大高度，类似于求树的高度
        this.maxAggsLevel = Math.max(this.maxAggsLevel, this.aggsLevel);

        super.visit(node);

        --this.aggsLevel;
    }

    /**
     * 获取危害标签
     */
    public Set<String> getDangerousTags() {

        // 查询语句超过5k
        if (super.ret.toString().length() > 5 * 1024) {
            // LOGGER.error("dsl length more than 5k {}", super.ret.toString());
            this.tags.add(DangerousDslTagEnum.DSL_LENGTH_TOO_LARGE.getTag());
        }
        // aggs 嵌套层数过深
        if (this.maxAggsLevel >= 3) {
            this.tags.add(DangerousDslTagEnum.AGGS_DEEP_NEST.getTag());
        }

        return tags;
    }

}
