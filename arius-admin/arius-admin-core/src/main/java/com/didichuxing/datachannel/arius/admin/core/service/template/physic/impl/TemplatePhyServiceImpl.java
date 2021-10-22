package com.didichuxing.datachannel.arius.admin.core.service.template.physic.impl;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.IndexTemplateLogicDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.IndexTemplatePhysicalDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.template.TemplatePhysicalStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateLogicPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplatePhysicalPO;
import com.didichuxing.datachannel.arius.admin.common.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.common.event.template.PhysicalTemplateDeleteEvent;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.IndexNameFactory;
import com.didichuxing.datachannel.arius.admin.common.util.RackUtils;
import com.didichuxing.datachannel.arius.admin.core.component.CacheSwitch;
import com.didichuxing.datachannel.arius.admin.core.component.ResponsibleConvertTool;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ESRegionRackService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.TemplateLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.TemplatePhyService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplateLogicDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplatePhysicalDAO;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.indices.catindices.CatIndexResult;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.ModuleEnum.TEMPLATE;
import static com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum.DELETE;

/**
 * @author d06679
 * @date 2019/3/29
 */
@Service
public class TemplatePhyServiceImpl implements TemplatePhyService {

    private static final ILog                              LOGGER                         = LogFactory
        .getLog(TemplatePhyServiceImpl.class);

    public static final Integer                            NOT_CHECK                      = -100;
    private static final Integer                           INDEX_OP_OK                    = 0;
    private static final Integer                           TOMORROW_INDEX_NOT_CREATE      = 1;
    private static final Integer                           EXPIRE_INDEX_NOT_DELETE        = 2;
    private static final Integer                           INDEX_ALL_ERR                  = TOMORROW_INDEX_NOT_CREATE
                                                                                            + EXPIRE_INDEX_NOT_DELETE;

