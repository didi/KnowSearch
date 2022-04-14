package com.didichuxing.datachannel.arius.admin.core.service.monitor;

import com.didichuxing.datachannel.arius.admin.client.bean.common.BaseResult;
import com.didichuxing.datachannel.arius.admin.client.bean.common.NameValue;
import com.didichuxing.datachannel.arius.admin.client.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.monitor.AppMonitorRuleDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.monitor.NotifyGroupDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.monitor.QueryNotifyGroupDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.monitor.QueryMonitorRuleDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.monitor.MonitorRuleDetailVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.monitor.NotifyGroupVO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.Alert;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.Metric;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.MetricSinkPoint;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.MonitorAlertDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.n9e.UserInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.po.monitor.MonitorRulePO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.monitor.NotifyGroupPO;
import com.didichuxing.datachannel.arius.admin.common.exception.NotExistException;

import java.util.List;
import java.util.Properties;

public interface MonitorService {
    /**
     * 创建一个监控告警的规则
     * @param monitorDTO
     * @return
     */
    Result<Long> createMonitorRule(AppMonitorRuleDTO monitorDTO);

    /**
     * 删除一个监控告警的规则
     * @param id
     * @return
     */
    Result<Boolean> deleteMonitorRule(Long id);

    /**
     * 更新一个监控告警的规则
     * @param monitorDTO
     * @return
     */
    Result<Void> modifyMonitorRule(AppMonitorRuleDTO monitorDTO);

    /**
     * 获取所有的监控告警的规则
     * @return
     */
    BaseResult findMonitorRules(QueryMonitorRuleDTO dto);

    /**
     * 获取一个监控规则的详情
     * @param id
     * @return
     */
    Result<MonitorRuleDetailVO> getMonitorRuleDetail(Long id);

    /**
     * 根据id获取一个监控规则的详情
     * @param id
     * @return
     */
    MonitorRulePO getMonitorRuleById(Long id);

    /**
     * 获取一个监控策略
     * @param alertRuleId
     * @return
     */
    MonitorRulePO getByAlertRuleId(Long alertRuleId);


    Result<Boolean> switchMonitorRule(Long id, Integer status);

    /**
     * 获取一段时间内的告警历史
     * @param id
     * @param startTime
     * @param endTime
     * @return
     */
    Result<List<Alert>> getMonitorAlertHistory(Long id, Long startTime, Long endTime);

    /**
     * 获取一个告警的详情
     * @param alertId
     * @return
     */
    Result<MonitorAlertDetail> getMonitorAlertDetail(Long alertId);

    /**
     * 指标的上报和查询
     */
    Boolean sinkMetrics(List<MetricSinkPoint> pointList);

    /**
     * 获取指标
     * @param metric
     * @param startTime
     * @param endTime
     * @param step
     * @param tags
     * @return
     */
    Metric getMetrics(String metric, Long startTime, Long endTime, Integer step, Properties tags);

    //告警组

    /**
     * 添加告警组
     * @param notifyGroupPO
     * @return
     */
    int insertNotifyGroup(NotifyGroupPO notifyGroupPO);

    /**
     * 添加告警组，调用夜莺添加告警组
     * @param notifyGroupDTO
     * @return
     */
    int saveNotifyGroup(NotifyGroupDTO notifyGroupDTO);

    /**
     * 物理删除告警组
     * @param id
     * @return
     */
    int deleteNotifyGroup(long id);

    /**
     * 逻辑删除告警组，调用夜莺删除告警组
     * @param id
     * @return
     * @throws NotExistException
     */
    int removeNotifyGroup(long id) throws NotExistException;

    /**
     * 修改告警组信息，调用夜莺修改告警组
     * @param dto
     * @return
     * @throws NotExistException
     */
    int modifyNotifyGroup(NotifyGroupDTO dto) throws Exception;

    /**
     * 获取告警组详情，远程调用夜莺获取组详情
     * @param id
     * @return
     * @throws NotExistException
     */
    NotifyGroupVO getNotifyGroupVO(long id) throws NotExistException;

    /**
     * 获取告警组PO
     * @param id
     * @return
     */
    NotifyGroupPO getNotifyGroup(long id);

    /**
     * 根据条件查询告警组PO列表
     * @param param
     * @return
     */
    List<NotifyGroupPO> findNotifyGroups(QueryNotifyGroupDTO param);

    /**
     * 告警组分页查询
     * @param param
     * @return
     */
    PaginationResult<NotifyGroupVO> findNotifyGroupPage(QueryNotifyGroupDTO param);

    /**
     * 根据id批量查询告警组PO
     * @param ids
     * @return
     */
    List<NotifyGroupPO> listNotifyGroupByIds(List<Long> ids);

    /**
     * 启用/停用 告警组
     * @param id
     * @param status
     * @throws NotExistException
     */
    void switchNotifyGroup(Long id, Integer status) throws NotExistException;

    /**
     * 检查告警组是否被使用， 并返回已经使用的策略名字， 否则返回空数组
     * @param id
     * @return
     */
    List<String> checkNotifyGroupUsed(Long id);

    /**
     * 根据关键字查找人员
     * @param keyword
     * @return
     */
    List<UserInfo> findN9eUsers(String keyword);

    /**
     * 获取告警对象类型
     * @return
     */
    List<NameValue> findMonitorCategory();

    /**
     * 获取告警策略的统计类型
     * @return
     */
    List<NameValue> findMonitorStatsTypes();

    /**
     * 获取告警策略的度量方式
     * @return
     */
    List<NameValue> findMonitorOperators();

    /**
     * 根据告警对象类型，获取指标
     * @param category
     * @return
     */
    List<String> findMonitorMetrics(String category);
}
