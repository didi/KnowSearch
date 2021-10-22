package com.didi.arius.gateway.remote;

import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.exception.ServerException;
import com.didi.arius.gateway.common.utils.HttpClient;
import com.didi.arius.gateway.remote.response.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author fitz
 * @date 2021/5/20 11:53 上午
 */
@Service
public class AriusAdminRemoteServiceImpl implements AriusAdminRemoteService {

    private static final Logger bootLogger = LoggerFactory.getLogger(QueryConsts.BOOT_LOGGER);

    @Value("${arius.gateway.adminUrl}")
    private String adminUrl;
    private Map<String, String> headerParams = new HashMap<String, String>(){{put(QueryConsts.GATEWAY_GET_APP_TICKET_NAME, QueryConsts.GATEWAY_GET_APP_TICKET);}};

    private static String APPID_SUFFIX              = "/v2/thirdpart/gateway/listApp";
    private static String DATACENTER_SUFFIX         = "/v2/thirdpart/common/cluster/list";
    private static String SCHEMA_SUFFIX             = "/v2/thirdpart/gateway/getTemplateMap";
    private static String GATEWAY_NODES_SUFFIX      = "/v2/thirdpart/gateway/alivecount";
    private static String TEMPLATE_INFO_SUFFIX      = "/v2/thirdpart/gateway/listDeployInfo";
    private static String CONFIG_LIST_SUFFIX        = "/v2/thirdpart/common/config/query";
    private static String GATEWAY_HEARTBEAT_SUFFIX  = "/v2/thirdpart/gateway/heartbeat";
    private static String GET_DSL_TEMPLATES_SUFFIX  = "/v2/thirdpart/gateway/dsl/scrollDslTemplates";


    private static String SCHEMA_PARAME_CLUSTER         = "cluster";
    private static String SCHEMA_PARAME_CLUSTER_NAME    = "clusterName";
    private static String SCHEMA_DATACENTER_NAME        = "dataCenter";

    private static String DSL_QUERY_FIRST_PARAMS    = "{\"dslTemplateVersion\":\"%s\",\"lastModifyTime\":%d,\"scrollSize\":1000}";
    private static String DSL_QUERY_PARAMS          = "{\"dslTemplateVersion\":\"%s\",\"lastModifyTime\":%d,\"scrollSize\":1000,\"scrollId\" : \"%s\"}";
    private static String DSL_TEMPLATE_VERSION      = "V2";

    private static String GATEWAY_DYNAMIC_CONFIG    = "{\"valueGroup\":\"arius.gateway.config\"}";

    @Override
    public AppListResponse listApp() {
        AppListResponse appListResponse = HttpClient.forward(adminUrl + APPID_SUFFIX, "GET", null, headerParams, null, AppListResponse.class);

        if (appListResponse.getCode() != 0) {
            bootLogger.error("resetAppDetails get app list error, code={}, message={}", appListResponse.getCode(), appListResponse.getMessage());
            throw new ServerException("resetAppDetails get app list error");
        }

        if (appListResponse.getData().size() < QueryConsts.MIN_APPID_NUMBER) {
            bootLogger.error("resetAppDetails get app list exception, appid_size={}", appListResponse.getData().size());
            throw new ServerException("resetAppDetails get app list exception");
        }
        return appListResponse;
    }

    @Override
    public DataCenterListResponse listCluster() {
        DataCenterListResponse response = HttpClient.forward(adminUrl + DATACENTER_SUFFIX, "GET", null, headerParams, null,DataCenterListResponse.class);
        if (response.getCode() != 0) {
            bootLogger.error("resetDataCenterMap get datacenter list error, code={}, message={}", response.getCode(), response.getMessage());
            throw new ServerException("resetDataCenterMap get datacenter list error");
        }
        return response;
    }

