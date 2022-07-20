package com.didichuxing.datachannel.arius.admin.biz.template.srv.cold;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import java.util.List;

/**
 * @author chengxiang, zqr
 * @date 2022/5/13
 */
public interface ColdManager {

    /**
     * move2ColdNode
     *
     * @param logicTemplateId 逻辑模板id
     * @return
     */
    Result<Boolean> move2ColdNode(Integer logicTemplateId) throws ESOperateException;

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
    @Deprecated
    Result<Boolean> move2ColdNode(String cluster);

    Result<Integer> batchChangeHotDay(Integer coldSaveDays, String operator, List<Integer> templateIdList,
                                      Integer projectId);
}