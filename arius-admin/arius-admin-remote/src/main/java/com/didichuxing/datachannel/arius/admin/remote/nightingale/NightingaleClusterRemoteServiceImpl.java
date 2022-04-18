package com.didichuxing.datachannel.arius.admin.remote.nightingale;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.response.EcmCreateApp;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.response.EcmOperateAppBase;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.response.EcmSubTaskLog;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.response.EcmTaskStatus;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.BaseHttpUtil;
import com.didichuxing.datachannel.arius.admin.remote.nightingale.bean.NightingaleCreateTaskParam;
import com.didichuxing.datachannel.arius.admin.remote.nightingale.bean.NightingaleResult;
import com.didichuxing.datachannel.arius.admin.remote.nightingale.bean.NightingaleTaskStatus;
import com.didichuxing.datachannel.arius.admin.remote.zeus.bean.ZeusResult;
import com.didichuxing.datachannel.arius.admin.remote.zeus.bean.ZeusSubTaskLog;
import com.google.common.collect.Maps;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@NoArgsConstructor
public class NightingaleClusterRemoteServiceImpl implements NightingaleClusterRemoteService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NightingaleClusterRemoteServiceImpl.class);

    private static final String NIGHTINGALE_INVOKE_FAILED_TIPS = "调用夜莺失败";

    @Value("${nightingale.job.base-url:}")
    private String              baseUrl;

    @Value("${nightingale.job.user-token:}")
    private String              userToken;

    @Override
    public Result<EcmOperateAppBase> createTask(List<String> hostList, String args) {
        NightingaleCreateTaskParam nightingaleCreateTaskParam = new NightingaleCreateTaskParam(hostList, args);

        String url = baseUrl + "/api/job-ce/tasks";
        String response = null;
        try {
            LOGGER.info("class=NightingaleClusterRemoteServiceImpl||method=createTask||params={}", nightingaleCreateTaskParam);
            response = BaseHttpUtil.postForString(url, JSON.toJSONString(nightingaleCreateTaskParam), buildHeader());

            Result<Object> createResult = convert2Result(JSON.parseObject(response, NightingaleResult.class));
            if (createResult.failed()) {
                return Result.buildFail(createResult.getMessage());
            }
            return Result.buildSucc(new EcmCreateApp(Integer.valueOf(createResult.getData().toString()), hostList));
        } catch (Exception e) {
            LOGGER.error("class=NightingaleClusterRemoteServiceImpl||method=createTask||params={}||response={}||error={}",
                    nightingaleCreateTaskParam, response, e.getMessage());
        }
        return Result.buildFail(NIGHTINGALE_INVOKE_FAILED_TIPS);
    }

    @Override
    public Result<Object> actionTask(Integer taskId, String action) {
        return actionHostTask(taskId, null, action);
    }

    @Override
    public Result<Object> actionHostTask(Integer taskId, String hostname, String action) {
        Map<String, Object>  params = Maps.newHashMap();
        if (!AriusObjUtils.isBlank(hostname)) {
            params.put("hostname", hostname);
        }
        params.put("action", action);

        String url = baseUrl + String.format("/api/job-ce/task/%d/action", taskId);
        String response = null;
        try {
            LOGGER.info("class=NightingaleClusterRemoteServiceImpl||method=actionHostTask||taskId={}||action={}", taskId, action);

            response = BaseHttpUtil.putForString(url, JSON.toJSONString(params), buildHeader());

            return convert2Result(JSON.parseObject(response, NightingaleResult.class));
        } catch (Exception e) {
            LOGGER.error("class=NightingaleClusterRemoteServiceImpl||method=actionHostTask||taskId={}||action={}||response={}||error={}",
                    taskId, action, response, e.getMessage());
        }
        return Result.buildFail(NIGHTINGALE_INVOKE_FAILED_TIPS);
    }

    @Override
    public Result<List<EcmTaskStatus>> getTaskStatus(Integer taskId) {
        String url = baseUrl + String.format("/api/job-ce/task/%d/result", taskId);

        String response = null;
        try {
            LOGGER.info("class=NightingaleClusterRemoteServiceImpl||method=getTaskStatus||taskId={}", taskId);

            response = BaseHttpUtil.get(url, null);

            ZeusResult zeusResult = JSON.parseObject(response, ZeusResult.class);
            if (zeusResult.failed()) {
                LOGGER.error("class=NightingaleClusterRemoteServiceImpl||method=getTaskStatus||taskId={}||response={}", taskId, response);
                return Result.buildFail(zeusResult.getMsg());
            }

            NightingaleTaskStatus nightingaleTaskStatus = JSON.parseObject(JSON.toJSONString(zeusResult.getData()), NightingaleTaskStatus.class);
            return Result.buildSucc(nightingaleTaskStatus.convert2EcmHostStatusEnumList(taskId));
        } catch (Exception e) {
            LOGGER.error("class=NightingaleClusterRemoteServiceImpl||method=getTaskStatus||taskId={}response={}||error={}",
                    taskId, response, e.getMessage());
        }
        return Result.buildFail(NIGHTINGALE_INVOKE_FAILED_TIPS);
    }

    @Override
    public Result<EcmSubTaskLog> getTaskLog(Integer taskId, String hostname) {
        Result<String> stdoutResult = getTaskStdOutLog(taskId, hostname);
        Result<String> stderrResult = getTaskStdErrLog(taskId, hostname);
        if (stderrResult.failed() && stdoutResult.failed()) {
            return Result.buildFail();
        }
        return Result.buildSucc(new EcmSubTaskLog(stdoutResult.getData(), stderrResult.getData()));

    }

    private Result<String> getTaskStdOutLog(Integer taskId, String hostname) {
        String url = baseUrl + String.format("/api/job-ce/task/%d/host/%s/stdout", taskId, hostname);
        String response = null;
        try {
            LOGGER.info("class=NightingaleClusterRemoteServiceImpl||method=getTaskStdOutLog||taskId={}||hostname={}",
                    taskId, hostname);

            response = BaseHttpUtil.get(url, null, buildHeader());

            Result<Object> result = convert2Result(JSON.parseObject(response, NightingaleResult.class));
            if (result.failed()) {
                return Result.buildFrom(result);
            }
            List<ZeusSubTaskLog> zeusSubTaskLogs = JSON.parseArray(JSON.toJSONString(result.getData()), ZeusSubTaskLog.class);
            if (zeusSubTaskLogs == null || zeusSubTaskLogs.isEmpty()) {
                return Result.buildSuccWithMsg("");
            }
            return Result.buildSucc(zeusSubTaskLogs.get(0).getStdout(), "");
        } catch (Exception e) {
            LOGGER.error("class=NightingaleClusterRemoteServiceImpl||method=getTaskStdOutLog||taskId={}||hostname={}||response={}||error={}",
                    taskId, hostname, response, e.getMessage());
        }
        return Result.buildFail();
    }

    private Result<String> getTaskStdErrLog(Integer taskId, String hostname) {
        String url = baseUrl + String.format("/api/job-ce/task/%d/host/%s/stderr", taskId, hostname);
        String response = null;
        try {
            LOGGER.info("class=NightingaleClusterRemoteServiceImpl||method=getTaskStdErrLog||taskId={}||hostname={}",
                    taskId, hostname);

            response = BaseHttpUtil.get(url, null, buildHeader());

            Result<Object> result = convert2Result(JSON.parseObject(response, NightingaleResult.class));
            if (result.failed()) {
                return Result.buildFrom(result);
            }
            List<ZeusSubTaskLog> zeusSubTaskLogs = JSON.parseArray(JSON.toJSONString(result.getData()), ZeusSubTaskLog.class);
            if (zeusSubTaskLogs == null || zeusSubTaskLogs.isEmpty()) {
                return Result.buildSuccWithMsg("");
            }
            return Result.buildSucc(zeusSubTaskLogs.get(0).getStdout(), "");
        } catch (Exception e) {
            LOGGER.error("class=NightingaleClusterRemoteServiceImpl||method=getTaskStdErrLog||taskId={}||hostname={}||response={}||error={}",
                    taskId, hostname, response, e.getMessage());
        }
        return Result.buildFail();
    }

    private <T> Result<Object> convert2Result(NightingaleResult<T> nightingaleResult) {
        if (nightingaleResult.failed()){
            return Result.buildFail(nightingaleResult.getErr());
        }
        return Result.buildSucc(nightingaleResult.getDat());
    }

    private Map<String, String> buildHeader() {
        Map<String, String> headers = Maps.newHashMap();
        headers.put("Content-Type", "application/json");
        headers.put("X-User-Token", userToken);
        return headers;
    }
}