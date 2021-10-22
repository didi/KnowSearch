package com.didichuxing.datachannel.arius.admin.rest.controller.v2.op.template;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.constant.DataCenterEnum;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.IndexTemplateLogicDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.IndexTemplatePhysicalDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.OpLogicTemplateVO;
import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.client.constant.template.DataTypeEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.template.TemplateDeployRoleEnum;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import com.google.common.collect.Lists;

/**
 * @author d06679
 * @date 2019/4/22
 */
public class TemplateLogicControllerTest extends AriusAdminApplicationTests {

    private static final String URL_BASE_TEMPLATE = "/v2/op/template/logic";

    @Test
    public void list() throws Exception {
        ResponseEntity<Result> resp = restTemplate.exchange(baseUrl + URL_BASE_TEMPLATE + "/list", HttpMethod.POST,
            new HttpEntity<>(new IndexTemplateLogicDTO(), headers), Result.class);
        System.out.println(resp.getBody().toString());
        Result<List<OpLogicTemplateVO>> result = JSON.parseObject(resp.getBody().toString(),
            new TypeReference<Result<List<OpLogicTemplateVO>>>() {
            }.getType());
        Assert.assertTrue(result.getData().size() > 0);
    }

    @Test
    public void add() throws Exception {
        IndexTemplateLogicDTO logicDTO = new IndexTemplateLogicDTO();
        logicDTO.setName("template_test_us");
        logicDTO.setExpression("template_test_us*");
        logicDTO.setDataType(DataTypeEnum.LOG.getCode());
        logicDTO.setDateFormat("_yyyy-MM-dd");
        logicDTO.setDataCenter(DataCenterEnum.CN.getCode());
        logicDTO.setExpireTime(7);
        logicDTO.setLibraDepartmentId("1");
        logicDTO.setLibraDepartment("平台技术部-公共平台-订单系统");
        logicDTO.setResponsible("zhagnhezhen");
        logicDTO.setDateField("time");
        logicDTO.setQuota(10.0);
        logicDTO.setAppId(1587);
        logicDTO.setDesc("test");

        List<IndexTemplatePhysicalDTO> physicalInfos = Lists.newArrayList();
        logicDTO.setPhysicalInfos(physicalInfos);

        IndexTemplatePhysicalDTO physicalDTO = new IndexTemplatePhysicalDTO();
        physicalDTO.setCluster("elk-test");
        physicalDTO.setRack("r1");
        physicalDTO.setShard(1);
        physicalDTO.setRole(TemplateDeployRoleEnum.MASTER.getCode());
        physicalDTO.setConfig("{}");
        physicalInfos.add(physicalDTO);

        ResponseEntity<Result> resp = restTemplate.exchange(baseUrl + URL_BASE_TEMPLATE + "/add", HttpMethod.PUT,
            new HttpEntity<>(logicDTO, headers), Result.class);
        System.out.println(resp.getBody().toString());
        Result<Integer> resultAdd = JSON.parseObject(resp.getBody().toString(), new TypeReference<Result<Integer>>() {
        }.getType());

        Assert.assertTrue(resultAdd.getData() > 0);

        resp = restTemplate.exchange(baseUrl + URL_BASE_TEMPLATE + "/del?logicId=" + resultAdd.getData(),
            HttpMethod.DELETE, new HttpEntity<>(null, headers), Result.class);
        System.out.println(resp.getBody().toString());
        Result resultDel = JSON.parseObject(resp.getBody().toString(), new TypeReference<Result>() {
        }.getType());
        Assert.assertTrue(resultDel.getCode().equals(ResultType.SUCCESS.getCode()));
    }

    @Test
    public void edit() throws Exception {

    }

    @Test
    public void getConfig() throws Exception {

    }

    @Test
    public void updateConfig() throws Exception {

    }

}