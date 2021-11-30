package com.didichuxing.datachannel.arius.admin.rest.controller.v2.op.template;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_OP;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.ConsoleTemplatePhyVO;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.didichuxing.datachannel.arius.admin.biz.template.TemplatePhyManager;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.TemplatePhyService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.IndexTemplatePhysicalDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.TemplatePhysicalCopyDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.TemplatePhysicalDCDRDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.TemplatePhysicalUpgradeDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.IndexTemplatePhysicalVO;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.dcdr.TemplateDcdrManager;
import com.google.common.collect.Lists;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * @author d06679
 * @date 2017/10/23
 */
@RestController
@RequestMapping(V2_OP + "/template/physical")
@Api(tags = "es集群物理模板接口(REST)")
public class TemplatePhysicalController {

    @Autowired
    private TemplatePhyService  templatePhyService;

    @Autowired
    private TemplatePhyManager  templatePhyManager;

    @Autowired
    private TemplateDcdrManager templateDcdrManager;

    @Autowired
    private ESIndexService      esIndexService;

    /**
     * @param param
     * @param request
     * @return
     * @deprecated
     */
    @PostMapping("/list")
    @ResponseBody
    @ApiOperation(value = "获取物理模板列表接口")
    public Result<List<ConsoleTemplatePhyVO>> list(@RequestBody IndexTemplatePhysicalDTO param,
                                                   HttpServletRequest request) {
        return Result.buildSucc(templatePhyManager.getConsoleTemplatePhyVOS(param, HttpRequestUtils.getAppId(request)));
    }

    @GetMapping("/get")
    @ResponseBody
    @ApiOperation(value = "获取指定物理模板接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "physicalId", value = "物理模板ID", required = true) })
    public Result<IndexTemplatePhysicalVO> get(@RequestParam("physicalId") Long physicalId) {
        return Result.buildSucc(
            ConvertUtil.obj2Obj(templatePhyService.getTemplateById(physicalId), IndexTemplatePhysicalVO.class));
    }

    @DeleteMapping("/del")
    @ResponseBody
    @ApiOperation(value = "删除物理模板接口", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "physicalId", value = "物理模板ID", required = true) })
    public Result<Void> delete(HttpServletRequest request,
                         @RequestParam(value = "physicalId") Long physicalId) throws ESOperateException {
        return templatePhyManager.delTemplate(physicalId, HttpRequestUtils.getOperator(request));
    }

    @PutMapping("/upgrade")
    @ResponseBody
    @ApiOperation(value = "升级物理模板接口", notes = "")
    public Result<Void> upgrade(HttpServletRequest request,
                          @RequestBody TemplatePhysicalUpgradeDTO param) throws ESOperateException {
        return templatePhyManager.upgradeTemplate(param, HttpRequestUtils.getOperator(request));
    }

    @PutMapping("/copy")
    @ResponseBody
    @ApiOperation(value = "复制物理模板接口", notes = "")
    public Result<Void> copy(HttpServletRequest request,
                       @RequestBody TemplatePhysicalCopyDTO param) throws AdminOperateException {
        return templatePhyManager.copyTemplate(param, HttpRequestUtils.getOperator(request));
    }

    @PostMapping("/edit")
    @ResponseBody
    @ApiOperation(value = "编辑物理资源接口", notes = "")
    public Result<Void> edit(HttpServletRequest request,
                       @RequestBody IndexTemplatePhysicalDTO param) throws ESOperateException {
        return templatePhyManager.editTemplate(param, HttpRequestUtils.getOperator(request));
    }

    @PutMapping("/checkMeta")
    @ResponseBody
    @ApiOperation(value = "元数据校验接口", notes = "")
    public Result<Void> checkMeta() {
        return Result.build(templatePhyManager.checkMeta());
    }

    @PutMapping("/dcdr")
    @ResponseBody
    @ApiOperation(value = "创建dcdr链路", notes = "")
    public Result<Void> createDcdr(HttpServletRequest request,
                             @RequestBody TemplatePhysicalDCDRDTO param) throws AdminOperateException {
        if (param.getPhysicalId() != null && CollectionUtils.isEmpty(param.getPhysicalIds())) {
            param.setPhysicalIds(Collections.singletonList(param.getPhysicalId()));
        }

        if (StringUtils.isNotBlank(param.getReplicaCluster()) && CollectionUtils.isEmpty(param.getReplicaClusters())) {
            param.setReplicaClusters(Collections.singletonList(param.getReplicaCluster()));
        }

        return templateDcdrManager.createPhyDcdr(param, HttpRequestUtils.getOperator(request));
    }

    @DeleteMapping("/dcdr")
    @ResponseBody
    @ApiOperation(value = "删除dcdr链路", notes = "")
    public Result<Void> deleteDcdr(HttpServletRequest request,
                             @RequestBody TemplatePhysicalDCDRDTO param) throws AdminOperateException {
        return templateDcdrManager.deletePhyDcdr(param, HttpRequestUtils.getOperator(request));
    }

    @PutMapping("/batchUpdateIndexRack")
    @ResponseBody
    @ApiOperation(value = "批量修改索引rack", notes = "")
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "String", name = "cluster", value = "集群", required = true),
                         @ApiImplicitParam(paramType = "query", dataType = "String", name = "indices", value = "索引", required = true),
                         @ApiImplicitParam(paramType = "query", dataType = "String", name = "rack", value = "rack", required = true) })
    public Result<Void> batchUpdateIndexRack(HttpServletRequest request, @RequestParam(value = "cluster") String cluster,
                                       @RequestParam(value = "indices") String indices,
                                       @RequestParam(value = "rack") String rack) throws AdminOperateException {
        return Result
            .build(esIndexService.syncBatchUpdateRack(cluster, Lists.newArrayList(indices.split(",")), rack, 3));
    }

    @GetMapping("")
    @ResponseBody
    @ApiOperation(value = "获取物理模板列表接口", notes = "")
    public Result<List<IndexTemplatePhysicalVO>> getPhyTemplates(@RequestParam IndexTemplatePhysicalDTO param) {
        return Result
            .buildSucc(ConvertUtil.list2List(templatePhyService.getByCondt(param), IndexTemplatePhysicalVO.class));
    }
}
