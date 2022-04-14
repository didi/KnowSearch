package com.didichuxing.datachannel.arius.admin.base;

import com.didichuxing.datachannel.arius.admin.source.TemplateInfoSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;

/**
 * @author cjm
 */
public class BaseTemplateInfoTest extends BaseLogicClusterInfoTest {

    protected static TemplateInfoSource.TemplateInfo templateInfo;

    /**
     * 在当前类的所有测试方法之前执行
     */
    @BeforeAll
    public static void preHandle() throws IOException {
        BaseLogicClusterInfoTest.preHandle();
        // 创建模版
        templateInfo = TemplateInfoSource.createTemplate(phyClusterInfo.getPhyClusterName(), logicClusterInfo.getLogicClusterName(), logicClusterInfo.getLogicClusterId());
    }

    /**
     * 在当前类中的所有测试方法之后执行
     */
    @AfterAll
    public static void afterCompletion() throws IOException {
        // 删除模版
        TemplateInfoSource.deleteTemplate(templateInfo.getLogicTemplateId());
        BaseLogicClusterInfoTest.afterCompletion();
    }
}
