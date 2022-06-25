package com.didichuxing.datachannel.arius.admin.biz.template.srv.cold;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
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
    Tuple<Set<String>, Set<String>> getColdAndHotIndex(Long physicalId);

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
}