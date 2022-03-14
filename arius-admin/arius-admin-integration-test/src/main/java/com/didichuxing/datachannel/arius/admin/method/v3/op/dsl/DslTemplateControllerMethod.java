package com.didichuxing.datachannel.arius.admin.method.v3.op.dsl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.didichuxing.datachannel.arius.admin.AriusClient;
import com.didichuxing.datachannel.arius.admin.client.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.dsl.template.DslTemplateConditionDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.metrics.GatewayJoinQueryDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.DslTemplateVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.GatewayJoinVO;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_OP;

/**
 * @author cjm
 */
public class DslTemplateControllerMethod {

    public static final String DSL_TEMPLATE = V3_OP + "/dsl/template";

    public static PaginationResult<DslTemplateVO> page(DslTemplateConditionDTO query) throws IOException {
        String path = String.format("%s/page", DSL_TEMPLATE);
        return JSON.parseObject(AriusClient.post(path, query), new TypeReference<PaginationResult<DslTemplateVO>>(){});
    }

    public static Result<DslTemplateVO> getDetailTemplate(String dslTemplateMd5) throws IOException {
        String path = String.format("%s/detail/%s", DSL_TEMPLATE, dslTemplateMd5);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<DslTemplateVO>>(){});
    }

    public static Result<Boolean> changeStatus(String dslTemplateMd5) throws IOException {
        String path = String.format("%s/change/status/%s", DSL_TEMPLATE, dslTemplateMd5);
        return JSON.parseObject(AriusClient.put(path), new TypeReference<Result<Boolean>>(){});
    }

    public static Result<Boolean> updateQueryLimit(List<String> dslTemplateMd5List, Double queryLimit) throws IOException {
        String path = String.format("%s/update/queryLimit", DSL_TEMPLATE);
        Map<String, Object> params = new HashMap<>();
        String dslTemplateMd5Strs = String.join(",", dslTemplateMd5List);
        params.put("dslTemplateMd5List", dslTemplateMd5Strs);
        params.put("queryLimit", queryLimit);
        return JSON.parseObject(AriusClient.put(path, params, null), new TypeReference<Result<Boolean>>(){});
    }

    public static Result<List<GatewayJoinVO>> slowList(GatewayJoinQueryDTO queryDTO) throws IOException {
        String path = String.format("%s/slow/list", DSL_TEMPLATE);
        return JSON.parseObject(AriusClient.post(path, queryDTO), new TypeReference<Result<List<GatewayJoinVO>>>(){});
    }

    public static Result<List<GatewayJoinVO>> errorList(GatewayJoinQueryDTO queryDTO) throws IOException {
        String path = String.format("%s/error/list", DSL_TEMPLATE);
        return JSON.parseObject(AriusClient.post(path, queryDTO), new TypeReference<Result<List<GatewayJoinVO>>>(){});
    }
}