    @Override
    public TemplateInfoListResponse getTemplateInfoMap(String cluster) {
        Map<String, String[]> requestParams = new HashMap<>(1);
        requestParams.put(SCHEMA_PARAME_CLUSTER, new String[]{cluster});

        TemplateInfoListResponse templateInfoListResponse = HttpClient.forward(adminUrl + SCHEMA_SUFFIX, "GET", null, headerParams, requestParams,TemplateInfoListResponse.class);
        if (templateInfoListResponse.getCode() != 0) {
            bootLogger.error("resetDataCenterDetail get template info list error, code={}, message={}", templateInfoListResponse.getCode(), templateInfoListResponse.getMessage());
            throw new ServerException("resetDataCenterDetail get template info list error");
        }
        return templateInfoListResponse;
    }

    @Override
    public ActiveCountResponse getAliveCount(String clusterName) {
        Map<String, String[]> requestParams = new HashMap<>(1);
        requestParams.put(SCHEMA_PARAME_CLUSTER_NAME, new String[]{clusterName});
        ActiveCountResponse response = HttpClient.forward(adminUrl + GATEWAY_NODES_SUFFIX, "GET", null, headerParams, requestParams,ActiveCountResponse.class);
        if (response.getCode() != 0) {
            bootLogger.error("AdminSchedule alivecount error, code={}, message={}", response.getCode(), response.getMessage());
        }
        return response;
    }

    @Override
    public IndexTemplateListResponse listDeployInfo() {
        Map<String, String[]> requestParams = new HashMap<>(1);
        requestParams.put(SCHEMA_DATACENTER_NAME, new String[]{"cn"});//TODO:houxiufeng 这里先写死
        IndexTemplateListResponse response = HttpClient.forward(adminUrl + TEMPLATE_INFO_SUFFIX, "GET", null, headerParams, requestParams, IndexTemplateListResponse.class);

        if (response.getCode() != 0) {
            bootLogger.error("resetTemplateInfo error, code={}, message={}", response.getCode(), response.getMessage());
            throw new ServerException("resetTemplateInfo error");
        }

        if (response.getData().size() < QueryConsts.MIN_TEMPLATE_NUMBER) {
            bootLogger.error("resetTemplateInfo exception, template size={}", response.getData().size());
            throw new ServerException("resetTemplateInfo error");
        }
        return response;
    }

    @Override
    public DynamicConfigListResponse listQueryConfig() {
        DynamicConfigListResponse response = HttpClient.forward(adminUrl + CONFIG_LIST_SUFFIX, "POST", GATEWAY_DYNAMIC_CONFIG, headerParams, null, DynamicConfigListResponse.class);
        if (response.getCode() != 0) {
            bootLogger.error("AdminSchedule resetDynamicConfigInfo error, code={}, message={}", response.getCode(), response.getMessage());
        }
        return response;
    }

    @Override
    public void heartbeat(String clusterName, String hostName, Integer port) {
        String heartBeatBody = String.format("{\"clusterName\":\"%s\",\"hostName\":\"%s\",\"port\":%d}", clusterName, hostName, port);
        BaseAdminResponse response = HttpClient.forward(adminUrl + GATEWAY_HEARTBEAT_SUFFIX, "PUT", heartBeatBody, headerParams, null, BaseAdminResponse.class);
        if (response.getCode() != 0) {
            bootLogger.error("HeartBeatSchedule heartbeat error, code={}, message={}", response.getCode(), response.getMessage());
        }
    }

    @Override
    public DSLTemplateListResponse listDslTemplates(long lastModifyTime, String scrollId) {
        String queryStr = StringUtils.isBlank(scrollId) ?
                String.format(DSL_QUERY_FIRST_PARAMS, DSL_TEMPLATE_VERSION, lastModifyTime) :
                String.format(DSL_QUERY_PARAMS, DSL_TEMPLATE_VERSION, lastModifyTime, scrollId);
        DSLTemplateListResponse dslTemplateListResponse = HttpClient.forward(adminUrl + GET_DSL_TEMPLATES_SUFFIX, "POST", queryStr, null, null,DSLTemplateListResponse.class);

        if (dslTemplateListResponse.getCode() != 0) {
            bootLogger.error("resetDSLInfo get dsl list error, code={}, message={}", dslTemplateListResponse.getCode(), dslTemplateListResponse.getMessage());
            throw new ServerException("resetDSLInfo get dsl list error");
        }
        return dslTemplateListResponse;
    }
}
