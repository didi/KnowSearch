package com.didichuxing.datachannel.arius.admin.core.service.monitor.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.didichuxing.datachannel.arius.admin.common.bean.common.BaseResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.NameValue;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.monitor.AppMonitorRuleDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.monitor.NotifyGroupDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.monitor.QueryMonitorRuleDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.monitor.QueryNotifyGroupDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.monitor.MonitorRuleDetailVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.monitor.NotifyGroupVO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.GlobalParams;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.App;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.*;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.n9e.AlertRule;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.n9e.UserGroup;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.n9e.UserGroupMember;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.n9e.UserInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.po.monitor.MonitorRulePO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.monitor.NotifyGroupPO;
import com.didichuxing.datachannel.arius.admin.common.constant.*;
import com.didichuxing.datachannel.arius.admin.common.converter.CommonConverter;
import com.didichuxing.datachannel.arius.admin.common.exception.N9eRemoteException;
import com.didichuxing.datachannel.arius.admin.common.exception.NotExistException;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.core.service.monitor.MonitorService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.monitor.MonitorRuleDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.monitor.NotifyGroupDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.resource.ClusterDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.resource.LogicClusterDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplateLogicDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplatePhysicalDAO;
import com.didichuxing.datachannel.arius.admin.remote.monitor.RemoteMonitorService;
import com.didichuxing.datachannel.arius.admin.remote.nightingale.N9eMonitorService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by dengshan on 2020/10/31
 */
@Service
public class MonitorServiceImpl implements MonitorService {

    protected static final ILog  LOGGER = LogFactory.getLog(MonitorServiceImpl.class);

    private static final String STATUS = "status";
    private static final String UPDATE_TIME = "update_time";
    private static final String NO_ID = "告警组不存在ID";

    @Autowired
    private RemoteMonitorService remoteMonitorService;

    @Autowired
    private N9eMonitorService n9eMonitorService;

    @Autowired
    private MonitorRuleDAO monitorRuleDAO;

    @Autowired
    private NotifyGroupDAO notifyGroupDAO;

    @Autowired
    private LogicClusterDAO logicClusterDAO;

    @Autowired
    private ClusterDAO clusterDAO;

    @Autowired
    private IndexTemplateLogicDAO indexTemplateLogicDAO;

    @Autowired
    private IndexTemplatePhysicalDAO indexTemplatePhysicalDAO;


    @Autowired
    private AppService appService;

    @Override
    public Result<Long> createMonitorRule(AppMonitorRuleDTO monitorDTO) {
        String notifyGroups = monitorDTO.getNotifyGroups();
        if (StringUtils.isNotBlank(notifyGroups)) {
            Set<Long> notifyGroupIds = Arrays.stream(notifyGroups.split(",")).map(Long::valueOf).collect(Collectors.toSet());
            String userGroupIds = notifyGroupDAO.selectBatchIds(notifyGroupIds).stream().map(NotifyGroupPO::getUserGroupId).map(String::valueOf).collect(Collectors.joining(" "));
            monitorDTO.setNotifyGroups(userGroupIds);

        }
        Long alertRuleId = n9eMonitorService.createAlertRule(CommonConverter.convert2AlertRule(null, monitorDTO));

        if (null == alertRuleId) {
            return Result.buildFail("获取夜莺的策略id为空");
        }

        MonitorRulePO monitorRulePO = new MonitorRulePO();

        monitorRulePO.setName(monitorDTO.getName());
        monitorRulePO.setAlertRuleId(alertRuleId);
        monitorRulePO.setAppId(monitorDTO.getAppId());
        monitorRulePO.setOperator(GlobalParams.CURRENT_USER.get());
        monitorRulePO.setCategory(monitorDTO.getCategory());
        monitorRulePO.setObjectNames(monitorDTO.getObjectNames());
        monitorRulePO.setMetrics(monitorDTO.getMetrics());
        monitorRulePO.setPriority(monitorDTO.getPriority());
        monitorRulePO.setNotifyGroups(notifyGroups);
        monitorRulePO.setNotifyUsers(monitorDTO.getNotifyUsers());
        monitorRulePO.setStatus(MonitorRuleStatusEnum.ENABLE.getValue());
        monitorRuleDAO.insert(monitorRulePO);
        return Result.buildSucc(monitorRulePO.getId());
    }

