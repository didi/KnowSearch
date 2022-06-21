package com.didichuxing.datachannel.arius.admin.rest.controller.v3.op.app;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ConsoleAppVO;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.didichuxing.datachannel.arius.admin.biz.app.AppManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author linyunan
 * @date 2021-04-28
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

    @GetMapping("/ids-names")
    @ResponseBody
    @ApiOperation(value = "获取APP主键和名称列表接口")
    public Result<List<Tuple<Integer, String>>> listIdsAndNames() {
        // TODO: app_id需要更换为project_id
        Result<List<ConsoleAppVO>> ret = appManager.list();
        List<Tuple<Integer, String>> res = Lists.newArrayList();
        if (ret.success()) {
            List<ConsoleAppVO> data = ret.getData();
            for (ConsoleAppVO appVO : data) {
                Tuple<Integer, String> ids2NamesTuple = new Tuple<>();
                ids2NamesTuple.setV1(appVO.getId());
                ids2NamesTuple.setV2(appVO.getName());
                res.add(ids2NamesTuple);
            }
            return Result.buildSucc(res);
        }

        return Result.buildFail("操作失败, 请确认服务是否正常");
    }

}
