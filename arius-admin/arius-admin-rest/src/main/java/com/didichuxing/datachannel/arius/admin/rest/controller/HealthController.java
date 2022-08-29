package com.didichuxing.datachannel.arius.admin.rest.controller;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.task.dashboard.collector.IndexDashBoardCollector;
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

    /**
     * 探活接口
     *
     * @return
     */
    @Autowired
    private IndexDashBoardCollector indexDashBoardCollector;
    @GetMapping("/test")
    @ResponseBody
    @ApiOperation(value = "探活接口", notes = "")
    public Result<String> demo1(@RequestParam String cluster) {
        indexDashBoardCollector.collectSingleCluster(cluster,System.currentTimeMillis());
        return
                Result.buildSuccWithMsg("let's go");
    }

}
