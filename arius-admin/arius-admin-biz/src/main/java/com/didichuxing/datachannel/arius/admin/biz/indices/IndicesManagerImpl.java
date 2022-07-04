package com.didichuxing.datachannel.arius.admin.biz.indices;

import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateConstant.PRIMARY;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.didichuxing.datachannel.arius.admin.biz.page.IndexPageSearchHandle;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord.Builder;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndexCatCellDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndexQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndicesBlockSettingDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.manage.IndexCatCellWithConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.srv.IndexForceMergeDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.srv.IndexRolloverDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.index.IndexCatCell;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.IndexShardInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexCatCellVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexMappingVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexSettingVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexShardInfoVO;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.PageSearchHandleTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.index.IndexBlockEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TriggerWayEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didichuxing.datachannel.arius.admin.common.mapping.AriusIndexTemplateSetting;
import com.didichuxing.datachannel.arius.admin.common.util.*;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexCatService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didichuxing.datachannel.arius.admin.metadata.job.index.IndexCatInfoCollector;
import com.didiglobal.logi.elasticsearch.client.response.indices.catindices.CatIndexResult;
import com.didiglobal.logi.elasticsearch.client.response.setting.common.MappingConfig;
import com.didiglobal.logi.elasticsearch.client.response.setting.index.IndexConfig;
import com.didiglobal.logi.elasticsearch.client.response.setting.index.MultiIndexsConfig;
import com.didiglobal.logi.elasticsearch.client.utils.JsonUtils;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.security.service.ProjectService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author lyn
 * @date 2021/09/28
 **/
@Component
public class IndicesManagerImpl implements IndicesManager {
    public static final String START = "*";
    private static final ILog     LOGGER = LogFactory.getLog(IndicesManagerImpl.class);
    public static final int            RETRY_COUNT = 3;
    @Autowired
    private             ProjectService projectService;

    @Autowired
    private ESIndexCatService     esIndexCatService;

    @Autowired
    private ESIndexService        esIndexService;

    @Autowired
    private ClusterPhyService clusterPhyService;

    @Autowired
    private IndexTemplatePhyService indexTemplatePhyService;

    @Autowired
    private OperateRecordService operateRecordService;

    @Autowired
    private IndexCatInfoCollector indexCatInfoCollector;

    @Autowired
    private HandleFactory         handleFactory;

    @Autowired
    private ClusterLogicService clusterLogicService;

    @Autowired
    private ClusterRegionService clusterRegionService;

    @Autowired
    private IndexTemplateService indexTemplateService;



    private static final String DEFAULT_SORT_TERM = "timestamp";

    @Override
    public PaginationResult<IndexCatCellVO> pageGetIndex(IndexQueryDTO condition, Integer projectId) throws NotFindSubclassException {
        BaseHandle baseHandle     = handleFactory.getByHandlerNamePer(PageSearchHandleTypeEnum.INDEX.getPageSearchType());
        if (baseHandle instanceof IndexPageSearchHandle) {
            IndexPageSearchHandle handle = (IndexPageSearchHandle) baseHandle;
            return handle.doPage(condition, projectId);
        }

        LOGGER.warn("class=TemplateLogicManagerImpl||method=pageGetConsoleClusterVOS||msg=failed to get the TemplateLogicPageSearchHandle");

        return PaginationResult.buildFail("获取索引分页信息失败");
    }

