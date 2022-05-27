package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.cluster.logic;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.TemplateSrvManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterTemplateSrvVO;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(V3_OP + "/logic/cluster/templateSrv")
@Api(tags = "ES逻辑集群索引服务接口(REST)")
public class ClusterLogicTemplateSrvController {

    @Autowired
    private TemplateSrvManager templateSrvManager;

    @GetMapping("/{clusterLogicId}")
    @ResponseBody
    @ApiOperation(value = "获取逻辑集群当前已经开启的索引服务")
    public Result<List<ESClusterTemplateSrvVO>> list(@PathVariable Long clusterLogicId) {
        return templateSrvManager.getClusterLogicTemplateSrv(clusterLogicId);
    }

    @GetMapping("/{clusterLogicId}/select")
    @ResponseBody
    @ApiOperation(value = "获取逻辑集群可供选择的索引服务")
    public Result<List<ESClusterTemplateSrvVO>> listSelect(@PathVariable Long clusterLogicId) {
        return templateSrvManager.getClusterLogicSelectableTemplateSrv(clusterLogicId);
    }

    @PutMapping("/{clusterLogicId}/{templateSrvId}")
    @ResponseBody
    @ApiOperation(value = "打开逻辑集群的指定索引服务", notes = "")
    public Result<Boolean> addTemplateSrvId(HttpServletRequest request, @PathVariable Long clusterLogicId,
                                            @PathVariable String templateSrvId) {
        return templateSrvManager.addTemplateSrvForClusterLogic(clusterLogicId, templateSrvId,
            HttpRequestUtil.getOperator(request));
    }

    @DeleteMapping("/{clusterLogicId}/{templateSrvId}")
    @ResponseBody
    @ApiOperation(value = "关闭指定集群的指定索引服务", notes = "")
    public Result<Boolean> delTemplateSrvId(HttpServletRequest request, @PathVariable Long clusterLogicId,
                                            @PathVariable String templateSrvId) {
        return templateSrvManager.delTemplateSrvForClusterLogic(clusterLogicId, templateSrvId,
            HttpRequestUtil.getOperator(request) );
    }
}