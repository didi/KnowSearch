package com.didichuxing.datachannel.arius.admin.biz.page;

import com.didichuxing.datachannel.arius.admin.biz.app.ProjectLogicTemplateAuthManager;
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
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.security.service.ProjectService;
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
public class TemplateLogicPageSearchHandle extends AbstractPageSearchHandle<PageDTO, ConsoleTemplateVO> {

    private static final ILog LOGGER = LogFactory.getLog(TemplateLogicPageSearchHandle.class);

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectLogicTemplateAuthManager projectLogicTemplateAuthManager;

    @Autowired
    private IndexTemplatePhyService indexTemplatePhyService;

    @Autowired
    private IndexTemplateService indexTemplateService;

    @Autowired
    private ClusterLogicService clusterLogicService;


    private static final FutureUtil<Void> BUILD_BELONG_CLUSTER_FUTURE_UTIL = FutureUtil.init("BUILD_BELONG_CLUSTER_FUTURE_UTIL", 10, 10, 100);

    private static final FutureUtil<Void> RESOURCE_BUILD_FUTURE_UTIL = FutureUtil.init("RESOURCE_BUILD_FUTURE_UTIL", 10, 10, 100);


    @Override
    protected Result<Boolean> checkCondition(PageDTO pageDTO, Integer projectId) {
        if (!projectService.checkProjectExist(projectId)) {
            return Result.buildParamIllegal("项目不存在");
        }
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
    protected void initCondition(PageDTO condition, Integer projectId) {
        if (condition instanceof TemplateConditionDTO) {
            TemplateConditionDTO templateConditionDTO = (TemplateConditionDTO) condition;
            templateConditionDTO.setProjectId(projectId);
        }
    }

    @Override
    protected PaginationResult<ConsoleTemplateVO> buildPageData(PageDTO pageDTO, Integer projectId) {
        TemplateConditionDTO condition          = (TemplateConditionDTO) pageDTO;
        List<IndexTemplate>  matchIndexTemplate = indexTemplateService.pagingGetLogicTemplatesByCondition(condition);
        long totalHit        = indexTemplateService.fuzzyLogicTemplatesHitByCondition(condition);

        List<ConsoleTemplateVO> consoleTemplateVOList = buildOtherInfo(matchIndexTemplate);
        return PaginationResult.buildSucc(consoleTemplateVOList, totalHit, condition.getPage(), condition.getSize());
    }

    /******************************************private***********************************************/
    private List<ConsoleTemplateVO> buildOtherInfo(List<IndexTemplate> indexTemplateList) {
        if (CollectionUtils.isEmpty(indexTemplateList)) { return Lists.newArrayList();}

        List<ConsoleTemplateVO> consoleTemplateVOList = ConvertUtil.list2List(indexTemplateList, ConsoleTemplateVO.class);
        //1. 设置逻辑集群
        RESOURCE_BUILD_FUTURE_UTIL
                .runnableTask(() -> setTemplateClusterName(consoleTemplateVOList))
                .waitExecute();
        return consoleTemplateVOList;
    }

    private void setTemplateClusterName(List<ConsoleTemplateVO> consoleTemplateVOList) {
        if (CollectionUtils.isEmpty(consoleTemplateVOList)) { return;}

        for (ConsoleTemplateVO consoleTemplateVO : consoleTemplateVOList) {
            BUILD_BELONG_CLUSTER_FUTURE_UTIL.runnableTask(() -> {
                ClusterLogic clusterLogic = clusterLogicService.getClusterLogicById(consoleTemplateVO.getResourceId());
                if (null != clusterLogic) { consoleTemplateVO.setCluster(clusterLogic.getName());}
            });
        }
        BUILD_BELONG_CLUSTER_FUTURE_UTIL.waitExecute();
    }
}