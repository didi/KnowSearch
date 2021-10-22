package com.didichuxing.datachannel.arius.admin.metadata.job.template.model;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameUtils;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClusterHitNode {


    private final ILog LOGGER = LogFactory.getLog(ClusterHitNode.class);

    private String clusterName;
    private Map<String/*templateName*/, IndexHitNode> templateHitNodeMap = new HashMap<>();
    private Map<String/*alias*/, Set<String/*index*/>> aliasMap;
    private Map<String/*indexName*/, IndexHitNode> indexHitNodeMap = new HashMap<>();


    public ClusterHitNode(long time, String clusterName, List<IndexTemplatePhyWithLogic> templates, ESClusterService esClusterService) {
        this.clusterName = clusterName;
        this.aliasMap = esClusterService.getAliasMap(clusterName);

        LOGGER.info("cluster:{}, alias:{}", clusterName, JSON.toJSONString(aliasMap));

        if (templates != null) {
            for (IndexTemplatePhyWithLogic template : templates) {
                if (template == null) {
                    continue;
                }

                templateHitNodeMap.put(template.getName(), new IndexHitNode(template));
            }
        }

        for (String template : templateHitNodeMap.keySet()) {
            IndexHitNode indexHitNode = templateHitNodeMap.get(template);
            Set<String> indexDateNames = indexHitNode.getIndexDateNames(time);
            LOGGER.info("cluster:{}, template:{}, indexSize:{}", clusterName, template, indexDateNames.size());

            for (String index : indexDateNames) {
                if (indexHitNodeMap.containsKey(index)) {
                    LOGGER.error("two template have same index, cluster:{}, index:{}, template1:{}, template2:{}",
                            clusterName, index, indexHitNode.getName(), indexHitNodeMap.get(index).getName());
                }

                indexHitNodeMap.put(index, indexHitNode);
            }
        }
    }

    /**
     * @param index
     * @param count
     * @param templateHitPoMap
     * @param date
     * @param isAlias 索引名称是否为别名
     * @return
     */
    public boolean matchIndex(String index, Long count, TemplateHitPOMap templateHitPoMap, String date, boolean isAlias) {
        IndexHitNode indexHitNode = indexHitNodeMap.get(index);
        if (indexHitNode != null) {
            templateHitPoMap.addData(indexHitNode.getTemplate(), index, count, date);
            return true;
        }

        // 尝试去除版本信息
        index = IndexNameUtils.removeVersion(index);
        indexHitNode = indexHitNodeMap.get(index);
        if (indexHitNode != null) {
            templateHitPoMap.addData(indexHitNode.getTemplate(), index, count, date);
            return true;
        }

        // 处理特殊的模版
        for (String template : templateHitNodeMap.keySet()) {
            IndexHitNode ihn = templateHitNodeMap.get(template);
            if (ihn.matchIndex(index)) {
                indexHitNode = ihn;
                templateHitPoMap.addData(indexHitNode.getTemplate(), index, count, date);
            }
        }
        if (indexHitNode != null) {
            return true;
        }

        // 尝试匹配别名
        boolean match = false;
        if (!isAlias && aliasMap.containsKey(index)) {
            Set<String> aliasIndexs = aliasMap.get(index);
            for (String aliasIndex : aliasIndexs) {
                if (matchIndex(aliasIndex, count, templateHitPoMap, date, true)) {
                    match = true;
                }
            }
        }

        if (match) {
            return true;
        }

        return false;
    }

    /**
     * 根据用户传入的索引名称进行匹配
     *
     * @param indices
     * @param count
     * @param templateHitPoMap
     * @param date
     * @return
     */
    public boolean matchIndices(String indices, Long count, TemplateHitPOMap templateHitPoMap, String date) {
        if (!indices.endsWith("*")) {
            indices = indices + "*";
        }

        boolean match = false;
        for (String indexName : indexHitNodeMap.keySet()) {
            IndexHitNode indexHitNode = indexHitNodeMap.get(indexName);
            if (IndexNameUtils.indexExpMatch(indexName, indices)) {
                templateHitPoMap.addData(indexHitNode.getTemplate(), indexName, count, date);
                match = true;
            }
        }

        // 尝试特殊匹配
        for (String template : templateHitNodeMap.keySet()) {
            IndexHitNode ihn = templateHitNodeMap.get(template);
            if (ihn.matchIndices(indices)) {
                templateHitPoMap.addData(ihn.getTemplate(), ihn.getTemplate().getName(), count, date);
                match = true;
            }
        }

        // 尝试匹配别名
        for (String alias : aliasMap.keySet()) {
            if (IndexNameUtils.indexExpMatch(alias, indices)) {
                Set<String> aliasIndexs = aliasMap.get(alias);
                for (String aliasIndex : aliasIndexs) {
                    if (matchIndex(aliasIndex, count, templateHitPoMap, date, true)) {
                        match = true;
                    }
                }
            }
        }

        if (!match && indices.length() > 2) {
            // 去除末尾的版本信息在试试看
            String oldIndices = indices;
            indices = IndexNameUtils.removeVersion(indices.substring(0, indices.length() - 1)) + "*";
            if (!oldIndices.equalsIgnoreCase(indices)) {
                if (matchIndices(indices, count, templateHitPoMap, date)) {
                    match = true;
                }
            }
        }

        return match;
    }

}
