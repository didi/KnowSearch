package com.didichuxing.datachannel.arius.admin.core.notify.service;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.core.notify.NotifyInfo;
import com.didichuxing.datachannel.arius.admin.core.notify.NotifyTaskTypeEnum;

/**
 * @author didi
 */
public interface NotifyService {

    /**
     * 同步发送通知
     * @param type 发送任务类型
     * @param data 发送数据
     * @param receivers 接收人列表
     * @return
     */
    Result<Void> send(NotifyTaskTypeEnum type, NotifyInfo data, List<String> receivers);

    /**
     * 异步发送通知
     * @param type 发送任务类型
     * @param data 发送数据
     * @param receivers 接收人列表
     * @return
     */
    void sendAsync(NotifyTaskTypeEnum type, NotifyInfo data, List<String> receivers);

}
