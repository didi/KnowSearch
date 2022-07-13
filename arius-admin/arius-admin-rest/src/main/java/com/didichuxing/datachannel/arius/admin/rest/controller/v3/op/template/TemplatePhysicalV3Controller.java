package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.template;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.didichuxing.datachannel.arius.admin.biz.template.TemplatePhyManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplatePhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplatePhysicalUpgradeDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.IndexTemplatePhysicalVO;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didiglobal.logi.security.util.HttpRequestUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Created by linyunan on 2021-07-30
 */
@RestController
@RequestMapping(V3 + "/templates/physical")
@Api(tags = "索引模板管理接口(REST)")
public class TemplatePhysicalV3Controller {

    @Autowired
    private TemplatePhyManager templatePhyManager;

    @GetMapping("/{logicId}")
    @ResponseBody
    @ApiOperation(value = "根据逻辑模板Id获取多个物理模板信息", notes = "")
    public Result<List<IndexTemplatePhysicalVO>> list(@PathVariable("logicId") Integer logicId) {
        return templatePhyManager.getTemplatePhies(logicId);
    }

    @GetMapping("/names")
    @ResponseBody
    @ApiOperation(value = "获取物理模板名称列表接口")
    public Result<List<String>> listTemplatePhyNames(HttpServletRequest request) {
        return Result.buildSucc(templatePhyManager.getTemplatePhyNames(HttpRequestUtil.getProjectId(request)));
    }

    @GetMapping("/{templatePhyId}/copy-cluster-phy-names")
    @ResponseBody
    @ApiOperation(value = "获取物理模板可复制的物理集群名称")
    public Result<List<String>> getAppNodeNames(@PathVariable Long templatePhyId) {
        return Result.buildSucc(templatePhyManager.getCanCopyTemplatePhyClusterPhyNames(templatePhyId));
    }

    @PutMapping("")
    @ResponseBody
    @ApiOperation(value = "编辑多个物理模板接口", notes = "")
    public Result<Boolean> multipleEdit(HttpServletRequest request,
                                        @RequestBody List<IndexTemplatePhyDTO> params) throws ESOperateException {
        return templatePhyManager.editMultipleTemplate(params, HttpRequestUtil.getOperator(request));
    }

    @PostMapping("/upgrade")
    @ResponseBody
    @ApiOperation(value = "升级多个物理模板接口", notes = "")
    public Result<Boolean> multipleUpgrade(HttpServletRequest request,
                                           @RequestBody List<TemplatePhysicalUpgradeDTO> params) throws ESOperateException {
        return templatePhyManager.upgradeMultipleTemplate(params, HttpRequestUtil.getOperator(request),
            HttpRequestUtil.getProjectId(request));
    }
}