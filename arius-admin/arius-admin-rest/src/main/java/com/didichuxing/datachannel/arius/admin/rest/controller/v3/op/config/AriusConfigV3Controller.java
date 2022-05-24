package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.config;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.COMMA;
import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.config.AriusConfigInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.config.AriusConfigInfoVO;
import com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant;
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
@RequestMapping(V3_OP + "/config")
@Api(tags = "运维配置接口(REST)")
public class AriusConfigV3Controller {

    @Autowired
    private AriusConfigInfoService ariusConfigInfoService;

    @PostMapping("/list")
    @ResponseBody
    @ApiOperation(value = "获取配置列表接口" )
    public Result<List<AriusConfigInfoVO>> listConfig(@RequestBody AriusConfigInfoDTO param) {
        return Result
            .buildSucc(ConvertUtil.list2List(ariusConfigInfoService.queryByCondition(param), AriusConfigInfoVO.class));
    }

    @PostMapping("/switch")
    @ResponseBody
    @ApiOperation(value = "使能配置接口" )
    public Result<Void> switchConfig(HttpServletRequest request, @RequestBody AriusConfigInfoDTO param) {
        return ariusConfigInfoService.switchConfig(param.getId(), param.getStatus(),
            HttpRequestUtils.getOperator(request));
    }

    @DeleteMapping("/del")
    @ResponseBody
    @ApiOperation(value = "删除配置接口" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "id", value = "配置ID", required = true) })
    public Result<Void> deleteConfig(HttpServletRequest request, @RequestParam(value = "id") Integer id) {
        return ariusConfigInfoService.delConfig(id, HttpRequestUtils.getOperator(request));
    }

    @PutMapping("/add")
    @ResponseBody
    @ApiOperation(value = "新建配置接口" )
    public Result<Integer> addConfig(HttpServletRequest request, @RequestBody AriusConfigInfoDTO param) {
        return ariusConfigInfoService.addConfig(param, HttpRequestUtils.getOperator(request));
    }

    @PostMapping("/edit")
    @ResponseBody
    @ApiOperation(value = "编辑配置接口" )
    public Result<Void> editConfig(HttpServletRequest request, @RequestBody AriusConfigInfoDTO param) {
        return ariusConfigInfoService.editConfig(param, HttpRequestUtils.getOperator(request));
    }

    @GetMapping("/cluster—version")
    @ResponseBody
    @ApiOperation(value = "获取集群版本列表")
    public Result<List<String>> listClusterVersion() {
        return Result.buildSucc(new ArrayList<>(ariusConfigInfoService.stringSettingSplit2Set(AriusConfigConstant.ARIUS_COMMON_GROUP,
                AriusConfigConstant.CLUSTER_PACKAGE_VERSION_LIST, AriusConfigConstant.CLUSTER_PACKAGE_VERSION_LIST_DEFAULT_VALUE, COMMA)));
    }

    @GetMapping("/cluster—data-center")
    @ResponseBody
    @ApiOperation(value = "获取数据中心列表")
    public Result<List<String>> listClusterDataCenter() {
        return Result.buildSucc(new ArrayList<>(ariusConfigInfoService.stringSettingSplit2Set(AriusConfigConstant.ARIUS_COMMON_GROUP,
                AriusConfigConstant.CLUSTER_DATA_CENTER_LIST, AriusConfigConstant.CLUSTER_DATA_CENTER_LIST_DEFAULT_VALUE, COMMA)));
    }

    @GetMapping("/cluster—resource-type")
    @ResponseBody
    @ApiOperation(value = "获取集群资源列表")
    public Result<List<String>> listClusterResourceType() {
        return Result.buildSucc(new ArrayList<>(ariusConfigInfoService.stringSettingSplit2Set(AriusConfigConstant.ARIUS_COMMON_GROUP,
                AriusConfigConstant.CLUSTER_RESOURCE_TYPE_LIST, AriusConfigConstant.CLUSTER_RESOURCE_TYPE_LIST_DEFAULT_VALUE, COMMA)));
    }


    @GetMapping("/cluster—node-count")
    @ResponseBody
    @ApiOperation(value = "获取集群节点数")
    public Result<List<String>> listClusterNodeCount() {
        return Result.buildSucc(new ArrayList<>(ariusConfigInfoService.stringSettingSplit2Set(AriusConfigConstant.ARIUS_COMMON_GROUP,
                AriusConfigConstant.CLUSTER_NODE_COUNT_LIST, AriusConfigConstant.CLUSTER_NODE_COUNT_LIST_DEFAULT_VALUE, COMMA)));
    }

    @GetMapping("/time-format")
    @ResponseBody
    @ApiOperation(value = "获取时间格式列表")
    public Result<List<String>> listTimeFormat() {
        return Result.buildSucc(new ArrayList<>(ariusConfigInfoService.stringSettingSplit2Set(AriusConfigConstant.ARIUS_COMMON_GROUP,
                AriusConfigConstant.LOGIC_TEMPLATE_TIME_FORMAT_LIST, AriusConfigConstant.LOGIC_TEMPLATE_TIME_FORMAT_LIST_DEFAULT_VALUE, COMMA)));
    }


    @GetMapping("/node—specification")
    @ResponseBody
    @ApiOperation(value = "获取节点规格列表")
    public Result<List<String>> listClusterNodeSpecification() {
        return Result.buildSucc(new ArrayList<>(ariusConfigInfoService.stringSettingSplit2Set(AriusConfigConstant.ARIUS_COMMON_GROUP,
                AriusConfigConstant.CLUSTER_NODE_SPECIFICATION_LIST, AriusConfigConstant.CLUSTER_NODE_SPECIFICATION_LIST_DEFAULT_VALUE, COMMA)));
    }
}
