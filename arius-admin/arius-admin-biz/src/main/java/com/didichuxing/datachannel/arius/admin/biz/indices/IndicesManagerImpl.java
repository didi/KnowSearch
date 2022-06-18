package com.didichuxing.datachannel.arius.admin.biz.indices;

import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum.INDEX_BLOCK_SETTING;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum.INDEX_OP;
import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateContant.PRIMARY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPhyManager;
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
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexCatCellVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexMappingVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexSettingVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexShardInfoVO;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;
import com.didichuxing.datachannel.arius.admin.common.constant.PageSearchHandleTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.index.IndexBlockEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.mapping.AriusTypeProperty;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.common.util.SizeUtil;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexCatService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.metadata.job.index.IndexCatInfoCollector;
import com.didiglobal.logi.elasticsearch.client.response.indices.catindices.CatIndexResult;
import com.didiglobal.logi.elasticsearch.client.response.setting.common.MappingConfig;
import com.didiglobal.logi.elasticsearch.client.response.setting.index.IndexConfig;
import com.didiglobal.logi.elasticsearch.client.response.setting.index.MultiIndexsConfig;
import com.didiglobal.logi.elasticsearch.client.utils.JsonUtils;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;

/**
 * @author lyn
 * @date 2021/09/28
 **/
@Component
public class IndicesManagerImpl implements IndicesManager {
    private static final ILog     LOGGER = LogFactory.getLog(IndicesManagerImpl.class);
    public static final int RETRY_COUNT = 3;
    @Autowired
    private AppService            appService;

    @Autowired
    private ESIndexCatService     esIndexCatService;

    @Autowired
    private ESIndexService        esIndexService;

    @Autowired
    private ClusterPhyManager     clusterPhyManager;

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
    public PaginationResult<IndexCatCellVO> pageGetIndex(IndexQueryDTO condition, Integer appId) {
        BaseHandle baseHandle     = handleFactory.getByHandlerNamePer(PageSearchHandleTypeEnum.INDEX.getPageSearchType());
        if (baseHandle instanceof IndexPageSearchHandle) {
            IndexPageSearchHandle handle = (IndexPageSearchHandle) baseHandle;
            return handle.doPage(condition, appId);
        }

        LOGGER.warn("class=TemplateLogicManagerImpl||method=pageGetConsoleClusterVOS||msg=failed to get the TemplateLogicPageSearchHandle");

        return PaginationResult.buildFail("获取索引分页信息失败");
    }

    @Override
    public Result<Void> createIndex(IndexCatCellWithConfigDTO indexCreateDTO, Integer appId) {
        Result<String> getClusterRet = getClusterPhyByClusterNameAndAppId(indexCreateDTO.getCluster(), appId);
        if (getClusterRet.failed()) {
            return Result.buildFrom(getClusterRet);
        }
        String phyCluster = getClusterRet.getData();
        try {
            esIndexService.syncCreateIndex(phyCluster, indexCreateDTO.getIndex(), indexCreateDTO.getMapping(), indexCreateDTO.getSetting(), RETRY_COUNT);
        } catch (Exception e) {
            LOGGER.error("class=IndicesManagerImpl||method=createIndex||msg=create index failed||index={}" + indexCreateDTO.getIndex(), e);
            return Result.buildFail();
        }
        return Result.buildSucc();
    }

    @Override
    public Result<Boolean> deleteIndex(List<IndexCatCellDTO> params, Integer appId, String operator) {
        return batchOperateIndex(params, appId, (cluster, indexNameList) -> {
            if (indexNameList.size() == esIndexService.syncBatchDeleteIndices(cluster, indexNameList, RETRY_COUNT)) {
                Result<Boolean> batchSetIndexFlagInvalidResult = updateIndicesFlagInvalid(cluster, indexNameList);
                if (batchSetIndexFlagInvalidResult.success()) {
                    operateRecordService.save(INDEX_OP, OperationEnum.DELETE, null,
                        String.format("批量删除%s集群中的索引：%s", cluster, ListUtils.strList2String(indexNameList)), operator);
                }
            }
            return Result.buildSucc();
        });
    }

