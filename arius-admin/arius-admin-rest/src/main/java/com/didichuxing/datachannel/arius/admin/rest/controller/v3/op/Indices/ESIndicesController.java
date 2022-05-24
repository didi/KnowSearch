package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.Indices;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndicesConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndicesOpenOrCloseDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexCatCellVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexMappingVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexSettingVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexShardInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.didichuxing.datachannel.arius.admin.biz.indices.IndicesManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndicesBlockSettingDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndicesClearDTO;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author lyn
 * @date 2021/09/28
 **/
@RestController
@RequestMapping(V3 + "/indices")
@Api(tags = "Console-索引管理接口(REST)")
public class ESIndicesController {
    @Autowired
    private IndicesManager indicesManager;

    @PostMapping("/page")
    @ResponseBody
    @ApiOperation(value = "分页获取索引列表信息", notes = "携带可读可写标志位")
    public PaginationResult<IndexCatCellVO> pageGetIndexCatInfoVO(HttpServletRequest request, @RequestBody IndicesConditionDTO condition) {
        return indicesManager.pageGetIndexCatInfoVO(condition, HttpRequestUtils.getProjectId(request));
    }

    @GetMapping("/select")
    @ResponseBody
    @ApiOperation(value = "获取单个索引的详情信息")
    public Result<IndexCatCellVO> getIndexCatInfoVO(HttpServletRequest request, @RequestParam String clusterPhyName, @RequestParam String indexName) {
        return indicesManager.getIndexCatInfo(clusterPhyName, indexName, HttpRequestUtils.getProjectId(request));
    }

    @DeleteMapping("")
    @ResponseBody
    @ApiOperation(value = "批量删除索引")
    public Result<Boolean> delete(@RequestBody List<IndicesClearDTO> params, HttpServletRequest request) {
        return indicesManager.batchDeleteIndex(params, HttpRequestUtils.getProjectId(request),
            HttpRequestUtils.getOperator(request));
    }

    @PutMapping("/close")
    @ResponseBody
    @ApiOperation(value = "关闭索引")
    public Result<Boolean> close(@RequestBody List<IndicesOpenOrCloseDTO> params, HttpServletRequest request) {
        return indicesManager.batchUpdateIndexStatus(params, false, HttpRequestUtils.getProjectId(request),
                HttpRequestUtils.getOperator(request));
    }

    @PutMapping("/open")
    @ResponseBody
    @ApiOperation(value = "开启索引")
    public Result<Boolean> open(@RequestBody List<IndicesOpenOrCloseDTO> params, HttpServletRequest request) {
        return indicesManager.batchUpdateIndexStatus(params, true, HttpRequestUtils.getProjectId(request),
                HttpRequestUtils.getOperator(request));
    }

    @PutMapping("/block")
    @ResponseBody
    @ApiOperation(value = "批量编辑索引阻塞设置")
    public Result<Boolean> editIndexBlockSetting(@RequestBody List<IndicesBlockSettingDTO> params,
                                                 HttpServletRequest request) {
        return indicesManager.batchEditIndexBlockSetting(params, HttpRequestUtils.getProjectId(request),
            HttpRequestUtils.getOperator(request));
    }

    @GetMapping("{clusterPhyName}/{indexName}/shard")
    @ResponseBody
    @ApiOperation(value = "获取索引shard分配详情")
    public Result<List<IndexShardInfoVO>> getIndexShard(@PathVariable String clusterPhyName,
                                                        @PathVariable String indexName, HttpServletRequest request) {
        return indicesManager.getIndexShardsInfo(clusterPhyName, indexName, HttpRequestUtils.getProjectId(request));
    }

    @GetMapping("{clusterPhyName}/{indexName}/mapping")
    @ResponseBody
    @ApiOperation(value = "获取索引mapping信息")
    public Result<IndexMappingVO> mapping(@PathVariable String clusterPhyName, @PathVariable String indexName,
                                          HttpServletRequest request) {
        return indicesManager.getIndexMapping(clusterPhyName, indexName, HttpRequestUtils.getProjectId(request));
    }

    @GetMapping("{clusterPhyName}/{indexName}/setting")
    @ResponseBody
    @ApiOperation(value = "获取模板setting信息")
    public Result<IndexSettingVO> setting(@PathVariable String clusterPhyName, @PathVariable String indexName,
                                          HttpServletRequest request) {
        return indicesManager.getIndexSetting(clusterPhyName, indexName, HttpRequestUtils.getProjectId(request));
    }
}