package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.indices;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import com.didichuxing.datachannel.arius.admin.biz.indices.IndicesManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndexCatCellDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.manage.IndexCatCellWithConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.srv.IndexForceMergeDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.srv.IndexRolloverDTO;
import com.didiglobal.knowframework.security.util.HttpRequestUtil;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chengxiang
 * @date 2022/5/31
 */
@RestController
@RequestMapping(V3 + "/indices/srv")
@Api(tags = "索引服务")
public class IndicesSrvController extends BaseIndicesController {

    @Autowired
    private IndicesManager indicesManager;

    @PostMapping("/rollover")
    @ResponseBody
    @ApiOperation(value = "rollover")
    public Result<Void> rollover(HttpServletRequest request, @RequestBody IndexRolloverDTO param) {
        List<IndexCatCellDTO> indices = param.getIndices();
        Result<Boolean> checkClusterValidResult = checkClusterValid(
            indices.stream().map(IndexCatCellDTO::getCluster).distinct().collect(Collectors.toList()));
        if (checkClusterValidResult.failed()) {
            return Result.buildFrom(checkClusterValidResult);
        }

        return indicesManager.rollover(param, HttpRequestUtil.getOperator(request),HttpRequestUtil.getProjectId(request));
    }

    @PostMapping("/shrink")
    @ResponseBody
    @ApiOperation(value = "shrink")
    public Result<Void> shrink(HttpServletRequest request, @RequestBody IndexCatCellWithConfigDTO param) {
        Result<Boolean> checkClusterValidResult = checkClusterValid(Lists.newArrayList(param.getCluster()));
        if (checkClusterValidResult.failed()) {
            return Result.buildFrom(checkClusterValidResult);
        }

        return indicesManager.shrink(param, HttpRequestUtil.getOperator(request),HttpRequestUtil.getProjectId(request));
    }

    @PostMapping("/split")
    @ResponseBody
    @ApiOperation(value = "split")
    public Result<Void> split(HttpServletRequest request, @RequestBody IndexCatCellWithConfigDTO param) {
        Result<Boolean> checkClusterValidResult = checkClusterValid(Lists.newArrayList(param.getCluster()));
        if (checkClusterValidResult.failed()) {
            return Result.buildFrom(checkClusterValidResult);
        }

        return indicesManager.split(param, HttpRequestUtil.getOperator(request),HttpRequestUtil.getProjectId(request));
    }

    @PostMapping("/force-merge")
    @ResponseBody
    @ApiOperation(value = "forceMerge")
    public Result<Void> forceMerge(HttpServletRequest request, @RequestBody IndexForceMergeDTO param) {
        List<IndexCatCellDTO> indices = param.getIndices();
        Result<Boolean> checkClusterValidResult = checkClusterValid(
            indices.stream().map(IndexCatCellDTO::getCluster).distinct().collect(Collectors.toList()));
        if (checkClusterValidResult.failed()) {
            return Result.buildFrom(checkClusterValidResult);
        }

        return indicesManager.forceMerge(param, HttpRequestUtil.getOperator(request),HttpRequestUtil.getProjectId(request));
    }

}