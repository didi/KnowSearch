package com.didichuxing.datachannel.arius.admin.core.service.common;

import java.util.List;
import java.util.Set;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.config.AriusConfigInfoDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.config.AriusConfigStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.config.AriusConfigInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.po.config.AriusConfigInfoPO;
import com.didichuxing.datachannel.arius.admin.core.service.common.impl.AriusConfigInfoServiceImpl;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.config.AriusConfigInfoDAO;

import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static com.didichuxing.datachannel.arius.admin.util.CustomDataSource.ariusConfigInfoDTOFactory;


@Transactional(timeout = 1000)
@Rollback
public class AriusConfigInfoServiceTest extends AriusAdminApplicationTests {

    private static final String OPERATOR = "wpk";
    private static final String GROUP = "2";
    private static final String VALUE = "12345";
    private static final String STRING_VALUE = "wpktest";

    @Autowired
    private AriusConfigInfoService ariusConfigInfoServiceImp = new AriusConfigInfoServiceImpl();


    @Autowired
    private AriusConfigInfoDAO configInfoDAO;


    @Test
    public void addConfigTest() {
        AriusConfigInfoDTO configInfoDT = ariusConfigInfoDTOFactory();

        // 插入空记录
        Assertions.assertTrue(ariusConfigInfoServiceImp.addConfig(null, null).failed());

        // 插入新记录
        Assertions.assertTrue(ariusConfigInfoServiceImp.addConfig(configInfoDT, OPERATOR).success());

        // 插入重复的记录
        Assertions.assertTrue(ariusConfigInfoServiceImp.addConfig(configInfoDT, OPERATOR).duplicate());
    }


    @Test
    public void delConfigTest() {
        // 插入一个新记录
        AriusConfigInfoDTO configInfoDTO = ariusConfigInfoDTOFactory();
        Integer id = (Integer) ariusConfigInfoServiceImp.addConfig(configInfoDTO, OPERATOR).getData();

        // null删除
        Assertions.assertTrue(ariusConfigInfoServiceImp.delConfig(id + 1, OPERATOR).failed());

        // 删除这个记录
        Assertions.assertTrue(ariusConfigInfoServiceImp.delConfig(id, OPERATOR).success());

        // 删除之后查询失败
        Assertions.assertNull(configInfoDAO.getbyId(id));
    }

    @Test
    public void editConfigTest() {
        // 更新空记录
        AriusConfigInfoDTO configInfoDTO = ariusConfigInfoDTOFactory();
        Assertions.assertTrue(ariusConfigInfoServiceImp.editConfig(configInfoDTO, OPERATOR).failed());

        // 插入记录
        Integer id = (Integer) ariusConfigInfoServiceImp.addConfig(configInfoDTO, OPERATOR).getData();
        configInfoDTO.setId(id);

        // 更新记录
        configInfoDTO.setValue(VALUE);
        Assertions.assertTrue(ariusConfigInfoServiceImp.editConfig(configInfoDTO, OPERATOR).success());
        AriusConfigInfoPO ariusConfigInfoPO = configInfoDAO.getbyId(id);
        Assertions.assertEquals(configInfoDTO.getValue(), ariusConfigInfoPO.getValue());

        // 异常情况处理
        // 配置ID为空
        configInfoDTO = new AriusConfigInfoDTO();
        Assertions.assertTrue(ariusConfigInfoServiceImp.editConfig(configInfoDTO, OPERATOR).failed());

        // 配置值为空
        configInfoDTO.setId(id + 1);
        Assertions.assertTrue(ariusConfigInfoServiceImp.editConfig(configInfoDTO, OPERATOR).failed());

        // 配置不存在
        configInfoDTO.setValue(OPERATOR);
        Assertions.assertTrue(ariusConfigInfoServiceImp.editConfig(configInfoDTO, OPERATOR).failed());
    }

