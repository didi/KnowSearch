package com.didichuxing.datachannel.arius.admin.metadata.job.template.model;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameUtils;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClusterHitNode {
    private static final ILog LOGGER = LogFactory.getLog(ClusterHitNode.class);

    private Map<String/*templateName*/, IndexHitNode> templateHitNodeMap = new HashMap<>();
    private Map<String/*alias*/, Set<String/*index*/>> aliasMap;
    private Map<String/*indexName*/, IndexHitNode> indexHitNodeMap = new HashMap<>();


    public ClusterHitNode(long time, String clusterName, List<IndexTemplatePhyWithLogic> templates, ESClusterService esClusterService) {
        this.aliasMap = esClusterService.syncGetAliasMap(clusterName);

        LOGGER.info("class=ClusterHitNode||method=ClusterHitNode||msg=cluster:{}, alias:{}", clusterName, JSON.toJSONString(aliasMap));

        if (templates != null) {
            for (IndexTemplatePhyWithLogic template : templates) {
                if (template == null) {
                    continue;
                }

                templateHitNodeMap.put(template.getName(), new IndexHitNode(template));
            }
        }

        for(Map.Entry<String, IndexHitNode> entry : templateHitNodeMap.entrySet()){
            String template = entry.getKey();
            IndexHitNode indexHitNode   = templateHitNodeMap.get(template);
            Set<String>  indexDateNames = indexHitNode.getIndexDateNames(time);
            LOGGER.info("class=ClusterHitNode||method=ClusterHitNode||msg=cluster:{}, template:{}, indexSize:{}", clusterName, template, indexDateNames.size());

            for (String index : indexDateNames) {
                if (indexHitNodeMap.containsKey(index)) {
                    LOGGER.error("class=ClusterHitNode||method=ClusterHitNode||errMsg=two template have same index, cluster:{}, index:{}, template1:{}, template2:{}",
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
            templateHitPoMap.addData(indexHitNode.getTemplate(), index, count);
            return true;
        }

        // 尝试去除版本信息
        index = IndexNameUtils.removeVersion(index);
        indexHitNode = indexHitNodeMap.get(index);
        if (indexHitNode != null) {
            templateHitPoMap.addData(indexHitNode.getTemplate(), index, count);
            return true;
        }

        // 处理特殊的模版
        for(Map.Entry<String/*templateName*/, IndexHitNode> entry : templateHitNodeMap.entrySet()){
            String template = entry.getKey();
            IndexHitNode ihn = templateHitNodeMap.get(template);
            if (ihn.matchIndex(index)) {
                indexHitNode = ihn;
                templateHitPoMap.addData(indexHitNode.getTemplate(), index, count);
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

        return match;
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
        match = matchIndexHit(indices, count, templateHitPoMap, match);

        // 尝试特殊匹配
        match = matchTemplateHit(indices, count, templateHitPoMap, match);

        // 尝试匹配别名
        match = matchAlias(indices, count, templateHitPoMap, date, match);

        match = matchExcluteVersionTail(indices, count, templateHitPoMap, date, match);

        return match;
    }

    private boolean matchExcluteVersionTail(String indices, Long count, TemplateHitPOMap templateHitPoMap, String date, boolean match) {
        if (!match && indices.length() > 2) {
            // 去除末尾的版本信息在试试看
            String oldIndices = indices;
            indices = IndexNameUtils.removeVersion(indices.substring(0, indices.length() - 1)) + "*";
            if (!oldIndices.equalsIgnoreCase(indices) &&
                matchIndices(indices, count, templateHitPoMap, date)) {
                match = true;
            }
        }
        return match;
    }

    private boolean matchAlias(String indices, Long count, TemplateHitPOMap templateHitPoMap, String date, boolean match) {
        for(Map.Entry<String/*alias*/, Set<String/*index*/>> entry : aliasMap.entrySet()){
            String alias = entry.getKey();
            if (IndexNameUtils.indexExpMatch(alias, indices)) {
                Set<String> aliasIndexs = aliasMap.get(alias);
                for (String aliasIndex : aliasIndexs) {
                    if (matchIndex(aliasIndex, count, templateHitPoMap, date, true)) {
                        match = true;
                    }
                }
            }
        }
        return match;
    }

    private boolean matchTemplateHit(String indices, Long count, TemplateHitPOMap templateHitPoMap, boolean match) {
        for(Map.Entry<String/*templateName*/, IndexHitNode> entry : templateHitNodeMap.entrySet()){
            String template = entry.getKey();
            IndexHitNode ihn = templateHitNodeMap.get(template);
            if (ihn.matchIndices(indices)) {
                templateHitPoMap.addData(ihn.getTemplate(), ihn.getTemplate().getName(), count);
                match = true;
            }
        }
        return match;
    }

    private boolean matchIndexHit(String indices, Long count, TemplateHitPOMap templateHitPoMap, boolean match) {
        for(Map.Entry<String/*indexName*/, IndexHitNode> entry : indexHitNodeMap.entrySet()){
            String indexName = entry.getKey();
            IndexHitNode indexHitNode = indexHitNodeMap.get(indexName);
            if (IndexNameUtils.indexExpMatch(indexName, indices)) {
                templateHitPoMap.addData(indexHitNode.getTemplate(), indexName, count);
                match = true;
            }
        }
        return match;
    }

}
