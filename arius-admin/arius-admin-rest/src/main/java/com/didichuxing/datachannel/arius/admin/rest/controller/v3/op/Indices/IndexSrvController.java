package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.Indices;

import com.didichuxing.datachannel.arius.admin.biz.indices.IndicesManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.manage.IndexCatCellWithConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.srv.IndexForceMergeDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.srv.IndexRolloverDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

/**
 * @author chengxiang
 * @date 2022/5/31
 */
@RestController
@RequestMapping(V3_OP + "/index/srv")
@Api(tags = "索引服务")
public class IndexSrvController {

    @Autowired
    private IndicesManager indicesManager;

    @PostMapping("/rollover")
    @ResponseBody
    @ApiOperation(value = "rollover")
    public Result<Void> rollover(HttpServletRequest request, @RequestBody IndexRolloverDTO param) {
        return indicesManager.rollover(param);
    }

    @PostMapping("/shrink")
    @ResponseBody
    @ApiOperation(value = "shrink")
    public Result<Void> shrink(HttpServletRequest request, @RequestBody IndexCatCellWithConfigDTO param) {
        return indicesManager.shrink(param);
    }

    @PostMapping("/split")
    @ResponseBody
    @ApiOperation(value = "split")
    public Result<Void> split(HttpServletRequest request, @RequestBody IndexCatCellWithConfigDTO param) {
        return indicesManager.split(param);
    }

    @PostMapping("/forceMerge")
    @ResponseBody
    @ApiOperation(value = "forceMerge")
    public Result<Void> forceMerge(HttpServletRequest request, @RequestBody IndexForceMergeDTO param) {
        return indicesManager.forceMerge(param);
    }

}
