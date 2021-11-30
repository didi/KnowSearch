package com.didichuxing.datachannel.arius.admin.core.service.extend.login;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.account.LoginDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.user.AriusUserInfoDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author cjm
 */
@Transactional
@Rollback
public class LoginServiceTest extends AriusAdminApplicationTests {

    @Autowired
    private LoginService loginService;

    @Test
    void loginAuthenticateTest() {
        // 涉及 HttpServletRequest 的封装，通过集成测试
    }

    @Test
    void logoutTest() {
        // 涉及 HttpServletRequest 的封装，通过集成测试
    }

    @Test
    void interceptorCheckTest() {
        // 涉及 HttpServletRequest 的封装，通过集成测试
    }

    /**
     *         if (AriusObjUtils.isNull(userInfoDTO)) {
     *             return Result.buildParamIllegal("用户信息为空");
     *         }
     *         if (AriusObjUtils.isNull(userInfoDTO.getName())) {
     *             return Result.buildParamIllegal("名字为空");
     *         }
     *         if (AriusObjUtils.isNull(userInfoDTO.getPassword())) {
     *             return Result.buildParamIllegal("密码为空");
     *         }
     *         if (AriusObjUtils.isNull(userInfoDTO.getDomainAccount())) {
     *             return Result.buildParamIllegal("域账号为空");
     *         }
     *         if (AriusObjUtils.isNull(userInfoDTO.getStatus())) {
     *             return Result.buildParamIllegal("状态为空");
     *         }
     */

    @Test
    void registerTest() {
        AriusUserInfoDTO userInfoDTO = new AriusUserInfoDTO();
        // 所有信息为空
        Result<Long> register = loginService.register(userInfoDTO, 1);
        Assertions.assertNotEquals(0, register.getCode());
        // 名字不为空
        userInfoDTO.setName("test_test");
        register = loginService.register(userInfoDTO, 1);
        Assertions.assertNotEquals(0, register.getCode());
        // 名字、密码不为空
        userInfoDTO.setPassword("123456");
        register = loginService.register(userInfoDTO, 1);
        Assertions.assertNotEquals(0, register.getCode());
        // 名字、密码、域账号不为空
        userInfoDTO.setDomainAccount("qwertyu");
        register = loginService.register(userInfoDTO, 1);
        Assertions.assertNotEquals(0, register.getCode());
        // 名字、密码、域账号、状态不为空
        userInfoDTO.setStatus(1);
        register = loginService.register(userInfoDTO, 1);
        Assertions.assertNotEquals(0, register.getCode());
        // 名字、密码、域账号、状态、邮箱不为空
        userInfoDTO.setEmail("123@test.com");
        register = loginService.register(userInfoDTO, 1);
        Assertions.assertNotEquals(0, register.getCode());
        // 名字、密码、域账号、状态、邮箱、mobile不为空
        userInfoDTO.setMobile("1239993333");
        register = loginService.register(userInfoDTO, 1);
        Assertions.assertNotEquals(0, register.getCode());
        userInfoDTO.setRole(2);
        register = loginService.register(userInfoDTO, 1);
        Assertions.assertEquals(0, register.getCode());
    }
}
