package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.cluster.logic;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterLogicManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ConsoleClusterVO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterLogic;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ESClusterLogicService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;


/**
 * @author lanxinzheng
 * @date 2020/10/22
 */
@RestController
@RequestMapping(V3_OP + "/logic/cluster")
@Api(value = "运维逻辑集群接口(REST)")
public class ESLogicClusterOpV3Controller {

    @Autowired
    private ESClusterLogicService esClusterLogicService;

    @Autowired
    private AppService          appService;

    @Autowired
    private ClusterLogicManager logicClusterFactory;

    @GetMapping("/list")
    @ResponseBody
    @ApiOperation(value = "获取逻辑集群列表接口", notes = "")
    @ApiImplicitParams({
        @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "appId", required = false,
            value = "app的Id, 若无则不根据app筛选（取全部集群）"),
        @ApiImplicitParam(paramType = "query", dataType = "Boolean", name = "onlyOwn", required = false, defaultValue = "false",
            value = "为true时只取app是owner的逻辑集群，为false时取有权限的逻辑集群，仅当appId有值时有效，默认为false" ),
    })
    public Result<List<ConsoleClusterVO>> getLogicClusterLists(
        @RequestParam(value = "appId", required = false, defaultValue = "-1") Integer appId,
        @RequestParam(value = "onlyOwn", required = false, defaultValue = "false") Boolean onlyOwn) {

        List<ESClusterLogic> esClusterLogics = null;
        if (appId.equals(-1)) {
            // 未指定app，取所有集群
            esClusterLogics = esClusterLogicService.listAllLogicClusters();
        } else {
            // 指定了app
            if (appService.getAppById(appId) == null) {
                return Result.buildFrom(Result.buildNotExist("应用不存在"));
            }

            if (onlyOwn) {
                // 只取app作为owner的
                esClusterLogics = esClusterLogicService.getOwnedLogicClustersByAppId(appId);
            } else {
                // 取app有权限的
                esClusterLogics = esClusterLogicService.getHasAuthLogicClustersByAppId(appId);
            }
        }

        // 运维页面，筛选的appId用于判断集群权限
        return Result.buildSucc(logicClusterFactory.batchBuildOpClusterVOs(esClusterLogics, appId));
    }
}
