package com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESConfigDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESZeusConfigDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.esconfig.EsConfigActionEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.esconfig.ESConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.po.esconfig.ESConfigPO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats.AriusStatsDcdrInfoESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.ecm.ESClusterConfigDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * @author wpk
 * @date 2021/07/05
 */
@Transactional(timeout = 1000)
@Rollback
public class ESClusterConfigServiceTest extends AriusAdminApplicationTest {

    @Autowired
    private ESClusterConfigService esClusterConfigService;

    @MockBean
    private ClusterPhyService esClusterPhyService;

    @MockBean
    private OperateRecordService operateRecordService;

    @Autowired
    private ESClusterConfigDAO esClusterConfigDAO;

    /**
     * eSZeusConfigDTOFactory 生成ESZeusConfigDTO新的记录
     * 设计不同的数据覆盖校验的分支 执行正常的数据操作
     */
    @Test
    public void getZeusConfigContentTest() {
        ESZeusConfigDTO esZeusConfigDTO = CustomDataSource.esZeusConfigDTOFactory();
        esZeusConfigDTO.setTypeName("");
        Assertions.assertEquals("type name is empty",
                esClusterConfigService.getZeusConfigContent(esZeusConfigDTO, EsConfigActionEnum.ADD.getCode()).getMessage());
        esZeusConfigDTO.setEnginName("");
        Assertions.assertEquals("engin name is empty",
                esClusterConfigService.getZeusConfigContent(esZeusConfigDTO, EsConfigActionEnum.ADD.getCode()).getMessage());
        esZeusConfigDTO.setClusterName("");
        Assertions.assertEquals("cluster name is empty",
                esClusterConfigService.getZeusConfigContent(esZeusConfigDTO, EsConfigActionEnum.ADD.getCode()).getMessage());
        esZeusConfigDTO = CustomDataSource.esZeusConfigDTOFactory();
        Assertions.assertEquals("es clusterPhy is empty",
                esClusterConfigService.getZeusConfigContent(esZeusConfigDTO, EsConfigActionEnum.ADD.getCode()).getMessage());
        ESConfigDTO esConfigDTO = CustomDataSource.esConfigDTOFactory();
        Mockito.when(esClusterPhyService.getClusterByName(esZeusConfigDTO.getClusterName())).thenReturn(CustomDataSource.esClusterPhyFactory());
        esConfigDTO.setClusterId(CustomDataSource.esClusterPhyFactory().getId().longValue());
        esConfigDTO.setTypeName(esZeusConfigDTO.getTypeName());
        esConfigDTO.setEnginName(esZeusConfigDTO.getEnginName());
        Assertions.assertEquals("old es config is empty", esClusterConfigService.getZeusConfigContent(esZeusConfigDTO, EsConfigActionEnum.EDIT.getCode()).getMessage());
        esClusterConfigDAO.insert(ConvertUtil.obj2Obj(esConfigDTO, ESConfigPO.class));
        Assertions.assertTrue(esClusterConfigService.getZeusConfigContent(esZeusConfigDTO, EsConfigActionEnum.DELETE.getCode()).success());
    }

    /**
     * 原方法中存在硬编码的问题，导致单元测试的分支覆盖依赖于数据库
     */
    @Test
    public void listEsClusterConfigByClusterIdTest() {
        List<ESConfigPO> defaultConfigs = esClusterConfigDAO.listByClusterId(1L);
        Long clusterId = 1234L;
        Assertions.assertTrue(defaultConfigs.containsAll(esClusterConfigService.listEsClusterConfigByClusterId(clusterId).getData()));
        ESConfigDTO esConfigDTO = CustomDataSource.esConfigDTOFactory();
        esConfigDTO.setClusterId(clusterId);
        ESConfigPO esConfigPO = ConvertUtil.obj2Obj(esConfigDTO, ESConfigPO.class);
        esClusterConfigDAO.insert(esConfigPO);
        Assertions.assertTrue(esClusterConfigService.listEsClusterConfigByClusterId(clusterId).getData().stream().anyMatch(e -> e.getClusterId().equals(clusterId)));
    }

