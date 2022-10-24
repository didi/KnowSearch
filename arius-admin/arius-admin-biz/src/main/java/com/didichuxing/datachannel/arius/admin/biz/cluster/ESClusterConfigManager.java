package com.didichuxing.datachannel.arius.admin.biz.cluster;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.ecm.ESConfigVO;
import java.util.List;
import java.util.Set;

/**
 * esclusterconfig
 *
 * @author shizeying
 * @date 2022/07/11
 */
public interface ESClusterConfigManager {
    /**
     * 编辑configdesc
     *
     * @param param     入参
     * @param operator  操作人或角色
     * @param projectId 项目id
     * @return {@code Result<Void>}
     */
    Result<Void> editConfigDesc(ESConfigDTO param, String operator, Integer projectId);

    /**
     * 获取ES集群模板config
     *
     * @param type 类型
     * @return {@code Result<ESConfigVO>}
     */
    Result<ESConfigVO> getEsClusterTemplateConfig(String type);

    Result<Set<String>> gainEsClusterRoles(Long clusterId);

    /**
     * 获取ES集群config通过id
     *
     * @param configId configid
     * @return {@code Result<ESConfigVO>}
     */
    Result<ESConfigVO> getEsClusterConfigById(Long configId);

    /**
     * 获得ES集群配置
     *
     * @param clusterId 集群id
     * @return {@code Result<List<ESConfigVO>>}
     */
    Result<List<ESConfigVO>> gainEsClusterConfigs(Long clusterId);
}