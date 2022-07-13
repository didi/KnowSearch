package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.config;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.COMMA;
import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.config.AriusConfigInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.config.AriusConfigInfoVO;
import com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 *
 * @author d06679
 * @date 2017/10/23
 */
@RestController
@RequestMapping(V3 + "/config")
@Api(tags = "运维配置接口(REST)")
public class AriusConfigV3Controller {

    @Autowired
    private AriusConfigInfoService ariusConfigInfoService;

    @PostMapping("/list")
    @ResponseBody
    @ApiOperation(value = "获取配置列表接口")
    public Result<List<AriusConfigInfoVO>> listConfig(@RequestBody AriusConfigInfoDTO param) {
        return Result
            .buildSucc(ConvertUtil.list2List(ariusConfigInfoService.queryByCondition(param), AriusConfigInfoVO.class));
    }

    @PostMapping("/switch")
    @ResponseBody
    @ApiOperation(value = "使能配置接口")
    public Result<Void> switchConfig(HttpServletRequest request, @RequestBody AriusConfigInfoDTO param) {
        return ariusConfigInfoService.switchConfig(param.getId(), param.getStatus(),
            HttpRequestUtil.getOperator(request));
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    @ApiOperation(value = "删除配置接口")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "id", value = "配置ID", required = true) })
    public Result<Void> deleteConfig(HttpServletRequest request, @PathVariable Integer id) {
        return ariusConfigInfoService.delConfig(id, HttpRequestUtil.getOperator(request));
    }

    @PostMapping("/add")
    @ResponseBody
    @ApiOperation(value = "新建配置接口")
    public Result<Integer> addConfig(HttpServletRequest request, @RequestBody AriusConfigInfoDTO param) {
        return ariusConfigInfoService.addConfig(param, HttpRequestUtil.getOperator(request));
    }

    @PutMapping()
    @ResponseBody
    @ApiOperation(value = "编辑配置接口")
    public Result<Void> editConfig(HttpServletRequest request, @RequestBody AriusConfigInfoDTO param) {
        return ariusConfigInfoService.editConfig(param, HttpRequestUtil.getOperator(request));
    }

    @GetMapping("/cluster-version")
    @ResponseBody
    @ApiOperation(value = "获取集群版本列表")
    public Result<List<String>> listClusterVersion() {
        return Result.buildSucc(new ArrayList<>(ariusConfigInfoService.stringSettingSplit2Set(
            AriusConfigConstant.ARIUS_COMMON_GROUP, AriusConfigConstant.CLUSTER_PACKAGE_VERSION_LIST,
            AriusConfigConstant.CLUSTER_PACKAGE_VERSION_LIST_DEFAULT_VALUE, COMMA)));
    }

    @GetMapping("/cluster-data-center")
    @ResponseBody
    @ApiOperation(value = "获取数据中心列表")
    public Result<List<String>> listClusterDataCenter() {
        return Result.buildSucc(new ArrayList<>(ariusConfigInfoService.stringSettingSplit2Set(
            AriusConfigConstant.ARIUS_COMMON_GROUP, AriusConfigConstant.CLUSTER_DATA_CENTER_LIST,
            AriusConfigConstant.CLUSTER_DATA_CENTER_LIST_DEFAULT_VALUE, COMMA)));
    }

    @GetMapping("/cluster-resource-type")
    @ResponseBody
    @ApiOperation(value = "获取集群资源列表")
    public Result<List<String>> listClusterResourceType() {
        return Result.buildSucc(new ArrayList<>(ariusConfigInfoService.stringSettingSplit2Set(
            AriusConfigConstant.ARIUS_COMMON_GROUP, AriusConfigConstant.CLUSTER_RESOURCE_TYPE_LIST,
            AriusConfigConstant.CLUSTER_RESOURCE_TYPE_LIST_DEFAULT_VALUE, COMMA)));
    }

    @GetMapping("/cluster-node-count")
    @ResponseBody
    @ApiOperation(value = "获取集群节点数")
    public Result<List<String>> listClusterNodeCount() {
        return Result.buildSucc(new ArrayList<>(ariusConfigInfoService.stringSettingSplit2Set(
            AriusConfigConstant.ARIUS_COMMON_GROUP, AriusConfigConstant.CLUSTER_NODE_COUNT_LIST,
            AriusConfigConstant.CLUSTER_NODE_COUNT_LIST_DEFAULT_VALUE, COMMA)));
    }

    @GetMapping("/time-format")
    @ResponseBody
    @ApiOperation(value = "获取时间格式列表")
    public Result<List<String>> listTimeFormat() {
        return Result.buildSucc(new ArrayList<>(ariusConfigInfoService.stringSettingSplit2Set(
            AriusConfigConstant.ARIUS_COMMON_GROUP, AriusConfigConstant.LOGIC_TEMPLATE_TIME_FORMAT_LIST,
            AriusConfigConstant.LOGIC_TEMPLATE_TIME_FORMAT_LIST_DEFAULT_VALUE, COMMA)));
    }

    @GetMapping("/node-specification")
    @ResponseBody
    @ApiOperation(value = "获取节点规格列表")
    public Result<List<String>> listClusterNodeSpecification() {
        return Result.buildSucc(new ArrayList<>(ariusConfigInfoService.stringSettingSplit2Set(
            AriusConfigConstant.ARIUS_COMMON_GROUP, AriusConfigConstant.CLUSTER_NODE_SPECIFICATION_LIST,
            AriusConfigConstant.CLUSTER_NODE_SPECIFICATION_LIST_DEFAULT_VALUE, COMMA)));
    }
}