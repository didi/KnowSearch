package com.didichuxing.datachannel.arius.admin.rest.controller.v3.app;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.core.component.RoleTool;
import com.didiglobal.logi.security.common.vo.user.UserBriefVO;
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
    private RoleTool roleTool;
    
    @GetMapping()
    @ResponseBody
    @ApiOperation(value = "获取管理员列表")
    public Result<List<UserBriefVO>> getAdminList(HttpServletRequest request) {
        return Result.buildSucc(roleTool.getAdminList());
    }
}