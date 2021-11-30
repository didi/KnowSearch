package com.didichuxing.datachannel.arius.admin.util;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.workorder.content.TemplateCreateContent;
import com.didichuxing.datachannel.arius.admin.client.bean.common.GatewayHeartbeat;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.app.AppDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.app.AppTemplateAuthDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.*;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.config.AriusConfigInfoDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.monitor.AppMonitorRuleDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.oprecord.OperateRecordDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.IndexTemplateLogicDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.IndexTemplatePhysicalDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.user.AriusUserInfoDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.app.AppTemplateAuthEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterNodeRoleEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterNodeStatusEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterTypeEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ResourceLogicTypeEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.template.TemplateDeployRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.po.app.AppPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.ecm.ESMachineNormsPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateLogicPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplatePhysicalPO;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.DataCenterEnum;
import com.didichuxing.datachannel.arius.admin.common.util.TemplateUtils;
import org.springframework.mock.web.MockMultipartFile;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Stream;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.G_PER_SHARD;
import static com.didichuxing.datachannel.arius.admin.core.service.template.physic.impl.TemplatePhyServiceImpl.NOT_CHECK;

public class CustomDataSource {

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
        gatewayHeartbeat.setClusterName("wpk");
        gatewayHeartbeat.setHostName("www.wpk.com");
        gatewayHeartbeat.setPort(8080);
        return gatewayHeartbeat;
    }

    public static ESZeusConfigDTO esZeusConfigDTOFactory() {
        ESZeusConfigDTO esZeusConfigDTO = new ESZeusConfigDTO();
        esZeusConfigDTO.setClusterName("wpk");
        esZeusConfigDTO.setEnginName("engin");
        esZeusConfigDTO.setTypeName("es");
        esZeusConfigDTO.setContent("");
        esZeusConfigDTO.setClusterId(1l);
        return esZeusConfigDTO;
    }

    public static ClusterPhy esClusterPhyFactory() {
        ClusterPhy clusterPhy = new ClusterPhy();
        clusterPhy.setId(1);
        clusterPhy.setCluster("wpk");
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
        esPackageDTO.setEsVersion("7.6.0");
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

    public static ESPluginDTO esPluginDTOFactory() {
        ESPluginDTO esPluginDTO = new ESPluginDTO();
        esPluginDTO.setVersion("1.1.1.1000");
        esPluginDTO.setName("");
        esPluginDTO.setCreator("wpk");
        esPluginDTO.setDesc("test");
        esPluginDTO.setFileName("");
        esPluginDTO.setUrl("");
        esPluginDTO.setMd5("");
        return esPluginDTO;
    }

    public static ESClusterDTO esClusterDTOFactory() {
        ESClusterDTO esClusterDTO = new ESClusterDTO();
        esClusterDTO.setCluster("wpk");
        esClusterDTO.setDesc("test");
        esClusterDTO.setHttpAddress("1234");
        esClusterDTO.setType(ESClusterTypeEnum.ES_DOCKER.getCode());
        esClusterDTO.setDataCenter(DataCenterEnum.CN.getCode());
        esClusterDTO.setIdc("a test");
        esClusterDTO.setEsVersion("7.6.0.0");
        esClusterDTO.setTemplateSrvs("1,2,3");
        esClusterDTO.setPackageId(1l);
        return esClusterDTO;
    }

    public static TemplatePhysicalPO templatePhysicalSource() {
        TemplatePhysicalPO po = new TemplatePhysicalPO();
        po.setLogicId(1);
        po.setName("test");
        po.setExpression("1");
        po.setCluster("c1");
        po.setRack("1");
        po.setShard(1);
        po.setShardRouting(1);
        po.setVersion(1);
        po.setRole(1);
        po.setStatus(1);
        po.setConfig("{}");
        return po;
    }

    public static TemplateLogicPO templateLogicSource() {
        TemplateLogicPO po = new TemplateLogicPO();
        po.setAppId(1);
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
        return po;
    }

    public static ESRoleClusterDTO esRoleClusterDTOFactory() {
        ESRoleClusterDTO esRoleClusterDTO = new ESRoleClusterDTO();
        esRoleClusterDTO.setRoleClusterName("wpk");
        esRoleClusterDTO.setRole(ESClusterNodeRoleEnum.CLIENT_NODE.getDesc());
        esRoleClusterDTO.setMachineSpec("");
        esRoleClusterDTO.setPlugIds("");
        esRoleClusterDTO.setElasticClusterId(12345l);
        esRoleClusterDTO.setPodNumber(3);
        esRoleClusterDTO.setCfgId(1);
        esRoleClusterDTO.setPlugIds("");
        esRoleClusterDTO.setEsVersion("");
        esRoleClusterDTO.setPidCount(1);
        return esRoleClusterDTO;
    }

    public static ESRoleClusterHostDTO esRoleClusterHostDTOFactory() {
        ESRoleClusterHostDTO esRoleClusterHostDTO = new ESRoleClusterHostDTO();
        esRoleClusterHostDTO.setCluster("wpk");
        esRoleClusterHostDTO.setIp("127.0.0.0");
        esRoleClusterHostDTO.setRack("");
        esRoleClusterHostDTO.setHostname("wpk");
        esRoleClusterHostDTO.setPort("8080");
        esRoleClusterHostDTO.setRole(ESClusterNodeRoleEnum.DATA_NODE.getCode());
        esRoleClusterHostDTO.setStatus(ESClusterNodeStatusEnum.ONLINE.getCode());
        esRoleClusterHostDTO.setRoleClusterId(1234l);
        esRoleClusterHostDTO.setNodeSet("");
        return esRoleClusterHostDTO;
    }

    public static ESLogicClusterDTO esLogicClusterDTOFactory() {
        ESLogicClusterDTO esLogicClusterDTO = new ESLogicClusterDTO();
        esLogicClusterDTO.setName("wpk");
        esLogicClusterDTO.setAppId(123);
        esLogicClusterDTO.setResponsible("wpk");
        esLogicClusterDTO.setType(ResourceLogicTypeEnum.EXCLUSIVE.getCode());
        esLogicClusterDTO.setQuota(3d);
        esLogicClusterDTO.setMemo("Test");
        return esLogicClusterDTO;
    }


    public static ClusterRegionDTO clusterRegionDTOFactory() {
        ClusterRegionDTO clusterRegionDTO = new ClusterRegionDTO();
        clusterRegionDTO.setLogicClusterId(AdminConstant.REGION_NOT_BOUND_LOGIC_CLUSTER_ID);
        clusterRegionDTO.setPhyClusterName("wpk");
        clusterRegionDTO.setRacks("r1,r2");
        return clusterRegionDTO;
    }

    public static ClusterSettingDTO clusterSettingDTOFactory() {
        ClusterSettingDTO clusterSettingDTO = new ClusterSettingDTO();
        clusterSettingDTO.setClusterName("logi-elasticsearch-7.6.0");
        clusterSettingDTO.setKey("cluster.routing.allocation.balance.index");
        clusterSettingDTO.setValue("0.61");
        return clusterSettingDTO;
    }

    public static AppTemplateAuthDTO appTemplateAuthDTOFactory() {
        AppTemplateAuthDTO appTemplateAuthDTO = new AppTemplateAuthDTO();
        appTemplateAuthDTO.setAppId(3);
        appTemplateAuthDTO.setTemplateId(19481);
        appTemplateAuthDTO.setType(AppTemplateAuthEnum.RW.getCode());
        appTemplateAuthDTO.setResponsible("admin");
        return appTemplateAuthDTO;
    }

    public static IndexTemplateLogicDTO indexTemplateLogicDTOFactory() {
        IndexTemplateLogicDTO indexTemplateLogicDTO = new IndexTemplateLogicDTO();
        indexTemplateLogicDTO.setName("wpkTest-1");
        indexTemplateLogicDTO.setAppId(1);
        indexTemplateLogicDTO.setDataType(1);
        indexTemplateLogicDTO.setDateFormat("_yyyy-MM-dd");
        indexTemplateLogicDTO.setExpression("wpkTest-1*");
        indexTemplateLogicDTO.setDateField("timeStamp");
        indexTemplateLogicDTO.setResponsible("admin");
        indexTemplateLogicDTO.setDataCenter("cn");
        indexTemplateLogicDTO.setQuota(30D);

        return indexTemplateLogicDTO;
    }
}
