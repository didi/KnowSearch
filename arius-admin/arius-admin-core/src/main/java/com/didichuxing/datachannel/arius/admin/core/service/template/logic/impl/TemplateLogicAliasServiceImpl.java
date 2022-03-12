package com.didichuxing.datachannel.arius.admin.core.service.template.logic.impl;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.alias.IndexTemplateAliasDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateAlias;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateAliasPO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.component.CacheSwitch;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.TemplateLogicAliasService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplateAliasDAO;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class TemplateLogicAliasServiceImpl implements TemplateLogicAliasService {

    @Autowired
    private IndexTemplateAliasDAO indexTemplateAliasDAO;

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
}
