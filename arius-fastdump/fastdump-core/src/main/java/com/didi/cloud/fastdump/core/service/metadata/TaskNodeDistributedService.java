package com.didi.cloud.fastdump.core.service.metadata;

import java.util.List;
import java.util.Map;

/**
 * Created by linyunan on 2022/9/6
 * 任务在各个ip节点上的执行分布服务
 */
public interface TaskNodeDistributedService {
    boolean putTaskIpList(String taskId, List<String> ipList);

    List<String> getTaskIpList(String taskId);

    boolean removeTaskIpList(String taskId);

    Map<String, List<String>> listAllTaskIpListMap();
}
