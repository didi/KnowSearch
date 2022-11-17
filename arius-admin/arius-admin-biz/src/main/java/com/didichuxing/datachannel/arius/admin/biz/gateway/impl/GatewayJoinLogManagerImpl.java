package com.didichuxing.datachannel.arius.admin.biz.gateway.impl;

import com.didichuxing.datachannel.arius.admin.biz.dsl.impl.DslTemplateManagerImpl;
import com.didichuxing.datachannel.arius.admin.biz.gateway.GatewayJoinLogManager;
import com.didichuxing.datachannel.arius.admin.biz.page.DslTemplatePageSearchHandle;
import com.didichuxing.datachannel.arius.admin.biz.page.GatewayJoinPageSearchHandle;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.GatewayJoinQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.GatewayJoin;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.GatewayJoinVO;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.service.project.ProjectConfigService;
import com.didichuxing.datachannel.arius.admin.metadata.service.GatewayJoinLogService;
import java.util.ArrayList;
import java.util.List;

import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import static com.didichuxing.datachannel.arius.admin.common.constant.PageSearchHandleTypeEnum.DSL_TEMPLATE;
import static com.didichuxing.datachannel.arius.admin.common.constant.PageSearchHandleTypeEnum.GATEWAY_JOIN;

@Component
public class GatewayJoinLogManagerImpl implements GatewayJoinLogManager {

    private static final ILog LOGGER = LogFactory.getLog(GatewayJoinLogManagerImpl.class);
    @Autowired
    private GatewayJoinLogService gatewayJoinLogService;
    @Autowired
    private ProjectConfigService projectConfigService;
    @Autowired
    private HandleFactory handleFactory;
    @Override
    public Result<List<GatewayJoinVO>> getGatewayErrorList(Long projectId, Long startDate, Long endDate) {
        return Result.buildSucc(ConvertUtil.list2List(
            gatewayJoinLogService.getGatewayErrorList(projectId, startDate, endDate).getData(), GatewayJoinVO.class));
    }

    @Override
    public Result<List<GatewayJoinVO>> getGatewaySlowList(Long projectId, Long startDate, Long endDate) {
        return Result.buildSucc(ConvertUtil.list2List(
            gatewayJoinLogService.getGatewaySlowList(projectId, startDate, endDate).getData(), GatewayJoinVO.class));
    }

    @Override
    public Result<Long> getSearchCountByProjectId(String dataCenter, Long projectId, Long startDate, Long endDate) {
        return gatewayJoinLogService.getSearchCountByProjectId(projectId, startDate, endDate);
    }

    @Override
    public Result<String> getDSLByProjectIdAndIndexName(Integer projectId, String indexName) {
    
        return Result.buildSucc(gatewayJoinLogService.getOneDSLByProjectIdAndIndexName(projectId, indexName));
    }

    @Override
    public PaginationResult<GatewayJoinVO> getGatewayJoinPage(Integer projectId, GatewayJoinQueryDTO queryDTO) throws NotFindSubclassException {
        BaseHandle baseHandle = handleFactory.getByHandlerNamePer(GATEWAY_JOIN.getPageSearchType());
        if (baseHandle instanceof GatewayJoinPageSearchHandle) {
            GatewayJoinPageSearchHandle handle = (GatewayJoinPageSearchHandle) baseHandle;
            return handle.doPage(queryDTO, projectId);
        }

        LOGGER.warn(
                "class=GatewayJoinLogManagerImpl||method=getGatewayJoinPage||msg=failed to get the GatewayJoinPageSearchHandle");

        return PaginationResult.buildFail("分页获取查询诊断信息失败");
    }
}