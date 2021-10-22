package com.didichuxing.datachannel.arius.admin.rest.controller.v3.normal;

import com.didichuxing.datachannel.arius.admin.biz.extend.department.DepartmentManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_NORMAL;

@RestController
@RequestMapping(V3_NORMAL + "/depart")
@Api(tags = "Normal-部门相关接口(REST)")
public class NormalDepartController {

    @Autowired
    private DepartmentManager departmentManager;

    @GetMapping("")
    @ResponseBody
    @ApiOperation(value = "获取部门列表")
    public Result listDepartments() {
        return departmentManager.listDepartments();
    }
}
