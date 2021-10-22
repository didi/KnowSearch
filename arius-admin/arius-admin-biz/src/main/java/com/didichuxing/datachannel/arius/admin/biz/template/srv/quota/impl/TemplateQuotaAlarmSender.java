package com.didichuxing.datachannel.arius.admin.biz.template.srv.quota.impl;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.quota.TemplateQuotaManager;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.App;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.quota.LogicTemplateQuotaUsage;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateNotifyESPO;
import com.didichuxing.datachannel.arius.admin.common.event.quota.TemplateQuotaEvent;
import com.didichuxing.datachannel.arius.admin.common.util.AriusDateUtils;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.core.notify.MailTool;
import com.didichuxing.datachannel.arius.admin.core.notify.NotifyTaskTypeEnum;
import com.didichuxing.datachannel.arius.admin.core.notify.NotifyTool;
import com.didichuxing.datachannel.arius.admin.core.notify.info.template.TemplateQuotaUsageAlarmNotifyInfo;
import com.didichuxing.datachannel.arius.admin.core.notify.service.NotifyService;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.TemplateLogicService;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.template.TemplateNotifyDAO;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author d06679
 * @date 2019/5/24
 */
@Component
public class TemplateQuotaAlarmSender implements ApplicationListener<TemplateQuotaEvent> {

    private static final ILog                  LOGGER                 = LogFactory
        .getLog(TemplateQuotaAlarmSender.class);

    @Autowired
    private TemplateLogicService               templateLogicService;

    @Autowired
    private AppService                         appService;

    @Autowired
    private MailTool                           mailTool;

    @Autowired
    private TemplateQuotaManager               templateQuotaManager;

    @Value("${admin.url.console}")
    private String                             adminUrlConsole;

    @Autowired
    protected TemplateNotifyDAO                templateNotifyDAO;

    @Autowired
    private NotifyService                      notifyService;

    @Autowired
    private NotifyTool                         notifyTool;

    private final static int                   RATION_80              = 80;
    private final static int                   RATIO_85               = 85;
    private final static int                   RATIO_90               = 90;
    private final static int                   RATIO_95               = 95;

    private final static Map<Integer, Integer> RATION_NOTIFY_RULE_MAP = new HashMap<>();

    static {
        RATION_NOTIFY_RULE_MAP.put(RATION_80, 1);
        RATION_NOTIFY_RULE_MAP.put(RATIO_85, 2);
        RATION_NOTIFY_RULE_MAP.put(RATIO_90, 3);
        RATION_NOTIFY_RULE_MAP.put(RATIO_95, 4);
    }

    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(TemplateQuotaEvent event) {
        if (null == event) {
            return;
        }

        if (!EnvUtil.isOnline() && !EnvUtil.isPre()) {
            return;
        }

        LogicTemplateQuotaUsage templateQuotaUsage = event.getTemplateQuotaUsage();
        IndexTemplateLogic templateLogic = templateLogicService.getLogicTemplateById(templateQuotaUsage.getLogicId());
        App app = appService.getAppById(templateLogic.getAppId());

        if (!templateQuotaManager.enableClt(templateLogic.getId())) {
            return;
        }

        // 防疲劳
        if (!canNotify(event, templateLogic, app)) {
            LOGGER.info("method=onApplicationEvent||logicId={}||msg=anti-fatigue", templateLogic.getId());
            return;
        }

        LOGGER.info("method=onApplicationEvent||templateName={}||templateQuotaUsage={}", templateLogic.getName(),
            templateQuotaUsage);

        notifyService.send(NotifyTaskTypeEnum.TEMPLATE_QUOTA_USAGE_ALARM_ERROR,
                TemplateQuotaUsageAlarmNotifyInfo.builder()
                        .app(app)
                        .templateLogic(templateLogic)
                        .templateQuotaUsage(templateQuotaUsage)
                        .ariusConsole(adminUrlConsole)
                        .ctlRange(templateQuotaManager.getCtlRange(templateQuotaUsage.getLogicId())).build(),
                        Lists.newArrayList(app.getResponsible().split(",")));

        Double usage = (templateQuotaUsage.getActualDiskG() / templateQuotaUsage.getQuotaDiskG()) * 100;
        if (usage > RATIO_95) {
            notifyTool.bySms(app.getResponsible(), genSmsContent(templateLogic, app));
        }
    }

