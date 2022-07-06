package com.didichuxing.datachannel.arius.admin.biz.page;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplateConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ConsoleTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.SortTermEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.DataTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.google.common.collect.Lists;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by linyunan on 2021-10-14
 */
@Component
public class TemplateLogicPageSearchHandle extends AbstractPageSearchHandle<TemplateConditionDTO, ConsoleTemplateVO> {

    @Autowired
    private IndexTemplateService indexTemplateService;

    @Autowired
    private ClusterLogicService clusterLogicService;


    private static final FutureUtil<Void> BUILD_BELONG_CLUSTER_FUTURE_UTIL = FutureUtil.init("BUILD_BELONG_CLUSTER_FUTURE_UTIL", 10, 10, 100);

    private static final FutureUtil<Void> RESOURCE_BUILD_FUTURE_UTIL = FutureUtil.init("RESOURCE_BUILD_FUTURE_UTIL", 10, 10, 100);


    @Override
    protected Result<Boolean> checkCondition(TemplateConditionDTO templateConditionDTO, Integer projectId) {

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

    @Override
    protected void initCondition(TemplateConditionDTO condition, Integer projectId) {
        if (!AuthConstant.SUPER_PROJECT_ID.equals(projectId)){
            condition.setProjectId(projectId);
        }
        
    }

    @Override
    protected PaginationResult<ConsoleTemplateVO> buildPageData(TemplateConditionDTO condition, Integer projectId) {
        List<IndexTemplate> matchIndexTemplate = indexTemplateService.pagingGetLogicTemplatesByCondition(condition);
        Integer totalHit = indexTemplateService.fuzzyLogicTemplatesHitByCondition(condition).intValue();

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