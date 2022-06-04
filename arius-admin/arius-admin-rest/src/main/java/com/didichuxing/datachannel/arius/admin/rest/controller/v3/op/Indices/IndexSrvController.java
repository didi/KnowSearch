package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.Indices;

import com.didichuxing.datachannel.arius.admin.biz.indices.IndexManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.manage.IndexCatCellWithConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.srv.IndexForceMergeDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.srv.IndexRolloverDTO;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.util.List;

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
    private IndexManager indexManager;

    @PostMapping("/rollover")
    @ResponseBody
    @ApiOperation(value = "rollover")
    public Result<Void> rollover(HttpServletRequest request, @RequestBody IndexRolloverDTO param) {
        return indexManager.rollover(param, HttpRequestUtils.getAppId(request));
    }

    @PostMapping("/shrink")
    @ResponseBody
    @ApiOperation(value = "shrink")
    public Result<Void> shrink(HttpServletRequest request, @RequestBody IndexCatCellWithConfigDTO param) {
        return Result.buildFail();
    }

    @PostMapping("/split")
    @ResponseBody
    @ApiOperation(value = "split")
    public Result<Void> split(HttpServletRequest request, @RequestBody IndexCatCellWithConfigDTO param) {
        return Result.buildFail();
    }

    @PostMapping("/forceMerge")
    @ResponseBody
    @ApiOperation(value = "forceMerge")
    public Result<List<String>> forceMerge(HttpServletRequest request, @RequestBody IndexForceMergeDTO param) {
        return Result.buildFail();
    }

}
