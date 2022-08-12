package com.didichuxing.datachannel.arius.admin.core.service.template.dcdr;

import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESOpTimeoutRetry;
import com.didichuxing.datachannel.arius.admin.persistence.es.cluster.ESDCDRDAO;
import com.didiglobal.logi.elasticsearch.client.request.dcdr.DCDRTemplate;
import com.google.common.collect.Sets;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * esdcdrservice实现
 *
 * @author shizeying
 * @date 2022/08/12
 */
@Service
public class ESDCDRServiceImpl implements ESDCDRService {
    @Autowired
    private ESDCDRDAO esDCDRDAO;
    
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
    @Override
    public boolean put(String methodName, String cluster, String name, String template,
                       String replicaCluster, int tryTimes) throws ESOperateException {
        return ESOpTimeoutRetry.esRetryExecute(methodName, tryTimes,
                () -> esDCDRDAO.putAutoReplication(cluster, name, template, replicaCluster));
    }
    
    @Override
    public boolean delete(String methodName, String cluster, String name, int tryTimes) throws ESOperateException {
        return ESOpTimeoutRetry.esRetryExecute(methodName, tryTimes,
                () -> esDCDRDAO.deleteAutoReplication(cluster, name));
    }
    
    /**
     * 删除
     *
     * @param methodName     方法名称
     * @param cluster        集群
     * @param replicaCluster 集群复制
     * @param indices        索引集合
     * @param tryTimes       重试次数
     * @return boolean
     */
    @Override
    public boolean delete(String methodName, String cluster, String replicaCluster, Set<String> indices, int tryTimes)
            throws ESOperateException {
        return ESOpTimeoutRetry.esRetryExecute(methodName, tryTimes,
                () -> esDCDRDAO.deleteReplication(cluster, replicaCluster, Sets.newHashSet(indices)));
    }
    
    /**
     * 获取
     *
     * @param cluster 集群
     * @param name    名字
     * @return {@link DCDRTemplate}
     * @throws ESOperateException esoperateException
     */
    @Override
    public DCDRTemplate get(String cluster, String name) throws ESOperateException {
        return esDCDRDAO.getAutoReplication(cluster, name);
    }
    
    /**
     * 存在
     *
     * @param cluster 集群
     * @param name    名字
     * @return boolean
     * @throws ESOperateException esoperateException
     */
    @Override
    public boolean exist(String cluster, String name) throws ESOperateException {
        return get(cluster, name) != null;
    }
}