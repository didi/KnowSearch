package com.didichuxing.datachannel.arius.admin.rest.controller.v3.app;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.biz.app.ESUserManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ESUserDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ESUser;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ConsoleESUserVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ConsoleESUserWithVerifyCodeVO;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;

@AutoConfigureMockMvc
class ESUserV3ControllerTest extends AriusAdminApplicationTest {
    
    @MockBean
    private ESUserManager mockEsUserManager;
    
    @Test
    void testCreateESUerByProject() throws Exception {
        final ESUserDTO esUserDTO = new ESUserDTO();
        //esUserDTO.setId();
        //esUserDTO.setIsRoot();
        //esUserDTO.setVerifyCode();
        //esUserDTO.setResponsible();
        //esUserDTO.setMemo();
        //esUserDTO.setIsActive();
        //esUserDTO.setQueryThreshold();
        //esUserDTO.setCluster();
        //esUserDTO.setSearchType();
        //esUserDTO.setDataCenter();
        //esUserDTO.setProjectId();
        //// Setup
        //when(mockEsUserManager.registerESUser(
        //        ).thenReturn(Result.buildFail(0));
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                post(V3+"/es-user/1595", 0).content("content").contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .headers(headers )
        
        ).andReturn().getResponse();
        
        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("expectedResponse");
    }
    
    @Test
    void testCreateESUerByProject_ESUserManagerReturnsNoItem() throws Exception {
        // Setup
        when(mockEsUserManager.registerESUser(
                new ESUserDTO(0, 0, "verifyCode", "responsible", "memo", 0, 0, "cluster", 0, "dataCenter", 0), 0,
                "operator")).thenReturn(Result.buildSucc());
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                post("/es-user/{projectId}", 0).content("content").contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
        
        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("");
    }
    
    @Test
    void testCreateESUerByProject_ESUserManagerReturnsFailure() throws Exception {
        // Setup
        when(mockEsUserManager.registerESUser(
                new ESUserDTO(0, 0, "verifyCode", "responsible", "memo", 0, 0, "cluster", 0, "dataCenter", 0), 0,
                "operator")).thenReturn(Result.buildFail());
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                post("/es-user/{projectId}", 0).content("content").contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
        
        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("expectedResponse");
    }
    
    @Test
    void testGetNoCodeESUser() throws Exception {
        // Setup
        // Configure ESUserManager.getNoCodeESUser(...).
        final Result<List<ConsoleESUserWithVerifyCodeVO>> listResult = Result.buildFail(Arrays.asList(
                new ConsoleESUserWithVerifyCodeVO(0, "name", "verifyCode", "departmentId", "department", "responsible",
                        "memo", 0, "dataCenter")));
        when(mockEsUserManager.getNoCodeESUser(0, "operator")).thenReturn(listResult);
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                get("/es-user/get-no-code-login").accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
        
        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("expectedResponse");
    }
    
    @Test
    void testGetNoCodeESUser_ESUserManagerReturnsNoItem() throws Exception {
        // Setup
        when(mockEsUserManager.getNoCodeESUser(0, "operator")).thenReturn(Result.buildSucc());
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                get("/es-user/get-no-code-login").accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
        
        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("");
    }
    
    @Test
    void testGetNoCodeESUser_ESUserManagerReturnsNoItems() throws Exception {
        // Setup
        // Configure ESUserManager.getNoCodeESUser(...).
        final Result<List<ConsoleESUserWithVerifyCodeVO>> listResult = Result.buildFail(Collections.emptyList());
        when(mockEsUserManager.getNoCodeESUser(0, "operator")).thenReturn(listResult);
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                get("/es-user/get-no-code-login").accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
        
        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("[]");
    }
    
    @Test
    void testGetNoCodeESUser_ESUserManagerReturnsFailure() throws Exception {
        // Setup
        // Configure ESUserManager.getNoCodeESUser(...).
        final Result<List<ConsoleESUserWithVerifyCodeVO>> listResult = Result.buildFail();
        when(mockEsUserManager.getNoCodeESUser(0, "operator")).thenReturn(listResult);
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                get("/es-user/get-no-code-login").accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
        
        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("expectedResponse");
    }
    
    @Test
    void testListESUserByProjectId() throws Exception {
        // Setup
        // Configure ESUserManager.listESUsersByProjectId(...).
        final Result<List<ESUser>> listResult = Result.buildFail(Arrays.asList(
                new ESUser(0, "name", 0, "verifyCode", "departmentId", "department", "responsible", "memo", 0, 0,
                        "cluster", 0, "dataCenter", 0, false, "ip", "indexExp")));
        when(mockEsUserManager.listESUsersByProjectId(0, "operator")).thenReturn(listResult);
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(get("/es-user").accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        
        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("expectedResponse");
    }
    
    @Test
    void testListESUserByProjectId_ESUserManagerReturnsNoItem() throws Exception {
        // Setup
        when(mockEsUserManager.listESUsersByProjectId(0, "operator")).thenReturn(Result.buildSucc());
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(get("/es-user").accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        
        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("");
    }
    
    @Test
    void testListESUserByProjectId_ESUserManagerReturnsNoItems() throws Exception {
        // Setup
        // Configure ESUserManager.listESUsersByProjectId(...).
        final Result<List<ESUser>> listResult = Result.buildFail(Collections.emptyList());
        when(mockEsUserManager.listESUsersByProjectId(0, "operator")).thenReturn(listResult);
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(get("/es-user").accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        
        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("[]");
    }
    
    @Test
    void testListESUserByProjectId_ESUserManagerReturnsFailure() throws Exception {
        // Setup
        // Configure ESUserManager.listESUsersByProjectId(...).
        final Result<List<ESUser>> listResult = Result.buildFail();
        when(mockEsUserManager.listESUsersByProjectId(0, "operator")).thenReturn(listResult);
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(get("/es-user").accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        
        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("expectedResponse");
    }
    
    @Test
    void testDeleteESUserByProject() throws Exception {
        // Setup
        when(mockEsUserManager.deleteESUserByProject(0, 0, "operator")).thenReturn(Result.buildFail(null));
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                        delete("/es-user/{projectId}/{esUser}", 0, 0).accept(MediaType.APPLICATION_JSON)).andReturn()
                .getResponse();
        
        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("expectedResponse");
    }
    
    @Test
    void testDeleteESUserByProject_ESUserManagerReturnsNoItem() throws Exception {
        // Setup
        when(mockEsUserManager.deleteESUserByProject(0, 0, "operator")).thenReturn(Result.buildSucc());
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                        delete("/es-user/{projectId}/{esUser}", 0, 0).accept(MediaType.APPLICATION_JSON)).andReturn()
                .getResponse();
        
        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("");
    }
    
    @Test
    void testDeleteESUserByProject_ESUserManagerReturnsFailure() throws Exception {
        // Setup
        when(mockEsUserManager.deleteESUserByProject(0, 0, "operator")).thenReturn(Result.buildFail());
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                        delete("/es-user/{projectId}/{esUser}", 0, 0).accept(MediaType.APPLICATION_JSON)).andReturn()
                .getResponse();
        
        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("expectedResponse");
    }
    
    @Test
    void testDeleteAllESUserByProject() throws Exception {
        // Setup
        when(mockEsUserManager.deleteAllESUserByProject(0, "operator")).thenReturn(Result.buildFail(null));
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                delete("/es-user/{projectId}", 0).accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
        
        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("expectedResponse");
    }
    
    @Test
    void testDeleteAllESUserByProject_ESUserManagerReturnsNoItem() throws Exception {
        // Setup
        when(mockEsUserManager.deleteAllESUserByProject(0, "operator")).thenReturn(Result.buildSucc());
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                delete("/es-user/{projectId}", 0).accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
        
        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("");
    }
    
    @Test
    void testDeleteAllESUserByProject_ESUserManagerReturnsFailure() throws Exception {
        // Setup
        when(mockEsUserManager.deleteAllESUserByProject(0, "operator")).thenReturn(Result.buildFail());
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                delete("/es-user/{projectId}", 0).accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
        
        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("expectedResponse");
    }
    
    @Test
    void testUpdate1() throws Exception {
        // Setup
        when(mockEsUserManager.editESUser(
                new ESUserDTO(0, 0, "verifyCode", "responsible", "memo", 0, 0, "cluster", 0, "dataCenter", 0),
                "operator")).thenReturn(Result.buildFail(null));
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                put("/es-user/path").content("content").contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
        
        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("expectedResponse");
    }
    
    @Test
    void testUpdate1_ESUserManagerReturnsNoItem() throws Exception {
        // Setup
        when(mockEsUserManager.editESUser(
                new ESUserDTO(0, 0, "verifyCode", "responsible", "memo", 0, 0, "cluster", 0, "dataCenter", 0),
                "operator")).thenReturn(Result.buildSucc());
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                put("/es-user/path").content("content").contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
        
        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("");
    }
    
    @Test
    void testUpdate1_ESUserManagerReturnsFailure() throws Exception {
        // Setup
        when(mockEsUserManager.editESUser(
                new ESUserDTO(0, 0, "verifyCode", "responsible", "memo", 0, 0, "cluster", 0, "dataCenter", 0),
                "operator")).thenReturn(Result.buildFail());
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                put("/es-user/path").content("content").contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
        
        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("expectedResponse");
    }
    

    
 
    
    
    
    @Test
    void testGet() throws Exception {
        // Setup
        // Configure ESUserManager.get(...).
        final Result<ConsoleESUserVO> consoleESUserVOResult = Result.buildFail(
                new ConsoleESUserVO(0, "memo", 0, "dataCenter"));
        when(mockEsUserManager.get(0)).thenReturn(consoleESUserVOResult);
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                get("/es-user/{esUser}", 0).accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
        
        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("expectedResponse");
    }
    
    @Test
    void testGet_ESUserManagerReturnsNoItem() throws Exception {
        // Setup
        when(mockEsUserManager.get(0)).thenReturn(Result.buildSucc());
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                get("/es-user/{esUser}", 0).accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
        
        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("");
    }
    
    @Test
    void testGet_ESUserManagerReturnsFailure() throws Exception {
        // Setup
        // Configure ESUserManager.get(...).
        final Result<ConsoleESUserVO> consoleESUserVOResult = Result.buildFail();
        when(mockEsUserManager.get(0)).thenReturn(consoleESUserVOResult);
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                get("/es-user/{esUser}", 0).accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
        
        // Verify the results
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo("expectedResponse");
    }
}