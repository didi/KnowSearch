package com.didichuxing.datachannel.arius.admin.core.service.template.logic.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.TemplateConstant.TEMPLATE_NAME_CHAR_SET;
import static com.didichuxing.datachannel.arius.admin.common.constant.TemplateConstant.TEMPLATE_NAME_SIZE_MAX;
import static com.didichuxing.datachannel.arius.admin.common.constant.TemplateConstant.TEMPLATE_NAME_SIZE_MIN;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.alias.ConsoleTemplateAliasSwitchDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.alias.IndexTemplateAliasDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateAlias;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.IndexTemplatePO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.IndexTemplatePhyPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateAliasPO;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.component.CacheSwitch;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.TemplateLogicAliasService;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESOpClient;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplateAliasDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplateDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplatePhyDAO;
import com.didiglobal.logi.elasticsearch.client.ESClient;
import com.didiglobal.logi.elasticsearch.client.request.index.putalias.PutAliasNode;
import com.didiglobal.logi.elasticsearch.client.request.index.putalias.PutAliasType;
import com.didiglobal.logi.elasticsearch.client.response.indices.getalias.ESIndicesGetAliasResponse;
import com.didiglobal.logi.elasticsearch.client.response.indices.putalias.ESIndicesPutAliasResponse;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TemplateLogicAliasServiceImpl implements TemplateLogicAliasService {

    @Autowired
    private IndexTemplateAliasDAO indexTemplateAliasDAO;
    @Autowired
    private IndexTemplateDAO indexTemplateDAO;
    @Autowired
    private IndexTemplatePhyDAO indexTemplatePhyDAO;
    @Autowired
    private ESOpClient esOpClient;
    @Autowired
    private CacheSwitch cacheSwitch;

    private Cache<Integer, List<String>> templateLogicAliasCache = CacheBuilder
            .newBuilder().expireAfterWrite(60, TimeUnit.MINUTES).maximumSize(1000).build();
    private Cache<String, Map<Integer, List<String>>> templateAliasMapCache = CacheBuilder
            .newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).maximumSize(100).build();

    /**
     * 获取别名
     *
     * @param logicId logicId
     * @return list
     */
    @Override
    public List<String> getAliasesById(Integer logicId) {
        List<TemplateAliasPO> indexTemplateAliasPOS = indexTemplateAliasDAO.listByTemplateId(logicId);
        if(CollectionUtils.isEmpty(indexTemplateAliasPOS)){
            return new ArrayList<>();
        }

        return indexTemplateAliasPOS.stream().map(i -> i.getName()).collect(Collectors.toList());
    }

    /**
     * 从缓存中获取模板所有的别名
     * @param logicId
     * @return
     */
    @Override
    public List<String> getAliasesByIdFromCache(Integer logicId) {
        try {
            return templateLogicAliasCache.get(logicId, () -> getAliasesById(logicId));
        } catch (ExecutionException e) {
            return getAliasesById(logicId);
        }
    }

    /**
     * 获取别名
     *
     * @return list
     */
    @Override
    public List<IndexTemplateAlias> listAlias() {
        return ConvertUtil.list2List(indexTemplateAliasDAO.listAll(), IndexTemplateAlias.class);
    }

    /**
     * 获取平台所有索引别名
     * @return
     */
    @Override
    public Map<Integer, List<String>> listAliasMap() {
        Map<Integer, List<String>> templateAliasMap = Maps.newHashMap();

        List<IndexTemplateAlias> aliasList = ConvertUtil.list2List(indexTemplateAliasDAO.listAll(), IndexTemplateAlias.class);

        if (CollectionUtils.isNotEmpty(aliasList)) {
            for (IndexTemplateAlias alias : aliasList) {
                Integer key = alias.getLogicId();
                List<String> aliasNames = templateAliasMap.getOrDefault(key, new ArrayList<>());
                aliasNames.add(alias.getName());
                templateAliasMap.putIfAbsent(key, aliasNames);
            }
        }
        return templateAliasMap;
    }

    @Override
    public Map<Integer, List<String>> listAliasMapWithCache() {
        if (cacheSwitch.logicTemplateCacheEnable()) {
            try {
                return templateAliasMapCache.get("listAliasMap", this::listAliasMap);
            } catch (ExecutionException e) {
                return listAliasMap();
            }
        }
        return listAliasMap();
    }



    /**
     * 增加一个索引的别名
     *
     * @param indexTemplateAlias
     * @return
     */
    @Override
    public Result<Boolean> addAlias(IndexTemplateAliasDTO indexTemplateAlias) {
        //这里进行别名检查
        Result result = aliasChecked(indexTemplateAlias.getName(), null, indexTemplateAlias.getLogicId());
        if (null != result) {
            return Result.buildSucc(result.success(),result.getMessage());
        }
        int ret = indexTemplateAliasDAO.insert( ConvertUtil.obj2Obj(indexTemplateAlias, TemplateAliasPO.class));

        return Result.buildSucc(ret > 0);
    }

    /**
     * 删除一个索引的别名
     *
     * @param indexTemplateAlias
     * @return
     */
    @Override
    public Result<Boolean> delAlias(IndexTemplateAliasDTO indexTemplateAlias) {
        int ret =  indexTemplateAliasDAO.delete(indexTemplateAlias.getLogicId(), indexTemplateAlias.getName());

        return Result.buildSucc(ret > 0);
    }

    @Override
    @Transactional
    public Result aliasSwitch(ConsoleTemplateAliasSwitchDTO aliasSwitchDTO) throws ESOperateException {
        //检查别名
        Result result = aliasChecked(aliasSwitchDTO.getAliasName(), aliasSwitchDTO.getAppId(), null);
        if (result != null && result.failed()) {
            return result;
        }
        List<TemplateAliasPO> insertAliasList = new ArrayList<>();
        List<Integer> deleteAliasList = new ArrayList<>();
        Set<Integer> logicIdList = new HashSet<>();
        List<PutAliasNode> nodes;
        List<IndexTemplatePO> indexIndexTemplatePOS = indexTemplateDAO.listByProjectId(aliasSwitchDTO.getAppId());
        //检查索引
        if (CollectionUtils.isEmpty(aliasSwitchDTO.getAddAliasIndices()) && CollectionUtils.isEmpty(aliasSwitchDTO.getDelAliasIndices())) {
            return Result.buildFail("操作的索引名称不能为空！");
        }
        Result<List<PutAliasNode>> nodesResult = buildAliasNodes(aliasSwitchDTO, indexIndexTemplatePOS, insertAliasList, deleteAliasList, logicIdList);
        if (nodesResult.success()) {
            nodes = new ArrayList<>(nodesResult.getData());
        } else {
            return nodesResult;
        }
        //这里需要在同一个物理集群中，否则别名没有意义
        List<IndexTemplatePhyPO> physicalPOList = indexTemplatePhyDAO.listByLogicIds(new ArrayList<>(logicIdList));
        String cluster = "";
        if (CollectionUtils.isNotEmpty(physicalPOList)) {
            Set<String> set = physicalPOList.stream().map(IndexTemplatePhyPO::getCluster).collect(Collectors.toSet());
            if (set.size() != 1) {
                return Result.buildFail("索引所在集群不一致！");
            }
            cluster = set.toArray(new String[1])[0];
        } else {
            return Result.buildFail("物理模板不存在！");
        }
        //写入数据
        if (CollectionUtils.isNotEmpty(deleteAliasList)) {
            indexTemplateAliasDAO.deleteBatch(deleteAliasList, aliasSwitchDTO.getAliasName());
        }
        if (CollectionUtils.isNotEmpty(insertAliasList)) {
            indexTemplateAliasDAO.insertBatch(insertAliasList);
        }
        //写入ES
        ESClient client = esOpClient.getESClient(cluster);
        if (CollectionUtils.isNotEmpty(aliasSwitchDTO.getDelAliasIndices()) && CollectionUtils.isEmpty(aliasSwitchDTO.getAddAliasIndices())) {
            //在仅删除别名的时候会出现别名不存在的异常，所以在这里判断要删除别名的索引至少存在一个拥有要删除的别名，并且优化提示信息
            ESIndicesGetAliasResponse response = client.admin().indices().prepareAlias(StringUtils.join(aliasSwitchDTO.getDelAliasIndices(), ",").split(",")).execute().actionGet(30, TimeUnit.SECONDS);
            if (null == response || null == response.getM() || response.getM().values().stream().noneMatch(indexNode -> indexNode.getAliases().containsKey(aliasSwitchDTO.getAliasName()))) {
                throw new ESOperateException("操作的别名不存在！");
            }
        }
        ESIndicesPutAliasResponse response = client.admin().indices().preparePutAlias().addPutAliasNodes(nodes).execute().actionGet(30, TimeUnit.SECONDS);
        if (null == response || !response.getAcknowledged()) {
            throw new ESOperateException("设置别名失败！");
        }
        return Result.buildSucc();
    }

    private Result<List<PutAliasNode>> buildAliasNodes(ConsoleTemplateAliasSwitchDTO aliasSwitchDTO, List<IndexTemplatePO> indexIndexTemplatePOS, List<TemplateAliasPO> insertAliasList, List<Integer> deleteAliasList, Set<Integer> logicIdList) {
        List<PutAliasNode> nodes = new ArrayList<>();
        List<PutAliasNode> addNodes = buildAliasNodes(aliasSwitchDTO, PutAliasType.ADD, indexIndexTemplatePOS, insertAliasList, deleteAliasList, logicIdList);
        if (null == addNodes) {
            Result.buildFail("索引模板不存在！");
        } else if (CollectionUtils.isNotEmpty(addNodes)) {
            nodes.addAll(addNodes);
        }
        List<PutAliasNode> removeNodes = buildAliasNodes(aliasSwitchDTO, PutAliasType.REMOVE, indexIndexTemplatePOS, insertAliasList, deleteAliasList, logicIdList);
        if (removeNodes == null) {
            Result.buildFail("索引模板不存在！");
        } else if (CollectionUtils.isNotEmpty(removeNodes)) {
            nodes.addAll(removeNodes);
        }
        return Result.buildSucc(nodes);
    }

    private List<PutAliasNode> buildAliasNodes(ConsoleTemplateAliasSwitchDTO aliasSwitchDTO, PutAliasType aliasType, List<IndexTemplatePO> indexIndexTemplatePOS, List<TemplateAliasPO> insertAliasList, List<Integer> deleteAliasList, Set<Integer> logicIdList) {
        List<PutAliasNode> nodes = new ArrayList<>();
        List<String> indexList = null;
        String aliasName = aliasSwitchDTO.getAliasName();
        if (PutAliasType.ADD == aliasType) {
            indexList = aliasSwitchDTO.getAddAliasIndices();
        } else if (PutAliasType.REMOVE == aliasType) {
            indexList = aliasSwitchDTO.getDelAliasIndices();
        }

        if (CollectionUtils.isEmpty(indexList)) {
            return nodes;
        }
        for (String indexName : indexList) {
            Integer logicId = getLogicIdByIndexName(indexIndexTemplatePOS, indexName);
            if (null == logicId) {
                return null;
            }
            PutAliasNode node = new PutAliasNode();
            node.setType(aliasType);
            node.setAlias(aliasName);
            node.setIndex(indexName);
            nodes.add(node);
            TemplateAliasPO aliasPO = new TemplateAliasPO();
            if (!logicIdList.contains(logicId)) {
                //针对逻辑ID去重，避免一个逻辑模板存在多条相同的别名记录
                if (PutAliasType.ADD == aliasType) {
                    aliasPO.setName(aliasName);
                    aliasPO.setLogicId(logicId);
                    insertAliasList.add(aliasPO);
                }
                deleteAliasList.add(logicId);
                logicIdList.add(aliasPO.getLogicId());
            }
        }
        return nodes;
    }

    /**
     * 检查别名是否基于appId唯一，检查模板是否已经拥有该别名，检查别名是否和索引模板互为前缀
     *
     * @param name
     * @param appId
     * @param logicId
     * @return
     */
    private Result aliasChecked(String name, Integer appId, Integer logicId) {
        if (StringUtils.isBlank(name)) {
            return Result.buildFail("别名名称不能为空");
        }
        if (name.length() < TEMPLATE_NAME_SIZE_MIN || name.length() > TEMPLATE_NAME_SIZE_MAX) {
            return Result.buildParamIllegal(String.format("名称长度非法, %s-%s",TEMPLATE_NAME_SIZE_MIN,TEMPLATE_NAME_SIZE_MAX));
        }
        for (Character c : name.toCharArray()) {
            if (!TEMPLATE_NAME_CHAR_SET.contains(c)) {
                return Result.buildParamIllegal("名称包含非法字符, 只能包含小写字母、数字、-、_和.");
            }
        }

        String prefix = name.substring(0, 1);
        if (StringUtils.containsAny(prefix, "_", "-", "+")) {
            return Result.buildParamIllegal("Invalid alias name must not start with '_', '-', or '+'");
        }

        // 这里进行别名检查
        List<IndexTemplateAlias> aliasList = ConvertUtil.list2List(indexTemplateAliasDAO.listAll(), IndexTemplateAlias.class);
        List<IndexTemplatePO> poList = indexTemplateDAO.listAll();
        Set<Integer> logicIds = new HashSet<>();
        if (null != logicId) {
            logicIds.add(logicId);
        }
        if (CollectionUtils.isNotEmpty(aliasList)) {
            for (IndexTemplateAlias alias : aliasList) {
                if (alias.getName().equals(name)) {
                    logicIds.add(alias.getLogicId());
                }
                if (alias.getName().equals(name) && alias.getLogicId().equals(logicId)) {
                    // 模板已经拥有该别名，返回true，不需要重复写入
                    return Result.buildSucc(true);
                }
            }
        }
        //判断别名不能和索引互为前缀
        if (CollectionUtils.isNotEmpty(poList)) {
            if (poList.stream().anyMatch(IndexTemplateLogic -> IndexTemplateLogic.getName().startsWith(name) || name.startsWith(IndexTemplateLogic.getName()))) {
                return Result.buildFail("别名不能和索引模板互为前缀！");
            }
            List<IndexTemplatePO> indexTemplatePOS = poList.stream().filter(po -> logicIds.contains(po.getId())).collect(Collectors.toList());
            Set<Integer> appIds = new HashSet<>();
            if (null != appId) {
                appIds.add(appId);
            }
            if (CollectionUtils.isNotEmpty(indexTemplatePOS)) {
                appIds.addAll(indexTemplatePOS.stream().map(IndexTemplatePO::getProjectId).collect(Collectors.toSet()));
            } else if (CollectionUtils.isEmpty(indexTemplatePOS) && CollectionUtils.isNotEmpty(logicIds)) {
                return Result.buildFail("索引模板不存在！");
            }
            if (appIds.size() > 1) {
                //别名已经被其他appId占用了
                return Result.buildFail("别名已被占用！");
            }
        }
        return null;
    }


    private Integer getLogicIdByIndexName(List<IndexTemplatePO> indexIndexTemplatePOS, String indexName) {
        for (IndexTemplatePO template : indexIndexTemplatePOS) {
            String expression = template.getExpression();
            //判断索引名称是否满足索引模板
            if ((!expression.endsWith("*") && template.getName().equals(indexName)) || (expression.endsWith("*") && indexName.startsWith(expression.substring(0, expression.length() - 1)))) {
                return template.getId();
            }
        }
        return null;
    }
}