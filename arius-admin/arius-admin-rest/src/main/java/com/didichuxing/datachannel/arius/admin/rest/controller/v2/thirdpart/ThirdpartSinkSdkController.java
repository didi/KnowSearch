package com.didichuxing.datachannel.arius.admin.rest.controller.v2.thirdpart;

import com.didichuxing.datachannel.arius.admin.biz.thardpart.SinkManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.SinkSdkAppVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.SinkSdkIDCTemplateDeployInfoVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.SinkSdkTemplateDeployInfoVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_THIRD_PART;

@RestController
@RequestMapping(V2_THIRD_PART + "/flink")
@Api(tags = "Flink依赖接口(REST)")
public class ThirdpartSinkSdkController {

    @Autowired
    private SinkManager sinkManager;

    @GetMapping("/getApp")
    @ResponseBody
    @ApiOperation(value = "获取APP信息接口", notes = "获取权限信息、验证码因袭，请求头中需要提供ticket")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "header", dataType = "String", name = "X-ARIUS-FLINK-TICKET", value = "接口ticket", required = true),
                         @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "appId", value = "Y应用ID", required = true) })
    public Result<SinkSdkAppVO> listApp(HttpServletRequest request, Integer appId) {
        return null;
       // return sinkManager.listApp(request, appId);
    }

    @GetMapping("/getDeployInfo")
    @ResponseBody
    @ApiOperation(value = "获取模板信息", notes = "主从结构组织")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "String", name = "templateName", value = "模板名字", required = true) })
    public Result<SinkSdkTemplateDeployInfoVO> listDeployInfo(@RequestParam(value = "templateName") String templateName) {
        return null;
        //return sinkManager.listDeployInfo(templateName);
    }

    @GetMapping("/getIDCDeployInfo")
    @ResponseBody
    @ApiOperation(value = "获取模板信息", notes = "主从结构组织")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "String", name = "templateName", value = "模板名字", required = true) })
    public Result<SinkSdkIDCTemplateDeployInfoVO> getIDCDeployInfo(@RequestParam(value = "templateName") String templateName) {
        //return sinkManager.getIDCDeployInfo(templateName);
        return null;
    }
}
