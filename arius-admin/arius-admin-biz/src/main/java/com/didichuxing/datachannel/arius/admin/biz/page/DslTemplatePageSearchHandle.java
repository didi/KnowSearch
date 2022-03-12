package com.didichuxing.datachannel.arius.admin.biz.page;

import com.didichuxing.datachannel.arius.admin.client.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.PageDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.dsl.template.DslTemplateConditionDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.DslTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.po.dsl.DslTemplatePO;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.metadata.service.DslTemplateService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cjm
 */
@Component
public class DslTemplatePageSearchHandle extends BasePageSearchHandle<DslTemplateVO> {

    private static final ILog LOGGER = LogFactory.getLog(DslTemplatePageSearchHandle.class);

    @Autowired
    private AppService appService;

    @Autowired
    private DslTemplateService dslTemplateService;

    @Override
    protected Result<Boolean> validCheckForAppId(Integer appId) {
        if (!appService.isAppExists(appId)) {
            return Result.buildParamIllegal("项目不存在");
        }
        return Result.buildSucc(true);
    }

    @Override
    protected Result<Boolean> validCheckForCondition(PageDTO pageDTO, Integer appId) {
        if (pageDTO instanceof DslTemplateConditionDTO) {
            DslTemplateConditionDTO dslTemplateConditionDTO = (DslTemplateConditionDTO) pageDTO;
            String queryIndex = dslTemplateConditionDTO.getQueryIndex();
            if (!AriusObjUtils.isBlack(queryIndex) && (queryIndex.startsWith("*") || queryIndex.startsWith("?"))) {
                return Result.buildParamIllegal("查询索引名称不允许带类似*, ?等通配符");
            }

            return Result.buildSucc(true);
        }

        LOGGER.error("class=DslTemplatePageSearchHandle||method=validCheckForCondition||errMsg=failed to convert PageDTO to DslTemplateConditionDTO");

        return Result.buildFail();
    }

    @Override
    protected void init(PageDTO pageDTO) {
        // Do nothing
    }

    @Override
    protected PaginationResult<DslTemplateVO> buildWithAuthType(PageDTO pageDTO, Integer authType, Integer appId) {
        return PaginationResult.buildSucc();
    }

    @Override
    protected PaginationResult<DslTemplateVO> buildWithoutAuthType(PageDTO pageDTO, Integer appId) {
        DslTemplateConditionDTO condition = buildInitDslTemplateConditionDTO(pageDTO);
        if (null == condition) {
            LOGGER.error(
                    "class=DslTemplatePageSearchHandle||method=buildWithoutAuthType||errMsg=failed to convert PageDTO to DslTemplateConditionDTO");
            return PaginationResult.buildFail("获取DSL查询模板信息失败");
        }

        Tuple<Long, List<DslTemplatePO>> tuple = dslTemplateService.getDslTemplatePage(appId, condition);
        if (tuple == null) {
            return PaginationResult.buildSucc( new ArrayList<>(), 0L, condition.getFrom(), condition.getSize());
        }
        List<DslTemplateVO> dslTemplateVOList = ConvertUtil.list2List(tuple.v2(), DslTemplateVO.class);
        return PaginationResult.buildSucc(dslTemplateVOList, tuple.v1(), condition.getFrom(), condition.getSize());
    }

    private DslTemplateConditionDTO buildInitDslTemplateConditionDTO(PageDTO pageDTO) {
        if (pageDTO instanceof DslTemplateConditionDTO) {
            return (DslTemplateConditionDTO) pageDTO;
        }
        return null;
    }
}