    @Override
    public Result<Void> createIndex(IndexCatCellWithConfigDTO indexCreateDTO, Integer projectId, String operator) {
        Result<String> getClusterRet = getClusterPhyByClusterNameAndProjectId(indexCreateDTO.getCluster(), projectId);
        if (getClusterRet.failed()) {
            return Result.buildFrom(getClusterRet);
        }
        String phyCluster = getClusterRet.getData();
        IndexConfig indexConfig = new IndexConfig();
        if (StringUtils.isNotBlank(indexCreateDTO.getMapping())) {
            Result<MappingConfig> mappingResult = AriusIndexMappingConfigUtils
                .parseMappingConfig(indexCreateDTO.getMapping());
            if (mappingResult.failed()) {
                return Result.buildFrom(mappingResult);
            }
            indexConfig.setMappings(mappingResult.getData());
        }

        if (StringUtils.isNotBlank(indexCreateDTO.getSetting())) {
            indexConfig.setSettings(AriusIndexTemplateSetting.flat(JSON.parseObject(indexCreateDTO.getSetting())));
        }
        try {
            esIndexService.syncCreateIndex(phyCluster, indexCreateDTO.getIndex(), indexConfig, RETRY_COUNT);
        } catch (Exception e) {
            LOGGER.error("class=IndicesManagerImpl||method=createIndex||msg=create index failed||index={}" + indexCreateDTO.getIndex(), e);
            return Result.buildFail("索引创建失败，请联系管理员检查集群后重新创建！");
        }
        operateRecordService.save(new OperateRecord.Builder().content(
                        String.format("物理集群:[%s],创建索引：[%s]", indexCreateDTO.getCluster(), indexCreateDTO.getIndex()))
                .operationTypeEnum(OperateTypeEnum.INDEX_MANAGEMENT_CREATE).userOperation(operator)
                .project(projectService.getProjectBriefByProjectId(projectId)).bizId(indexCreateDTO.getIndex())
                .build());
        try {
            indexCatInfoCollector.collectIndexCatInfoByCluster(phyCluster);
        } catch (Exception e) {
            LOGGER.error("class=IndicesManagerImpl||method=createIndex||msg=collectIndexCatInfoByCluster failed||index={}" + indexCreateDTO.getIndex(), e);
            return Result.buildFail("索引创建成功，采集任务执行失败，请3分钟后再查询！");
        }
        return Result.buildSucc();
    }

