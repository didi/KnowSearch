package com.didi.arius.gateway.util;

import static com.didi.arius.gateway.common.metadata.AppDetail.RequestType.CLUSTER;

import com.didi.arius.gateway.common.consts.QueryConsts;
import com.didi.arius.gateway.common.metadata.AppDetail;
import com.didi.arius.gateway.common.metadata.BaseContext;
import com.didi.arius.gateway.common.metadata.ESCluster;
import com.didi.arius.gateway.common.metadata.IndexTemplate;
import com.didi.arius.gateway.common.metadata.QueryContext;
import com.didi.arius.gateway.common.metadata.TemplateClusterInfo;
import com.didi.arius.gateway.common.metadata.TemplateInfo;
import com.didi.arius.gateway.elasticsearch.client.ESClient;
import com.didi.arius.gateway.remote.response.AliasesInfoResponse;
import com.didi.arius.gateway.remote.response.AppDetailResponse;
import com.didi.arius.gateway.remote.response.AppListResponse;
import com.didi.arius.gateway.remote.response.BaseInfoResponse;
import com.didi.arius.gateway.remote.response.DSLTemplateListResponse;
import com.didi.arius.gateway.remote.response.DSLTemplateResponse;
import com.didi.arius.gateway.remote.response.DSLTemplateWrapResponse;
import com.didi.arius.gateway.remote.response.DataCenterListResponse;
import com.didi.arius.gateway.remote.response.DataCenterResponse;
import com.didi.arius.gateway.remote.response.DynamicConfigListResponse;
import com.didi.arius.gateway.remote.response.DynamicConfigResponse;
import com.didi.arius.gateway.remote.response.IndexTemplateListResponse;
import com.didi.arius.gateway.remote.response.IndexTemplateResponse;
import com.didi.arius.gateway.remote.response.MasterInfoResponse;
import com.didi.arius.gateway.remote.response.SlaveInfoResponse;
import com.didi.arius.gateway.remote.response.TempaletAliasResponse;
import com.didi.arius.gateway.remote.response.TemplateInfoListResponse;
import com.didi.arius.gateway.remote.response.TemplateInfoResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

/**
 * @author wuxuan
 * @Date 2022/6/6
 */
public class CustomDataSource {
    public static final String PHY_CLUSTER_NAME = "gateway_test_1";
    public static final String CLUSTER_NAME = "dc-es02";
    public static final String ip = "127.0.0.0" ;
    public static final String INDEX_NAME = "cn_record.arius.template.value";
    public static final String INDEX_NAME2 = "cn_record.arius.template.value_2021-05";
    public static final String INDEX_NAME3 = "cn_record.arius.template.value_2022-06";
    public static final String ALIAS = "ALIAS";
    public static final int appid = 1;

    public static AppListResponse appListResponseFactory() {
        AppListResponse appListResponse = new AppListResponse();
        appListResponse.setCode(1);
        appListResponse.setMessage("message");
        appListResponse.setVersion("1.2.2");
        List<AppDetailResponse> data = new ArrayList<>();
        data.add(appDetailResponseFactory());
        appListResponse.setData(data);
        return appListResponse;
    }

    public static AppDetailResponse appDetailResponseFactory() {
        AppDetailResponse appDetailResponse = new AppDetailResponse();
        appDetailResponse.setCluster(CLUSTER_NAME);
        appDetailResponse.setQueryThreshold(1);
        appDetailResponse.setId(appid);
        appDetailResponse.setVerifyCode("789");
        List<String> ips = new ArrayList<>();
        ips.add(ip);
        appDetailResponse.setIp(ips);
        return appDetailResponse;
    }

    public static BaseContext baseContextFactory() {
        BaseContext baseContext = queryContextFactory();
        baseContext.setAuthentication("Basic MTo3ODk=");
        baseContext.setAppid(appid);
        baseContext.setAppDetail(appDetailFactory());
        return baseContext;
    }
    public static AppDetail appDetailFactory() {
        AppDetail appDetail = new AppDetail();
        appDetail.setId(appid);
        appDetail.setCluster(PHY_CLUSTER_NAME);
        appDetail.setSearchType(CLUSTER);
        List<String> ips = new ArrayList<String>();
        ips.add(ip);
        appDetail.setIp(ips);
        List<String> windexexp =new ArrayList<>();
        windexexp.add(INDEX_NAME);
        windexexp.add(INDEX_NAME2);
        List<String> indexexp =new ArrayList<>();
        indexexp.add(INDEX_NAME);
        indexexp.add(INDEX_NAME2);
        appDetail.setWindexExp(windexexp);
        appDetail.setIndexExp(indexexp);
        appDetail.setIsRoot(0);
        return appDetail;
    }

