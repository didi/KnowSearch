package com.didichuxing.datachannel.arius.admin.rest.controller;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("healthController")
@RequestMapping("/health")
@Api("web应用探活接口(REST)")
public class HealthController {

    /**
     * 探活接口
     *
     * @return
     */
    @GetMapping("")
    @ResponseBody
    @ApiOperation(value = "探活接口", notes = "")
    public Result<String> demo() {
        return Result.buildSuccWithMsg("let's go");
    }

}
