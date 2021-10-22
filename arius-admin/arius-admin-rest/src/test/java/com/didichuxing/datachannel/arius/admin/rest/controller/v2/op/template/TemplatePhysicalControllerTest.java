package com.didichuxing.datachannel.arius.admin.rest.controller.v2.op.template;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.IndexTemplatePhysicalDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.IndexTemplatePhysicalVO;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;

/**
 * @author d06679
 * @date 2019/4/24
 */
public class TemplatePhysicalControllerTest extends AriusAdminApplicationTests {

    private static final String URL_BASE_TEMPLATE = "/v2/op/template_physical";

    @Test
    public void list() throws Exception {
        ResponseEntity<Result> resp = restTemplate.exchange(baseUrl + URL_BASE_TEMPLATE + "/list", HttpMethod.POST,
            new HttpEntity<>(new IndexTemplatePhysicalDTO(), headers), Result.class);
        System.out.println(resp.getBody().toString());
        Result<List<IndexTemplatePhysicalVO>> result = JSON.parseObject(resp.getBody().toString(),
            new TypeReference<Result<List<IndexTemplatePhysicalVO>>>() {
            }.getType());
        Assert.assertTrue(result.getData().size() > 0);
    }

    @Test
    public void get() throws Exception {

    }

    @Test
    public void delete() throws Exception {

    }

    @Test
    public void upgrade() throws Exception {

    }

    @Test
    public void copy() throws Exception {

    }

    @Test
    public void edit() throws Exception {

    }

}