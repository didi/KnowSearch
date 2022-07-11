package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.indices;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import com.didichuxing.datachannel.arius.admin.biz.indices.IndicesManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndexCatCellDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndexQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndicesBlockSettingDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.manage.IndexCatCellWithConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexCatCellVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexMappingVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexSettingVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.indices.IndexShardInfoVO;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lyn
 * @date 2021/09/28
 **/
@RestController
@RequestMapping(V3 + "/indices")
@Api(tags = "索引管理接口(REST)")
public class IndicesController extends BaseIndicesController{
    @Autowired
    private IndicesManager indicesManager;

    @PostMapping("/page")
    @ResponseBody
    @ApiOperation(value = "分页获取索引列表信息", notes = "携带可读可写标志位")
    public PaginationResult<IndexCatCellVO> pageGetIndexCatInfoVO(HttpServletRequest request,
                                                                  @RequestBody IndexQueryDTO condition) throws NotFindSubclassException {
        return indicesManager.pageGetIndex(condition, HttpRequestUtil.getProjectId(request));
    }

    @PostMapping("")
    @ResponseBody
    @ApiOperation(value = "创建索引")
    public Result<Void> createIndex(HttpServletRequest request, @RequestBody IndexCatCellWithConfigDTO param) {
        Result<Boolean> checkClusterValidResult = checkClusterValid(Lists.newArrayList(param.getCluster()));
        if (checkClusterValidResult.failed()) { return Result.buildFrom(checkClusterValidResult);}

        return indicesManager.createIndex(param, HttpRequestUtil.getProjectId(request),
                HttpRequestUtil.getOperator(request));
    }

    @GetMapping("/{cluster}/{indexName}")
    @ResponseBody
    @ApiOperation(value = "查询索引")
    public Result<IndexCatCellVO> getIndex(HttpServletRequest request, @PathVariable String cluster,
                                           @PathVariable String indexName) {
        return indicesManager.getIndexCatInfo(cluster, indexName, HttpRequestUtil.getProjectId(request));
    }

    @GetMapping("/{cluster}/{indexName}/exists")
    @ResponseBody
    @ApiOperation(value = "查询索引")
    public Result<Boolean> isExists(HttpServletRequest request, @PathVariable String cluster,
                                    @PathVariable String indexName) {
        return indicesManager.isExists(cluster, indexName, HttpRequestUtil.getProjectId(request));
    }

    @DeleteMapping("")
    @ResponseBody
    @ApiOperation(value = "删除索引")
    public Result<Boolean> deleteIndex(HttpServletRequest request, @RequestBody List<IndexCatCellDTO> params) {
        Result<Boolean> checkClusterValidResult = checkClusterValid(params.stream().map(IndexCatCellDTO::getCluster)
                .distinct().collect(Collectors.toList()));
        if (checkClusterValidResult.failed()) { return Result.buildFrom(checkClusterValidResult);}

        return indicesManager.deleteIndex(params, HttpRequestUtil.getProjectId(request),
            HttpRequestUtil.getOperator(request));
    }

    @PutMapping("/mapping")
    @ResponseBody
    @ApiOperation(value = "编辑mapping")
    public Result<Void> editMapping(HttpServletRequest request, @RequestBody IndexCatCellWithConfigDTO param) {
        Result<Boolean> checkClusterValidResult = checkClusterValid(Lists.newArrayList(param.getCluster()));
        if (checkClusterValidResult.failed()) { return Result.buildFrom(checkClusterValidResult);}

        return indicesManager.editMapping(param, HttpRequestUtil.getProjectId(request));
    }

    @GetMapping("/{cluster}/{indexName}/mapping")
    @ResponseBody
    @ApiOperation(value = "查询mapping")
    public Result<IndexMappingVO> getMapping(@PathVariable String cluster, @PathVariable String indexName,
                                             HttpServletRequest request) {
        return indicesManager.getMapping(cluster, indexName, HttpRequestUtil.getProjectId(request));
    }

    @PutMapping("/setting")
    @ResponseBody
    @ApiOperation(value = "编辑setting")
    public Result<Void> editSetting(HttpServletRequest request, @RequestBody IndexCatCellWithConfigDTO param) throws ESOperateException {
        Result<Boolean> checkClusterValidResult = checkClusterValid(Lists.newArrayList(param.getCluster()));
        if (checkClusterValidResult.failed()) { return Result.buildFrom(checkClusterValidResult);}

        return indicesManager.editSetting(param, HttpRequestUtil.getProjectId(request));
    }

    @GetMapping("/{cluster}/{indexName}/setting")
    @ResponseBody
    @ApiOperation(value = "查询setting")
    public Result<IndexSettingVO> getSetting(@PathVariable String cluster, @PathVariable String indexName,
                                             HttpServletRequest request) {
        return indicesManager.getSetting(cluster, indexName, HttpRequestUtil.getProjectId(request));
    }