    /**
     * 状态的转换：1(正常) 2（禁用）-1（删除）
     */
    @Test
    public void switchConfigTest() {
        // 插入一个新记录
        AriusConfigInfoDTO configInfoDTO = ariusConfigInfoDTOFactory();
        Integer id = (Integer) ariusConfigInfoServiceImp.addConfig(configInfoDTO, OPERATOR).getData();
        configInfoDTO.setId(id);

        // 覆盖配置不存在
        Assertions.assertTrue(ariusConfigInfoServiceImp.switchConfig(id + 1,
                AriusConfigStatusEnum.NORMAL.getCode(), OPERATOR).failed());

        // 覆盖状态非法
        int illegalAriusConfigStatus = 1000;
        Assertions.assertTrue(ariusConfigInfoServiceImp.switchConfig(id, illegalAriusConfigStatus, OPERATOR).failed());
        Assertions.assertNotEquals(illegalAriusConfigStatus, configInfoDAO.getbyId(id).getStatus());

        // 合法的修改
        Assertions.assertTrue(ariusConfigInfoServiceImp.switchConfig(id,
                AriusConfigStatusEnum.DISABLE.getCode(), OPERATOR).success());
        AriusConfigInfoPO ariusConfigInfoPO = configInfoDAO.getbyId(id);
        Assertions.assertEquals(AriusConfigStatusEnum.DISABLE.getCode(), ariusConfigInfoPO.getStatus());
    }

    @Test
    public void getConfigByGroupTest() {
        AriusConfigInfoDTO configInfoDTO = ariusConfigInfoDTOFactory();

        // 插入一个新记录
        Integer id = (Integer) ariusConfigInfoServiceImp.addConfig(configInfoDTO, OPERATOR).getData();
        List<AriusConfigInfo> configByGroup = ariusConfigInfoServiceImp.getConfigByGroup(configInfoDTO.getValueGroup());
        Assertions.assertTrue(configByGroup.stream().anyMatch(a -> a.getId().equals(id)));

        // 如果配置组不存在 返回空列表
        Assertions.assertTrue(ariusConfigInfoServiceImp.getConfigByGroup("1234").isEmpty());
    }

    @Test
    public void queryByCondtTest() {
        AriusConfigInfoDTO configInfoDTO1 = ariusConfigInfoDTOFactory();

        Integer id = (Integer) ariusConfigInfoServiceImp.addConfig(configInfoDTO1, OPERATOR).getData();
        Assertions.assertNotNull(id);
        AriusConfigInfoDTO configInfoDTO = new AriusConfigInfoDTO();
        configInfoDTO.setValueGroup(configInfoDTO1.getValueGroup());
        List<AriusConfigInfo> ariusConfigInfos = ariusConfigInfoServiceImp.queryByCondt(configInfoDTO);
        Assertions.assertTrue(ariusConfigInfos.stream().anyMatch(a -> a.getId().equals(id)));
    }

    @Test
    public void getConfigByIdTest() {
        AriusConfigInfoDTO configInfoDTO = ariusConfigInfoDTOFactory();

        Integer id = (Integer) ariusConfigInfoServiceImp.addConfig(configInfoDTO, OPERATOR).getData();
        Assertions.assertEquals(id, ariusConfigInfoServiceImp.getConfigById(id).getId());
    }

    @Test
    public void updateValuebyGroupAndNameTest() {
        // 插入一个新记录
        AriusConfigInfoDTO configInfoDTO = ariusConfigInfoDTOFactory();
        Integer id = (Integer) ariusConfigInfoServiceImp.addConfig(configInfoDTO, OPERATOR).getData();

        // 值为空
        Assertions.assertTrue(ariusConfigInfoServiceImp.updateValueByGroupAndName(GROUP,
                configInfoDTO.getValueName(), null).failed());

        // 覆盖配置不存在
        Assertions.assertTrue(ariusConfigInfoServiceImp.updateValueByGroupAndName(GROUP,
                configInfoDTO.getValueName(), VALUE).failed());

        // 合法的修改
        Assertions.assertTrue(ariusConfigInfoServiceImp.updateValueByGroupAndName(configInfoDTO.getValueGroup(),
                configInfoDTO.getValueName(), VALUE).success());
        AriusConfigInfoPO ariusConfigInfoPO = configInfoDAO.getbyId(id);
        Assertions.assertEquals(VALUE, ariusConfigInfoPO.getValue());
    }

