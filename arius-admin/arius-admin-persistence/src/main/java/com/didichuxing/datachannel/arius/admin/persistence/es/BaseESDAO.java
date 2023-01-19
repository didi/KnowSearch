package com.didichuxing.datachannel.arius.admin.persistence.es;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Autowired;

import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.NullESClientException;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ParsingExceptionUtils;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESGatewayClient;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESOpClient;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESUpdateClient;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.datacentre.DataCentreUtil;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dsls.DslLoaderUtil;
import com.didiglobal.knowframework.elasticsearch.client.ESClient;
import com.didiglobal.knowframework.elasticsearch.client.gateway.direct.DirectRequest;
import com.didiglobal.knowframework.elasticsearch.client.gateway.direct.DirectResponse;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.google.common.collect.Lists;

/**
 * 直接操作es集群的dao
 */
public class BaseESDAO {
    protected static final ILog LOGGER = LogFactory.getLog(BaseESDAO.class);
    public static final String REASON    = "reason";
    public static final String ERROR     = "error";
    public static final String TYPE      = "type";
    public static final String CAUSED_BY = "caused_by";

    /**
     * 索引名数据中心加载工具类
     */
    @Autowired
    protected DataCentreUtil    dataCentreUtil;
    /**
     * 加载查询语句工具类
     */
    @Autowired
    protected DslLoaderUtil     dslLoaderUtil;
    /**
     * 查询es客户端
     */
    @Autowired
    protected ESGatewayClient   gatewayClient;
    /**
     * 更新es客户端
     */
    @Autowired
    protected ESUpdateClient    updateClient;

    /**
     * Arius操作es集群的client
     */
    @Autowired
    protected ESOpClient        esOpClient;

    public DirectResponse getDirectResponse(String clusterName, String methodType, String url) throws ESOperateException {
        ESClient esClient = esOpClient.getESClient(clusterName);
        DirectResponse directResponse = new DirectResponse();
        if (esClient == null) {
            LOGGER.error("class=BaseESDAO||method=getDirectResponse||clusterName={}||errMsg=esClient is null",
                clusterName);
            throw new NullESClientException(clusterName);
        }

        DirectRequest directRequest = new DirectRequest(methodType, url);
        try {
            return esClient.direct(directRequest).actionGet(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOGGER.error("class=BaseESDAO||method=getDirectResponse||clusterName={}||errMsg=fail to direct",
                clusterName, e.getMessage(), e);
            ParsingExceptionUtils.abnormalTermination(e);
        }
        directResponse.setRestStatus(RestStatus.SERVICE_UNAVAILABLE);
        return directResponse;
    }

    public <T> List<T> commonGet(String clusterName, String directRequestContent, Class<T> clazz) throws ESOperateException {
        DirectResponse directResponse = getDirectResponse(clusterName, "Get", directRequestContent);
        List<T> list = Lists.newArrayList();
        if (directResponse.getRestStatus() == RestStatus.OK
            && StringUtils.isNoneBlank(directResponse.getResponseContent())) {
            try {
                List<T> resList = ConvertUtil.str2ObjArrayByJson(directResponse.getResponseContent(), clazz);
                list.addAll(resList);
            } catch (Exception e) {
                LOGGER.error("class=BaseESDAO||method=commonGet||cluster={}||directRequestContent={}||"
                             + "clazzName={}||errMsg=str convert obj error:{}",
                    clusterName, directRequestContent, clazz.getName(), e.getMessage(), e);
                ParsingExceptionUtils.abnormalTermination(e);
            }
        }
        return list;
    }
    

    
}