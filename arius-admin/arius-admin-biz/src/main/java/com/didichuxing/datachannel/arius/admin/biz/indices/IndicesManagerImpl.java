package com.didichuxing.datachannel.arius.admin.biz.indices;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.didichuxing.datachannel.arius.admin.biz.page.IndexPageSearchHandle;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
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
import com.didichuxing.datachannel.arius.admin.common.bean.entity.operaterecord.template.TemplateMappingOperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.operaterecord.template.TemplateSettingOperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.*;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.PageSearchHandleTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.index.IndexBlockEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.event.index.RefreshCatIndexInfoEvent;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didichuxing.datachannel.arius.admin.common.mapping.AriusIndexTemplateSetting;
import com.didichuxing.datachannel.arius.admin.common.tuple.TupleTwo;
import com.didichuxing.datachannel.arius.admin.common.tuple.Tuples;
import com.didichuxing.datachannel.arius.admin.common.util.*;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexCatService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didichuxing.datachannel.arius.admin.metadata.job.index.IndexCatInfoCollector;
import com.didiglobal.knowframework.elasticsearch.client.response.indices.catindices.CatIndexResult;
import com.didiglobal.knowframework.elasticsearch.client.response.setting.common.MappingConfig;
import com.didiglobal.knowframework.elasticsearch.client.response.setting.index.IndexConfig;
import com.didiglobal.knowframework.elasticsearch.client.response.setting.index.MultiIndexsConfig;
import com.didiglobal.knowframework.elasticsearch.client.utils.JsonUtils;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.knowframework.security.service.ProjectService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateConstant.PRIMARY;

/**
 * @author lyn
 * @date 2021/09/28
 **/
@Component
public class IndicesManagerImpl implements IndicesManager {
    public static final String      START             = "*";
    private static final ILog       LOGGER            = LogFactory.getLog(IndicesManagerImpl.class);
    public static final int         RETRY_COUNT       = 3;
    private static final String     PROPERTIES        = "\"properties\"";
    private static final String CONDITIONS = "conditions";
    private static final String MAX_AGE="max_age";
    private static final String MAX_DOCS="max_docs";
    private static final String MAX_SIZE="max_size";
    @Autowired
    private ProjectService          projectService;

    @Autowired
    private ESIndexCatService       esIndexCatService;
    @Autowired
    private ESClusterService esClusterService;

    @Autowired
    private ESIndexService          esIndexService;

    @Autowired
    private ClusterPhyService       clusterPhyService;

    @Autowired
    private IndexTemplatePhyService indexTemplatePhyService;

    @Autowired
    private OperateRecordService    operateRecordService;

    @Autowired
    private IndexCatInfoCollector   indexCatInfoCollector;

    @Autowired
    private HandleFactory           handleFactory;

    @Autowired
    private ClusterLogicService     clusterLogicService;

    @Autowired
    private ClusterRegionService    clusterRegionService;

    private static final FutureUtil<Result<Void>> FUTURE_UTIL_RESULT = FutureUtil.init("IndicesManagerImpl", 10, 10,
            100);

    @Override
    public PaginationResult<IndexCatCellVO> pageGetIndex(IndexQueryDTO condition,
                                                         Integer projectId) throws NotFindSubclassException {
        BaseHandle baseHandle = handleFactory.getByHandlerNamePer(PageSearchHandleTypeEnum.INDEX.getPageSearchType());
        if (baseHandle instanceof IndexPageSearchHandle) {
            IndexPageSearchHandle handle = (IndexPageSearchHandle) baseHandle;
            return handle.doPage(condition, projectId);
        }

        LOGGER.warn(
            "class=TemplateLogicManagerImpl||method=pageGetConsoleClusterVOS||msg=failed to get the TemplateLogicPageSearchHandle");

        return PaginationResult.buildFail("获取索引分页信息失败");
    }