    public static DSLTemplateResponse dslTemplateResponseFactory(){
        final DSLTemplateResponse dslTemplateResponse = new DSLTemplateResponse();
        dslTemplateResponse.setKey("key");
        dslTemplateResponse.setAriusCreateTime("ariusCreateTime");
        dslTemplateResponse.setAriusModifyTime("ariusModifyTime");
        dslTemplateResponse.setResponseLenAvg(1.0);
        dslTemplateResponse.setRequestType("requestType");
        dslTemplateResponse.setSearchType("searchType");
        dslTemplateResponse.setEsCostAvg(1.0);
        dslTemplateResponse.setTotalHitsAvg(1.0);
        dslTemplateResponse.setTotalShardsAvg(1.0);
        dslTemplateResponse.setQueryLimit(1.0);
        dslTemplateResponse.setEnable(false);
        dslTemplateResponse.setCheckMode("checkMode");
        return dslTemplateResponse;
    }

    public static DynamicConfigResponse dynamicConfigResponseFactory(){
        DynamicConfigResponse dynamicConfigResponse = new DynamicConfigResponse();
        dynamicConfigResponse.setCreateTime(0L);
        dynamicConfigResponse.setDimension(0L);
        dynamicConfigResponse.setEdit(0L);
        dynamicConfigResponse.setId(0L);
        dynamicConfigResponse.setMemo("memo");
        dynamicConfigResponse.setStatus(0L);
        dynamicConfigResponse.setUpdateTime(0L);
        dynamicConfigResponse.setValue("{\n" +
                "    \"appids\":[\n" +
                "        1,\n" +
                "        2,\n" +
                "        3,\n" +
                "        4\n" +
                "    ],\n" +
                "    \"didi-123deMacBook-Pro.local\":true\n" +
                "}");
        dynamicConfigResponse.setValueGroup("valueGroup");
        dynamicConfigResponse.setValueName(QueryConsts.DETAIL_LOG_FLAG);
        return dynamicConfigResponse;
    }

    public static DynamicConfigListResponse dynamicConfigListResponseFactory() {
        DynamicConfigListResponse dynamicConfigListResponse = new DynamicConfigListResponse();
        dynamicConfigListResponse.setCode(0);
        List<DynamicConfigResponse> data = new ArrayList<>();
        DynamicConfigResponse dynamicConfigResponse = CustomDataSource.dynamicConfigResponseFactory();
        data.add(dynamicConfigResponse);
        DynamicConfigResponse dynamicConfigResponseOne = CustomDataSource.dynamicConfigResponseFactory();
        dynamicConfigResponseOne.setValueName(null);
        data.add(dynamicConfigResponseOne);
        DynamicConfigResponse dynamicConfigResponseTwo = CustomDataSource.dynamicConfigResponseFactory();
        dynamicConfigResponseTwo.setValueName(QueryConsts.MAPPING_INDEXNAME_WHITE_APPIDS);
        data.add(dynamicConfigResponseTwo);
        DynamicConfigResponse dynamicConfigResponseThree = CustomDataSource.dynamicConfigResponseFactory();
        dynamicConfigResponseThree.setValueName(QueryConsts.FORBIDDEN_SETTING_PATH);
        data.add(dynamicConfigResponseThree);
        DynamicConfigResponse dynamicConfigResponseFour = CustomDataSource.dynamicConfigResponseFactory();
        dynamicConfigResponseFour.setValue("value");
        data.add(dynamicConfigResponseFour);
        DynamicConfigResponse dynamicConfigResponseFive = CustomDataSource.dynamicConfigResponseFactory();
        dynamicConfigResponseFive.setValue("{\n" +
                "    \"appids\":[\n" +
                "\n" +
                "    ]\n" +
                "}");
        data.add(dynamicConfigResponseFive);
        dynamicConfigListResponse.setData(data);
        return dynamicConfigListResponse;
    }

