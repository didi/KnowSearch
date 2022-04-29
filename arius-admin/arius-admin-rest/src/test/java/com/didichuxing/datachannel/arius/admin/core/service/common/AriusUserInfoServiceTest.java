package com.didichuxing.datachannel.arius.admin.core.service.common;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.user.AriusUserInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.arius.AriusUserInfoPO;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUserRoleEnum;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.arius.AriusUserInfoDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import org.junit.jupiter.api.Assertions;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.didichuxing.datachannel.arius.admin.util.CustomDataSource.ariusUserInfoDTOFactory;

@Transactional(timeout = 1000)
@Rollback
public class AriusUserInfoServiceTest extends AriusAdminApplicationTest {

    @MockBean
    private AriusUserInfoDAO ariusUserInfoDAO;

    @Autowired
    private AriusUserInfoService ariusUserInfoService;

    private static final String defaultName = "wpk";

    @Test
    public void getByDomainAccountTest() {
        Mockito.when(ariusUserInfoDAO.getByDomainAccount(Mockito.anyString())).thenReturn(new AriusUserInfoPO());
        Assertions.assertNotNull(ariusUserInfoService.getByDomainAccount("admin"));
    }

    @Test
    public void saveTest() {
        AriusUserInfoDTO ariusUserInfoDTO = ariusUserInfoDTOFactory();
        ariusUserInfoDTO.setStatus(null);
        Assertions.assertTrue(ariusUserInfoService.save(ariusUserInfoDTO).failed());
        ariusUserInfoDTO.setDomainAccount(null);
        Assertions.assertTrue(ariusUserInfoService.save(ariusUserInfoDTO).failed());
        ariusUserInfoDTO.setPassword(null);
        Assertions.assertTrue(ariusUserInfoService.save(ariusUserInfoDTO).failed());
        ariusUserInfoDTO.setName(null);
        Assertions.assertTrue(ariusUserInfoService.save(ariusUserInfoDTO).failed());
        Assertions.assertTrue(ariusUserInfoService.save(null).failed());

        ariusUserInfoDTO = ariusUserInfoDTOFactory();
        Mockito.when(ariusUserInfoDAO.insert(Mockito.any())).thenReturn(1);
        ariusUserInfoDTO.setDomainAccount("admin_test");
        Mockito.when(ariusUserInfoDAO.getByDomainAccount("admin_test")).thenReturn(null);
        Assertions.assertTrue(ariusUserInfoService.save(ariusUserInfoDTO).success());

        ariusUserInfoDTO.setDomainAccount("admin");
        Assertions.assertTrue(ariusUserInfoService.save(ariusUserInfoDTO).success());
    }

    @Test
    public void saveByUsersTest() {
        Assertions.assertTrue(ariusUserInfoService.saveByUsers("").isEmpty());
        Mockito.when(ariusUserInfoDAO.insert(Mockito.any())).thenReturn(1);
        String userNames = "wpk,w,p,k";
        Assertions.assertEquals(userNames.split(",").length, ariusUserInfoService.saveByUsers(userNames).size());
    }

    @Test
    public void getUserByIdsTest() {
        Assertions.assertEquals("", ariusUserInfoService.getUserByIds(""));
        AriusUserInfoDTO ariusUserInfoDTO = ariusUserInfoDTOFactory();
        AriusUserInfoPO ariusUserInfoPO = CustomDataSource.getAriusUserInfoPO();
        ariusUserInfoPO.setDomainAccount(ariusUserInfoDTO.getDomainAccount());
        ariusUserInfoPO.setName(ariusUserInfoDTO.getName());
        ariusUserInfoPO.setId(ariusUserInfoDTO.getId());
        ariusUserInfoPO.setName(ariusUserInfoDTO.getName());
        Mockito.when(ariusUserInfoDAO.getById(Mockito.anyLong())).thenReturn(ariusUserInfoPO);
        Assertions.assertEquals(ariusUserInfoDTO.getName(), ariusUserInfoService.getUserByIds("1,2"));
    }

    @Test
    public void listAllEnableTest() {
        Mockito.when(ariusUserInfoDAO.listAllEnable()).thenReturn(CustomDataSource.getAriusUserInfoPOList());
        Assertions.assertEquals(CustomDataSource.SIZE, ariusUserInfoService.listAllEnable().size());
    }

    @Test
    public void deleteTest() {
        Mockito.when(ariusUserInfoDAO.update(Mockito.any())).thenReturn(1);
        Assertions.assertTrue(ariusUserInfoService.delete(1L));
    }

    @Test
    public void isOpByUserNameTest() {
        AriusUserInfoDTO ariusUserInfoDTO = ariusUserInfoDTOFactory();
        Mockito.when(ariusUserInfoDAO.getByDomainAccount(ariusUserInfoDTO.getName())).thenReturn(null);
        Assertions.assertFalse(ariusUserInfoService.isOPByDomainAccount(ariusUserInfoDTO.getDomainAccount()));

        ariusUserInfoDTO.setRole(AriusUserRoleEnum.OP.getRole());
        AriusUserInfoPO ariusUserInfoPO = CustomDataSource.getAriusUserInfoPO();
        ariusUserInfoPO.setRole(AriusUserRoleEnum.OP.getRole());
        Mockito.when(ariusUserInfoDAO.getByDomainAccount(Mockito.anyString())).thenReturn(ariusUserInfoPO);
        Assertions.assertTrue(ariusUserInfoService.isOPByDomainAccount(ariusUserInfoDTO.getDomainAccount()));
    }

