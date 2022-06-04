package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.Indices;

import com.didichuxing.datachannel.arius.admin.biz.indices.IndicesManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.*;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.manage.IndexCatCellWithConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexCatCellVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexMappingVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexSettingVO;
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
@RequestMapping(V3_OP + "/index/manage")
@Api(tags = "索引管理")
public class IndexManageController {

    @Autowired
    private IndicesManager indexManager;

    @PostMapping("/page")
    @ResponseBody
    @ApiOperation(value = "分页查询索引管理列表")
    public PaginationResult<IndexCatCellVO> pageGetIndex(HttpServletRequest request, @RequestBody IndexQueryDTO param) {
        return indexManager.pageGetIndex(param, HttpRequestUtils.getAppId(request));
    }

    @PutMapping("")
    @ResponseBody
    @ApiOperation(value = "创建索引")
    public Result<Void> createIndex(HttpServletRequest request, @RequestBody IndexCatCellWithConfigDTO param) {
        return indexManager.createIndex(param, HttpRequestUtils.getAppId(request));
    }

    @GetMapping("")
    @ResponseBody
    @ApiOperation(value = "查询索引")
    public Result<IndexCatCellVO> getIndex(HttpServletRequest request, @RequestParam String cluster, @RequestParam String index) {
        return indexManager.getIndexCatInfo(cluster, index, HttpRequestUtils.getAppId(request));
    }

    @DeleteMapping("")
    @ResponseBody
    @ApiOperation(value = "删除索引")
    public Result<Void> deleteIndex(HttpServletRequest request, @RequestBody List<IndexCatCellDTO> param) {
        return indexManager.deleteIndex(param, HttpRequestUtils.getAppId(request), HttpRequestUtils.getOperator(request));
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

    @PutMapping("/block")
    @ResponseBody
    @ApiOperation(value = "批量编辑索引阻塞设置")
    public Result<Boolean> editIndexBlockSetting(@RequestBody List<IndicesBlockSettingDTO> params,
                                                 HttpServletRequest request) {
        return indexManager.batchEditIndexBlockSetting(params, HttpRequestUtils.getAppId(request),
                HttpRequestUtils.getOperator(request));
    }

    @PutMapping("/alias")
    @ResponseBody
    @ApiOperation(value = "编辑别名")
    public Result<Void> alias(HttpServletRequest request, @RequestBody IndexCatCellWithConfigDTO param) {
        return indexManager.editAlias(param, Boolean.TRUE, HttpRequestUtils.getAppId(request));
    }

    @DeleteMapping("/alias")
    @ResponseBody
    @ApiOperation(value = "删除别名")
    public Result<Void> deleteAlias(HttpServletRequest request, @RequestBody IndexCatCellWithConfigDTO param) {
        return indexManager.editAlias(param, Boolean.FALSE, HttpRequestUtils.getAppId(request));
    }

    @PutMapping("/close")
    @ResponseBody
    @ApiOperation(value = "关闭索引")
    public Result<Boolean> close(HttpServletRequest request, @RequestBody List<IndexCatCellDTO> params) {
        return indexManager.batchUpdateIndexStatus(params, false, HttpRequestUtils.getAppId(request),
                HttpRequestUtils.getOperator(request));
    }

}