    /**
     * 需要进行大量的分支覆盖
     * 这里只有ADD和EDIT的操作
     */
    @Test
    public void esClusterConfigActionTest() {
        ESConfigDTO esConfigDTO = CustomDataSource.esConfigDTOFactory();
        EsConfigActionEnum actionEnum = EsConfigActionEnum.ADD;
        Assertions.assertEquals(Result.buildParamIllegal("esConfigDTO is empty").getMessage(),
                esClusterConfigService.esClusterConfigAction(null, actionEnum, CustomDataSource.OPERATOR).getMessage());
        Long clusterId = 1234L;
        esConfigDTO.setClusterId(clusterId);
        Long id = esClusterConfigService.esClusterConfigAction(esConfigDTO, actionEnum, CustomDataSource.OPERATOR).getData();
        Assertions.assertEquals(clusterId, esClusterConfigDAO.getById(id).getClusterId());
        Assertions.assertEquals(Result.buildParamIllegal("operator is empty").getMessage(),
                esClusterConfigService.esClusterConfigAction(esConfigDTO, actionEnum, "").getMessage());
        esConfigDTO.setClusterId(null);
        Assertions.assertEquals(Result.buildParamIllegal("clusterId name is empty").getMessage(),
                esClusterConfigService.esClusterConfigAction(esConfigDTO, actionEnum, CustomDataSource.OPERATOR).getMessage());
        esConfigDTO.setTypeName("");
        Assertions.assertEquals(Result.buildParamIllegal("type name is empty").getMessage(),
                esClusterConfigService.esClusterConfigAction(esConfigDTO, actionEnum, CustomDataSource.OPERATOR).getMessage());
        esConfigDTO.setEnginName("");
        Assertions.assertEquals(Result.buildParamIllegal("engin name is empty").getMessage(),
                esClusterConfigService.esClusterConfigAction(esConfigDTO, actionEnum, CustomDataSource.OPERATOR).getMessage());
        esConfigDTO.setConfigData("");
        Assertions.assertEquals(Result.buildParamIllegal("config data is empty").getMessage(),
                esClusterConfigService.esClusterConfigAction(esConfigDTO, actionEnum, CustomDataSource.OPERATOR).getMessage());
        esConfigDTO = CustomDataSource.esConfigDTOFactory();
        actionEnum = EsConfigActionEnum.EDIT;
        Assertions.assertEquals(Result.buildParamIllegal("id is empty").getMessage(),
                esClusterConfigService.esClusterConfigAction(esConfigDTO, actionEnum, CustomDataSource.OPERATOR).getMessage());
        esConfigDTO.setId(id + 1);
        Assertions.assertEquals(Result.buildParamIllegal("config is not exist, please create first").getMessage(),
                esClusterConfigService.esClusterConfigAction(esConfigDTO, actionEnum, CustomDataSource.OPERATOR).getMessage());
        esConfigDTO.setId(id);
        esConfigDTO.setDesc("just a test");
        esClusterConfigService.setConfigValid(id);
        id = esClusterConfigService.esClusterConfigAction(esConfigDTO, actionEnum, CustomDataSource.OPERATOR).getData();
        Assertions.assertEquals(esConfigDTO.getDesc(), esClusterConfigDAO.getById(id).getDesc());
    }

    @Test
    public void batchCreateEsClusterConfigsTest() {
        ESConfigDTO esConfigDTO = CustomDataSource.esConfigDTOFactory();
        List<ESConfigDTO> esConfigDTOS = Arrays.asList(esConfigDTO);
        List<Long> clusterConfig = esClusterConfigService.batchCreateEsClusterConfigs(esConfigDTOS, CustomDataSource.OPERATOR);
        ESConfigPO esConfigPo = esClusterConfigDAO.getByClusterIdAndTypeAndEnginAndVersion(esConfigDTO.getClusterId(),
                esConfigDTO.getTypeName(), esConfigDTO.getEnginName(), esConfigDTO.getVersionConfig());
        Assertions.assertTrue(clusterConfig.stream().anyMatch(l -> l.equals(esConfigPo.getId())));
    }

