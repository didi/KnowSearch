package com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.bean.po.ecm.ESMachineNormsPO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.ecm.ESMachineNormsDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Rollback
public class ESMachineNormsServiceTest extends AriusAdminApplicationTest {

    @Autowired
    private ESMachineNormsService esMachineNormsService;

    @Autowired
    private ESMachineNormsDAO machineNormsDAO;

    @Test
    public void listMachineNormsTest() {
        ESMachineNormsPO esMachineNormsPO = CustomDataSource.esMachineNormsPOFactory();
        List<ESMachineNormsPO> esMachineNormsPOs= esMachineNormsService.listMachineNorms();
        Assertions.assertTrue(esMachineNormsPOs.stream().allMatch(esMachineNormsPO1 -> !esMachineNormsPO1.getId().equals(esMachineNormsPO.getId())));
        machineNormsDAO.insert(esMachineNormsPO);
        esMachineNormsPOs= esMachineNormsService.listMachineNorms();
        Assertions.assertTrue(esMachineNormsPOs.stream().anyMatch(esMachineNormsPO1 -> esMachineNormsPO1.getId().equals(esMachineNormsPO.getId())));
    }

    @Test
    public void getByIdTest() {
        ESMachineNormsPO esMachineNormsPO = CustomDataSource.esMachineNormsPOFactory();
        machineNormsDAO.insert(esMachineNormsPO);
        Assertions.assertNull(esMachineNormsService.getById(esMachineNormsPO.getId()+1));
        Assertions.assertNotNull(esMachineNormsService.getById(esMachineNormsPO.getId()));
    }

}
