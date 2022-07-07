package com.didichuxing.datachannel.arius.admin.rest.controller.v2.console.cluster;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_CONSOLE;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterLogicVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.PluginVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.ecm.ESClusterNodeSepcVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ConsoleTemplateVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(V2_CONSOLE + "/cluster")
@Api(tags = "Console-用户侧集群接口(REST)")
public class ConsoleClusterController {

    //@Autowired
    //private ClusterLogicService clusterLogicService;
    //
    //@Autowired
    //private ClusterLogicManager     clusterLogicManager;

    @GetMapping("/list")
    @ResponseBody
    @ApiOperation(value = "获取APP拥有的集群列表【三方接口】",tags = "【三方接口】" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "projectId", value = "应用ID", required = true) })
    @Deprecated
    public Result<List<ClusterLogicVO>> getAppLogicClusters(@RequestParam("projectId") Integer projectId) {
        return Result.buildFail("接口已经下线：迁移到v3");
        //return clusterLogicManager.getProjectLogicClusters(projectId);
    }

    @GetMapping("/listAll")
    @ResponseBody
    @ApiOperation(value = "获取平台所有的集群列表【三方接口】",tags = "【三方接口】" )
    @Deprecated
    public Result<List<ClusterLogicVO>> getDataCenterLogicClusters(@RequestParam(value = "projectId",required = false) Integer projectId) {
         return Result.buildFail("接口已经下线：迁移到v3");
        //return clusterLogicManager.getLogicClustersByProjectId(projectId);
    }

    @GetMapping("/get")
    @ResponseBody
    @ApiOperation(value = "获取集群详情" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "clusterId", value = "集群ID", required = true),
                         @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "projectId", value =
                                 "PROJECT ID", required = true) })
    @Deprecated
    public Result<ClusterLogicVO> getAppLogicClusters(@RequestParam("clusterId") Long clusterId,
                                                        @RequestParam("projectId") Integer projectId) {
         return Result.buildFail("接口已经下线：迁移到v3");
        //return clusterLogicManager.getProjectLogicClusters(clusterId, projectId);
    }

    @GetMapping("/logicTemplates")
    @ResponseBody
    @ApiOperation(value = "获取逻辑集群所有逻辑模板列表" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "clusterId", value = "逻辑集群ID", required = true)
                         })
    public Result<List<ConsoleTemplateVO>> getClusterLogicTemplates(HttpServletRequest request,
                                                                    @RequestParam(value = "clusterId") Long clusterId) {
         return Result.buildFail("接口已经下线：迁移到v3");
        //return clusterLogicManager.getClusterLogicTemplates(request, clusterId);
    }

    @GetMapping("machinespec/list")
    @ResponseBody
    @ApiOperation(value = "获取当前集群支持的套餐列表【三方接口】",tags = "【三方接口】" )
    @Deprecated
    public Result<List<ESClusterNodeSepcVO>> listMachineSpec() {
         return Result.buildFail("接口已经下线：迁移到v3");
        //return clusterLogicManager.listMachineSpec();
    }

    @GetMapping("/plugins")
    @ResponseBody
    @ApiOperation(value = "获取逻辑集群插件列表(用户侧获取)" )
    public Result<List<PluginVO>> pluginList(Long clusterId) {
         return Result.buildFail("接口已经下线：迁移到v3");
        //return Result.buildSucc(
        //    ConvertUtil.list2List(clusterLogicService.getClusterLogicPlugins(clusterId), PluginVO.class));
    }
}