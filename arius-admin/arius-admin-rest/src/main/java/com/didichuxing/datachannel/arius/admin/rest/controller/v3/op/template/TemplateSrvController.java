package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.template;

import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.TemplateSrvManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplateWithSrvConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.TemplateSrv;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.TemplateWithSrvVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

/**
 * @author chengxiang
 * @date 2022/5/18
 */
@RestController
@RequestMapping(V3_OP + "/template/srv")
@Api(tags = "模板服务接口")
public class TemplateSrvController {

    @Autowired
    private TemplateSrvManager templateSrvManager;


    @GetMapping("/{templateId}/unavailableSrv")
    @ResponseBody
    @ApiOperation(value = "获取不可用的模板服务")
    public Result<List<TemplateSrv>> getTemplateUnavailableSrv(@PathVariable Integer templateId) {
        return templateSrvManager.getTemplateUnavailableSrv(templateId);
    }

    @PostMapping("/page")
    @ResponseBody
    @ApiOperation(value = "分页模糊查询模板服务")
    public PaginationResult<TemplateWithSrvVO> pageGetTemplateWithSrv(@RequestBody TemplateWithSrvConditionDTO condition) {
        return templateSrvManager.pageGetTemplateWithSrv(condition);
    }
}
