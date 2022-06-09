package com.didichuxing.datachannel.arius.admin.biz.template.new_srv.cold;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;

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
}
