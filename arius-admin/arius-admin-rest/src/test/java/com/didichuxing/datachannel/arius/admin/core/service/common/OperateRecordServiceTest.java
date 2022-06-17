package com.didichuxing.datachannel.arius.admin.core.service.common;

import static com.didichuxing.datachannel.arius.admin.util.CustomDataSource.OperateRecordDTOFatory;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.oprecord.OperateRecordDTO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.optrecord.OperateRecordDAO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Rollback
public class OperateRecordServiceTest extends AriusAdminApplicationTest {

    @Autowired
    private OperateRecordService operateRecordService;

    @MockBean
    private OperateRecordDAO operateRecordDAO;

    

    @Test
    public void saveModuleEnumOperationEnumObjectStringTest() {
        Mockito.when(operateRecordDAO.insert(Mockito.any())).thenReturn(1);
        OperateRecordDTO operateRecordDTO = OperateRecordDTOFatory();
    }

    @Test
    public void saveIntIntStringStringStringTest() {
        Mockito.when(operateRecordDAO.insert(Mockito.any())).thenReturn(1);
        OperateRecordDTO operateRecordDTO = OperateRecordDTOFatory();
    }

    @Test
    public void saveOperateRecordDTOTest() {
        OperateRecordDTO operateRecordDTO = OperateRecordDTOFatory();
        operateRecordDTO.setOperateId(null);
        Assertions.assertTrue(operateRecordService.save(operateRecordDTO).failed());
        operateRecordDTO.setModuleId(null);
        Assertions.assertTrue(operateRecordService.save(operateRecordDTO).failed());
        Assertions.assertTrue(operateRecordService.save(operateRecordDTO).failed());
        operateRecordDTO.setContent(null);
        Assertions.assertTrue(operateRecordService.save(operateRecordDTO).failed());
        Assertions.assertTrue(operateRecordService.save(operateRecordDTO).failed());
        operateRecordDTO.setOperateId(null);
        Assertions.assertTrue(operateRecordService.save(operateRecordDTO).failed());
        Assertions.assertTrue(operateRecordService.save(operateRecordDTO).failed());
        operateRecordDTO.setModuleId(null);
        Assertions.assertTrue(operateRecordService.save(operateRecordDTO).failed());
        Assertions.assertTrue(operateRecordService.save(null).failed());
        operateRecordDTO = OperateRecordDTOFatory();
        Mockito.when(operateRecordDAO.insert(Mockito.any())).thenReturn(1);
        Assertions.assertTrue(operateRecordService.save(operateRecordDTO).success());
    }

    
}