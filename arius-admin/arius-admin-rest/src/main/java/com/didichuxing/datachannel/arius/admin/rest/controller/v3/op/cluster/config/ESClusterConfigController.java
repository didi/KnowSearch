package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.cluster.config;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.ecm.ESConfigVO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.RoleCluster;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.esconfig.ESConfig;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESClusterConfigService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.RoleClusterService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author lyn
 * @date 2020-12-30
 */
@RestController
@RequestMapping(V3_OP + "/cluster/config")
@Api(tags = "ES集群Config接口(REST)")
public class ESClusterConfigController {

    @Autowired
    private ESClusterConfigService esClusterConfigService;

    @Autowired
    private RoleClusterService roleClusterService;

    @GetMapping("/{clusterId}/list")
    @ResponseBody
    @ApiOperation(value = "获取ES集群配置列表")
    public Result<List<ESConfigVO>> gainEsClusterConfigs(@PathVariable Long clusterId) {
        Result<List<ESConfig>> listResult = esClusterConfigService.listEsClusterConfigByClusterId(clusterId);
        return Result.buildSucc(ConvertUtil.list2List(listResult.getData(), ESConfigVO.class));
    }

    @GetMapping("/{configId}")
    @ResponseBody
    @ApiOperation(value = "获取ES集群配置")
    public Result<ESConfigVO> gainEsClusterConfig(@PathVariable Long configId) {
        return Result.buildSucc(ConvertUtil.obj2Obj(esClusterConfigService.getEsClusterConfigById(configId), ESConfigVO.class));
    }

    @GetMapping("/{clusterId}/roles")
    @ResponseBody
    @ApiOperation(value = "获取可操作配置文件的ES集群角色")
    public Result<Set<String>> gainEsClusterRoles(@PathVariable Long clusterId) {
        List<RoleCluster> roleClusters = roleClusterService.getAllRoleClusterByClusterId(clusterId.intValue());
        return Result.buildSucc(
				roleClusters.stream().filter(Objects::nonNull).map(RoleCluster::getRole).collect(Collectors.toSet()));
    }

    @GetMapping("/{type}/template")
    @ResponseBody
    @ApiOperation(value = "获取ES集群模板配置")
    public Result<ESConfigVO> gainEsClusterTemplateConfig(@PathVariable String type) {
        return Result.buildSucc(ConvertUtil.obj2Obj(esClusterConfigService.getEsClusterTemplateConfig(type), ESConfigVO.class));
    }

    @PutMapping("")
    @ResponseBody
    @ApiOperation(value = "更新ES集群配置描述")
    public Result<Void> editEsClusterConfigDesc(HttpServletRequest request, @RequestBody ESConfigDTO param) {
        return esClusterConfigService.editConfigDesc(param, HttpRequestUtils.getOperator(request));
    }
}
