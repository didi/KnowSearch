package com.didichuxing.datachannel.arius.admin.v3.op.app;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.didichuxing.datachannel.arius.admin.AriusClient;

import com.didichuxing.datachannel.arius.admin.BaseContextTest;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ConsoleAppVO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class AppV3Test extends BaseContextTest {

    @Test
    public void listNamesTest() throws IOException {
        Result<List<ConsoleAppVO>> result = JSON.parseObject(AriusClient.get("/v3/op/app/list"), new TypeReference<Result<List<ConsoleAppVO>>>(){});
        Assertions.assertTrue(result.success());
    }
}
