package com.didichuxing.datachannel.arius.admin.core.service.common;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.oprecord.OperateRecordDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.ModuleEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.operaterecord.OperateRecord;
import com.didichuxing.datachannel.arius.admin.core.service.common.impl.OperateRecordServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static com.didichuxing.datachannel.arius.admin.util.CustomDataSource.OperateRecordDTOFatory;

@Transactional
@Rollback
public class OperateRecordServiceTest extends AriusAdminApplicationTests {

    @Autowired
    private OperateRecordServiceImpl operateRecordService;

    @Test
    public void listTest() {
        // null查询
        Assertions.assertTrue(operateRecordService.list(null).isEmpty());

        // 插入数据
        OperateRecordDTO operateRecordDTO = OperateRecordDTOFatory();
        operateRecordService.save(operateRecordDTO);

        // 正常查询
        List<OperateRecord> list = operateRecordService.list(operateRecordDTO);
        Assertions.assertTrue(list.contains(operateRecordService.getLastRecord(operateRecordDTO.getModuleId(),
                operateRecordDTO.getOperateId(), operateRecordDTO.getBizId(), operateRecordDTO.getBeginTime())));
    }

    @Test
    public void saveModuleEnumOperationEnumObjectStringTest() {
        OperateRecordDTO operateRecordDTO = OperateRecordDTOFatory();
        Assertions.assertTrue(operateRecordService.save(ModuleEnum.valueOf(operateRecordDTO.getModuleId()),
                OperationEnum.valueOf(operateRecordDTO.getOperateId()),
                operateRecordDTO.getBizId(), operateRecordDTO.getContent(), operateRecordDTO.getOperator()).success());
    }

    @Test
    public void saveIntIntStringStringStringTest() {
        OperateRecordDTO operateRecordDTO = OperateRecordDTOFatory();
        Assertions.assertTrue(operateRecordService.save(operateRecordDTO.getModuleId(),
                operateRecordDTO.getOperateId(), operateRecordDTO.getBizId(), operateRecordDTO.getContent(), operateRecordDTO.getOperator()).success());
    }

    @Test
    public void saveOperateRecordDTOTest() {
        OperateRecordDTO operateRecordDTO = OperateRecordDTOFatory();
        Assertions.assertTrue(operateRecordService.save(operateRecordDTO).success());
    }

    @Test
    public void getLastRecordTest() {
        OperateRecordDTO operateRecordDTO = OperateRecordDTOFatory();

        // null查询
        Assertions.assertNull(operateRecordService.getLastRecord(operateRecordDTO.getModuleId(),
                operateRecordDTO.getOperateId(), operateRecordDTO.getBizId(), operateRecordDTO.getBeginTime()));

        // 插入数据
        operateRecordService.save(operateRecordDTO);

        // 正常查询 测试对应的属性相同
        Object[] expected = new Object[]{operateRecordDTO.getOperateId(),
                operateRecordDTO.getModuleId(), operateRecordDTO.getBizId(), operateRecordDTO.getContent(), operateRecordDTO.getOperator()};
        OperateRecord operateRecord = operateRecordService.getLastRecord(operateRecordDTO.getModuleId(),
                operateRecordDTO.getOperateId(), operateRecordDTO.getBizId(), operateRecordDTO.getBeginTime());
        Object[] actual = new Object[]{operateRecord.getOperateId(),
                operateRecord.getModuleId(), operateRecord.getBizId(), operateRecord.getContent(), operateRecord.getOperator()};
        Assertions.assertTrue(Arrays.equals(expected, actual));
    }
}
