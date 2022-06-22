package com.didichuxing.datachannel.arius.admin.common.bean.entity.gateway;

import com.didichuxing.datachannel.arius.admin.common.bean.po.gateway.GatewayJoinPO;
import lombok.Data;

import java.util.List;

/**
 * @author cjm
 */
@Deprecated
@Data
public class SearchGatewayResponse {

    /**
     * Gateway的slow或error信息
     */
    private List<GatewayJoinPO> records;

    /**
     * 查询命中记录数
     */
    private Long totalHits;
}