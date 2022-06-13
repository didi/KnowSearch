package com.didichuxing.datachannel.arius.admin.rest.controller.v3.app;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.core.component.RoleTool;
import com.didiglobal.logi.security.common.vo.user.UserBriefVO;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.swing.text.html.Option;
import org.apache.zookeeper.Op;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    
    @GetMapping("/is-admin")
    @ResponseBody
    @ApiOperation(value = "判断是否为管理员")
    public Result<Void> isAdmin(HttpServletRequest request) {
        final String operator = HttpRequestUtil.getOperator(request);
        if (!roleTool.isAdmin(operator)) {
            return Result.buildFail("当前角色非管理员");
        }
        return Result.buildSucc();
    }
    
    @PostMapping("/is-admin/")
    @ResponseBody
    @ApiOperation(value = "通过指定的role ids返回管理员列表id")
    public Result<List<Integer>> isAdmin(HttpServletRequest request,@RequestBody List<Integer> roleIds) {
        final List<Integer> ids = Optional.ofNullable(roleIds).orElse(Lists.newArrayList()).stream()
                .filter(AuthConstant.ADMIN_ROLE_ID::equals).collect(Collectors.toList());
    
        return Result.buildSucc(ids);
    }
    
    @GetMapping()
    @ResponseBody
    @ApiOperation(value = "获取管理员列表")
    public Result<List<UserBriefVO>> getAdminList(HttpServletRequest request) {
        return Result.buildSucc(roleTool.getAdminList());
    }
}