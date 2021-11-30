package com.didichuxing.datachannel.arius.admin.persistence.es;

import java.util.concurrent.TimeUnit;

import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Autowired;

import com.didichuxing.datachannel.arius.admin.persistence.component.ESGatewayClient;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESOpClient;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESUpdateClient;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.datacentre.DataCentreUtil;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls.DslLoaderUtil;
import com.didiglobal.logi.elasticsearch.client.ESClient;
import com.didiglobal.logi.elasticsearch.client.gateway.direct.DirectRequest;
import com.didiglobal.logi.elasticsearch.client.gateway.direct.DirectResponse;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
/**
 * 直接操作es集群的dao
 */
public class BaseESDAO {
    protected static final ILog      LOGGER = LogFactory.getLog(BaseESDAO.class);

    /**
     * 索引名数据中心加载工具类
     */
    @Autowired
    protected DataCentreUtil  dataCentreUtil;
    /**
     * 加载查询语句工具类
     */
    @Autowired
    protected DslLoaderUtil   dslLoaderUtil;
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

    public DirectResponse getDirectResponse(String clusterName, String methodType, String url) {
        ESClient esClient = esOpClient.getESClient(clusterName);
        if (esClient == null) {
            LOGGER.error("class=BaseESDAO||method=getDirectResponse||clusterName={}||errMsg=esClient is null",
                clusterName);
            DirectResponse directResponse = new DirectResponse();
            directResponse.setRestStatus(RestStatus.SERVICE_UNAVAILABLE);
            return directResponse;
        }

        DirectRequest directRequest = new DirectRequest(methodType, url);
        return esClient.direct(directRequest).actionGet(30, TimeUnit.SECONDS);
    }
}