    @Override
    public Result<Boolean> deleteIndex(List<IndexCatCellDTO> params, Integer projectId, String operator) {
        return batchOperateIndex(params, projectId, (cluster, indexNameList) -> {
            if (indexNameList.size() == esIndexService.syncBatchDeleteIndices(cluster, indexNameList, RETRY_COUNT)) {
                Result<Boolean> batchSetIndexFlagInvalidResult = updateIndexFlagInvalid(cluster, indexNameList);
                if (batchSetIndexFlagInvalidResult.success()) {
                    operateRecordService.save(new OperateRecord.Builder()
                                    .operationTypeEnum(OperateTypeEnum.INDEX_MANAGEMENT_DELETE)
                                    .userOperation(operator)
                                    .content(String.format("批量删除%s集群中的索引：%s", cluster, ListUtils.strList2String(indexNameList)))
                                    .triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER)
                                    .project(projectService.getProjectBriefByProjectId(projectId))
                            
                            .build());
                }
            }
            return Result.buildSucc();
        });
    }

    @Override
    public  <T,U,R> Result<Boolean> batchOperateIndex(List<IndexCatCellDTO> params, Integer projectId, BiFunction<String,List<String>, Result<Void>> function) {
        for (IndexCatCellDTO param : params) {
            Result<String> getClusterRet = getClusterPhyByClusterNameAndProjectId(param.getCluster(), projectId);
            if (getClusterRet.failed()) {
                return Result.buildFrom(getClusterRet);
            }
            String phyCluster = getClusterRet.getData();
            param.setCluster(phyCluster);
            Result<Void> ret = basicCheckParam(param.getCluster(), param.getIndex(), projectId);
            if (ret.failed()) {
                return Result.buildFrom(ret);
            }
        }

        Map<String, List<String>> cluster2IndexNameListMap = ConvertUtil.list2MapOfList(params,
                IndexCatCellDTO::getCluster, IndexCatCellDTO::getIndex);

        for (Map.Entry<String, List<String>> entry : cluster2IndexNameListMap.entrySet()) {
            String cluster = entry.getKey();
            List<String> indexNameList = entry.getValue();
                Result<Void> r = function.apply(cluster, indexNameList);
                if(r.failed()){
                    return Result.buildFrom(r);
                }
        }
        sleep(1000L);
        return Result.buildSucc(true);
    }
    @Override
    public Result<Boolean> openIndex(List<IndexCatCellDTO> params, Integer projectId, String operator) {
        return this.batchOperateIndex(params, projectId, (cluster, indexNameList) -> {
            try {

                boolean syncOpenOrCloseResult = esIndexService.syncBatchOpenIndices(cluster, indexNameList, 3);
                if (!syncOpenOrCloseResult) {
                    return Result.buildFail("批量开启索引失败");
                }

                Result<Boolean> setCatIndexResult = updateIndicesStatus(cluster, indexNameList, "open");
                if (!setCatIndexResult.success()) {
                    return Result.buildFail("批量更新索引状态失败");
                }
                operateRecordService.save(
                        new OperateRecord.Builder().project(projectService.getProjectBriefByProjectId(projectId))
                                .content(String.format("批量开启%s集群中的索引：%s", cluster,
                                        ListUtils.strList2String(indexNameList))).userOperation(operator)
                                .triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER)
                                .operationTypeEnum(OperateTypeEnum.INDEX_MANAGEMENT_OP_INDEX).build());
              

            } catch (ESOperateException e) {
                LOGGER.error(
                        "class=IndicesManagerImpl||method=closeIndicesStatus||cluster={}||indexNameList={}||errMsg={}",
                        cluster, ListUtils.strList2String(indexNameList), e.getMessage(), e);
                return Result.buildFail(e.getMessage());
            }

            return Result.buildSucc();
        });
    }

    @Override
    public Result<Boolean> closeIndex(List<IndexCatCellDTO> params, Integer projectId, String operator) {
        return this.batchOperateIndex(params, projectId, (cluster, indexNameList) -> {
            try {

                boolean syncOpenOrCloseResult = esIndexService.syncBatchCloseIndices(cluster, indexNameList, 3);
                if (!syncOpenOrCloseResult) {
                    return Result.buildFail("批量关闭索引失败");
                }

                Result<Boolean> setCatIndexResult = updateIndicesStatus(cluster, indexNameList, "close");
                if (!setCatIndexResult.success()) {
                    return Result.buildFail("批量更新索引状态失败");
                }
                 operateRecordService.save(
                         new OperateRecord.Builder()
                                 .content(String.format("批量关闭%s集群中的索引：%s", cluster, ListUtils.strList2String(indexNameList)))
                                 .project(projectService.getProjectBriefByProjectId(projectId))
                                 .operationTypeEnum(OperateTypeEnum.INDEX_MANAGEMENT_OP_INDEX)
                                 .userOperation(operator)
                                 .triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER)
                                 .build()
                         
                         
                 );
                

            } catch (ESOperateException e) {
                LOGGER.error(
                    "class=IndicesManagerImpl||method=closeIndicesStatus||cluster={}||indexNameList={}||errMsg={}",
                    cluster, ListUtils.strList2String(indexNameList), e.getMessage(), e);
                return Result.buildFail(e.getMessage());
            }

            return Result.buildSucc();
        });
    }

    @Override
    public Result<Boolean> updateIndexFlagInvalid(String cluster, List<String> indexNameList) {
        //不采集已删除索引
        indexCatInfoCollector.updateNotCollectorIndexNames(cluster, indexNameList);
        //更新存储cat/index信息的元信息索引中对应文档删除标识位为true
        boolean succ = indexNameList.size() == esIndexCatService.syncUpdateCatIndexDeleteFlag(cluster, indexNameList, 3);
        if (!succ) {
            LOGGER.error(
                "class=IndicesManagerImpl||method=batchSetIndexFlagInvalid||cluster={}||indexNameList={}||errMsg=failed to batchSetIndexFlagInvalid",
                cluster, ListUtils.strList2String(indexNameList));
        }
        return Result.build(succ);
    }

    private Result<Boolean> updateIndicesStatus(String cluster, List<String> indexNameList, String status) {
        boolean succ = indexNameList.size() == esIndexCatService.syncUpdateCatIndexStatus(cluster, indexNameList, status, 3);
        if (!succ) {
            LOGGER.error(
                    "class=IndicesManagerImpl||method=batchSetIndexStatus||cluster={}||indexNameList={}||errMsg=failed to batchSetIndexStatus",
                    cluster, ListUtils.strList2String(indexNameList));
        }
        return Result.build(succ);
    }

    @Override
    public Result<Boolean> editIndexBlockSetting(List<IndicesBlockSettingDTO> params, Integer projectId,
                                                 String operator) {
        for (IndicesBlockSettingDTO param : params) {
            Result<String> getClusterRet = getClusterPhyByClusterNameAndProjectId(param.getCluster(), projectId);
            if (getClusterRet.failed()) {
                return Result.buildFrom(getClusterRet);
            }
            String phyCluster = getClusterRet.getData();
            param.setCluster(phyCluster);
        }
        Result<Boolean> checkResult = checkEditIndexBlockSetting(params, projectId);
        if (checkResult.failed()) {
            return checkResult;
        }

        Map<String, List<IndicesBlockSettingDTO>> cluster2IndicesBlockSettingListMap = ConvertUtil.list2MapOfList(
            params, IndicesBlockSettingDTO::getCluster, indicesBlockSettingDTO -> indicesBlockSettingDTO);

        cluster2IndicesBlockSettingListMap.forEach((cluster, indicesBlockSettingList) -> {
            for (IndicesBlockSettingDTO indicesBlockSetting : indicesBlockSettingList) {
                try {
                    boolean succ = false;
                    if (IndexBlockEnum.READ.getType().equals(indicesBlockSetting.getType())) {
                        succ = esIndexService.syncBatchBlockIndexRead(cluster,
                            Lists.newArrayList(indicesBlockSetting.getIndex()), indicesBlockSetting.getValue(), 3);
                    }

                    if (IndexBlockEnum.WRITE.getType().equals(indicesBlockSetting.getType())) {
                        succ = esIndexService.syncBatchBlockIndexWrite(cluster,
                            Lists.newArrayList(indicesBlockSetting.getIndex()), indicesBlockSetting.getValue(), 3);
                    }

                    if (succ) {
                        String operateContent = String.format("设置【%s】集群中的索引【%s】的block信息中的【%s】配置值为：%s", cluster,
                            indicesBlockSetting.getIndex(), indicesBlockSetting.getType(),
                            indicesBlockSetting.getValue());
                        
                        operateRecordService.save(new Builder()
                                
                                .triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER)
                                .content(operateContent)
                                .userOperation(operator)
                                .project(projectService.getProjectBriefByProjectId(projectId))
                                .operationTypeEnum(OperateTypeEnum.INDEX_MANAGEMENT_OP_INDEX)
                                .build());
                    }
                } catch (ESOperateException e) {
                    LOGGER.error(
                        "class=IndicesManagerImpl||method=editIndexBlockSetting||cluster={}||index={}||errMsg={}",
                        cluster, indicesBlockSetting.getIndex(), e.getMessage(), e);
                }
            }
        });

        return Result.buildSucc(true);
    }

    @Override
    public Result<IndexMappingVO> getMapping(String cluster, String indexName, Integer projectId) {
        Result<Void> ret = basicCheckParam(cluster, indexName, projectId);
        if (ret.failed()) {
            return Result.buildFrom(ret);
        }

        IndexMappingVO indexMappingVO = new IndexMappingVO();
        String mappingConfig = esIndexService.syncGetIndexMapping(cluster, indexName);
        indexMappingVO.setMappings(mappingConfig);
        indexMappingVO.setIndexName(indexName);
        return Result.buildSucc(indexMappingVO);
    }

    @Override
    public Result<Void> editMapping(IndexCatCellWithConfigDTO param, Integer projectId) {
        Result<String> getClusterRet = getClusterPhyByClusterNameAndProjectId(param.getCluster(), projectId);
        if (getClusterRet.failed()) {
            return Result.buildFrom(getClusterRet);
        }
        String phyCluster = getClusterRet.getData();
        String indexName = param.getIndex();
        String mapping = param.getMapping();
        Result<Void> ret = basicCheckParam(phyCluster, indexName, projectId);
        if (ret.failed()) {
            return Result.buildFrom(ret);
        }
        if (StringUtils.isBlank(mapping)) {
            return Result.buildFail("请传入索引Mapping");
        }
        Result<MappingConfig> mappingRet;
        if (!StringUtils.contains(mapping, "\"properties\"")) {
            //这里为了兼容多 type索引，前端进针对用户输入的内容做封装，所以后端解析封装
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("properties", mapping);
            mappingRet = AriusIndexMappingConfigUtils.parseMappingConfig(jsonObject.toJSONString());
        } else {
            mappingRet = AriusIndexMappingConfigUtils.parseMappingConfig(mapping);
        }
        if (mappingRet.failed()) {
            return Result.buildFail("mapping 转换异常");
        }

        return Result.build(esIndexService.syncUpdateIndexMapping(phyCluster, indexName, mappingRet.getData()));
    }

    @Override
    public Result<IndexSettingVO> getSetting(String cluster, String indexName, Integer projectId) {
        Result<Void> ret = basicCheckParam(cluster, indexName, projectId);
        if (ret.failed()) {
            return Result.buildFrom(ret);
        }

        IndexSettingVO indexSettingVO = new IndexSettingVO();
        MultiIndexsConfig multiIndexsConfig = esIndexService.syncGetIndexConfigs(cluster, indexName);
        if (null == multiIndexsConfig) {
            LOGGER.warn(
                "class=IndicesManagerImpl||method=getSetting||cluster={}||index={}||errMsg=get empty Index configs ",
                cluster, indexName);
            return Result.buildSucc(indexSettingVO);
        }

        IndexConfig indexConfig = multiIndexsConfig.getIndexConfig(indexName);
        if (null == indexConfig) {
            LOGGER.warn(
                "class=IndicesManagerImpl||method=getSetting||cluster={}||index={}||errMsg=get empty Index configs ",
                cluster, indexName);
            return Result.buildSucc(indexSettingVO);
        }
        indexSettingVO.setProperties(JsonUtils.reFlat(indexConfig.getSettings()));
        indexSettingVO.setIndexName(indexName);
        return Result.buildSucc(indexSettingVO);
    }

    @Override
    public Result<Void> editSetting(IndexCatCellWithConfigDTO param, Integer projectId) throws ESOperateException {
        Result<String> getClusterRet = getClusterPhyByClusterNameAndProjectId(param.getCluster(), projectId);
        if (getClusterRet.failed()) {
            return Result.buildFrom(getClusterRet);
        }
        String phyCluster = getClusterRet.getData();
        String indexName = param.getIndex();
        Result<Void> ret = basicCheckParam(phyCluster, indexName, projectId);
        if (ret.failed()) {
            return Result.buildFrom(ret);
        }

        JSONObject settingObj = JSON.parseObject(param.getSetting());
        if (null == settingObj) {
            return Result.buildFail("setting 配置非法");
        }
        Map<String, IndexConfig> configMap = esIndexService.syncGetIndexSetting(param.getCluster(),
            Lists.newArrayList(indexName), RETRY_COUNT);
        Map<String, String> sourceSettings = AriusOptional.ofObjNullable(configMap.get(indexName))
            .map(IndexConfig::getSettings).orElse(Maps.newHashMap());
        final Map<String, String> finalSettingMap = IndexSettingsUtil.getChangedSettings(sourceSettings,
            JsonUtils.flat(settingObj));
        return Result.build(esIndexService.syncPutIndexSettings(phyCluster, Lists.newArrayList(indexName),
            finalSettingMap, RETRY_COUNT));
    }

    @Override
    public Result<List<IndexShardInfoVO>> getIndexShardsInfo(String cluster, String indexName, Integer projectId) {
        Result<String> getClusterRet = getClusterPhyByClusterNameAndProjectId(cluster, projectId);
        if (getClusterRet.failed()) {
            return Result.buildFrom(getClusterRet);
        }
        String phyCluster = getClusterRet.getData();
        Result<Void> ret = basicCheckParam(phyCluster, indexName, projectId);
        if (ret.failed()) {
            return Result.buildFrom(ret);
        }

        List<IndexShardInfo> indexShardInfoList = esIndexCatService.syncGetIndexShardInfo(phyCluster, indexName);
        List<IndexShardInfoVO> indexNodeShardVOList = indexShardInfoList.stream().filter(this::filterPrimaryShard)
            .map(this::coverUnit).collect(Collectors.toList());
        return Result.buildSucc(indexNodeShardVOList);
    }

    @Override
    public Result<IndexCatCellVO> getIndexCatInfo(String cluster, String indexName, Integer projectId) {
        Result<String> getClusterRet = getClusterPhyByClusterNameAndProjectId(cluster, projectId);
        if (getClusterRet.failed()) {
            return Result.buildFrom(getClusterRet);
        }
        String phyCluster = getClusterRet.getData();
        Integer queryProjectId = null;
        if (!AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
            queryProjectId = projectId;
        }
        Tuple<Long, List<IndexCatCell>> totalHitAndIndexCatCellListTuple = esIndexCatService
            .syncGetCatIndexInfo(cluster, indexName, null, queryProjectId, 0L, 1L, DEFAULT_SORT_TERM, true);
        if (null == totalHitAndIndexCatCellListTuple
            || CollectionUtils.isEmpty(totalHitAndIndexCatCellListTuple.getV2())) {
            return Result.buildFail("获取单个索引详情信息失败");
        }
        //设置索引阻塞信息
        List<IndexCatCell> finalIndexCatCellList = esIndexService.buildIndexAliasesAndBlockInfo(phyCluster,
            totalHitAndIndexCatCellListTuple.getV2());
        List<IndexCatCellVO> indexCatCellVOList = ConvertUtil.list2List(finalIndexCatCellList, IndexCatCellVO.class);

        return Result.buildSucc(indexCatCellVOList.get(0));
    }

    @Override
    public Result<Void> addIndexAliases(IndexCatCellWithConfigDTO param, Integer projectId) {
        Result<String> getClusterRet = getClusterPhyByClusterNameAndProjectId(param.getCluster(), projectId);
        if (getClusterRet.failed()) {
            return Result.buildFrom(getClusterRet);
        }
        String phyCluster = getClusterRet.getData();

        return esIndexService.addAliases(phyCluster, param.getIndex(), param.getAliases());
    }

    @Override
    public Result<Void> deleteIndexAliases(IndexCatCellWithConfigDTO param, Integer projectId) {
        Result<String> getClusterRet = getClusterPhyByClusterNameAndProjectId(param.getCluster(), projectId);
        if (getClusterRet.failed()) {
            return Result.buildFrom(getClusterRet);
        }
        String phyCluster = getClusterRet.getData();

        return esIndexService.deleteAliases(phyCluster, param.getIndex(), param.getAliases());
    }

    @Override
    public Result<List<String>> getIndexAliases(String cluster, String indexName, Integer projectId) {
        Result<String> getClusterRet = getClusterPhyByClusterNameAndProjectId(cluster, projectId);
        if (getClusterRet.failed()) {
            return Result.buildFrom(getClusterRet);
        }
        String phyCluster = getClusterRet.getData();
        Map<String, List<String>> aliasMap = esIndexService.syncGetIndexAliasesByIndices(phyCluster, indexName);
        return Result.buildSucc(aliasMap.getOrDefault(indexName,Lists.newArrayList()));
    }

    @Override
    public Result<Void> rollover(IndexRolloverDTO param) {
        if (null == param.getIndices()) {
            return Result.buildFail("索引为空");
        }
        for (IndexCatCellDTO indexCatCellDTO : param.getIndices()) {
            String cluster = indexCatCellDTO.getCluster();
            List<Tuple<String, String>> aliasList = esIndexService.syncGetIndexAliasesByExpression(cluster, indexCatCellDTO.getIndex());
            if (AriusObjUtils.isEmptyList(aliasList)) {
                return Result.buildFail("alias 为空");
            }

            Result<Void> rolloverResult = esIndexService.rollover(cluster, aliasList.get(0).getV2(),param.getContent());
            if (rolloverResult.failed()) {
                return rolloverResult;
            }
        }

        return Result.buildSucc();
    }

    @Override
    public Result<Void> shrink(IndexCatCellWithConfigDTO param){
        return esIndexService.shrink(param.getCluster(), param.getIndex(), param.getTargetIndex(), param.getExtra());
    }

    @Override
    public Result<Void> split(IndexCatCellWithConfigDTO param){
        return esIndexService.split(param.getCluster(), param.getIndex(), param.getTargetIndex(), param.getExtra());
    }

    @Override
    public Result<Void> forceMerge(IndexForceMergeDTO param) {
        if (null == param.getIndices()) {
            return Result.buildFail("索引为空");
        }

        for (IndexCatCellDTO indexCatCellDTO : param.getIndices()) {
            Result<Void> forceMergeResult = esIndexService.forceMerge(indexCatCellDTO.getCluster(), indexCatCellDTO.getIndex(), param.getMaxNumSegments(), param.getOnlyExpungeDeletes());
            if (forceMergeResult.failed()) {
                return forceMergeResult;
            }
        }

        return Result.buildSucc();
    }


    @Override
    public Result<List<String>> getClusterPhyIndexName(String clusterPhyName, Integer projectId) {
        if (!projectService.checkProjectExist(projectId)) {
            return Result.buildParamIllegal(String.format("There is no projectId:%s", projectId));
        }

        return Result.buildSucc(esIndexService.syncGetIndexName(clusterPhyName));
    }
    @Override
    public Result<List<String>> getClusterLogicIndexName(String clusterLogicName, Integer projectId) {
        ClusterLogic clusterLogic = clusterLogicService.getClusterLogicByName(clusterLogicName);
        if (clusterLogic == null) {
            return Result.buildFail();
        }
        ClusterRegion clusterRegion = clusterRegionService.getRegionByLogicClusterId(clusterLogic.getId());
        if (clusterRegion == null) {
            return Result.buildFail();
        }
        Result<List<IndexTemplate>> listResult = indexTemplateService.listByRegionId(Math.toIntExact(clusterRegion.getId()));
        List<IndexTemplate> indexTemplates = listResult.getData();

        List<CatIndexResult> catIndexResultList = new ArrayList<>();
        indexTemplates.forEach(indexTemplate -> {
            catIndexResultList.addAll(esIndexService.syncCatIndexByExpression(clusterRegion.getPhyClusterName(),
                    indexTemplate.getExpression()));
        });
        List<String> indexNames =  catIndexResultList.stream().map(CatIndexResult::getIndex).collect(Collectors.toList());
        return Result.buildSucc(indexNames);
    }

    @Override
    public Result<Boolean> isExists(String cluster, String indexName, Integer projectId) {
        if (!projectService.checkProjectExist(projectId)) {
            return Result.buildParamIllegal(String.format("当前登录项目Id[%s]不存在, 无权限操作", projectId));
        }
        Result<String> getClusterRet = getClusterPhyByClusterNameAndProjectId(cluster, projectId);
        if (getClusterRet.failed()) {
            return Result.buildFrom(getClusterRet);
        }
        String phyClusterName = getClusterRet.getData();
        if (!clusterPhyService.isClusterExists(phyClusterName)) {
            return Result.buildParamIllegal(String.format("物理集群[%s]不存在", phyClusterName));
        }
        return Result.buildSucc(esIndexService.syncIsIndexExist(phyClusterName, indexName));
    }

    @Override
    public List<String> listIndexNameByTemplatePhyId(Long physicalId) {
        List<CatIndexResult> indices = listIndexCatInfoByTemplatePhyId(physicalId);
        if (CollectionUtils.isEmpty(indices)) {
            return Lists.newArrayList();
        }
        return indices.stream().map(CatIndexResult::getIndex).sorted().collect(Collectors.toList());
    }
    @Override
    public List<CatIndexResult> listIndexCatInfoByTemplatePhyId(Long physicalId) {
        IndexTemplatePhyWithLogic templatePhysicalWithLogic = indexTemplatePhyService.getTemplateWithLogicById(physicalId);
        if (templatePhysicalWithLogic == null) {
            return Lists.newArrayList();
        }
        String expression = templatePhysicalWithLogic.getExpression();
        if (templatePhysicalWithLogic.getVersion() != null && templatePhysicalWithLogic.getVersion() > 0
                && !expression.endsWith(START)) {
            expression = expression + START;
        }
        return esIndexService.syncCatIndexByExpression(templatePhysicalWithLogic.getCluster(),
                expression);
    }

    /***************************************************private**********************************************************/
    private Result<Void> basicCheckParam(String cluster, String index, Integer projectId) {
        if (!projectService.checkProjectExist(projectId)) {
            return Result.buildParamIllegal(String.format("当前登录项目Id[%s]不存在, 无权限操作", projectId));
        }

        if (!clusterPhyService.isClusterExists(cluster)) {
            return Result.buildParamIllegal(String.format("物理集群[%s]不存在", cluster));
        }

        if (!esIndexService.syncIsIndexExist(cluster, index)) {
            return Result.buildParamIllegal(String.format("集群[%s]中的索引[%s]不存在", cluster, index));
        }

        return Result.buildSucc();
    }

    private Result<Boolean> checkEditIndexBlockSetting(List<IndicesBlockSettingDTO> params, Integer projectId) {
        for (IndicesBlockSettingDTO param : params) {
            Result<Void> ret = basicCheckParam(param.getCluster(), param.getIndex(), projectId);
            if (ret.failed()) {
                return Result.buildFrom(ret);
            }

            if (!IndexBlockEnum.isIndexBlockExit(param.getType())) {
                return Result.buildParamIllegal(String.format("阻塞类型%s不存在", param.getType()));
            }
        }

        return Result.buildSucc();
    }

    private boolean filterPrimaryShard(IndexShardInfo indexShardInfo) {
        if (null == indexShardInfo) {
            return false;
        }

        return PRIMARY.equals(indexShardInfo.getPrirep());
    }

    /**
     * 统一单位 byte
     * @param indexShardInfo
     * @return
     */
    private IndexShardInfoVO coverUnit(IndexShardInfo indexShardInfo) {
        IndexShardInfoVO indexShardInfoVO = ConvertUtil.obj2Obj(indexShardInfo, IndexShardInfoVO.class);
        indexShardInfoVO.setStoreInByte(SizeUtil.getUnitSize(indexShardInfo.getStore()));
        return indexShardInfoVO;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private Result<String> getClusterPhyByClusterNameAndProjectId(String cluster, Integer projectId) {
        String phyClusterName;
        if (AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
            phyClusterName = cluster;
        } else {
            ClusterLogic clusterLogic = clusterLogicService.getClusterLogicByName(cluster);
            if (null == clusterLogic) {
                return Result.buildParamIllegal(String.format("逻辑集群[%s]不存在", cluster));
            }
            ClusterRegion clusterRegion = clusterRegionService.getRegionByLogicClusterId(clusterLogic.getId());
            if (null == clusterRegion) {
                return Result.buildParamIllegal("逻辑集群未绑定Region");
            }
            phyClusterName = clusterRegion.getPhyClusterName();
        }
        return Result.buildSucc(phyClusterName);
    }
}