    private Cache<String, List<IndexTemplatePhy>>          templatePhysicalCache          = CacheBuilder.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES).maximumSize(100).build();

    private Cache<String, List<IndexTemplatePhyWithLogic>> templatePhysicalWothLogicCache = CacheBuilder.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES).maximumSize(100).build();

    @Autowired
    private IndexTemplatePhysicalDAO                       indexTemplatePhysicalDAO;

    @Autowired
    private IndexTemplateLogicDAO                          indexTemplateLogicDAO;

    @Autowired
    private OperateRecordService                           operateRecordService;

    @Autowired
    private ESIndexService                                 esIndexService;

    @Autowired
    private ESTemplateService                              esTemplateService;

    @Autowired
    private ResponsibleConvertTool                         responsibleConvertTool;

    @Autowired
    private TemplateLogicService                           templateLogicService;

    @Autowired
    private ESRegionRackService                            esRegionRackService;

    @Autowired
    private CacheSwitch                                    cacheSwitch;

    /**
     * 条件查询
     *
     * @param param 参数
     * @return 物理模板列表
     */
    @Override
    public List<IndexTemplatePhy> getByCondt(IndexTemplatePhysicalDTO param) {
        return ConvertUtil.list2List(
            indexTemplatePhysicalDAO.listByCondition(ConvertUtil.obj2Obj(param, TemplatePhysicalPO.class)),
            IndexTemplatePhy.class);
    }

    /**
     * 查询指定逻辑模板对应的物理模板
     *
     * @param logicId 逻辑模板
     * @return result
     */
    @Override
    public List<IndexTemplatePhy> getTemplateByLogicId(Integer logicId) {
        return ConvertUtil.list2List(indexTemplatePhysicalDAO.listByLogicId(logicId), IndexTemplatePhy.class);
    }

    /**
     * 从缓存中查询指定逻辑模板对应的物理模板
     * @param logicId 逻辑模板
     * @return list
     */
    @Override
    public List<IndexTemplatePhy> getTemplatesByLogicIdFromCache(Integer logicId) {
        if (logicId == null) {
            return Lists.newArrayList();
        }

        List<IndexTemplatePhy> indexTemplatePhies = listTemplateByOpenCache();
        if (CollectionUtils.isEmpty(indexTemplatePhies)) {
            return Lists.newArrayList();
        }

        return indexTemplatePhies.parallelStream()
            .filter(i -> i.getLogicId() != null && logicId.intValue() == i.getLogicId().intValue())
            .collect(Collectors.toList());
    }

    /**
     * 查询指定id的模板
     *
     * @param physicalId 物理模板id
     * @return result
     */
    @Override
    public IndexTemplatePhy getTemplateById(Long physicalId) {
        return ConvertUtil.obj2Obj(indexTemplatePhysicalDAO.getById(physicalId), IndexTemplatePhy.class);
    }

    /**
     * 查询指定id的模板 包含逻辑模板信息
     *
     * @param physicalId 物理模板id
     * @return result
     */
    @Override
    public IndexTemplatePhyWithLogic getTemplateWithLogicById(Long physicalId) {
        TemplatePhysicalPO physicalPO = indexTemplatePhysicalDAO.getById(physicalId);
        return buildIndexTemplatePhysicalWithLogic(physicalPO);
    }

    /**
     * 删除
     *
     * @param physicalId 物理模板id
     * @param operator   操作人
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result delTemplate(Long physicalId, String operator) throws ESOperateException {
        TemplatePhysicalPO oldPO = indexTemplatePhysicalDAO.getById(physicalId);
        if (oldPO == null) {
            return Result.buildNotExist("template not exist");
        }

        boolean succ = 1 == indexTemplatePhysicalDAO.updateStatus(physicalId,
            TemplatePhysicalStatusEnum.INDEX_DELETING.getCode());
        if (succ) {
            // 删除集群中的模板
            esTemplateService.syncDelete(oldPO.getCluster(), oldPO.getName(), 0);

            operateRecordService.save(TEMPLATE, DELETE, oldPO.getLogicId(), "删除" + oldPO.getCluster() + "物理模板",
                operator);

            SpringTool.publish(new PhysicalTemplateDeleteEvent(this, ConvertUtil.obj2Obj(oldPO, IndexTemplatePhy.class),
                templateLogicService.getLogicTemplateWithPhysicalsById(oldPO.getLogicId())));
        }

        return Result.build(succ);
    }

    /**
     * 删除
     *
     * @param logicId  id
     * @param operator 操作人
     * @return result
     * @throws ESOperateException e
     */
    @Override
    public Result delTemplateByLogicId(Integer logicId, String operator) throws ESOperateException {
        List<TemplatePhysicalPO> physicalPOs = indexTemplatePhysicalDAO.listByLogicId(logicId);

        boolean succ = true;
        if (CollectionUtils.isEmpty(physicalPOs)) {
            LOGGER.info("method=delTemplateByLogicId||logicId={}||msg=template no physical info!", logicId);
        } else {
            LOGGER.info("method=delTemplateByLogicId||logicId={}||physicalSize={}||msg=template has physical info!",
                logicId, physicalPOs.size());
            for (TemplatePhysicalPO physicalPO : physicalPOs) {
                if (delTemplate(physicalPO.getId(), operator).failed()) {
                    succ = false;
                }

            }
        }

        return Result.build(succ);
    }

    /**
     * 修改由于逻辑模板修改而物理模板需要同步修改的属性
     * 目前有:
     * expression
     *
     * @param param    参数
     * @param operator 操作人
     * @return result
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result editTemplateFromLogic(IndexTemplateLogicDTO param, String operator) throws ESOperateException {
        if (param == null) {
            return Result.buildFail("参数为空！");
        }
        List<TemplatePhysicalPO> physicalPOs = indexTemplatePhysicalDAO.listByLogicId(param.getId());
        if (CollectionUtils.isEmpty(physicalPOs)) {
            return Result.buildSucc();
        }

        for (TemplatePhysicalPO physicalPO : physicalPOs) {
            if (AriusObjUtils.isChanged(param.getExpression(), physicalPO.getExpression())) {
                TemplatePhysicalPO updateParam = new TemplatePhysicalPO();
                updateParam.setId(physicalPO.getId());
                updateParam.setExpression(param.getExpression());
                boolean succeed = (1 == indexTemplatePhysicalDAO.update(updateParam));
                if (succeed) {
                    esTemplateService.syncUpdateExpression(physicalPO.getCluster(), physicalPO.getName(),
                        param.getExpression(), 0);
                } else {
                    LOGGER.warn("editTemplateFromLogic fail||physicalId={}||expression={}", physicalPO.getId(),
                        param.getExpression());
                    return Result.build(false);
                }
            }

            if (isValidShardNum(param.getShardNum())
                && AriusObjUtils.isChanged(param.getShardNum(), physicalPO.getShard())) {
                TemplatePhysicalPO updateParam = new TemplatePhysicalPO();
                updateParam.setId(physicalPO.getId());
                updateParam.setShard(param.getShardNum());
                boolean succeed = 1 == indexTemplatePhysicalDAO.update(updateParam);
                if (succeed) {
                    LOGGER.info("editTemplateFromLogic succeed||physicalId={}||preShardNum={}||currentShardNum={}",
                        physicalPO.getId(), physicalPO.getShard(), param.getShardNum());

                    esTemplateService.syncUpdateRackAndShard(physicalPO.getCluster(), physicalPO.getName(),
                        physicalPO.getRack(), param.getShardNum(), physicalPO.getShardRouting(), 0);
                } else {
                    LOGGER.warn("editTemplateFromLogic fail||physicalId={}||expression={}", physicalPO.getId(),
                        param.getExpression());
                    return Result.build(false);
                }
            }
        }

        return Result.buildSucc();
    }

    /**
     * 通过集群和名字查询
     *
     * @param cluster      集群
     * @param templateName 名字
     * @return result 不存在返回null
     */
    @Override
    public IndexTemplatePhy getTemplateByClusterAndName(String cluster, String templateName) {
        return ConvertUtil.obj2Obj(indexTemplatePhysicalDAO.getByClusterAndName(cluster, templateName),
            IndexTemplatePhy.class);
    }

    /**
     * 通过集群和名字查询
     *
     * @param cluster      集群
     * @param templateName 名字
     * @return result 不存在返回null
     */
    @Override
    public IndexTemplatePhyWithLogic getTemplateWithLogicByClusterAndName(String cluster, String templateName) {
        return buildIndexTemplatePhysicalWithLogic(indexTemplatePhysicalDAO.getByClusterAndName(cluster, templateName));
    }

    /**
     * 根据物理模板状态获取模板列表
     *
     * @param cluster 集群
     * @param status  状态
     * @return list
     */
    @Override
    public List<IndexTemplatePhy> getTemplateByClusterAndStatus(String cluster, int status) {
        return ConvertUtil.list2List(indexTemplatePhysicalDAO.listByClusterAndStatus(cluster, status),
            IndexTemplatePhy.class);
    }

    /**
     * 获取状态正常的模板列表
     *
     * @param cluster 集群
     * @return list
     */
    @Override
    public List<IndexTemplatePhy> getNormalTemplateByCluster(String cluster) {
        return ConvertUtil.list2List(
            indexTemplatePhysicalDAO.listByClusterAndStatus(cluster, TemplatePhysicalStatusEnum.NORMAL.getCode()),
            IndexTemplatePhy.class);
    }

    /**
     * 根据集群和分区获取模板列表
     *
     * @param cluster 集群
     * @param racks
     * @return list
     */
    @Override
    public List<IndexTemplatePhy> getNormalTemplateByClusterAndRack(String cluster, Collection<String> racks) {
        if (CollectionUtils.isEmpty(racks)) {
            return Lists.newArrayList();
        }
        List<IndexTemplatePhy> templatePhysicals = getNormalTemplateByCluster(cluster);
        if (CollectionUtils.isEmpty(templatePhysicals)) {
            return Lists.newArrayList();
        }
        return templatePhysicals.stream()
            .filter(templatePhysical -> RackUtils.hasIntersect(templatePhysical.getRack(), racks))
            .collect(Collectors.toList());
    }

    /**
     * 获取模板匹配的索引列表，按着时间排序
     * 注意：
     * 该方法只能识别出那些时间后缀是一样的情况；
     * 如果模板中途修改过时间后缀，则无法识别之前时间后缀的索引
     *
     * @param physicalId 物理模板id
     * @return list
     */
    @Override
    public List<String> getMatchNoVersionIndexNames(Long physicalId) {
        IndexTemplatePhyWithLogic templatePhysicalWithLogic = getTemplateWithLogicById(physicalId);
        if (templatePhysicalWithLogic == null) {
            return Lists.newArrayList();
        }
        Set<String> indices = esIndexService.syncGetIndexNameByExpression(templatePhysicalWithLogic.getCluster(),
            templatePhysicalWithLogic.getExpression());
        if (CollectionUtils.isEmpty(indices)) {
            return Lists.newArrayList();
        }

        Set<String> noVersionIndices = indices.stream()
            .map(indexName -> IndexNameFactory.genIndexNameClear(indexName, templatePhysicalWithLogic.getExpression()))
            .collect(Collectors.toSet());

        List<String> matchIndices = Lists.newArrayList();
        for (String noVersionIndex : noVersionIndices) {
            if (IndexNameFactory.noVersionIndexMatchExpression(noVersionIndex,
                templatePhysicalWithLogic.getExpression(),
                templatePhysicalWithLogic.getLogicTemplate().getDateFormat())) {
                matchIndices.add(noVersionIndex);
            }
        }

        Collections.sort(matchIndices);

        return matchIndices;
    }

    /**
     * 获取模板匹配的索引列表，按着时间排序
     *
     * @param physicalId 物理模板id
     * @return list
     */
    @Override
    public List<String> getMatchIndexNames(Long physicalId) {
        IndexTemplatePhyWithLogic templatePhysicalWithLogic = getNormalAndDeletingTemplateWithLogicById(physicalId);
        if (templatePhysicalWithLogic == null) {
            return Lists.newArrayList();
        }

        List<CatIndexResult> indices = esIndexService.syncCatIndexByExpression(templatePhysicalWithLogic.getCluster(),
            templatePhysicalWithLogic.getExpression());
        if (CollectionUtils.isEmpty(indices)) {
            return Lists.newArrayList();
        }

        List<String> matchIndices = Lists.newArrayList();
        for (CatIndexResult indexResult : indices) {
            LOGGER.info("method=getMatchIndexNames||msg=fetch should be deleted indices||template={}||status={}||"
                        + "cluster={}||docCount={}||docSize={}",
                templatePhysicalWithLogic.getName(), templatePhysicalWithLogic.getStatus(),
                templatePhysicalWithLogic.getCluster(), indexResult.getDocsCount(), indexResult.getStoreSize());

            if (IndexNameFactory.indexMatchExpression(indexResult.getIndex(), templatePhysicalWithLogic.getExpression(),
                templatePhysicalWithLogic.getLogicTemplate().getDateFormat())) {
                matchIndices.add(indexResult.getIndex());
            }
        }

        Collections.sort(matchIndices);

        return matchIndices;
    }

    /**
     * 批量获取模板信息
     *
     * @param physicalIds 物理模板id
     * @return list
     */
    @Override
    public List<IndexTemplatePhyWithLogic> getTemplateWithLogicByIds(List<Long> physicalIds) {
        if (CollectionUtils.isEmpty(physicalIds)) {
            return Lists.newArrayList();
        }
        List<TemplatePhysicalPO> templatePhysicalPOS = indexTemplatePhysicalDAO.listByIds(physicalIds);
        return batchBuildTemplatePhysicalWithLogic(templatePhysicalPOS);
    }

    /**
     * 根据名字查询
     *
     * @param template 名字
     * @return list
     */
    @Override
    public List<IndexTemplatePhyWithLogic> getTemplateWithLogicByName(String template) {
        List<TemplatePhysicalPO> templatePhysicalPOS = indexTemplatePhysicalDAO.listByName(template);
        return batchBuildTemplatePhysicalWithLogic(templatePhysicalPOS);
    }

    /**
     * 获取全量
     *
     * @return list
     */
    @Override
    public List<IndexTemplatePhy> listTemplate() {
        return ConvertUtil.list2List(indexTemplatePhysicalDAO.listAll(), IndexTemplatePhy.class);
    }

    @Override
    public List<IndexTemplatePhy> listTemplateWithCache() {
        if (cacheSwitch.physicalTemplateCacheEnable()) {
            try {
                return templatePhysicalCache.get("listTemplate", this::listTemplate);
            } catch (ExecutionException e) {
                return listTemplate();
            }
        }
        return listTemplate();
    }

    @Override
    public List<IndexTemplatePhy> listTemplateByOpenCache() {
        try {
            return templatePhysicalCache.get("listTemplate", this::listTemplate);
        } catch (ExecutionException e) {
            return listTemplate();
        }
    }

    /**
     * 获取IndexTemplatePhysicalWithLogic
     *
     * @return list
     */
    @Override
    public List<IndexTemplatePhyWithLogic> listTemplateWithLogic() {
        List<TemplatePhysicalPO> templatePhysicalPOS = indexTemplatePhysicalDAO.listAll();
        return batchBuildTemplatePhysicalWithLogic(templatePhysicalPOS);
    }

    @Override
    public List<IndexTemplatePhyWithLogic> listTemplateWithLogicWithCache() {
        if (cacheSwitch.physicalTemplateCacheEnable()) {
            try {
                return templatePhysicalWothLogicCache.get("listTemplateWithLogic", this::listTemplateWithLogic);
            } catch (ExecutionException e) {
                return listTemplateWithLogic();
            }
        }
        return listTemplateWithLogic();
    }

    /**
     * 根绝逻辑模板id列表查询
     *
     * @param logicIds 列表
     * @return list
     */
    @Override
    public List<IndexTemplatePhy> getTemplateByLogicIds(List<Integer> logicIds) {
        return ConvertUtil.list2List(indexTemplatePhysicalDAO.listByLogicIds(logicIds), IndexTemplatePhy.class);
    }

    @Override
    public Result updateTemplateName(IndexTemplatePhy physical, String operator) throws ESOperateException {

        TemplatePhysicalPO oldPhysicalPO = indexTemplatePhysicalDAO.getById(physical.getId());

        TemplatePhysicalPO editParam = new TemplatePhysicalPO();
        editParam.setId(physical.getId());
        editParam.setName(physical.getName());

        boolean succ = 1 == indexTemplatePhysicalDAO.update(editParam);
        if (!succ) {
            return Result.buildFail("修改物理模板失败");
        }

        return Result.build(
            esTemplateService.syncUpdateName(physical.getCluster(), oldPhysicalPO.getName(), physical.getName(), 0));
    }

    @Override
    public IndexTemplatePhyWithLogic buildIndexTemplatePhysicalWithLogic(TemplatePhysicalPO physicalPO) {
        if (physicalPO == null) {
            return null;
        }

        IndexTemplatePhyWithLogic indexTemplatePhyWithLogic = ConvertUtil.obj2Obj(physicalPO,
            IndexTemplatePhyWithLogic.class);

        TemplateLogicPO logicPO = indexTemplateLogicDAO.getById(physicalPO.getLogicId());
        if (logicPO == null) {
            LOGGER.warn("logic template not exist||logicId={}", physicalPO.getLogicId());
            return indexTemplatePhyWithLogic;
        }
        indexTemplatePhyWithLogic.setLogicTemplate(ConvertUtil.obj2Obj(logicPO, IndexTemplateLogic.class));
        return indexTemplatePhyWithLogic;
    }

    @Override
    public List<IndexTemplatePhy> getTemplateByRegionId(Long regionId) {
        ClusterRegion region = esRegionRackService.getRegionById(regionId);
        if (AriusObjUtils.isNull(region)) {
            return Lists.newArrayList();
        }

        return getNormalTemplateByClusterAndRack(region.getPhyClusterName(), RackUtils.racks2List(region.getRacks()));
    }

    /**************************************** private method ****************************************************/
    private List<IndexTemplatePhyWithLogic> batchBuildTemplatePhysicalWithLogic(List<TemplatePhysicalPO> templatePhysicalPOS) {
        if (CollectionUtils.isEmpty(templatePhysicalPOS)) {
            return Lists.newArrayList();
        }

        List<Integer> logicIds = templatePhysicalPOS.stream().map(TemplatePhysicalPO::getLogicId)
            .collect(Collectors.toList());
        List<TemplateLogicPO> templateLogicPOS = indexTemplateLogicDAO.listByIds(logicIds);
        Map<Integer, TemplateLogicPO> id2IndexTemplateLogicPOMap = ConvertUtil.list2Map(templateLogicPOS,
            TemplateLogicPO::getId);

        List<IndexTemplatePhyWithLogic> physicalWithLogics = Lists.newArrayList();
        for (TemplatePhysicalPO templatePhysicalPO : templatePhysicalPOS) {
            IndexTemplatePhyWithLogic physicalWithLogic = ConvertUtil.obj2Obj(templatePhysicalPO,
                IndexTemplatePhyWithLogic.class);
            physicalWithLogic.setLogicTemplate(responsibleConvertTool
                .obj2Obj(id2IndexTemplateLogicPOMap.get(templatePhysicalPO.getLogicId()), IndexTemplateLogic.class));

            physicalWithLogics.add(physicalWithLogic);
        }

        return physicalWithLogics;
    }

    private IndexTemplatePhyWithLogic getNormalAndDeletingTemplateWithLogicById(Long physicalId) {
        TemplatePhysicalPO physicalPO = indexTemplatePhysicalDAO.getNormalAndDeletingById(physicalId);
        return buildIndexTemplatePhysicalWithLogic(physicalPO);
    }

    /**
     * 判定是否是合法的shard number.
     *
     * @param shardNum
     * @return
     */
    private boolean isValidShardNum(Integer shardNum) {
        return shardNum != null && shardNum > 0;
    }
}
