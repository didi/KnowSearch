package com.didichuxing.datachannel.arius.admin.login;

import com.alibaba.fastjson.util.TypeUtils;
import com.didichuxing.datachannel.arius.admin.AriusClient;
import com.didichuxing.datachannel.arius.admin.BaseContextTests;
import com.didichuxing.datachannel.arius.admin.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.user.AriusUserInfoDTO;
import com.didichuxing.datachannel.arius.admin.constant.RequestPathThirdpart;
import com.didichuxing.datachannel.arius.admin.source.AriusDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class LoginTest extends BaseContextTests {
    private static Long userId;

    /**
     * 登录采用rsa加密
     *
     * @throws Exception
     */
    @BeforeAll
    public static void setData() throws Exception {
        AriusUserInfoDTO dto = AriusDataSource.ariusUserInfo();
        Result result = new AriusClient().post(RequestPathThirdpart.REGISTER, dto);
        userId = TypeUtils.castToLong(result.getData());
    }

    @Test
    public void registerTest() {

    }
}
