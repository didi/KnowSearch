package com.didichuxing.datachannel.arius.admin.core.service.common;

import java.util.ArrayList;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.config.AriusConfigInfoDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.config.AriusConfigStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.po.config.AriusConfigInfoPO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.config.AriusConfigInfoDAO;

import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static com.didichuxing.datachannel.arius.admin.util.CustomDataSource.ariusConfigInfoDTOFactory;

@Transactional(timeout = 1000)
@Rollback
public class AriusConfigInfoServiceTest extends AriusAdminApplicationTest {

    @Autowired
    private AriusConfigInfoService ariusConfigInfoService;

    @MockBean
    private AriusConfigInfoDAO configInfoDAO;

    @Test
    public void addConfigTest() {
        AriusConfigInfoDTO configInfoDT = ariusConfigInfoDTOFactory();
        Assertions.assertTrue(ariusConfigInfoService.addConfig(null, null).failed());
        configInfoDT.setValueName(null);
        Assertions.assertTrue(ariusConfigInfoService.addConfig(configInfoDT, CustomDataSource.OPERATOR).failed());
        configInfoDT.setValueGroup(null);
        Assertions.assertTrue(ariusConfigInfoService.addConfig(configInfoDT, CustomDataSource.OPERATOR).failed());
        configInfoDT = ariusConfigInfoDTOFactory();
        configInfoDT.setValueGroup("test");
        Mockito.when(configInfoDAO.getByGroupAndName(Mockito.eq("test"), Mockito.any())).thenReturn(new AriusConfigInfoPO());
        Assertions.assertTrue(ariusConfigInfoService.addConfig(configInfoDT, CustomDataSource.OPERATOR).failed());
        Mockito.when(configInfoDAO.getByGroupAndName(Mockito.eq("test2"), Mockito.any())).thenReturn(null);
        Mockito.when(configInfoDAO.insert(Mockito.any())).thenReturn(1);
        configInfoDT.setValueGroup("test2");
        Assertions.assertTrue(ariusConfigInfoService.addConfig(configInfoDT, CustomDataSource.OPERATOR).success());
    }

    @Test
    public void delConfigTest() {
        Mockito.when(configInfoDAO.getbyId(1)).thenReturn(null);
        Assertions.assertTrue(ariusConfigInfoService.delConfig(1, CustomDataSource.OPERATOR).failed());
        Mockito.when(configInfoDAO.getbyId(Mockito.anyInt())).thenReturn(new AriusConfigInfoPO());
        Mockito.when(configInfoDAO.updateByIdAndStatus(Mockito.anyInt(), Mockito.eq(AriusConfigStatusEnum.DELETED.getCode()))).thenReturn(1);
        Assertions.assertTrue(ariusConfigInfoService.delConfig(1, CustomDataSource.OPERATOR).success());
    }

    @Test
    public void editConfigTest() {
        AriusConfigInfoDTO configInfoDTO = ariusConfigInfoDTOFactory();
        configInfoDTO.setId(null);
        Assertions.assertTrue(ariusConfigInfoService.editConfig(configInfoDTO, CustomDataSource.OPERATOR).failed());

        configInfoDTO.setId(1);
        Mockito.when(configInfoDAO.getbyId(1)).thenReturn(null);
        Assertions.assertTrue(ariusConfigInfoService.editConfig(configInfoDTO, CustomDataSource.OPERATOR).failed());

        configInfoDTO.setId(2);
        Mockito.when(configInfoDAO.getbyId(2)).thenReturn(new AriusConfigInfoPO());
        Mockito.when(configInfoDAO.update(Mockito.any())).thenReturn(1);
        Assertions.assertTrue(ariusConfigInfoService.editConfig(configInfoDTO, CustomDataSource.OPERATOR).success());
    }

    /**
     * 状态的转换：1(正常) 2（禁用）-1（删除）
     */
    @Test
    public void switchConfigTest() {
        Mockito.when(configInfoDAO.getbyId(1)).thenReturn(null);
        Assertions.assertTrue(ariusConfigInfoService.switchConfig(1, 1, CustomDataSource.OPERATOR).failed());
        Mockito.when(configInfoDAO.getbyId(2)).thenReturn(new AriusConfigInfoPO());
        Assertions.assertTrue(ariusConfigInfoService.switchConfig(2, -10, CustomDataSource.OPERATOR).failed());
        Mockito.when(configInfoDAO.updateByIdAndStatus(2, 1)).thenReturn(1);
        Assertions.assertTrue(ariusConfigInfoService.switchConfig(2, 1, CustomDataSource.OPERATOR).success());
    }

    @Test
    public void getConfigByGroupTest() {
        Mockito.when(configInfoDAO.listByGroup("test")).thenReturn(new ArrayList<>());
        Assertions.assertTrue(ariusConfigInfoService.getConfigByGroup("test").isEmpty());
        Mockito.when(configInfoDAO.listByGroup("test2")).thenReturn(CustomDataSource.getAriusConfigInfoPOList());
        Assertions.assertFalse(ariusConfigInfoService.getConfigByGroup("test2").isEmpty());
    }

    @Test
    public void queryByCondtTest() {
        Mockito.when(configInfoDAO.listByCondition(Mockito.any())).thenReturn(CustomDataSource.getAriusConfigInfoPOList());
        Assertions.assertFalse(ariusConfigInfoService.queryByCondt(ariusConfigInfoDTOFactory()).isEmpty());
    }

    @Test
    public void getConfigByIdTest() {
        Mockito.when(configInfoDAO.getbyId(Mockito.anyInt())).thenReturn(CustomDataSource.getAriusConfigInfoPO());
        Assertions.assertNotNull(ariusConfigInfoService.getConfigById(1));
    }

