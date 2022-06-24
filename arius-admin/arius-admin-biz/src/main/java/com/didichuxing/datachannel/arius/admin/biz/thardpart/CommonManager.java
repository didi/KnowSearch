package com.didichuxing.datachannel.arius.admin.biz.thardpart;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.config.AriusConfigInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ThirdPartClusterVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.config.ThirdpartConfigVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ThirdPartTemplateLogicWithMasterTemplateResourceVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ThirdpartTemplateLogicVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ThirdpartTemplatePhysicalVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ThirdpartTemplateVO;
import java.io.UnsupportedEncodingException;
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