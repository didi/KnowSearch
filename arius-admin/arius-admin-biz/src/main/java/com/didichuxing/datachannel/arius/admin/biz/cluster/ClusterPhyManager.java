package com.didichuxing.datachannel.arius.admin.biz.cluster;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ConsoleClusterPhyVO;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import java.util.List;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESClusterDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESClusterJoinDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ESClusterPhyRegionInfoVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ESClusterPhyVO;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ResourceLogicTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterStatis;
import org.springframework.transaction.annotation.Transactional;

public interface ClusterPhyManager {

    /**
     * 对集群下所有模板执行拷贝索引的mapping到模板操作
     * @param cluster 集群
     * @param retryCount 重试次数
     * @return true/false
     */
    boolean copyMapping(String cluster, int retryCount);

    /**
     * 同步元数据
     * @param cluster    集群名称
     * @param retryCount 重试次数
     * @return
     */
    boolean syncTemplateMetaData(String cluster, int retryCount);

    /**
     * 物理集群资源使用率
     * @param cluster 物理集群
     * @return
     */
    ESClusterStatis getClusterResourceUsage(String cluster);

    /**
     * 获取物理集群状态信息
     * @param cluster 集群名称
     * @return
     */
    ESClusterStatis getClusterStatus(String cluster);

    /**
     * 根据指定物理集群和Racks获取对应状态信息
     * @param cluster 物理集群
     * @return
     */
    ESClusterStatis getPhyClusterStatus(String cluster);

    /**
     * 集群是否存在
     * @param clusterName 集群名字
     * @return true 存在
     */

    boolean isClusterExists(String clusterName);

    /**
     * 释放racks
     * @param cluster    集群名称
     * @param racks      要释放的racks，逗号分隔
     * @param retryCount 重试次数
     * @return result
     */
    Result releaseRacks(String cluster, String racks, int retryCount);

    /**
     * 获取控制台物理集群信息列表
     * @param currentAppId 当前登录项目
     */
    List<ConsoleClusterPhyVO> getConsoleClusterPhyVOS(ESClusterDTO param, Integer currentAppId);

    /**
     * 获取单个控制台物理集群信息
     * @param currentAppId 当前登录项目
     */
    ConsoleClusterPhyVO getConsoleClusterPhyVO(Integer clusterId, Integer currentAppId);

    /**
     * 获取物理集群节点划分信息
     */
    Result<List<ESClusterPhyRegionInfoVO>> getClusterPhyRegionInfos(Integer clusterPyhId);

    /**
     * 获取可用物理集群列表
     * @param clusterLogicType 逻辑集群类型
     * @see ResourceLogicTypeEnum
     */
	Result<List<String>> listCanBeAssociatedClustersPhys(Integer clusterLogicType);

    /**
     * 集群接入
     */
	Result clusterJoin(ESClusterJoinDTO param, String operator);

    /**
     * 校验节点ip是否有效
     */
    Result checkValidForClusterNodes(List<String> ips);

    /**
     * 删除接入集群
     * 删除顺序: region ——> clusterLogic ——> clusterHost ——> clusterRole  ——> cluster
     */
    Result deleteClusterJoin(Integer clusterId, String operator);
}