    @Override
    public Result<Boolean> deleteMonitorRule(Long id) {
        MonitorRulePO monitorRulePO = this.getMonitorRuleById(id);
        if (null == monitorRulePO) {
            return Result.buildFail("获取告警策略为空");
        }

        Boolean status = n9eMonitorService.deleteAlertRuleById(monitorRulePO.getAlertRuleId());
        if (!status.booleanValue()) {
            return Result.buildFail("从夜莺删除策略失败");
        }

        monitorRulePO.setStatus(MonitorRuleStatusEnum.DELETE.getValue());
        monitorRuleDAO.updateById(monitorRulePO);
        return Result.buildSucc();
    }

    @Override
    public Result<Void> modifyMonitorRule(AppMonitorRuleDTO monitorDTO) {
        MonitorRulePO monitorRulePO = this.getMonitorRuleById(monitorDTO.getId());
        if (null == monitorRulePO) {
            return Result.buildFail("获取告警策略为空");
        }
        String notifyGroups = monitorDTO.getNotifyGroups();
        if (StringUtils.isNotBlank(notifyGroups)) {
            Set<Long> notifyGroupIds = Arrays.stream(notifyGroups.split(",")).map(Long::valueOf).collect(Collectors.toSet());
            String userGroupIds = notifyGroupDAO.selectBatchIds(notifyGroupIds).stream().map(NotifyGroupPO::getUserGroupId).map(String::valueOf).collect(Collectors.joining(" "));
            monitorDTO.setNotifyGroups(userGroupIds);

        }
        Boolean b = n9eMonitorService.modifyAlertRule(CommonConverter.convert2AlertRule(monitorRulePO.getAlertRuleId(), monitorDTO));
        if (!b.booleanValue()) {
            return Result.buildFail("从夜莺更改策略失败");
        }
        monitorRulePO.setName(monitorDTO.getName());
        monitorRulePO.setCategory(monitorDTO.getCategory());
        monitorRulePO.setMetrics(monitorDTO.getMetrics());
        monitorRulePO.setPriority(monitorDTO.getPriority());
        monitorRulePO.setOperator(GlobalParams.CURRENT_USER.get());
        monitorRulePO.setNotifyGroups(notifyGroups);
        monitorRulePO.setObjectNames(monitorDTO.getObjectNames());

        monitorRuleDAO.updateById(monitorRulePO);
        return Result.build(true);
    }

    @Override
    public BaseResult findMonitorRules(QueryMonitorRuleDTO dto) {

        App app = appService.getAppById(dto.getAppId());
        if (null == app) {
            return Result.buildFail("appid不存在");
        }

        Page<MonitorRulePO> page = new Page<>(dto.getPageNo(), dto.getPageSize());
        QueryWrapper<MonitorRulePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(dto.getAppId() != null, "app_id", dto.getAppId())
                .like(StringUtils.isNotBlank(dto.getName()), "name", dto.getName())
                .eq(StringUtils.isNotBlank(dto.getOperator()),"operator", dto.getOperator())
                .eq(dto.getPriority() != null, "priority", dto.getPriority())
                .eq(dto.getCategory() != null, "category", dto.getCategory())
                .eq(dto.getStatus() != null, STATUS, dto.getStatus())
                .apply(StringUtils.isNotBlank(dto.getObjectId()), "FIND_IN_SET ('" + dto.getObjectId() + "',object_ids)");
        queryWrapper.orderByDesc(UPDATE_TIME);
        Page<MonitorRulePO> monitorRulePOPage = monitorRuleDAO.selectPage(page, queryWrapper);
        List<MonitorRulePO> records = monitorRulePOPage.getRecords();


        List<MonitorRuleSummary> summaryList = new ArrayList<>();
        for (MonitorRulePO po : records) {
            MonitorRuleSummary summary = new MonitorRuleSummary();
            summary.setId(po.getId());
            summary.setName(po.getName());
            summary.setAppId(po.getAppId());
            summary.setOperator(po.getOperator());
            summary.setUpdateTime(po.getUpdateTime().getTime());
            summary.setPriority(po.getPriority());
            summary.setStatus(po.getStatus());
            summary.setCategoryName(MonitorCategoryEnum.val2Text(po.getCategory()));
            summary.setObjectNames(po.getObjectNames());
            summaryList.add(summary);
        }
        return PaginationResult.buildSucc(summaryList, monitorRulePOPage.getTotal(), monitorRulePOPage.getCurrent(), monitorRulePOPage.getSize());
    }

