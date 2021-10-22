package com.didichuxing.datachannel.arius.admin.rest.controller.v2.op.cluster;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ConsoleClusterVO;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESLogicClusterDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESLogicClusterRackInfoDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESClusterDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.LogicClusterRackVO;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ResourceLogicTypeEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;

/**
 * @author d06679
 * @date 2019/4/19
 */
public class ESLogicClusterControllerTest extends AriusAdminApplicationTests {

    private static final String URL_BASE_RESOURCE = "/v2/op/cluster";

    @Test
    public void list() throws Exception {
        ResponseEntity<Result> resp = restTemplate.exchange(baseUrl + URL_BASE_RESOURCE + "/list", HttpMethod.POST,
            new HttpEntity<>(new ESClusterDTO(), headers), Result.class);
        System.out.println(resp.getBody().toString());
        Result<List<ConsoleClusterVO>> result = JSON.parseObject(resp.getBody().toString(),
            new TypeReference<Result<List<ConsoleClusterVO>>>() {
            }.getType());
        Assert.assertTrue(result.getData().size() > 0);
    }

    @Test
    public void get() throws Exception {
        ResponseEntity<Result> resp = restTemplate.getForEntity(baseUrl + URL_BASE_RESOURCE + "/get?resourceId=2",
            Result.class);
        System.out.println(resp.getBody().toString());
        Result<ConsoleClusterVO> result = JSON.parseObject(resp.getBody().toString(),
            new TypeReference<Result<ConsoleClusterVO>>() {
            }.getType());
        Assert.assertTrue(result.getData().getId() == 2);
    }

    @Test
    public void add() throws Exception {
        ESLogicClusterDTO dto = new ESLogicClusterDTO();
        dto.setName("zhanghezhen测试");
        dto.setType(ResourceLogicTypeEnum.PUBLIC.getCode());
        dto.setAppId(1587);
        dto.setResponsible("sdfasfasfasdfasdfas");
        dto.setQuota(100.0);
        dto.setMemo("test");

        ResponseEntity<Result> resp = restTemplate.exchange(baseUrl + URL_BASE_RESOURCE + "/add", HttpMethod.PUT,
            new HttpEntity<>(dto, headers), Result.class);
        System.out.println(resp.getBody().toString());
        Result<Long> resultAdd = JSON.parseObject(resp.getBody().toString(), new TypeReference<Result<Long>>() {
        }.getType());
        Assert.assertTrue(resultAdd.getCode().equals(ResultType.SUCCESS.getCode()));

        resp = restTemplate.exchange(baseUrl + URL_BASE_RESOURCE + "/add", HttpMethod.PUT,
            new HttpEntity<>(dto, headers), Result.class);
        System.out.println(resp.getBody().toString());
        Result resultAddAgain = JSON.parseObject(resp.getBody().toString(), new TypeReference<Result>() {
        }.getType());
        Assert.assertTrue(resultAddAgain.getCode().equals(ResultType.DUPLICATION.getCode()));

        resp = restTemplate.exchange(baseUrl + URL_BASE_RESOURCE + "/del?resourceId=" + resultAdd.getData(),
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
    public void itemList() throws Exception {
    }

    @Test
    public void getItemsByResourceId() throws Exception {
        ResponseEntity<Result> resp = restTemplate
            .getForEntity(baseUrl + URL_BASE_RESOURCE + "/item/getByResource?resourceId=2", Result.class);
        System.out.println(resp.getBody().toString());
        Result<List<LogicClusterRackVO>> result = JSON.parseObject(resp.getBody().toString(),
            new TypeReference<Result<List<LogicClusterRackVO>>>() {
            }.getType());
        Assert.assertTrue(result.getData().size() >= 0);
    }

    @Test
    public void addItemForResource() throws Exception {
        ESLogicClusterRackInfoDTO newDTO = new ESLogicClusterRackInfoDTO();
        newDTO.setLogicClusterId(2L);
        newDTO.setPhyClusterName("elk-stable");
        newDTO.setRacks("r1,r2,r3");

        ResponseEntity<Result> resp = restTemplate.exchange(baseUrl + URL_BASE_RESOURCE + "/item/add", HttpMethod.PUT,
            new HttpEntity<>(newDTO, headers), Result.class);
        System.out.println(resp.getBody().toString());
        Result<Long> resultAdd = JSON.parseObject(resp.getBody().toString(), new TypeReference<Result<Long>>() {
        }.getType());
        Assert.assertTrue(resultAdd.getCode().equals(ResultType.SUCCESS.getCode()));

        resp = restTemplate.exchange(baseUrl + URL_BASE_RESOURCE + "/item/add", HttpMethod.PUT,
            new HttpEntity<>(newDTO, headers), Result.class);
        System.out.println(resp.getBody().toString());
        Result resultAddAgain = JSON.parseObject(resp.getBody().toString(), new TypeReference<Result>() {
        }.getType());
        Assert.assertTrue(resultAddAgain.getCode().equals(ResultType.DUPLICATION.getCode()));

        resp = restTemplate.getForEntity(baseUrl + URL_BASE_RESOURCE + "/item/getByResource?resourceId=2",
            Result.class);
        System.out.println(resp.getBody().toString());
        Result<List<LogicClusterRackVO>> resultGet = JSON.parseObject(resp.getBody().toString(),
            new TypeReference<Result<List<LogicClusterRackVO>>>() {
            }.getType());
        Assert.assertTrue(resultGet.getData().size() >= 3);

        for (LogicClusterRackVO itemVO : resultGet.getData()) {
            resp = restTemplate.exchange(baseUrl + URL_BASE_RESOURCE + "/item/del?itemId=" + itemVO.getId(),
                HttpMethod.DELETE, new HttpEntity<>(null, headers), Result.class);
            System.out.println(resp.getBody().toString());
            Result resultDel = JSON.parseObject(resp.getBody().toString(), new TypeReference<Result>() {
            }.getType());
            Assert.assertTrue(resultDel.getCode().equals(ResultType.SUCCESS.getCode()));
        }

    }

}