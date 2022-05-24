package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.template.manage;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.srv.TemplateQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.manage.TemplateWithManageVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

/**
 * @author chengxiang
 * @date 2022/5/24
 */
@RestController
@RequestMapping(V3_OP + "/template/manage")
@Api(tags = "模板管理接口")
public class TemplateManageController {

    @PostMapping("/page")
    @ResponseBody
    @ApiOperation(value = "分页模糊查询「模板管理」")
    public PaginationResult<TemplateWithManageVO> pageGetTemplateWithManage(@RequestBody TemplateQueryDTO condition) {
        return null;
    }


}
