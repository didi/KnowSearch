package com.didichuxing.datachannel.arius.admin.biz.page;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.didichuxing.datachannel.arius.admin.client.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.TemplateConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.biz.app.AppLogicTemplateAuthManager;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplateLogicManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.PageDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.ConsoleTemplateVO;
import com.didichuxing.datachannel.arius.admin.client.constant.template.DataTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppTemplateAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogic;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.TemplateLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.TemplatePhyService;
import com.google.common.collect.Lists;

import static com.didichuxing.datachannel.arius.admin.client.constant.app.AppTemplateAuthEnum.isTemplateAuthExitByCode;

/**
 * Created by linyunan on 2021-10-14
 */
@Component
public class TemplateLogicPageSearchHandle extends BasePageSearchHandle<ConsoleTemplateVO> {

    private static final ILog LOGGER = LogFactory.getLog(TemplateLogicPageSearchHandle.class);

    @Autowired
    private AppService                  appService;

    @Autowired
    private TemplatePhyService          templatePhyService;

    @Autowired
    private TemplateLogicManager        templateLogicManager;

    @Autowired
    private TemplateLogicService        templateLogicService;

    @Autowired
    private AppLogicTemplateAuthManager appLogicTemplateAuthManager;

    private static final FutureUtil<Void> futureUtil = FutureUtil.initBySystemAvailableProcessors("TemplateLogicPageSearchHandle",100);

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

            if (null != templateConditionDTO.getAuthType() && !isTemplateAuthExitByCode(templateConditionDTO.getAuthType())){
                return Result.buildParamIllegal("权限类型不存在");
            }

