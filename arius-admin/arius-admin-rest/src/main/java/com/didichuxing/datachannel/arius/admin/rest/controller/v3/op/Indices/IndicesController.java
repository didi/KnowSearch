package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.Indices;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.*;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.manage.IndexCatCellWithConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexCatCellVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexMappingVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexSettingVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexShardInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.didichuxing.datachannel.arius.admin.biz.indices.IndicesManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author lyn
 * @date 2021/09/28
 **/
@RestController
@RequestMapping(V3 + "/indices")
@Api(tags = "索引管理接口(REST)")
public class IndicesController {
    @Autowired
    private IndicesManager indicesManager;

    @PostMapping("/page")
    @ResponseBody
    @ApiOperation(value = "分页获取索引列表信息", notes = "携带可读可写标志位")
    public PaginationResult<IndexCatCellVO> pageGetIndexCatInfoVO(HttpServletRequest request, @RequestBody IndexQueryDTO condition) {
        return indicesManager.pageGetIndex(condition, HttpRequestUtils.getAppId(request));
    }

    @PutMapping("")
    @ResponseBody
    @ApiOperation(value = "创建索引")
    public Result<Void> createIndex(HttpServletRequest request, @RequestBody IndexCatCellWithConfigDTO param) {
        return indicesManager.createIndex(param, HttpRequestUtils.getAppId(request));
    }

    @GetMapping("")
    @ResponseBody
    @ApiOperation(value = "查询索引")
    public Result<IndexCatCellVO> getIndex(HttpServletRequest request, @RequestParam String cluster, @RequestParam String index) {
        return indicesManager.getIndexCatInfo(cluster, index, HttpRequestUtils.getAppId(request));
    }

    @DeleteMapping("")
    @ResponseBody
    @ApiOperation(value = "删除索引")
    public Result<Void> deleteIndex(HttpServletRequest request, @RequestBody List<IndexCatCellDTO> param) {
        return indicesManager.deleteIndex(param, HttpRequestUtils.getAppId(request), HttpRequestUtils.getOperator(request));
    }

    @PutMapping("/mapping")
    @ResponseBody
    @ApiOperation(value = "编辑mapping")
    public Result<Void> editMapping(HttpServletRequest request, @RequestBody IndexCatCellWithConfigDTO param) {
        //return indexManager.editMapping(param, HttpRequestUtils.getAppId(request));
        return Result.buildFail();
    }

    @GetMapping("/mapping")
    @ResponseBody
    @ApiOperation(value = "查询mapping")
    public Result<IndexMappingVO> getMapping(HttpServletRequest request, @RequestBody IndexCatCellWithConfigDTO param) {
        //return indexManager.getIndexMapping(clusterPhyName, indexName, HttpRequestUtils.getAppId(request));
        return  Result.buildFail();
    }

    @PutMapping("/setting")
    @ResponseBody
    @ApiOperation(value = "编辑setting")
    public Result<Void> editSetting(HttpServletRequest request, @RequestBody IndexCatCellWithConfigDTO param) {
        //return indexManager.editSetting(param, HttpRequestUtils.getAppId(request));
        return Result.buildFail();
    }

    @GetMapping("/setting")
    @ResponseBody
    @ApiOperation(value = "查询setting")
    public Result<IndexSettingVO> getSetting(HttpServletRequest request, @RequestBody IndexCatCellWithConfigDTO param) {
        //return indexManager.getIndexSetting(clusterPhyName, indexName, HttpRequestUtils.getAppId(request));
        return Result.buildFail();
    }

    @PutMapping("/close")
    @ResponseBody
    @ApiOperation(value = "关闭索引")
    public Result<Boolean> close(HttpServletRequest request, @RequestBody List<IndexCatCellDTO> params) {
        return indicesManager.batchUpdateIndexStatus(params, false, HttpRequestUtils.getAppId(request),
                HttpRequestUtils.getOperator(request));
    }

    @PutMapping("/open")
    @ResponseBody
    @ApiOperation(value = "开启索引")
    public Result<Boolean> open(@RequestBody List<IndicesOpenOrCloseDTO> params, HttpServletRequest request) {
        return Result.buildFail();
    }

    @PutMapping("/block")
    @ResponseBody
    @ApiOperation(value = "批量编辑索引阻塞设置")
    public Result<Boolean> editIndexBlockSetting(@RequestBody List<IndicesBlockSettingDTO> params,
                                                 HttpServletRequest request) {
        return indicesManager.batchEditIndexBlockSetting(params, HttpRequestUtils.getAppId(request),
            HttpRequestUtils.getOperator(request));
    }

    @PutMapping("/alias")
    @ResponseBody
    @ApiOperation(value = "编辑别名")
    public Result<Void> alias(HttpServletRequest request, @RequestBody IndexCatCellWithConfigDTO param) {
        return indicesManager.editAlias(param, Boolean.TRUE, HttpRequestUtils.getAppId(request));
    }

    @DeleteMapping("/alias")
    @ResponseBody
    @ApiOperation(value = "删除别名")
    public Result<Void> deleteAlias(HttpServletRequest request, @RequestBody IndexCatCellWithConfigDTO param) {
        return indicesManager.editAlias(param, Boolean.FALSE, HttpRequestUtils.getAppId(request));
    }

    @GetMapping("{clusterPhyName}/{indexName}/shard")
    @ResponseBody
    @ApiOperation(value = "获取索引shard分配详情")
    public Result<List<IndexShardInfoVO>> getIndexShard(@PathVariable String clusterPhyName,
                                                        @PathVariable String indexName, HttpServletRequest request) {
        return indicesManager.getIndexShardsInfo(clusterPhyName, indexName, HttpRequestUtils.getAppId(request));
    }

    @GetMapping("{clusterPhyName}/{indexName}/mapping")
    @ResponseBody
    @ApiOperation(value = "获取索引mapping信息")
    public Result<IndexMappingVO> mapping(@PathVariable String clusterPhyName, @PathVariable String indexName,
                                          HttpServletRequest request) {
        return indicesManager.getIndexMapping(clusterPhyName, indexName, HttpRequestUtils.getAppId(request));
    }

    @GetMapping("{clusterPhyName}/{indexName}/setting")
    @ResponseBody
    @ApiOperation(value = "获取模板setting信息")
    public Result<IndexSettingVO> setting(@PathVariable String clusterPhyName, @PathVariable String indexName,
                                          HttpServletRequest request) {
        return indicesManager.getIndexSetting(clusterPhyName, indexName, HttpRequestUtils.getAppId(request));
    }
}
