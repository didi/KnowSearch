package com.didichuxing.datachannel.arius.admin.remote.nightingale;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.n9e.AlertRule;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.n9e.UserGroup;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.n9e.UserGroupMember;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.n9e.UserInfo;

import java.util.List;

public interface N9eMonitorService {
    /**
     * 新建告警策略
     * @param alertRule
     * @return
     */
    Long createAlertRule(AlertRule alertRule);

    /**
     * 删除告警策略
     * @param id
     * @return
     */
    Boolean deleteAlertRuleById(Long id);

    /**
     * 修改告警策略
     * @param alertRule
     * @return
     */
    Boolean modifyAlertRule(AlertRule alertRule);

    /**
     * 获得告警策略详情
     * @param id
     * @return
     */
    AlertRule getAlertRuleById(Long id);

    /**
     * 批量更新告警策略status
     * @param ids
     * @param status //0 启动，1禁用
     * @return
     */
    Boolean batchModifyAlertRuleStatus(List<Long> ids, Integer status);

    Long createUserGroup(UserGroup userGroup);

    Boolean delUserGroup(Long groupId);

    UserGroupMember getUserGroup(Long groupId);

    Boolean modifyUserGroup(Long groupId, UserGroup userGroup);

    /**
     * 增加/删除组成员
     * @param groupId
     * @param userIds
     * @param opt 1.增加 2.删除
     * @return
     */
    Boolean manageGroupMembers(Long groupId, List<Long> userIds, Integer opt);

    List<UserInfo> findUsers(String keyword);



}
