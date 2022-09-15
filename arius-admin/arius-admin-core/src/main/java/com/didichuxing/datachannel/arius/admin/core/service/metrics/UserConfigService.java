package com.didichuxing.datachannel.arius.admin.core.service.metrics;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.metrics.UserConfigInfoDTO;

import java.util.List;

/**
 * @author wangpengkai
 */
public interface UserConfigService {
    /**
     * 获取物理集群指标看板下的配置
     * @param param 指标的信息说明DTO
     * @return 二级目录下的指标名称列表
     */
    List<String> getMetricsByTypeAndUserName(UserConfigInfoDTO param);

    /**
     * 更新物理集群看板下的配置
     * @param param 指标的信息说明DTO
     * @return result
     */
    Result<Integer> updateByMetricsByTypeAndUserName(UserConfigInfoDTO param);

    /**
     * 删除用户账号下的指标配置信息
     * @param userName 账号
     */
    void deleteByUserName(String userName,Integer projectId);
}