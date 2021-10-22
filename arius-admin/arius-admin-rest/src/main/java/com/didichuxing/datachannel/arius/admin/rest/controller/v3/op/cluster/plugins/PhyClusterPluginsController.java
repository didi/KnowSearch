package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.cluster.plugins;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESPluginDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ESPluginVO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESPluginService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

/**
 * @author linyunan
 * @date 2021-03-15
 */
@RestController
@RequestMapping({V3_OP + "/cluster/phy/plugins"})
@Api(tags = "es物理集群插件接口")
public class PhyClusterPluginsController {

    private final ESClusterPhyService esClusterPhyService;
    private final ESPluginService   esPluginService;

    @Autowired
    public PhyClusterPluginsController(ESClusterPhyService esClusterPhyService,
                                       ESPluginService   esPluginService) {
        this.esClusterPhyService = esClusterPhyService;
        this.esPluginService = esPluginService;
    }

    @GetMapping("/{cluster}/get")
    @ResponseBody
    @ApiOperation(value = "获取ES集群插件列表")
    public Result<List<ESPluginVO>> pluginList(@PathVariable(value = "cluster") String cluster) {
        return Result.buildSucc(ConvertUtil.list2List(esClusterPhyService.listClusterPlugins(cluster), ESPluginVO.class));
    }

    @PostMapping("")
    @ResponseBody
    @ApiOperation(value = "上传插件")
    public Result add(List<ESPluginDTO> param) {
        return esPluginService.addESPlugins(param);
    }

    @DeleteMapping("/{pluginId}")
    @ResponseBody
    @ApiOperation(value = "删除ES本地插件信息")
    public Result deleteEsClusterConfig(HttpServletRequest request, @PathVariable(value = "pluginId") Long pluginId) {
        return esPluginService.deletePluginById(pluginId, HttpRequestUtils.getOperator(request));
    }

    @PutMapping("")
    @ResponseBody
    @ApiOperation(value = "编辑本地插件信息")
    public Result edit(HttpServletRequest request, @RequestBody ESPluginDTO esPluginDTO) {
        return esPluginService.updateESPluginDesc(esPluginDTO, HttpRequestUtils.getOperator(request));
    }
}