    @Override
    public Result<Void> createIndex(IndexCatCellWithConfigDTO indexCreateDTO, Integer projectId, String operator)
            throws AdminOperateException {
        Result<String> getClusterRet = getClusterPhyByClusterNameAndProjectId(indexCreateDTO.getCluster(), projectId);
        if (getClusterRet.failed()) { return Result.buildFrom(getClusterRet);}

        String realPhyCluster = getClusterRet.getData();

        // 校验创建索引在平台的合法性
        Result<Void> checkValidRet = checkValid(indexCreateDTO, realPhyCluster);
        if (checkValidRet.failed()) { return checkValidRet;}

        // 初始化分配集群信息
        Result<Void> initRet = initIndexCreateDTO(indexCreateDTO, projectId);
        if (initRet.failed()) { return initRet;}

        // 处理索引 mapping
        IndexConfig indexConfig = new IndexConfig();
        if (StringUtils.isNotBlank(indexCreateDTO.getMapping())) {
            Result<MappingConfig> mappingResult = AriusIndexMappingConfigUtils
                .parseMappingConfig(indexCreateDTO.getMapping());
            if (mappingResult.failed()) {
                return Result.buildFrom(mappingResult);
            }
            indexConfig.setMappings(mappingResult.getData());
        }

        // 处理索引 setting
        if (StringUtils.isNotBlank(indexCreateDTO.getSetting())) {
            indexConfig.setSettings(AriusIndexTemplateSetting.flat(JSON.parseObject(indexCreateDTO.getSetting())));
        }
        boolean succ = false;
        try {
            // 1. es创建真实索引
            boolean syncCreateIndexRet = esIndexService.syncCreateIndex(realPhyCluster, indexCreateDTO.getIndex(),
                    indexConfig, RETRY_COUNT);

            // 2. 同步在元数据Cat_index系统索引中添加此索引元数据文档
            if (syncCreateIndexRet) {
                //在分页查询的时候被强制要求deleteFlag=false，如果数据不存在deleteFlag会导致查询不到；
                indexCreateDTO.setDeleteFlag(false);
                indexCreateDTO.setTimestamp(LocalDateTime.now().toInstant(ZoneOffset.ofHours(8)).toEpochMilli());
                succ = esIndexCatService.syncInsertCatIndex(Lists.newArrayList(indexCreateDTO), RETRY_COUNT);
            }

            if (succ) {
                operateRecordService.saveOperateRecordWithManualTrigger(
                        String.format("物理集群:[%s], 创建索引：[%s]", indexCreateDTO.getCluster(),
                                indexCreateDTO.getIndex()), operator, projectId, indexCreateDTO.getIndex(), OperateTypeEnum.INDEX_MANAGEMENT_CREATE);
            }
        } catch (ESOperateException e) {
            LOGGER.error("class=IndicesManagerImpl||method=createIndex||msg=create index failed||index={}",
                    indexCreateDTO.getIndex(), e);
            return Result.buildFail(String.format("索引创建失败, %s", e.getMessage()));
        }
        if (succ) { return Result.buildSuccWithMsg("索引创建成功");}

        return Result.buildFail("创建索引失败, 请检查集群是否异常");
    }

    @Override
    public Result<Boolean> deleteIndex(List<IndexCatCellDTO> params, Integer projectId, String operator) {
        return batchOperateIndex(params, projectId, (cluster, indexNameList) -> {
            if (indexNameList.size() == esIndexService.syncBatchDeleteIndices(cluster, indexNameList, RETRY_COUNT)) {
                Result<Void> batchSetIndexFlagInvalidResult = updateIndexFlagInvalid(cluster, indexNameList);
                if (batchSetIndexFlagInvalidResult.success()) {
                    for (String indexName : indexNameList) {
    
                        operateRecordService.saveOperateRecordWithManualTrigger(
                                String.format("删除索引：【%s】", indexName), operator, projectId, indexName,
                                OperateTypeEnum.INDEX_MANAGEMENT_DELETE);
                    }

                }
            }
            return Result.buildSucc();
        });
    }

