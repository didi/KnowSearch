package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.cluster.plugins;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;
import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPhyManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPluginsManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.PluginDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.PluginVO;
import com.didichuxing.datachannel.arius.admin.common.util.ProjectUtils;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
 * @author linyunan
 * @date 2021-03-15
 */
@RestController
@RequestMapping({ V3_OP + "/cluster/phy/plugins", V3 + "/cluster/phy/plugins" })
@Api(tags = "ES物理集群插件接口")
public class PhyClusterPluginsController {

    @Autowired
    private ClusterPhyManager clusterPhyManager;

    @Autowired
    private ClusterPluginsManager clusterPluginsManager;

    @GetMapping("/{cluster}")
    @ResponseBody
    @ApiOperation(value = "获取ES集群插件列表")
    public Result<List<PluginVO>> pluginList(@PathVariable(value = "cluster") String cluster) {
        return clusterPhyManager.listPlugins(cluster);
    }

    @PostMapping("")
    @ResponseBody
    @ApiOperation(value = "上传插件")
    public Result<Long> add(HttpServletRequest request,PluginDTO param) {
        final Result<Void> result = ProjectUtils.checkProjectCorrectly(i -> i, HttpRequestUtil.getProjectId(request),
                HttpRequestUtil.getProjectId(request));
        if (result.failed()){
            return Result.buildFail(result.getMessage());
        }
        return clusterPluginsManager.addPlugins(param);
    }

    @DeleteMapping("/{pluginId}")
    @ResponseBody
    @ApiOperation(value = "删除ES本地插件信息")
    public Result<Long> deleteEsClusterConfig(HttpServletRequest request, @PathVariable(value = "pluginId") Long pluginId) {
          final Result<Void> result = ProjectUtils.checkProjectCorrectly(i -> i, HttpRequestUtil.getProjectId(request),
                HttpRequestUtil.getProjectId(request));
        if (result.failed()){
            return Result.buildFail(result.getMessage());
        }
        return clusterPluginsManager.deletePluginById(pluginId, HttpRequestUtil.getOperator(request));
    }

    @PutMapping("")
    @ResponseBody
    @ApiOperation(value = "编辑本地插件描述")
    public Result<Void> edit(HttpServletRequest request, @RequestBody PluginDTO pluginDTO) {
          final Result<Void> result = ProjectUtils.checkProjectCorrectly(i -> i, HttpRequestUtil.getProjectId(request),
                HttpRequestUtil.getProjectId(request));
        if (result.failed()){
            return Result.buildFail(result.getMessage());
        }
        return clusterPluginsManager.editPluginDesc(pluginDTO, HttpRequestUtil.getOperator(request));
    }
}