package com.didichuxing.datachannel.arius.admin.rest.controller.v3.project;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_SECURITY;

import com.didichuxing.datachannel.arius.admin.biz.project.PermissionExtendManager;
import com.didiglobal.logi.security.common.Result;
import com.didiglobal.logi.security.common.vo.permission.PermissionTreeVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 权限点v3控制器
 *
 * @author shizeying
 * @date 2022/06/14
 */
@RestController
@Api(value = "权限相关API接口", tags = { "权限相关API接口" })
@RequestMapping(V3_SECURITY + "/permission")
public class PermissionV3Controller {
	@Autowired
	private PermissionExtendManager permissionExtendManager;
	
	@GetMapping("/resource-owner")
	@ResponseBody
	@ApiOperation(value = "获取资源own角色权限树", notes = "以树的形式返回所有权限")
	public Result<PermissionTreeVO> isAdmin(HttpServletRequest request) {
		
		return permissionExtendManager.buildPermissionTreeByResourceOwn();
	}
	
}