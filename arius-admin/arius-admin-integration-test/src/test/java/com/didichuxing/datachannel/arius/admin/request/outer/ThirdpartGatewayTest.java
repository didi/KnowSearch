package com.didichuxing.datachannel.arius.admin.request.outer;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.didichuxing.datachannel.arius.admin.base.BaseContextTest;
import com.didichuxing.datachannel.arius.admin.common.bean.common.GatewayHeartbeat;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.ScrollDslTemplateRequest;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.ScrollDslTemplateResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.GatewayAppVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.GatewayTemplateDeployInfoVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.GatewayTemplatePhysicalVO;
import com.didichuxing.datachannel.arius.admin.util.CompareUtil;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author wuxuan
 * @Date 2022/6/16
 */
public class ThirdpartGatewayTest extends BaseContextTest {

    @Test
    public void testHeartbeat() throws IOException {
        GatewayHeartbeat heartbeat = new GatewayHeartbeat();
        heartbeat.setClusterName("admin");
        heartbeat.setPort(8080);
        heartbeat.setHostName("admin");

        Result<Void> result = ThirdpartGatewayControllerMethod.heartbeat(heartbeat);
        Assert.assertTrue(result.success());
    }

    @Test
    public void testHeartbeatByClusterName() throws IOException {
        Result<Integer> result = ThirdpartGatewayControllerMethod.heartbeat("normal");

        Assert.assertTrue(result.getData()>=0);
        Assert.assertTrue(result.success());
    }

    @Test
    public void testGetGatewayAliveNodeNames() throws IOException {
        Result<List<String>> result = ThirdpartGatewayControllerMethod.getGatewayAliveNodeNames();
        Assert.assertTrue(result.getData()!=null);
        Assert.assertTrue(result.success());
    }

    @Test
    public void testListApp() throws IOException {
        //调用main中的request方法去访问接口
        Result<List<GatewayAppVO>> result = ThirdpartGatewayControllerMethod.listApp();
        Assert.assertTrue(result.success());
        String templateJsonFile = "src/main/resources/template.thirdpart.gateway/gatewayappvo.json";
        for (GatewayAppVO gatewayAppVO:result.getData()){
            String responseJsonString = CompareUtil.serialize(gatewayAppVO);
            Assert.assertTrue(CompareUtil.compareJson(templateJsonFile,responseJsonString));
        }
    }

    @Test
    public void testGetTemplateMap() throws IOException {
        Result<Map<String, GatewayTemplatePhysicalVO>> result = ThirdpartGatewayControllerMethod.getTemplateMap("lcx-0401");
        Assert.assertTrue(result.success());
        String templateJsonFile = "src/main/resources/template.thirdpart.gateway/gatewaytemplatephysicalvo.json";
        String responseJsonString = JSONObject.toJSONString(result.getData());
        Assert.assertTrue(CompareUtil.compareJson(templateJsonFile,responseJsonString));
    }

    @Test
    public void testListDeployInfo() throws IOException {
        Result<Map<String, GatewayTemplateDeployInfoVO>> result = ThirdpartGatewayControllerMethod.listDeployInfo("cn");
        Assert.assertTrue(result.success());
        String templateJsonFile = "src/main/resources/template.thirdpart.gateway/gatewaytemplatedeployinfovo.json";
        String responseJsonString = JSONObject.toJSONString(result.getData(), SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue);
        Assert.assertTrue(CompareUtil.compareJson(templateJsonFile,responseJsonString));
    }

    @Test
    public void testScrollSearchDslTemplate() throws IOException {
        ScrollDslTemplateRequest scrollDslTemplateRequest = new ScrollDslTemplateRequest();
        scrollDslTemplateRequest.setDslTemplateVersion("7.6.1");
        scrollDslTemplateRequest.setScrollSize(23L);
        scrollDslTemplateRequest.setLastModifyTime(System.currentTimeMillis());
        Result<ScrollDslTemplateResponse> result = ThirdpartGatewayControllerMethod.scrollSearchDslTemplate(scrollDslTemplateRequest);
        Assert.assertTrue(result.success());

        String templateJsonFile = "src/main/resources/template.thirdpart.gateway/scrollDslTemplateResponse.json";
        String responseJsonString = CompareUtil.serialize(result.getData());
        Assert.assertTrue(CompareUtil.compareJson(templateJsonFile,responseJsonString));
    }

}
