package com.didichuxing.datachannel.arius.admin.core.service.template.dcdr;

import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didiglobal.logi.elasticsearch.client.request.dcdr.DCDRTemplate;
import java.util.Set;

/**
 * esdcdrservice
 *
 * @author shizeying
 * @date 2022/08/12
 */
public interface ESDCDRService {
    /**
     * 将自动复制 put自动复制
     *
     * @param methodName     方法名称
     * @param cluster        集群
     * @param name           名字
     * @param template       template
     * @param replicaCluster 集群复制
     * @param tryTimes       重试次数
     * @return boolean
     */
    boolean put(String methodName, String cluster, String name, String template, String replicaCluster,
                int tryTimes) throws ESOperateException;
    
    /**
     * 删除自动复制
     *
     * @param cluster    集群
     * @param name       名字
     * @param methodName 方法名称
     * @param tryTimes   试次
     * @return boolean
     */
    boolean delete(String methodName, String cluster, String name, int tryTimes) throws ESOperateException;
    
    /**
     * 删除
     *
     * @param cluster        集群
     * @param replicaCluster 集群复制
     * @param indices        索引集合
     * @param methodName     方法名称
     * @param tryTimes       重试次数
     * @return boolean
     */
    boolean delete(String methodName, String cluster, String replicaCluster, Set<String> indices, int tryTimes)
            throws ESOperateException;
    
    /**
     * 获取
     *
     * @param cluster 集群
     * @param name    名字
     * @return {@link DCDRTemplate}
     * @throws ESOperateException esoperateException
     */
    DCDRTemplate get(String cluster, String name) throws ESOperateException;
    
    /**
     * 存在
     *
     * @param cluster 集群
     * @param name    名字
     * @return boolean
     * @throws ESOperateException esoperateException
     */
    boolean exist(String cluster, String name) throws ESOperateException;
}