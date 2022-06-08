package com.didichuxing.datachannel.arius.admin.source;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ConsoleTemplateVO;
import com.didichuxing.datachannel.arius.admin.method.v3.op.template.TemplateLogicV3ControllerMethod;

import lombok.Data;

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
        return templateInfo;
    }

    public static void deleteTemplate(Integer logicTemplateId) throws IOException {
        Result<Void> result = TemplateLogicV3ControllerMethod.deleteTemplate(logicTemplateId);
        Assertions.assertTrue(result.success());
    }
}