    @Test
    public void upsertValueByGroupAndNameTest() {
        AriusConfigInfoDTO configInfoDTO = ariusConfigInfoDTOFactory();

        // 获取不存在就新增
        Integer id = (Integer) ariusConfigInfoServiceImp.upsertValueByGroupAndName(configInfoDTO.getValueGroup(),
                configInfoDTO.getValueName(), configInfoDTO.getValue()).getData();
        Assertions.assertEquals(id,
                configInfoDAO.getByGroupAndName(configInfoDTO.getValueGroup(), configInfoDTO.getValueName()).getId());

        // 修改配置的值
        ariusConfigInfoServiceImp.upsertValueByGroupAndName(configInfoDTO.getValueGroup(), configInfoDTO.getValueName(), VALUE);
        Assertions.assertEquals(VALUE, configInfoDAO.getbyId(id).getValue());
    }

    @Test
    public void intSettingTest() {
        // 插入一个新记录
        AriusConfigInfoDTO configInfoDTO = ariusConfigInfoDTOFactory();
        ariusConfigInfoServiceImp.addConfig(configInfoDTO, OPERATOR);

        // 原记录不存在返回默认值
        Assertions.assertEquals(Integer.valueOf(VALUE), ariusConfigInfoServiceImp.intSetting(GROUP,
                configInfoDTO.getValueName(), Integer.valueOf(VALUE)));

        // 记录存在返回值
        Assertions.assertEquals(Integer.valueOf(configInfoDTO.getValue()),
                ariusConfigInfoServiceImp.intSetting(configInfoDTO.getValueGroup(), configInfoDTO.getValueName(), Integer.valueOf(VALUE)));

        // 进入异常分支
        configInfoDTO = ariusConfigInfoDTOFactory();
        configInfoDTO.setValueGroup(GROUP);
        configInfoDTO.setValue(STRING_VALUE);
        ariusConfigInfoServiceImp.addConfig(configInfoDTO, OPERATOR);
        Assertions.assertEquals(Integer.valueOf(VALUE),
                ariusConfigInfoServiceImp.intSetting(configInfoDTO.getValueGroup(), configInfoDTO.getValueName(), Integer.valueOf(VALUE)));

    }

    @Test
    public void longSettingTest() {
        // 插入一个新记录
        AriusConfigInfoDTO configInfoDTO = ariusConfigInfoDTOFactory();
        ariusConfigInfoServiceImp.addConfig(configInfoDTO, OPERATOR);
        Long longValue = 1L;

        // 原记录不存在返回默认值
        Assertions.assertEquals(longValue, ariusConfigInfoServiceImp.longSetting(GROUP,
                configInfoDTO.getValueName(), longValue));

        // 记录存在返回值
        Assertions.assertEquals(Long.valueOf(configInfoDTO.getValue()),
                ariusConfigInfoServiceImp.longSetting(configInfoDTO.getValueGroup(), configInfoDTO.getValueName(), longValue));

        // 进入异常分支
        configInfoDTO = ariusConfigInfoDTOFactory();
        configInfoDTO.setValueGroup(GROUP);
        configInfoDTO.setValue(STRING_VALUE);
        ariusConfigInfoServiceImp.addConfig(configInfoDTO, OPERATOR);
        Assertions.assertEquals(longValue,
                ariusConfigInfoServiceImp.longSetting(configInfoDTO.getValueGroup(), configInfoDTO.getValueName(), longValue));
    }

    @Test
    public void doubleSettingTest() {
        // 插入一个新记录
        AriusConfigInfoDTO configInfoDTO = ariusConfigInfoDTOFactory();
        ariusConfigInfoServiceImp.addConfig(configInfoDTO, OPERATOR);
        Double doubleValue = 1.0;

        // 原记录不存在返回默认值
        Assertions.assertEquals(doubleValue, ariusConfigInfoServiceImp.doubleSetting(GROUP,
                configInfoDTO.getValueName(), doubleValue));

        // 记录存在返回值
        Assertions.assertEquals(Double.valueOf(configInfoDTO.getValue()),
                ariusConfigInfoServiceImp.doubleSetting(configInfoDTO.getValueGroup(), configInfoDTO.getValueName(), doubleValue));

        // 进入异常分支
        configInfoDTO = ariusConfigInfoDTOFactory();
        configInfoDTO.setValueGroup(GROUP);
        configInfoDTO.setValue(STRING_VALUE);
        ariusConfigInfoServiceImp.addConfig(configInfoDTO, OPERATOR);
        Assertions.assertEquals(doubleValue,
                ariusConfigInfoServiceImp.doubleSetting(configInfoDTO.getValueGroup(), configInfoDTO.getValueName(), doubleValue));
    }

