package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.Indices;

import com.didichuxing.datachannel.arius.admin.biz.indices.manage.NewIndexManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndexCatCellDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.manage.IndexCatCellWithCreateInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.manage.IndexQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexCatCellVO;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
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
@RequestMapping(V3_OP + "/index/manage")
@Api(tags = "索引管理")
public class IndexManageController {

    @Autowired
    private NewIndexManager newIndexManager;

    @PostMapping("/page")
    @ResponseBody
    @ApiOperation(value = "分页查询索引管理列表")
    public PaginationResult<IndexCatCellVO> pageGetIndexCatCell(HttpServletRequest request, @RequestBody IndexQueryDTO param) {
        return newIndexManager.pageGetIndexCatCell(param, HttpRequestUtils.getAppId(request));
    }

    @PutMapping("")
    @ResponseBody
    @ApiOperation(value = "创建索引")
    public Result<Void> createIndex(HttpServletRequest request, @RequestBody IndexCatCellWithCreateInfoDTO param) {
        return newIndexManager.createIndex(param, HttpRequestUtils.getAppId(request));
    }

    @GetMapping("")
    @ResponseBody
    @ApiOperation(value = "查询索引")
    public Result<IndexCatCellDTO> getIndex(HttpServletRequest request, @RequestParam String indexName) {
        return newIndexManager.getIndex(indexName, HttpRequestUtils.getAppId(request));
    }

    @DeleteMapping("")
    @ResponseBody
    @ApiOperation(value = "删除索引")
    public Result<Void> deleteIndex(HttpServletRequest request, @RequestBody IndexCatCellDTO param) {
        return newIndexManager.deleteIndex(param, HttpRequestUtils.getAppId(request));
    }

    @PostMapping("/mapping")
    @ResponseBody
    @ApiOperation(value = "编辑mapping")
    public Result<Void> editMapping(HttpServletRequest request, @RequestBody IndexCatCellDTO param) {
        return newIndexManager.editMapping(param, HttpRequestUtils.getAppId(request));
    }

    @GetMapping("/mapping")
    @ResponseBody
    @ApiOperation(value = "查询mapping")
    public Result<String> getMapping(HttpServletRequest request, @RequestParam String indexName) {
        return newIndexManager.getMapping(indexName, HttpRequestUtils.getAppId(request));
    }

    @PostMapping("/setting")
    @ResponseBody
    @ApiOperation(value = "编辑setting")
    public Result<Void> editSetting(HttpServletRequest request, @RequestBody IndexCatCellDTO param) {
        return newIndexManager.editSetting(param, HttpRequestUtils.getAppId(request));
    }

    @GetMapping("/setting")
    @ResponseBody
    @ApiOperation(value = "查询setting")
    public Result<String> getSetting(HttpServletRequest request, @RequestParam String indexName) {
        return newIndexManager.getSetting(indexName, HttpRequestUtils.getAppId(request));
    }

    @PostMapping("/blockRead")
    @ResponseBody
    @ApiOperation(value = "禁用读")
    public Result<Void> blockRead(HttpServletRequest request, @RequestBody IndexCatCellDTO param) {
        return newIndexManager.blockRead(param, HttpRequestUtils.getAppId(request));
    }

    @PostMapping("/blockWrite")
    @ResponseBody
    @ApiOperation(value = "禁用写")
    public Result<Void> blockWrite(HttpServletRequest request, @RequestBody IndexCatCellDTO param) {
        return newIndexManager.blockWrite(param, HttpRequestUtils.getAppId(request));
    }

    @PostMapping("/alias")
    @ResponseBody
    @ApiOperation(value = "编辑别名")
    public Result<Void> alias(HttpServletRequest request, @RequestBody IndexCatCellDTO param) {
        return newIndexManager.editAlias(param, HttpRequestUtils.getAppId(request));
    }

    @DeleteMapping("/alias")
    @ResponseBody
    @ApiOperation(value = "删除别名")
    public Result<Void> deleteAlias(HttpServletRequest request, @RequestBody IndexCatCellDTO param) {
        return newIndexManager.deleteAlias(param, HttpRequestUtils.getAppId(request));
    }

    @PostMapping("/close")
    @ResponseBody
    @ApiOperation(value = "关闭索引")
    public Result<Void> close(HttpServletRequest request, @RequestBody IndexCatCellDTO param) {
        return newIndexManager.close(param, HttpRequestUtils.getAppId(request));
    }

}