    public static QueryContext queryContextFactory(){
        QueryContext queryContext = new QueryContext();
        queryContext.setClusterId("clusterId");
        queryContext.setAppid(0);
        queryContext.setClusterName(CLUSTER_NAME);
        queryContext.setRequest(null);
        queryContext.setChannel(null);
        queryContext.setResponse(null);
        queryContext.setIndices(Arrays.asList("value"));
        queryContext.setXUserName("xUserName");
        queryContext.setRestName("restName");
        queryContext.setClient(new ESClient(CLUSTER_NAME, "version"));
        queryContext.setClientVersion("clientVersion");
        queryContext.setFromKibana(false);
        queryContext.setNewKibana(false);
        queryContext.setPreQueryEsTime(0L);
        return queryContext;
    }

    public static IndexTemplate indexTemplateFactory(){
        IndexTemplate indexTemplate = new IndexTemplate();
        indexTemplate.setId(0);
        indexTemplate.setName(INDEX_NAME);
        indexTemplate.setDateField("dateField");
        indexTemplate.setDateFormat("dateFormat");
        indexTemplate.setExpireTime(0L);
        indexTemplate.setExpression("expression");
        indexTemplate.setIsDefaultRouting(false);
        indexTemplate.setVersion(0);
        indexTemplate.setDeployStatus(IndexTemplate.DeployStatus.MASTER_AND_SLAVE);
        TemplateClusterInfo masterInfo = new TemplateClusterInfo();
        masterInfo.setAccessApps(new HashSet<>(Arrays.asList(0)));
        masterInfo.setCluster(CLUSTER_NAME);
        indexTemplate.setMasterInfo(masterInfo);
        TemplateClusterInfo templateClusterInfo = new TemplateClusterInfo();
        templateClusterInfo.setAccessApps(new HashSet<>(Arrays.asList(0)));
        templateClusterInfo.setCluster(CLUSTER_NAME);
        indexTemplate.setSlaveInfos(Arrays.asList(templateClusterInfo));
        return indexTemplate;
    }

    public static DataCenterResponse dataCenterResponseFactory(){
        DataCenterResponse dataCenterResponse = new DataCenterResponse();
        dataCenterResponse.setId(0L);
        dataCenterResponse.setCluster(CLUSTER_NAME);
        dataCenterResponse.setReadAddress("readAddress");
        dataCenterResponse.setHttpAddress("httpAddress");
        dataCenterResponse.setWriteAddress("writeAddress");
        dataCenterResponse.setHttpWriteAddress("httpWriteAddress");
        dataCenterResponse.setDesc("desc");
        dataCenterResponse.setType(0);
        dataCenterResponse.setEsVersion("esVersion");
        dataCenterResponse.setPassword("password");
        dataCenterResponse.setRunMode(1);
        dataCenterResponse.setWriteAction("writeAction");
        return dataCenterResponse;
    }

    public static DataCenterListResponse dataCenterListResponseFactory(){
        DataCenterListResponse dataCenterListResponse = new DataCenterListResponse();
        List<DataCenterResponse> dataCenterResponses = new ArrayList<>();
        DataCenterResponse dataCenterResponse = dataCenterResponseFactory();
        dataCenterResponses.add(dataCenterResponse);
        DataCenterResponse dataCenterResponseOne = dataCenterResponseFactory();
        dataCenterResponseOne.setHttpAddress(null);
        dataCenterResponses.add(dataCenterResponseOne);
        DataCenterResponse dataCenterResponseTwo = dataCenterResponseFactory();
        dataCenterResponseTwo.setEsVersion(null);
        dataCenterResponses.add(dataCenterResponseTwo);
        DataCenterResponse dataCenterResponseThree = dataCenterResponseFactory();
        dataCenterResponseThree.setRunMode(0);
        dataCenterResponses.add(dataCenterResponseThree);
        dataCenterListResponse.setData(dataCenterResponses);
        return dataCenterListResponse;
    }