    @Override
    public Result<MonitorRuleDetailVO> getMonitorRuleDetail(Long id) {
        MonitorRulePO monitorRulePO = getMonitorRuleById(id);
        if (null == monitorRulePO) {
            return Result.buildFail("告警策略为空");
        }
        AlertRule alertRule = n9eMonitorService.getAlertRuleById(monitorRulePO.getAlertRuleId());
        if (null == alertRule) {
            return Result.buildFail("从夜莺获取策略失败");
        }

        AppMonitorRuleDTO appMonitorRuleDTO = CommonConverter.convert2MonitorRuleDTO(monitorRulePO, alertRule);

        App app = appService.getAppById(appMonitorRuleDTO.getAppId().intValue());
        MonitorRuleDetailVO vo = new MonitorRuleDetailVO();
        vo.setAppName(app.getName());
        vo.setCategoryName(MonitorCategoryEnum.val2Text(monitorRulePO.getCategory()));
        vo.setObjectNames(Lists.newArrayList(monitorRulePO.getObjectNames().split(",")));
        if (alertRule.getNotifyGroupsDetail() != null) {
            vo.setNotifyGroups(alertRule.getNotifyGroupsDetail().stream().map(UserGroup::getName).collect(Collectors.toList()));
        }
        if (alertRule.getNotifyUsersDetail() != null) {
            vo.setNotifyUsers(alertRule.getNotifyUsersDetail().stream().map(UserInfo::getUsername).collect(Collectors.toList()));
        }
        vo.setMonitorRule(appMonitorRuleDTO);

        return Result.buildSucc(vo);
    }

    @Override
    public MonitorRulePO getMonitorRuleById(Long id) {
        return monitorRuleDAO.selectById(id);
    }

    @Override
    public MonitorRulePO getByAlertRuleId(Long alertRuleId) {
        return monitorRuleDAO.selectOne(new QueryWrapper<MonitorRulePO>().eq("alert_rule_id", alertRuleId));
    }

    @Override
    public Result<Boolean> switchMonitorRule(Long id, Integer status) {
        MonitorRulePO monitorRule = getMonitorRuleById(id);
        if (monitorRule == null) {
            return Result.buildFail("告警策略为空");
        }
        Boolean b = n9eMonitorService.batchModifyAlertRuleStatus(Lists.newArrayList(monitorRule.getAlertRuleId()), status);
        if (!b.booleanValue()) {
            return Result.buildFail("修改夜莺状态失败");
        }
        monitorRule.setStatus(status);
        monitorRuleDAO.updateById(monitorRule);
        return Result.buildSucc();
    }

