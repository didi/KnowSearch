package com.didichuxing.datachannel.arius.admin.core.service.metrics;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.metrics.MetricsConfigInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.MetricsTypeEnum;

import java.util.List;

/**
 * @author wangpengkai
 */
public interface MetricsConfigService {
    /**
     * 获取物理集群指标看板下的配置
     * @param param 指标的信息说明DTO
     * @return 二级目录下的指标名称列表
     */
    List<String> getMetricsByTypeAndDomainAccount(MetricsConfigInfoDTO param);

    /**
     * 更新物理集群看板下的配置
     * @param param 指标的信息说明DTO
     * @return result
     */
    Result<Integer> updateByMetricsByTypeAndDomainAccount(MetricsConfigInfoDTO param);

    /**
     * 删除用户账号下的指标配置信息
     * @param domainAccount 账号
     */
    void deleteByDomainAccount(String domainAccount);
}