    @Test
    public void updateValuebyGroupAndNameTest() {
        Assertions.assertTrue(ariusConfigInfoService.updateValueByGroupAndName(null, null, null).failed());
        Mockito.when(configInfoDAO.getByGroupAndName("test", "test")).thenReturn(null);
        Assertions.assertTrue(ariusConfigInfoService.updateValueByGroupAndName("test", "test", "test").failed());
        AriusConfigInfoPO ariusConfigInfoPO = new AriusConfigInfoPO();
        ariusConfigInfoPO.setId(1);
        Mockito.when(configInfoDAO.getByGroupAndName("test", "test")).thenReturn(ariusConfigInfoPO);
        Mockito.when(configInfoDAO.getbyId(1)).thenReturn(new AriusConfigInfoPO());
        Mockito.when(configInfoDAO.update(Mockito.any())).thenReturn(1);
        Assertions.assertTrue(ariusConfigInfoService.updateValueByGroupAndName("test", "test", "test").success());
    }

    // @Test
    public void intSettingTest() {
        Mockito.when(configInfoDAO.getByGroupAndName("test", "test")).thenReturn(null);
        Assertions.assertEquals(1, ariusConfigInfoService.intSetting("test", "test", 1));
        AriusConfigInfoPO ariusConfigInfoPO = CustomDataSource.getAriusConfigInfoPO();
        ariusConfigInfoPO.setValue("2");
        Mockito.when(configInfoDAO.getByGroupAndName("test", "test")).thenReturn(ariusConfigInfoPO);
        Assertions.assertEquals(2, ariusConfigInfoService.intSetting("test", "test", 1));
    }

    // @Test
    public void longSettingTest() {
        AriusConfigInfoPO ariusConfigInfoPO = CustomDataSource.getAriusConfigInfoPO();
        ariusConfigInfoPO.setValue("3");
        Mockito.when(configInfoDAO.getByGroupAndName("test", "test")).thenReturn(ariusConfigInfoPO);
        Assertions.assertEquals(3L, ariusConfigInfoService.longSetting("test", "test", 2L));
        Mockito.when(configInfoDAO.getByGroupAndName("test", "test")).thenReturn(null);
        long defaultValue = 1L;
        Assertions.assertEquals(defaultValue, ariusConfigInfoService.longSetting("test", "test", defaultValue));
    }

    // @Test
    public void doubleSettingTest() {
        Mockito.when(configInfoDAO.getByGroupAndName("test", "test")).thenReturn(null);
        Assertions.assertEquals(1D, ariusConfigInfoService.doubleSetting("test", "test", 1D));
        AriusConfigInfoPO ariusConfigInfoPO = CustomDataSource.getAriusConfigInfoPO();
        ariusConfigInfoPO.setValue("2");
        Mockito.when(configInfoDAO.getByGroupAndName("test", "test")).thenReturn(ariusConfigInfoPO);
        Assertions.assertEquals(2D, ariusConfigInfoService.doubleSetting("test", "test", 1D));
    }

    // @Test
    public void stringSettingTest() {
        Mockito.when(configInfoDAO.getByGroupAndName("test", "test")).thenReturn(null);
        Assertions.assertEquals("1", ariusConfigInfoService.stringSetting("test", "test", "1"));
        AriusConfigInfoPO ariusConfigInfoPO = CustomDataSource.getAriusConfigInfoPO();
        ariusConfigInfoPO.setValue("2");
        Mockito.when(configInfoDAO.getByGroupAndName("test", "test")).thenReturn(ariusConfigInfoPO);
        Assertions.assertEquals("2", ariusConfigInfoService.stringSetting("test", "test", "1"));
    }

    // @Test
    public void stringSettingSplit2SetTest() {
        Mockito.when(configInfoDAO.getByGroupAndName("test", "test")).thenReturn(null);
        Assertions.assertEquals(2, ariusConfigInfoService.stringSettingSplit2Set("test", "test", "1,2", ",").size());
        AriusConfigInfoPO ariusConfigInfoPO = CustomDataSource.getAriusConfigInfoPO();
        ariusConfigInfoPO.setValue("2");
        Mockito.when(configInfoDAO.getByGroupAndName("test", "test")).thenReturn(ariusConfigInfoPO);
        Assertions.assertEquals(2, ariusConfigInfoService.stringSettingSplit2Set("test", "test", "1", ",").size());
    }

    // @Test
    public void booleanSettingTest() {
        Mockito.when(configInfoDAO.getByGroupAndName("test", "test")).thenReturn(null);
        Assertions.assertTrue(ariusConfigInfoService.booleanSetting("test", "test", true));
        AriusConfigInfoPO ariusConfigInfoPO = CustomDataSource.getAriusConfigInfoPO();
        ariusConfigInfoPO.setValue("2");
        Mockito.when(configInfoDAO.getByGroupAndName("test", "test")).thenReturn(ariusConfigInfoPO);
        Assertions.assertTrue(ariusConfigInfoService.booleanSetting("test", "test", false));
    }

    @Test
    public void objectSettingTest() {
        Mockito.when(configInfoDAO.getByGroupAndName("test", "test")).thenReturn(null);
        AriusConfigInfoPO ariusConfigInfoPO = CustomDataSource.getAriusConfigInfoPO();
        Assertions.assertNotNull(ariusConfigInfoService.objectSetting("test", "test", ariusConfigInfoPO, AriusConfigInfoPO.class));
        ariusConfigInfoPO.setValue("2");
        Mockito.when(configInfoDAO.getByGroupAndName("test", "test")).thenReturn(ariusConfigInfoPO);
        Assertions.assertNull(ariusConfigInfoService.objectSetting("test", "test", null, AriusConfigInfoPO.class));
    }


}