    @Override
    public Result<List<Alert>> getMonitorAlertHistory(Long id, Long startTime, Long endTime) {
        MonitorRulePO monitorRuleDO = this.getMonitorRuleById(id);
        if (null == monitorRuleDO) {
            return Result.buildFail("获取monitorRule为空");
        }
        List<Alert> alertList = remoteMonitorService.getAlerts(monitorRuleDO.getAlertRuleId(), startTime / 1000,
            endTime / 1000);
        if (CollectionUtils.isEmpty(alertList)) {
            return Result.buildSuccWithMsg("无历史告警!");
        }
        return Result.buildSucc(alertList);
    }

    @Override
    public Result<MonitorAlertDetail> getMonitorAlertDetail(Long alertId) {
        Alert alert = remoteMonitorService.getAlertById(alertId);
        if (null == alert) {
            return Result.buildFail("从odin获取告警历史失败");
        }

        MonitorRulePO monitorRuleDO = this.getByAlertRuleId(alert.getStrategyId());
        if (null == monitorRuleDO) {
            return Result.buildFail("获取monitorRule为空");
        }
        alert.setMonitorId(monitorRuleDO.getId());

        Metric metric = remoteMonitorService.getMetrics(alert.getMetric(), (alert.getStartTime() - 3600) * 1000,
            (alert.getEndTime() + 3600) * 1000, 60, alert.getTags());

        return Result.buildSucc(new MonitorAlertDetail(alert, metric));
    }

    @Override
    public int insertNotifyGroup(NotifyGroupPO notifyGroupPO) {
        return notifyGroupDAO.insert(notifyGroupPO);
    }

    @Override
    public int saveNotifyGroup(NotifyGroupDTO notifyGroupDTO) {
        UserGroup userGroup = new UserGroup();
        userGroup.setName(notifyGroupDTO.getName());
        userGroup.setNote(notifyGroupDTO.getComment());
        Long userGroupId = n9eMonitorService.createUserGroup(userGroup);
        if (userGroupId == null) {
            throw new N9eRemoteException("夜莺创建用户组异常");
        }
        List<Long> userIds = Arrays.stream(notifyGroupDTO.getMembers().split(",")).map(x -> x.split(";")[0]).map(Long::valueOf).collect(Collectors.toList());
        Boolean b = n9eMonitorService.manageGroupMembers(userGroupId, userIds, 1);
        if (!b.booleanValue()) {
            throw new N9eRemoteException("夜莺添加用户异常");
        }
        NotifyGroupPO notifyGroupPO = new NotifyGroupPO();
        notifyGroupPO.setUserGroupId(userGroupId);
        notifyGroupPO.setName(notifyGroupDTO.getName());
        notifyGroupPO.setMembers(notifyGroupDTO.getMembers());
        notifyGroupPO.setComment(notifyGroupDTO.getComment());
        notifyGroupPO.setStatus(notifyGroupDTO.getStatus());
        notifyGroupPO.setOperator(GlobalParams.CURRENT_USER.get());
        Long appId = notifyGroupDTO.getAppId() != null ? notifyGroupDTO.getAppId() :GlobalParams.CURRENT_APPID.get().longValue();
        notifyGroupPO.setAppId(appId);
        return insertNotifyGroup(notifyGroupPO);
    }

    @Override
    public int deleteNotifyGroup(long id) {
        return notifyGroupDAO.deleteById(id);
    }

    @Override
    public int removeNotifyGroup(long id) throws NotExistException {
        NotifyGroupPO notifyGroup = getNotifyGroup(id);
        if (notifyGroup == null) {
            throw new NotExistException(NO_ID + id);
        }
        Boolean b = n9eMonitorService.delUserGroup(notifyGroup.getUserGroupId());
        if (!b.booleanValue()) {
            throw new N9eRemoteException("夜莺删除用户组信息异常");
        }
        NotifyGroupPO po = new NotifyGroupPO();
        po.setId(id);
        po.setStatus(NotifyGroupStatusEnum.DELETE.getValue());
        return notifyGroupDAO.updateById(po);
    }

