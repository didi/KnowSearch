package com.didichuxing.datachannel.arius.admin.v3.thirdpart;

import com.didichuxing.datachannel.arius.admin.BaseContextTest;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.account.LoginDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.user.AriusUserInfoDTO;
import com.didichuxing.datachannel.arius.admin.method.v3.thirdpart.LoginControllerMethod;
import com.didichuxing.datachannel.arius.admin.source.CustomDataSource;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.io.IOException;


/**
 * @author wuxuan
 * @Date 2022/3/30
 */
public class LoginTests extends BaseContextTest {

    /**
     * 注意这里密码要先进行RSA加密，再进行Base64加密
     */
    String password="hTw1yTAuEifG/HN82zFkHzTK1N2rQ9WCw8QuRgfITAy9aNJ7IccoFQwM11sblbhPmkKHGV+rsbO+rzenRmjwiB7bmyu8kYgNWPZuI5wXYKFeeBPbXXd2NQDM01i9oUDU8KAiN60rY83XSiEm4X2iBVKOgYlq3SEchNkodfsBWts=";

    @Test
    public void testLogin() throws IOException{
        LoginDTO loginDTO=new LoginDTO();
        loginDTO.setDomainAccount("admin");
        loginDTO.setPassword(password);
        Result<Boolean> result= LoginControllerMethod.teslLogin(loginDTO);
        Assert.assertTrue(result.success());
    }

    @Test
    public void testLogout() throws IOException{
        Result<Boolean> result=LoginControllerMethod.testLogout();
        Assert.assertTrue(result.success());
    }

    @Test
    public void testRegister() throws IOException{
        AriusUserInfoDTO ariusUserInfoDTO = CustomDataSource.getariusUserInfoDTOFactory();
        Result<Long> result=LoginControllerMethod.testRegister(ariusUserInfoDTO);
        Assert.assertTrue(result.success());
    }

    @Test
    public void testGetPublicKey() throws IOException{
        Result<String> result=LoginControllerMethod.testGetPublicKey();
        Assert.assertTrue(result.success());
    }
}
