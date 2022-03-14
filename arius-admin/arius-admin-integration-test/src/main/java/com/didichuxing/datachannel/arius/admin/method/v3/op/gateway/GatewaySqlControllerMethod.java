package com.didichuxing.datachannel.arius.admin.method.v3.op.gateway;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.didichuxing.datachannel.arius.admin.AriusClient;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;

import java.io.IOException;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

/**
 * @author cjm
 */
public class GatewaySqlControllerMethod {

    public static final String GATEWAY_SQL = V3_OP + "/gateway/sql";

    public static Result<String> directSqlSearchTest(String phyClusterName, String sql) throws IOException {
        String path = String.format("%s/%s", GATEWAY_SQL, phyClusterName);
        return JSON.parseObject(AriusClient.post(path, sql), new TypeReference<Result<String>>(){});
    }

    public static Result<String> sqlExplainTest(String sql) throws IOException {
        String path = String.format("%s/explain", GATEWAY_SQL);
        return JSON.parseObject(AriusClient.post(path, sql), new TypeReference<Result<String>>(){});
    }
}
