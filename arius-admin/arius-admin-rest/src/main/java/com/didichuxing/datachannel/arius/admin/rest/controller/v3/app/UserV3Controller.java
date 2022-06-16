package com.didichuxing.datachannel.arius.admin.rest.controller.v3.app;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import com.didichuxing.datachannel.arius.admin.biz.app.UserExtendManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.core.component.RoleTool;
import com.didiglobal.logi.security.common.dto.user.UserDTO;
import com.didiglobal.logi.security.common.vo.user.UserBriefVO;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户v3控制器
 *
 * @author shizeying
 * @date 2022/06/14
 */
@RestController
@RequestMapping({ V3 + "/user" })
@Api(tags = "用户相关获取 (REST)")
public class UserV3Controller {
    @Autowired
    private RoleTool          roleTool;
    @Autowired
    private UserExtendManager userManager;
    
    @GetMapping()
    @ResponseBody
    @ApiOperation(value = "获取管理员列表")
    public Result<List<UserBriefVO>> getAdminList(HttpServletRequest request) {
        return Result.buildSucc(roleTool.getAdminList());
    }
    
    @PutMapping("/add")
    @ResponseBody
    @ApiOperation(value = "用户新增接口，暂时没有考虑权限", notes = "")
    public Result<Void> add(HttpServletRequest request,
                                                                @RequestBody UserDTO param) {
        return userManager.addUser(param, HttpRequestUtil.getOperator(request));
    }
}