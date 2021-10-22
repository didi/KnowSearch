package com.didichuxing.datachannel.arius.admin.core.service.workorder.handler.clusterReStart;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import com.google.common.collect.Multimap;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author lyn
 * @date 2021-01-21
 */
public class ClusterOpConfigRestartHandlerTest extends AriusAdminApplicationTests {

    @Test
    public void saveEsConfigToDbTest(){

        ESConfigDTO esConfigDTO01 = new ESConfigDTO();
        esConfigDTO01.setId(123L);
        esConfigDTO01.setClusterId(22L);
        esConfigDTO01.setTypeName("es.yml");
        esConfigDTO01.setEnginName("masternode");
        esConfigDTO01.setVersionConfig(1);

        ESConfigDTO esConfigDTO02 = new ESConfigDTO();
        esConfigDTO02.setId(124L);
        esConfigDTO02.setClusterId(22L);
        esConfigDTO02.setTypeName("jvm.options");
        esConfigDTO02.setEnginName("masternode");
        esConfigDTO02.setVersionConfig(1);

        ESConfigDTO esConfigDTO03 = new ESConfigDTO();
        esConfigDTO03.setId(125L);
        esConfigDTO03.setClusterId(22L);
        esConfigDTO03.setTypeName("filebeat.yml");
        esConfigDTO03.setEnginName("masternode");
        esConfigDTO03.setVersionConfig(1);

        ESConfigDTO esConfigDTO04 = new ESConfigDTO();
        esConfigDTO04.setId(126L);
        esConfigDTO04.setClusterId(22L);
        esConfigDTO04.setTypeName("filebeat.yml");
        esConfigDTO04.setEnginName("clientnode");
        esConfigDTO04.setVersionConfig(1);

        ESConfigDTO esConfigDTO05 = new ESConfigDTO();
        esConfigDTO05.setId(127L);
        esConfigDTO05.setClusterId(22L);
        esConfigDTO05.setTypeName("es.yml");
        esConfigDTO05.setEnginName("clientnode");
        esConfigDTO05.setVersionConfig(1);

        List<ESConfigDTO> newEsConfigs = Arrays.asList(esConfigDTO01,esConfigDTO02,esConfigDTO03,esConfigDTO04,esConfigDTO05);
        Multimap<String, Long> role2ConfigIdsMultiMap = ConvertUtil
                .list2MulMap(newEsConfigs, ESConfigDTO::getEnginName, ESConfigDTO::getId);

        List<Long> list = (List<Long>) role2ConfigIdsMultiMap.get("masternode");
        System.out.println(11);
    }


}