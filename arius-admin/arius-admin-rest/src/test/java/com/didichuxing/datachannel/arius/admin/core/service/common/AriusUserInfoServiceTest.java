package com.didichuxing.datachannel.arius.admin.core.service.common;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.user.AriusUserInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.arius.AriusUserInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.po.arius.AriusUserInfoPO;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUserRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUserStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.arius.AriusUserInfoDAO;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Assertions;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.didichuxing.datachannel.arius.admin.util.CustomDataSource.ariusUserInfoDTOFactory;

@Transactional(timeout = 1000)
@Rollback
public class AriusUserInfoServiceTest extends AriusAdminApplicationTests {

    @Autowired
    private AriusUserInfoDAO ariusUserInfoDAO;

    @Autowired
    private AriusUserInfoService ariusUserInfoService;

    private static final String defaultName = "wpk";

    @Test
    public void getByDomainAccountTest() {
        // 插入新的记录
        AriusUserInfoDTO ariusUserInfoDTO = ariusUserInfoDTOFactory();
        Long id = ariusUserInfoService.save(ariusUserInfoDTO).getData();
        // 通过域账号查询
        Assertions.assertEquals(id, ariusUserInfoService.getByDomainAccount(ariusUserInfoDTO.getDomainAccount()).getId());
    }

    @Test
    public void saveTest() {
        // 插入新的记录
        AriusUserInfoDTO ariusUserInfoDTO = ariusUserInfoDTOFactory();
        Long id = ariusUserInfoService.save(ariusUserInfoDTO).getData();
        AriusUserInfoPO ariusUserInfoPO = ariusUserInfoDAO.getById(id);
        Assertions.assertEquals(id,ariusUserInfoPO.getId());

        // 插入旧的记录
        Assertions.assertEquals(id,ariusUserInfoService.save(ariusUserInfoDTO).getData());
    }

    @Test
    public void saveByUsersTest() {
        // 插入空记录
        String userNames = "";
        Assertions.assertTrue(ariusUserInfoService.saveByUsers(userNames).isEmpty());

        // 插入连串的用户名
        userNames = "wpk,w,p,k";
        List<Long> ids = ariusUserInfoService.saveByUsers(userNames);
        List<Long> checkIds = new ArrayList<>();
        for (String user : userNames.split(",")) {
            checkIds.add(ariusUserInfoDAO.getByName(user).getId());
        }
        Assertions.assertEquals(ids, checkIds);

    }

    @Test
    public void getUserByIdsTest() {
        // ""传入
        Assertions.assertEquals("", ariusUserInfoService.getUserByIds(""));

        // 传入新的记录
        AriusUserInfoDTO ariusUserInfoDTO = ariusUserInfoDTOFactory();
        Long id = ariusUserInfoService.save(ariusUserInfoDTO).getData();
        String ids = String.valueOf(id) + "," + String.valueOf(id + 1);
        Assertions.assertEquals(ariusUserInfoDTO.getName(), ariusUserInfoService.getUserByIds(ids));
    }

    @Test
    public void listAllEnableTest() {
        AriusUserInfoDTO ariusUserInfoDTO = ariusUserInfoDTOFactory();
        Long id = ariusUserInfoService.save(ariusUserInfoDTO).getData();
        List<AriusUserInfo> ariusUserInfos = ariusUserInfoService.listAllEnable();
        Assertions.assertTrue(CollectionUtils.isNotEmpty(ariusUserInfos));
        Assertions.assertTrue(ariusUserInfos.stream().anyMatch(a -> a.getId().equals(id)));
    }

    @Test
    public void deleteTest() {
        AriusUserInfoDTO ariusUserInfoDTO = ariusUserInfoDTOFactory();
        Long id = ariusUserInfoService.save(ariusUserInfoDTO).getData();
        if (ariusUserInfoService.delete(id)) {
            AriusUserInfoPO param = ariusUserInfoDAO.getById(id);
            Assertions.assertEquals(AriusUserStatusEnum.DISABLE.getCode(), param.getStatus());
        }
    }

    @Test
    public void processUserDuplicateTest() {
        AriusUserInfoDTO ariusUserInfoDTO = ariusUserInfoDTOFactory();
        Long id1 = ariusUserInfoService.save(ariusUserInfoDTO).getData();
        ariusUserInfoDTO = ariusUserInfoDTOFactory();
        ariusUserInfoDTO.setDomainAccount(defaultName+"new");
        Long id2 = ariusUserInfoService.save(ariusUserInfoDTO).getData();

        // 去重
        Assertions.assertTrue(ariusUserInfoService.processUserDuplicate());

        // 检查
        Assertions.assertTrue(ariusUserInfoDAO.getById(id1).getStatus().equals(AriusUserStatusEnum.DISABLE.getCode())
                ||ariusUserInfoDAO.getById(id2).getStatus().equals(AriusUserStatusEnum.DISABLE.getCode()));
    }

    @Test
    public void isOpByUserNameTest() {
        AriusUserInfoDTO ariusUserInfoDTO = ariusUserInfoDTOFactory();

        // 未找到返回false
        Assertions.assertFalse(ariusUserInfoService.isOPByDomainAccount(ariusUserInfoDTO.getName()));
        Long id = ariusUserInfoService.save(ariusUserInfoDTO).getData();

        // 找到且角色匹配
        Assertions.assertTrue(ariusUserInfoService.isOPByDomainAccount(ariusUserInfoDTO.getName()));

        // 找到但是角色不匹配
        ariusUserInfoDTO.setRole(AriusUserRoleEnum.RD.getRole());
        ariusUserInfoDTO.setId(id);
        ariusUserInfoDAO.update(ConvertUtil.obj2Obj(ariusUserInfoDTO,AriusUserInfoPO.class));
        Assertions.assertFalse(ariusUserInfoService.isOPByDomainAccount(ariusUserInfoDTO.getName()));
    }