    public static BaseInfoResponse baseInfoResponseFactory() {
        BaseInfoResponse baseInfoResponse = new BaseInfoResponse();
        baseInfoResponse.setExpireTime(1L);
        baseInfoResponse.setExpression("expression*");
        baseInfoResponse.setIsDefaultRouting(true);
        baseInfoResponse.setDeployStatus(1);
        List<String> aliases = new ArrayList<>();
        aliases.add(INDEX_NAME);
        aliases.add(INDEX_NAME2);
        aliases.add(ALIAS);
        baseInfoResponse.setAliases(aliases);
        baseInfoResponse.setIngestPipeline("ingestpipeline");
        baseInfoResponse.setId(1);
        baseInfoResponse.setBlockRead(false);
        baseInfoResponse.setDateField("datafiled");
        baseInfoResponse.setBlockWrite(false);
        baseInfoResponse.setDateFormat("dataformat");
        baseInfoResponse.setVersion(1);
        return baseInfoResponse;
    }

    public static MasterInfoResponse masterInfoResponseFactory(){
        MasterInfoResponse masterInfoResponse = new MasterInfoResponse();
        masterInfoResponse.setCluster(CLUSTER_NAME);
        List<Integer> accessApps = new ArrayList<>();
        accessApps.add(1);
        accessApps.add(2);
        masterInfoResponse.setAccessApps(accessApps);
        masterInfoResponse.setTopic("topic");
        masterInfoResponse.setMappingIndexNameEnable(true);
        Map<String, String> typeIndexMapping = new HashMap<>();
        typeIndexMapping.put(CLUSTER_NAME,INDEX_NAME);
        masterInfoResponse.setTypeIndexMapping(typeIndexMapping);
        masterInfoResponse.setTemplateId(1L);
        masterInfoResponse.setTemplateName("templateName");
        return masterInfoResponse;
    }

    public static List<SlaveInfoResponse> slaveInfoResponsesFactory() {
        List<SlaveInfoResponse> slaveInfoResponses = new ArrayList<>();
        SlaveInfoResponse slaveInfoResponse = new SlaveInfoResponse();
        List<Integer> accessApps = new ArrayList<>();
        accessApps.add(1);
        accessApps.add(2);
        slaveInfoResponse.setAccessProjects(accessApps);
        slaveInfoResponse.setCluster(CLUSTER_NAME);
        slaveInfoResponse.setTopic("topic");
        slaveInfoResponse.setMappingIndexNameEnable(true);
        slaveInfoResponse.setTemplateId(1L);
        slaveInfoResponse.setTemplateName("templateName");
        Map<String, String> typeIndexMapping = new HashMap<>();
        typeIndexMapping.put(CLUSTER_NAME,INDEX_NAME);
        slaveInfoResponse.setTypeIndexMapping(typeIndexMapping);
        slaveInfoResponses.add(slaveInfoResponse);
        return slaveInfoResponses;
    }

    public static IndexTemplateResponse indexTemplateResponseFactory(){
        IndexTemplateResponse indexTemplateResponse = new IndexTemplateResponse();
        indexTemplateResponse.setBaseInfo(baseInfoResponseFactory());
        indexTemplateResponse.setMasterInfo(masterInfoResponseFactory());
        indexTemplateResponse.setSlaveInfos(slaveInfoResponsesFactory());
        return indexTemplateResponse;
    }

    public static IndexTemplateListResponse indexTemplateListResponseFactory(){
        IndexTemplateListResponse indexTemplateListResponse = new IndexTemplateListResponse();
        indexTemplateListResponse.setMessage("message");
        Map<String, IndexTemplateResponse> data = new HashMap<>();
        IndexTemplateResponse indexTemplateResponse = indexTemplateResponseFactory();
        data.put("key",indexTemplateResponseFactory());
        indexTemplateListResponse.setData(data);
        indexTemplateListResponse.setCode(1);
        indexTemplateListResponse.setVersion("1.3.2");
        return indexTemplateListResponse;
    }

