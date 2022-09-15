package com.didichuxing.datachannel.arius.admin.remote.zeus;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.response.EcmCreateApp;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.response.EcmOperateAppBase;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.response.EcmSubTaskLog;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.response.EcmTaskStatus;
import com.didichuxing.datachannel.arius.admin.common.util.BaseHttpUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.remote.zeus.bean.*;
import com.didichuxing.datachannel.arius.admin.remote.zeus.bean.request.ZeusCreateTaskParam;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@NoArgsConstructor
public class ZeusClusterRemoteServiceImpl implements ZeusClusterRemoteService {
    private static final Logger LOGGER   = LoggerFactory.getLogger(ZeusClusterRemoteServiceImpl.class);

    @Value("${zeus.server}")
    private String              zeusServer;

    @Value("${zeus.token}")
    private String              zeusToken;

    @Value("${zeus.templateId}")
    private Integer             zeusTemplateIdId;

    @Value("${zeus.user}")
    private String              zeusUser;

    @Value("${zeus.batch}")
    private Integer             zeusBatch;

    @Value("${zeus.timeOut}")
    private Integer             zeusTimeOut;

    @Value("${zeus.tolerance}")
    private Integer             zeusTolerance;

    private static final String API_TASK = "/api/task/";

    private static final String API_AGENTS_LIST = "/api/agents-list";

    @Override
    public Result<EcmOperateAppBase> createTask(List<String> hostList, String args) {
        ZeusCreateTaskParam zeusCreateTaskParam = buildCreateZeusTaskParam(hostList, args);

        String url = zeusServer + "/api/task?token=" + zeusToken;
        String response = null;
        try {
            LOGGER.info("class=ZeusClusterRemoteServiceImpl||method=createTask||params={}", zeusCreateTaskParam);
            response = BaseHttpUtil.postForString(url, JSON.toJSONString(zeusCreateTaskParam), buildHeader());

            Result<Object> createResult = convert2Result(JSON.parseObject(response, ZeusResult.class));
            if (createResult.failed()) {
                return Result.buildFail(createResult.getMessage());
            }
            return Result.buildSucc(new EcmCreateApp(Integer.valueOf(createResult.getData().toString()), hostList));
        } catch (Exception e) {
            LOGGER.error("class=ZeusClusterRemoteServiceImpl||method=createTask||params={}||response={}||error={}",
                zeusCreateTaskParam, response, e.getMessage());
        }
        return Result.buildFail();
    }

    @Override
    public Result<Object> actionTask(Integer taskId, String action) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("task_id", taskId);
        params.put("action", action);

