package com.didichuxing.datachannel.arius.admin.rest.controller.v2.console.template;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_CONSOLE;
import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

import com.didichuxing.datachannel.arius.admin.biz.template.TemplatePhyStaticsManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESIndexStats;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.TemplateStatsInfoVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@NoArgsConstructor
@RestController()
@RequestMapping(V2_CONSOLE + "/template/statis")
@Api(tags = "Console-用户侧索引模板统计信息接口(REST)：见："+V3_OP)
@Deprecated
public class ConsoleTemplateStatisController extends BaseConsoleTemplateController {

    @Autowired
    private TemplatePhyStaticsManager templatePhyStaticsManager;

    /**
     * 根据逻辑模板id获取模板的monitor统计信息
     *
     * @param logicTemplateId 模板id
     * @param startDate       毫秒
     * @param endDate         毫秒
     * @return
     */
    @GetMapping(path = "/query.do")
    @ApiOperation(value = "获取索引模板的统计信息【三方接口】",tags = "【三方接口】" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "logicTemplateId", value = "模板id", required = true),
            @ApiImplicitParam(paramType = "query", dataType = "Long", name = "startDate", value = "查询开始时间，毫秒时间戳", required = true),
            @ApiImplicitParam(paramType = "query", dataType = "Long", name = "endDate", value = "查询结束时间，毫秒时间戳", required = true) })
    public Result<List<ESIndexStats>> getIndexStatis(@RequestParam(value = "templateId") Long logicTemplateId,
                                                     @RequestParam(value = "startDate") Long startDate,
                                                     @RequestParam(value = "endDate") Long endDate) {
        //return templatePhyStaticsManager.getIndexStatics(logicTemplateId, startDate, endDate);
         return Result.buildFail("接口已经下线：迁移到v3");
    }

    /**
     * 根据模板id获取模板的基本统计信息
     *
     * @param logicTemplateId 模板id
     * @return
     */
    @GetMapping(path = "/statisInfo.do")
    @ApiOperation(value = "根据模板id，查询模板的基本统计信息【三方接口】",tags = "【三方接口】" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "templateId", value = "模板id", required = true) })
    public Result<TemplateStatsInfoVO> getTemplateBaseStatisInfo(@RequestParam(value = "templateId") Long logicTemplateId) {
        //return templatePhyStaticsManager.getTemplateBaseStatisticalInfoByLogicTemplateId(logicTemplateId);
         return Result.buildFail("接口已经下线：迁移到v3");
    }
}