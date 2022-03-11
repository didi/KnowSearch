package com.didichuxing.datachannel.arius.admin.core.service.extend.login;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.user.AriusUserInfoDTO;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppUserInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusUserInfoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author cjm
 */
@Transactional
@Rollback
public class LoginServiceTest extends AriusAdminApplicationTest {

    @Autowired
    private LoginService loginService;

    @MockBean
    private AriusUserInfoService ariusUserInfoService;

    @MockBean
    private AppUserInfoService appUserInfoService;

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
        Assertions.assertNotEquals(0, register.getCode());
        Mockito.when(appUserInfoService.recordAppidAndUser(Mockito.anyInt(), Mockito.any())).thenReturn(true);
        Mockito.when(ariusUserInfoService.save(Mockito.any())).thenReturn(Result.buildSucc(1L));
        Assertions.assertTrue(loginService.register(userInfoDTO, 1).success());
    }
}