    @Override
    public int modifyNotifyGroup(NotifyGroupDTO dto) throws Exception {
        NotifyGroupPO notifyGroup = getNotifyGroup(dto.getId());
        if (notifyGroup == null) {
            throw new NotExistException(NO_ID + dto.getId());
        }
        //组名变了
        if (StringUtils.isNotBlank(dto.getName()) && !Objects.equals(dto.getName(), notifyGroup.getName())) {
            UserGroup userGroup = new UserGroup();
            userGroup.setName(dto.getName());
            Boolean b = n9eMonitorService.modifyUserGroup(notifyGroup.getUserGroupId(), userGroup);
            if (!b.booleanValue()) {
                throw new N9eRemoteException("夜莺修改用户组信息异常");
            }
        }
        //人员变了, 先删除旧成员， 再添加新成员
        if (!CollectionUtils.isEqualCollection(Arrays.asList(dto.getMembers().split(",")), Arrays.asList(notifyGroup.getMembers().split(",")))) {
            List<Long> oldUserIdList = Arrays.stream(notifyGroup.getMembers().split(",")).map(x -> x.split(";")[0]).map(Long::valueOf).collect(Collectors.toList());
            List<Long> userIdList = Arrays.stream(dto.getMembers().split(",")).map(x -> x.split(";")[0]).map(Long::valueOf).collect(Collectors.toList());
            Boolean b1 = n9eMonitorService.manageGroupMembers(notifyGroup.getUserGroupId(), oldUserIdList, 2);
            if (!b1.booleanValue()) {
                throw new N9eRemoteException("夜莺删除组成员异常");
            }
            Boolean b2 = n9eMonitorService.manageGroupMembers(notifyGroup.getUserGroupId(), userIdList, 1);
            if (!b2.booleanValue()) {
                throw new N9eRemoteException("夜莺添加组成员异常");
            }
        }

        BeanUtils.copyProperties(dto, notifyGroup);
        return notifyGroupDAO.updateById(notifyGroup);
    }

    @Override
    public NotifyGroupVO getNotifyGroupVO(long id) throws NotExistException {
        NotifyGroupPO notifyGroup = getNotifyGroup(id);
        if (notifyGroup == null) {
            return null;
        }
        UserGroupMember groupMember = n9eMonitorService.getUserGroup(notifyGroup.getUserGroupId());
        if (groupMember == null) {
            throw new N9eRemoteException("夜莺用户组不存在");
        }
        App app = appService.getAppById(notifyGroup.getAppId().intValue());
        if (null == app) {
            throw new NotExistException("app不存在:" + notifyGroup.getAppId());
        }

        List<NotifyGroupVO.IdName> userList = groupMember.getUsers().stream().map(x -> new NotifyGroupVO.IdName(x.getId(), x.getUsername())).collect(Collectors.toList());
        NotifyGroupVO vo = new NotifyGroupVO();
        BeanUtils.copyProperties(notifyGroup, vo);
        vo.setAppName(app.getName());
        vo.setUserList(userList);
        return vo;
    }

    @Override
    public NotifyGroupPO getNotifyGroup(long id) {
        return notifyGroupDAO.selectById(id);
    }

    @Override
    public List<NotifyGroupPO> findNotifyGroups(QueryNotifyGroupDTO param) {
        NotifyGroupPO notifyGroupPO = ConvertUtil.obj2Obj(param, NotifyGroupPO.class);
        QueryWrapper<NotifyGroupPO> queryWrapper = new QueryWrapper<>(notifyGroupPO);
        queryWrapper.orderByDesc(UPDATE_TIME);
        return notifyGroupDAO.selectList(queryWrapper);
    }

