package com.didichuxing.datachannel.arius.admin.remote.elasticcloud;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.EcmParamBase;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.elasticcloud.ElasticCloudCommonActionParam;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.elasticcloud.ElasticCloudCreateActionParam;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.elasticcloud.ElasticCloudScaleActionParam;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.response.EcmOperateAppBase;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.response.EcmSubTaskLog;
import com.didichuxing.datachannel.arius.admin.common.util.BaseHttpUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean.ESDDCloudClusterInfo;
import com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean.bizenum.EcmActionEnum;
import com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean.request.ElasticCloudCreateParamDTO;
import com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean.response.ElasticCloudAppStatus;
import com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean.response.ElasticCloudResult;
import com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean.response.ElasticCloudStatus;
import com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean.response.ElasticCloudUserLog;
import com.google.common.collect.Maps;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service
@NoArgsConstructor
public class ElasticCloudRemoteServiceImpl implements ElasticCloudRemoteService {
    
    private static final Logger LOGGER             = LoggerFactory.getLogger(ElasticCloudRemoteServiceImpl.class);

    @Value("${cloud.default.server}")
    private String              defaultUrl;

    @Value("${cloud.default.token}")
    private String              defaultToken;

    private static final String CLUSTER_PREFIX_URI = "/namespaces/{namespace}/regions/{machineRoom}/statefulapps";

    @Override
    public Result<ElasticCloudAppStatus> createAndStartAll(ElasticCloudCreateParamDTO elasticCloudCreateParamDTO, String namespace, String machineRoom) {
        String uri = CLUSTER_PREFIX_URI.replace("{namespace}", namespace).replace("{machineRoom}", machineRoom);
        String response = null;
        try {
            LOGGER.info("class=ElasticCloudRemoteServiceImpl||method=createAndStartAll||uri={}", uri);
            response = BaseHttpUtil.postForString(defaultUrl + uri,
                    JSON.toJSONString(elasticCloudCreateParamDTO),
                    getHttpHeader()
            );

            LOGGER.info("class=ElasticCloudRemoteServiceImpl||method=createAndStartAll||response={}", response);
            ElasticCloudAppStatus elasticCloudAppStatus = JSON.parseObject(response, ElasticCloudAppStatus.class);
            if (elasticCloudAppStatus.getTaskId() == null){
                return Result.buildFail("集群创建失败");
            }

            return Result.buildSucc(elasticCloudAppStatus);

        } catch (Exception e) {
            LOGGER.error("class=ElasticCloudRemoteServiceImpl||method=createAndStartAll||uri={}||response={}||error={}",
                    uri, response,e.getMessage());
        }
        return Result.buildFail("集群创建失败");
    }

    @Override
    public Result<EcmOperateAppBase> upgradeOrRestartByGroup(ElasticCloudCommonActionParam elasticCloudCommonActionParam) {
        Map<String, Object> param = Maps.newHashMap();
        param.put("ImageAddress", elasticCloudCommonActionParam.getImageName());

        String uri = buildActionUri(elasticCloudCommonActionParam, "update");

        String response = null;
        try {
            LOGGER.info("class=ElasticCloudRemoteServiceImpl||method=upgradeOrRestartByGroup||param={}||uri={}",
                    elasticCloudCommonActionParam, uri);

            response = BaseHttpUtil.putForString(defaultUrl + uri, JSON.toJSONString(param), getHttpHeader());
            EcmOperateAppBase ecmOperateAppBase = JSON.parseObject(response, EcmOperateAppBase.class);
            if (ecmOperateAppBase.getTaskId() == null){
                return Result.buildFail("集群更新失败");
            }

            return Result.buildSucc(ecmOperateAppBase);
        } catch (Exception e) {
            LOGGER.info("class=ElasticCloudRemoteServiceImpl||method=upgradeOrRestartByGroup||param={}||uri={}||response={}||error={}",
                    elasticCloudCommonActionParam, uri, response, e.getMessage());
        }
        return Result.buildFail(response);
    }

