package com.didichuxing.datachannel.arius.admin.v3.op.template;

import com.didichuxing.datachannel.arius.admin.base.BaseTemplateInfoTest;
import com.didichuxing.datachannel.arius.admin.client.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.TemplateConditionDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.TemplateSettingDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.BaseTemplateVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.ConsoleTemplateDetailVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.ConsoleTemplateVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.TemplateSettingVO;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.method.v3.op.cluster.phy.ESPhyClusterTemplateSrvControllerMethod;
import com.didichuxing.datachannel.arius.admin.method.v3.op.template.TemplateLogicV3ControllerMethod;
import com.didichuxing.datachannel.arius.admin.source.CustomDataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author cjm
 */
public class TemplateLogicV3Test extends BaseTemplateInfoTest {

    @Test
    public void listTemplateLogicNamesTest() throws IOException {
        Result<List<String>> result = TemplateLogicV3ControllerMethod.listTemplateLogicNames();
        Assertions.assertTrue(result.success());
        Assertions.assertTrue(result.getData().contains(templateInfo.getTemplateName()));
    }

    @Test
    public void pageGetConsoleTemplateVOSTest() throws IOException {
        TemplateConditionDTO dto = CustomDataSource.getTemplateConditionDTO();
        dto.setName(templateInfo.getTemplateName());
        PaginationResult<ConsoleTemplateVO> result = TemplateLogicV3ControllerMethod.pageGetConsoleTemplateVOS(dto);
        Set<String> templateNameSet = result.getData().getBizData().stream().map(BaseTemplateVO::getName).collect(Collectors.toSet());
        Assertions.assertTrue(templateNameSet.contains(templateInfo.getTemplateName()));
    }

    @Test
    public void checkTemplateValidForCreateTest() throws IOException {
        // 包含大写，不合法
        String templateName = CustomDataSource.getRandomString(6).toUpperCase();
        Result<Void> result = TemplateLogicV3ControllerMethod.checkTemplateValidForCreate(templateName);
        Assertions.assertTrue(result.failed());
        // 合法的
        String templateName2 = CustomDataSource.getRandomString(6).toLowerCase();
        Result<Void> result2 = TemplateLogicV3ControllerMethod.checkTemplateValidForCreate(templateName2);
        Assertions.assertTrue(result2.success());
    }

    @Test
    public void checkTemplateEditMappingTest() throws IOException {
        // 先开启Mapping设置索引模版服务
        int templateMappingTemplatesrvId = TemplateServiceEnum.TEMPLATE_MAPPING.getCode();
        Result<Boolean> result1 =
                ESPhyClusterTemplateSrvControllerMethod.addTemplateSrvId(phyClusterInfo.getPhyClusterName(), String.valueOf(templateMappingTemplatesrvId));
        Assertions.assertTrue(result1.success());
        // 先关闭Mapping设置索引模版服务
        Result<Boolean> result2 = ESPhyClusterTemplateSrvControllerMethod
                .delTemplateSrvId(phyClusterInfo.getPhyClusterName(), String.valueOf(TemplateServiceEnum.TEMPLATE_MAPPING.getCode()));
        Assertions.assertTrue(result2.success());

        // 由于之前创建集群的时候，不一定开启Mapping设置模版服务
        Result<Boolean> result3 = TemplateLogicV3ControllerMethod.checkTemplateEditMapping(templateInfo.getLogicTemplateId());
        Assertions.assertTrue(result3.failed());

        // 未开启，则先开启Mapping设置索引模版服务
        int templateSrvId = TemplateServiceEnum.TEMPLATE_MAPPING.getCode();
        Result<Boolean> result4 = ESPhyClusterTemplateSrvControllerMethod.addTemplateSrvId(phyClusterInfo.getPhyClusterName(), String.valueOf(templateSrvId));
        Assertions.assertTrue(result4.success());

        Result<Boolean> result5 = TemplateLogicV3ControllerMethod.checkTemplateEditMapping(templateInfo.getLogicTemplateId());
        Assertions.assertTrue(result5.success());
    }

    @Test
    public void checkTemplateEditServiceTest() throws IOException {
        // 先开启Mapping设置索引模版服务
        int templateMappingTemplatesrvId = TemplateServiceEnum.TEMPLATE_MAPPING.getCode();
        Result<Boolean> result1 =
                ESPhyClusterTemplateSrvControllerMethod.addTemplateSrvId(phyClusterInfo.getPhyClusterName(), String.valueOf(templateMappingTemplatesrvId));
        Assertions.assertTrue(result1.success());
        Result<Boolean> result2 =
                TemplateLogicV3ControllerMethod.checkTemplateEditService(templateInfo.getLogicTemplateId(), templateMappingTemplatesrvId);
        Assertions.assertTrue(result2.success());
        // 再关闭Mapping设置索引模版服务
        Result<Boolean> result3 = ESPhyClusterTemplateSrvControllerMethod
                .delTemplateSrvId(phyClusterInfo.getPhyClusterName(), String.valueOf(templateMappingTemplatesrvId));
        Assertions.assertTrue(result3.success());
        Result<Boolean> result4 =
                TemplateLogicV3ControllerMethod.checkTemplateEditService(templateInfo.getLogicTemplateId(), templateMappingTemplatesrvId);
        Assertions.assertTrue(result4.failed());
    }

    @Test
    public void switchRolloverStatusTest() throws IOException {
        Boolean disable = templateInfo.getConsoleTemplateVO().getDisableIndexRollover();
        int newStatus = (disable ? 0 : 1) ^ 1;
        Result<Void> result = TemplateLogicV3ControllerMethod.switchRolloverStatus(templateInfo.getLogicTemplateId(), newStatus);
        Assertions.assertTrue(result.success());
        Result<ConsoleTemplateDetailVO> result2 = TemplateLogicV3ControllerMethod.getConsoleTemplateDetail(templateInfo.getLogicTemplateId());
        Assertions.assertTrue(result2.success());
        Assertions.assertNotEquals(result2.getData().getDisableIndexRollover(), disable);
    }

    @Test
    public void customizeSettingTest() throws IOException {
        Result<TemplateSettingVO> result = TemplateLogicV3ControllerMethod.getTemplateSettings(templateInfo.getLogicTemplateId());
        Assertions.assertTrue(result.success());
        TemplateSettingDTO dto = new TemplateSettingDTO();
        dto.setAsyncTranslog(!result.getData().isAsyncTranslog());
        dto.setLogicId(templateInfo.getLogicTemplateId());
        Result<Void> result2 = TemplateLogicV3ControllerMethod.customizeSetting(dto);
        Assertions.assertTrue(result2.success());
        Result<TemplateSettingVO> result3 = TemplateLogicV3ControllerMethod.getTemplateSettings(templateInfo.getLogicTemplateId());
        Assertions.assertEquals(result3.getData().isAsyncTranslog(), dto.isAsyncTranslog());
    }

    @Test
    public void getTemplateSettingsTest() throws IOException {
        Result<TemplateSettingVO> result = TemplateLogicV3ControllerMethod.getTemplateSettings(templateInfo.getLogicTemplateId());
        Assertions.assertTrue(result.success());
    }
}
