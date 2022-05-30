package com.didichuxing.datachannel.arius.admin.rest.controller.v2.thirdpart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.biz.gateway.GatewayManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.GatewayHeartbeat;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.alias.IndexTemplateAliasDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.ScrollDslTemplateRequest;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.ScrollDslTemplateResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.po.dsl.DslTemplatePO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.GatewayESUserVO;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(controllers = ThirdpartGatewayController.class)
class ThirdpartGatewayControllerTest extends AriusAdminApplicationTest {
    
    @Autowired
    private MockMvc               mockMvc;
    @Autowired
    private WebApplicationContext context;
    
    @MockBean
    private GatewayManager mockGatewayManager;
    
    @Test
    void testHeartbeat1() throws Exception {
        // Setup
        when(mockGatewayManager.heartbeat(new GatewayHeartbeat("clusterName", "hostName", 0))).thenReturn(
                Result.buildFail(null));
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                put("/gateway/heartbeat").content("content").contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
        
        // Verify the results
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("expectedResponse", response.getContentAsString());
    }
    
    @Test
    void testHeartbeat1_GatewayManagerReturnsNoItem() throws Exception {
        // Setup
        when(mockGatewayManager.heartbeat(new GatewayHeartbeat("clusterName", "hostName", 0))).thenReturn(
                Result.buildSucc());
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                put("/gateway/heartbeat").content("content").contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
        
        // Verify the results
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("", response.getContentAsString());
    }
    
    @Test
    void testHeartbeat1_GatewayManagerReturnsFailure() throws Exception {
        // Setup
        when(mockGatewayManager.heartbeat(new GatewayHeartbeat("clusterName", "hostName", 0))).thenReturn(
                Result.buildFail());
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                put("/gateway/heartbeat").content("content").contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
        
        // Verify the results
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("expectedResponse", response.getContentAsString());
    }
    
    @Test
    void testHeartbeat2() throws Exception {
        // Setup
        when(mockGatewayManager.heartbeat("clusterName")).thenReturn(Result.buildFail(0));
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                        get("/gateway/alivecount").param("clusterName", "clusterName").accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        
        // Verify the results
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("expectedResponse", response.getContentAsString());
    }
    
    @Test
    void testHeartbeat2_GatewayManagerReturnsNoItem() throws Exception {
        // Setup
        when(mockGatewayManager.heartbeat("clusterName")).thenReturn(Result.buildSucc());
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                        get("/gateway/alivecount").param("clusterName", "clusterName").accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        
        // Verify the results
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("", response.getContentAsString());
    }
    
    @Test
    void testHeartbeat2_GatewayManagerReturnsFailure() throws Exception {
        // Setup
        when(mockGatewayManager.heartbeat("clusterName")).thenReturn(Result.buildFail());
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                        get("/gateway/alivecount").param("clusterName", "clusterName").accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        
        // Verify the results
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("expectedResponse", response.getContentAsString());
    }
    
    @Test
    void testGetGatewayAliveNodeNames() throws Exception {
        // Setup
        when(mockGatewayManager.getGatewayAliveNodeNames("Normal")).thenReturn(
                Result.buildFail(Arrays.asList("value")));
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                get("/gateway/aliveNodeName").accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
        
        // Verify the results
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("expectedResponse", response.getContentAsString());
    }
    
    @Test
    void testGetGatewayAliveNodeNames_GatewayManagerReturnsNoItem() throws Exception {
        // Setup
        when(mockGatewayManager.getGatewayAliveNodeNames("Normal")).thenReturn(Result.buildSucc());
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                get("/gateway/aliveNodeName").accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
        
        // Verify the results
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("", response.getContentAsString());
    }
    
    @Test
    void testGetGatewayAliveNodeNames_GatewayManagerReturnsNoItems() throws Exception {
        // Setup
        when(mockGatewayManager.getGatewayAliveNodeNames("Normal")).thenReturn(
                Result.buildFail(Collections.emptyList()));
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                get("/gateway/aliveNodeName").accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
        
        // Verify the results
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("[]", response.getContentAsString());
    }
    
    @Test
    void testGetGatewayAliveNodeNames_GatewayManagerReturnsFailure() throws Exception {
        // Setup
        when(mockGatewayManager.getGatewayAliveNodeNames("Normal")).thenReturn(Result.buildFail());
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                get("/gateway/aliveNodeName").accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
        
        // Verify the results
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("expectedResponse", response.getContentAsString());
    }
    
