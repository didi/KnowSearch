package com.didichuxing.datachannel.arius.admin.util;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.app.AppDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.app.AppPO;

import java.util.stream.Stream;

public class CustomDataSource {

    public static <T> Stream<T> fromJSON(String json, Class<T> cls) {
        return Stream.of(JSON.parseObject(json, cls));
    }

    public static Stream<AppPO> appPOSource() {
        return fromJSON("{\"id\": null,\"name\": \"test\",\"isRoot\": 1,\"verifyCode\": \"1\",\"department\": \"1\",\"departmentId\": \"1\",\"responsible\": \"1\",\"memo\": \"1\",\"queryThreshold\": 100,\"cluster\": \"\",\"searchType\": 0,\"dataCenter\": \"\"}", AppPO.class);
    }

    public static Stream<AppDTO> appDTOSource() {
        return fromJSON("{\"id\": null,\"name\": \"test\",\"isRoot\": 1,\"verifyCode\": \"1\",\"department\": \"1\",\"departmentId\": \"1\",\"responsible\": \"1\",\"memo\": \"1\",\"queryThreshold\": 100,\"cluster\": \"\",\"searchType\": 0,\"dataCenter\": \"\"}", AppDTO.class);
    }

}
