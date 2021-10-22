package com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESConfigDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESConfigListDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.esconfig.ESConfig;
import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

/**
 * @author lyn
 * @date 2021-01-06
 */
public class ESClusterConfigServiceTest extends AriusAdminApplicationTests {
    @Autowired
    private ESClusterConfigService esClusterConfigService;

    @Test
    public void test01(){

        ESConfig esClusterTemplateConfig01 = esClusterConfigService.getEsClusterTemplateConfig("elasticsearch.yml");
        ESConfig esClusterTemplateConfig02 = esClusterConfigService.getEsClusterTemplateConfig("jvm.options");
        System.out.println(esClusterTemplateConfig01.getConfigData());
        System.out.println(esClusterTemplateConfig02.getConfigData());

    }

    @Test
    public void test02(){
        ESConfig esClusterTemplateConfig = esClusterConfigService.getEsClusterConfigById(1L);
        System.out.println(esClusterTemplateConfig.getConfigData());
        Assert.assertNotNull(esClusterTemplateConfig);
    }

    @Test
    public void test03(){
        ESConfig esClusterTemplateConfig = esClusterConfigService.getByClusterIdAndTypeAndEngin(1059L,"elasticsearch.yml","masternode");
        System.out.println(esClusterTemplateConfig.getConfigData());
        Assert.assertNotNull(esClusterTemplateConfig);
    }

    @Test
    public  void  test04(){
        ESConfigListDTO esConfigListDTO = new ESConfigListDTO();
        ESConfigDTO esConfigDTO01 = new ESConfigDTO();
        esConfigDTO01.setDesc("test");
        esConfigDTO01.setConfigData("test");
        esConfigDTO01.setEnginName("masternode");
        esConfigDTO01.setTypeName("elasticsearch.yml");
        esConfigDTO01.setClusterId(33L);

        ESConfigDTO esConfigDTO02 = new ESConfigDTO();
        esConfigDTO02.setDesc("test");
        esConfigDTO02.setConfigData("test");
        esConfigDTO02.setEnginName("masternode");
        esConfigDTO02.setTypeName("jvm.options");
        esConfigDTO02.setClusterId(33L);

        ESConfigDTO esConfigDTO03 = new ESConfigDTO();
        esConfigDTO03.setDesc("test");
        esConfigDTO03.setConfigData("test");
        esConfigDTO03.setEnginName("masternode");
        esConfigDTO03.setTypeName("filebeat.yml");
        esConfigDTO03.setClusterId(33L);

        List<ESConfigDTO> esConfigDTOS = Arrays.asList(esConfigDTO01, esConfigDTO02, esConfigDTO03);

        List<Long> clusterConfig = esClusterConfigService.batchCreateEsClusterConfigs(esConfigDTOS, "lyn");
        System.out.println("11");
    }

    @Test
    public  void  test05(){
        ESConfigDTO esConfigDTO01 = new ESConfigDTO();
        esConfigDTO01.setId(620L);
        esConfigDTO01.setDesc("test");
        esConfigDTO01.setConfigData("test111");
        esConfigDTO01.setEnginName("masternode");
        esConfigDTO01.setTypeName("jvm.optionselasticsearch.yml");
        esConfigDTO01.setClusterId(1046L);
    }

    @Test
    public  void  test06(){
        Result<List<ESConfig>> listResult = esClusterConfigService.listEsClusterConfigByClusterId(11L);
        System.out.println(listResult.getData());
    }

    @Test
    public  void  test07(){
        Result listResult = esClusterConfigService.deleteEsClusterConfig(613L,"linyunan_i");
        System.out.println(listResult.getData());
    }
}