            String templateName = templateConditionDTO.getName();
            if (!AriusObjUtils.isBlack(templateName) && (templateName.startsWith("*") || templateName.startsWith("?"))) {
                return Result.buildParamIllegal("模板名称不允许带类似*, ?等通配符查询");
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
    protected PaginationResult<ConsoleTemplateVO> buildWithAuthType(PageDTO pageDTO, Integer authType, Integer appId) {
        TemplateConditionDTO condition = buildInitTemplateConditionDTO(pageDTO);
        if (null == condition) {
            LOGGER.error(
                "class=TemplateLogicPageSearchHandle||method=buildWithAuthType||errMsg=failed to convert PageDTO to TemplateConditionDTO");
            return PaginationResult.buildFail("获取模板查询信息失败");
        }

        //1. 获取管理/读写/读/无权限的模板信息
        List<IndexTemplateLogic> appAuthTemplatesList = templateLogicManager.getTemplatesByAppIdAndAuthType(appId,
            condition.getAuthType());
        if (CollectionUtils.isEmpty(appAuthTemplatesList)) {
            return PaginationResult.buildSucc();
        }

        //2. 根据无模板名称、有模板名称、有数量类型、有模板名称与数据类型等进行模糊匹配, 得出总结果
        List<IndexTemplateLogic> meetConditionTemplateList = getMeetConditionTemplateList(condition, appAuthTemplatesList);

        //3. 设置命中数
        int hitTotal = meetConditionTemplateList.size();

        //4. 根据匹配结果进行对模板id进行排序, 根据分页信息过滤出需要获取的模板id
        Collections.sort(meetConditionTemplateList);

        //5. 最后页临界点处理
        long size = getLastPageSize(condition, meetConditionTemplateList.size());

        List<IndexTemplateLogic> fuzzyAndLimitTemplateList = meetConditionTemplateList.subList(condition.getFrom().intValue(), (int) size);
        List<ConsoleTemplateVO>  consoleTemplateVOList     = ConvertUtil.list2List(fuzzyAndLimitTemplateList, ConsoleTemplateVO.class);
        //6. 设置权限
        consoleTemplateVOList.forEach(consoleTemplateVO -> consoleTemplateVO.setAuthType(condition.getAuthType()));
        //7. 设置所属物理集群名称列表
        setTemplateBelongClusterPhyNames(consoleTemplateVOList);
        
        return PaginationResult.buildSucc(consoleTemplateVOList, hitTotal, condition.getFrom(), condition.getSize());
    }

    private void setTemplateBelongClusterPhyNames(List<ConsoleTemplateVO> consoleTemplateVOList) {
        if (CollectionUtils.isEmpty(consoleTemplateVOList)) {
            return;
        }

        for (ConsoleTemplateVO consoleTemplateVO : consoleTemplateVOList) {
            futureUtil.runnableTask(() -> {
                Set<String> clusterNameList = templatePhyService.getTemplateByLogicId(consoleTemplateVO.getId())
                                                .stream()
                                                .map(IndexTemplatePhy::getCluster)
                                                .collect(Collectors.toSet());

                consoleTemplateVO.setClusterPhies(Lists.newArrayList(clusterNameList));
            });
        }

        futureUtil.waitExecute();
    }

    @Override
    protected PaginationResult<ConsoleTemplateVO> buildWithoutAuthType(PageDTO pageDTO, Integer appId) {
        TemplateConditionDTO condition = buildInitTemplateConditionDTO(pageDTO);
        if (null == condition) {
            LOGGER.error(
                    "class=TemplateLogicPageSearchHandle||method=buildWithoutAuthType||errMsg=failed to convert PageDTO to TemplateConditionDTO");
            return PaginationResult.buildFail("获取模板查询信息失败");
        }
        
        List<IndexTemplateLogic> pagingGetLogicTemplatesList = templateLogicService.pagingGetLogicTemplatesByCondition(condition);

        List<ConsoleTemplateVO> consoleTemplateVOList = doBuildWithoutAuthType(pagingGetLogicTemplatesList, appId);

        Long totalHit = templateLogicService.fuzzyLogicTemplatesHitByCondition(condition);

        return PaginationResult.buildSucc(consoleTemplateVOList, totalHit, condition.getFrom(), condition.getSize());
    }

    /******************************************private***********************************************/
    /**
     * 根据无模板名称、有模板名称、有数量类型、有模板名称与数据类型等进行模糊匹配, 得出总结果
     *
     * @param condition
     * @param appAuthTemplatesList
     * @return
     */
    private List<IndexTemplateLogic> getMeetConditionTemplateList(TemplateConditionDTO condition,
                                                                  List<IndexTemplateLogic> appAuthTemplatesList) {
        List<IndexTemplateLogic> meetConditionTemplateList = Lists.newArrayList();
        if (AriusObjUtils.isBlack(condition.getName()) && null == condition.getDataType()) {
            meetConditionTemplateList.addAll(appAuthTemplatesList);
            return meetConditionTemplateList;
        }
        if (!AriusObjUtils.isBlack(condition.getName()) && null == condition.getDataType()) {
            meetConditionTemplateList = appAuthTemplatesList.stream().filter(r -> r.getName().contains(condition.getName()))
                .collect(Collectors.toList());
            return meetConditionTemplateList;
        }
        if (AriusObjUtils.isBlack(condition.getName()) && null != condition.getDataType()) {
            meetConditionTemplateList = appAuthTemplatesList.stream()
                .filter(r -> r.getDataType().equals(condition.getDataType())).collect(Collectors.toList());
            return meetConditionTemplateList;
        }
        if (!AriusObjUtils.isBlack(condition.getName()) && null != condition.getDataType()) {
            meetConditionTemplateList = appAuthTemplatesList.stream()
                .filter(r -> r.getName().contains(condition.getName()) && r.getDataType().equals(condition.getDataType()))
                .collect(Collectors.toList());
            return meetConditionTemplateList;
        }
        return meetConditionTemplateList;
    }

    private List<ConsoleTemplateVO> doBuildWithoutAuthType(List<IndexTemplateLogic> indexTemplateLogicList,
                                                           Integer appId) {
        if (CollectionUtils.isEmpty(indexTemplateLogicList)) {
            return Lists.newArrayList();
        }

        List<AppTemplateAuth> appTemplateAuthList = appLogicTemplateAuthManager
            .getTemplateAuthListByTemplateListAndAppId(appId, indexTemplateLogicList);

        Map<Integer, Integer> templateId2AuthTypeMap = ConvertUtil.list2Map(appTemplateAuthList,
            AppTemplateAuth::getTemplateId, AppTemplateAuth::getType);

        List<ConsoleTemplateVO> consoleTemplateVOList = ConvertUtil.list2List(indexTemplateLogicList,
            ConsoleTemplateVO.class);

        //1. 设置权限
        consoleTemplateVOList.forEach(
            consoleTemplateVO -> consoleTemplateVO.setAuthType(templateId2AuthTypeMap.get(consoleTemplateVO.getId())));

        //2. 设置所属物理集群名称列表
        setTemplateBelongClusterPhyNames(consoleTemplateVOList);

        return consoleTemplateVOList;
    }

    private TemplateConditionDTO buildInitTemplateConditionDTO(PageDTO pageDTO) {
        if (pageDTO instanceof TemplateConditionDTO) {
            return (TemplateConditionDTO) pageDTO;
        }
        return null;
    }
}
