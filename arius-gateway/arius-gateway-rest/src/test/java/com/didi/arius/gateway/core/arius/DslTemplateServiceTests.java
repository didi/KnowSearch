package com.didi.arius.gateway.core.arius;

import com.alibaba.fastjson.JSON;
import com.didi.arius.gateway.common.metadata.DSLTemplate;
import com.didi.arius.gateway.core.service.arius.DslTemplateService;
import com.didi.arius.gateway.rest.AriusGatewayApplication;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AriusGatewayApplication.class)
public class DslTemplateServiceTests {

    @Autowired
    private DslTemplateService dslTemplateService;

    @Before
    public void setUp() {
    }

    @Test
    public void testPutDSLTemplate(){
        dslTemplateService.putDSLTemplate("key1", new DSLTemplate(1, 1, true));
    }

    @Test
    public void testRemoveDSLTemplate() {
        dslTemplateService.putDSLTemplate("key1", new DSLTemplate(1, 1, true));
        DSLTemplate dslTemplate = dslTemplateService.getDSLTemplate("key1");
        System.out.println(JSON.toJSONString(dslTemplate));
        assertEquals(true, dslTemplate != null);
        dslTemplateService.removeDSLTemplate("key1");
        assertEquals(true, dslTemplateService.getDSLTemplate("key1") == null);
    }

    @Test
    public void testGetDSLTemplate() {
        dslTemplateService.putDSLTemplate("key1", new DSLTemplate(1, 1, true));
        DSLTemplate dslTemplate = dslTemplateService.getDSLTemplate("key1");
        System.out.println(JSON.toJSONString(dslTemplate));
        assertEquals(true, dslTemplate != null);
    }

    @Test
    public void testGetDslTemplateKeys() {
        dslTemplateService.putDSLTemplate("key1", new DSLTemplate(1, 1, true));
        final List<String> dslTemplateKeys = dslTemplateService.getDslTemplateKeys();
        System.out.println(JSON.toJSONString(dslTemplateKeys));
        assertEquals(true, dslTemplateKeys.size() >= 0);
    }

    @Test
    public void testPutNewDSLTemplate(){
        dslTemplateService.putNewDSLTemplate("key1", new DSLTemplate(1, 1, true));
    }

    @Test
    public void testGetNewDSLTemplate(){
        dslTemplateService.putNewDSLTemplate("key1", new DSLTemplate(1, 1, true));
        final DSLTemplate dslTemplate = dslTemplateService.getNewDSLTemplate("key1");
        System.out.println(JSON.toJSONString(dslTemplate));
        assertEquals(true, dslTemplate != null);
    }

    @Test
    public void testGetNewDslTemplateKeys(){
        dslTemplateService.putNewDSLTemplate("key1", new DSLTemplate(1, 1, true));
        final List<String> newDslTemplateKeys = dslTemplateService.getNewDslTemplateKeys();
        System.out.println(JSON.toJSONString(newDslTemplateKeys));
        assertEquals(true, newDslTemplateKeys.size() >= 0);
    }

    @Test
    public void testResetDslInfo(){
        dslTemplateService.resetDslInfo();
    }


}
