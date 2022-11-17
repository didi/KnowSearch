package com.didichuxing.datachannel.arius.admin.biz.page;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.oprecord.OperateRecordDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.operaterecord.OperateRecordVO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.knowframework.security.common.vo.project.ProjectBriefVO;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
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
public class OperateRecordPageSearchHandle extends AbstractPageSearchHandle<OperateRecordDTO, OperateRecordVO> {
    private static final ILog    LOGGER = LogFactory.getLog(OperateRecordPageSearchHandle.class);

    @Autowired
    private OperateRecordService operateRecordService;

    @Override
    protected Result<Boolean> checkCondition(OperateRecordDTO condition, Integer projectId) {

        return Result.buildSucc();
    }

    @Override
    protected void initCondition(OperateRecordDTO condition, Integer projectId) {
        if (StringUtils.isBlank(condition.getProjectName())) {
            if (!AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
                final ProjectBriefVO projectBriefVO = projectService.getProjectBriefByProjectId(projectId);
                condition.setProjectName(projectBriefVO.getProjectName());
            }
        }
        condition.setFrom((condition.getPage() - 1) * condition.getSize());

        // Do nothing
    }

    @Override
    protected PaginationResult<OperateRecordVO> buildPageData(OperateRecordDTO pageDTO, Integer projectId) {

        final Tuple<Long, List<OperateRecordVO>> tuple = operateRecordService
            .pagingGetOperateRecordByCondition(pageDTO);
        if (tuple == null) {
            return PaginationResult.buildSucc(Collections.emptyList(), 0L, pageDTO.getPage(), pageDTO.getSize());
        }
        return PaginationResult.buildSucc(tuple.getV2(), tuple.v1(), pageDTO.getPage(), pageDTO.getSize());
    }

}