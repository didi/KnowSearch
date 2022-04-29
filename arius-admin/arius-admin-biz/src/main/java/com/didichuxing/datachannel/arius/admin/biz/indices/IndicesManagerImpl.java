package com.didichuxing.datachannel.arius.admin.biz.indices;

import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum.INDEX_BLOCK_SETTING;
import static com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.ModuleEnum.INDEX_OP;
import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateContant.PRIMARY;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PagingData;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndicesOpenOrCloseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPhyManager;
import com.didichuxing.datachannel.arius.admin.biz.page.IndicesPageSearchHandle;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndicesBlockSettingDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndicesClearDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndicesConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexCatCellVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexMappingVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexSettingVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexShardInfoVO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.mapping.AriusTypeProperty;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary.IndexShardInfo;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.common.constant.PageSearchHandleTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.index.IndexBlockEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.common.util.SizeUtil;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexCatService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.metadata.job.index.IndexCatInfoCollector;
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
    @Autowired
    private AppService            appService;

    @Autowired
    private ESIndexCatService     esIndexCatService;

    @Autowired
    private ESIndexService        esIndexService;

    @Autowired
    private ClusterPhyManager     clusterPhyManager;

    @Autowired
    private OperateRecordService  operateRecordService;

    @Autowired
    private IndexCatInfoCollector indexCatInfoCollector;

    @Autowired
    private HandleFactory         handleFactory;

    private static final String DEFAULT_SORT_TERM = "timestamp";

    @Override
    public PaginationResult<IndexCatCellVO> pageGetIndexCatInfoVO(IndicesConditionDTO condition, Integer appId) {
        BaseHandle baseHandle     = handleFactory.getByHandlerNamePer(PageSearchHandleTypeEnum.INDICES.getPageSearchType());
        if (baseHandle instanceof IndicesPageSearchHandle) {
            IndicesPageSearchHandle handle = (IndicesPageSearchHandle) baseHandle;
            return handle.doPageHandle(condition, null, appId);
        }

        LOGGER.warn("class=TemplateLogicManagerImpl||method=pageGetConsoleClusterVOS||msg=failed to get the TemplateLogicPageSearchHandle");

        return PaginationResult.buildFail("获取索引分页信息失败");
    }

    @Override
    public Result<Boolean> batchDeleteIndex(List<IndicesClearDTO> params, Integer appId, String operator) {
        for (IndicesClearDTO param : params) {
            Result<Void> ret = basicCheckParam(param.getClusterPhyName(), param.getIndex(), appId);
            if (ret.failed()) {
                return Result.buildFrom(ret);
            }
        }

        Map<String, List<String>> cluster2IndexNameListMap = ConvertUtil.list2MapOfList(params,
            IndicesClearDTO::getClusterPhyName, IndicesClearDTO::getIndex);

        cluster2IndexNameListMap.forEach((cluster, indexNameList) -> {
            try {
                if (indexNameList.size() == esIndexService.syncBatchDeleteIndices(cluster, indexNameList, 3)) {
                    Result<Boolean> batchSetIndexFlagInvalidResult = batchSetIndexFlagInvalid(cluster, indexNameList);
                    if (batchSetIndexFlagInvalidResult.success()) {
                        operateRecordService.save(INDEX_OP, OperationEnum.DELETE, null,
                            String.format("批量删除%s集群中的索引：%s", cluster, ListUtils.strList2String(indexNameList)),
                            operator);
                    }
                }
            } catch (Exception e) {
                LOGGER.error(
                    "class=IndicesManagerImpl||method=batchDeleteIndex||cluster={}||indexNameList={}||errMsg={}",
                    cluster, ListUtils.strList2String(indexNameList), e.getMessage(), e);
            }
        });

        //索引删除完毕添加延迟时间, 防止refresh操作没及时生成segment, 就立马触发查询
        sleep(1000L);
        return Result.buildSucc(true);
    }

    @Override
    public Result<Boolean> batchUpdateIndexStatus(List<IndicesOpenOrCloseDTO> params, boolean indexNewStatus, Integer appId, String operator) {
        for (IndicesOpenOrCloseDTO param : params) {
            Result<Void> ret = basicCheckParam(param.getClusterPhyName(), param.getIndex(), appId);
            if (ret.failed()) {
                return Result.buildFrom(ret);
            }
        }

        Map<String, List<String>> cluster2IndexNameListMap = ConvertUtil.list2MapOfList(params,
                IndicesOpenOrCloseDTO::getClusterPhyName, IndicesOpenOrCloseDTO::getIndex);

        cluster2IndexNameListMap.forEach((cluster, indexNameList) -> {
            try {
                boolean syncOpenOrCloseResult = indexNewStatus ? esIndexService.syncBatchOpenIndices(cluster, indexNameList, 3) :
                        esIndexService.syncBatchCloseIndices(cluster, indexNameList, 3);
                if (syncOpenOrCloseResult) {
                    Result<Boolean> setCatIndexResult = batchSetIndexStatus(cluster, indexNameList, indexNewStatus);
                    if (setCatIndexResult.success()) {
                        if (indexNewStatus) {
                            operateRecordService.save(INDEX_OP, OperationEnum.OPEN_INDEX, null,
                                    String.format("批量开启%s集群中的索引：%s", cluster, ListUtils.strList2String(indexNameList)),
                                    operator);
                        } else {
                            operateRecordService.save(INDEX_OP, OperationEnum.CLOSE_INDEX, null,
                                    String.format("批量关闭%s集群中的索引：%s", cluster, ListUtils.strList2String(indexNameList)),
                                    operator);
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error(
                        "class=IndicesManagerImpl||method=batchUpdateIndexStatus||cluster={}||indexNameList={}||errMsg={}",
                        cluster, ListUtils.strList2String(indexNameList), e.getMessage(), e);
            }
        });
        // 防止refresh操作没及时生成segment, 就立马触发查询
        sleep(1000L);
        return Result.buildSucc(true);
    }

    @Override
    public Result<Boolean> batchSetIndexFlagInvalid(String cluster, List<String> indexNameList) {
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

    private Result<Boolean> batchSetIndexStatus(String cluster, List<String> indexNameList, boolean indexNewStatus) {
        boolean succ = indexNameList.size() == esIndexCatService.syncUpdateCatIndexStatus(cluster, indexNameList, indexNewStatus, 3);
        if (!succ) {
            LOGGER.error(
                    "class=IndicesManagerImpl||method=batchSetIndexStatus||cluster={}||indexNameList={}||errMsg=failed to batchSetIndexStatus",
                    cluster, ListUtils.strList2String(indexNameList));
        }
        return Result.build(succ);
    }

    @Override
    public Result<Boolean> batchEditIndexBlockSetting(List<IndicesBlockSettingDTO> params, Integer appId,
                                                      String operator) {
        Result<Boolean> checkResult = checkEditIndexBlockSetting(params, appId);
        if (checkResult.failed()) {
            return checkResult;
        }

        Map<String, List<IndicesBlockSettingDTO>> cluster2IndicesBlockSettingListMap = ConvertUtil.list2MapOfList(
            params, IndicesBlockSettingDTO::getClusterPhyName, indicesBlockSettingDTO -> indicesBlockSettingDTO);

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
    public Result<IndexMappingVO> getIndexMapping(String clusterPhyName, String indexName, Integer appId) {
        Result<Void> ret = basicCheckParam(clusterPhyName, indexName, appId);
        if (ret.failed()) {
            return Result.buildFrom(ret);
        }

        IndexMappingVO indexMappingVO = new IndexMappingVO();
        AriusTypeProperty ariusTypeProperty = new AriusTypeProperty();
        String indexMappingStr = esIndexService.syncGetIndexMapping(clusterPhyName, indexName);
        JSONObject indexMappingJsonObj = JSON.parseObject(indexMappingStr);
        ariusTypeProperty.setProperties(indexMappingJsonObj);
        indexMappingVO.setTypeProperties(ariusTypeProperty);
        indexMappingVO.setIndexName(indexName);
        return Result.buildSucc(indexMappingVO);
    }

    @Override
    public Result<IndexSettingVO> getIndexSetting(String clusterPhyName, String indexName, Integer appId) {
        Result<Void> ret = basicCheckParam(clusterPhyName, indexName, appId);
        if (ret.failed()) {
            return Result.buildFrom(ret);
        }

        IndexSettingVO indexSettingVO = new IndexSettingVO();
        MultiIndexsConfig multiIndexsConfig = esIndexService.syncGetIndexConfigs(clusterPhyName, indexName);
        if (null == multiIndexsConfig) {
            LOGGER.warn(
                "class=IndicesManagerImpl||method=getIndexSetting||cluster={}||index={}||errMsg=get empty Index configs ",
                clusterPhyName, indexName);
            return Result.buildSucc(indexSettingVO);
        }

        IndexConfig indexConfig = multiIndexsConfig.getIndexConfig(indexName);
        if (null == indexConfig) {
            LOGGER.warn(
                "class=IndicesManagerImpl||method=getIndexSetting||cluster={}||index={}||errMsg=get empty Index configs ",
                clusterPhyName, indexName);
            return Result.buildSucc(indexSettingVO);
        }
        indexSettingVO.setProperties(JsonUtils.reFlat(indexConfig.getSettings()));
        indexSettingVO.setIndexName(indexName);
        return Result.buildSucc(indexSettingVO);
    }

    @Override
    public Result<List<IndexShardInfoVO>> getIndexShardsInfo(String clusterPhyName, String indexName, Integer appId) {
        Result<Void> ret = basicCheckParam(clusterPhyName, indexName, appId);
        if (ret.failed()) {
            return Result.buildFrom(ret);
        }

        List<IndexShardInfo> indexShardInfoList = esIndexCatService.syncGetIndexShardInfo(clusterPhyName, indexName);
        List<IndexShardInfoVO> indexNodeShardVOList = indexShardInfoList.stream().filter(this::filterPrimaryShard)
            .map(this::coverUnit).collect(Collectors.toList());
        return Result.buildSucc(indexNodeShardVOList);
    }

    @Override
    public Result<IndexCatCellVO> getIndexCatInfo(String clusterPhyName, String indexName, Integer appId) {
        //1.建立单个索引查询的查询条件信息
        IndicesConditionDTO indicesConditionDTO = buildOneIndicesConditionDTO(clusterPhyName, indexName);
        PaginationResult<IndexCatCellVO> indexCatCellVOPaginationResult = pageGetIndexCatInfoVO(indicesConditionDTO, appId);
        if (indexCatCellVOPaginationResult.failed()) {
            return Result.buildFail("获取单个索引详情信息失败");
        }

        //2.索引信息查询
        PagingData<IndexCatCellVO> indexCatCellVOs = indexCatCellVOPaginationResult.getData();
        if (indexCatCellVOs == null || CollectionUtils.isEmpty(indexCatCellVOs.getBizData())) {
            return Result.buildSuccWithMsg("获取单个索引详情信息为空");
        }

        return Result.buildSucc(ConvertUtil.obj2Obj(indexCatCellVOs.getBizData().get(0), IndexCatCellVO.class));
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
            Result<Void> ret = basicCheckParam(param.getClusterPhyName(), param.getIndex(), appId);
            if (ret.failed()) {
                return Result.buildFrom(ret);
            }

            if (!IndexBlockEnum.isIndexBlockExit(param.getType())) {
                return Result.buildParamIllegal(String.format("阻塞类型%s不存在", param.getType()));
            }
        }

        return Result.buildSucc();
    }

    private IndicesConditionDTO buildOneIndicesConditionDTO(String clusterPhyName, String indexName) {
        IndicesConditionDTO indicesConditionDTO = new IndicesConditionDTO();
        indicesConditionDTO.setIndex(indexName);
        indicesConditionDTO.setClusterPhyName(Arrays.asList(clusterPhyName));
        indicesConditionDTO.setSortTerm(DEFAULT_SORT_TERM);
        indicesConditionDTO.setPage(1L);
        indicesConditionDTO.setSize(1L);
        return indicesConditionDTO;
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
}
