package com.didichuxing.datachannel.arius.admin.persistence.mysql.gateway;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.didichuxing.datachannel.arius.admin.common.bean.po.gateway.GatewayNodePO;

/**
 * @author d06679
 */
@Repository
public interface GatewayNodeDAO {

    int recordGatewayNode(GatewayNodePO gatewayNodePO);

    int aliveCountByClusterNameAndTime(@Param("clusterName") String clusterName,
                                       @Param("time") Date time);

    List<GatewayNodePO> listAliveNodeByClusterNameAndTime(@Param("clusterName") String clusterName,
                                                          @Param("time") Date time);

}
