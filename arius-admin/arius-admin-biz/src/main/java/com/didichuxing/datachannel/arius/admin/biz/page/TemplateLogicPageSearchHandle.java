package com.didichuxing.datachannel.arius.admin.biz.page;

import com.didichuxing.datachannel.arius.admin.biz.app.AppLogicTemplateAuthManager;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplateLogicManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.PageDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplateConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ConsoleTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.constant.SortTermEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.DataTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by linyunan on 2021-10-14
 */
@Component
public class TemplateLogicPageSearchHandle extends BasePageSearchHandle<ConsoleTemplateVO> {

    private static final ILog LOGGER = LogFactory.getLog(TemplateLogicPageSearchHandle.class);

    @Autowired
    private AppService                  appService;

    @Autowired
    private AppLogicTemplateAuthManager appLogicTemplateAuthManager;

    @Autowired
    private IndexTemplatePhyService indexTemplatePhyService;

    @Autowired
    private TemplateLogicManager        templateLogicManager;

    @Autowired
    private IndexTemplateService indexTemplateService;

    @Autowired
    private ClusterLogicService          clusterLogicService;


    private static final FutureUtil<Void> BUILD_BELONG_CLUSTER_FUTURE_UTIL = FutureUtil.init("BUILD_BELONG_CLUSTER_FUTURE_UTIL",10,10,100);

    private static final FutureUtil<Void> RESOURCE_BUILD_FUTURE_UTIL = FutureUtil.init("RESOURCE_BUILD_FUTURE_UTIL",10,10,100);

    @Override
    protected Result<Boolean> validCheckForAppId(Integer appId) {
        if (!appService.isAppExists(appId)) {
            return Result.buildParamIllegal("项目不存在");
        }
        return Result.buildSucc(true);
    }

    @Override
    protected Result<Boolean> validCheckForCondition(PageDTO pageDTO, Integer appId) {
        if (pageDTO instanceof TemplateConditionDTO) {
            TemplateConditionDTO templateConditionDTO = (TemplateConditionDTO) pageDTO;

            if (null != templateConditionDTO.getDataType() && !DataTypeEnum.isExit(templateConditionDTO.getDataType())) {
                return Result.buildParamIllegal("数据类型不存在");
            }

            String templateName = templateConditionDTO.getName();
            if (!AriusObjUtils.isBlack(templateName) && (templateName.startsWith("*") || templateName.startsWith("?"))) {
                return Result.buildParamIllegal("模板名称不允许带类似*, ?等通配符查询");
            }

            if (null != templateConditionDTO.getSortTerm() && !SortTermEnum.isExit(templateConditionDTO.getSortTerm())) {
                return Result.buildParamIllegal(String.format("暂不支持排序类型[%s]", templateConditionDTO.getSortTerm()));
            }

            return Result.buildSucc(true);
        }

        LOGGER.error("class=IndicesPageSearchHandle||method=validCheckForCondition||errMsg=failed to convert PageDTO to templateConditionDTO");

        return Result.buildFail();
    }

    @Override
    protected void init(PageDTO pageDTO) {
        // Do nothing
    }

