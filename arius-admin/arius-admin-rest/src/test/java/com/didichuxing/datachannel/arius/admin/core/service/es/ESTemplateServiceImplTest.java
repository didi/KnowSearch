package com.didichuxing.datachannel.arius.admin.core.service.es;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didiglobal.logi.elasticsearch.client.response.setting.common.MappingConfig;
import com.didiglobal.logi.elasticsearch.client.response.setting.template.MultiTemplatesConfig;
import com.didiglobal.logi.elasticsearch.client.response.setting.template.TemplateConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Transactional
@Rollback
public class ESTemplateServiceImplTest extends AriusAdminApplicationTests {

    @Autowired
    private ESTemplateService esTemplateService;

    @Test
    void syncCreateTest() {
        // 正常的创建
        String clusterName = "cjm_6.6.2_test";
        String templateName = "test_template_1";
        String expression = "test_template_1*";
        String rack = "*";
        Integer shard = 1;
        Integer shardRouting = 1;
        try {
            // 正常创建
            boolean ret = esTemplateService.syncCreate(clusterName, templateName, expression, rack, shard, null, 1);
            Assertions.assertTrue(ret);
            // 不存在的集群
            ret = esTemplateService.syncCreate("testtest", templateName, expression, rack, shard, null, 1);
            Assertions.assertFalse(ret);
        } catch (ESOperateException e) {
            e.printStackTrace();
        }
    }

    @Test
    void syncDeleteTest() {
        String clusterName = "cjm_6.6.2_test";
        String templateName = "test_template_1";
        boolean ret = false;
        try {
            // 正常删除
            ret = esTemplateService.syncDelete(clusterName, templateName, 1);
            Assertions.assertTrue(ret);
        } catch (ESOperateException e) {
            e.printStackTrace();
        }
        // 集群不存在
        try {
            esTemplateService.syncDelete("testtest", templateName, 1);
        } catch (ESOperateException e) {
            e.printStackTrace();
        }
        // 模版不存在
        try {
            esTemplateService.syncDelete(clusterName, "testoooppp", 1);
        } catch (ESOperateException e) {
            e.printStackTrace();
        }
    }

    @Test
    void syncUpdateRackAndShard() {
        String clusterName = "cjm_6.6.2_test";
        String templateName = "test_template_1";
        String rack = "r1";
        Integer shard = 1;
        Integer shardRouting = 1;
        boolean ret = false;
        try {
            // 正常的更新
             ret = esTemplateService.syncUpdateRackAndShard(clusterName, templateName, rack, shard, null, 1);
            Assertions.assertTrue(ret);
        } catch (ESOperateException e) {
            e.printStackTrace();
        }
        try {
            // 集群不存在
            ret = esTemplateService.syncUpdateRackAndShard("ggggg", templateName, rack, shard, null, 1);
        } catch (ESOperateException e) {
            e.printStackTrace();
        }
        try {
            // 模版不存在
            ret = esTemplateService.syncUpdateRackAndShard(clusterName, "testtest", rack, shard, null, 1);
        } catch (ESOperateException e) {
            e.printStackTrace();
        }
    }

    @Test
    void syncUpdateExpression() {
        String clusterName = "cjm_6.6.2_test";
        String templateName = "test_template_1";
        String expression = "test_template_1*";
        boolean ret = false;
        try {
            // 正常的更新
            ret = esTemplateService.syncUpdateExpression(clusterName, templateName, expression, 1);
            Assertions.assertTrue(ret);
        } catch (ESOperateException e) {
            e.printStackTrace();
        }
        try {
            // 集群不存在
            ret = esTemplateService.syncUpdateExpression("testtest", templateName, expression, 1);
        } catch (ESOperateException e) {
            e.printStackTrace();
        }
        try {
            // 模版不存在
            ret = esTemplateService.syncUpdateExpression(clusterName, "testtest", expression, 1);
        } catch (ESOperateException e) {
            e.printStackTrace();
        }
    }

    @Test
    void syncUpdateShardNum() {
        String clusterName = "cjm_6.6.2_test";
        String templateName = "test_template_1";
        Integer shard = 1;
        boolean ret;
        try {
            // 正常的更新
            ret = esTemplateService.syncUpdateShardNum(clusterName, templateName, shard, 1);
            Assertions.assertTrue(ret);
        } catch (ESOperateException e) {
            e.printStackTrace();
        }
        try {
            // 集群不存在
            ret = esTemplateService.syncUpdateShardNum("testtest", templateName, shard, 1);
        } catch (ESOperateException e) {
            e.printStackTrace();
        }
        try {
            // 模版不存在
            ret = esTemplateService.syncUpdateShardNum(clusterName, "testtest", shard, 1);
        } catch (ESOperateException e) {
            e.printStackTrace();
        }
    }

