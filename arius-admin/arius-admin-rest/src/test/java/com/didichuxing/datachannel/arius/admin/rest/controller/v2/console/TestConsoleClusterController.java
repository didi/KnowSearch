package com.didichuxing.datachannel.arius.admin.rest.controller.v2.console;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

public class TestConsoleClusterController extends AriusAdminApplicationTests {

    private static final String URL_CONSOLE_INDEX = "/v2/console/cluster";

    @Test
    public void testListAll(){
        ResponseEntity<Result> resp = restTemplate.getForEntity(baseUrl + URL_CONSOLE_INDEX + "/listAllPO", Result.class);
    }


    @Test
    public void testMachinespecList(){
        ResponseEntity<Result> resp = restTemplate.getForEntity(baseUrl + URL_CONSOLE_INDEX + "/machinespec/list", Result.class);
    }
}
