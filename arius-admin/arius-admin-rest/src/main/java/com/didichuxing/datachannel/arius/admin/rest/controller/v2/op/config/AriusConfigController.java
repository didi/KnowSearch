package com.didichuxing.datachannel.arius.admin.rest.controller.v2.op.config;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_OP;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.config.AriusConfigInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.config.AriusConfigInfoVO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * 
 *
 * @author d06679
 * @date 2017/10/23
 */
@RestController
@RequestMapping(V2_OP + "/config")
@Api(tags = "运维配置接口(REST)")
public class AriusConfigController {

    @Autowired
    private AriusConfigInfoService ariusConfigInfoService;

    @PostMapping("/list")
    @ResponseBody
    @ApiOperation(value = "获取配置列表接口", notes = "")
    public Result<List<AriusConfigInfoVO>> list(@RequestBody AriusConfigInfoDTO param) {
        return Result
            .buildSucc(ConvertUtil.list2List(ariusConfigInfoService.queryByCondt(param), AriusConfigInfoVO.class));
    }

    @GetMapping("/get")
    @ResponseBody
    @ApiOperation(value = "获取指定配置接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "configId", value = "配置ID", required = true) })
    public Result<AriusConfigInfoVO> get(@RequestParam("configId") Integer configId) {
        return Result
            .buildSucc(ConvertUtil.obj2Obj(ariusConfigInfoService.getConfigById(configId), AriusConfigInfoVO.class));
    }

    @PostMapping("/switch")
    @ResponseBody
    @ApiOperation(value = "使能配置接口", notes = "")
    public Result<Void> switchConfig(HttpServletRequest request, @RequestBody AriusConfigInfoDTO param) {
        return ariusConfigInfoService.switchConfig(param.getId(), param.getStatus(),
            HttpRequestUtils.getOperator(request));
    }

    @DeleteMapping("/del")
    @ResponseBody
    @ApiOperation(value = "删除配置接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "id", value = "配置ID", required = true) })
    public Result<Void> delete(HttpServletRequest request, @RequestParam(value = "id") Integer id) {
        return ariusConfigInfoService.delConfig(id, HttpRequestUtils.getOperator(request));
    }

    @PutMapping("/add")
    @ResponseBody
    @ApiOperation(value = "新建配置接口", notes = "")
    public Result<Integer> add(HttpServletRequest request, @RequestBody AriusConfigInfoDTO param) {
        return ariusConfigInfoService.addConfig(param, HttpRequestUtils.getOperator(request));
    }

    @PostMapping("/edit")
    @ResponseBody
    @ApiOperation(value = "编辑配置接口", notes = "")
    public Result<Void> edit(HttpServletRequest request, @RequestBody AriusConfigInfoDTO param) {
        return ariusConfigInfoService.editConfig(param, HttpRequestUtils.getOperator(request));
    }

    // *************************************** RESTFUL  API ***************************************

    @GetMapping("")
    @ResponseBody
    @ApiOperation(value = "获取配置列表接口", notes = "")
    public Result<List<AriusConfigInfoVO>> getAriusConfigs(@RequestParam AriusConfigInfoDTO param) {
        return Result.buildSucc(ConvertUtil.list2List(
                ariusConfigInfoService.queryByCondt(param),
                AriusConfigInfoVO.class));
    }
}
