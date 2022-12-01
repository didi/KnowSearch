package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.cluster.config;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ESClusterConfigManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.ecm.ESConfigVO;
import com.didiglobal.knowframework.security.util.HttpRequestUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lyn
 * @date 2020-12-30
 */
@RestController
@RequestMapping({ V3 + "/cluster/phy/config-file" })
@Api(tags = "ES集群Config接口(REST)")
public class ESClusterConfigController {

    @Autowired
    private ESClusterConfigManager esClusterConfigManager;

    @GetMapping("")
    @ResponseBody
    @ApiOperation(value = "获取ES集群配置列表")
    @ApiImplicitParam(paramType = "query", dataType = "String", name = "clusterId", value = "物理集群ID", required = true)
    public Result<List<ESConfigVO>> gainEsClusterConfigs(@RequestParam("clusterId") Long clusterId) {

        return esClusterConfigManager.gainEsClusterConfigs(clusterId);
    }

    @GetMapping("/{configId}")
    @ResponseBody
    @ApiOperation(value = "获取ES集群配置")
    public Result<ESConfigVO> gainEsClusterConfig(@PathVariable Long configId) {
        return esClusterConfigManager.getEsClusterConfigById(configId);
    }

    @GetMapping("/roles/{clusterId}")
    @ResponseBody
    @ApiOperation(value = "获取可操作配置文件的ES集群角色")
    public Result<Set<String>> gainEsClusterRoles(@PathVariable Long clusterId) {

        return esClusterConfigManager.gainEsClusterRoles(clusterId);

    }

    @GetMapping("/template/{type}")
    @ResponseBody
    @ApiOperation(value = "获取ES集群模板配置")
    public Result<ESConfigVO> gainEsClusterTemplateConfig(@PathVariable String type) {

        return esClusterConfigManager.getEsClusterTemplateConfig(type);
    }

    @PutMapping("")
    @ResponseBody
    @ApiOperation(value = "更新ES集群配置描述")
    public Result<Void> editEsClusterConfigDesc(HttpServletRequest request, @RequestBody ESConfigDTO param) {
        return esClusterConfigManager.editConfigDesc(param, HttpRequestUtil.getOperator(request),
            HttpRequestUtil.getProjectId(request));

    }
}