    @Override
    // todo: 灰度后清空这里代码
    protected PaginationResult<ConsoleTemplateVO> buildWithAuthType(PageDTO pageDTO, Integer authType, Integer appId) {
        TemplateConditionDTO condition = buildInitTemplateConditionDTO(pageDTO, appId);

        //1. 获取管理/读写/读/无权限的模板信息
        List<IndexTemplate> appAuthTemplatesList = templateLogicManager.getTemplatesByAppIdAndAuthType(appId, condition.getAuthType());
        if (CollectionUtils.isEmpty(appAuthTemplatesList)) {
            return PaginationResult.buildSucc(null, 0, condition.getPage(), condition.getSize());
        }

        //2. 根据无模板名称、有模板名称、有数量类型、有模板名称与数据类型等进行模糊匹配, 得出总结果
        List<IndexTemplate> meetConditionTemplateList = getMeetConditionTemplateList(condition, appAuthTemplatesList);

        //3. 设置命中数
        int hitTotal = meetConditionTemplateList.size();

        //4. 根据匹配结果进行对模板id进行排序, 根据分页信息过滤出需要获取的模板id
        sort(meetConditionTemplateList, condition.getSortTerm(), condition.getOrderByDesc());

        // 5.内存分页
        List<IndexTemplate> fuzzyAndLimitTemplateList = filterFullDataByPage(meetConditionTemplateList, condition);
        List<ConsoleTemplateVO>  consoleTemplateVOList     = ConvertUtil.list2List(fuzzyAndLimitTemplateList, ConsoleTemplateVO.class);

        //6. 设置权限
        //7. 设置所属物理集群名称列表
        //8. 设置是否开启了indexRollover能力
        RESOURCE_BUILD_FUTURE_UTIL
                .runnableTask(() -> consoleTemplateVOList.forEach(consoleTemplateVO -> consoleTemplateVO.setAuthType(condition.getAuthType())))
                .runnableTask(() -> setTemplateBelongClusterPhyNames(consoleTemplateVOList))
                .runnableTask(() -> setTemplateIndexRolloverStatus(consoleTemplateVOList))
                .runnableTask(() -> setTemplateClusterName(consoleTemplateVOList))
                .waitExecute();

        return PaginationResult.buildSucc(consoleTemplateVOList, hitTotal, condition.getPage(), condition.getSize());
    }

    @Override
    protected PaginationResult<ConsoleTemplateVO> buildWithoutAuthType(PageDTO pageDTO, Integer appId) {
        TemplateConditionDTO condition = buildInitTemplateConditionDTO(pageDTO, appId);

        List<IndexTemplate> matchIndexTemplate = indexTemplateService.pagingGetLogicTemplatesByCondition(condition);
        Integer totalHit = indexTemplateService.fuzzyLogicTemplatesHitByCondition(condition).intValue();

        List<ConsoleTemplateVO> consoleTemplateVOList = doBuildWithoutAuthType(matchIndexTemplate);
        return PaginationResult.buildSucc(consoleTemplateVOList, totalHit, condition.getPage(), condition.getSize());
    }

    /******************************************private***********************************************/
    /**
     * 根据无模板名称、有模板名称、有数量类型、有模板名称与数据类型等进行模糊匹配, 得出总结果
     *
     * @param condition
     * @param appAuthTemplatesList
     * @return
     */
    private List<IndexTemplate> getMeetConditionTemplateList(TemplateConditionDTO condition,
                                                             List<IndexTemplate> appAuthTemplatesList) {
        List<IndexTemplate> meetConditionTemplateList = Lists.newArrayList();
        if (null != condition.getHasDCDR()) {
            appAuthTemplatesList = appAuthTemplatesList.stream().filter(r -> condition.getHasDCDR().equals(r.getHasDCDR()))
                    .collect(Collectors.toList());
        }

        if (!AriusObjUtils.isEmptyList(condition.getClusterPhies())) {
            Set<String> logicIdSet = indexTemplatePhyService.getMatchNormalLogicIdByCluster(condition.getClusterPhies().get(0));
            appAuthTemplatesList = appAuthTemplatesList.stream().filter(r -> logicIdSet.contains(r.getId().toString()))
                    .collect(Collectors.toList());
        }

        if (!AriusObjUtils.isBlack(condition.getName())) {
            appAuthTemplatesList = appAuthTemplatesList.stream().filter(r -> r.getName().contains(condition.getName()))
                .collect(Collectors.toList());
        }
        if (null != condition.getDataType()) {
            appAuthTemplatesList = appAuthTemplatesList.stream()
                .filter(r -> r.getDataType().equals(condition.getDataType())).collect(Collectors.toList());
        }

        if (null != condition.getResourceId()) {
            appAuthTemplatesList = appAuthTemplatesList.stream()
                    .filter(r -> r.getResourceId().equals(condition.getResourceId())).collect(Collectors.toList());
        }

        meetConditionTemplateList.addAll(appAuthTemplatesList);
        return meetConditionTemplateList;
    }