    @Test
    public void isRDByUserNameTest() {
        AriusUserInfoDTO ariusUserInfoDTO = ariusUserInfoDTOFactory();

        // 未找到返回false
        Assertions.assertFalse(ariusUserInfoService.isOPByDomainAccount(ariusUserInfoDTO.getName()));
        Long id = ariusUserInfoService.save(ariusUserInfoDTO).getData();

        // 找到但是角色不匹配
        Assertions.assertFalse(ariusUserInfoService.isRDByDomainAccount(ariusUserInfoDTO.getName()));

        // 找到且角色匹配
        ariusUserInfoDTO.setRole(AriusUserRoleEnum.RD.getRole());
        ariusUserInfoDTO.setId(id);
        ariusUserInfoDAO.update(ConvertUtil.obj2Obj(ariusUserInfoDTO,AriusUserInfoPO.class));
        Assertions.assertTrue(ariusUserInfoService.isRDByDomainAccount(ariusUserInfoDTO.getName()));
    }

    @Test
    public void getByNameTest() {
        AriusUserInfoDTO ariusUserInfoDTO = ariusUserInfoDTOFactory();
        Assertions.assertNull(ariusUserInfoService.getByName(ariusUserInfoDTO.getName()));
        Long id = ariusUserInfoService.save(ariusUserInfoDTO).getData();
        Assertions.assertEquals(id, ariusUserInfoService.getByName(ariusUserInfoDTO.getName()).getId());
    }

    @Test
    public void getByNameFromCacheTest() {
        AriusUserInfoDTO ariusUserInfoDTO = ariusUserInfoDTOFactory();
        Assertions.assertNull(ariusUserInfoService.getByName(ariusUserInfoDTO.getName()));
        Long id = ariusUserInfoService.save(ariusUserInfoDTO).getData();
        Assertions.assertEquals(id, ariusUserInfoService.getByName(ariusUserInfoDTO.getName()).getId());
    }

    @Test
    public void getUserRoleFromCacheTest() {
        AriusUserInfoDTO ariusUserInfoDTO = ariusUserInfoDTOFactory();
        Assertions.assertNull(ariusUserInfoService.getByName(ariusUserInfoDTO.getName()));
        ariusUserInfoService.save(ariusUserInfoDTO);
        Assertions.assertEquals(ariusUserInfoDTO.getRole(), ariusUserInfoService.getByName(ariusUserInfoDTO.getName()).getRole());
    }

    @Test
    public void listByRolesTest() {
        AriusUserInfoDTO ariusUserInfoDTO = ariusUserInfoDTOFactory();
        Long id = ariusUserInfoService.save(ariusUserInfoDTO).getData();
        List<Integer> roles = Arrays.asList(ariusUserInfoDTO.getRole());
        List<AriusUserInfo> ariusUserInfos = ariusUserInfoService.listByRoles(roles);
        Assertions.assertTrue(ariusUserInfos.stream().anyMatch(a -> a.getId().equals(id)));
    }

    @Test
    public void searchOnJobStaffByKeyWordTest() {
        AriusUserInfoDTO ariusUserInfoDTO = ariusUserInfoDTOFactory();
        Long id = ariusUserInfoService.save(ariusUserInfoDTO).getData();
        List<AriusUserInfo> ariusUserInfos = ariusUserInfoService.searchOnJobStaffByKeyWord(ariusUserInfoDTO.getName());
        Assertions.assertTrue(ariusUserInfos.stream().anyMatch(a -> a.getId().equals(id)));
    }

    @Test
    public void deleteUserRoleTest() {
        AriusUserInfoDTO ariusUserInfoDTO = ariusUserInfoDTOFactory();
        Long id = ariusUserInfoService.save(ariusUserInfoDTO).getData();
        Assertions.assertTrue(ariusUserInfoService.deleteUserRole(ariusUserInfoDTO.getName()));
        Assertions.assertEquals(AriusUserStatusEnum.NORMAL.getCode(),ariusUserInfoDAO.getById(id).getStatus());
    }

    @Test
    public void addUserRoleTest() {
        // 做了一些状态刷新的操作，查找记录，记录存在就刷新状态返回记录，否则设置字段，插入新的记录
        AriusUserInfoDTO ariusUserInfoDTO = ariusUserInfoDTOFactory();
        Long id = ariusUserInfoService.save(ariusUserInfoDTO).getData();
        ariusUserInfoDTO.setRole(-1);
        Assertions.assertTrue(ariusUserInfoService.addUserRole(ariusUserInfoDTO));
        Assertions.assertEquals(ariusUserInfoDTO.getRole(), ariusUserInfoDAO.getById(id).getRole());
        String newName = "new" + defaultName;
        ariusUserInfoDTO.setName(newName);
        Assertions.assertTrue(ariusUserInfoService.addUserRole(ariusUserInfoDTO));
        Assertions.assertEquals(AriusUserStatusEnum.NORMAL.getCode(), ariusUserInfoDAO.getByName(newName).getStatus());
    }

    @Test
    public void updateUserRoleTest() {
        AriusUserInfoDTO ariusUserInfoDTO = ariusUserInfoDTOFactory();
        Assertions.assertFalse(ariusUserInfoService.updateUserRole(ariusUserInfoDTO));
        ariusUserInfoService.save(ariusUserInfoDTO).getData();
        Assertions.assertTrue(ariusUserInfoService.updateUserRole(ariusUserInfoDTO));
    }
}
