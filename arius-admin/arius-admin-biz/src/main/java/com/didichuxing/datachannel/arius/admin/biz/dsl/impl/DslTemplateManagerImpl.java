package com.didichuxing.datachannel.arius.admin.biz.dsl.impl;

import com.didichuxing.datachannel.arius.admin.biz.dsl.DslTemplateManager;
import com.didichuxing.datachannel.arius.admin.biz.page.DslTemplatePageSearchHandle;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.dsl.DslQueryLimitDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.dsl.template.DslTemplateConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.dsl.DslTemplatePO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.DslTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.metadata.service.DslTemplateService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.security.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.PageSearchHandleTypeEnum.DSL_TEMPLATE;

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
    public Result<Boolean> updateDslTemplateQueryLimit(List<DslQueryLimitDTO> dslQueryLimitDTOList) {
        if (CollectionUtils.isEmpty(dslQueryLimitDTOList)) {
            return Result.build(true);
        }
        return Result.buildSucc(dslTemplateService.updateDslTemplateQueryLimit(dslQueryLimitDTOList));
    }

    @Override
    public Result<Boolean> changeDslTemplateStatus(Integer appId, String dslTemplateMd5) {
        if (StringUtils.isEmpty(dslTemplateMd5)) {
            return Result.build(true);
        }
        return Result.buildSucc(dslTemplateService.updateDslTemplateStatus(appId, dslTemplateMd5));
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