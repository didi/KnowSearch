package com.didichuxing.datachannel.arius.admin.biz.template.srv.cold;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;

import java.util.Set;

/**
 * @author zqr
 * @date 2020-09-09
 */
public interface TemplateColdManager {

    /**
     * move2ColdNode
     * @param cluster
     * @return
     */
    Result<Boolean> move2ColdNode(String cluster);

    /**
     * 获取cold索引
     * @param physicalId 物理模板ID
     * @return set集合
     */
    Set<String> getColdIndex(Long physicalId);

    /**
     * fetchClusterDefaultHotDay
     * @param phyCluster
     * @return
     */
    int fetchClusterDefaultHotDay(String phyCluster);

    /**
     * batchChangeHotDay
     * @param days
     * @param operator
     * @return
     */
    Result<Integer> batchChangeHotDay(Integer days, String operator);

    /**
     * 修改热数据的rack
     * @param physicalId 物理模板id
     * @param tgtRack 目标rack
     * @param retryCount 重试次数
     * @throws ESOperateException e
     * @return true/false
     */
    boolean updateHotIndexRack(Long physicalId, String tgtRack, int retryCount) throws ESOperateException;
}
