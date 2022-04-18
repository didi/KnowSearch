package com.didichuxing.datachannel.arius.admin.rest.controller.v3.thirdpart;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_THIRD_PART_SSO;

import com.didichuxing.datachannel.arius.admin.biz.extend.account.LoginManager;
import com.didichuxing.datachannel.arius.admin.common.util.HttpRequestUtils;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.account.LoginDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.user.AriusUserInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.component.RSATool;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(V3_THIRD_PART_SSO)
@Api(tags = "SSO-Login相关接口(REST)")
public class LoginController {

    @Autowired
    private LoginManager loginManager;

    @Autowired
    private RSATool      rsaTool;

    @PostMapping("/login")
    @ResponseBody
    @ApiOperation(value = "登录", notes = "无需走SSO逻辑")
    public Result<Boolean> login(HttpServletRequest request, HttpServletResponse response, @RequestBody LoginDTO loginDTO) {
        return loginManager.loginAuthenticateAndGetUserInfo(request, response, loginDTO);
    }

    @DeleteMapping("/logout")
    @ResponseBody
    @ApiOperation(value = "登出")
    public Result<Boolean> logout(HttpServletRequest request, HttpServletResponse response) {
        return loginManager.logout(request, response);
    }

    @PostMapping("/register")
    @ResponseBody
    @ApiOperation(value = "注册")
    public Result<Long> register(@RequestBody AriusUserInfoDTO userInfoDTO, HttpServletRequest request) {
        return loginManager.register(userInfoDTO, HttpRequestUtils.getAppId(request));
    }

    @GetMapping("/publicKey")
    @ResponseBody
    @ApiOperation(value = "获取公钥")
    public Result<String> getPublicKey() {
        return Result.build(Boolean.TRUE, rsaTool.getPublicKey());
    }
}
