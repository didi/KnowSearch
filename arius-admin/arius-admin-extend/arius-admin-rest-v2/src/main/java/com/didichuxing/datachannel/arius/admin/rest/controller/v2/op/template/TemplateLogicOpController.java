package com.didichuxing.datachannel.arius.admin.rest.controller.v2.op.template;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_OP;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateLogicDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.OpLogicTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.TemplateLogicService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(V2_OP + "/template/logic")
@Api(tags = "es集群逻辑模板接口(REST)")
public class TemplateLogicOpController {

    @Autowired
    private TemplateLogicService templateLogicService;

    @PostMapping("/list")
    @ResponseBody
    @ApiOperation(value = "获取逻辑模板列表接口" )
    @Deprecated
    public Result<List<OpLogicTemplateVO>> list(@RequestBody IndexTemplateLogicDTO param) {
        return Result
            .buildSucc(ConvertUtil.list2List(templateLogicService.getLogicTemplates(param), OpLogicTemplateVO.class));
    }

    @RequestMapping(path = "/blockRead", method = RequestMethod.PUT)
    @ResponseBody
    @ApiOperation(value = "是否禁读" )
    @Deprecated
    public Result updateBlockReadState(HttpServletRequest request, @RequestBody IndexTemplateLogicDTO param) {
        return templateLogicService.updateBlockReadState(param.getId(), param.getBlockRead(),
            HttpRequestUtils.getOperator(request));
    }

    @RequestMapping(path = "/blockWrite", method = RequestMethod.PUT)
    @ResponseBody
    @ApiOperation(value = "是否禁写" )
    @Deprecated
    public Result updateBlockWriteState(HttpServletRequest request, @RequestBody IndexTemplateLogicDTO param) {
        return templateLogicService.updateBlockWriteState(param.getId(), param.getBlockWrite(),
            HttpRequestUtils.getOperator(request));
    }

}
