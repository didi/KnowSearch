package com.didichuxing.datachannel.arius.admin.source;

import com.didichuxing.datachannel.arius.admin.biz.workorder.content.TemplateCreateContent;
import com.didichuxing.datachannel.arius.admin.client.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.TemplateConditionDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.workorder.WorkOrderDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.workorder.WorkOrderProcessDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.order.WorkOrderSubmittedVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.ConsoleTemplateVO;
import com.didichuxing.datachannel.arius.admin.client.constant.template.DataTypeEnum;
import com.didichuxing.datachannel.arius.admin.method.v3.normal.NormalOrderControllerMethod;
import com.didichuxing.datachannel.arius.admin.method.v3.op.cluster.phy.ESPhyClusterControllerMethod;
import com.didichuxing.datachannel.arius.admin.method.v3.op.template.TemplateLogicV3ControllerMethod;
import com.didichuxing.datachannel.arius.admin.source.CustomDataSource;
import lombok.Data;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.util.List;

/**
 * @author cjm
 */
public class TemplateInfoSource {

    @Data
    public static class TemplateInfo {
        private Integer logicTemplateId;
        private String templateName;
        private ConsoleTemplateVO consoleTemplateVO;
    }

    public static TemplateInfo createTemplate(String phyClusterName, String logicClusterName, Long logicClusterId) throws IOException {
        TemplateInfo templateInfo = new TemplateInfo();
        // 发起工单申请
        String type = "templateCreate";
        String templateName = CustomDataSource.getRandomString(8).toLowerCase();
        templateInfo.templateName = templateName;
        WorkOrderDTO workOrderDTO = CustomDataSource.getWorkOrderDTO(type);
        TemplateCreateContent templateCreateContent = new TemplateCreateContent();
        templateCreateContent.setName(templateName);
        templateCreateContent.setDataCenter("cn");
        templateCreateContent.setDataType(DataTypeEnum.LOG.getCode());
        templateCreateContent.setResponsible(CustomDataSource.operator);
        templateCreateContent.setDesc("testetsetestets");
        templateCreateContent.setExpireTime(-1);
        templateCreateContent.setCyclicalRoll(false);
        templateCreateContent.setDiskQuota(30d);
        templateCreateContent.setMapping("{\"name\":{\"type\":\"keyword\",\"index\":true,\"doc_values\":true}}");
        templateCreateContent.setResourceId(logicClusterId);
        templateCreateContent.setCluster(logicClusterName);
        templateCreateContent.setIdField("");
        templateCreateContent.setDateField("");
        templateCreateContent.setRoutingField("");
        templateCreateContent.setDateFieldFormat(null);
        templateCreateContent.setDisableIndexRollover(true);
        templateCreateContent.setCancelCopy(false);
        templateCreateContent.setAsyncTranslog(false);
        templateCreateContent.setCustomerAnalysis("");
        templateCreateContent.setDynamicTemplates("");
        workOrderDTO.setContentObj(templateCreateContent);
        Result<WorkOrderSubmittedVO> result = NormalOrderControllerMethod.submit(type, workOrderDTO);
        Assertions.assertTrue(result.success());

        // 通过工单
        Long orderId = result.getData().getId();
        WorkOrderProcessDTO processDTO = CustomDataSource.getWorkOrderProcessDTO(orderId);
        // 获取 rack 和 设置 rack
        Result<List<String>> result4 =
                ESPhyClusterControllerMethod.getValidRacksListByDiskSize(phyClusterName, logicClusterName, String.valueOf(templateCreateContent.getDiskQuota()));
        Assertions.assertTrue(result4.success());
        TemplateCreateContent templateCreateContent2 = new TemplateCreateContent();
        templateCreateContent2.setCluster(phyClusterName);
        templateCreateContent2.setRack(result4.getData().get(0));
        processDTO.setContentObj(templateCreateContent2);
        Result<Void> result2 = NormalOrderControllerMethod.process(orderId, processDTO);
        Assertions.assertTrue(result2.success());

        // 获取刚创建的 templateId，先分页获取 template，根据名字查询
        TemplateConditionDTO dto = CustomDataSource.getTemplateConditionDTO();
        dto.setName(templateName);
        PaginationResult<ConsoleTemplateVO> result3 = TemplateLogicV3ControllerMethod.pageGetConsoleTemplateVOS(dto);
        Assertions.assertTrue(result3.success());
        for(ConsoleTemplateVO consoleTemplateVO : result3.getData().getBizData()) {
            if(consoleTemplateVO.getName().equals(templateName)) {
                templateInfo.logicTemplateId = consoleTemplateVO.getId();
                templateInfo.consoleTemplateVO = consoleTemplateVO;
                break;
            }
        }
        return templateInfo;
    }

    public static void deleteTemplate(Integer logicTemplateId) throws IOException {
        Result<Void> result = TemplateLogicV3ControllerMethod.deleteTemplate(logicTemplateId);
        Assertions.assertTrue(result.success());
    }
}