    @Test
    void syncUpsertSetting() {
        String clusterName = "cjm_6.6.2_test";
        String templateName = "test_template_1";
        Map<String, String> setting = new HashMap<>();
        setting.put("index.number_of_shards", "2");
        boolean ret;
        try {
            // 正常的更新
            ret = esTemplateService.syncUpsertSetting(clusterName, templateName, setting, 1);
            Assertions.assertTrue(ret);

        } catch (ESOperateException e) {
            e.printStackTrace();
        }
        try {
            // 集群不存在
            setting.put("index.number_of_shards", "3");
            ret = esTemplateService.syncUpsertSetting("testtest", templateName, setting, 1);
        } catch (ESOperateException e) {
            e.printStackTrace();
        }
        try {
            // 模版不存在
            setting.put("index.number_of_shards", "1");
            ret = esTemplateService.syncUpsertSetting(clusterName, "testtest", setting, 1);
        } catch (ESOperateException e) {
            e.printStackTrace();
        }
        try {
            // setting不存在（包含不存在的setting）
            setting.put("index.testtest", "gggg");
            ret = esTemplateService.syncUpsertSetting(clusterName, "testtest", setting, 1);
        } catch (ESOperateException e) {
            e.printStackTrace();
        }
    }

    @Test
    void syncCopyMappingAndAlias() {
        String srcCluster = "cjm_6.6.2_test";
        String srcTemplateName = "test_template_1";
        String tgtCluster = "test_7.6.2_cjm";
        String tgtTemplateName = "tgt_test_template_1";
        boolean ret;
        try {
            // 正常的拷贝
            ret = esTemplateService.syncCopyMappingAndAlias(srcCluster, srcTemplateName, tgtCluster, tgtTemplateName, 1);
            Assertions.assertTrue(ret);
        } catch (ESOperateException e) {
            e.printStackTrace();
        }
        try {
            // 源集群不存在
            ret = esTemplateService.syncCopyMappingAndAlias("testest", srcTemplateName, tgtCluster, tgtTemplateName, 1);
        } catch (ESOperateException e) {
            e.printStackTrace();
        }
        try {
            // 目标集群不存在
            ret = esTemplateService.syncCopyMappingAndAlias(srcCluster, srcTemplateName, "testtest", tgtTemplateName, 1);
        } catch (ESOperateException e) {
            e.printStackTrace();
        }
    }

    @Test
    void syncUpdateTemplateConfig() {
        String clusterName = "cjm_6.6.2_test";
        String templateName = "test_template_1";
        TemplateConfig templateConfig = new TemplateConfig();
        templateConfig.setSettings("index.number_of_shards", "3");
        templateConfig.setTemplate(templateName);
        boolean ret;
        try {
            // 正常的更新
            ret = esTemplateService.syncUpdateTemplateConfig(clusterName, templateName, templateConfig, 1);
            Assertions.assertTrue(ret);

        } catch (ESOperateException e) {
            e.printStackTrace();
        }
        try {
            // 集群不存在
            ret = esTemplateService.syncUpdateTemplateConfig("testtest", templateName, templateConfig, 1);
        } catch (ESOperateException e) {
            e.printStackTrace();
        }
        try {
            // 模版不存在
            ret = esTemplateService.syncUpdateTemplateConfig(clusterName, "testtest", templateConfig, 1);
        } catch (ESOperateException e) {
            e.printStackTrace();
        }
        try {
            // 修改不存在的配置
            templateConfig.setSettings("index.testetest", "gggg");
            ret = esTemplateService.syncUpdateTemplateConfig(clusterName, templateName, templateConfig, 1);
        } catch (ESOperateException e) {
            e.printStackTrace();
        }
    }

    @Test
    void syncGetTemplateConfig() {
        String clusterName = "cjm_6.6.2_test";
        String templateName = "test_template_1";
        TemplateConfig templateConfig = esTemplateService.syncGetTemplateConfig(clusterName, templateName);
        Assertions.assertNotNull(templateConfig);
        // 不存在的集群
        templateConfig = esTemplateService.syncGetTemplateConfig("testtest", templateName);
        Assertions.assertNull(templateConfig);
        // 不存在的模版
        templateConfig = esTemplateService.syncGetTemplateConfig(clusterName, "ggggg");
        Assertions.assertNull(templateConfig);
    }

