package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esmonitorjob.node;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didiglobal.logi.elasticsearch.client.model.ESActionResponse;
import com.didiglobal.logi.elasticsearch.client.model.RestRequest;
import com.didiglobal.logi.elasticsearch.client.model.RestResponse;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.ActionRequestValidationException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ESNodeStatsRequest extends BaseTimeoutRequest<ESNodeStatsRequest> {

    public static final String INDICES = "indices";
    public static final String OS = "os";
    public static final String PROCESS = "process";
    public static final String JVM = "jvm";
    public static final String THREAD_POOL = "thread_pool";
    public static final String FS = "fs";
    public static final String TRANSPORT = "transport";
    public static final String HTTP = "http";
    public static final String BREAKERS = "breakers";
    public static final String SCRIPT = "script";

    private final Set<String> flags = new HashSet<>();
    private String[] nodesIds = null;
    private String level = null;

    public final ESNodeStatsRequest nodesIds(String... nodesIds) {
        this.nodesIds = nodesIds;
        return this;
    }

    public ESNodeStatsRequest level(String level) {
        this.level = level;
        return this;
    }

    @SuppressWarnings("all")
    public ESNodeStatsRequest all() {
        flags.add(INDICES);
        flags.add(OS);
        flags.add(PROCESS);
        flags.add(JVM);
        flags.add(THREAD_POOL);
        flags.add(FS);
        flags.add(TRANSPORT);
        flags.add(HTTP);
        flags.add(BREAKERS);
        flags.add(SCRIPT);
        return this;
    }

    public ESNodeStatsRequest clear() {
        flags.clear();
        return this;
    }


    public ESNodeStatsRequest flag(String name, boolean isSet) {
        Optional.ofNullable(isSet ? flags.add(name) : flags.remove(name));
        return this;
    }

    public boolean isSet(String name) {
        return flags.contains(name);
    }

    @Override
    public ActionRequestValidationException validate() {
        return null;
    }

    @Override
    public RestRequest toRequest() throws Exception {
        String endpoint = buildEndPoint();
        RestRequest restRequest = new RestRequest("GET", endpoint, null);
        //加上超时时间
        restRequest.getParams().put("timeout", timeout);
        if (null != level) {
            restRequest.getParams().put("level", level);
        }
        return restRequest;
    }

    @Override
    public ESActionResponse toResponse(RestResponse response) throws Exception {
        String respStr = response.getResponseContent();
        JSONObject obj = JSON.parseObject(respStr);
        Object nodes = obj.remove("_nodes");
        ESNodeStatsResponse nodesStatsResponse = JSON.parseObject(obj.toJSONString(), ESNodeStatsResponse.class);
        if (null != nodes) {
            nodesStatsResponse.setFailedNodes(((JSONObject) nodes).getInteger("failed"));
        }
        return nodesStatsResponse;
    }

    private String buildEndPoint() {
        String nodes = nodesIds != null ? StringUtils.join(nodesIds, ",").trim() : null;
        String flagStr = flags.size() < 10 ? StringUtils.join(flags, ",").trim() : null;
        String nodeStatUrl = null == nodes ? "_nodes/stats" : String.format("_nodes/%s/stats", nodes);
        String finalUrl = null == flagStr ? nodeStatUrl : nodeStatUrl + "/" + flagStr;
        return finalUrl;
    }
}
