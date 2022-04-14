package com.didichuxing.datachannel.arius.admin.core.service.common;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.oprecord.OperateRecordDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.ModuleEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.operaterecord.OperateRecord;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.optrecord.OperateRecordDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static com.didichuxing.datachannel.arius.admin.util.CustomDataSource.OperateRecordDTOFatory;

@Transactional
@Rollback
public class OperateRecordServiceTest extends AriusAdminApplicationTest {

    @Autowired
    private OperateRecordService operateRecordService;

    @MockBean
    private OperateRecordDAO operateRecordDAO;

    @Test
    public void listTest() {
        Assertions.assertTrue(operateRecordService.list(null).getData().isEmpty());
        Mockito.when(operateRecordDAO.listByCondition(Mockito.any())).thenReturn(CustomDataSource.getOperateRecordPOList());
        OperateRecordDTO operateRecordDTO = OperateRecordDTOFatory();
        Assertions.assertEquals(CustomDataSource.SIZE, operateRecordService.list(operateRecordDTO).getData().size());
    }

    @Test
    public void saveModuleEnumOperationEnumObjectStringTest() {
        Mockito.when(operateRecordDAO.insert(Mockito.any())).thenReturn(1);
        OperateRecordDTO operateRecordDTO = OperateRecordDTOFatory();
        Assertions.assertTrue(operateRecordService.save(ModuleEnum.valueOf(operateRecordDTO.getModuleId()),
                OperationEnum.valueOf(operateRecordDTO.getOperateId()),
                operateRecordDTO.getBizId(), operateRecordDTO.getContent(), operateRecordDTO.getOperator()).success());
    }

    @Test
    public void saveIntIntStringStringStringTest() {
        Mockito.when(operateRecordDAO.insert(Mockito.any())).thenReturn(1);
        OperateRecordDTO operateRecordDTO = OperateRecordDTOFatory();
        Assertions.assertTrue(operateRecordService.save(operateRecordDTO.getModuleId(),
                operateRecordDTO.getOperateId(), operateRecordDTO.getBizId(), operateRecordDTO.getContent(), operateRecordDTO.getOperator()).success());
    }

    @Test
    public void saveOperateRecordDTOTest() {
        OperateRecordDTO operateRecordDTO = OperateRecordDTOFatory();
        operateRecordDTO.setOperateId(null);
        Assertions.assertTrue(operateRecordService.save(operateRecordDTO).failed());
        operateRecordDTO.setModuleId(null);
        Assertions.assertTrue(operateRecordService.save(operateRecordDTO).failed());
        operateRecordDTO.setOperator(null);
        Assertions.assertTrue(operateRecordService.save(operateRecordDTO).failed());
        operateRecordDTO.setContent(null);
        Assertions.assertTrue(operateRecordService.save(operateRecordDTO).failed());
        operateRecordDTO.setBizId(null);
        Assertions.assertTrue(operateRecordService.save(operateRecordDTO).failed());
        operateRecordDTO.setOperateId(null);
        Assertions.assertTrue(operateRecordService.save(operateRecordDTO).failed());
        operateRecordDTO.setBizId(null);
        Assertions.assertTrue(operateRecordService.save(operateRecordDTO).failed());
        operateRecordDTO.setModuleId(null);
        Assertions.assertTrue(operateRecordService.save(operateRecordDTO).failed());
        Assertions.assertTrue(operateRecordService.save(null).failed());
        operateRecordDTO = OperateRecordDTOFatory();
        Mockito.when(operateRecordDAO.insert(Mockito.any())).thenReturn(1);
        Assertions.assertTrue(operateRecordService.save(operateRecordDTO).success());
    }

    @Test
    public void getLastRecordTest() {
        OperateRecordDTO operateRecordDTO = OperateRecordDTOFatory();
        Assertions.assertNull(operateRecordService.getLastRecord(operateRecordDTO.getModuleId(),
                operateRecordDTO.getOperateId(), operateRecordDTO.getBizId(), operateRecordDTO.getBeginTime()));
        Mockito.when(operateRecordDAO.listByCondition(Mockito.any())).thenReturn(CustomDataSource.getOperateRecordPOList());
        OperateRecord operateRecord = operateRecordService.getLastRecord(operateRecordDTO.getModuleId(),
                operateRecordDTO.getOperateId(), operateRecordDTO.getBizId(), operateRecordDTO.getBeginTime());
        Assertions.assertNotNull(operateRecord);
    }
}