    @Test
    public void isRDByUserNameTest() {
        AriusUserInfoDTO ariusUserInfoDTO = ariusUserInfoDTOFactory();
        Mockito.when(ariusUserInfoDAO.getByDomainAccount(ariusUserInfoDTO.getName())).thenReturn(null);
        Assertions.assertFalse(ariusUserInfoService.isRDByDomainAccount(ariusUserInfoDTO.getDomainAccount()));

        ariusUserInfoDTO.setRole(AriusUserRoleEnum.RD.getRole());
        AriusUserInfoPO ariusUserInfoPO = CustomDataSource.getAriusUserInfoPO();
        ariusUserInfoPO.setRole(AriusUserRoleEnum.RD.getRole());
        Mockito.when(ariusUserInfoDAO.getByDomainAccount(Mockito.anyString())).thenReturn(ariusUserInfoPO);
        Assertions.assertTrue(ariusUserInfoService.isRDByDomainAccount(ariusUserInfoDTO.getDomainAccount()));
    }

    @Test
    public void getByNameTest() {
        Mockito.when(ariusUserInfoDAO.getByName(Mockito.any())).thenReturn(CustomDataSource.getAriusUserInfoPO());
        Assertions.assertNotNull(ariusUserInfoService.getByName("admin"));
    }

    @Test
    public void listByRolesTest() {
        Mockito.when(ariusUserInfoDAO.listByRoles(Mockito.anyList())).thenReturn(CustomDataSource.getAriusUserInfoPOList());
        List<Integer> roles = new ArrayList<>();
        roles.add(1);
        Assertions.assertFalse(ariusUserInfoService.listByRoles(roles).isEmpty());
    }

    @Test
    public void searchOnJobStaffByKeyWordTest() {
        Mockito.when(ariusUserInfoDAO.getByDomainAccount("admin")).thenReturn(CustomDataSource.getAriusUserInfoPO());
        Assertions.assertFalse(ariusUserInfoService.searchOnJobStaffByKeyWord("admin").isEmpty());
    }

    @Test
    public void deleteUserRoleTest() {
        AriusUserInfoDTO ariusUserInfoDTO = ariusUserInfoDTOFactory();
        ariusUserInfoDTO.setName("test");
        Mockito.when(ariusUserInfoDAO.getByName("test")).thenReturn(null);
        Assertions.assertTrue(ariusUserInfoService.deleteUserRole(ariusUserInfoDTO.getName()));

        Mockito.when(ariusUserInfoDAO.getByName(Mockito.anyString())).thenReturn(null);
        Mockito.when(ariusUserInfoDAO.update(Mockito.any())).thenReturn(1);
        ariusUserInfoDTO = ariusUserInfoDTOFactory();
        Assertions.assertTrue(ariusUserInfoService.deleteUserRole(ariusUserInfoDTO.getName()));
    }

    @Test
    public void addUserRoleTest() {
        AriusUserInfoDTO ariusUserInfoDTO = ariusUserInfoDTOFactory();
        ariusUserInfoDTO.setName("test");
        Mockito.when(ariusUserInfoDAO.getByName("test")).thenReturn(CustomDataSource.getAriusUserInfoPO());
        Mockito.when(ariusUserInfoDAO.update(Mockito.any())).thenReturn(1);
        Assertions.assertTrue(ariusUserInfoService.addUserRole(ariusUserInfoDTO));

        Mockito.when(ariusUserInfoDAO.getByName(Mockito.any())).thenReturn(null);
        Mockito.when(ariusUserInfoDAO.insert(Mockito.any())).thenReturn(1);
        ariusUserInfoDTO = ariusUserInfoDTOFactory();
        Assertions.assertTrue(ariusUserInfoService.addUserRole(ariusUserInfoDTO));
    }

    @Test
    public void updateUserRoleTest() {
        AriusUserInfoDTO ariusUserInfoDTO = ariusUserInfoDTOFactory();
        ariusUserInfoDTO.setName("test");
        Mockito.when(ariusUserInfoDAO.getByName("test")).thenReturn(null);
        Assertions.assertFalse(ariusUserInfoService.updateUserRole(ariusUserInfoDTO));
        Mockito.when(ariusUserInfoDAO.getByName(Mockito.anyString())).thenReturn(CustomDataSource.getAriusUserInfoPO());
        Mockito.when(ariusUserInfoDAO.update(Mockito.any())).thenReturn(1);
        ariusUserInfoDTO = ariusUserInfoDTOFactory();
        Assertions.assertTrue(ariusUserInfoService.updateUserRole(ariusUserInfoDTO));
    }
}
