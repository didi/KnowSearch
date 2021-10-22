package com.didichuxing.datachannel.arius.admin.app;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.AriusClient;
import com.didichuxing.datachannel.arius.admin.BaseContextTests;
import com.didichuxing.datachannel.arius.admin.RandomFilledBean;
import com.didichuxing.datachannel.arius.admin.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.app.AppDTO;
import com.didichuxing.datachannel.arius.admin.constant.RequestPathOP;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class AppTest extends BaseContextTests {

    private AppDTO appDTO;

    @BeforeAll
    public void setData() {

    }

    @BeforeEach
    public void setApp() {
        appDTO = RandomFilledBean.getRandomBeanOfType(AppDTO.class);
        appDTO.setIsRoot(1);
        appDTO.setIsActive(1);
        appDTO.setSearchType(0);
    }

    @Test
    public void loginTest() throws IOException {
        Result result = new AriusClient().post(RequestPathOP.APP_ADD, appDTO);
        System.out.println(JSON.toJSONString(result));
    }
}
