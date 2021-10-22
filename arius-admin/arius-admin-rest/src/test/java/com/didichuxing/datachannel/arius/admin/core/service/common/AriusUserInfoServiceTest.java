package com.didichuxing.datachannel.arius.admin.core.service.common;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.user.AriusUserInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.arius.AriusUserInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.po.arius.AriusUserInfoPO;
import com.didichuxing.datachannel.arius.admin.common.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUserRoleEnum;
import com.didichuxing.datachannel.arius.admin.core.service.common.*;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.app.AppDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.arius.AriusUserInfoDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.resource.LogicClusterDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplateLogicDAO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Transactional
@Rollback
//@SpringBootTest
public class AriusUserInfoServiceTest extends AriusAdminApplicationTests {

    @Autowired
    private AriusUserInfoDAO ariusUserInfoDAO;

    @Autowired
    private IndexTemplateLogicDAO templateLogicDAO;

    @Autowired
    private LogicClusterDAO logicClusterDAO;

    @Autowired
    private HandleFactory handleFactory;

    @Autowired
    private AppDAO appDAO;

    @Autowired
    private AriusUserInfoService ariusUserInfoService;

    private static final String defaultName = "wpk";

    private static AriusUserInfoDTO newAriusUserInfoDTOFactory() {
        AriusUserInfoDTO ariusUserInfoDTO = new AriusUserInfoDTO();
        ariusUserInfoDTO.setEmail("");
        ariusUserInfoDTO.setMobile("");
        ariusUserInfoDTO.setStatus(1);
        ariusUserInfoDTO.setDomainAccount(defaultName);
        ariusUserInfoDTO.setName(defaultName);
        ariusUserInfoDTO.setPassword("1");
        ariusUserInfoDTO.setRole(2);
        return ariusUserInfoDTO;
    }

    @Test
    public void refreshCache() {

    }

    @Test
    public void save() {
        //插入新的记录
        AriusUserInfoDTO ariusUserInfoDTO = newAriusUserInfoDTOFactory();
        Long id = ariusUserInfoService.save(ariusUserInfoDTO);
        AriusUserInfoPO ariusUserInfoPO = ariusUserInfoDAO.getById(id);
        Assertions.assertEquals(id,ariusUserInfoPO.getId());
        //插入旧的记录
        Assertions.assertEquals(id,ariusUserInfoService.save(ariusUserInfoDTO));
    }

    @Test
    public void saveByUsers() {
        //插入空记录
        String userNames = "";
        Assertions.assertTrue(ariusUserInfoService.saveByUsers(userNames).isEmpty());
        //插入连串的用户名
        userNames = "wpk,w,p,k";
        List<Long> ids = ariusUserInfoService.saveByUsers(userNames);
        List<Long> checkIds = new ArrayList<>();
        for (String user : userNames.split(",")) {
            checkIds.add(ariusUserInfoDAO.getByName(user).getId());
        }
        Assertions.assertEquals(ids, checkIds);

    }

    @Test
    public void getUserByIds() {

    }

    @Test
    public void delete() {

    }

    @Test
    public void processUserDuplicate() {

    }

    @Test
    public void isOpByUserName() {

    }

    @Test
    public void isRDByUserName() {

    }

    @Test
    public void getByName() {

    }

    @Test
    public void getByNameFromCache() {

    }

    @Test
    public void getUserRoleFromCache() {

    }

    @Test
    public void deleteUserRole() {

    }

    @Test
    public void addUserRole() {

    }

    @Test
    public void updateUserRole() {

    }

    @Test
    public void syncUserInfoToDbFromLoginProtocol() {

    }
}
