package com.didichuxing.datachannel.arius.admin.rest.controller.v2.console.template;

import com.didichuxing.datachannel.arius.admin.biz.template.TemplatePhyStatisManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.AppIdTemplateAccessCountVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_CONSOLE;

@RestController
@RequestMapping(V2_CONSOLE + "/template/access")
@Api(tags = "Console-用户侧索引模板访问统计信息接口(REST)")
public class ConsoleTemplateAccessController extends BaseConsoleTemplateController {

    @Autowired
    private TemplatePhyStatisManager templatePhyStatisManager;

    /**
     * 根据模板Id获取[startDate, endDate]的appid访问统计信息
     *
     * @param logicTemplateId 逻辑索引模板ID
     * @param startDate 开始时刻
     * @param endDate 结束时刻
     * @return
     */
    @GetMapping(path = "/appInfosByDateRange.do")
    @ApiOperation(value = "根据模板Id获取[startDate, endDate(毫秒)]的appid访问统计信息【三方接口】",tags = "【三方接口】" )
    public Result<List<AppIdTemplateAccessCountVO>> getAccessAppInfos(
            @ApiParam(name="templateId", value="逻辑索引模板ID", required = true)
            @RequestParam(value = "templateId")    int logicTemplateId,

            @ApiParam(name="startDate", value="开始时刻", required = true)
            @RequestParam(value = "startDate")      Long startDate,

            @ApiParam(name="endDate", value="结束时刻", required = true)
            @RequestParam(value = "endDate")        Long endDate){
        return templatePhyStatisManager.getAccessAppInfos(logicTemplateId, startDate, endDate);
    }

    /**
     * 根据模板名称获取最近days天的appid访问统计信息
     *
     * @param logicTemplateId 逻辑索引模板ID
     * @param days 最近多少天
     * @return
     */
    @GetMapping(path = "/appids.do")
    @ApiOperation(value = "根据模板名称获取最近days天的appid访问统计信息【三方接口】",tags = "【三方接口】" )
    public Result<Map<Integer, Long>> getAccessAppids(
            @ApiParam(name="templateId", value="逻辑索引模板ID", required = true)
            @RequestParam(value = "templateId")    int logicTemplateId,

            @ApiParam(name="days", value="最近多少天", required = true)
            @RequestParam(value = "days")          int days){
        return templatePhyStatisManager.getAccessStatsInfoByTemplateIdAndDays(logicTemplateId, days);
    }
}
