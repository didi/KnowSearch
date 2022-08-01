package com.didichuxing.datachannel.arius.admin.rest.controller;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterLogicManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("healthController")
@RequestMapping("/health")
@Api("web应用探活接口(REST)")
public class HealthController {

    /**
     * 探活接口
     *
     * @return
     */
    @GetMapping("")
    @ResponseBody
    @ApiOperation(value = "探活接口", notes = "")
    public Result<String> demo() {
        return Result.buildSuccWithMsg("let's go");
    }

    @Autowired
    private ClusterLogicManager clusterLogicManager;
    @GetMapping("/test")
    @ResponseBody
    @ApiOperation(value = "探活接口", notes = "")
    public Result<String> demo1(@RequestParam Long id) {
        clusterLogicManager.updateClusterLogicHealth(id);
        return Result.buildSuccWithMsg("let's go");
    }

}
