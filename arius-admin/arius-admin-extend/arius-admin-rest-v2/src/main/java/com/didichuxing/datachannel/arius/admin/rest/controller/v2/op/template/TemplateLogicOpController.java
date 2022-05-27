package com.didichuxing.datachannel.arius.admin.rest.controller.v2.op.template;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_OP;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.OpLogicTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(V2_OP + "/template/logic")
@Api(tags = "es集群逻辑模板接口(REST)")
public class TemplateLogicOpController {

    @Autowired
    private IndexTemplateService indexTemplateService;

    @PostMapping("/list")
    @ResponseBody
    @ApiOperation(value = "获取逻辑模板列表接口" )

    public Result<List<OpLogicTemplateVO>> list(@RequestBody IndexTemplateDTO param) {
        return Result
            .buildSucc(ConvertUtil.list2List(indexTemplateService.getLogicTemplates(param), OpLogicTemplateVO.class));
    }

    @RequestMapping(path = "/blockRead", method = RequestMethod.PUT)
    @ResponseBody
    @ApiOperation(value = "是否禁读" )

    public Result updateBlockReadState(HttpServletRequest request, @RequestBody IndexTemplateDTO param) {
        return indexTemplateService.updateBlockReadState(param.getId(), param.getBlockRead(),
            HttpRequestUtil.getOperator(request));
    }

    @RequestMapping(path = "/blockWrite", method = RequestMethod.PUT)
    @ResponseBody
    @ApiOperation(value = "是否禁写" )

    public Result updateBlockWriteState(HttpServletRequest request, @RequestBody IndexTemplateDTO param) {
        return indexTemplateService.updateBlockWriteState(param.getId(), param.getBlockWrite(),
            HttpRequestUtil.getOperator(request));
    }

}