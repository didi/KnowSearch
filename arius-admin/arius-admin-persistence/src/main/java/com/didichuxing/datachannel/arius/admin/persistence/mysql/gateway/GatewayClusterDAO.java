package com.didichuxing.datachannel.arius.admin.persistence.mysql.gateway;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.didichuxing.datachannel.arius.admin.common.bean.po.gateway.GatewayClusterPO;


/**
 * @author d06679
 */
@Repository
public interface GatewayClusterDAO {

    /**
     * 插入数据
     * @param param po
     * @return int
     */
    int insert(GatewayClusterPO param);

    /**
     * 查询所有数据
     * @return List<GatewayClusterPO>
     */
    List<GatewayClusterPO> listAll();

}
