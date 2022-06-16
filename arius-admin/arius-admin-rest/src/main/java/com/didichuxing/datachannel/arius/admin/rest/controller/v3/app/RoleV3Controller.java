package com.didichuxing.datachannel.arius.admin.rest.controller.v3.app;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import com.didichuxing.datachannel.arius.admin.biz.app.RoleExtendManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.core.component.RoleTool;
import com.didiglobal.logi.security.common.vo.user.UserBriefVO;
import com.didiglobal.logi.security.exception.LogiSecurityException;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    private RoleTool          roleTool;
    @Autowired
    private RoleExtendManager roleExtendManager;
    
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
    
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除角色", notes = "根据角色id删除角色")
    @ApiImplicitParam(name = "id", value = "角色id", dataType = "int", required = true)
    public Result<Void> delete(@PathVariable Integer id, HttpServletRequest request) {
       
        return roleExtendManager.deleteRoleByRoleId(id, request);
    }
    @GetMapping("/admin")
    @ResponseBody
    @ApiOperation(value = "返回管理员列表id")
    public Result<List<Integer>> getAdminRoleIds(HttpServletRequest request) {
        return Result.buildSucc(Lists.newArrayList(AuthConstant.ADMIN_ROLE_ID));
    }
    
  
}