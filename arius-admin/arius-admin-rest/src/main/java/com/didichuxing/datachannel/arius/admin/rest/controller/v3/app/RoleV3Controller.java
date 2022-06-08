package com.didichuxing.datachannel.arius.admin.rest.controller.v3.app;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.core.component.RoleTool;
import com.didiglobal.logi.security.common.vo.user.UserBriefVO;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 角色v3控制器
 *
 * @author shizeying
 * @date 2022/06/08
 */
@RestController
@RequestMapping({ V3 + "/role" })
@Api(tags = "角色判断获取 (REST)")
public class RoleV3Controller {
    @Autowired
    private RoleTool roleTool;
    
    @GetMapping("is-admin")
    @ResponseBody
    @ApiOperation(value = "判断是否为管理员")
    public Result<Void> isSuper(HttpServletRequest request) {
        final String operator = HttpRequestUtil.getOperator(request);
        if (!roleTool.isAdmin(operator)) {
            return Result.buildFail("当前角色非管理员");
        }
        return Result.buildSucc();
    }
    
    @GetMapping()
    @ResponseBody
    @ApiOperation(value = "获取管理员列表")
    public Result<List<UserBriefVO>> superList(HttpServletRequest request) {
        return Result.buildSucc(roleTool.adminList());
    }
}