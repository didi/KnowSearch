package com.didichuxing.datachannel.arius.admin.rest.controller.v2.op.cluster;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ESClusterPhyVO;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESClusterDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ESRoleClusterHostVO;
import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;

/**
 * @author d06679
 * @date 2019/4/19
 */
public class ESPhyClusterControllerTest extends AriusAdminApplicationTests {

    private static final String URL_BASE_CLUSTER = "/v2/op/cluster";

    @Test
    public void list() throws Exception {
        ResponseEntity<Result> resp = restTemplate.exchange(baseUrl + URL_BASE_CLUSTER + "/list", HttpMethod.POST,
            new HttpEntity<>(new ESClusterDTO(), headers), Result.class);
        System.out.println(resp.getBody().toString());
        Result<List<ESClusterPhyVO>> result = JSON.parseObject(resp.getBody().toString(),
            new TypeReference<Result<List<ESClusterPhyVO>>>() {
            }.getType());
        Assert.assertTrue(result.getData().size() > 0);
    }

    @Test
    public void edit() throws Exception {

    }

    @Test
    public void syncClusterNodeInfoFromESTask() throws Exception {
        ResponseEntity<Result> resp = restTemplate.exchange(
            baseUrl + URL_BASE_CLUSTER + "/syncClusterNodeInfoFromEsTask?cluster=elk-test", HttpMethod.POST,
            new HttpEntity<>(null, headers), Result.class);
        System.out.println(resp.getBody().toString());
        Result result = JSON.parseObject(resp.getBody().toString(), new TypeReference<Result>() {
        }.getType());
        Assert.assertTrue(result.getCode().equals(ResultType.SUCCESS.getCode()));
    }

    @Test
    public void nodeList() throws Exception {
        ResponseEntity<Result> resp = restTemplate.exchange(baseUrl + URL_BASE_CLUSTER + "/node/list", HttpMethod.POST,
            new HttpEntity<>(new ESClusterDTO(), headers), Result.class);
        System.out.println(resp.getBody().toString());
        Result<List<ESRoleClusterHostVO>> result = JSON.parseObject(resp.getBody().toString(),
            new TypeReference<Result<List<ESRoleClusterHostVO>>>() {
            }.getType());
        Assert.assertTrue(result.getData().size() > 0);
    }

    @Test
    public void getNodesByCluster() throws Exception {
        ResponseEntity<Result> resp = restTemplate
            .getForEntity(baseUrl + URL_BASE_CLUSTER + "/node/listByCluster?cluster=lyn1223", Result.class);
        System.out.println(resp.getBody().toString());
        Result<List<ESRoleClusterHostVO>> result = JSON.parseObject(resp.getBody().toString(),
            new TypeReference<Result<List<ESRoleClusterHostVO>>>() {
            }.getType());
        Assert.assertTrue(result.getData().size() > 0);
    }

    @Test
    public void editNodeStatus() throws Exception {

    }

}