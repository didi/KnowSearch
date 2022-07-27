package com.didi.arius.gateway.remote;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.exception.ServerException;
import com.didi.arius.gateway.common.metadata.TemplateAlias;
import com.didi.arius.gateway.common.utils.HttpClient;
import com.didi.arius.gateway.remote.response.*;

import lombok.NoArgsConstructor;

/**
 * @author fitz
 * @date 2021/5/20 11:53 上午
 */
@Service
@NoArgsConstructor
public class AriusAdminRemoteServiceImpl implements AriusAdminRemoteService {

    private static final Logger bootLogger = LoggerFactory.getLogger(QueryConsts.BOOT_LOGGER);

    @Value("${arius.gateway.adminUrl}")
    private String adminUrl;
    private static final Map<String, String> headerParams = new HashMap<>();

    private static final String APPID_SUFFIX              = "/v3/thirdpart/gateway/project";
    private static final String DATACENTER_SUFFIX         = "/v3/thirdpart/common/cluster";
    private static final String SCHEMA_SUFFIX             = "/v3/thirdpart/gateway/template";
    private static final String GATEWAY_NODES_SUFFIX      = "/v3/thirdpart/gateway/alivecount";
    private static final String TEMPLATE_INFO_SUFFIX      = "/v3/thirdpart/gateway/deploy-info";
    private static final String CONFIG_LIST_SUFFIX        = "/v3/thirdpart/common/config/query";
    private static final String GATEWAY_HEARTBEAT_SUFFIX  = "/v3/thirdpart/gateway/heartbeat";
    private static final String GET_DSL_TEMPLATES_SUFFIX  = "/v3/thirdpart/gateway/dsl/scroll-dsl-template";
    private static final String ADD_TEMPLATE_ALIAS = "/v3/thirdpart/gateway/alias";
    private static final String DEL_TEMPLATE_ALIAS = "/v3/thirdpart/gateway/alias";


    private static final String SCHEMA_PARAME_CLUSTER         = "cluster";
    private static final String SCHEMA_PARAME_CLUSTER_NAME    = "clusterName";
    private static final String SCHEMA_DATACENTER_NAME        = "dataCenter";

    private static final String DSL_QUERY_FIRST_PARAMS    = "{\"dslTemplateVersion\":\"%s\",\"lastModifyTime\":%d,\"scrollSize\":1000}";
    private static final String DSL_QUERY_PARAMS          = "{\"dslTemplateVersion\":\"%s\",\"lastModifyTime\":%d,\"scrollSize\":1000,\"scrollId\" : \"%s\"}";
    private static final String DSL_TEMPLATE_VERSION      = "V2";

    private static final String GATEWAY_DYNAMIC_CONFIG    = "{\"valueGroup\":\"arius.gateway.config\",\"status\":1}";

    static {
        headerParams.put(QueryConsts.GATEWAY_GET_APP_TICKET_NAME, QueryConsts.GATEWAY_GET_APP_TICKET);
    }

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
        requestParams.put(SCHEMA_DATACENTER_NAME, new String[]{"cn"});
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

    @Override
    public TempaletAliasResponse addAdminTemplateAlias(TemplateAlias templateAlias) {
        return HttpClient.forward(adminUrl + ADD_TEMPLATE_ALIAS, "POST", JSON.toJSONString(templateAlias), headerParams, null,
                TempaletAliasResponse.class);
    }

    @Override
    public TempaletAliasResponse delAdminTemplateAlias(TemplateAlias templateAlias) {
        return HttpClient.forward(adminUrl + DEL_TEMPLATE_ALIAS, "DELETE", JSON.toJSONString(templateAlias), headerParams, null,
                TempaletAliasResponse.class);
    }
}