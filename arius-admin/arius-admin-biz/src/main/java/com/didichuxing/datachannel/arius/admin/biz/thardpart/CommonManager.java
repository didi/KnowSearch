package com.didichuxing.datachannel.arius.admin.biz.thardpart;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ThirdPartClusterVO;
import java.util.List;

public interface CommonManager {
    

    /**
     * 获取物理集群列表接口
     * @return
     */
    Result<List<ThirdPartClusterVO>> listDataCluster();

    /**
     * 获取集群接口
     * @param cluster
     * @return
     */
    Result<ThirdPartClusterVO> getDataCluster(String cluster);




 
}