    @Test
    void syncGetMappingsByClusterName() {
        String clusterName = "cjm_6.6.2_test";
        String templateName = "test_template_1";
        MappingConfig mappingConfig = esTemplateService.syncGetMappingsByClusterName(clusterName, templateName);
        Assertions.assertNotNull(mappingConfig);
        // 不存在的集群
        mappingConfig = esTemplateService.syncGetMappingsByClusterName("testtest", templateName);
        Assertions.assertNull(mappingConfig);
        // 不存在的模版
        mappingConfig = esTemplateService.syncGetMappingsByClusterName(clusterName, "ggggg");
        Assertions.assertNull(mappingConfig);
    }

    @Test
    void syncGetTemplates() {
        String clusterName = "cjm_6.6.2_test";
        String templateName = "test_template_1";
        MultiTemplatesConfig multiTemplatesConfig = esTemplateService.syncGetTemplates(clusterName, templateName);
        Assertions.assertNotNull(multiTemplatesConfig);
        // 不存在的集群
        multiTemplatesConfig = esTemplateService.syncGetTemplates("testtest", templateName);
        Assertions.assertNull(multiTemplatesConfig);
        // 不存在的模版
        multiTemplatesConfig = esTemplateService.syncGetTemplates(clusterName, "ggggg");
        Assertions.assertNull(multiTemplatesConfig);
    }

    @Test
    void syncGetAllTemplates() {
        String clusterName = "cjm_6.6.2_test";
        List<String> clusterList = new ArrayList<>();
        clusterList.add(clusterName);
        Map<String, TemplateConfig> stringTemplateConfigMap = esTemplateService.syncGetAllTemplates(clusterList);
        Assertions.assertFalse(stringTemplateConfigMap.isEmpty());
        // 包含不存在的集群，全都返回null
        clusterList.add("testtest");
        stringTemplateConfigMap = esTemplateService.syncGetAllTemplates(clusterList);
        Assertions.assertNull(stringTemplateConfigMap);
    }

    @Test
    void syncUpdateName() {
        String clusterName = "cjm_6.6.2_test";
        String srcTemplateName = "test_template_1";
        String tgtTemplateName = "tgt_test_template_1";
        boolean ret;
        try {
            ret = esTemplateService.syncUpdateName(clusterName, srcTemplateName, tgtTemplateName, 1);
            Assertions.assertTrue(ret);
        } catch (ESOperateException e) {
            e.printStackTrace();
        }
        try {
            // 集群不存在
            ret = esTemplateService.syncUpdateName("testtest", srcTemplateName, tgtTemplateName, 1);
        } catch (ESOperateException e) {
            e.printStackTrace();
        }
        try {
            // 源模版不存在
            ret = esTemplateService.syncUpdateName(clusterName, "testtest", tgtTemplateName, 1);
        } catch (ESOperateException e) {
            e.printStackTrace();
        }
    }

    @Test
    void syncCheckTemplateConfig() {
        String clusterName = "cjm_6.6.2_test";
        String templateName = "test_template_1";
        TemplateConfig templateConfig = new TemplateConfig();
        templateConfig.setSettings("index.number_of_shards", "1");
        boolean ret;
        try {
            ret = esTemplateService.syncCheckTemplateConfig(clusterName, templateName, templateConfig, 1);
            Assertions.assertTrue(ret);
        } catch (ESOperateException e) {
            e.printStackTrace();
        }
        try {
            // 集群不存在
            ret = esTemplateService.syncCheckTemplateConfig("testtest", templateName, templateConfig, 1);
        } catch (ESOperateException e) {
            e.printStackTrace();
        }
        try {
            // 模版不存在
            ret = esTemplateService.syncCheckTemplateConfig(clusterName, "testtest", templateConfig, 1);
        } catch (ESOperateException e) {
            e.printStackTrace();
        }
        try {
            // 包含错误的配置
            templateConfig.setSettings("testtest", "gggg");
            ret = esTemplateService.syncCheckTemplateConfig(clusterName, templateName, templateConfig, 1);
        } catch (ESOperateException e) {
            e.printStackTrace();
        }
    }

    @Test
    void syncGetTemplateNum() {
        String clusterName = "cjm_6.6.2_test";
        long num = esTemplateService.syncGetTemplateNum(clusterName);
        Assertions.assertTrue(num > 0);
        // 不存在的集群
        num = esTemplateService.syncGetTemplateNum("testtest");
        Assertions.assertEquals(0, num);
    }

}