    @Test
    void testListApp() throws Exception {
        // Setup
        // Configure GatewayManager.listProject(...).
        final Result<List<GatewayESUserVO>> listResult = Result.buildFail(Arrays.asList(
                new GatewayESUserVO(0, "name", "verifyCode", 0, "cluster", 0, Arrays.asList("value"),
                        Arrays.asList("value"), Arrays.asList("value"), "dataCenter", 0, 0, 0, 0)));
        when(mockGatewayManager.listProject(any(HttpServletRequest.class))).thenReturn(listResult);
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                get("/gateway/listApp").accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
        
        // Verify the results
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("expectedResponse", response.getContentAsString());
    }
    
    @Test
    void testListApp_GatewayManagerReturnsNoItem() throws Exception {
        // Setup
        when(mockGatewayManager.listProject(any(HttpServletRequest.class))).thenReturn(Result.buildSucc());
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                get("/gateway/listApp").accept(MediaType.APPLICATION_JSON).header("X-ARIUS-GATEWAY-TICKET", "xTc59aY72")
        
        ).andReturn().getResponse();
        
        // Verify the results
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("", response.getContentAsString());
    }
    
    @Test
    void testListApp_GatewayManagerReturnsNoItems() throws Exception {
        // Setup
        // Configure GatewayManager.listProject(...).
        final Result<List<GatewayESUserVO>> listResult = Result.buildFail(Collections.emptyList());
        when(mockGatewayManager.listProject(any(HttpServletRequest.class))).thenReturn(listResult);
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                get("/gateway/listApp").accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
        
        // Verify the results
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("[]", response.getContentAsString());
    }
    
    @Test
    void testListApp_GatewayManagerReturnsFailure() throws Exception {
        // Setup
        // Configure GatewayManager.listProject(...).
        final Result<List<GatewayESUserVO>> listResult = Result.buildFail();
        when(mockGatewayManager.listProject(any(HttpServletRequest.class))).thenReturn(listResult);
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                get("/gateway/listApp").accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
        
        // Verify the results
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("expectedResponse", response.getContentAsString());
    }
    
    @Test
    void testGetTemplateMap() throws Exception {
        // Setup
        when(mockGatewayManager.getTemplateMap("cluster")).thenReturn(Result.buildFail(new HashMap<>()));
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                        get("/gateway/getTemplateMap").param("cluster", "cluster").accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        
        // Verify the results
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("expectedResponse", response.getContentAsString());
    }
    
    @Test
    void testGetTemplateMap_GatewayManagerReturnsNoItem() throws Exception {
        // Setup
        when(mockGatewayManager.getTemplateMap("cluster")).thenReturn(Result.buildSucc());
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                        get("/gateway/getTemplateMap").param("cluster", "cluster").accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        
        // Verify the results
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("", response.getContentAsString());
    }
    
    @Test
    void testGetTemplateMap_GatewayManagerReturnsFailure() throws Exception {
        // Setup
        when(mockGatewayManager.getTemplateMap("cluster")).thenReturn(Result.buildFail());
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                        get("/gateway/getTemplateMap").param("cluster", "cluster").accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        
        // Verify the results
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("expectedResponse", response.getContentAsString());
    }
    
    @Test
    void testListDeployInfo() throws Exception {
        // Setup
        when(mockGatewayManager.listDeployInfo("dataCenter")).thenReturn(Result.buildFail(new HashMap<>()));
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                        get("/gateway/listDeployInfo").param("dataCenter", "dataCenter").accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        
        // Verify the results
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("expectedResponse", response.getContentAsString());
    }
    
    @Test
    void testListDeployInfo_GatewayManagerReturnsNoItem() throws Exception {
        // Setup
        when(mockGatewayManager.listDeployInfo("dataCenter")).thenReturn(Result.buildSucc());
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                        get("/gateway/listDeployInfo").param("dataCenter", "dataCenter").accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        
        // Verify the results
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("", response.getContentAsString());
    }
    
    @Test
    void testListDeployInfo_GatewayManagerReturnsFailure() throws Exception {
        // Setup
        when(mockGatewayManager.listDeployInfo("dataCenter")).thenReturn(Result.buildFail());
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                        get("/gateway/listDeployInfo").param("dataCenter", "dataCenter").accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        
        // Verify the results
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("expectedResponse", response.getContentAsString());
    }
    
    @Test
    void testScrollSearchDslTemplate() throws Exception {
        // Setup
        // Configure GatewayManager.scrollSearchDslTemplate(...).
        final Result<ScrollDslTemplateResponse> scrollDslTemplateResponseResult = Result.buildFail(
                new ScrollDslTemplateResponse(Arrays.asList(
                        new DslTemplatePO("ariusCreateTime", "ariusModifyTime", 0.0, "requestType", "searchType", 0L,
                                0.0, 0.0, 0.0, 0.0, 0.0, "logTime", "indiceSample", "dslTemplate", 0L, "dslType",
                                "indices", "dslTemplateMd5", 0.0, 0.0, 0L, 0, "dsl", 0.0, "flinkTime", 0.0, false,
                                false, false, "checkMode", 0L, "version", "dslTag")), "scrollId"));
        when(mockGatewayManager.scrollSearchDslTemplate(new ScrollDslTemplateRequest())).thenReturn(
                scrollDslTemplateResponseResult);
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                post("/gateway/dsl/scrollDslTemplates").content("content").contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
        
        // Verify the results
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("expectedResponse", response.getContentAsString());
    }
    
    @Test
    void testScrollSearchDslTemplate_GatewayManagerReturnsNoItem() throws Exception {
        // Setup
        when(mockGatewayManager.scrollSearchDslTemplate(new ScrollDslTemplateRequest())).thenReturn(Result.buildSucc());
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                post("/gateway/dsl/scrollDslTemplates").content("content").contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
        
        // Verify the results
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("", response.getContentAsString());
    }
    
    @Test
    void testScrollSearchDslTemplate_GatewayManagerReturnsFailure() throws Exception {
        // Setup
        // Configure GatewayManager.scrollSearchDslTemplate(...).
        final Result<ScrollDslTemplateResponse> scrollDslTemplateResponseResult = Result.buildFail();
        when(mockGatewayManager.scrollSearchDslTemplate(new ScrollDslTemplateRequest())).thenReturn(
                scrollDslTemplateResponseResult);
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                post("/gateway/dsl/scrollDslTemplates").content("content").contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
        
        // Verify the results
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("expectedResponse", response.getContentAsString());
    }
    
    @Test
    void testAddAlias() throws Exception {
        // Setup
        when(mockGatewayManager.addAlias(new IndexTemplateAliasDTO())).thenReturn(Result.buildFail(false));
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                post("/gateway/addAlias").content("content").contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
        
        // Verify the results
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("expectedResponse", response.getContentAsString());
    }
    
    @Test
    void testAddAlias_GatewayManagerReturnsNoItem() throws Exception {
        // Setup
        when(mockGatewayManager.addAlias(new IndexTemplateAliasDTO())).thenReturn(Result.buildSucc());
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                post("/gateway/addAlias").content("content").contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
        
        // Verify the results
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("", response.getContentAsString());
    }
    
    @Test
    void testAddAlias_GatewayManagerReturnsFailure() throws Exception {
        // Setup
        when(mockGatewayManager.addAlias(new IndexTemplateAliasDTO())).thenReturn(Result.buildFail());
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                post("/gateway/addAlias").content("content").contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
        
        // Verify the results
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("expectedResponse", response.getContentAsString());
    }
    
    @Test
    void testDelAlias() throws Exception {
        // Setup
        when(mockGatewayManager.delAlias(new IndexTemplateAliasDTO())).thenReturn(Result.buildFail(false));
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                post("/gateway/delAlias").content("content").contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
        
        // Verify the results
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("expectedResponse", response.getContentAsString());
    }
    
    @Test
    void testDelAlias_GatewayManagerReturnsNoItem() throws Exception {
        // Setup
        when(mockGatewayManager.delAlias(new IndexTemplateAliasDTO())).thenReturn(Result.buildSucc());
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                post("/gateway/delAlias").content("content").contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
        
        // Verify the results
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("", response.getContentAsString());
    }
    
    @Test
    void testDelAlias_GatewayManagerReturnsFailure() throws Exception {
        // Setup
        when(mockGatewayManager.delAlias(new IndexTemplateAliasDTO())).thenReturn(Result.buildFail());
        
        // Run the test
        final MockHttpServletResponse response = mockMvc.perform(
                post("/gateway/delAlias").content("content").contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
        
        // Verify the results
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("expectedResponse", response.getContentAsString());
    }
}