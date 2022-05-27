package com.didichuxing.datachannel.arius.admin.util;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.bean.common.GatewayHeartbeat;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.AppDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.AppTemplateAuthDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.*;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.config.AriusConfigInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.monitor.AppMonitorRuleDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.oprecord.OperateRecordDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.alias.IndexTemplateAliasDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.user.AriusUserInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.constant.app.AppClusterLogicAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.app.AppTemplateAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterResourceTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppClusterLogicAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppTemplateAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.po.app.AppPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.arius.AriusUserInfoPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.config.AriusConfigInfoPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.ecm.ESMachineNormsPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.esplugin.PluginPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.gateway.GatewayClusterNodePO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.metrics.UserMetricsConfigPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.operaterecord.OperateRecordInfoPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.*;
import com.didichuxing.datachannel.arius.admin.common.constant.DataCenterEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.PluginTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.RunModeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterDynamicConfigsEnum;
import org.springframework.mock.web.MockMultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class CustomDataSource {

    public static final String PHY_CLUSTER_NAME = "admin_test_1";
    public static final String PHY_CLUSTER_NAME_LOGI="logi-em-matedata-cluster";

    public static final String OPERATOR = "admin";

    public static final int SIZE = 10;

    public static <T> Stream<T> fromJSON(String json, Class<T> cls) {
        return Stream.of(JSON.parseObject(json, cls));
    }

    public static Stream<AppPO> appPOSource() {
        return fromJSON("{\"id\": null,\"name\": \"test\",\"isRoot\": 1,\"verifyCode\": \"1\",\"department\": \"1\",\"departmentId\": \"1\",\"responsible\": \"1\",\"memo\": \"1\",\"queryThreshold\": 100,\"cluster\": \"\",\"searchType\": 0,\"dataCenter\": \"\"}", AppPO.class);
    }

    public static Stream<AppDTO> appDTOSource() {
        return fromJSON("{\"id\": null,\"name\": \"test\",\"isRoot\": 1,\"verifyCode\": \"1\",\"department\": \"1\",\"departmentId\": \"1\",\"responsible\": \"1\",\"memo\": \"1\",\"queryThreshold\": 100,\"cluster\": \"\",\"searchType\": 0,\"dataCenter\": \"\"}", AppDTO.class);
    }

    public static AriusUserInfoDTO ariusUserInfoDTOFactory() {
        AriusUserInfoDTO ariusUserInfoDTO = new AriusUserInfoDTO();
        ariusUserInfoDTO.setEmail("");
        ariusUserInfoDTO.setMobile("");
        ariusUserInfoDTO.setStatus(1);
        ariusUserInfoDTO.setDomainAccount("wpk");
        ariusUserInfoDTO.setName("wpk");
        ariusUserInfoDTO.setPassword("1");
        ariusUserInfoDTO.setRole(2);
        return ariusUserInfoDTO;
    }

    public static AriusConfigInfoDTO ariusConfigInfoDTOFactory() {
        AriusConfigInfoDTO configInfoDTO = new AriusConfigInfoDTO();
        configInfoDTO.setValue("1234");
        configInfoDTO.setValueName("wp");
        configInfoDTO.setValueGroup("1");
        configInfoDTO.setDimension(1);
        configInfoDTO.setMemo("");
        configInfoDTO.setStatus(1);
        return configInfoDTO;
    }

    public static OperateRecordDTO OperateRecordDTOFatory() {
        OperateRecordDTO operateRecordDTO = new OperateRecordDTO();
        operateRecordDTO.setBizId("12");
        operateRecordDTO.setContent("");
        operateRecordDTO.setModuleId(2);
        operateRecordDTO.setOperateId(9);
        operateRecordDTO.setOperator("wpk");
        return operateRecordDTO;
    }

    public static AppMonitorRuleDTO appMonitorRuleDTOFactory() {
        AppMonitorRuleDTO appMonitorRuleDTO = new AppMonitorRuleDTO();
        appMonitorRuleDTO.setAppId(1L);
        appMonitorRuleDTO.setName("add");
        return appMonitorRuleDTO;
    }

    public static GatewayHeartbeat gatewayHeartbeatFactory()  {
        GatewayHeartbeat gatewayHeartbeat = new GatewayHeartbeat();
        gatewayHeartbeat.setClusterName(PHY_CLUSTER_NAME);
        gatewayHeartbeat.setHostName("www.wpk.com");
        gatewayHeartbeat.setPort(8080);
        return gatewayHeartbeat;
    }

    public static ESZeusConfigDTO esZeusConfigDTOFactory() {
        ESZeusConfigDTO esZeusConfigDTO = new ESZeusConfigDTO();
        esZeusConfigDTO.setClusterName(PHY_CLUSTER_NAME);
        esZeusConfigDTO.setEnginName("engin");
        esZeusConfigDTO.setTypeName("es");
        esZeusConfigDTO.setContent("");
        esZeusConfigDTO.setClusterId(1l);
        return esZeusConfigDTO;
    }

    public static ClusterPhy esClusterPhyFactory() {
        ClusterPhy clusterPhy = new ClusterPhy();
        clusterPhy.setId(1);
        clusterPhy.setCluster(PHY_CLUSTER_NAME);
        clusterPhy.setPlugIds("1,2,3,4,5");
        return clusterPhy;
    }

    public static ESConfigDTO esConfigDTOFactory() {
        ESConfigDTO esConfigDTO = new ESConfigDTO();
        esConfigDTO.setClusterId(1l);
        esConfigDTO.setEnginName("wpkEngin");
        esConfigDTO.setTypeName("wpk");
        esConfigDTO.setVersionConfig(1);
        esConfigDTO.setDesc("");
        esConfigDTO.setSelected(1);
        esConfigDTO.setConfigData("you are right");
        esConfigDTO.setVersionTag("1.0");
        esConfigDTO.setVersionConfig(1);
        return esConfigDTO;
    }

    public static ESPackageDTO esPackageDTOFactory() {
        ESPackageDTO esPackageDTO = new ESPackageDTO();
        esPackageDTO.setUrl("www.baidu.com");
        esPackageDTO.setCreator("wpk");
        esPackageDTO.setDesc("");
        esPackageDTO.setEsVersion("7.6.0.0");
        esPackageDTO.setFileName("wpk");
        esPackageDTO.setManifest(3);
        esPackageDTO.setMd5("");
        esPackageDTO.setUploadFile(new MockMultipartFile("wpk",new byte[3]));
        return esPackageDTO;
    }

    public static ESMachineNormsPO esMachineNormsPOFactory() {
        ESMachineNormsPO esMachineNormsPO = new ESMachineNormsPO();
        esMachineNormsPO.setRole("wpk");
        esMachineNormsPO.setSpec("wpk");
        return esMachineNormsPO;
    }

    public static PluginDTO esPluginDTOFactory() {
        PluginDTO pluginDTO = new PluginDTO();
        pluginDTO.setVersion("1.1.1.1000");
        pluginDTO.setPDefault(PluginTypeEnum.DEFAULT_PLUGIN.getCode());
        pluginDTO.setName("test");
        pluginDTO.setCreator("wpk");
        pluginDTO.setDesc("test");
        pluginDTO.setFileName("test");
        pluginDTO.setUrl("");
        pluginDTO.setMd5("");
        pluginDTO.setUploadFile(new MockMultipartFile("test", new byte[]{0, 1, 2}));
        return pluginDTO;
    }

    public static ClusterPhyDTO esClusterDTOFactory() {
        ClusterPhyDTO esClusterDTO = new ClusterPhyDTO();
        esClusterDTO.setId(157);
        esClusterDTO.setCluster("lyn-test-public12-08");
        esClusterDTO.setDesc("test");
        esClusterDTO.setHttpAddress("1234");
        esClusterDTO.setType(ESClusterTypeEnum.ES_DOCKER.getCode());
        esClusterDTO.setDataCenter(DataCenterEnum.CN.getCode());
        esClusterDTO.setIdc("a test");
        esClusterDTO.setEsVersion("7.6.0.0");
        esClusterDTO.setTemplateSrvs("1,2,3");
        esClusterDTO.setImageName("test");
        esClusterDTO.setCreator("wpk");
        esClusterDTO.setLevel(0);
        esClusterDTO.setPackageId(1L);
        esClusterDTO.setRunMode(RunModeEnum.READ_WRITE_SHARE.getRunMode());
        esClusterDTO.setHttpWriteAddress("2.0.0.0");
        esClusterDTO.setWriteAddress("2.0.0.0");
        esClusterDTO.setReadAddress("2.0.0.0");
        esClusterDTO.setNsTree("test");
        esClusterDTO.setHealth(1);
        esClusterDTO.setPassword("");
        return esClusterDTO;
    }

    public static TemplateAliasPO templateAliasSource() {
        TemplateAliasPO po = new TemplateAliasPO();
        po.setId(1);
        po.setName("test");
        po.setLogicId(1);
        po.setLogicId(1);
        return po;
    }

    public static IndexTemplateAliasDTO indexTemplateAliasDTOFactory() {
        IndexTemplateAliasDTO dto = new IndexTemplateAliasDTO();
        dto.setName("test");
        dto.setLogicId(1);
        return dto;
    }

    public static IndexTemplateConfigDTO indexTemplateConfigDTOFactory() {
        IndexTemplateConfigDTO dto = new IndexTemplateConfigDTO();
        dto.setId(1L);
        dto.setLogicId(1);
        dto.setDynamicLimitEnable(1);
        dto.setMappingImproveEnable(1);
        dto.setAdjustRackTpsFactor(1d);
        dto.setIsSourceSeparated(1);
        dto.setDynamicLimitEnable(1);
        dto.setDisableIndexRollover(true);
        return dto;
    }

    public static TemplateConfigPO templateConfigSource() {
        TemplateConfigPO po = new TemplateConfigPO();
        po.setLogicId(1);
        po.setId(1L);
        po.setAdjustRackShardFactor(1d);
        return po;
    }

    public static IndexTemplatePhyPO templatePhysicalSource() {
        IndexTemplatePhyPO po = new IndexTemplatePhyPO();
        po.setLogicId(1);
        po.setName("test");
        po.setExpression("1");
        po.setCluster(PHY_CLUSTER_NAME);
        po.setRack("1");
        po.setShard(1);
        po.setShardRouting(1);
        po.setVersion(1);
        po.setRole(1);
        po.setStatus(1);
        po.setConfig("{}");
        return po;
    }

    public static IndexTemplatePO templateLogicSource() {
        IndexTemplatePO po = new IndexTemplatePO();
        po.setId(1);
        po.setProjectId(1);
        po.setName("test");
        po.setDataType(1);
        po.setDateFormat("");
        po.setDataCenter("");
        po.setExpireTime(3);
        po.setHotTime(3);
        po.setLibraDepartmentId("");
        po.setLibraDepartment("");
        po.setResponsible("");
        po.setDateField("");
        po.setDateFieldFormat("");
        po.setIdField("");
        po.setRoutingField("");
        po.setExpression("");
        po.setDesc("");
        po.setQuota(0D);
        po.setIngestPipeline("");
        po.setWriteRateLimit(-1);
        po.setRegionId(100);
        return po;
    }

    public static ESClusterRoleDTO esRoleClusterDTOFactory() {
        ESClusterRoleDTO esClusterRoleDTO = new ESClusterRoleDTO();
        esClusterRoleDTO.setRoleClusterName("wpk");
        esClusterRoleDTO.setRole(ESClusterNodeRoleEnum.CLIENT_NODE.getDesc());
        esClusterRoleDTO.setMachineSpec("");
        esClusterRoleDTO.setPlugIds("");
        esClusterRoleDTO.setElasticClusterId(12345L);
        esClusterRoleDTO.setPodNumber(3);
        esClusterRoleDTO.setCfgId(1);
        esClusterRoleDTO.setPlugIds("");
        esClusterRoleDTO.setEsVersion("");
        esClusterRoleDTO.setPidCount(1);
        return esClusterRoleDTO;
    }

    public static ESClusterRoleHostDTO esRoleClusterHostDTOFactory() {
        ESClusterRoleHostDTO esClusterRoleHostDTO = new ESClusterRoleHostDTO();
        esClusterRoleHostDTO.setCluster("test_cluster");
        esClusterRoleHostDTO.setIp("127.0.0.0");
        esClusterRoleHostDTO.setRack("r1");
        esClusterRoleHostDTO.setHostname("wpk");
        esClusterRoleHostDTO.setPort("8080");
        esClusterRoleHostDTO.setRole(ESClusterNodeRoleEnum.DATA_NODE.getCode());
        esClusterRoleHostDTO.setStatus(ESClusterNodeStatusEnum.ONLINE.getCode());
        esClusterRoleHostDTO.setRoleClusterId(1234L);
        esClusterRoleHostDTO.setNodeSet("");
        esClusterRoleHostDTO.setRegionId(100);
        return esClusterRoleHostDTO;
    }

    public static ESLogicClusterDTO esLogicClusterDTOFactory() {
        ESLogicClusterDTO esLogicClusterDTO = new ESLogicClusterDTO();
        esLogicClusterDTO.setName("wpkTest");
        esLogicClusterDTO.setAppId(123);
        esLogicClusterDTO.setResponsible("wpk");
        esLogicClusterDTO.setType(ClusterResourceTypeEnum.EXCLUSIVE.getCode());
        esLogicClusterDTO.setQuota(3d);
        esLogicClusterDTO.setMemo("Test");
        return esLogicClusterDTO;
    }


    public static ClusterRegionDTO clusterRegionDTOFactory() {
        ClusterRegionDTO clusterRegionDTO = new ClusterRegionDTO();
        //clusterRegionDTO.setLogicClusterId(AdminConstant.REGION_NOT_BOUND_LOGIC_CLUSTER_ID);
        clusterRegionDTO.setPhyClusterName("wpk");
        clusterRegionDTO.setRacks("r1,r2");
        return clusterRegionDTO;
    }

    public static ClusterSettingDTO clusterSettingDTOFactory() {
        ClusterSettingDTO clusterSettingDTO = new ClusterSettingDTO();
        clusterSettingDTO.setClusterName(PHY_CLUSTER_NAME);
        clusterSettingDTO.setKey(ClusterDynamicConfigsEnum.CLUSTER_ROUTING_ALLOCATION_BALANCE_INDEX.getName());
        clusterSettingDTO.setValue("0.61");
        return clusterSettingDTO;
    }

    public static AppTemplateAuthDTO appTemplateAuthDTOFactory() {
        AppTemplateAuthDTO appTemplateAuthDTO = new AppTemplateAuthDTO();
        appTemplateAuthDTO.setAppId(1);
        appTemplateAuthDTO.setTemplateId(1147);
        appTemplateAuthDTO.setType(AppTemplateAuthEnum.RW.getCode());
        appTemplateAuthDTO.setResponsible("admin");
        return appTemplateAuthDTO;
    }

    public static IndexTemplateDTO indexTemplateLogicDTOFactory() {
        IndexTemplateDTO indexTemplateDTO = new IndexTemplateDTO();
        indexTemplateDTO.setName("wpkTest-1");
        indexTemplateDTO.setProjectId(1);
        indexTemplateDTO.setDataType(1);
        indexTemplateDTO.setDateFormat("_yyyy-MM-dd");
        indexTemplateDTO.setExpression("wpkTest-1*");
        indexTemplateDTO.setDateField("timeStamp");
        indexTemplateDTO.setResponsible("admin");
        indexTemplateDTO.setDataCenter("cn");
        indexTemplateDTO.setQuota(30D);

        return indexTemplateDTO;
    }

    public static TemplateTypePO templateTypeSource() {
        TemplateTypePO po = new TemplateTypePO();
        po.setId(1);
        po.setName("test");
        return po;
    }

    public static List<TemplateAliasPO> getTemplateAliasPOList() {
        List<TemplateAliasPO> list = new ArrayList<>();
        for(int i = 0; i <= SIZE; i++) {
            TemplateAliasPO templateAliasPO = CustomDataSource.templateAliasSource();
            templateAliasPO.setName(templateAliasPO.getName() + i);
            list.add(templateAliasPO);
        }
        return list;
    }

    public static AppClusterLogicAuth appClusterLogicAuthSource() {
        AppClusterLogicAuth appClusterLogicAuth = new AppClusterLogicAuth();
        appClusterLogicAuth.setLogicClusterId(1L);
        appClusterLogicAuth.setAppId(1);
        appClusterLogicAuth.setId(1L);
        appClusterLogicAuth.setType(AppClusterLogicAuthEnum.ACCESS.getCode());
        appClusterLogicAuth.setResponsible("admin");
        return appClusterLogicAuth;
    }

    public static AppTemplateAuth appTemplateAuthSource() {
        AppTemplateAuth appTemplateAuth = new AppTemplateAuth();
        appTemplateAuth.setAppId(1);
        appTemplateAuth.setTemplateId(1);
        appTemplateAuth.setId(1l);
        return appTemplateAuth;
    }

    public static List<AppTemplateAuth> getAppTemplateAuthList() {
        List<AppTemplateAuth> list = new ArrayList<>();
        for(int i = 1; i <= SIZE; i++) {
            AppTemplateAuth po = CustomDataSource.appTemplateAuthSource();
            po.setTemplateId(i);
            po.setId((long) i);
            list.add(po);
        }
        return list;
    }


    public static List<IndexTemplatePO> getTemplateLogicPOList() {
        List<IndexTemplatePO> list = new ArrayList<>();
        for(int i = 1; i <= SIZE; i++) {
            IndexTemplatePO po = CustomDataSource.templateLogicSource();
            po.setId(i);
            po.setName(po.getName() + "i");
            list.add(po);
        }
        return list;
    }

    public static List<IndexTemplatePhyPO> getTemplatePhysicalPOList() {
        List<IndexTemplatePhyPO> list = new ArrayList<>();
        for(int i = 1; i <= SIZE; i++) {
            IndexTemplatePhyPO po = CustomDataSource.templatePhysicalSource();
            list.add(po);
        }
        return list;
    }

    public static List<TemplateTypePO> getTemplateTypePOList() {
        List<TemplateTypePO> list = new ArrayList<>();
        for(int i = 1; i <= SIZE; i++) {
            TemplateTypePO po = CustomDataSource.templateTypeSource();
            list.add(po);
        }
        return list;
    }

    public static List<AppClusterLogicAuth> getAppClusterLogicAuthList() {
        List<AppClusterLogicAuth> list = new ArrayList<>();
        for(int i = 1; i <= SIZE; i++) {
            AppClusterLogicAuth po = CustomDataSource.appClusterLogicAuthSource();
            po.setId((long) i);
            po.setLogicClusterId((long) i);
            list.add(po);
        }
        return list;
    }

    public static IndexTemplatePhy getIndexTemplatePhy() {
        IndexTemplatePhy indexTemplatePhy = new IndexTemplatePhy();
        indexTemplatePhy.setId(1L);
        indexTemplatePhy.setLogicId(1);
        return indexTemplatePhy;
    }

    public static List<IndexTemplatePhy> getIndexTemplatePhyList() {
        List<IndexTemplatePhy> list = new ArrayList<>();
        for(int i = 1; i <= SIZE; i++ ) {
            IndexTemplatePhy po = CustomDataSource.getIndexTemplatePhy();
            po.setId((long) i);
            po.setLogicId(i);
            po.setCluster(PHY_CLUSTER_NAME);
            list.add(po);
        }
        return list;
    }

    public static ClusterLogic getClusterLogic() {
        ClusterLogic clusterLogic = new ClusterLogic();
        clusterLogic.setId(1L);
        clusterLogic.setName("test");
        return clusterLogic;
    }

    public static List<ClusterLogic> getClusterLogicList() {
        List<ClusterLogic> list = new ArrayList<>();
        for(int i = 1; i <= SIZE; i++ ) {
            ClusterLogic po = CustomDataSource.getClusterLogic();
            po.setId((long) i);
            po.setName("test" + i);
            list.add(po);
        }
        return list;
    }

    public static UserMetricsConfigPO getMetricsConfigPO() {
        UserMetricsConfigPO userMetricsConfigPO = new UserMetricsConfigPO();
        userMetricsConfigPO.setId(1);
        userMetricsConfigPO.setMetricInfo("[{\"domainAccount\":\"admin\",\"firstMetricsType\":\"cluster\",\"metricsTypes\":[\"cpuUsage\",\"cpuLoad1M\",\"cpuLoad5M\",\"cpuLoad15M\",\"diskUsage\",\"diskInfo\",\"nodesForDiskUsageGte75Percent\",\"recvTransSize\",\"sendTransSize\",\"readTps\",\"writeTps\",\"searchLatency\",\"indexingLatency\",\"shardNu\",\"movingShards\",\"bigShards\",\"bigIndices\",\"invalidNodes\",\"pendingTasks\"],\"secondMetricsType\":\"overview\"},{\"domainAccount\":\"admin\",\"firstMetricsType\":\"cluster\",\"metricsTypes\":[\"os-cpu-percent\",\"os-cpu-load_average-1m\",\"os-cpu-load_average-5m\",\"os-cpu-load_average-15m\",\"fs-total-disk_free_percent\",\"transport-tx_count_rate\",\"transport-rx_count_rate\",\"transport-tx_size_in_bytes_rate\",\"transport-rx_size_in_bytes_rate\",\"indices-indexing-index_total_rate\",\"indices-indexing-index_time_in_millis\",\"thread_pool-bulk-rejected\",\"thread_pool-bulk-queue\",\"indices-search-query_total_rate\",\"indices-search-fetch_total_rate\",\"indices-search-query_time_in_millis\",\"indices-search-fetch_time_in_millis\",\"thread_pool-search-queue\",\"thread_pool-search-rejected\",\"indices-search-scroll_current\",\"indices-search-scroll_time_in_millis\",\"indices-merges-total_time_in_millis\",\"indices-refresh-total_time_in_millis\",\"indices-flush-total_time_in_millis\",\"indices-query_cache-hit_count\",\"indices-query_cache-miss_count\",\"indices-request_cache-hit_count\",\"indices-request_cache-miss_count\",\"http-current_open\",\"indices-segments-count\",\"indices-segments-memory_in_bytes\",\"indices-segments-term_vectors_memory_in_bytes\",\"indices-segments-points_memory_in_bytes\",\"indices-segments-doc_values_memory_in_bytes\",\"indices-segments-index_writer_memory_in_bytes\",\"indices-docs-count\",\"indices-store-size_in_bytes\",\"indices-translog-uncommitted_size_in_bytes\",\"indices-query_cache-memory_size_in_bytes\",\"indices-request_cache-memory_size_in_bytes\",\"jvm-gc-young-collection_count_rate\",\"jvm-gc-old-collection_count_rate\",\"jvm-gc-young-collection_time_in_millis\",\"jvm-gc-old-collection_time_in_millis\",\"jvm-mem-heap_used_in_bytes\",\"jvm-mem-non_heap_used_in_bytes\",\"jvm-mem-heap_used_percent\"],\"secondMetricsType\":\"node\"},{\"domainAccount\":\"admin\",\"firstMetricsType\":\"user_show\",\"metricsTypes\":[\"docsCount\",\"docsDeleted\",\"priStoreSize\",\"storeSize\"],\"secondMetricsType\":\"indexSearch\"},{\"domainAccount\":\"admin\",\"firstMetricsType\":\"user_show\",\"metricsTypes\":[\"searchCount\",\"totalCostAvg\"],\"secondMetricsType\":\"dslTemplate\"},{\"domainAccount\":\"admin\",\"firstMetricsType\":\"cluster\",\"metricsTypes\":[\"shardNu\",\"store-size_in_bytes\",\"docs-count\",\"search-query_total_rate\",\"search-fetch_total_rate\",\"merges-total_rate\",\"refresh-total_rate\",\"flush-total_rate\",\"indexing-index_total_rate\",\"indexing-index_time_in_millis\",\"search-query_time_in_millis\",\"search-fetch_time_in_millis\",\"search-scroll_total_rate\",\"search-scroll_time_in_millis\",\"merges-total_time_in_millis\",\"refresh-total_time_in_millis\",\"flush-total_time_in_millis\",\"query_cache-memory_size_in_bytes\",\"segments-memory_in_bytes\",\"segments-term_vectors_memory_in_bytes\",\"segments-points_memory_in_bytes\",\"segments-doc_values_memory_in_bytes\",\"segments-index_writer_memory_in_bytes\",\"translog-size_in_bytes\"],\"secondMetricsType\":\"index\"}]");
        return userMetricsConfigPO;
    }

    public static GatewayClusterNodePO getGatewayNodePO() {
        GatewayClusterNodePO gatewayClusterNodePO = new GatewayClusterNodePO();
        gatewayClusterNodePO.setId(1);
        gatewayClusterNodePO.setClusterName(PHY_CLUSTER_NAME);
        return gatewayClusterNodePO;
    }

    public static List<GatewayClusterNodePO> getGatewayNodePOList() {
        List<GatewayClusterNodePO> list = new ArrayList<>();
        for(int i = 1; i <= SIZE; i++ ) {
            GatewayClusterNodePO po = CustomDataSource.getGatewayNodePO();
            po.setId(i);
            list.add(po);
        }
        return list;
    }

    public static PluginDTO getESPluginDTO() {
        PluginDTO pluginDTO = new PluginDTO();
        pluginDTO.setId(1L);
        pluginDTO.setDesc("test");
        pluginDTO.setName("name_test");
        pluginDTO.setPhysicClusterId("1");
        pluginDTO.setDesc("test_desc");
        pluginDTO.setUploadFile(new MockMultipartFile("test", new byte[10]));
        return pluginDTO;
    }

    public static PluginPO getESPluginPO() {
        PluginPO pluginPO = new PluginPO();
        pluginPO.setId(1L);
        pluginPO.setDesc("test");
        pluginPO.setName("name_test");
        pluginPO.setPhysicClusterId("1");
        pluginPO.setDesc("test_desc");
        return pluginPO;
    }

    public static List<PluginPO> getESPluginPOList() {
        List<PluginPO> list = new ArrayList<>();
        for(int i = 1; i <= SIZE; i++ ) {
            PluginPO po = CustomDataSource.getESPluginPO();
            po.setId((long) i);
            list.add(po);
        }
        return list;
    }

    public static OperateRecordInfoPO getOperateRecordPO() {
        OperateRecordInfoPO operateRecordPO = new OperateRecordInfoPO();
        operateRecordPO.setId(1);
        return operateRecordPO;
    }


    public static List<OperateRecordInfoPO> getOperateRecordPOList() {
        List<OperateRecordInfoPO> list = new ArrayList<>();
        for(int i = 1; i <= SIZE; i++ ) {
            OperateRecordInfoPO po = CustomDataSource.getOperateRecordPO();
            po.setId(i);
            list.add(po);
        }
        return list;
    }

    public static AriusUserInfoPO getAriusUserInfoPO() {
        AriusUserInfoPO ariusUserInfoPO = new AriusUserInfoPO();
        ariusUserInfoPO.setId(1L);
        return ariusUserInfoPO;
    }

    public static List<AriusUserInfoPO> getAriusUserInfoPOList() {
        List<AriusUserInfoPO> list = new ArrayList<>();
        for(int i = 1; i <= SIZE; i++ ) {
            AriusUserInfoPO po = CustomDataSource.getAriusUserInfoPO();
            po.setId((long) i);
            list.add(po);
        }
        return list;
    }

    public static AriusConfigInfoPO getAriusConfigInfoPO() {
        AriusConfigInfoPO ariusConfigInfoPO = new AriusConfigInfoPO();
        ariusConfigInfoPO.setId(1);
        ariusConfigInfoPO.setValueGroup("test");
        ariusConfigInfoPO.setValueName("test_name");
        return ariusConfigInfoPO;
    }

    public static List<AriusConfigInfoPO> getAriusConfigInfoPOList() {
        List<AriusConfigInfoPO> list = new ArrayList<>();
        for(int i = 1; i <= SIZE; i++ ) {
            AriusConfigInfoPO po = CustomDataSource.getAriusConfigInfoPO();
            po.setId(i);
            po.setValueGroup("test" + i);
            po.setValueName("test_name" + i);
            list.add(po);
        }
        return list;
    }
}