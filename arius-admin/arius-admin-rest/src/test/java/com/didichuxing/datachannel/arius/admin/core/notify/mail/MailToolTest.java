package com.didichuxing.datachannel.arius.admin.core.notify.mail;

import com.didichuxing.datachannel.arius.admin.core.notify.MailTool;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ESClusterLogicService;
import org.springframework.beans.factory.annotation.Autowired;

import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.TemplateLogicService;
import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;

/**
 * @author d06679
 * @date 2019-09-09
 */
public class MailToolTest extends AriusAdminApplicationTests {

    @Autowired
    private MailTool mailTool;

    @Autowired
    private TemplateLogicService templateLogicService;

    @Autowired
    private ESClusterLogicService esClusterLogicService;

    @Autowired
    private AppService           appService;

//    @Test
//    public void testAppCreatedMail() {
//        App app = new App();
//        app.setId(1);
//        app.setName("test");
//        app.setVerifyCode("123456");
//        app.setResponsible("zhanghezhen");
//
//        mailTool.sendMailContent(new AppCreatedContent(Lists.newArrayList(app.getResponsible()), app));
//    }
//
//    @Test
//    public void testAriusUserOfflineTransferApp() {
//        App app = new App();
//        app.setId(1);
//        app.setName("test");
//        app.setVerifyCode("123456");
//        app.setResponsible("zhanghezhen");
//
//        mailTool.sendMailContent(
//            new AriusUserOfflineTransferAppNotifyInfo("zhanghezhen", Lists.newArrayList(app), "zhanghezhen"));
//    }
//
//    @Test
//    public void testAriusUserOfflineTransferTemplateAndResource() {
//        List<IndexTemplateLogic> templateLogics = templateLogicService.acquireAllLogicTemplates();
//        Collections.shuffle(templateLogics);
//
//        List<ESClusterLogic> esClusterLogics = esClusterLogicService.getDataCenterLogicClusters();
//        Collections.shuffle( esClusterLogics );
//
//        mailTool.sendMailContent(new AriusUserOfflineTransferTemplateAndResourceNotifyInfo("zhanghezhen",
//            templateLogics.subList(0, 2), esClusterLogics.subList(0, 2), "zhanghezhen"));
//    }
//
//    @Test
//    public void testImportantTemplateAuth() {
//        mailTool.sendMailContent(
//            new ImportantTemplateAuthNotifyInfo(Lists.newArrayList("zhanghezhen"), "templatename", "console"));
//    }
//
//    @Test
//    public void testQuotaCtlOnline() {
//        List<IndexTemplateLogic> templateLogics = templateLogicService.acquireAllLogicTemplates();
//        Collections.shuffle(templateLogics);
//
//        templateLogics = templateLogics.subList(0, 10);
//
//        Map<Integer, LogicTemplateQuotaUsage> logicId2LogicTemplateQuotaUsageMap = Maps.newHashMap();
//        for (IndexTemplateLogic templateLogic : templateLogics) {
//            LogicTemplateQuotaUsage usage = new LogicTemplateQuotaUsage();
//            usage.setLogicId(templateLogic.getId());
//            usage.setTemplate(templateLogic.getName());
//            usage.setActualDiskG(1000.0);
//            usage.setActualCpuCount(5.0);
//            usage.setQuotaDiskG(2000.0);
//            usage.setQuotaCpuCount(10.0);
//            logicId2LogicTemplateQuotaUsageMap.put(templateLogic.getId(), usage);
//        }
//
//        mailTool.sendMailContent(
//            new QuotaCtlOnlineContent(templateLogics, logicId2LogicTemplateQuotaUsageMap, "zhanghezhen"));
//    }

}