    /**
     * 批处理操作指数
     *
     * @param params    参数个数 {@link IndexCatCellDTO#getCluster()}这里使用此参数作为集群名称；如果是超级项目，就是物理集群，反之为逻辑集群
     * @param projectId 项目id
     * @param function  函数
     * @return {@code Result<Boolean>}
     */
    @Override
    public <T, U, R> Result<Boolean> batchOperateIndex(List<IndexCatCellDTO> params, Integer projectId,
                                                       BiFunction<String, List<String>, Result<Void>> function) {
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
            FUTURE_UTIL_RESULT.callableTask(() -> function.apply(cluster, indexNameList));
        }
        Optional<Result<Void>> voidResult = FUTURE_UTIL_RESULT.waitResult().stream().filter(Result::failed).findAny();
        if (voidResult.isPresent()) {
            return Result.buildFrom(voidResult.get());
        }
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
                for (String indexName : indexNameList) {
        
                    operateRecordService.saveOperateRecordWithManualTrigger(String.format("开启索引：【%s】", indexName),
                            operator, projectId, indexName, OperateTypeEnum.INDEX_SERVICE_OP_INDEX);
                }

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
                for (String indexName : indexNameList) {
                    operateRecordService.saveOperateRecordWithManualTrigger(String.format("关闭索引：【%s】", indexName),
                            operator, projectId, indexName, OperateTypeEnum.INDEX_SERVICE_OP_INDEX);
                }

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
    public Result<Void> updateIndexFlagInvalid(String cluster, List<String> indexNameList) {
        //不采集已删除索引
        indexCatInfoCollector.updateNotCollectorIndexNames(cluster, indexNameList);
        //更新存储cat/index信息的元信息索引中对应文档删除标识位为true
        boolean succ = indexNameList.size() == esIndexCatService.syncUpdateCatIndexDeleteFlag(cluster, indexNameList,
            3);
        if (!succ) {
            LOGGER.error(
                "class=IndicesManagerImpl||method=batchSetIndexFlagInvalid||cluster={}||indexNameList={}||errMsg=failed to batchSetIndexFlagInvalid",
                cluster, ListUtils.strList2String(indexNameList));
        }
        return Result.build(succ);
    }

    private Result<Boolean> updateIndicesStatus(String cluster, List<String> indexNameList, String status) {
        boolean succ = indexNameList.size() == esIndexCatService.syncUpdateCatIndexStatus(cluster, indexNameList,
            status, 3);
        if (!succ) {
            LOGGER.error(
                "class=IndicesManagerImpl||method=batchSetIndexStatus||cluster={}||indexNameList={}||errMsg=failed to batchSetIndexStatus",
                cluster, ListUtils.strList2String(indexNameList));
        }
        return Result.build(succ);
    }

