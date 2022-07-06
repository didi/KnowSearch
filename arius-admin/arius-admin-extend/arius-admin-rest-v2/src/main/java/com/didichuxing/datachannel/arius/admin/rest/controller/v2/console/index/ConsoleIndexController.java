package com.didichuxing.datachannel.arius.admin.rest.controller.v2.console.index;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_CONSOLE;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(V2_CONSOLE + "/index")
@Api(tags = "Console-用户侧索引接口(REST)")
@Deprecated
public class ConsoleIndexController {

    //@Autowired
    //private ESIndexService esIndexService;

    @GetMapping("/mapping/get")
    @ResponseBody
    @ApiOperation(value = "获取索引的mapping信息【三方接口】",tags = "【三方接口】" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "String", name = "cluster", value = "集群名称", required = true),
                         @ApiImplicitParam(paramType = "query", dataType = "String", name = "index", value = "索引名称", required = true) })
    public Result<String> indexMapping(@RequestParam("cluster") String cluster, @RequestParam("index") String index) {
         return Result.buildFail("接口已经下线：迁移到v3");
        //return Result.buildSucc(esIndexService.syncGetIndexMapping(cluster, index),"");
    }
}