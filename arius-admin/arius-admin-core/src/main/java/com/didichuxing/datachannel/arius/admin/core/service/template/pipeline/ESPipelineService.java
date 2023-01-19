package com.didichuxing.datachannel.arius.admin.core.service.template.pipeline;

import java.util.Map;

import com.didichuxing.datachannel.arius.admin.common.bean.common.ESPipelineProcessor;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.ESPipeline;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didiglobal.knowframework.elasticsearch.client.request.ingest.Pipeline;

/**
 * espipelineservice
 *
 * @author shizeying
 * @date 2022/08/12
 */
public interface ESPipelineService {
    /**
     * 保存
     *
     * @param esPipeline ES管道
     * @param methodName 方法名称
     * @param tryTimes   试次
     * @return boolean
     * @throws ESOperateException esoperateException
     */
    boolean save(String methodName,ESPipeline esPipeline,int tryTimes) throws ESOperateException;
    
    /**
     * 获取
     *
     * @param cluster 集群
     * @param name    名字
     * @return {@link ESPipelineProcessor}
     */
    ESPipelineProcessor get(String cluster, String name);

    /**
     * 获取集群的全量pipeline
     * @param cluster 集群
     * @return
     */
    Map<String, Pipeline> getClusterPipelines(String cluster) throws ESOperateException;

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
    boolean delete(String cluster, String name,String methodName,int tryTimes) throws ESOperateException;
}