package com.didichuxing.datachannel.arius.admin.biz.page;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.GatewayJoinQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.project.ProjectConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.GatewayJoin;
import com.didichuxing.datachannel.arius.admin.common.bean.po.dsl.DslTemplatePO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.gateway.GatewayJoinPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.DslTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.GatewayJoinVO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.QueryDiagnosisTabNameEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.project.ProjectConfigService;
import com.didichuxing.datachannel.arius.admin.metadata.service.GatewayJoinLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class GatewayJoinPageSearchHandle extends AbstractPageSearchHandle<GatewayJoinQueryDTO, GatewayJoinVO>{
    @Autowired
    private GatewayJoinLogService gatewayJoinLogService;
    @Autowired
    private ProjectConfigService projectConfigService;

    private static final Long             QUERY_COUNT_THRESHOLD = 10000L;

    @Override
    protected Result<Boolean> checkCondition(GatewayJoinQueryDTO condition, Integer projectId) {
        String queryIndex = condition.getQueryIndex();
        if (!AriusObjUtils.isBlack(queryIndex) && (queryIndex.startsWith("*") || queryIndex.startsWith("?"))) {
            return Result.buildParamIllegal("查询索引名称不允许带类似*, ?等通配符");
        }
        // 只允许查询前10000条数据
        long startNum = (condition.getPage() - 1) * condition.getSize();
        if(startNum >= QUERY_COUNT_THRESHOLD) {
            return Result.buildParamIllegal(String.format("查询条数不能超过%d条", QUERY_COUNT_THRESHOLD));
        }
        return Result.buildSucc(true);
    }

    @Override
    protected void initCondition(GatewayJoinQueryDTO condition, Integer projectId) {
        //do nothing
    }

    @Override
    protected PaginationResult<GatewayJoinVO> buildPageData(GatewayJoinQueryDTO condition, Integer projectId) {
        Tuple<Long, List<GatewayJoinPO>> tuple = null;
        try {
            if (QueryDiagnosisTabNameEnum.SLOW_QUERY.getTabName().equals(condition.getTabName())) {
                ProjectConfig projectConfig = projectConfigService.getProjectConfig(projectId);
                if(AriusObjUtils.isNull(projectConfig)){
                    return PaginationResult.buildFail("项目配置不存在");
                }
                Integer slowQueryTime = projectConfig.getSlowQueryTimes();
                tuple = gatewayJoinLogService.getGatewayJoinSlowQueryLogPage(projectId, condition, slowQueryTime);
            } else {
                tuple = gatewayJoinLogService.getGatewayJoinErrorLogPage(projectId, condition);
            }
        } catch (ESOperateException e) {
            return PaginationResult.buildFail(e.getMessage());
        }

        if (tuple == null) {
            return PaginationResult.buildSucc(new ArrayList<>(), 0L, condition.getPage(), condition.getSize());
        }
        List<GatewayJoinVO> gatewayJoinVOList = ConvertUtil.list2List(tuple.v2(), GatewayJoinVO.class);
        return PaginationResult.buildSucc(gatewayJoinVOList, tuple.v1(), condition.getPage(), condition.getSize());
    }
}
