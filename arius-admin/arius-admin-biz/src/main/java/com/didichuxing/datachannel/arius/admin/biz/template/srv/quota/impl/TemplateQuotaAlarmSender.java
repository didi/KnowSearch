package com.didichuxing.datachannel.arius.admin.biz.template.srv.quota.impl;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.quota.TemplateQuotaManager;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.App;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.quota.LogicTemplateQuotaUsage;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateNotifyESPO;
import com.didichuxing.datachannel.arius.admin.common.event.quota.TemplateQuotaEvent;
import com.didichuxing.datachannel.arius.admin.common.util.AriusDateUtils;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.template.TemplateNotifyDAO;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.security.service.ProjectService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @author d06679
 * @date 2019/5/24
 */
@Component
@NoArgsConstructor
public class TemplateQuotaAlarmSender implements ApplicationListener<TemplateQuotaEvent> {

    private static final ILog                  LOGGER                 = LogFactory
        .getLog(TemplateQuotaAlarmSender.class);

    @Autowired
    private IndexTemplateService indexTemplateService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TemplateQuotaManager               templateQuotaManager;

    @Value("${admin.url.console}")
    private String                             adminUrlConsole;

    @Autowired
    protected TemplateNotifyDAO                templateNotifyDAO;

    private static final  int                   RATION_80              = 80;
    private static final  int                   RATIO_85               = 85;
    private static final  int                   RATIO_90               = 90;
    private static final  int                   RATIO_95               = 95;

    private static final Map<Integer, Integer> RATION_NOTIFY_RULE_MAP = new HashMap<>();

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
        IndexTemplate templateLogic = indexTemplateService.getLogicTemplateById(templateQuotaUsage.getLogicId());
      

        if (!templateQuotaManager.enableClt(templateLogic.getId())||!projectService.checkProjectExist(templateLogic.getProjectId())) {
            return;
        }

        // 防疲劳
        if (!canNotify(event, templateLogic, templateLogic.getProjectId())) {
            LOGGER.info("class=TemplateQuotaAlarmSender||method=onApplicationEvent||logicId={}||msg=anti-fatigue", templateLogic.getId());
            return;
        }

        LOGGER.info("class=TemplateQuotaAlarmSender||method=onApplicationEvent||templateName={}||templateQuotaUsage={}", templateLogic.getName(),
            templateQuotaUsage);

        Double usage = (templateQuotaUsage.getActualDiskG() / templateQuotaUsage.getQuotaDiskG()) * 100;
    }

    /**************************************** private method ****************************************************/
    //提醒的疲劳度控制
    //控制规则：一个模板一天之内超过80%最多提醒一次，超过85%最多提醒两次，超过90%最多提醒三次，超过95%最多提醒四次
    private boolean canNotify(TemplateQuotaEvent event, IndexTemplate templateLogic, Integer projectId ) {
        LogicTemplateQuotaUsage templateQuotaUsage = event.getTemplateQuotaUsage();

        Integer logicTemplateId = templateLogic.getId();
        String templateName = templateLogic.getName();
        String zeroDate = String.valueOf(AriusDateUtils.getZeroDate().getTime());

        Double diskUsage = templateQuotaUsage.getActualDiskG() / templateQuotaUsage.getQuotaDiskG();
        Double usage = diskUsage * 100;
        Integer usageRatio;

        if (usage >= RATIO_95) {
            usageRatio = RATIO_95;
        } else if (usage >= RATIO_90) {
            usageRatio = RATIO_90;
        } else if (usage >= RATIO_85) {
            usageRatio = RATIO_85;
        } else if (usage >= RATION_80) {
            usageRatio = RATION_80;
        } else {
            return false;
        }

        List<TemplateNotifyESPO> templateNotifyESPOList = templateNotifyDAO.getByLogicTemplIdAndRate(logicTemplateId,
            zeroDate, usageRatio);

        if (!EnvUtil.isOnline()) {
            LOGGER.info("class=TemplateQuotaAlarmSender||method=canNotify||templateName={}||templateNotifyESPOInES={}||templateQuotaUsage={}",
                templateLogic.getName(), JSON.toJSONString(templateNotifyESPOList), templateQuotaUsage);
        }

        if (CollectionUtils.isEmpty(templateNotifyESPOList)) {
            templateNotifyDAO.insertTemplateNotifyESPO(
                new TemplateNotifyESPO(logicTemplateId, projectId, templateName, zeroDate, usageRatio, 1));
            return true;
        } else {
            TemplateNotifyESPO templateNotifyESPO = templateNotifyESPOList.get(0);
            Integer ratio = templateNotifyESPO.getRate();
            Integer needNotifyNu = RATION_NOTIFY_RULE_MAP.get(ratio);
            if (null != needNotifyNu && needNotifyNu.intValue() > templateNotifyESPO.getNotifyNu()) {
                templateNotifyESPO.setNotifyNu(templateNotifyESPO.getNotifyNu() + 1);
                templateNotifyDAO.insertTemplateNotifyESPO(templateNotifyESPO);
                return true;
            }
        }

        if (!EnvUtil.isOnline()) {
            LOGGER.info("class=TemplateQuotaAlarmSender||method=canNotify||templateName={}||templateNotifyESPOInES={}||msg=notifyInfo too more!",
                templateLogic.getName(), JSON.toJSONString(templateNotifyESPOList));
        }

        return false;
    }

   

}