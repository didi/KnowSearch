package com.didichuxing.datachannel.arius.admin.rest.controller.v2.op.app;

import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.app.AppConfigDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.app.AppDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.app.AppConfigVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.app.AppVO;
import com.didichuxing.datachannel.arius.admin.client.constant.app.AppSearchTypeEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;

/**
 * @author d06679
 * @date 2019/4/11
 */
public class AppOpControllerTest extends AriusAdminApplicationTests {

    private static final String URL_BASE_APP = "/v2/op/app";

    @Test
    public void list() throws Exception {
        ResponseEntity<Result> resp = restTemplate.getForEntity(baseUrl + URL_BASE_APP + "/list", Result.class);
        System.out.println(resp.getBody().toString());
        Result<List<AppVO>> result = JSON.parseObject(resp.getBody().toString(),
            new TypeReference<Result<List<AppVO>>>() {
            }.getType());
        Assert.assertTrue(result.getData().size() > 0);
    }

    @Test
    public void get() throws Exception {
        ResponseEntity<Result> resp = restTemplate.getForEntity(baseUrl + URL_BASE_APP + "/get?appId=6", Result.class);
        System.out.println(resp.getBody().toString());
        Result<AppVO> result = JSON.parseObject(resp.getBody().toString(), new TypeReference<Result<AppVO>>() {
        }.getType());
        Assert.assertTrue(result.getData().getId().equals(6));
    }

    @Test
    public void addForbidden() throws Exception {
        AppDTO appDTO = getNewApp();
        ResponseEntity<Result> resp = restTemplate.postForEntity(baseUrl + URL_BASE_APP + "/add", appDTO, Result.class);
        System.out.println(resp.getBody().toString());
        Assert.assertTrue(resp.getBody().getCode() == ResultType.OPERATE_FORBIDDEN_ERROR.getCode());
    }

    @Test
    public void add() throws Exception {
        AppDTO appDTO = getNewApp();
        ResponseEntity<Result> resp = restTemplate.exchange(baseUrl + URL_BASE_APP + "/add", HttpMethod.POST,
            new HttpEntity<>(appDTO, headers), Result.class);
        System.out.println(resp.getBody().toString());
        Assert.assertTrue(resp.getBody().getCode() == ResultType.SUCCESS.getCode());

        Result<Integer> result = JSON.parseObject(resp.getBody().toString(), new TypeReference<Result<Integer>>() {
        }.getType());
        System.out.println("new app: " + result.getData());

        // 校验配置初始值
        AppConfigVO configVO = getAppConfigById(result.getData());
        Assert.assertTrue(configVO != null);
        Assert.assertTrue(configVO.getAggrAnalyzeEnable().equals(AdminConstant.YES));
        Assert.assertTrue(configVO.getAnalyzeResponseEnable().equals(AdminConstant.YES));
        Assert.assertTrue(configVO.getDslAnalyzeEnable().equals(AdminConstant.NO));
        Assert.assertTrue(configVO.getIsSourceSeparated().equals(AdminConstant.NO));

        appDTO = getNewApp();
        resp = restTemplate.exchange(baseUrl + URL_BASE_APP + "/add", HttpMethod.POST,
            new HttpEntity<>(appDTO, headers), Result.class);
        System.out.println(resp.getBody().toString());
        Assert.assertTrue(resp.getBody().getCode() == ResultType.DUPLICATION.getCode());

        resp = restTemplate.exchange(baseUrl + URL_BASE_APP + "/delete?appId=" + result.getData(), HttpMethod.DELETE,
            new HttpEntity<>(appDTO, headers), Result.class);
        System.out.println(resp.getBody().toString());
        Assert.assertTrue(resp.getBody().getCode() == ResultType.SUCCESS.getCode());

    }

