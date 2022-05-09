package com.didichuxing.datachannel.arius.admin.persistence.mysql.workorder;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.didichuxing.datachannel.arius.admin.common.bean.po.order.AriusWorkOrderInfoPO;
import org.springframework.stereotype.Repository;

/**
 * @author fengqiongfeng
 * @date 2020/8/26
 */
@Repository
public interface AriusWorkOrderInfoDAO {
    
    /**
     * 新增
     *
     * @param param 入参
     * @return int
     */
    int insert(AriusWorkOrderInfoPO param);
    
    /**
     * 获取通过id
     *
     * @param id id
     * @return {@link AriusWorkOrderInfoPO}
     */
    AriusWorkOrderInfoPO getById(@Param("id")Long id);

    List<AriusWorkOrderInfoPO> list();
    
    /**
     * 更新订单状态通过id
     *
     * @param id id
     * @param status 状态
     * @return int
     */
    int updateOrderStatusById(@Param("id")Long id,
                              @Param("status")Integer status);

    int update(AriusWorkOrderInfoPO param);
    
    /**
     * 由申请人和状态列表
     *
     * @param applicant 申请人
     * @param status 状态
     * @return {@link List}<{@link AriusWorkOrderInfoPO}>
     */
    List<AriusWorkOrderInfoPO> listByApplicantAndStatus(@Param("applicant")String applicant,
                                                        @Param("status")Integer status);
    
    /**
     * 由审批人列表和地位
     *
     * @param approver 审批人
     * @param status 状态
     * @return {@link List}<{@link AriusWorkOrderInfoPO}>
     */
    List<AriusWorkOrderInfoPO> listByApproverAndStatus(@Param("approver")String approver,
                                                       @Param("status")Integer status);
    
    /**
     * 列表状态
     *
     * @param status 状态
     * @return {@link List}<{@link AriusWorkOrderInfoPO}>
     */
    List<AriusWorkOrderInfoPO> listByStatus(@Param("status")Integer status);
    
    /**
     * 更新扩展id
     *
     * @param param 入参
     * @return int
     */
    int updateExtensionsById(AriusWorkOrderInfoPO param);
    
    /**
     * 列表处理时间
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return {@link List}<{@link AriusWorkOrderInfoPO}>
     */
    List<AriusWorkOrderInfoPO> listByHandleTime(@Param("startTime")Date startTime,
                                                @Param("endTime")Date endTime);
}