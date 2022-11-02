package com.didichuxing.datachannel.arius.admin.biz.page;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.dsl.template.DslTemplateConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.dsl.DslTemplatePO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.DslTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.metadata.service.DslTemplateService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 详细介绍类情况.
 *
 * @ClassName DslTemplatePageSearchHandle
 * @Author gyp
 * @Date 2022/6/13
 * @Version 1.0
 */
@Component
public class DslTemplatePageSearchHandle extends AbstractPageSearchHandle<DslTemplateConditionDTO, DslTemplateVO> {

    @Autowired
    private DslTemplateService dslTemplateService;

    @Override
    protected Result<Boolean> checkCondition(DslTemplateConditionDTO condition, Integer projectId) {

        String queryIndex = condition.getQueryIndex();
        if (!AriusObjUtils.isBlack(queryIndex) && (queryIndex.startsWith("*") || queryIndex.startsWith("?"))) {
            return Result.buildParamIllegal("查询索引名称不允许带类似*, ?等通配符");
        }
        if(AriusObjUtils.isNull(condition.getShowMetadata())){
            return Result.buildParamIllegal("是否展示元信息参数不能为空");
        }
        return Result.buildSucc(true);
    }

    @Override
    protected void initCondition(DslTemplateConditionDTO condition, Integer projectId) {
        // Do nothing
    }

    @Override
    protected PaginationResult<DslTemplateVO> buildPageData(DslTemplateConditionDTO condition, Integer projectId) {
        Tuple<Long, List<DslTemplatePO>> tuple;
        //普通项目只能查该项目下的dsl模板
        try {
            if (!AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
                tuple = dslTemplateService.getDslTemplatePage(projectId, condition);
                // 超级项目不带 projectId 条件查询时，可查到所有项目的 dsl 模板
            } else {
                if(condition.getShowMetadata()){
                    tuple = dslTemplateService.getDslTemplatePage(condition.getProjectId(), condition);
                }else{
                    tuple = dslTemplateService.getDslTemplatePageWithoutMetadataCluster(condition.getProjectId(), condition);
                }
            }
        } catch (ESOperateException e) {
            return PaginationResult.buildFail(e.getMessage());
        }

        if (tuple == null) {
            return PaginationResult.buildSucc(new ArrayList<>(), 0L, condition.getPage(), condition.getSize());
        }
        List<DslTemplateVO> dslTemplateVOList = ConvertUtil.list2List(tuple.v2(), DslTemplateVO.class);
        return PaginationResult.buildSucc(dslTemplateVOList, tuple.v1(), condition.getPage(), condition.getSize());
    }
}