    @Override
    public Result<Void> editIndexBlockSetting(List<IndicesBlockSettingDTO> params, Integer projectId, String operator) {
        for (IndicesBlockSettingDTO param : params) {
            Result<String> getClusterRet = getClusterPhyByClusterNameAndProjectId(param.getCluster(), projectId);
            if (getClusterRet.failed()) {
                return Result.buildFrom(getClusterRet);
            }
            String phyCluster = getClusterRet.getData();
            param.setCluster(phyCluster);
        }
        Result<Void> checkResult = checkEditIndexBlockSetting(params, projectId);
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
                        String writeOrRead=StringUtils.equals(indicesBlockSetting.getType(),"write")?"写":"读";
                        String value=indicesBlockSetting.getValue().equals(Boolean.FALSE)?"启用":"禁用";
                     
                        
                        for (IndicesBlockSettingDTO param : params) {
                            String operateContent = String.format("【%s】:%s%s", param.getIndex(), value, writeOrRead);
                            operateRecordService.saveOperateRecordWithManualTrigger(operateContent, operator, projectId,
                                    param.getIndex(), OperateTypeEnum.INDEX_SERVICE_READ_WRITE_CHANGE);
                        }

                    }
                } catch (ESOperateException e) {
                    LOGGER.error(
                        "class=IndicesManagerImpl||method=editIndexBlockSetting||cluster={}||index={}||errMsg={}",
                        cluster, indicesBlockSetting.getIndex(), e.getMessage(), e);
                }
            }
        });

        return Result.build(true);
    }

    @Override
    public Result<IndexMappingVO> getMapping(String cluster, String indexName, Integer projectId) {
        Result<String> getClusterRet = getClusterPhyByClusterNameAndProjectId(cluster, projectId);
        if (getClusterRet.failed()) {
            return Result.buildFrom(getClusterRet);
        }
        String phyCluster = getClusterRet.getData();
        Result<Void> ret = basicCheckParam(phyCluster, indexName, projectId);
        if (ret.failed()) {
            return Result.buildFrom(ret);
        }

        IndexMappingVO indexMappingVO = new IndexMappingVO();
        String mappingConfig = esIndexService.syncGetIndexMapping(phyCluster, indexName);
        indexMappingVO.setMappings(mappingConfig);
        indexMappingVO.setIndexName(indexName);
        return Result.buildSucc(indexMappingVO);
    }

    @Override
    public Result<Void> editMapping(IndexCatCellWithConfigDTO param, Integer projectId, String operate)
            throws AdminOperateException {
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
        final Result<IndexMappingVO> beforeMapping = getMapping(param.getCluster(), indexName, projectId);
        Result<MappingConfig> mappingRet;
        if (!StringUtils.contains(mapping, PROPERTIES)) {
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

        try {
            final boolean syncUpdateIndexMapping = esIndexService.syncUpdateIndexMapping(phyCluster, indexName,
                mappingRet.getData());
            if (syncUpdateIndexMapping) {

                final Result<IndexMappingVO> afterMapping = getMapping(param.getCluster(), indexName, projectId);
    
                operateRecordService.saveOperateRecordWithManualTrigger(
                        new TemplateMappingOperateRecord(beforeMapping.getData(), afterMapping.getData()).toString(),
                        operate, projectId, indexName, OperateTypeEnum.INDEX_TEMPLATE_MANAGEMENT_EDIT_MAPPING);
            }

        } catch (ESOperateException e) {
            LOGGER.error("class=IndicesManagerImpl||method=editMapping||errMsg={}", e.getMessage(), e);
            return Result.buildFail(e.getMessage() + ":" + e.getCause());
        }

        return Result.buildSucc();
    }

    @Override
    public Result<IndexSettingVO> getSetting(String cluster, String indexName, Integer projectId) {
        Result<String> getClusterRet = getClusterPhyByClusterNameAndProjectId(cluster, projectId);
        if (getClusterRet.failed()) {
            return Result.buildFrom(getClusterRet);
        }
        String phyCluster = getClusterRet.getData();
        Result<Void> ret = basicCheckParam(phyCluster, indexName, projectId);
        if (ret.failed()) {
            return Result.buildFrom(ret);
        }

        IndexSettingVO indexSettingVO = new IndexSettingVO();
        MultiIndexsConfig multiIndexsConfig = esIndexService.syncGetIndexConfigs(phyCluster, indexName);
        if (null == multiIndexsConfig) {
            LOGGER.warn(
                "class=IndicesManagerImpl||method=getSetting||cluster={}||index={}||errMsg=get empty Index configs ",
                phyCluster, indexName);
            return Result.buildSucc(indexSettingVO);
        }

        IndexConfig indexConfig = multiIndexsConfig.getIndexConfig(indexName);
        if (null == indexConfig) {
            LOGGER.warn(
                "class=IndicesManagerImpl||method=getSetting||cluster={}||index={}||errMsg=get empty Index configs ",
                phyCluster, indexName);
            return Result.buildSucc(indexSettingVO);
        }
        indexSettingVO.setProperties(JsonUtils.reFlat(indexConfig.getSettings()));
        indexSettingVO.setIndexName(indexName);
        return Result.buildSucc(indexSettingVO);
    }

    @Override
    public Result<Void> editSetting(IndexCatCellWithConfigDTO param, Integer projectId,
                                    String operator) throws ESOperateException {
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
        
        
    
        final Result<IndexSettingVO> beforeSetting = getSetting(param.getCluster(), indexName, projectId);
        IndexSettingsUtil.checkImmutableSettingAndCorrectSetting(param.getSetting(),beforeSetting.getData(),projectId);
        JSONObject settingObj = JSON.parseObject(param.getSetting());
        if (null == settingObj) {
            return Result.buildFail("setting 配置非法");
        }
        
        Map<String, IndexConfig> configMap = esIndexService.syncGetIndexSetting(phyCluster,
            Lists.newArrayList(indexName), RETRY_COUNT);
        Map<String, String> sourceSettings = AriusOptional.ofObjNullable(configMap.get(indexName))
            .map(IndexConfig::getSettings).orElse(Maps.newHashMap());
        final Map<String, String> finalSettingMap = IndexSettingsUtil.getChangedSettings(sourceSettings,
            JsonUtils.flat(settingObj));
        boolean syncPutIndexSettings = true;
        if (finalSettingMap.size() > 0) {
            syncPutIndexSettings = esIndexService.syncPutIndexSettings(phyCluster,
                    Lists.newArrayList(indexName), finalSettingMap, RETRY_COUNT);
            if (syncPutIndexSettings) {
                final Result<IndexSettingVO> afterSetting = getSetting(param.getCluster(), indexName, projectId);
                operateRecordService.saveOperateRecordWithManualTrigger(
                        new TemplateSettingOperateRecord(beforeSetting.getData(), afterSetting.getData()).toString(),
                        operator, projectId, indexName, OperateTypeEnum.INDEX_TEMPLATE_MANAGEMENT_EDIT_SETTING);
            }
        }


        return Result.build(syncPutIndexSettings);
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

        List<IndexShardInfo> indexShardInfoList = null;
        try {
            indexShardInfoList = esIndexCatService.syncGetIndexShardInfo(phyCluster, indexName);
        } catch (ESOperateException e) {
            LOGGER.error("class=IndicesManagerImpl||method=getIndexShardsInfo||clusterName={}||errMsg=fail to get indexShardInfo",
                    cluster);
            return Result.buildFail("获取索引shard(主)在节点中的分布详情异常");
        }
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
    
        IndexCatCell indexCatCell = esIndexCatService.syncGetCatIndexInfoById(phyCluster, indexName);
        if (Objects.isNull(indexCatCell)) {
            return Result.buildFail("获取单个索引详情信息失败");
        }
        //设置索引阻塞信息
        List<IndexCatCell> finalIndexCatCellList = esIndexService.buildIndexAliasesAndBlockInfo(phyCluster,
                Collections.singletonList(indexCatCell));
        List<IndexCatCellVO> indexCatCellVOList = ConvertUtil.list2List(finalIndexCatCellList, IndexCatCellVO.class);
    
        return Result.buildSucc(indexCatCellVOList.get(0));
    }

    @Override
    public Result<Void> addIndexAliases(IndexCatCellWithConfigDTO param, Integer projectId, String operator) {
        Result<String> getClusterRet = getClusterPhyByClusterNameAndProjectId(param.getCluster(), projectId);
        if (getClusterRet.failed()) {
            return Result.buildFrom(getClusterRet);
        }
        String phyCluster = getClusterRet.getData();
        Result<Void> result = esIndexService.addAliases(phyCluster, param.getIndex(), param.getAliases());
        if (result.success()) {
    
            operateRecordService.saveOperateRecordWithManualTrigger(
                    String.format("index:【%s】, 添加别名：【%s】", param.getIndex(), param.getAliases()), operator,
                    projectId, param.getIndex(), OperateTypeEnum.INDEX_MANAGEMENT_ALIAS_MODIFY);
        }
        return result;
    }

    @Override
    public Result<Void> deleteIndexAliases(IndexCatCellWithConfigDTO param, Integer projectId, String operator) {
        Result<String> getClusterRet = getClusterPhyByClusterNameAndProjectId(param.getCluster(), projectId);
        if (getClusterRet.failed()) {
            return Result.buildFrom(getClusterRet);
        }
        String phyCluster = getClusterRet.getData();
        Result<Void> result = esIndexService.deleteAliases(phyCluster, param.getIndex(), param.getAliases());
        if (result.success()) {
            operateRecordService.saveOperateRecordWithManualTrigger(
                    String.format("index:【%s】删除别名：【%s】", param.getIndex(), param.getAliases()), operator, projectId,
                    param.getIndex(), OperateTypeEnum.INDEX_MANAGEMENT_ALIAS_MODIFY);
        }
        return result;
    }

    @Override
    public Result<List<String>> getIndexAliases(String cluster, String indexName, Integer projectId) {
        Result<String> getClusterRet = getClusterPhyByClusterNameAndProjectId(cluster, projectId);
        if (getClusterRet.failed()) {
            return Result.buildFrom(getClusterRet);
        }
        String phyCluster = getClusterRet.getData();
        Map<String, List<String>> aliasMap = esIndexService.syncGetIndexAliasesByIndices(phyCluster, indexName);
        return Result.buildSucc(aliasMap.getOrDefault(indexName, Lists.newArrayList()));
    }

    @Override
    public Result<Void> rollover(IndexRolloverDTO param, String operator, Integer projectId) {
        if (null == param.getIndices()) {
            return Result.buildFail("索引为空");
        }
        JSONArray resultMessage=new JSONArray();
        for (IndexCatCellDTO indexCatCellDTO : param.getIndices()) {
            String cluster = indexCatCellDTO.getCluster();
            if (!RegexUtils.checkEndWithHyphenNumbers(indexCatCellDTO.getIndex())) {
                return Result.buildFail("索引后缀必须按照横杠加数字为结尾才可以进行 rollover, 如：test-1");
            }
            final List<String> aliasList = esIndexService.syncGetIndexAliasesByExpression(cluster,
                    indexCatCellDTO.getIndex()).stream().map(Tuple::getV2).collect(Collectors.toList());
            if (AriusObjUtils.isEmptyList(aliasList)) {
                return Result.buildFail("alias 为空");
            }
            final String aliasName = aliasList.stream()
                    .map(alias -> Tuples.of(alias, esIndexService.countIndexByAlias(cluster, alias)))
                    .filter(aliasTuples -> aliasTuples.v2.success())
                    .map(aliasTuples -> Tuples.of(aliasTuples.v1, aliasTuples.v2().getData()))
                    .filter(aliasTuples -> aliasTuples.v2 == 1).findFirst().map(TupleTwo::v1).orElse(null);
    
            if (Objects.isNull(aliasName)) {
                return Result.buildFail(String.format("索引 %s 没有匹配到具有唯一性的别名，rollover 操作无法执行",
                        indexCatCellDTO.getIndex()));
            }

            Result<Void> rolloverResult = esIndexService.rollover(cluster, aliasName,
                param.getContent());
            if (rolloverResult.failed()) {
                return rolloverResult;
            }
            resultMessage.add(rolloverResult.getMessage());
            //操作记录
            operateRecordService.saveOperateRecordWithManualTrigger(
                    String.format("【%s】使用别名{%s}执行rollover",indexCatCellDTO.getIndex(),aliasName), operator, projectId,
                    indexCatCellDTO.getIndex(), OperateTypeEnum.INDEXING_SERVICE_RUN);
        }

        return Result.buildSuccWithMsg(resultMessage.toJSONString());
    }

    @Override
    public Result<Void> shrink(IndexCatCellWithConfigDTO param, String operator, Integer projectId) {
        final Result<Void> result = esIndexService.shrink(param.getCluster(), param.getIndex(), param.getTargetIndex(),
                param.getExtra());
        if (result.success()) {
            //操作记录
            operateRecordService.saveOperateRecordWithManualTrigger(String.format("【%s】执行 shrink", param.getIndex()),
                    operator, projectId, param.getIndex(), OperateTypeEnum.INDEXING_SERVICE_RUN);
        }
        return result;
       
    }

    @Override
    public Result<Void> split(IndexCatCellWithConfigDTO param, String operator, Integer projectId) {
        final Result<Void> result = esIndexService.split(param.getCluster(), param.getIndex(), param.getTargetIndex(),
                param.getExtra());
        if (result.success()){
              //操作记录
            operateRecordService.saveOperateRecordWithManualTrigger(String.format("【%s】执行split",param.getIndex()),
                    operator, projectId, param.getIndex(), OperateTypeEnum.INDEXING_SERVICE_RUN);
        }
        return result;
      
    }

    @Override
    public Result<Void> forceMerge(IndexForceMergeDTO param, String operator, Integer projectId) {
        if (null == param.getIndices()) {
            return Result.buildFail("索引为空");
        }
        JSONArray resultMessage=new JSONArray();
        List<TupleTwo</*cluster*/String,/*index*/String>> clusterIndexTuple=Lists.newArrayList();
        for (IndexCatCellDTO indexCatCellDTO : param.getIndices()) {
            Result<Void> forceMergeResult = esIndexService.forceMerge(indexCatCellDTO.getCluster(),
                indexCatCellDTO.getIndex(), param.getMaxNumSegments(), param.getOnlyExpungeDeletes());
            if (forceMergeResult.failed()) {
                return Result.buildFrom(forceMergeResult);
            }
                 //操作记录
            operateRecordService.saveOperateRecordWithManualTrigger(
                    String.format("【%s】执行 forceMerge", indexCatCellDTO.getIndex()), operator, projectId,
                    indexCatCellDTO.getIndex(), OperateTypeEnum.INDEXING_SERVICE_RUN);
            clusterIndexTuple.add(Tuples.of(indexCatCellDTO.getCluster(), indexCatCellDTO.getIndex()));
            resultMessage.add(JSON.parseObject(forceMergeResult.getMessage()));
        }
        //需要发布事件进行arius_cat_index_info 采集更新信息，尽可能保证数据的时效性
        SpringTool.publish(new RefreshCatIndexInfoEvent(this, clusterIndexTuple));
        return Result.buildSuccWithMsg(resultMessage.toJSONString());
    }

    @Override
    public Result<List<String>> getClusterPhyIndexName(String clusterPhyName, Integer projectId, String index) {
        if (!projectService.checkProjectExist(projectId)) {
            return Result.buildParamIllegal(String.format("There is no projectId:%s", projectId));
        }

        return Result.buildSucc(esIndexCatService.syncGetIndexListByProjectIdAndFuzzyIndexAndClusterPhy( clusterPhyName,index ));
    }

    @Override
    public Result<List<String>> getClusterLogicIndexName(String clusterLogicName, Integer projectId, String index) {
        List<String> indexNames = esIndexCatService.syncGetIndexListByProjectIdAndFuzzyIndexAndClusterLogic(projectId, clusterLogicName,index );
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

        // 判断索引名称是否和集群模版名称前缀匹配
        IndexCatCellWithConfigDTO indexWithConfigDTO = new IndexCatCellWithConfigDTO();
        indexWithConfigDTO.setIndex(indexName);
        Result<Void> checkValidRet = checkValid(indexWithConfigDTO, phyClusterName);
        if (checkValidRet.failed()) {
            return Result.buildFrom(checkValidRet);
        }

        return Result.buildSucc(esIndexService.syncIsIndexExist(phyClusterName, indexName));
    }

    @Override
    public List<IndexCatCellWithTemplateVO> listIndexCatCellWithTemplateByTemplatePhyId(Long physicalId) {
        List<CatIndexResult> indices = listIndexCatInfoByTemplatePhyId(physicalId);
        if (CollectionUtils.isEmpty(indices)) {
            return Lists.newArrayList();
        }
        return ConvertUtil.list2List(indices,IndexCatCellWithTemplateVO.class);
    }

    @Override
    public List<CatIndexResult> listIndexCatInfoByTemplatePhyId(Long physicalId) {
        IndexTemplatePhyWithLogic templatePhysicalWithLogic = indexTemplatePhyService
            .getTemplateWithLogicById(physicalId);
        if (templatePhysicalWithLogic == null) {
            return Lists.newArrayList();
        }
        String expression = templatePhysicalWithLogic.getExpression();
        if (templatePhysicalWithLogic.getVersion() != null && templatePhysicalWithLogic.getVersion() > 0
            && !expression.endsWith(START)) {
            expression = expression + START;
        }
        return esIndexService.syncCatIndexByExpression(templatePhysicalWithLogic.getCluster(), expression);
    }
    
    /**
     * @param clusterPhy
     * @param indexNameList
     * @param projectId
     * @param operator
     * @return
     */
    @Override
    public Result<Void> deleteIndexByCLusterPhy(String clusterPhy, List<String> indexNameList, Integer projectId,
                                                String operator) {
        if (indexNameList.size() == esIndexService.syncBatchDeleteIndices(clusterPhy, indexNameList, RETRY_COUNT)) {
            Result<Void> batchSetIndexFlagInvalidResult = updateIndexFlagInvalid(clusterPhy, indexNameList);
            if (batchSetIndexFlagInvalidResult.success()) {
                for (String indexName : indexNameList) {
                    operateRecordService.saveOperateRecordWithManualTrigger(String.format("删除索引：【%s】", indexName),
                            operator, projectId, indexName, OperateTypeEnum.INDEX_MANAGEMENT_DELETE);
                }
            
            }
            
        }
        
        
        return Result.buildSucc();
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

    private Result<Void> checkEditIndexBlockSetting(List<IndicesBlockSettingDTO> params, Integer projectId) {
        for (IndicesBlockSettingDTO param : params) {
            Result<Void> ret = basicCheckParam(param.getCluster(), param.getIndex(), projectId);
            if (ret.failed()) {
                return ret;
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

    /**
     * 注意， 这里普通用户侧前端传输cluster值是：逻辑集群名称，运维侧是：物理集群名称
     * @param cluster
     * @param projectId
     * @return
     */
    private Result<String> getClusterPhyByClusterNameAndProjectId(String cluster, Integer projectId) {
        String phyClusterName;
        if (AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
            phyClusterName = cluster;
        } else {
            ClusterLogic clusterLogic = clusterLogicService.getClusterLogicByNameAndProjectId(cluster,projectId );
            if (null == clusterLogic) {
                return Result.buildParamIllegal(String.format("逻辑集群[%s]不存在", cluster));
            }
            ClusterRegion clusterRegion = clusterRegionService.getRegionByLogicClusterId(clusterLogic.getId());
            if (null == clusterRegion) {
                return Result.buildParamIllegal("逻辑集群未绑定Region");
            }
            phyClusterName = clusterRegion.getPhyClusterName();
            if (!esClusterService.isConnectionStatus(phyClusterName)){
                return Result.buildFail(String.format("%s 集群不正常",cluster));
            }
        }
        return Result.buildSucc(phyClusterName);
    }

    private Result<Void> initIndexCreateDTO(IndexCatCellWithConfigDTO indexCreateDTO, Integer projectId) {
        if (!AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
            ClusterLogic clusterLogic = clusterLogicService.getClusterLogicByNameAndProjectId(indexCreateDTO.getCluster(), projectId);
            if (null == clusterLogic) {
                return Result.buildParamIllegal(String.format("逻辑集群[%s]不存在", indexCreateDTO.getCluster()));
            }
            ClusterRegion clusterRegion = clusterRegionService.getRegionByLogicClusterId(clusterLogic.getId());
            if (null == clusterRegion) { return Result.buildParamIllegal("逻辑集群未绑定Region");}

            // 这里用户侧，传逻辑集群名称 这里先补丁适配
            indexCreateDTO.setClusterLogic(clusterLogic.getName());
            indexCreateDTO.setResourceId(clusterLogic.getId());
            indexCreateDTO.setCluster(clusterRegion.getPhyClusterName());
        }

        indexCreateDTO.setPlatformCreateFlag(true);
        indexCreateDTO.setProjectId(projectId);
        return Result.buildSucc();
    }

    private Result<Void> checkValid(IndexCatCellWithConfigDTO params, String phyCluster) {
        String index = params.getIndex();
        String key   = phyCluster + "@" + index;

        List<IndexTemplatePhy> indexTemplatePhyList = indexTemplatePhyService.listTemplate();
        if (CollectionUtils.isEmpty(indexTemplatePhyList)) { return Result.buildSucc();}

        List<String> existKeyList = indexTemplatePhyList.stream().map(r -> r.getCluster() + "@" + r.getName())
                .distinct().collect(Collectors.toList());
        for (String existKey : existKeyList) {
            if (existKey.startsWith(key) || key.startsWith(existKey)) {
                return Result.buildFail(String.format("创建的索引名称[%s]不允许和该集群模板名称[%s]存在相互前缀的匹配, 请修改索引名称",
                        index, existKey.split("@")[1]));
            }
        }
        return Result.buildSucc();
    }
}