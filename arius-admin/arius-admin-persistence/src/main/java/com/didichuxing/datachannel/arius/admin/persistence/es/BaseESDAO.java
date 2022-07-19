package com.didichuxing.datachannel.arius.admin.persistence.es;

import static com.didichuxing.datachannel.arius.admin.persistence.constant.ESOperateConstant.ES_OPERATE_MIN_TIMEOUT;

import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
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
import com.google.common.collect.Lists;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 直接操作es集群的dao
 */
public class BaseESDAO {
    protected static final ILog LOGGER = LogFactory.getLog(BaseESDAO.class);

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

    public DirectResponse getDirectResponse(String clusterName, String methodType, String url) {
        ESClient esClient = esOpClient.getESClient(clusterName);
        DirectResponse directResponse = new DirectResponse();
        if (esClient == null) {
            LOGGER.error("class=BaseESDAO||method=getDirectResponse||clusterName={}||errMsg=esClient is null",
                clusterName);
            directResponse.setRestStatus(RestStatus.SERVICE_UNAVAILABLE);
            return directResponse;
        }

        DirectRequest directRequest = new DirectRequest(methodType, url);
        try {
            return esClient.direct(directRequest).actionGet(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOGGER.error("class=BaseESDAO||method=getDirectResponse||clusterName={}||errMsg=esClient is null",
                clusterName, e.getMessage(), e);
            directResponse.setRestStatus(RestStatus.SERVICE_UNAVAILABLE);
            return directResponse;
        }
    }

    public <T> List<T> commonGet(String clusterName, String directRequestContent, Class<T> clazz) {
        DirectResponse directResponse = getDirectResponse(clusterName, "Get", directRequestContent);
        List<T> list = Lists.newArrayList();
        if (directResponse.getRestStatus() == RestStatus.OK
            && StringUtils.isNoneBlank(directResponse.getResponseContent())) {
            try {
                List<T> resList = ConvertUtil.str2ObjArrayByJson(directResponse.getResponseContent(), clazz);
                list.addAll(resList);
            } catch (Exception e) {
                LOGGER.error("class=BaseESDAO||method=commonGet||cluster={}||directRequestContent={}||"
                             + "clazzName={}||errMst=str convert obj error:{}",
                    clusterName, directRequestContent, clazz.getName(), e.getMessage(), e);
            }
        }
        return list;
    }
    
    /**
     * 重试操作的方法体
     *
     * @param tryTimes     试次
     * @param esClientFunc ES客户端函数
     * @param doWhilePredicate   do while的另一种判断模式
     * @return {@code T}
     */
    protected <T> T performTryTimesMethods(BiFunction<Long,TimeUnit,T> esClientFunc,
                                           Predicate<T> doWhilePredicate, Integer tryTimes){
        Long minTimeoutNum = 1L;
        Long maxTimeoutNum = tryTimes.longValue();
        T t = null;
        do {
            t = esClientFunc.apply(/*降低因为抖动导致的等待时常,等待时常从低到高进行重试*/minTimeoutNum * ES_OPERATE_MIN_TIMEOUT,
                    TimeUnit.SECONDS);
            minTimeoutNum++;
            if (minTimeoutNum > maxTimeoutNum) {
                minTimeoutNum = maxTimeoutNum;
            }
        } while (tryTimes-- > 0 && doWhilePredicate.test(t));
        
        return t;
    }
    
}