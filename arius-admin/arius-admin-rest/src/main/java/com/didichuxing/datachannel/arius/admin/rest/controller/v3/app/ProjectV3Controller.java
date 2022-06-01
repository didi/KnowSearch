package com.didichuxing.datachannel.arius.admin.rest.controller.v3.app;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.core.component.RoleTool;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 项目v3控制器
 *
 * @author shizeying
 * @date 2022/06/01
 */
@RestController
@RequestMapping({ V3 + "/project" })
@Api(tags = "超级应用获取 (REST)")
public class ProjectV3Controller {
    @Autowired
    private RoleTool roleTool;
    
    @GetMapping
    @ResponseBody
    @ApiOperation(value = "获取超级应用id")
    public Result<Integer> listESUserByProjectId(HttpServletRequest request) {
        final String operator = HttpRequestUtil.getOperator(request);
        if (!roleTool.isAdmin(operator)) {
            return Result.buildFail("当前角色非管理员");
        }
        return Result.buildSucc(AuthConstant.SUPER_PROJECT_ID);
    }
}