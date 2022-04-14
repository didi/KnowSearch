package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.app;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.app.ConsoleAppVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.didichuxing.datachannel.arius.admin.biz.app.AppManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Created by linyunan on 2021-07-30
 */
@RestController
@RequestMapping(V3_OP + "/app")
@Api(tags = "APP相关接口(REST)")
public class AppV3Controller {

    @Autowired
    private AppManager appManager;

    @GetMapping("/list")
    @ResponseBody
    @ApiOperation(value = "获取APP列表接口")
    public Result<List<ConsoleAppVO>> listNames() {
        return appManager.list();
    }

}
