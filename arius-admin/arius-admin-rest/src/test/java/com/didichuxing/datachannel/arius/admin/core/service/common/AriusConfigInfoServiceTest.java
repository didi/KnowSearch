package com.didichuxing.datachannel.arius.admin.core.service.common;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.config.AriusConfigInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.config.AriusConfigInfoPO;
import com.didichuxing.datachannel.arius.admin.core.service.common.impl.AriusConfigInfoServiceImpl;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.config.AriusConfigInfoDAO;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Transactional
@Rollback
public class AriusConfigInfoServiceTest extends AriusAdminApplicationTests {

    private static final String operator = "wpk";

    @Autowired
    private AriusConfigInfoService ariusConfigInfoServiceImp;


    @Autowired
    private AriusConfigInfoDAO               configInfoDAO;

    private Cache<String, AriusConfigInfoPO> configCache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).maximumSize(100).build();

    @Test
    public void addConfig() {
        AriusConfigInfoDTO configInfoDT = new AriusConfigInfoDTO();
        configInfoDT.setValue("123");
        configInfoDT.setValueName("wpk");
        configInfoDT.setValueName("wpk");
        configInfoDT.setValueGroup("1");
        configInfoDT.setDimension(1);
        configInfoDT.setMemo("");
        configInfoDT.setStatus(1);
        //插入空表
        Assertions.assertTrue(ariusConfigInfoServiceImp.addConfig(null, null).failed());
        //System.out.println(ariusConfigInfoServiceImp.addConfig(configInfoDT,operator).failed());
        //插入新表
        Assertions.assertTrue(ariusConfigInfoServiceImp.addConfig(configInfoDT, operator).success());
        //System.out.println(ariusConfigInfoServiceImp.addConfig(configInfoDT,operator).success());
        //插入重复的表
        Assertions.assertTrue(ariusConfigInfoServiceImp.addConfig(configInfoDT, operator).duplicate());
        //System.out.println(ariusConfigInfoServiceImp.addConfig(configInfoDT,operator).duplicate());
    }

    //默认插入新的记录
    private AriusConfigInfoDTO addNewConfigInfo() {
        AriusConfigInfoDTO configInfoDTO = new AriusConfigInfoDTO();
        configInfoDTO.setValue("1234");
        configInfoDTO.setValueName("wp");
        configInfoDTO.setValueGroup("1");
        configInfoDTO.setDimension(1);
        configInfoDTO.setMemo("");
        configInfoDTO.setStatus(1);
        return configInfoDTO;
    }

    @Test
    public void delConfig() {
        //插入一个新记录
        AriusConfigInfoDTO configInfoDTO = addNewConfigInfo();
        Integer id = (Integer) ariusConfigInfoServiceImp.addConfig(configInfoDTO, operator).getData();
        //null删除
        Assertions.assertTrue(ariusConfigInfoServiceImp.delConfig(id + 1, operator).failed());
        //删除这个记录
        Assertions.assertTrue(ariusConfigInfoServiceImp.delConfig(id, operator).success());
    }

    @Test
    //最后一步出错
    public void editConfig() {
        //null的编辑,这里没有null的判断
        //Assertions.assertTrue(ariusConfigInfoServiceImp.editConfig(null,operator).failed());
        AriusConfigInfoDTO configInfoDTO = new AriusConfigInfoDTO();
        Assertions.assertTrue(ariusConfigInfoServiceImp.editConfig(configInfoDTO, operator).failed());
        //更新空记录
        configInfoDTO = addNewConfigInfo();
        Assertions.assertTrue(ariusConfigInfoServiceImp.editConfig(configInfoDTO, operator).failed());
        //插入记录
        Integer id = (Integer) ariusConfigInfoServiceImp.addConfig(configInfoDTO, operator).getData();
         configInfoDTO.setId(id);
        //更新记录
        configInfoDTO.setValue("1234");
        Assertions.assertTrue(ariusConfigInfoServiceImp.editConfig(configInfoDTO, operator).success());
        AriusConfigInfoPO ariusConfigInfoPO = configInfoDAO.getbyId(id);
        Assertions.assertEquals("1234", ariusConfigInfoPO.getValue());

    }

    @Test
    //状态的转换：1(正常) 2（禁用）-1（删除）
    public void switchConfig() {
        //插入一个新记录
        AriusConfigInfoDTO configInfoDTO = addNewConfigInfo();
        Integer id = (Integer) ariusConfigInfoServiceImp.addConfig(configInfoDTO, operator).getData();
        configInfoDTO.setId(id);
        //覆盖配置不存在
        Assertions.assertTrue(ariusConfigInfoServiceImp.switchConfig(id + 1, 1, operator).failed());
        //覆盖状态非法
        Assertions.assertTrue(ariusConfigInfoServiceImp.switchConfig(id, 1000, operator).failed());
        Assertions.assertNotEquals(1000,configInfoDAO.getbyId(id).getStatus());
        //合法的修改
        Assertions.assertTrue(ariusConfigInfoServiceImp.switchConfig(id,2,operator).success());
        AriusConfigInfoPO ariusConfigInfoPO = configInfoDAO.getbyId(id);
        Assertions.assertEquals(2,ariusConfigInfoPO.getStatus());
    }

    @Test
    public void getConfigByGroup() {

    }

    @Test
    public void queryByCondt() {

    }

    @Test
    public void getConfigById() {

    }

    @Test
    public void updateValuebyGroupAndName() {
        //插入一个新记录
        AriusConfigInfoDTO configInfoDTO = addNewConfigInfo();
        Integer id = (Integer) ariusConfigInfoServiceImp.addConfig(configInfoDTO, operator).getData();
        //值为空
        Assertions.assertTrue(ariusConfigInfoServiceImp.updateValueByGroupAndName("2", "wp", null).failed());
        //覆盖配置不存在
        Assertions.assertTrue(ariusConfigInfoServiceImp.updateValueByGroupAndName("2", "wp", "12345").failed());
        //合法的修改
        Assertions.assertTrue(ariusConfigInfoServiceImp.updateValueByGroupAndName("1", "wp", "12").success());
        AriusConfigInfoPO ariusConfigInfoPO = configInfoDAO.getbyId(id);
        Assertions.assertEquals("12",ariusConfigInfoPO.getValue());
    }

    @Test
    public void upsertValueByGroupAndName() {

    }

    @Test
    public void intSetting() {
        //插入一个新记录
        AriusConfigInfoDTO configInfoDTO = addNewConfigInfo();
        ariusConfigInfoServiceImp.addConfig(configInfoDTO, operator);
        //原记录不存在返回默认值
        Assertions.assertEquals(1, ariusConfigInfoServiceImp.intSetting("2", "wp", 1));
        //记录存在返回值
        Assertions.assertEquals(Integer.valueOf(configInfoDTO.getValue()), ariusConfigInfoServiceImp.intSetting(configInfoDTO.getValueGroup(), configInfoDTO.getValueName(), 1));
    }

    @Test
    public void longSetting() {
        //插入一个新记录
        AriusConfigInfoDTO configInfoDTO = addNewConfigInfo();
        ariusConfigInfoServiceImp.addConfig(configInfoDTO, operator);
        Long l = Long.valueOf(1);
        //原记录不存在返回默认值
        Assertions.assertEquals(l, ariusConfigInfoServiceImp.longSetting("2", "wp", l));
        //记录存在返回值
        Assertions.assertEquals(Long.valueOf(configInfoDTO.getValue()), ariusConfigInfoServiceImp.longSetting(configInfoDTO.getValueGroup(), configInfoDTO.getValueName(), l));
    }

    @Test
    public void doubleSetting() {
        //插入一个新记录
        AriusConfigInfoDTO configInfoDTO = addNewConfigInfo();
        ariusConfigInfoServiceImp.addConfig(configInfoDTO, operator);
        //原记录不存在返回默认值
        Assertions.assertEquals(1.0, ariusConfigInfoServiceImp.doubleSetting("2", "wp", 1.0));
        //记录存在返回值
        Assertions.assertEquals(Double.valueOf(configInfoDTO.getValue()), ariusConfigInfoServiceImp.doubleSetting(configInfoDTO.getValueGroup(), configInfoDTO.getValueName(), 1.0));
    }

    @Test
    public void stringSetting() {
        //插入一个新记录
        AriusConfigInfoDTO configInfoDTO = addNewConfigInfo();
        ariusConfigInfoServiceImp.addConfig(configInfoDTO, operator);
        //原记录不存在返回默认值
        Assertions.assertEquals("1", ariusConfigInfoServiceImp.stringSetting("2", "wp", "1"));
        //记录存在返回值
        Assertions.assertEquals(configInfoDTO.getValue(), ariusConfigInfoServiceImp.stringSetting(configInfoDTO.getValueGroup(), configInfoDTO.getValueName(), "1"));
    }

    @Test
    public void stringSettingSplit2Se() {

    }

    @Test
    public void booleanSetting() {
        //插入一个新记录
        AriusConfigInfoDTO configInfoDTO = addNewConfigInfo();
        ariusConfigInfoServiceImp.addConfig(configInfoDTO, operator);
        //原记录不存在返回默认值
        Assertions.assertTrue(ariusConfigInfoServiceImp.booleanSetting("2", "wp", true));
        //记录存在返回值
        Assertions.assertFalse(ariusConfigInfoServiceImp.booleanSetting(configInfoDTO.getValueGroup(), configInfoDTO.getValueName(), true));
    }

    @Test
    public void objectSetting() {
        //插入一个新记录
        AriusConfigInfoDTO configInfoDTO = addNewConfigInfo();
        ariusConfigInfoServiceImp.addConfig(configInfoDTO, operator);
        //查询结果无法进行类的转化
        Assertions.assertEquals(1,ariusConfigInfoServiceImp.objectSetting("2", "wp", 1,Integer.class));
        //查询结果可以进行类的转化
        Assertions.assertEquals("1234",ariusConfigInfoServiceImp.objectSetting(configInfoDTO.getValueGroup(), configInfoDTO.getValueName(), "1",String.class));
    }

    //后面还有一些私有方法，就不再通过反射的方式去测试了,多此一举

}