    private List<ConsoleTemplateVO> doBuildWithoutAuthType(List<IndexTemplate> indexTemplateList) {
        if (CollectionUtils.isEmpty(indexTemplateList)) {
            return Lists.newArrayList();
        }

        List<ConsoleTemplateVO> consoleTemplateVOList = ConvertUtil.list2List(indexTemplateList, ConsoleTemplateVO.class);
        //1. 设置逻辑集群
        RESOURCE_BUILD_FUTURE_UTIL
                .runnableTask(() -> setTemplateClusterName(consoleTemplateVOList))
                .waitExecute();

        return consoleTemplateVOList;
    }

    private TemplateConditionDTO buildInitTemplateConditionDTO(PageDTO pageDTO, Integer appId) {
        if (pageDTO instanceof TemplateConditionDTO) {
            TemplateConditionDTO condition = (TemplateConditionDTO) pageDTO;
            condition.setProjectId(appId);
            return condition;
        }
        return null;
    }

    private void setTemplateBelongClusterPhyNames(List<ConsoleTemplateVO> consoleTemplateVOList) {
        if (CollectionUtils.isEmpty(consoleTemplateVOList)) {
            return;
        }

        for (ConsoleTemplateVO consoleTemplateVO : consoleTemplateVOList) {
            BUILD_BELONG_CLUSTER_FUTURE_UTIL.runnableTask(() -> {
                Set<String> clusterNameList = indexTemplatePhyService.getTemplateByLogicId(consoleTemplateVO.getId())
                        .stream()
                        .map(IndexTemplatePhy::getCluster)
                        .collect(Collectors.toSet());

                consoleTemplateVO.setClusterPhies(Lists.newArrayList(clusterNameList));
            });
        }

        BUILD_BELONG_CLUSTER_FUTURE_UTIL.waitExecute();
    }

    private void setTemplateClusterName(List<ConsoleTemplateVO> consoleTemplateVOList) {
        if (CollectionUtils.isEmpty(consoleTemplateVOList)) {
            return;
        }

        for (ConsoleTemplateVO consoleTemplateVO : consoleTemplateVOList) {
            BUILD_BELONG_CLUSTER_FUTURE_UTIL.runnableTask(() -> {
                ClusterLogic clusterLogic = clusterLogicService.getClusterLogicById(consoleTemplateVO.getResourceId());
                consoleTemplateVO.setCluster(clusterLogic.getName());
            });
        }

        BUILD_BELONG_CLUSTER_FUTURE_UTIL.waitExecute();
    }

    private void setTemplateIndexRolloverStatus(List<ConsoleTemplateVO> consoleTemplateVOList) {
        if (CollectionUtils.isEmpty(consoleTemplateVOList)) { return;}

        for (ConsoleTemplateVO consoleTemplateVO : consoleTemplateVOList) {
            IndexTemplateConfig templateConfig = indexTemplateService.getTemplateConfig(consoleTemplateVO.getId());
            consoleTemplateVO.setDisableIndexRollover(templateConfig.getDisableIndexRollover());
        }
    }

    /**
     * 对条件匹配后的结果集进行排序
     * @param meetConditionTemplateList              条件匹配结果集
     * @param sortTerm                               排序字段
     * @see   SortTermEnum                           支持的排序字段枚举
     * @param orderByDesc                            是否降序排序 true 是 false 否
     */
    private void sort(List<IndexTemplate> meetConditionTemplateList, String sortTerm, Boolean orderByDesc) {
        // 使用默认排序
        if (null == sortTerm) {
            Collections.sort(meetConditionTemplateList);
            return;
        }

        meetConditionTemplateList.sort((o1, o2) -> {
            // 可在此添加需要排序的项
            if (SortTermEnum.CHECK_POINT_DIFF.getType().equals(sortTerm)) {
                return orderByDesc ? o2.getCheckPointDiff().compareTo(o1.getCheckPointDiff()) :
                        o1.getCheckPointDiff().compareTo(o2.getCheckPointDiff());
            }

            if (SortTermEnum.LEVEL.getType().equals(sortTerm)) {
                return orderByDesc ? o2.getLevel().compareTo(o1.getLevel()) :
                        o1.getLevel().compareTo(o2.getLevel());
            }

            // 不排序
            return 0;
        });
    }
}