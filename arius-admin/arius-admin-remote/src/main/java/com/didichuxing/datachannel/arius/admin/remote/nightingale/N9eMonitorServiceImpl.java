package com.didichuxing.datachannel.arius.admin.remote.nightingale;

import com.alibaba.fastjson.TypeReference;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.n9e.*;
import com.didichuxing.datachannel.arius.admin.common.component.RestTool;
import com.didichuxing.datachannel.arius.admin.remote.nightingale.bean.NightingaleResult;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@NoArgsConstructor
public class N9eMonitorServiceImpl implements N9eMonitorService{

    private Logger logger = LoggerFactory.getLogger(N9eMonitorServiceImpl.class);

    @Autowired
    private RestTool restTool;

    private static final String ADD_ALERT_RULES             =  "/alert-rules";
    private static final String RESOURCE_ALERT_RULE         = "/alert-rule/%s";
    private static final String MODIFY_ALERT_RULE_STATUS    = "/alert-rules/status";
    private static final String ADD_USER_GROUP              = "/user-groups";
    private static final String MANAGE_USER_GROUP           = "/user-group/%s";
    private static final String MANAGE_GROUP_MEMBERS        = "/user-group/%s/members";
    private static final String FIND_USERS                  = "/users";


    @Value("${monitor.n9e.url}")
    private String n9eMonitorUrl;

    @Override
    public Long createAlertRule(AlertRule alertRule) {
        String url = n9eMonitorUrl + ADD_ALERT_RULES;
        try {
            NightingaleResult<List<Long>> result = restTool.postObjectWithJsonContentAndHeader(url, buildHeader(),
                    Lists.newArrayList(alertRule), new TypeReference<NightingaleResult<List<Long>>>(){}.getType());

            if (null == result || result.failed()) {
                return null;
            }
            return result.getDat().get(0);
        } catch (Exception e) {
            logger.error("class=N9eMonitorServiceImpl||method=createAlertRule||errMsg=createAlertRule error", e);
            return null;
        }
    }

    @Override
    public Boolean deleteAlertRuleById(Long id) {
        String url = n9eMonitorUrl + String.format(RESOURCE_ALERT_RULE, id);
        try {
            NightingaleResult<Object> result = restTool.deleteWithParamsAndHeader(url,
                    buildHeader(), Maps.newHashMap(), new TypeReference<NightingaleResult<Object>>(){}.getType());

            if (null == result || result.failed()) {
                return false;
            }
        } catch (Exception e) {
            logger.error("class=N9eMonitorServiceImpl||method=deleteAlertRuleById||errMsg=deleteAlertRuleById error", e);
            return false;
        }
        return true;
    }

    @Override
    public Boolean modifyAlertRule(AlertRule alertRule) {
        String url = n9eMonitorUrl + String.format(RESOURCE_ALERT_RULE, alertRule.getId());
        try {
            NightingaleResult<Object> result = restTool.putObjectWithJsonContentAndHeader(url,
                    buildHeader(), alertRule, new TypeReference<NightingaleResult<Object>>(){}.getType());

            if (null == result || result.failed()) {
                return false;
            }
        } catch (Exception e) {
            logger.error("class=N9eMonitorServiceImpl||method=modifyAlertRule||msg=modifyAlertRule error", e);
            return false;
        }
        return true;
    }

    @Override
    public AlertRule getAlertRuleById(Long id) {
        String url = n9eMonitorUrl + String.format(RESOURCE_ALERT_RULE, id);
        try {
            NightingaleResult<AlertRule> result = restTool.getObjectWithParamsAndHeader(url,
                    buildHeader(), Maps.newHashMap(), new TypeReference<NightingaleResult<AlertRule>>() {}.getType());

            if (null == result || result.failed()) {
                return null;
            }
            return result.getDat();
        } catch (Exception e) {
            logger.error("class=N9eMonitorServiceImpl||method=getAlertRuleById||errMsg=getAlertRuleById error", e);
            return null;
        }
    }

    @Override
    public Boolean batchModifyAlertRuleStatus(List<Long> ids, Integer status) {
        String url = n9eMonitorUrl + MODIFY_ALERT_RULE_STATUS;
        Map<String, Object> map = Maps.newHashMap();
        map.put("ids", ids);
        map.put("status", status);
        try {
            NightingaleResult<Object> result = restTool.putObjectWithJsonContentAndHeader(url,
                    buildHeader(), map, new TypeReference<NightingaleResult<Object>>(){}.getType());

            if (null == result || result.failed()) {
                return false;
            }
        } catch (Exception e) {
            logger.error("class=N9eMonitorServiceImpl||method=batchModifyAlertRuleStatus||errMsg=batchModifyStatus error", e);
            return false;
        }
        return true;
    }

