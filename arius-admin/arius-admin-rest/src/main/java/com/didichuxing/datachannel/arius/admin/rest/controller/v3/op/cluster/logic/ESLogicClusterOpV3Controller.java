package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.cluster.logic;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterLogicManager;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterLogicConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESLogicClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterLogicTemplateIndexCountVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterLogicVO;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

/**
 * @author guoyoupeng_v
 * @date 2022/05/25
 */
@RestController
@RequestMapping({  V3 + "/cluster/logic" })
@Api(tags = "ES我的集群接口(REST)")
public class ESLogicClusterOpV3Controller {
    
    @Autowired
    private ClusterLogicManager clusterLogicManager;

    @GetMapping("/ids-names")
    @ResponseBody
    @ApiOperation(value = "获取project拥有的逻辑集群id和名称列表")
    public Result<List<Tuple<Long/*逻辑集群Id*/, String/*逻辑集群名称*/>>> getAppClusterLogicIdsAndNames(HttpServletRequest request) {
        return clusterLogicManager.listProjectClusterLogicIdsAndNames(HttpRequestUtil.getProjectId(request));
    }

    @GetMapping("/names")
    @ResponseBody
    @ApiOperation(value = "根据项目获取逻辑集群下的物理集群名称")
    public Result<List<String>> listClusterLogicNameByProjectId(HttpServletRequest request) {
        return Result.buildSucc(clusterLogicManager.listClusterLogicNameByProjectId(HttpRequestUtil.getProjectId(request)));
    }
    
    @GetMapping()
    @ResponseBody
    @ApiOperation(value = "根据projectId获取有权限的逻辑或物理集群信息")
    public Result<List<ClusterLogicVO>> getAppLogicClusterInfo(HttpServletRequest request) {
        return clusterLogicManager.getLogicClustersByProjectId(HttpRequestUtil.getProjectId(request));
    }

    @GetMapping("/{type}")
    @ResponseBody
    @ApiOperation(value = "根据项目和集群类型获取逻辑集群(项目对其有管理权限)名称列表")
    public Result<List<ClusterLogicVO>> getAppLogicClusterInfoByType(HttpServletRequest request,
                                                                       @PathVariable Integer type) {
        return clusterLogicManager.getProjectLogicClusterInfoByType(HttpRequestUtil.getProjectId(request), type);
    }

    @PostMapping("/page")
    @ResponseBody
    @ApiOperation(value = "条件获取逻辑集群列表")
    public PaginationResult<ClusterLogicVO> pageGetClusterLogics(HttpServletRequest request,
                                                                     @RequestBody ClusterLogicConditionDTO condition) {
        return clusterLogicManager.pageGetClusterLogics(condition, HttpRequestUtil.getProjectId(request));
    }
    
    @GetMapping("/detail/{clusterLogicId}")
    @ResponseBody
    @ApiOperation(value = "获取逻辑集群概览信息接口")
    @ApiImplicitParam(type = "Long", name = "clusterLogicId", value = "逻辑集群ID", required = true)
    public Result<ClusterLogicVO> detail(HttpServletRequest request, @PathVariable Long clusterLogicId) {
        return Result.buildSucc(
                clusterLogicManager.getClusterLogic(clusterLogicId, HttpRequestUtil.getProjectId(request)));
    }
    
    @GetMapping("/{logicClusterId}/{templateSize}/sizeCheck")
    @ResponseBody
    @ApiOperation(value = "校验模板大小资源是否充足,主要是为了避免用户反复的进行模板创建操作，对于申请的权限做一定的限制")
    public Result<Void> checkTemplateValidForCreate(@PathVariable("logicClusterId") Long logicClusterId,
                                                    @PathVariable("templateSize") String templateSize) {
        return Result.buildSucc();
    }
    
    @PutMapping()
    @ResponseBody
    @ApiOperation(value = "编辑逻辑集群接口")
    public Result<Void> modifyLogicCluster(HttpServletRequest request, @RequestBody ESLogicClusterDTO param) {
        return clusterLogicManager.editLogicCluster(param, HttpRequestUtil.getOperator(request),
                HttpRequestUtil.getProjectId(request));
    }
    
    @DeleteMapping("{clusterId}")
    @ResponseBody
    @ApiOperation(value = "下线集群")
    
    public Result<Void> delete(HttpServletRequest request, @PathVariable Long clusterId) throws AdminOperateException {
        return clusterLogicManager.deleteLogicCluster(clusterId, HttpRequestUtil.getOperator(request),
                HttpRequestUtil.getProjectId(request));
    }
    
    @GetMapping("/index-template-count/{clusterId}")
    @ResponseBody
    @ApiOperation(value = "提示用户索引和模板的数量")
    public Result<ClusterLogicTemplateIndexCountVO> indexTemplateCount(HttpServletRequest request, @PathVariable Long clusterId) {
        return clusterLogicManager.indexTemplateCount(clusterId, HttpRequestUtil.getOperator(request),
                HttpRequestUtil.getProjectId(request));
    }

    @GetMapping("/estimated-disk-size/{clusterLogicId}/{count}")
    @ResponseBody
    @ApiOperation(value = "获取预估磁盘大小")
    public Result<String>  estimatedDiskSize(@PathVariable Long clusterLogicId,@PathVariable Integer count) {
        return clusterLogicManager.estimatedDiskSize(clusterLogicId,count);
    }
}