    /**************************************** private method ****************************************************/
    //提醒的疲劳度控制
    //控制规则：一个模板一天之内超过80%最多提醒一次，超过85%最多提醒两次，超过90%最多提醒三次，超过95%最多提醒四次
    private boolean canNotify(TemplateQuotaEvent event, IndexTemplateLogic templateLogic, App app) {
        LogicTemplateQuotaUsage templateQuotaUsage = event.getTemplateQuotaUsage();

        Integer logicTemplateId = templateLogic.getId();
        String templateName = templateLogic.getName();
        String zeroDate = String.valueOf(AriusDateUtils.getZeroDate().getTime());

        Double diskUsage = templateQuotaUsage.getActualDiskG() / templateQuotaUsage.getQuotaDiskG();
        Double cpuUsage = templateQuotaUsage.getActualCpuCount() / templateQuotaUsage.getQuotaCpuCount();

        // Double usage = (diskUsage > cpuUsage ? diskUsage : cpuUsage) * 100;
        Double usage = diskUsage * 100;
        Integer usageRatio;

        if (usage >= RATIO_95) {
            usageRatio = RATIO_95;
        } else if (usage < RATIO_95 && usage >= RATIO_90) {
            usageRatio = RATIO_90;
        } else if (usage < RATIO_90 && usage >= RATIO_85) {
            usageRatio = RATIO_85;
        } else if (usage < RATIO_85 && usage >= RATION_80) {
            usageRatio = RATION_80;
        } else {
            return false;
        }

        List<TemplateNotifyESPO> templateNotifyESPOList = templateNotifyDAO.getByLogicTemplIdAndRate(logicTemplateId,
            zeroDate, usageRatio);

        if (!EnvUtil.isOnline()) {
            LOGGER.info("method=canNotify||templateName={}||templateNotifyESPOInES={}||templateQuotaUsage={}",
                templateLogic.getName(), JSON.toJSONString(templateNotifyESPOList), templateQuotaUsage);
        }

        if (CollectionUtils.isEmpty(templateNotifyESPOList)) {
            templateNotifyDAO.insertTemplateNotifyESPO(
                new TemplateNotifyESPO(logicTemplateId, app.getId(), templateName, zeroDate, usageRatio, 1));
            return true;
        } else {
            TemplateNotifyESPO templateNotifyESPO = templateNotifyESPOList.get(0);
            Integer ratio = templateNotifyESPO.getRate();
            Integer needNotifyNu = RATION_NOTIFY_RULE_MAP.get(ratio);
            if (null != needNotifyNu) {
                if (needNotifyNu.intValue() > templateNotifyESPO.getNotifyNu()) {
                    templateNotifyESPO.setNotifyNu(templateNotifyESPO.getNotifyNu() + 1);
                    templateNotifyDAO.insertTemplateNotifyESPO(templateNotifyESPO);
                    return true;
                }
            }
        }

        if (!EnvUtil.isOnline()) {
            LOGGER.info("method=canNotify||templateName={}||templateNotifyESPOInES={}||msg=notifyInfo too more!",
                templateLogic.getName(), JSON.toJSONString(templateNotifyESPOList));
        }

        return false;
    }

    private String genSmsContent(IndexTemplateLogic templateLogic, App app) {
        return "【Arius服务中心通知】你的appId[" + app.getId() + "]所负责的索引[" + templateLogic.getName() + "]资源利用率已经超过配额的" + RATIO_95
               + "%，请及时处理";
    }

}
