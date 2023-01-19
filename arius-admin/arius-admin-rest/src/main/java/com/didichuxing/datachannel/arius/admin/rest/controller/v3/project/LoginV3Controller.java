package com.didichuxing.datachannel.arius.admin.rest.controller.v3.project;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_SECURITY;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.didichuxing.datachannel.arius.admin.biz.project.LoginManager;
import com.didiglobal.knowframework.security.common.Result;
import com.didiglobal.knowframework.security.common.constant.Constants;
import com.didiglobal.knowframework.security.common.dto.account.AccountLoginDTO;
import com.didiglobal.knowframework.security.common.vo.user.UserBriefVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author cjm
 */
@RestController
@Api(value = "登录相关API接口", tags = { "权限相关接口" })
@RequestMapping(V3_SECURITY + Constants.ACCOUNT_LOGIN)
public class LoginV3Controller {

    @Autowired
    private LoginManager loginManager;

    @PostMapping("/login")
    @ApiOperation(value = "登录检查", notes = "检查SSO返回的Code")
    public Result<UserBriefVO> login(HttpServletRequest request, HttpServletResponse response,
                                     @RequestBody AccountLoginDTO loginDTO) {

        return loginManager.verifyLogin(loginDTO, request, response);
    }

    @PostMapping("/logout")
    @ApiOperation(value = "登出", notes = "检查SSO返回的Code")
    public Result<Boolean> logout(HttpServletRequest request, HttpServletResponse response) {
        return loginManager.logout(request, response);
    }
}