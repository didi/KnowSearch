package com.didichuxing.datachannel.arius.admin.rest.controller.v2.op.template;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_OP;

import com.didichuxing.datachannel.arius.admin.biz.template.TemplatePhyManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplatePhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplatePhysicalCopyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ConsoleTemplatePhyVO;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

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
    @ApiOperation(value = "获取物理模板列表接口【三方接口】",tags = "【三方接口】")
    public Result<List<ConsoleTemplatePhyVO>> list(@RequestBody IndexTemplatePhyDTO param,
                                                   HttpServletRequest request) {
         return Result.buildFail("接口已经下线：迁移到v3");
        //return Result.buildSucc(templatePhyManager.getConsoleTemplatePhyVOS(param, HttpRequestUtil.getProjectId(request)));
    }

    @DeleteMapping("/del")
    @ResponseBody
    @ApiOperation(value = "删除物理模板接口" )
    @ApiImplicitParams({ @ApiImplicitParam(paramType = "query", dataType = "Long", name = "physicalId", value = "物理模板ID", required = true) })
    public Result<Void> delete(HttpServletRequest request,
                         @RequestParam(value = "physicalId") Long physicalId) throws ESOperateException {
         return Result.buildFail("接口已经下线：迁移到v3");
        //return templatePhyManager.delTemplate(physicalId, HttpRequestUtil.getOperator(request));
    }

    @PutMapping("/copy")
    @ResponseBody
    @ApiOperation(value = "复制物理模板接口" )
    public Result<Void> copy(HttpServletRequest request,
                             @RequestBody TemplatePhysicalCopyDTO param) throws AdminOperateException {
         return Result.buildFail("接口已经下线：迁移到v3");
        //return templatePhyManager.copyTemplate(param, HttpRequestUtil.getOperator(request));
    }

}