    @Override
    public Result<EcmOperateAppBase> scaleAndExecuteAll(ElasticCloudScaleActionParam elasticCloudScaleActionParam) {
        Map<String, Object> param = Maps.newHashMap();
        param.put("podCount", elasticCloudScaleActionParam.getPodNum());
        param.put("subnetScale", true);

        String uri = buildActionUri(elasticCloudScaleActionParam, "scale");
        String response = null;
        try {
            LOGGER.info("class=ElasticCloudRemoteServiceImpl||method=scaleAndExecuteAll||param={}||uri={}",
                    elasticCloudScaleActionParam, uri);

            response = BaseHttpUtil.putForString(defaultUrl + uri, JSON.toJSONString(param), getHttpHeader());
            EcmOperateAppBase ecmOperateAppBase = JSON.parseObject(response, EcmOperateAppBase.class);
            if (ecmOperateAppBase.getTaskId() == null){
                return Result.buildFail(response);
            }

            return Result.buildSucc(ecmOperateAppBase);
        } catch (Exception e) {
            LOGGER.info("class=ElasticCloudRemoteServiceImpl||method=scaleAndExecuteAll||param={}||uri={}||response={}||error={}",
                    elasticCloudScaleActionParam, uri, response, e.getMessage());
        }
        return Result.buildFail();
    }

    @Override
    public Result actionNotFinishedTask(ElasticCloudCommonActionParam elasticCloudActionParam, EcmActionEnum actionEnum) {
        String action = "";
        switch (actionEnum) {
            case PAUSE: action = "pause"; break;
            case CONTINUE: action = "continue"; break;
            case REDO_FAILED: action = "redofailed"; break;
            case SKIP_FAILED: action = "skipfailed"; break;
            default:
        }

        Map<String, String> param = Maps.newHashMap();
        param.put("action", action);

        String response = null;
        String uri = buildActionUri(elasticCloudActionParam, action);
        try {
            LOGGER.info("class=ElasticCloudRemoteServiceImpl||method=actionNotFinishedTask||namespace={}||action={}", uri,action);
            response = BaseHttpUtil.putForString(defaultUrl + uri, JSON.toJSONString(param), getHttpHeader());

            ElasticCloudResult result = JSON.parseObject(response, ElasticCloudResult.class);
            if (!result.getMessage() .equals("success")){
                return Result.buildFail(result.getMessage());
            }

            return Result.buildSucc();
        } catch (Exception e) {
            LOGGER.error("class=ElasticCloudRemoteServiceImpl||method=actionNotFinishedTask||namespace={}||response={}||error={}",
                    uri, response,e.getMessage());
        }
        return Result.buildFail();
    }

    @Override
    public Result delete(ElasticCloudCommonActionParam elasticCloudActionParam) {
        String uri = buildActionUri(elasticCloudActionParam,"");
        uri = uri.substring(0, uri.length() - 1);

        String response = null;
        try {
            LOGGER.info("class=ElasticCloudRemoteServiceImpl||method=delete||namespace={}", uri);
            response = BaseHttpUtil.deleteForString(defaultUrl + uri, JSON.toJSONString(getHttpHeader()), getHttpHeader());

            ElasticCloudAppStatus esAppStatusDTO = JSON.parseObject(response, ElasticCloudAppStatus.class);
            if (esAppStatusDTO.getTaskId() == null){
                return Result.buildFail(response);
            }

            return Result.buildSucc(esAppStatusDTO);
        } catch (Exception e) {
            LOGGER.error("class=ElasticCloudRemoteServiceImpl||method=delete||namespace={}||response={}||error={}",
                    uri, response,e.getMessage());
        }
        return Result.build(false, response);
    }


    @Override
    public Result<EcmSubTaskLog> getTaskLog(Long taskId, String podName, EcmParamBase actionParamBase) {
        ElasticCloudCommonActionParam elasticCloudActionParam = null;
        if ( actionParamBase instanceof ElasticCloudCreateActionParam) {
            elasticCloudActionParam = createActionParamConvertToCommonActionParam(actionParamBase);
        }else{
            elasticCloudActionParam = (ElasticCloudCommonActionParam)actionParamBase;
        }

        String uri = buildActionUri(elasticCloudActionParam,"logs?taskID="+taskId);
        String ddCloudPodLog = getDDCloudPodLog(elasticCloudActionParam.getMachineRoom(), podName.substring(0,podName.length()-10));

        String response = null;
        try {
            LOGGER.info("class=ElasticCloudRemoteServiceImpl||method=getDDCloudLogByTaskId||namespace={}", uri);
            response = BaseHttpUtil.get(defaultUrl + uri, null,getHttpHeader());
            ElasticCloudUserLog diDiCloudUserLog = JSON.parseObject(response, ElasticCloudUserLog.class);
            if (!StringUtils.isEmpty(ddCloudPodLog)) {
                ddCloudPodLog = ddCloudPodLog.replace("\\n","\n");
            }
            return Result.buildSucc(new EcmSubTaskLog(ddCloudPodLog, diDiCloudUserLog.convert2FormattedString()));
        } catch (Exception e) {
            LOGGER.error("class=ElasticCloudRemoteServiceImpl||method=getTaskLog||namespace={}||response={}||error={}",
                    uri, response,e.getMessage());
        }
        return Result.buildFail();
    }

