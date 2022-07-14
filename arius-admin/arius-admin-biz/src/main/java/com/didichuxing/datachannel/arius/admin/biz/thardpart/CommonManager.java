package com.didichuxing.datachannel.arius.admin.biz.thardpart;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.config.AriusConfigInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ThirdPartClusterVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.config.ThirdpartConfigVO;
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

    /**
    * 获取配置列表接口
    * @param param
    * @return
    */
    Result<List<ThirdpartConfigVO>> queryConfig(AriusConfigInfoDTO param);

}