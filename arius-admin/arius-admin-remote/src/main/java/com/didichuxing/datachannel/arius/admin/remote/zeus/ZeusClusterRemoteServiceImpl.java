package com.didichuxing.datachannel.arius.admin.remote.zeus;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.response.EcmCreateApp;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.response.EcmOperateAppBase;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.response.EcmSubTaskLog;
import com.didichuxing.datachannel.arius.admin.remote.zeus.bean.ZeusTaskStatus;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.response.EcmTaskStatus;
import com.didichuxing.datachannel.arius.admin.common.util.BaseHttpUtil;
import com.didichuxing.datachannel.arius.admin.remote.zeus.bean.request.ZeusCreateTaskParam;
import com.didichuxing.datachannel.arius.admin.remote.zeus.bean.ZeusSubTaskLog;
import com.didichuxing.datachannel.arius.admin.remote.zeus.bean.ZeusResult;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ZeusClusterRemoteServiceImpl implements ZeusClusterRemoteService {
    private final static Logger LOGGER = LoggerFactory.getLogger(ZeusClusterRemoteServiceImpl.class);

    @Value("${zeus.server}")
    private String              zeusServer;

    @Value("${zeus.token}")
    private String              zeusToken;

    @Value("${zeus.tplid}")
    private Integer             zeusTplId;

    @Value("${zeus.user}")
    private String              zeusUser;

    @Override
    public Result<EcmOperateAppBase> createTask(List<String> hostList, String args) {
        ZeusCreateTaskParam zeusCreateTaskParam = buildCreateZeusTaskParam(hostList, args);

        String url = zeusServer + "/api/task?token=" + zeusToken;
        String response = null;
        try {
            LOGGER.info("class=ZeusClusterRemoteServiceImpl||method=createTask||params={}", zeusCreateTaskParam);
            response = BaseHttpUtil.postForString(url, JSONObject.toJSONString(zeusCreateTaskParam), buildHeader());

            Result<Integer> createResult = convert2Result(JSONObject.parseObject(response, ZeusResult.class));
            if (createResult.failed()) {
                return Result.buildFail(createResult.getMessage());
            }
            return Result.buildSucc(new EcmCreateApp(createResult.getData(), hostList));
        } catch (Exception e) {
            LOGGER.error("class=ZeusClusterRemoteServiceImpl||method=createTask||params={}||response={}||error={}",
                    zeusCreateTaskParam, response, e.getMessage());
        }
        return Result.buildFail();
    }

    @Override
    public Result actionTask(Integer taskId, String action) {
        Map<String, Object>  params = Maps.newHashMap();
        params.put("task_id", taskId);
        params.put("action", action);

        String url = zeusServer+"/api/task/action?token="+ zeusToken;
        String response = null;
        try {
            LOGGER.info("class=ZeusClusterRemoteServiceImpl||method=actionTask||taskId={}||action={}", taskId, action);

            response = BaseHttpUtil.postForString(url, JSONObject.toJSONString(params), null);

            return convert2Result(JSONObject.parseObject(response, ZeusResult.class));
        } catch (Exception e) {
            LOGGER.error("class=ZeusClusterRemoteServiceImpl||method=actionTask||taskId={}||action={}||response={}||error={}",
                    taskId, action, response, e.getMessage());
        }
        return Result.buildFail();
    }

    @Override
    public Result actionHostTask(Integer taskId, String hostname, String action) {
        Map<String, Object>  params = Maps.newHashMap();
        params.put("task_id", taskId);
        params.put("hostname", hostname);
        params.put("action", action);

        String url = zeusServer+"/api/task/host-action?token="+ zeusToken;
        String response = null;
        try {
            LOGGER.info("class=ZeusClusterRemoteServiceImpl||method=actionHostTask||taskId={}||hostname={}||action={}",
                    taskId, hostname, action);

            response = BaseHttpUtil.postForString(url, JSONObject.toJSONString(params), buildHeader());

            return convert2Result(JSONObject.parseObject(response, ZeusResult.class));
        } catch (Exception e) {
            LOGGER.error("class=ZeusClusterRemoteServiceImpl||method=actionTask||taskId={}||hostname={}||action={}||response={}||error={}",
                    taskId, hostname, action, response, e.getMessage());
        }
        return Result.buildFail();
    }

    @Override
    public Result<List<EcmTaskStatus>> getZeusTaskStatus(Integer taskId) {
        String url = zeusServer + "/api/task/" + taskId + "/result";

        String response = null;
        try {
            LOGGER.info("class=ZeusClusterRemoteServiceImpl||method=getZeusTaskStatus||taskId={}", taskId);

            response = BaseHttpUtil.get(url, null);

            ZeusResult zeusResult = JSONObject.parseObject(response, ZeusResult.class);
            if (zeusResult.failed()) {
                LOGGER.error("class=ZeusClusterRemoteServiceImpl||method=getZeusTaskStatus||taskId={}||response={}", taskId, response);
                return Result.buildFail(zeusResult.getMsg());
            }

            ZeusTaskStatus zeusTaskStatus = JSONObject.parseObject(JSON.toJSONString(zeusResult.getData()), ZeusTaskStatus.class);
            return Result.buildSucc(zeusTaskStatus.convert2EcmHostStatusEnumList(taskId));
        } catch (Exception e) {
            LOGGER.error("class=ZeusClusterRemoteServiceImpl||method=getZeusTaskStatus||taskId={}response={}||error={}",
                    taskId, response, e.getMessage());
        }
        return Result.buildFail(response);
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
        String url = zeusServer + "/api/task/" + taskId + "/stdouts.json?hostname=" + hostname;
        String response = null;
        try {
            LOGGER.info("class=ZeusClusterRemoteServiceImpl||method=getTaskStdOutLog||taskId={}||hostname={}",
                    taskId, hostname);

            response = BaseHttpUtil.get(url, null, buildHeader());

            Result result = convert2Result(JSONObject.parseObject(response, ZeusResult.class));
            if (result.failed()) {
                return result;
            }
            List<ZeusSubTaskLog> zeusSubTaskLogs = JSONObject.parseArray(JSON.toJSONString(result.getData()), ZeusSubTaskLog.class);
            if (zeusSubTaskLogs == null || zeusSubTaskLogs.isEmpty()) {
                return Result.buildSucc("");
            }
            return Result.buildSucc(zeusSubTaskLogs.get(0).getStdout(), "");
        } catch (Exception e) {
            LOGGER.error("class=ZeusClusterRemoteServiceImpl||method=getTaskStdOutLog||taskId={}||hostname={}||response={}||error={}",
                    taskId, hostname, response, e.getMessage());
        }
        return Result.buildFail();
    }

    private Result<String> getTaskStdErrLog(Integer taskId, String hostname) {
        String url = zeusServer + "/api/task/" + taskId + "/stderrs.json?hostname=" + hostname;
        String response = null;
        try {
            LOGGER.info("class=ZeusClusterRemoteServiceImpl||method=getTaskStdErrLog||taskId={}||hostname={}",
                    taskId, hostname);

            response = BaseHttpUtil.get(url, null, buildHeader());

            Result result = convert2Result(JSONObject.parseObject(response, ZeusResult.class));
            if (result.failed()) {
                return result;
            }
            List<ZeusSubTaskLog> zeusSubTaskLogs = JSONObject.parseArray(JSON.toJSONString(result.getData()), ZeusSubTaskLog.class);
            if (zeusSubTaskLogs == null || zeusSubTaskLogs.isEmpty()) {
                return Result.buildSucc("");
            }
            return Result.buildSucc(zeusSubTaskLogs.get(0).getStderr(), "");
        } catch (Exception e) {
            LOGGER.error("class=ZeusClusterRemoteServiceImpl||method=getTaskStdErrLog||taskId={}||hostname={}||response={}||error={}",
                    taskId, hostname, response, e.getMessage());
        }
        return Result.buildFail();
    }

    private Result convert2Result(ZeusResult zeusResult) {
        if (zeusResult.failed()){
            return Result.buildFail(zeusResult.getMsg());
        }
        return Result.buildSucc(zeusResult.getData());
    }

    private Map<String, String> buildHeader() {
        Map<String, String> headers = Maps.newHashMap();
        headers.put("Content-Type", "application/json");
        return headers;
    }

    private ZeusCreateTaskParam buildCreateZeusTaskParam(List<String> hostList, String args) {
        ZeusCreateTaskParam zeusCreateTaskParam = new ZeusCreateTaskParam();
        zeusCreateTaskParam.setTpl_id(zeusTplId);
        zeusCreateTaskParam.setAccount(zeusUser);
        zeusCreateTaskParam.setHosts(hostList);
        zeusCreateTaskParam.setBatch(0);
        zeusCreateTaskParam.setTolerance(0);
        zeusCreateTaskParam.setPause("");
        zeusCreateTaskParam.setTimeout(300);
        zeusCreateTaskParam.setArgs(args);
        return zeusCreateTaskParam;
    }
}