    @Override
    public Result<ElasticCloudStatus> getTaskStatus(EcmParamBase actionParamBase) {

        ElasticCloudCommonActionParam elasticCloudCommonActionParam = null;
        if ( actionParamBase instanceof ElasticCloudCreateActionParam) {
            elasticCloudCommonActionParam = createActionParamConvertToCommonActionParam(actionParamBase);
        }else{
            elasticCloudCommonActionParam = (ElasticCloudCommonActionParam)actionParamBase;
        }

        String uri = buildActionUri(elasticCloudCommonActionParam, "status");
        String response = null;
        try {
            LOGGER.info("class=ElasticCloudRemoteServiceImpl||method=getTaskStatus||namespace={}", uri);

            response =  BaseHttpUtil.get(defaultUrl + uri,null, getHttpHeader());
            ElasticCloudStatus elasticCloudStatus = JSON.parseObject(response, ElasticCloudStatus.class);


            return Result.buildSucc(elasticCloudStatus);
        } catch (Exception e) {
            LOGGER.error("class=ElasticCloudRemoteServiceImpl||method=getTaskStatus||namespace={}||response={}||error={}",
                    uri, response,e);
        }
        return Result.buildFail();
    }

    @Override
    public Result<String> getClusterInfo(ElasticCloudCommonActionParam elasticCloudCommonActionParam) {
        String url = defaultUrl + "/namespaces/" + elasticCloudCommonActionParam.getNsTree() + "/regions/all/statefulapps";
        String postForString = BaseHttpUtil.get(url, null, getHttpHeader());
        ESDDCloudClusterInfo esddCloudClusterInfo = JSON.parseObject(postForString).toJavaObject(ESDDCloudClusterInfo.class);
        if (esddCloudClusterInfo != null){
            return  Result.buildSuccWithMsg(JSON.toJSONString(esddCloudClusterInfo));
        }
        return Result.buildFail(postForString);
    }

    /**************************************** private method ****************************************************/
    /**
     * 根据podName 获取弹性云Pod启动日志
     * @param podName  容器HostName
     * @param region   机房
     * @return Result
     */
    private String getDDCloudPodLog(String region, String podName) {
        String url = defaultUrl + "/regions/" + region + "/pods/" + podName + "/log";
        String postForString = BaseHttpUtil.get(url, null,  getHttpHeader());
        if (!postForString.equals("")) {
            postForString = postForString.substring(1,postForString.length()-1);
        }
        return postForString;
    }


    private Map<String, String>  getHttpHeader(){
        Map<String, String> headers = Maps.newHashMap();
        headers.put("Authorization", "Bearer "+defaultToken);
        headers.put("Content-Type","application/json");
        return headers;
    }

    private String buildActionUri(ElasticCloudCommonActionParam elasticCloudActionParam, String action) {
        return new StringBuilder(CLUSTER_PREFIX_URI
                .replace("{namespace}", elasticCloudActionParam.namespace())
                .replace("{machineRoom}", elasticCloudActionParam.getMachineRoom()))
                .append('/')
                .append(elasticCloudActionParam.getPhyClusterName()).append('-').append(elasticCloudActionParam.getRoleName())
                .append('/')
                .append(action).toString();
    }

    private ElasticCloudCommonActionParam createActionParamConvertToCommonActionParam(EcmParamBase actionParamBase){
        ElasticCloudCreateActionParam elasticCloudCreateActionParam = (ElasticCloudCreateActionParam) actionParamBase;
        ElasticCloudCommonActionParam elasticCloudCommonActionParam = ConvertUtil.obj2Obj(elasticCloudCreateActionParam, ElasticCloudCommonActionParam.class);
        elasticCloudCommonActionParam.setMachineRoom(elasticCloudCreateActionParam.getIdc());

        return elasticCloudCommonActionParam;
    }
}