    @Override
    public Long createUserGroup(UserGroup userGroup) {
        String url = n9eMonitorUrl + ADD_USER_GROUP;
        try {
            NightingaleResult<Long> result = restTool.postObjectWithJsonContentAndHeader(url,
                    buildHeader(), userGroup, new TypeReference<NightingaleResult<Long>>(){}.getType());

            if (null == result || result.failed()) {
                return null;
            }
            return result.getDat();
        } catch (Exception e) {
            logger.error("class=N9eMonitorServiceImpl||method=createUserGroup||errMsg=createUserGroup error", e);
            return null;
        }
    }

    @Override
    public Boolean delUserGroup(Long groupId) {
        String url = n9eMonitorUrl + String.format(MANAGE_USER_GROUP, groupId);
        try {
            NightingaleResult<Boolean> result = restTool.deleteWithParamsAndHeader(url,
                    buildHeader(), Maps.newHashMap(), new TypeReference<NightingaleResult<Boolean>>() {}.getType());

            if (null == result || result.failed()) {
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error("class=N9eMonitorServiceImpl||method=delUserGroup||errMsg=delUserGroup error", e);
            return false;
        }
    }

    @Override
    public UserGroupMember getUserGroup(Long groupId) {
        String url = n9eMonitorUrl + String.format(MANAGE_USER_GROUP, groupId);
        try {
            NightingaleResult<UserGroupMember> result = restTool.getObjectWithParamsAndHeader(url,
                    buildHeader(), Maps.newHashMap(), new TypeReference<NightingaleResult<UserGroupMember>>() {}.getType());

            if (null == result || result.failed()) {
                return null;
            }
            return result.getDat();
        } catch (Exception e) {
            logger.error("class=N9eMonitorServiceImpl||method=getUserGroup||errMsg=getUserGroup error", e);
            return null;
        }
    }

    @Override
    public Boolean modifyUserGroup(Long groupId, UserGroup userGroup) {
        String url = n9eMonitorUrl + String.format(MANAGE_USER_GROUP, groupId);
        try {
            NightingaleResult<Boolean> result = restTool.putObjectWithJsonContentAndHeader(url,
                    buildHeader(), userGroup, new TypeReference<NightingaleResult<Boolean>>() {}.getType());
            if (null == result || result.failed()) {
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error("class=N9eMonitorServiceImpl||method=modifyUserGroup||errMsg=modifyUserGroup error", e);
            return false;
        }
    }

    @Override
    public Boolean manageGroupMembers(Long groupId, List<Long> userIds, Integer opt) {
        String url = n9eMonitorUrl + String.format(MANAGE_GROUP_MEMBERS, groupId);
        try {
            Map<String, List<Long>> map = Maps.newHashMap();
            map.put("ids", userIds);
            NightingaleResult<Boolean> result = null;

            if (opt == 1) {
                result = restTool.postObjectWithJsonContentAndHeader(url, buildHeader(), map, new TypeReference<NightingaleResult<Boolean>>(){}.getType());
            } else {
                result = restTool.deleteWithParamsAndHeader(url, buildHeader(), map, new TypeReference<NightingaleResult<Boolean>>(){}.getType());
            }

            if (null == result || result.failed()) {
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error("class=N9eMonitorServiceImpl||method=manageGroupMembers||errMsg=manageGroupMembers error", e);
            return false;
        }
    }

    @Override
    public List<UserInfo> findUsers(String keyword) {
        if (StringUtils.isBlank(keyword)) {
            return Lists.newArrayList();
        }
        String url = n9eMonitorUrl + FIND_USERS + "?query={query}&p={p}&limit={limit}";
        Map<String, Object> params = Maps.newHashMap();
        params.put("query", keyword);
        params.put("limit", 10);
        params.put("p", 1);
        try {
            NightingaleResult<UserInfoPage> result = restTool.getObjectWithParamsAndHeader(url, buildHeader(),
                    params, new TypeReference<NightingaleResult<UserInfoPage>>(){}.getType());

            if (null == result || result.failed()) {
                return Lists.newArrayList();
            }
            return result.getDat().getList();
        } catch (Exception e) {
            logger.error("class=N9eMonitorServiceImpl||method=findUsers||errMsg=createAlertRule error", e);
            return Lists.newArrayList();
        }
    }

    private Map<String, String> buildHeader() {
        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/json");
        header.put("Authorization", "cfdee0c4647a41402bd2f5484135391d");
        return header;
    }
}