    @Test
    public void deleteEsClusterConfigTest() {
        ESConfigDTO esConfigDTO = CustomDataSource.esConfigDTOFactory();
        Long clusterId = 1234L;
        esConfigDTO.setClusterId(clusterId);
        Long id = esClusterConfigService.esClusterConfigAction(esConfigDTO, EsConfigActionEnum.ADD, CustomDataSource.OPERATOR).getData();
        Assertions.assertTrue(esClusterConfigService.deleteEsClusterConfig(id + 1, CustomDataSource.OPERATOR).failed());
        Assertions.assertTrue(esClusterConfigService.deleteEsClusterConfig(id, CustomDataSource.OPERATOR).success());
    }

    @Test
    public void setConfigValidTest() {
        ESConfigDTO esConfigDTO = CustomDataSource.esConfigDTOFactory();
        Long clusterId = 1234L;
        esConfigDTO.setClusterId(clusterId);
        Long id = esClusterConfigService.esClusterConfigAction(esConfigDTO, EsConfigActionEnum.ADD, CustomDataSource.OPERATOR).getData();
        esClusterConfigService.deleteEsClusterConfig(id, CustomDataSource.OPERATOR);
        Assertions.assertTrue(esClusterConfigService.setConfigValid(id + 1).failed());
        Assertions.assertTrue(esClusterConfigService.setConfigValid(id).success());
    }

    @Test
    public void editConfigDescTest() {
        ESConfigDTO esConfigDTO = CustomDataSource.esConfigDTOFactory();
        Assertions.assertEquals(Result.buildParamIllegal("集群配置Id为空").getMessage(),
                esClusterConfigService.editConfigDesc(esConfigDTO, CustomDataSource.OPERATOR).getMessage());
        Long clusterId = 1234L;
        esConfigDTO.setClusterId(clusterId);
        Long id = esClusterConfigService.esClusterConfigAction(esConfigDTO, EsConfigActionEnum.ADD, CustomDataSource.OPERATOR).getData();
        esConfigDTO.setId(id);
        esConfigDTO.setDesc("new desc");
        Assertions.assertTrue(esClusterConfigService.editConfigDesc(esConfigDTO, CustomDataSource.OPERATOR).success());
        Assertions.assertEquals(esConfigDTO.getDesc(), esClusterConfigDAO.getById(id).getDesc());
        esConfigDTO.setConfigData("");
        Assertions.assertEquals(Result.buildParamIllegal("不允许修改集群配置数据信息").getMessage(),
                esClusterConfigService.editConfigDesc(esConfigDTO, CustomDataSource.OPERATOR).getMessage());
        esConfigDTO.setId(id + 1);
        Assertions.assertEquals(Result.buildParamIllegal("集群配置不存在").getMessage(),
                esClusterConfigService.editConfigDesc(esConfigDTO, CustomDataSource.OPERATOR).getMessage());
    }

    @Test
    public void setOldConfigInvalidTest() {
        ESConfigDTO esConfigDTO = CustomDataSource.esConfigDTOFactory();
        ESConfig esConfig = ConvertUtil.obj2Obj(esConfigDTO, ESConfig.class);
        Assertions.assertEquals(Result.buildFail("the old config is empty").getMessage(),
                esClusterConfigService.setOldConfigInvalid(esConfig).getMessage());
        Long clusterId = 1234L;
        esConfigDTO.setClusterId(clusterId);
        esClusterConfigService.esClusterConfigAction(esConfigDTO, EsConfigActionEnum.ADD, CustomDataSource.OPERATOR).getData();
        Assertions.assertEquals(Result.buildFail("the old config is empty").getMessage(),
                esClusterConfigService.setOldConfigInvalid(esConfig).getMessage());
    }
}