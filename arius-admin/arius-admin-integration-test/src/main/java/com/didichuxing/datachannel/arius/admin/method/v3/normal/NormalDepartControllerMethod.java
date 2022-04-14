package com.didichuxing.datachannel.arius.admin.method.v3.normal;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.didichuxing.datachannel.arius.admin.AriusClient;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;

import java.io.IOException;


import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_NORMAL;

/**
 * @author xuguang
 * @Date 2022/3/31
 */
public class NormalDepartControllerMethod {
    public static final String Accout = V3_NORMAL + "/depart";

    public static Result<String> listDepartments() throws IOException{
        return JSON.parseObject(AriusClient.get(Accout), new TypeReference<Result<String>>(){});
    }
}