    @PutMapping("/close")
    @ResponseBody
    @ApiOperation(value = "关闭索引")
    public Result<Boolean> close(HttpServletRequest request, @RequestBody List<IndexCatCellDTO> params) {
        Result<Boolean> checkClusterValidResult = checkClusterValid(params.stream().map(IndexCatCellDTO::getCluster)
                .distinct().collect(Collectors.toList()));
        if (checkClusterValidResult.failed()) { return Result.buildFrom(checkClusterValidResult);}

        return indicesManager.closeIndex(params, HttpRequestUtil.getProjectId(request),
            HttpRequestUtil.getOperator(request));
    }

    @PutMapping("/open")
    @ResponseBody
    @ApiOperation(value = "关闭索引")
    public Result<Boolean> open(HttpServletRequest request, @RequestBody List<IndexCatCellDTO> params) {
        Result<Boolean> checkClusterValidResult = checkClusterValid(params.stream().map(IndexCatCellDTO::getCluster)
                .distinct().collect(Collectors.toList()));
        if (checkClusterValidResult.failed()) { return Result.buildFrom(checkClusterValidResult);}

        return indicesManager.openIndex(params, HttpRequestUtil.getProjectId(request),
            HttpRequestUtil.getOperator(request));
    }

    @PutMapping("/block")
    @ResponseBody
    @ApiOperation(value = "批量编辑索引阻塞设置")
    public Result<Boolean> editIndexBlockSetting(@RequestBody List<IndicesBlockSettingDTO> params,
                                                 HttpServletRequest request) {
        Result<Boolean> checkClusterValidResult = checkClusterValid(params.stream().map(IndicesBlockSettingDTO::getCluster)
                .distinct().collect(Collectors.toList()));
        if (checkClusterValidResult.failed()) { return Result.buildFrom(checkClusterValidResult);}
        Result<Void> result = indicesManager.editIndexBlockSetting(params, HttpRequestUtil.getProjectId(request),
                HttpRequestUtil.getOperator(request));
        return Result.buildSucc(result.success()?Boolean.TRUE:Boolean.FALSE);
    }
    
    @GetMapping("/{cluster}/{indexName}/alias")
    @ResponseBody
    @ApiOperation(value = "获取索引别名")
    public Result<List<String>> getIndexAliases(HttpServletRequest request, @PathVariable String cluster, @PathVariable String indexName) {
        return indicesManager.getIndexAliases(cluster, indexName, HttpRequestUtil.getProjectId(request));
    }
    
    @PutMapping("/alias")
    @ResponseBody
    @ApiOperation(value = "编辑别名")
    public Result<Void> alias(HttpServletRequest request, @RequestBody IndexCatCellWithConfigDTO param) {
        Result<Boolean> checkClusterValidResult = checkClusterValid(Lists.newArrayList(param.getCluster()));
        if (checkClusterValidResult.failed()) { return Result.buildFrom(checkClusterValidResult);}

        return indicesManager.addIndexAliases(param, HttpRequestUtil.getProjectId(request),HttpRequestUtil.getOperator(request));
    }

    @DeleteMapping("/alias")
    @ResponseBody
    @ApiOperation(value = "删除别名")
    public Result<Void> deleteAlias(HttpServletRequest request, @RequestBody IndexCatCellWithConfigDTO param) {
        Result<Boolean> checkClusterValidResult = checkClusterValid(Lists.newArrayList(param.getCluster()));
        if (checkClusterValidResult.failed()) { return Result.buildFrom(checkClusterValidResult);}

        return indicesManager.deleteIndexAliases(param, HttpRequestUtil.getProjectId(request),HttpRequestUtil.getOperator(request));
    }

    @GetMapping("/{cluster}/{indexName}/shard")
    @ResponseBody
    @ApiOperation(value = "获取索引shard分配详情")
    public Result<List<IndexShardInfoVO>> getIndexShard(@PathVariable String cluster, @PathVariable String indexName,
                                                        HttpServletRequest request) {
        return indicesManager.getIndexShardsInfo(cluster, indexName, HttpRequestUtil.getProjectId(request));
    }

    @GetMapping("/{clusterPhyName}/phy/indices")
    @ResponseBody
    @ApiModelProperty(value = "获取物理集群索引列表")
    public Result<List<String>> getClusterPhyIndexName(@PathVariable String clusterPhyName,
                                                       HttpServletRequest request) {
        return indicesManager.getClusterPhyIndexName(clusterPhyName, HttpRequestUtil.getProjectId(request));
    }

    @GetMapping("/{clusterLogicName}/logic/indices")
    @ResponseBody
    @ApiModelProperty(value = "获取逻辑集群索引列表")
    public Result<List<String>> getClusterLogicIndexName(@PathVariable String clusterLogicName,
                                                         HttpServletRequest request) {
        return indicesManager.getClusterLogicIndexName(clusterLogicName, HttpRequestUtil.getProjectId(request));
    }


}