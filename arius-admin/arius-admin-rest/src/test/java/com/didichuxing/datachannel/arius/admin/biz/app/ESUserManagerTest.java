package com.didichuxing.datachannel.arius.admin.biz.app;

import static org.assertj.core.api.Assertions.assertThat;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ConsoleESUserDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ESUserConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ESUserDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ESUser;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ESUserConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ConsoleESUserVO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.app.ESUserDAO;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Rollback
public class ESUserManagerTest extends AriusAdminApplicationTest {
    
    @Autowired
    private ESUserManager esUserManagerImplUnderTest;
    @Autowired
    private ESUserDAO     esUserDAO;

    
    @Test
    public void testListESUsersByAllProject() {
        // Setup
        final ArrayList<Integer> projectIds = Lists.newArrayList(1593);
    
        // Run the test
        final List<Integer> userProjectIds = esUserManagerImplUnderTest.listESUsers().getData().stream()
                .map(ESUser::getProjectId).distinct().collect(Collectors.toList());
    
        // Verify the results
        assertThat(userProjectIds).isEqualTo(projectIds);
    }
    
    @Test
    void testListESUsersByProjectId() {
        // Setup
        // Run the test
        final Result<List<ESUser>> result = esUserManagerImplUnderTest.listESUsersByProjectId(1593, "admin");
        
        // Verify the results
        assertThat(result.getData()).hasSize(1);
    }
    
 
    
    @Test
    void testGetESUsersMap() {
        // Setup
        final Result<Map<Integer, List<ESUser>>> expectedResult = Result.buildFail(new HashMap<>());
        
        // Run the test
        final Result<Map<Integer, List<ESUser>>> result = esUserManagerImplUnderTest.getESUsersMap();
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testRegisterESUser() {
        // Setup
        final ESUserDTO appDTO = new ESUserDTO();
        appDTO.setIsRoot(0);
        appDTO.setVerifyCode("string");
        appDTO.setResponsible("admin");
        appDTO.setMemo("test");
        appDTO.setIsActive(1);
        appDTO.setQueryThreshold(100);
        appDTO.setCluster("logi-elasticsearch-7.6.0");
        appDTO.setSearchType(1);
        appDTO.setDataCenter("cn");
        appDTO.setProjectId(1595);
        final Integer count = esUserDAO.maxById();
    
        // Run the test
        final Result<Integer> result = esUserManagerImplUnderTest.registerESUser(appDTO, 1595, "admin");
        
        // Verify the results
        assertThat(result.getData()).isEqualTo(count + 1);
    }
    
    @Test
    void testGetProjectName() {
        // Setup
        final Result<String> expectedResult = Result.buildSucc("superApp");
        
        // Run the test
        final Result<String> result = esUserManagerImplUnderTest.getProjectName(13);
        
        // Verify the results
        assertThat(result.getData()).isEqualTo("superApp");
    }
    
    @Test
    void testUpdateESUserConfig() {
        // Setup
        final ESUserConfigDTO configDTO = new ESUserConfigDTO(0, 0, 0, 0, 0);
        
        // Run the test
        final Result<Void> result = esUserManagerImplUnderTest.updateESUserConfig(configDTO, "operator");
        
        // Verify the results
    }
    
    @Test
    void testEditESUser() {
        // Setup
        final ESUserDTO esUserDTO = new ESUserDTO(0, 0, "verifyCode", "responsible", "memo", 0, 0, "cluster", 0,
                "dataCenter", 0);
        
        // Run the test
        final Result<Void> result = esUserManagerImplUnderTest.editESUser(esUserDTO, "operator");
        
        // Verify the results
    }
    
    @Test
    void testDeleteESUserByProject() {
        // Setup
        // Run the test
        final Result<Void> result = esUserManagerImplUnderTest.deleteESUserByProject(0, 0, "operator");
        
        // Verify the results
    }
    
    @Test
    void testDeleteAllESUserByProject() {
        // Setup
        // Run the test
        final Result<Void> result = esUserManagerImplUnderTest.deleteAllESUserByProject(0, "operator");
        
        // Verify the results
    }
    
    @Test
    void testGetESUserConfig() {
        // Setup
        final ESUserConfig expectedResult = new ESUserConfig(0, 0, 0, 0, 0);
        
        // Run the test
        final ESUserConfig result = esUserManagerImplUnderTest.getESUserConfig(0);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testIsESUserExists1() {
        // Setup
        // Run the test
        final boolean result = esUserManagerImplUnderTest.isESUserExists(0);
        
        // Verify the results
        assertThat(result).isFalse();
    }
    
    @Test
    void testIsESUserExists2() {
        // Setup
        final ESUser esUser = new ESUser(0, "name", 0, "verifyCode", "departmentId", "department", "responsible",
                "memo", 0, 0, "cluster", 0, "dataCenter", 0, false, "ip", "indexExp");
        
        // Run the test
        final boolean result = esUserManagerImplUnderTest.isESUserExists(esUser);
        
        // Verify the results
        assertThat(result).isFalse();
    }
    
    @Test
    void testIsSuperESUser() {
        // Setup
        // Run the test
        final boolean result = esUserManagerImplUnderTest.isSuperESUser(0);
        
        // Verify the results
        assertThat(result).isFalse();
    }
    
    @Test
    void testVerifyAppCode() {
        // Setup
        // Run the test
        final Result<Void> result = esUserManagerImplUnderTest.verifyAppCode(0, "verifyCode");
        
        // Verify the results
    }
    
    @Test
    void testUpdate() {
        // Setup
        final HttpServletRequest request = new MockHttpServletRequest();
        final ConsoleESUserDTO consoleESUserDTO = new ConsoleESUserDTO(0, "memo", "dataCenter");
        
        // Run the test
        final Result<Void> result = esUserManagerImplUnderTest.update(, request, , consoleESUserDTO);
        
        // Verify the results
    }
    
    @Test
    void testGet() {
        // Setup
        final Result<ConsoleESUserVO> expectedResult = Result.buildFail(
                new ConsoleESUserVO(0, "memo", 0, "dataCenter"));
        
        // Run the test
        final Result<ConsoleESUserVO> result = esUserManagerImplUnderTest.get(13);
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
    
    @Test
    void testList() {
        // Setup
        final Result<List<ConsoleESUserVO>> expectedResult = Result.buildFail(
                Arrays.asList(new ConsoleESUserVO(0, "memo", 0, "dataCenter")));
        
        // Run the test
        final Result<List<ConsoleESUserVO>> result = esUserManagerImplUnderTest.list();
        
        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }
}