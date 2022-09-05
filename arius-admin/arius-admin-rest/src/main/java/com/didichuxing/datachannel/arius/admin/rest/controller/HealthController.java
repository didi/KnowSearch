package com.didichuxing.datachannel.arius.admin.rest.controller;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.task.dashboard.collector.ClusterDashBoardCollector;
import com.didichuxing.datachannel.arius.admin.task.dashboard.collector.IndexDashBoardCollector;
import com.didichuxing.datachannel.arius.admin.task.dashboard.collector.NodeDashBoardCollector;
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
   private IndexDashBoardCollector indexDashBoardCollector;
    @Autowired
    private ClusterDashBoardCollector clusterDashBoardCollector;
    @Autowired
    private NodeDashBoardCollector nodeDashBoardCollector;

    @GetMapping("/test")
    @ResponseBody
    @ApiOperation(value = "探活接口", notes = "")
    public Result<String> dem1o(@RequestParam String cluster) {
        indexDashBoardCollector.collectSingleCluster(cluster,System.currentTimeMillis());
        clusterDashBoardCollector.collectSingleCluster(cluster,System.currentTimeMillis());
        nodeDashBoardCollector.collectSingleCluster(cluster,System.currentTimeMillis());
        return Result.buildSuccWithMsg("let's go");
    }

}
