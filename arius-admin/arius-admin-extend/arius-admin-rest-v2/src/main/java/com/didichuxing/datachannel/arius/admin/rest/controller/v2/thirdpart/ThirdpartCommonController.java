package com.didichuxing.datachannel.arius.admin.rest.controller.v2.thirdpart;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_THIRD_PART;

import com.didichuxing.datachannel.arius.admin.biz.thardpart.CommonManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.config.AriusConfigInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ThirdPartClusterVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.config.ThirdpartConfigVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(V2_THIRD_PART + "/common")
@Api(tags = "第三方公共接口(REST)")
public class ThirdpartCommonController {

    @Autowired
    private CommonManager commonManager;

    @GetMapping("/cluster/list")
    @ResponseBody
    @ApiOperation(value = "获取物理集群列表接口【三方接口】",tags = "【三方接口】" )
    public Result<List<ThirdPartClusterVO>> listDataCluster() {
        return commonManager.listDataCluster();

    }

    @GetMapping("/cluster/getByName")
    @ResponseBody
    @ApiOperation(value = "获取集群接口【三方接口】",tags = "【三方接口】" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "String", name = "cluster", value = "集群名称", required = true) })
    public Result<ThirdPartClusterVO> getDataCluster(@RequestParam("cluster") String cluster) {
        return commonManager.getDataCluster(cluster);

    }
    
    @PostMapping("/config/query")
    @ResponseBody
    @ApiOperation(value = "获取配置列表接口", notes = "")
    public Result<List<ThirdpartConfigVO>> queryConfig(@RequestBody AriusConfigInfoDTO param) {
        return commonManager.queryConfig(param);
    }
}