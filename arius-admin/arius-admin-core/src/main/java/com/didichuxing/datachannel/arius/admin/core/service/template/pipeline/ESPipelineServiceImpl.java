package com.didichuxing.datachannel.arius.admin.core.service.template.pipeline;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.common.bean.common.ESPipelineProcessor;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.ESPipeline;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESOpTimeoutRetry;
import com.didichuxing.datachannel.arius.admin.persistence.es.cluster.ESPipelineDAO;
import com.didiglobal.knowframework.elasticsearch.client.request.ingest.Pipeline;

/**
 * espipelineservice实现
 *
 * @author shizeying
 * @date 2022/08/12
 */
@Service
public class ESPipelineServiceImpl implements ESPipelineService {
    @Autowired
    private ESPipelineDAO esPipelineDAO;
    
  
    @Override
    public boolean save(String methodName, ESPipeline esPipeline, int tryTimes) throws ESOperateException {
        return ESOpTimeoutRetry.esRetryExecute(methodName, tryTimes,
                () -> esPipelineDAO.save(esPipeline.getCluster(), esPipeline.getPipelineId(), esPipeline.getDateField(),
                        esPipeline.getDateFieldFormat(), esPipeline.getDateFormat(), esPipeline.getExpireDay(),
                        esPipeline.getRateLimit(), esPipeline.getVersion(), esPipeline.getIdField(),
                        esPipeline.getRoutingField()));
    }
    
    /**
     * 获取
     *
     * @param cluster 集群
     * @param name    名字
     * @return {@link ESPipelineProcessor}
     */
    @Override
    public ESPipelineProcessor get(String cluster, String name) {
        return esPipelineDAO.get(cluster,name);
    }

    /**
     * 获取集群的全量pipeline
     * @param cluster 集群
     * @return
     */
    @Override
    public Map<String, Pipeline> getClusterPipelines(String cluster) throws ESOperateException {
        return esPipelineDAO.getClusterPipelines(cluster);
    }

    /**
     * 删除
     *
     * @param cluster    集群
     * @param name       名字
     * @param methodName 方法名称
     * @param tryTimes   试次
     * @return boolean
     * @throws ESOperateException esoperateException
     */
    @Override
    public boolean delete(String cluster, String name, String methodName, int tryTimes) throws ESOperateException {
        return ESOpTimeoutRetry.esRetryExecute(methodName, tryTimes,
                () -> esPipelineDAO.delete(cluster, name));
    }
}