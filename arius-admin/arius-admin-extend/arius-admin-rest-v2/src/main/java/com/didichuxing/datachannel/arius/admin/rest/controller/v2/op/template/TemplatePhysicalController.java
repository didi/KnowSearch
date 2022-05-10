package com.didichuxing.datachannel.arius.admin.rest.controller.v2.op.template;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_OP;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.didichuxing.datachannel.arius.admin.biz.template.TemplatePhyManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplatePhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplatePhysicalCopyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ConsoleTemplatePhyVO;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;

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
    private TemplatePhyManager  templatePhyManager;

    /**
     * @param param
     * @param request
     * @return
     *
     */
    @PostMapping("/list")
    @ResponseBody
    @ApiOperation(value = "获取物理模板列表接口")
    public Result<List<ConsoleTemplatePhyVO>> list(@RequestBody IndexTemplatePhyDTO param,
                                                   HttpServletRequest request) {
        return Result.buildSucc(templatePhyManager.getConsoleTemplatePhyVOS(param, HttpRequestUtils.getAppId(request)));
    }

    @DeleteMapping("/del")
    @ResponseBody
    @ApiOperation(value = "删除物理模板接口" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "physicalId", value = "物理模板ID", required = true) })
    public Result<Void> delete(HttpServletRequest request,
                         @RequestParam(value = "physicalId") Long physicalId) throws ESOperateException {
        return templatePhyManager.delTemplate(physicalId, HttpRequestUtils.getOperator(request));
    }

    @PutMapping("/copy")
    @ResponseBody
    @ApiOperation(value = "复制物理模板接口" )
    public Result<Void> copy(HttpServletRequest request,
                             @RequestBody TemplatePhysicalCopyDTO param) throws AdminOperateException {
        return templatePhyManager.copyTemplate(param, HttpRequestUtils.getOperator(request));
    }

}