        String url = zeusServer + "/api/task/action?token=" + zeusToken;
        String response = null;
        try {
            LOGGER.info("class=ZeusClusterRemoteServiceImpl||method=actionTask||taskId={}||action={}", taskId, action);

            response = BaseHttpUtil.postForString(url, JSON.toJSONString(params), null);

            return convert2Result(JSON.parseObject(response, ZeusResult.class));
        } catch (Exception e) {
            LOGGER.error(
                "class=ZeusClusterRemoteServiceImpl||method=actionTask||taskId={}||action={}||response={}||error={}",
                taskId, action, response, e.getMessage());
        }
        return Result.buildFail();
    }

    @Override
    public Result<Object> actionHostTask(Integer taskId, String hostname, String action) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("task_id", taskId);
        params.put("hostname", hostname);
        params.put("action", action);

        String url = zeusServer + "/api/task/host-action?token=" + zeusToken;
        String response = null;
        try {
            LOGGER.info("class=ZeusClusterRemoteServiceImpl||method=actionHostTask||taskId={}||hostname={}||action={}",
                taskId, hostname, action);

            response = BaseHttpUtil.postForString(url, JSON.toJSONString(params), buildHeader());

            return convert2Result(JSON.parseObject(response, ZeusResult.class));
        } catch (Exception e) {
            LOGGER.error(
                "class=ZeusClusterRemoteServiceImpl||method=actionTask||taskId={}||hostname={}||action={}||response={}||error={}",
                taskId, hostname, action, response, e.getMessage());
        }
        return Result.buildFail();
    }

    @Override
    public Result<List<EcmTaskStatus>> getZeusTaskStatus(Integer taskId) {
        String url = zeusServer + API_TASK + taskId + "/result";

        String response = null;
        try {
            LOGGER.info("class=ZeusClusterRemoteServiceImpl||method=getZeusTaskStatus||taskId={}", taskId);

            response = BaseHttpUtil.get(url, null);

            ZeusResult zeusResult = JSON.parseObject(response, ZeusResult.class);
            if (zeusResult.failed()) {
                LOGGER.error("class=ZeusClusterRemoteServiceImpl||method=getZeusTaskStatus||taskId={}||response={}",
                    taskId, response);
                return Result.buildFail(zeusResult.getMsg());
            }

            ZeusTaskStatus zeusTaskStatus = JSON.parseObject(JSON.toJSONString(zeusResult.getData()),
                ZeusTaskStatus.class);
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

    @Override
    public Result<List<String>> getAgentsList(){
        String url = zeusServer + API_AGENTS_LIST + "?token=" + zeusToken;
        String  response = null;
        try {
            LOGGER.info("class=ZeusClusterRemoteServiceImpl||method=getAgentsList");
            response = BaseHttpUtil.get(url,null,buildHeader());
            Result<Object> result = convert2Result(JSON.parseObject(response,ZeusResult.class));
            if (result.failed()) {
                return Result.buildFrom(result);
            }
            //获取Zeus的Agents列表并将获取zeus中的ip列表
            ZeusAgentsList zeusAgentsList = JSON.parseObject(JSON.toJSONString(result.getData()),
                    ZeusAgentsList.class);
            List<String> ipList = zeusAgentsList.getDat().stream().map(ZeusDat::getIp).distinct().collect(Collectors.toList());
            return Result.buildSucc(ipList);
        } catch (Exception e) {
            LOGGER.error(
                    "class=ZeusClusterRemoteServiceImpl||method=getAgentsList||response={}||error={}",
                     response, e.getMessage());
        }
        return Result.buildFail();
    }

    private Result<String> getTaskStdOutLog(Integer taskId, String hostname) {
        String url = zeusServer + API_TASK + taskId + "/stdouts.json?hostname=" + hostname;
        String response = null;
        try {
            LOGGER.info("class=ZeusClusterRemoteServiceImpl||method=getTaskStdOutLog||taskId={}||hostname={}", taskId,
                hostname);

            response = BaseHttpUtil.get(url, null, buildHeader());

            Result<Object> result = convert2Result(JSON.parseObject(response, ZeusResult.class));
            if (result.failed()) {
                return Result.buildFrom(result);
            }
            List<ZeusSubTaskLog> zeusSubTaskLogs = JSON.parseArray(JSON.toJSONString(result.getData()),
                ZeusSubTaskLog.class);
            if (zeusSubTaskLogs == null || zeusSubTaskLogs.isEmpty()) {
                return Result.buildSuccWithMsg("");
            }
            return Result.buildSucc(zeusSubTaskLogs.get(0).getStdout(), "");
        } catch (Exception e) {
            LOGGER.error(
                "class=ZeusClusterRemoteServiceImpl||method=getTaskStdOutLog||taskId={}||hostname={}||response={}||error={}",
                taskId, hostname, response, e.getMessage());
        }
        return Result.buildFail();
    }

    private Result<String> getTaskStdErrLog(Integer taskId, String hostname) {
        String url = zeusServer + API_TASK + taskId + "/stderrs.json?hostname=" + hostname;
        String response = null;
        try {
            LOGGER.info("class=ZeusClusterRemoteServiceImpl||method=getTaskStdErrLog||taskId={}||hostname={}", taskId,
                hostname);

            response = BaseHttpUtil.get(url, null, buildHeader());

            Result<Object> result = convert2Result(JSON.parseObject(response, ZeusResult.class));
            if (result.failed()) {
                return Result.buildFrom(result);
            }
            List<ZeusSubTaskLog> zeusSubTaskLogs = JSON.parseArray(JSON.toJSONString(result.getData()),
                ZeusSubTaskLog.class);
            if (zeusSubTaskLogs == null || zeusSubTaskLogs.isEmpty()) {
                return Result.buildSuccWithMsg("");
            }
            return Result.buildSucc(zeusSubTaskLogs.get(0).getStderr(), "");
        } catch (Exception e) {
            LOGGER.error(
                "class=ZeusClusterRemoteServiceImpl||method=getTaskStdErrLog||taskId={}||hostname={}||response={}||error={}",
                taskId, hostname, response, e.getMessage());
        }
        return Result.buildFail();
    }

    private Result<Object> convert2Result(ZeusResult zeusResult) {
        if (zeusResult.failed()) {
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
        zeusCreateTaskParam.setTpl_id(zeusTemplateIdId);
        zeusCreateTaskParam.setAccount(zeusUser);
        zeusCreateTaskParam.setHosts(hostList);
        zeusCreateTaskParam.setBatch(zeusBatch);
        zeusCreateTaskParam.setTolerance(zeusTolerance);
        zeusCreateTaskParam.setPause(getPauseHosts(hostList));
        zeusCreateTaskParam.setTimeout(zeusTimeOut);
        zeusCreateTaskParam.setArgs(args);
        return zeusCreateTaskParam;
    }

    /**
     * 根据传入的主机列表设计暂停点，便于线上机器部署，设置的是2个机器为一组
     */
    private String getPauseHosts(List<String> hostList) {
        List<String> pauseHosts = Lists.newArrayList();
        for (int count = 0; count < hostList.size(); count = count + zeusBatch) {
            pauseHosts.add(hostList.get(count));
        }
        return ListUtils.strList2String(pauseHosts);
    }
}
