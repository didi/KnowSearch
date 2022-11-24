package com.didichuxing.datachannel.arius.admin.remote.fastindex;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.fastindex.FastIndexDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.fastindex.ESIndexMoveTaskAdjustReadRateContext;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.fastindex.ESIndexMoveTaskContext;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.fastindex.ESIndexMoveTaskStats;
import com.didichuxing.datachannel.arius.admin.common.util.BaseHttpUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Maps;

/**
 * @author didi
 */
@Service
public class ESIndexMoveTaskServiceImpl implements ESIndexMoveTaskService {

    private static final ILog   LOGGER               = LogFactory.getLog(ESIndexMoveTaskServiceImpl.class);
    private static final String HTTP_PREFIX          = "http://";
    private static final String HTTPS_PREFIX         = "https://";
    private static final String FAST_DUMP_EXIST_CODE = "1000";
    private static final String CODE                 = "code";
    private static final int    FAST_DUMP_SUCC_CODE  = 200;

    @Override
    public Tuple<String, Result<String>> submitTask(FastIndexDTO fastIndexDTO, ESIndexMoveTaskContext context) {
        String response = post(fastIndexDTO.getTaskSubmitAddress(), fastIndexDTO.getSourceClusterPassword(),
            "/index-move/submit", JSON.toJSONString(context));
        return new Tuple<>(response, response2Result(response, String.class));
    }

    @Override
    public Tuple<String, Result<ESIndexMoveTaskStats>> getTaskStats(FastIndexDTO fastIndexDTO, String fastDumpTaskId) {
        String response = get(fastIndexDTO.getTaskSubmitAddress(), fastIndexDTO.getSourceClusterPassword(),
            "/index-move/" + fastDumpTaskId + "/stats");
        return new Tuple<>(response, response2Result(response, ESIndexMoveTaskStats.class));
    }

    @Override
    public Result<List<ESIndexMoveTaskStats>> getAllTaskStats(FastIndexDTO fastIndexDTO) {
        String response = get(fastIndexDTO.getTaskSubmitAddress(), fastIndexDTO.getSourceClusterPassword(),
            "/index-move/all/stats");
        if (StringUtils.isNotBlank(response)) {
            Result result = JSON.parseObject(response, Result.class);
            if (FAST_DUMP_SUCC_CODE == result.getCode()) {
                if (null != result.getData()) {
                    return Result.buildSucc(ConvertUtil.str2ObjArrayByJson(JSON.toJSONString(result.getData()),
                        ESIndexMoveTaskStats.class));
                }
                return Result.buildSucc();
            }
        }
        return Result.buildFail("请求失败");
    }

    @Override
    public Tuple<String, Result<Void>> adjustReadRate(FastIndexDTO fastIndexDTO,
                                                      ESIndexMoveTaskAdjustReadRateContext context) {
        String response = put(fastIndexDTO.getTaskSubmitAddress(), fastIndexDTO.getSourceClusterPassword(),
            "/index-move/adjust-readRate", JSON.toJSONString(context));
        return new Tuple<>(response, response2Result(response, null));
    }

    @Override
    public Tuple<String, Result<Void>> stopTask(FastIndexDTO fastIndexDTO, String fastDumpTaskId) {
        String response = put(fastIndexDTO.getTaskSubmitAddress(), fastIndexDTO.getSourceClusterPassword(),
            "/index-move/" + fastDumpTaskId + "/stop", "");
        return new Tuple<>(response, response2Result(response, null));
    }

    @Override
    public Result<Boolean> checkHealth(FastIndexDTO fastIndexDTO) {
        String response = get(fastIndexDTO.getTaskSubmitAddress(), fastIndexDTO.getSourceClusterPassword(),
            "/check-health");
        if (StringUtils.isNotBlank(response)) {
            JSONObject jsonObject = JSON.parseObject(response);
            if (FAST_DUMP_EXIST_CODE.equals(jsonObject.getString(CODE))) {
                return Result.buildSucc(Boolean.TRUE);
            } else {
                return Result.buildSucc(Boolean.FALSE);
            }
        }
        return Result.buildFail("请求发送失败");
    }

    private <T> Result<T> response2Result(String response, Class<T> targetClass) {
        if (StringUtils.isNotBlank(response)) {
            Result result = JSON.parseObject(response, Result.class);
            if (FAST_DUMP_SUCC_CODE == result.getCode()) {
                if (null != result.getData() && null != targetClass) {
                    T data = ConvertUtil.str2ObjByJson(JSON.toJSONString(result.getData()), targetClass);
                    return Result.buildSucc(data);
                }
                return Result.buildSucc();
            }
        }
        return Result.buildFail("请求失败");
    }

    private String get(String taskSubmitAddress, String pw, String uri) {

        Map<String, String> headers = buildHeader(pw);

        String url = buildUrl(taskSubmitAddress, uri);
        String response = "";
        try {
            response = BaseHttpUtil.get(url, null, headers);
        } catch (Exception e) {
            //pass
            LOGGER.error(
                "class=ESIndexMoveTaskServiceImpl||method=get||taskSubmitAddress={}||pw={}||uri={}||msg=请求fastDump内核失败！",
                taskSubmitAddress, pw, uri, e);
        }
        return response;
    }

    private String put(String taskSubmitAddress, String pw, String uri, String body) {

        Map<String, String> headers = buildHeader(pw);

        String url = buildUrl(taskSubmitAddress, uri);
        String response = "";
        try {
            response = BaseHttpUtil.putForString(url, body, headers);
        } catch (Exception e) {
            //pass
            LOGGER.error(
                "class=ESIndexMoveTaskServiceImpl||method=put||taskSubmitAddress={}||pw={}||uri={}||msg=请求fastDump内核失败！",
                taskSubmitAddress, pw, uri, e);
        }
        return response;
    }

    private String post(String taskSubmitAddress, String pw, String uri, String body) {

        Map<String, String> headers = buildHeader(pw);

        String url = buildUrl(taskSubmitAddress, uri);
        String response = "";
        try {
            response = BaseHttpUtil.postForString(url, body, headers);
        } catch (Exception e) {
            //pass
            LOGGER.error(
                "class=ESIndexMoveTaskServiceImpl||method=post||taskSubmitAddress={}||pw={}||uri={}||msg=请求fastDump内核失败！",
                taskSubmitAddress, pw, uri, e);
        }
        return response;
    }

    private Map<String, String> buildHeader(String pw) {
        Map<String, String> headers = Maps.newHashMap();
        if (StringUtils.isNotBlank(pw)) {
            headers.put("Authorization",
                "Basic " + Base64.getEncoder().encodeToString(pw.getBytes(StandardCharsets.UTF_8)));
        }
        headers.put("Content-Type", "application/json");
        return headers;
    }

    private String buildUrl(String taskSubmitAddress, String uri) {
        StringBuilder urlBuilder = new StringBuilder("");
        if (StringUtils.startsWithAny(taskSubmitAddress, HTTP_PREFIX, HTTPS_PREFIX)) {
            urlBuilder.append(taskSubmitAddress);
        } else {
            urlBuilder.append(HTTP_PREFIX);
            urlBuilder.append(taskSubmitAddress);
        }
        if (StringUtils.isNotBlank(uri)) {
            urlBuilder.append(uri);
        }
        return urlBuilder.toString();
    }
}