    public static TemplateInfoResponse templateInfoResponseFactory(){
        TemplateInfoResponse templateInfoResponse = new TemplateInfoResponse();
        templateInfoResponse.setExpression("expression");
        templateInfoResponse.setId(1);
        templateInfoResponse.setVersion(1);
        templateInfoResponse.setAliases(aliasesFactory());
        return templateInfoResponse;
    }

    public static TemplateInfoListResponse templateInfoListResponseFactory(){
        TemplateInfoListResponse templateInfoListResponse = new TemplateInfoListResponse();
        templateInfoListResponse.setMessage("message");
        Map<String, TemplateInfoResponse> data = new HashMap<>();
        data.put("key",templateInfoResponseFactory());
        templateInfoListResponse.setData(data);
        return templateInfoListResponse;
    }

    public static TemplateInfo templateInfoFactory(){
        TemplateInfo templateInfo = new TemplateInfo();
        templateInfo.setNeedSource(true);
        templateInfo.setVersion(1);
        return templateInfo;
    }

    public static TempaletAliasResponse tempaletAliasResponseFactory() {
        TempaletAliasResponse tempaletAliasResponse = new TempaletAliasResponse();
        tempaletAliasResponse.setVersion("version");
        tempaletAliasResponse.setMessage("messgage");
        tempaletAliasResponse.setCode(1);
        tempaletAliasResponse.setData(true);
        return tempaletAliasResponse;
    }

    public static List<AliasesInfoResponse> aliasesFactory() {
        List<AliasesInfoResponse> aliases = new ArrayList<>();
        AliasesInfoResponse aliasesInfoResponse = new AliasesInfoResponse();
        aliasesInfoResponse.setName(CLUSTER_NAME);
        aliases.add(aliasesInfoResponse);
        return aliases;
    }

    public static ESClient esClientFactory() {
        ESClient esClient = new ESClient();
        esClient.setClusterName(CLUSTER_NAME);
        esClient.addHttpHost("127.0.0.0",8080);
        List<Header> headers = new ArrayList<>();
        Header header = new BasicHeader("name","value");
        headers.add(header);
        esClient.setHeaders(headers);
        esClient.start();
        return esClient;
    }

    public static ESCluster esClusterFactory() {
        ESCluster esCluster = new ESCluster();
        esCluster.setCluster(CustomDataSource.CLUSTER_NAME);
        esCluster.setType(ESCluster.Type.INDEX);
        esCluster.setEsVersion(QueryConsts.DEFAULT_ES_VERSION);
        esCluster.setEsClient(esClientFactory());
        return esCluster;
    }

    public static DSLTemplateListResponse dslTemplateListResponseFactory() {
        DSLTemplateListResponse dslTemplateListResponse = new DSLTemplateListResponse();
        DSLTemplateWrapResponse data = new DSLTemplateWrapResponse();
        DSLTemplateResponse dslTemplateResponse = CustomDataSource.dslTemplateResponseFactory();
        DSLTemplateResponse dslTemplateResponseOne = CustomDataSource.dslTemplateResponseFactory();
        dslTemplateResponseOne.setKey("key——key");
        List<DSLTemplateResponse> dslTemplatePoList = new ArrayList<>();
        dslTemplatePoList.add(dslTemplateResponse);
        dslTemplatePoList.add(dslTemplateResponseOne);
        data.setDslTemplatePoList(dslTemplatePoList);
        data.setScrollId("scrollId");
        dslTemplateListResponse.setData(data);
        return dslTemplateListResponse;
    }

    public static Map<String, IndexTemplate> indexTemplateMapFactory() {
        Map<String, IndexTemplate>  indexTemplateMap = new HashMap<>();
        indexTemplateMap.put(CustomDataSource.INDEX_NAME,indexTemplateFactory());
        indexTemplateMap.put(CustomDataSource.INDEX_NAME2,indexTemplateFactory());
        return indexTemplateMap;
    }

    public static Map<String, ESCluster> listESClusterFactory() {
        Map<String, ESCluster> listESCluster = new HashMap<>();
        listESCluster.put("key",CustomDataSource.esClusterFactory());
        ESCluster esCluster = CustomDataSource.esClusterFactory();
        esCluster.setType(ESCluster.Type.SOURCE);
        listESCluster.put(CLUSTER_NAME,esCluster);
        return listESCluster;
    }

}