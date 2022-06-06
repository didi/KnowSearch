package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.cluster.phy;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterTemplateSrvVO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterTemplateSrv;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.TemplateSrvManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

@RestController
@RequestMapping(V3_OP + "/phy/cluster/templateSrv")
@Api(tags = "ES物理集群索引服务接口(REST)")
@Deprecated
public class ESPhyClusterTemplateSrvController {

    @Autowired
    private TemplateSrvManager templateSrvManager;

    @GetMapping("/{clusterName}")
    @ResponseBody
    @ApiOperation(value = "获取集群当前已经开启的索引服务", notes = "")
    public Result<List<ESClusterTemplateSrvVO>> list(@PathVariable String clusterName) {
        Result<List<ClusterTemplateSrv>> listResult = templateSrvManager.getPhyClusterTemplateSrv(clusterName);

        if (listResult.failed()) {
            return Result.buildFail(listResult.getMessage());
        }

        return Result.buildSucc(ConvertUtil.list2List(listResult.getData(), ESClusterTemplateSrvVO.class));
    }

    @GetMapping("/{clusterName}/select")
    @ResponseBody
    @ApiOperation(value = "获取集群可供选择的索引服务", notes = "")
    public Result<List<ESClusterTemplateSrvVO>> listSelect(@PathVariable String clusterName) {
        Result<List<ClusterTemplateSrv>> listResult = templateSrvManager.getPhyClusterSelectableTemplateSrv(clusterName);

        if (listResult.failed()) {
            return Result.buildFail(listResult.getMessage());
        }

        return Result.buildSucc(ConvertUtil.list2List(listResult.getData(), ESClusterTemplateSrvVO.class));
    }

    @PutMapping("/{clusterName}/{templateSrvId}")
    @ResponseBody
    @ApiOperation(value = "打开指定集群的指定索引服务", notes = "")
    public Result<Boolean> addTemplateSrvId(HttpServletRequest request,
                                            @PathVariable("clusterName") String clusterName,
                                            @PathVariable("templateSrvId") String templateSrvId) {
        return templateSrvManager.checkTemplateSrv(clusterName, templateSrvId, HttpRequestUtils.getOperator(request));
    }

    @DeleteMapping("/{clusterName}/{templateSrvId}")
    @ResponseBody
    @ApiOperation(value = "关闭指定集群的指定索引服务", notes = "")
    public Result<Boolean> delTemplateSrvId(HttpServletRequest request, @PathVariable String clusterName, @PathVariable String templateSrvId) {
        return templateSrvManager.delTemplateSrv(clusterName, templateSrvId, HttpRequestUtils.getOperator(request));
    }
}
