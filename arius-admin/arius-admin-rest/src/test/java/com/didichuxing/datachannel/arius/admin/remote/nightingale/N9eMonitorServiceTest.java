package com.didichuxing.datachannel.arius.admin.remote.nightingale;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.n9e.*;
import com.didichuxing.datachannel.arius.admin.common.constant.NotifyChannelEnum;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class N9eMonitorServiceTest extends AriusAdminApplicationTests {

    @Autowired
    private N9eMonitorService n9eMonitorService;

    /**
     * 根据ID获取用户组
     */
    @Test
    public void testGetUserGroup() {
        UserGroupMember userGroup = n9eMonitorService.getUserGroup(9L);
        Assertions.assertNotNull(userGroup);
        Assertions.assertTrue(StringUtils.isNotBlank(JSON.toJSONString(userGroup)));
        System.out.println(userGroup);
        System.out.println(JSON.toJSONString(userGroup));
    }

    /**
     * 创建用户组
     */
    @Test
    public void testCreateUserGroup() {
        UserGroup userGroup = new UserGroup();
        userGroup.setName("fitz_test1");
        userGroup.setNote("comment test");
        Long i = n9eMonitorService.createUserGroup(userGroup);
        Assertions.assertNotNull(i);
        System.out.println(i);
    }

    /**
     * 修改用户组
     */
    @Test
    public void testModifyUserGroup() {
        UserGroup userGroup = new UserGroup();
        userGroup.setName("fitz_test1_modify");
        userGroup.setNote("comment test modify");
        Boolean b = n9eMonitorService.modifyUserGroup(10L, userGroup);
        Assertions.assertTrue(b);
        System.out.println(b);
    }

    /**
     * 删除用户组
     */
    @Test
    public void testDelUserGroup() {
        Boolean b = n9eMonitorService.delUserGroup(10L);
        Assertions.assertTrue(b);
        System.out.println(b);
    }

    /**
     * 添加/删除组成员
     */
    @Test
    public void testManageGroupMembers() {
//        BOOLEAN b = n9eMonitorService.manageGroupMembers(9L, Lists.newArrayList(13L, 14L), 1);
//        System.out.println(b);
        Boolean b = n9eMonitorService.manageGroupMembers(9L, Lists.newArrayList(14L), 2);
        Assertions.assertTrue(b);
        System.out.println(b);
    }

    /**
     * 根据关键字模糊搜索用户
     */
    @Test
    public void testFindUsers() {
        List<UserInfo> all = n9eMonitorService.findUsers("all");
        Assertions.assertTrue(CollectionUtils.isNotEmpty(all));
        System.out.println(all);
        System.out.println(JSON.toJSONString(all));
    }

    //告警策略

    /**
     * 根据ID查找告警策略
     */
    @Test
    public void testGetAlertRuleById() {
        AlertRule alertRule = n9eMonitorService.getAlertRuleById(152L);
        Assertions.assertNotNull(alertRule);
        System.out.println(alertRule);
        System.out.println(JSON.toJSONString(alertRule));
    }

    /**
     * 创建告警策略
     */
    @Test
    public void testCreateAlertRule() {
        AlertRule alertRule = new AlertRule();
        alertRule.setGroupId(27);
        alertRule.setName("fitz_auto");
        alertRule.setType(0);
        alertRule.setStatus(0);
        alertRule.setAlertDuration(60);
        alertRule.setExpression(new Expression());
        alertRule.setEnableStime("00:00");
        alertRule.setEnableEtime("23:59");
        alertRule.setEnableDaysOfWeek("1 2 3");
        alertRule.setPriority(1);
        alertRule.setNotifyChannels("");
        alertRule.setNotifyGroups("7 9");
        alertRule.setNotifyUsers("13 14");
        alertRule.setCallbacks("");

        Expression expression = alertRule.getExpression();
        expression.setTogetherOrAny(1);
        expression.setTriggerConditions(Lists.newArrayList());

        Exp exp = new Exp();
        exp.setOptr("=");
        exp.setFunc("all");
        exp.setMetric("go_memstats_buck_hash_sys_bytes");
        exp.setParams(Lists.newArrayList());
        exp.setThreshold(66);
        expression.getTriggerConditions().add(exp);

        Long id = n9eMonitorService.createAlertRule(alertRule);
        Assertions.assertNotNull(id);
        System.out.println(id);
    }

    /**
     * 修改告警策略
     */
    @Test
    public void testModifyAlertRule() {
        AlertRule alertRule = new AlertRule();
        alertRule.setId(157L);
        alertRule.setGroupId(27);
        alertRule.setName("fitz_auto");
        alertRule.setType(0);
        alertRule.setStatus(0);
        alertRule.setAlertDuration(60);
        alertRule.setExpression(new Expression());
        alertRule.setEnableStime("00:01");
        alertRule.setEnableEtime("23:59");
        alertRule.setEnableDaysOfWeek("1 2 3 4 5");
        alertRule.setPriority(1);
        alertRule.setNotifyChannels(NotifyChannelEnum.SMS.getValue());
        alertRule.setNotifyGroups("7 9");
        alertRule.setNotifyUsers("13 14");
        alertRule.setCallbacks("http://www.baidu.com");
//
        Expression expression = alertRule.getExpression();
        expression.setTogetherOrAny(1);
        expression.setTriggerConditions(Lists.newArrayList());

        Exp exp = new Exp();
        exp.setOptr("=");
        exp.setFunc("all");
        exp.setMetric("go_memstats_buck_hash_sys_bytes");
        exp.setParams(Lists.newArrayList());
        exp.setThreshold(66);
        expression.getTriggerConditions().add(exp);

        Boolean b = n9eMonitorService.modifyAlertRule(alertRule);
        Assertions.assertTrue(b);
        System.out.println(b);
    }

    /**
     * 批量修改状态
     */
    @Test
    public void testBatchModifyAlertRuleStatus() {
        Boolean b = n9eMonitorService.batchModifyAlertRuleStatus(Lists.newArrayList(157L, 129L), 1);
        Assertions.assertTrue(b);
        System.out.println(b);
    }

    /**
     * 删除告警策略
     */
    @Test
    public void testDeleteAlertRuleById() {
        Boolean b = n9eMonitorService.deleteAlertRuleById(157L);
        Assertions.assertTrue(b);
        System.out.println(b);
    }

}
