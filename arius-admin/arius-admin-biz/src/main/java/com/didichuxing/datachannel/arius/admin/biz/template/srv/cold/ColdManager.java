package com.didichuxing.datachannel.arius.admin.biz.template.srv.cold;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;

/**
 * @author chengxiang, zqr
 * @date 2022/5/13
 */
public interface ColdManager {

    /**
     * move2ColdNode
     * @param logicTemplateId 逻辑模板id
     * @return
     */
    Result<Void> move2ColdNode(Integer logicTemplateId);

    /**
     * fetchClusterDefaultHotDay
     * @param phyCluster
     * @return
     */
    int fetchClusterDefaultHotDay(String phyCluster);
    
    ///////////////srv
        /**
     * move2ColdNode
     * @param cluster
     * @return
     */
    Result<Boolean> move2ColdNode(String cluster);
}