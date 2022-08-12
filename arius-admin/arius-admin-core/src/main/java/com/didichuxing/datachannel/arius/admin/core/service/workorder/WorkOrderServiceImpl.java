package com.didichuxing.datachannel.arius.admin.core.service.workorder;

import com.didichuxing.datachannel.arius.admin.common.bean.po.order.WorkOrderPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.order.WorkOrderVO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.workorder.WorkOrderDAO;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 工作订单service实现
 *
 * @author shizeying
 * @date 2022/08/12
 */
@Service
public class WorkOrderServiceImpl implements WorkOrderService {
    @Autowired
    private WorkOrderDAO orderDao;
    
    /**
     * 获取通过id
     *
     * @param orderId 订单id
     * @return {@link WorkOrderPO}
     */
    @Override
    public WorkOrderPO getById(Long orderId) {
        return orderDao.getById(orderId);
    }
    
    /**
     * 新增
     *
     * @param orderPO 订单po
     * @return int
     */
    @Override
    public int insert(WorkOrderPO orderPO) {
        return orderDao.insert(orderPO);
    }
    
    /**
     * 更新
     *
     * @param orderPO 订单po
     * @return int
     */
    @Override
    public int update(WorkOrderPO orderPO) {
        return orderDao.update(orderPO);
    }
    
    /**
     * 列表
     *
     * @return {@link List}<{@link WorkOrderPO}>
     */
    @Override
    public List<WorkOrderPO> list() {
       return orderDao.list();
    }
    
    /**
     * 更新订单状态通过id
     *
     * @param id   id
     * @param code code
     * @return boolean
     */
    @Override
    public boolean updateOrderStatusById(Long id, Integer code) {
        return orderDao.updateOrderStatusById(id, code)>0;
    }
    
    /**
     * 由申请人和状态列表
     *
     * @param applicant 申请人
     * @param status    状态
     * @return {@link List}<{@link WorkOrderVO}>
     */
    @Override
    public List<WorkOrderVO> listByApplicantAndStatus(String applicant, Integer status) {
        return ConvertUtil.list2List(orderDao.listByApplicantAndStatus(applicant, status),
                WorkOrderVO.class);
    }
    
    /**
     * 通过状态和项目id列表
     *
     * @param status    状态
     * @param projectId 项目id
     * @return {@link List}<{@link WorkOrderVO}>
     */
    @Override
    public List<WorkOrderVO> listByStatusAndProjectId(Integer status, Integer projectId) {
        return ConvertUtil.list2List(
                orderDao.listByStatusAndProjectId(status, projectId), WorkOrderVO.class);
    }
    
    
    @Override
    public List<WorkOrderPO> listByApproverAndStatus(String approver, Integer status) {
        return orderDao.listByApproverAndStatus(approver,status);
    }
    
    /**
     * 列表状态
     *
     * @param status 代码
     * @return {@link List}<{@link WorkOrderPO}>
     */
    @Override
    public List<WorkOrderPO> listByStatus(Integer status) {
       return orderDao.listByStatus(status);
    }
}