    @Override
    public  <T,U,R> Result<Boolean> batchOperateIndex(List<IndexCatCellDTO> params, Integer appId, BiFunction<String,List<String>, Result<Void>> function) {
        for (IndexCatCellDTO param : params) {
            Result<String> getClusterRet = getClusterPhyByClusterNameAndAppId(param.getCluster(), appId);
            if (getClusterRet.failed()) {
                return Result.buildFrom(getClusterRet);
            }
            String phyCluster = getClusterRet.getData();
            param.setCluster(phyCluster);
            Result<Void> ret = basicCheckParam(param.getCluster(), param.getIndex(), appId);
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
    public Result<Boolean> openIndex(List<IndexCatCellDTO> params, Integer appId, String operator) {
        return this.batchOperateIndex(params, appId, (cluster, indexNameList) -> {
            try {

                boolean syncOpenOrCloseResult = esIndexService.syncBatchOpenIndices(cluster, indexNameList, 3);
                if (!syncOpenOrCloseResult) {
                    return Result.buildFail("批量开启索引失败");
                }

                Result<Boolean> setCatIndexResult = updateIndicesStatus(cluster, indexNameList, "open");
                if (!setCatIndexResult.success()) {
                    return Result.buildFail("批量更新索引状态失败");
                }

                operateRecordService.save(INDEX_OP, OperationEnum.OPEN_INDEX, null,
                        String.format("批量开启%s集群中的索引：%s", cluster, ListUtils.strList2String(indexNameList)), operator);

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
    public Result<Boolean> closeIndex(List<IndexCatCellDTO> params, Integer appId, String operator) {
        return this.batchOperateIndex(params, appId, (cluster, indexNameList) -> {
            try {

                boolean syncOpenOrCloseResult = esIndexService.syncBatchCloseIndices(cluster, indexNameList, 3);
                if (!syncOpenOrCloseResult) {
                    return Result.buildFail("批量关闭索引失败");
                }

                Result<Boolean> setCatIndexResult = updateIndicesStatus(cluster, indexNameList, "close");
                if (!setCatIndexResult.success()) {
                    return Result.buildFail("批量更新索引状态失败");
                }

                operateRecordService.save(INDEX_OP, OperationEnum.CLOSE_INDEX, null,
                    String.format("批量关闭%s集群中的索引：%s", cluster, ListUtils.strList2String(indexNameList)), operator);

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
    public Result<Boolean> updateIndicesFlagInvalid(String cluster, List<String> indexNameList) {
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
    public Result<Boolean> editIndexBlockSetting(List<IndicesBlockSettingDTO> params, Integer appId,
                                                 String operator) {
        for (IndicesBlockSettingDTO param : params) {
            Result<String> getClusterRet = getClusterPhyByClusterNameAndAppId(param.getCluster(), appId);
            if (getClusterRet.failed()) {
                return Result.buildFrom(getClusterRet);
            }
            String phyCluster = getClusterRet.getData();
            param.setCluster(phyCluster);
        }
        Result<Boolean> checkResult = checkEditIndexBlockSetting(params, appId);
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
                        operateRecordService.save(INDEX_BLOCK_SETTING, OperationEnum.EDIT, null, operateContent,
                            operator);
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
    public Result<IndexMappingVO> getMapping(String cluster, String indexName, Integer appId) {
        Result<Void> ret = basicCheckParam(cluster, indexName, appId);
        if (ret.failed()) {
            return Result.buildFrom(ret);
        }

        IndexMappingVO indexMappingVO = new IndexMappingVO();
        AriusTypeProperty ariusTypeProperty = new AriusTypeProperty();
        String indexMappingStr = esIndexService.syncGetIndexMapping(cluster, indexName);
        JSONObject indexMappingJsonObj = JSON.parseObject(indexMappingStr);
        ariusTypeProperty.setProperties(indexMappingJsonObj);
        indexMappingVO.setTypeProperties(ariusTypeProperty);
        indexMappingVO.setIndexName(indexName);
        return Result.buildSucc(indexMappingVO);
    }

    @Override
    public Result<Void> editMapping(IndexCatCellWithConfigDTO param, Integer appId) {
        Result<String> getClusterRet = getClusterPhyByClusterNameAndAppId(param.getCluster(), appId);
        if (getClusterRet.failed()) {
            return Result.buildFrom(getClusterRet);
        }
        String phyCluster = getClusterRet.getData();
        String indexName = param.getIndex();
        String mapping = param.getMapping();
        Result<Void> ret = basicCheckParam(phyCluster, indexName, appId);
        if (ret.failed()) {
            return Result.buildFrom(ret);
        }

        try {
            MappingConfig mappingConfig = new MappingConfig(JSON.parseObject(mapping));
            return Result.build(esIndexService.syncUpdateIndexMapping(phyCluster, indexName, mappingConfig));
        } catch (Exception e) {
            LOGGER.error("class=IndicesManagerImpl||method=editMapping||cluster={}||index={}||errMsg={}", phyCluster, indexName, e.getMessage(), e);
            return Result.buildFail();
        }
    }

    @Override
    public Result<IndexSettingVO> getSetting(String cluster, String indexName, Integer appId) {
        Result<Void> ret = basicCheckParam(cluster, indexName, appId);
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
    public Result<Void> editSetting(IndexCatCellWithConfigDTO param, Integer appId) {
        Result<String> getClusterRet = getClusterPhyByClusterNameAndAppId(param.getCluster(), appId);
        if (getClusterRet.failed()) {
            return Result.buildFrom(getClusterRet);
        }
        String phyCluster = getClusterRet.getData();
        String indexName = param.getIndex();
        Result<Void> ret = basicCheckParam(phyCluster, indexName, appId);
        if (ret.failed()) {
            return Result.buildFrom(ret);
        }

        JSONObject settingObj = JSON.parseObject(param.getSetting());
        if (null == settingObj) {
            return Result.buildFail("setting 配置非法");
        }
        Map<String, String> settingMap = JsonUtils.flat(settingObj);
        try {
            return Result.build(esIndexService.syncPutIndexSetting(phyCluster, Lists.newArrayList(indexName), settingMap, RETRY_COUNT));
        } catch (Exception e) {
            LOGGER.error("class=IndicesManagerImpl||method=editSetting||cluster={}||index={}||errMsg=update setting fail", phyCluster, indexName, e);
            return Result.buildFail("更新索引setting fail");
        }

    }

    @Override
    public Result<List<IndexShardInfoVO>> getIndexShardsInfo(String cluster, String indexName, Integer appId) {
        Result<String> getClusterRet = getClusterPhyByClusterNameAndAppId(cluster, appId);
        if (getClusterRet.failed()) {
            return Result.buildFrom(getClusterRet);
        }
        String phyCluster = getClusterRet.getData();
        Result<Void> ret = basicCheckParam(phyCluster, indexName, appId);
        if (ret.failed()) {
            return Result.buildFrom(ret);
        }

        List<IndexShardInfo> indexShardInfoList = esIndexCatService.syncGetIndexShardInfo(phyCluster, indexName);
        List<IndexShardInfoVO> indexNodeShardVOList = indexShardInfoList.stream().filter(this::filterPrimaryShard)
            .map(this::coverUnit).collect(Collectors.toList());
        return Result.buildSucc(indexNodeShardVOList);
    }

    @Override
    public Result<IndexCatCellVO> getIndexCatInfo(String cluster, String indexName, Integer appId) {
        Integer queryAppId = null;
        if (!appService.isSuperApp(appId)) {
            queryAppId = appId;
        }
        Tuple<Long, List<IndexCatCell>> totalHitAndIndexCatCellListTuple = esIndexCatService
            .syncGetCatIndexInfo(cluster, indexName, null, queryAppId, 0L, 1L, DEFAULT_SORT_TERM, true);
        if (null == totalHitAndIndexCatCellListTuple
            || CollectionUtils.isEmpty(totalHitAndIndexCatCellListTuple.getV2())) {
            return Result.buildFail("获取单个索引详情信息失败");
        }
        //设置索引阻塞信息
        List<IndexCatCell> finalIndexCatCellList = esIndexService.buildIndexRealTimeData(cluster,
            totalHitAndIndexCatCellListTuple.getV2());
        List<IndexCatCellVO> indexCatCellVOList = ConvertUtil.list2List(finalIndexCatCellList, IndexCatCellVO.class);

        return Result.buildSucc(indexCatCellVOList.get(0));
    }

    @Override
    public Result<Void> editAlias(IndexCatCellWithConfigDTO param, Boolean flag, Integer appId) {
        Result<String> getClusterRet = getClusterPhyByClusterNameAndAppId(param.getCluster(), appId);
        if (getClusterRet.failed()) {
            return Result.buildFrom(getClusterRet);
        }
        String phyCluster = getClusterRet.getData();
        if (!flag) {
            return esIndexService.deleteAliases(phyCluster, param.getIndex(), param.getAliases());
        } else {
            return esIndexService.addAliases(phyCluster, param.getIndex(), param.getAliases());
        }
    }

    @Override
    public Result<List<String>> getIndexAliases(String cluster, String indexName, Integer appId) {
        Result<String> getClusterRet = getClusterPhyByClusterNameAndAppId(cluster, appId);
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
       
        String conditions = generateConditions(param);
        for (IndexCatCellDTO indexCatCellDTO : param.getIndices()) {
            String cluster = indexCatCellDTO.getCluster();
            List<Tuple<String, String>> aliasList = esIndexService.syncGetIndexAliasesByExpression(cluster, indexCatCellDTO.getIndex());
            if (AriusObjUtils.isEmptyList(aliasList)) {
                return Result.buildFail("alias 为空");
            }

            Result<Void> rolloverResult = esIndexService.rollover(cluster, aliasList.get(0).getV2(),conditions);
            if (rolloverResult.failed()) {
                return rolloverResult;
            }
        }

        return Result.buildSucc();
    }

    private String generateConditions(IndexRolloverDTO param) {
        Map<String, Object> conditionsMap = new HashMap<>();
        if (StringUtils.isNotBlank(param.getMaxAge())) {
            conditionsMap.put("max_age", param.getMaxAge());
        }
        if (StringUtils.isNotBlank(param.getMaxSize())) {
            conditionsMap.put("max_size", param.getMaxSize());
        }
        if (null != param.getMaxDocs()) {
            conditionsMap.put("max_docs", param.getMaxDocs());
        }

        return String.format("{\"conditions\":%s}", JSON.toJSONString(conditionsMap));
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
    public Result<List<String>> getClusterPhyIndexName(String clusterPhyName, Integer appId) {
        if (!appService.isAppExists(appId)) {
            return Result.buildParamIllegal(String.format("There is no appId:%s", appId));
        }

        return Result.buildSucc(esIndexService.syncGetIndexName(clusterPhyName));
    }
    @Override
    public Result<List<String>> getClusterLogicIndexName(String clusterLogicName, Integer appId) {
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
    public Result<Boolean> isExists(String cluster, String indexName, Integer appId) {
        if (!appService.isAppExists(appId)) {
            return Result.buildParamIllegal(String.format("当前登录项目Id[%s]不存在, 无权限操作", appId));
        }
        Result<String> getClusterRet = getClusterPhyByClusterNameAndAppId(cluster, appId);
        if (getClusterRet.failed()) {
            return Result.buildFrom(getClusterRet);
        }
        String phyClusterName = getClusterRet.getData();
        if (!clusterPhyManager.isClusterExists(phyClusterName)) {
            return Result.buildParamIllegal(String.format("物理集群[%s]不存在", phyClusterName));
        }
        return Result.buildSucc(esIndexService.syncIsIndexExist(phyClusterName, indexName));
    }

    /***************************************************private**********************************************************/
    private Result<Void> basicCheckParam(String cluster, String index, Integer appId) {
        if (!appService.isAppExists(appId)) {
            return Result.buildParamIllegal(String.format("当前登录项目Id[%s]不存在, 无权限操作", appId));
        }

        if (!clusterPhyManager.isClusterExists(cluster)) {
            return Result.buildParamIllegal(String.format("物理集群[%s]不存在", cluster));
        }

        if (!esIndexService.syncIsIndexExist(cluster, index)) {
            return Result.buildParamIllegal(String.format("集群[%s]中的索引[%s]不存在", cluster, index));
        }

        return Result.buildSucc();
    }

    private Result<Boolean> checkEditIndexBlockSetting(List<IndicesBlockSettingDTO> params, Integer appId) {
        for (IndicesBlockSettingDTO param : params) {
            Result<Void> ret = basicCheckParam(param.getCluster(), param.getIndex(), appId);
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

    private Result<String> getClusterPhyByClusterNameAndAppId(String cluster, Integer appId) {
        String phyClusterName;
        if (appService.isSuperApp(appId)) {
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
