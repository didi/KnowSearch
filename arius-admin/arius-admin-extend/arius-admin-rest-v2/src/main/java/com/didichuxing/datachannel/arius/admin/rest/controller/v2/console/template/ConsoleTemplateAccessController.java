package com.didichuxing.datachannel.arius.admin.rest.controller.v2.console.template;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_CONSOLE;
import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

import com.didichuxing.datachannel.arius.admin.biz.template.TemplatePhyStaticsManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ProjectIdTemplateAccessCountVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(V2_CONSOLE + "/template/access")
@Api(tags = "Console-用户侧索引模板访问统计信息接口(REST)：见："+V3_OP)
@Deprecated
public class ConsoleTemplateAccessController extends BaseConsoleTemplateController {

    @Autowired
    private TemplatePhyStaticsManager templatePhyStaticsManager;

    /**
     * 根据模板Id获取[startDate, endDate]的projectId访问统计信息
     *
     * @param logicTemplateId 逻辑索引模板ID
     * @param startDate 开始时刻
     * @param endDate 结束时刻
     * @return
     */
    @GetMapping(path = "/appInfosByDateRange.do")
    @ApiOperation(value = "根据模板Id获取[startDate, endDate(毫秒)]的projectId访问统计信息【三方接口】",tags = "【三方接口】" )
    public Result<List<ProjectIdTemplateAccessCountVO>> getAccessAppInfos(
            @ApiParam(name="templateId", value="逻辑索引模板ID", required = true)
            @RequestParam(value = "templateId")    int logicTemplateId,

            @ApiParam(name="startDate", value="开始时刻", required = true)
            @RequestParam(value = "startDate")      Long startDate,

            @ApiParam(name="endDate", value="结束时刻", required = true)
            @RequestParam(value = "endDate")        Long endDate){
        return templatePhyStaticsManager.getAccessAppInfos(logicTemplateId, startDate, endDate);
    }

    /**
     * 根据模板名称获取最近days天的projectId访问统计信息
     *
     * @param logicTemplateId 逻辑索引模板ID
     * @param days 最近多少天
     * @return
     */
    @GetMapping(path = "/project-ids.do")
    @ApiOperation(value = "根据模板名称获取最近days天的projectId访问统计信息【三方接口】",tags = "【三方接口】" )
    public Result<Map<Integer, Long>> getAccessProjectIds(
            @ApiParam(name="templateId", value="逻辑索引模板ID", required = true)
            @RequestParam(value = "templateId")    int logicTemplateId,

            @ApiParam(name="days", value="最近多少天", required = true)
            @RequestParam(value = "days")          int days){
        return templatePhyStaticsManager.getAccessStatsInfoByTemplateIdAndDays(logicTemplateId, days);
    }
}