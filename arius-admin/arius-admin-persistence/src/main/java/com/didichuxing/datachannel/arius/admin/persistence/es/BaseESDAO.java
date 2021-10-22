package com.didichuxing.datachannel.arius.admin.persistence.es;

import com.didichuxing.datachannel.arius.admin.persistence.component.ESGatewayClient;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESOpClient;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESUpdateClient;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dataCentre.DataCentreUtil;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls.DslLoaderUtil;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 直接操作es集群的dao
 */
public class BaseESDAO {
    protected final ILog      LOGGER = LogFactory.getLog(BaseESDAO.class);

    /**
     * 索引名数据中心加载工具类
     */
    @Autowired
    protected DataCentreUtil dataCentreUtil;
    /**
     * 加载查询语句工具类
     */
    @Autowired
    protected DslLoaderUtil dslLoaderUtil;
    /**
     * 查询es客户端
     */
    @Autowired
    protected ESGatewayClient gatewayClient;
    /**
     * 更新es客户端
     */
    @Autowired
    protected ESUpdateClient  updateClient;

    /**
     * Arius操作es集群的client
     */
    @Autowired
    protected ESOpClient      esOpClient;
}
