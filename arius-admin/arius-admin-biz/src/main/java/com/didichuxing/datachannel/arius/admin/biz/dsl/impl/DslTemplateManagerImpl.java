package com.didichuxing.datachannel.arius.admin.biz.dsl.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.PageSearchHandleTypeEnum.DSL_TEMPLATE;

import com.didichuxing.datachannel.arius.admin.biz.dsl.DslTemplateManager;
import com.didichuxing.datachannel.arius.admin.biz.page.DslTemplatePageSearchHandle;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.dsl.template.DslTemplateConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.dsl.DslTemplatePO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.DslTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didichuxing.datachannel.arius.admin.common.tuple.TupleThree;
import com.didichuxing.datachannel.arius.admin.common.tuple.TupleTwo;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.metadata.service.DslTemplateService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.security.service.ProjectService;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * @author cjm
 */
@Component
public class DslTemplateManagerImpl implements DslTemplateManager {

    private static final ILog    LOGGER = LogFactory.getLog(DslTemplateManagerImpl.class);

    @Autowired
    private DslTemplateService   dslTemplateService;
    @Autowired
    private OperateRecordService operateRecordService;
    @Autowired
    private ProjectService       projectService;

    @Autowired
    private HandleFactory        handleFactory;

    @Override
    public Result<Boolean> updateDslTemplateQueryLimit(Integer projectId, String operator,
                                                       List<String> dslTemplateMd5List, Double queryLimit) {
        if (CollectionUtils.isEmpty(dslTemplateMd5List)) {
            return Result.build(true);
        }
        final TupleTwo</*变更后的结果*/Boolean, /*变更前的dsl*/Map</*dslTemplateMd5*/String, /*变更前的queryLimit*/ Double>> updateDslTemplateQueryLimit = dslTemplateService
            .updateDslTemplateQueryLimit(projectId, dslTemplateMd5List, queryLimit);
        if (Boolean.TRUE.equals(updateDslTemplateQueryLimit.v1)) {
            for (Entry<String, Double> entry : updateDslTemplateQueryLimit.v2.entrySet()) {
                OperateRecord operateRecord = new OperateRecord.Builder()
                    .content(String.format("queryLimit %f->%f", entry.getValue(), queryLimit))
                    .operationTypeEnum(OperateTypeEnum.QUERY_TEMPLATE_DSL_CURRENT_LIMIT_ADJUSTMENT)
                    .project(projectService.getProjectBriefByProjectId(projectId)).userOperation(operator)
                    .bizId(entry.getKey()).buildDefaultManualTrigger();
                operateRecordService.save(operateRecord);
            }

        }
        return Result.buildSucc();
    }

    @Override
    public Result<Boolean> changeDslTemplateStatus(Integer projectId, String operator, String dslTemplateMd5) {
        if (StringUtils.isEmpty(dslTemplateMd5)) {
            return Result.build(true);
        }
        final TupleThree</*变更前*/Boolean, /*变更后*/ Boolean, /*变更状态*/Boolean> templateStatus = dslTemplateService
            .updateDslTemplateStatus(projectId, dslTemplateMd5);
        if (Boolean.TRUE.equals(templateStatus.v3)) {
            OperateRecord operateRecord = new OperateRecord.Builder()
                .content("变更前:" + templateStatus.v1 + ";变更后:" + templateStatus.v3)
                .operationTypeEnum(OperateTypeEnum.QUERY_TEMPLATE_DISABLE)
                .project(projectService.getProjectBriefByProjectId(projectId)).userOperation(operator)
                .bizId(dslTemplateMd5).buildDefaultManualTrigger();
            operateRecordService.save(operateRecord);
        }
        return Result.build(templateStatus.v3);
    }

    @Override
    public Result<DslTemplateVO> getDslTemplateDetail(Integer projectId, String dslTemplateMd5) {
        if (StringUtils.isEmpty(dslTemplateMd5)) {
            return Result.buildSucc();
        }
        DslTemplatePO dslTemplatePO = dslTemplateService.getDslTemplateDetail(projectId, dslTemplateMd5);
        return Result.buildSucc(ConvertUtil.obj2Obj(dslTemplatePO, DslTemplateVO.class));
    }

    @Override
    public PaginationResult<DslTemplateVO> getDslTemplatePage(Integer projectId,
                                                              DslTemplateConditionDTO queryDTO) throws NotFindSubclassException {
        BaseHandle baseHandle = handleFactory.getByHandlerNamePer(DSL_TEMPLATE.getPageSearchType());
        if (baseHandle instanceof DslTemplatePageSearchHandle) {
            DslTemplatePageSearchHandle handle = (DslTemplatePageSearchHandle) baseHandle;
            return handle.doPage(queryDTO, projectId);
        }

        LOGGER.warn(
            "class=DslTemplateManagerImpl||method=getDslTemplatePage||msg=failed to get the DslTemplatePageSearchHandle");

        return PaginationResult.buildFail("分页获取DSL查询模版信息失败");
    }

}