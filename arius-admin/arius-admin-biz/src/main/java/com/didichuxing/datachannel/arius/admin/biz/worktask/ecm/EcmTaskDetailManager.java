package com.didichuxing.datachannel.arius.admin.biz.worktask.ecm;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.EcmTaskDetail;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.EcmTaskDetailProgress;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.response.EcmSubTaskLog;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminTaskException;

import java.util.List;

/**
 * ES工单任务详情 服务类
 * @author didi
 * @since 2020-09-24
 */
public interface EcmTaskDetailManager {
    int replace(EcmTaskDetail ecmTaskDetail);

    /**
     * 创建一个工单任务详情
     * @param  esEcmTaskDetail 工单任务
     * @return result
     */
    Result<Long> saveEcmTaskDetail(EcmTaskDetail esEcmTaskDetail);

    /**
     * 修改工单任务详情的taskId
     * @param
     * @return result
     */
    Result<Integer> updateByRoleAndOrderTaskId(Long workOrderTaskId, String roleName, Long taskId);

    /**
     * 根据工单任务ID获取详情列表
     * @param  workOrderTaskId 工单任务ID
     * @return List<EcmTaskDetailPO>
     */
    List<EcmTaskDetail> getEcmTaskDetailInOrder(Long workOrderTaskId);

    /**
     * 获取taskDetail
     * @param  workOrderTaskId 工单任务ID
     * @param  role  角色
     * @return List
     */
    List<EcmTaskDetail> getByOrderIdAndRoleAndTaskId(Integer workOrderTaskId, String role, Integer taskId);

    /**
     * 根据工单任务详情ID获取对应日志
     * @param  detailId 工单详情ID
     * @param  operator 操作人
     * @return result
     */
    Result<EcmSubTaskLog> getTaskDetailLog(Long detailId, String operator);

    /**
     * 根据工单任务ID获取详情列表 与 统计数据
     * @param  workOrderTaskId 工单任务ID
     * @return result
     */
    Result<EcmTaskDetailProgress> getEcmTaskDetailInfo(Long workOrderTaskId) throws AdminTaskException;

    /**
     * 获取waiting状态TaskDetail
     * @param workOrderTaskId
     * @return
     */
    EcmTaskDetailProgress buildInitialEcmTaskDetail(Long workOrderTaskId);

    /**
     * 编辑taskDetail
     * @param buildEcmTaskDetail
     * @return
     */
    Result<Long> editEcmTaskDetail(EcmTaskDetail buildEcmTaskDetail);

    /**
     * 获取ecm task Detail
     * @param workOrderId
     * @param hostname
     * @return
     */
    EcmTaskDetail getByWorkOderIdAndHostName(Long workOrderId, String hostname);

    /**
     * 根据工单任务ID删除对应的任务详情信息
     */
    Result<Void> deleteEcmTaskDetailsByTaskOrder(Long workOrderTaskId);
}