    @Test
    public void stringSettingTest() {
        // 插入一个新记录
        AriusConfigInfoDTO configInfoDTO = ariusConfigInfoDTOFactory();
        ariusConfigInfoServiceImp.addConfig(configInfoDTO, OPERATOR);

        // 原记录不存在返回默认值
        Assertions.assertEquals(STRING_VALUE,
                ariusConfigInfoServiceImp.stringSetting(GROUP, configInfoDTO.getValueName(), STRING_VALUE));

        // 记录存在返回值
        Assertions.assertEquals(configInfoDTO.getValue(),
                ariusConfigInfoServiceImp.stringSetting(configInfoDTO.getValueGroup(), configInfoDTO.getValueName(), STRING_VALUE));
    }

    @Test
    public void stringSettingSplit2SetTest() {
        // 插入一个新记录
        AriusConfigInfoDTO configInfoDTO = ariusConfigInfoDTOFactory();
        String str = "1,2,3,4,5";
        configInfoDTO.setValueGroup("12345");
        configInfoDTO.setValue(str);
        ariusConfigInfoServiceImp.addConfig(configInfoDTO, OPERATOR);

        //获取String类型配置 用字符分割
        Set<String> strings = ariusConfigInfoServiceImp.stringSettingSplit2Set(configInfoDTO.getValueGroup(), configInfoDTO.getValueName(), STRING_VALUE, ",");
        Assertions.assertEquals(Sets.newHashSet(str.split(",")),strings);
    }

    @Test
    public void booleanSettingTest() {
        // 插入一个新记录
        AriusConfigInfoDTO configInfoDTO = ariusConfigInfoDTOFactory();
        configInfoDTO.setValueGroup("123456");
        ariusConfigInfoServiceImp.addConfig(configInfoDTO, OPERATOR);

        // 原记录不存在返回默认值
        Assertions.assertTrue(ariusConfigInfoServiceImp.booleanSetting(GROUP,
                configInfoDTO.getValueName().concat(configInfoDTO.getValueName()), true));

        // 记录存在返回值
        Assertions.assertFalse(ariusConfigInfoServiceImp.booleanSetting(configInfoDTO.getValueGroup(),
                configInfoDTO.getValueName(), true));
    }

    @Test
    public void objectSettingTest() {
        // 插入一个新记录
        AriusConfigInfoDTO configInfoDTO = ariusConfigInfoDTOFactory();
        ariusConfigInfoServiceImp.addConfig(configInfoDTO, OPERATOR);
        int intValue = 1;

        // 原纪录不存在，返回默认值
        Assertions.assertEquals(intValue, ariusConfigInfoServiceImp.objectSetting(GROUP,
                configInfoDTO.getValueName(), intValue, Integer.class));

        // 查询结果可以进行类的转化
        Assertions.assertEquals(configInfoDTO.getValue(),
                ariusConfigInfoServiceImp.objectSetting(configInfoDTO.getValueGroup(),
                        configInfoDTO.getValueName(), STRING_VALUE, String.class));

        // 进入异常分支
        configInfoDTO = ariusConfigInfoDTOFactory();
        configInfoDTO.setValueGroup(GROUP);
        configInfoDTO.setValue(STRING_VALUE);
        ariusConfigInfoServiceImp.addConfig(configInfoDTO, OPERATOR);
        Assertions.assertEquals(intValue,
                ariusConfigInfoServiceImp.objectSetting(configInfoDTO.getValueGroup(), configInfoDTO.getValueName(), intValue,Integer.class));
    }


}