    @Test
    public void update() throws Exception {
        AppDTO updateParam = new AppDTO();
        updateParam.setId(-100);
        ResponseEntity<Result> resp = restTemplate.exchange(baseUrl + URL_BASE_APP + "/update", HttpMethod.PUT,
            new HttpEntity<>(updateParam, headers), Result.class);
        System.out.println(resp.getBody().toString());
        Assert.assertTrue(resp.getBody().getCode() == ResultType.NOT_EXIST.getCode());

        updateParam = new AppDTO();
        updateParam.setId(1133);

        // 可以修改的
        updateParam.setName("admin_ut_勿删");
        updateParam.setIsRoot(AdminConstant.YES);
        updateParam.setDepartmentId("1");
        updateParam.setDepartment("平台技术部-公共平台-订单系统");
        updateParam.setVerifyCode("123456");
        updateParam.setResponsible("zhonghua");
        updateParam.setSearchType(AppSearchTypeEnum.CLUSTER.getCode());
        updateParam.setCluster("elk-test");
        updateParam.setQueryThreshold(1000);
        updateParam.setMemo("updated");

        resp = restTemplate.exchange(baseUrl + URL_BASE_APP + "/update", HttpMethod.PUT,
            new HttpEntity<>(updateParam, headers), Result.class);
        System.out.println(resp.getBody().toString());
        Assert.assertTrue(resp.getBody().getCode() == ResultType.SUCCESS.getCode());

        AppVO appVO = getAppById(updateParam.getId());

        Assert.assertTrue(appVO.getName().equals(updateParam.getName()));
        Assert.assertTrue(appVO.getIsRoot().equals(updateParam.getIsRoot()));
        Assert.assertTrue(appVO.getDepartmentId().equals(updateParam.getDepartmentId()));
        Assert.assertTrue(appVO.getDepartment().equals(updateParam.getDepartment()));
        Assert.assertTrue(appVO.getVerifyCode().equals(updateParam.getVerifyCode()));
        Assert.assertTrue(appVO.getResponsible().equals(updateParam.getResponsible()));
        Assert.assertTrue(appVO.getSearchType().equals(updateParam.getSearchType()));
        Assert.assertTrue(appVO.getCluster().equals(updateParam.getCluster()));
        Assert.assertTrue(appVO.getQueryThreshold().equals(updateParam.getQueryThreshold()));
        Assert.assertTrue(appVO.getMemo().equals(updateParam.getMemo()));
    }

    @Test
    public void updateConfig() throws Exception {
        AppConfigDTO updateParam = new AppConfigDTO();
        updateParam.setAppId(1133);
        updateParam.setAnalyzeResponseEnable(AdminConstant.YES);
        updateParam.setDslAnalyzeEnable(AdminConstant.YES);
        updateParam.setAggrAnalyzeEnable(AdminConstant.YES);
        updateParam.setIsSourceSeparated(AdminConstant.NO);
        ResponseEntity<Result> resp = restTemplate.exchange(baseUrl + URL_BASE_APP + "/config/update", HttpMethod.PUT,
            new HttpEntity<>(updateParam, headers), Result.class);
        System.out.println(resp.getBody().toString());
        Assert.assertTrue(resp.getBody().getCode() == ResultType.SUCCESS.getCode());

        AppConfigVO configVO = getAppConfigById(1133);
        Assert.assertTrue(configVO.getAggrAnalyzeEnable().equals(updateParam.getAggrAnalyzeEnable()));
        Assert.assertTrue(configVO.getAnalyzeResponseEnable().equals(updateParam.getAnalyzeResponseEnable()));
        Assert.assertTrue(configVO.getDslAnalyzeEnable().equals(updateParam.getDslAnalyzeEnable()));
        Assert.assertTrue(configVO.getIsSourceSeparated().equals(updateParam.getIsSourceSeparated()));
    }

    @Test
    public void getAuth() throws Exception {

    }

    @Test
    public void addAuth() throws Exception {

    }

    @Test
    public void updateAuth() throws Exception {

    }

    @Test
    public void deleteAuth() throws Exception {

    }

    private AppDTO getNewApp() {
        AppDTO appDTO = new AppDTO();
        appDTO.setName("AdminOp-UT-Test");
        appDTO.setDepartmentId("1");
        appDTO.setDepartment("平台技术部-公共平台-订单系统");
        appDTO.setResponsible("zhagnhezhen");
        appDTO.setMemo("单测");
        return appDTO;
    }

    private AppVO getAppById(int appId) {
        ResponseEntity<Result> resp = restTemplate.getForEntity(baseUrl + URL_BASE_APP + "/get?appId=" + appId,
            Result.class);
        Result<AppVO> result = JSON.parseObject(resp.getBody().toString(), new TypeReference<Result<AppVO>>() {
        }.getType());
        return result.getData();
    }

    private AppConfigVO getAppConfigById(int appId) {
        ResponseEntity<Result> resp = restTemplate.getForEntity(baseUrl + URL_BASE_APP + "/config/get?appId=" + appId,
            Result.class);
        Result<AppConfigVO> result = JSON.parseObject(resp.getBody().toString(),
            new TypeReference<Result<AppConfigVO>>() {
            }.getType());
        return result.getData();
    }

}