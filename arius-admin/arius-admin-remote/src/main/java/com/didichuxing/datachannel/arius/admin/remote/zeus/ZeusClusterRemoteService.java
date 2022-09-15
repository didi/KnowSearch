package com.didichuxing.datachannel.arius.admin.remote.zeus;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.response.EcmOperateAppBase;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.response.EcmSubTaskLog;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.response.EcmTaskStatus;

public interface ZeusClusterRemoteService {
    Result<EcmOperateAppBase> createTask(List<String> hostList, String args);

    /**
     * 部署 or 继续部署 宙斯集群
     * @param taskId  宙斯任务Id
     * @param action  部署动作
     * start    开始
     * pause    暂停createTask
     * kill     结束
     * cancel   撤销
     *
     * @return Result
     */
    Result<Object> actionTask(Integer taskId, String action);

    /**
     * 部署 or 继续部署 宙斯集群
     * @param taskId  宙斯任务Id
     * @param hostname 主机名称
     * @param action  部署动作
     * @return Result
     */
    Result<Object> actionHostTask(Integer taskId, String hostname, String action);

    /**
     *  获取 宙斯任务部署 结果
     * @param taskId  宙斯任务Id
     * @return Result
     */
    Result<List<EcmTaskStatus>> getZeusTaskStatus(Integer taskId);

    /**
     *  获取 宙斯任务部署 日志
     * @param taskId   宙斯任务Id
     * @param hostname 主机名称
     * @return Result
     */
    Result<EcmSubTaskLog> getTaskLog(Integer taskId, String hostname);

    /**
     *  获取 宙斯部署 ip列表
     * @return Result
     */
    Result<List<String>> getAgentsList();

}