    @Override
    public PaginationResult<NotifyGroupVO> findNotifyGroupPage(QueryNotifyGroupDTO param) {
        Page<NotifyGroupPO> page = new Page<>(param.getPageNo(), param.getPageSize());
        QueryWrapper<NotifyGroupPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(param.getAppId() != null, "app_id", param.getAppId())
                .like(StringUtils.isNotBlank(param.getName()), "name", param.getName())
                .like(StringUtils.isNotBlank(param.getOperator()),"operator", param.getOperator())
                .eq(param.getStatus() != null, STATUS, param.getStatus())
                .like(StringUtils.isNotBlank(param.getMembers()), "members", param.getMembers());
        queryWrapper.orderByDesc(UPDATE_TIME);
        Page<NotifyGroupPO> notifyGroupPOPage = notifyGroupDAO.selectPage(page, queryWrapper);

        //解析出来name, id;name,id;name --> list<name>
        List<NotifyGroupVO> vos = notifyGroupPOPage.getRecords().stream().map(x -> {
            NotifyGroupVO vo = new NotifyGroupVO();
            BeanUtils.copyProperties(x, vo);
            vo.setUserList(Arrays.stream(x.getMembers().split(",")).filter(u -> u.split(";").length == 2).map(y -> {
                String[] split = y.split(";");
                return new NotifyGroupVO.IdName(Long.valueOf(split[0]), split[1]);
            }).collect(Collectors.toList()));
            return vo;
        }).collect(Collectors.toList());

        return PaginationResult.buildSucc(vos, notifyGroupPOPage.getTotal(), notifyGroupPOPage.getCurrent(), notifyGroupPOPage.getSize());
    }

    @Override
    public List<NotifyGroupPO> listNotifyGroupByIds(List<Long> ids) {
        return notifyGroupDAO.selectBatchIds(ids);
    }

    @Override
    public void switchNotifyGroup(Long id, Integer status) throws NotExistException {
        NotifyGroupPO notifyGroup = getNotifyGroup(id);
        if (notifyGroup == null) {
            throw new NotExistException(NO_ID + id);
        }
        //TODO:这里夜莺没有提供用户组的停用和启用功能。

        NotifyGroupPO po = new NotifyGroupPO();
        po.setId(id);
        po.setStatus(status);
        notifyGroupDAO.updateById(po);
    }

    @Override
    public List<String> checkNotifyGroupUsed(Long id) {
        QueryWrapper<MonitorRulePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne(STATUS, MonitorRuleStatusEnum.DELETE.getValue());
        queryWrapper.apply("FIND_IN_SET ('" + id + "',notify_groups)");
        List<MonitorRulePO> list = monitorRuleDAO.selectList(queryWrapper);
        return list.stream().map(MonitorRulePO::getName).collect(Collectors.toList());
    }

    @Override
    public List<UserInfo> findN9eUsers(String keyword) {
        return n9eMonitorService.findUsers(keyword);
    }

    @Override
    public List<NameValue> findMonitorCategory() {
        return Arrays.stream(MonitorCategoryEnum.values()).map(x -> new NameValue(x.getText(), x.getValue())).collect(Collectors.toList());
    }

    @Override
    public List<NameValue> findMonitorStatsTypes() {
        return Arrays.stream(MonitorStatsTypeEnum.values()).map(x -> new NameValue(x.getText(), x.getValue())).collect(Collectors.toList());
    }

    @Override
    public List<NameValue> findMonitorOperators() {
        return Arrays.stream(MonitorOperatorEnum.values()).map(x -> new NameValue(x.getText(), x.getValue())).collect(Collectors.toList());
    }

    @Override
    public List<String> findMonitorMetrics(String category) {
        return MonitorCategoryEnum.findByValue(category).getMetrics();
    }

    @Override
    public Boolean sinkMetrics(List<MetricSinkPoint> pointList) {
        return remoteMonitorService.sinkMetrics(pointList);
    }

    @Override
    public Metric getMetrics(String metricStr, Long startTime, Long endTime, Integer step, Properties tags) {
        return remoteMonitorService.getMetrics(metricStr, (startTime - 3600) * 1000, (endTime + 3600) * 1000, step,
            tags);
    }

    /**************************************************** private methods ****************************************************/
}
