package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.cluster.plugins;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPhyManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPluginManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.PluginDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.PluginVO;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didiglobal.knowframework.security.util.HttpRequestUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author linyunan
 * @date 2021-03-15
 */
@RestController
@RequestMapping({ V3 + "/cluster/phy/plugins" })
@Api(tags = "ES物理集群插件接口")
public class PhyClusterPluginController {

    @Autowired
    private ClusterPhyManager    clusterPhyManager;

    @Autowired
    private ClusterPluginManager clusterPluginManager;

    @GetMapping("/{cluster}")
    @ResponseBody
    @ApiOperation(value = "获取ES集群插件列表")
    public Result<List<PluginVO>> pluginList(@PathVariable(value = "cluster") String cluster) {
        return clusterPhyManager.listPlugins(cluster);
    }

    @PostMapping("")
    @ResponseBody
    @ApiOperation(value = "上传插件")
    public Result<Long> add(HttpServletRequest request, PluginDTO param) throws NotFindSubclassException {

        return clusterPluginManager.addPlugins(param, HttpRequestUtil.getProjectId(request));
    }

    @DeleteMapping("/{pluginId}")
    @ResponseBody
    @ApiOperation(value = "删除ES本地插件信息")
    public Result<Long> deleteEsClusterConfig(HttpServletRequest request,
                                              @PathVariable(value = "pluginId") Long pluginId) throws NotFindSubclassException {

        return clusterPluginManager.deletePluginById(pluginId, HttpRequestUtil.getOperator(request),
            HttpRequestUtil.getProjectId(request));
    }

    @PutMapping("")
    @ResponseBody
    @ApiOperation(value = "编辑本地插件描述")
    public Result<Void> edit(HttpServletRequest request, @RequestBody PluginDTO pluginDTO) {

        return clusterPluginManager.editPluginDesc(pluginDTO, HttpRequestUtil.getOperator(request),
            HttpRequestUtil.getProjectId(request));
    }
}