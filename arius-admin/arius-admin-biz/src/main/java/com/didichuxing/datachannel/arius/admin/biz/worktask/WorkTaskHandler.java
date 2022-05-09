package com.didichuxing.datachannel.arius.admin.biz.worktask;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.AriusOpTask;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;

/**
 * @author d06679
 * @date 2020/12/21
 */
public interface WorkTaskHandler extends BaseHandle {

    /**
     * 创建一个任务
     *
     * 1、校验任务内容是否合法
     * 2、提交任务
     * @param ariusOpTask 任务数据
     * @return result
     */
    Result<AriusOpTask> addTask(AriusOpTask ariusOpTask);
    
    /**存在联合国关闭任务
     * 判断一个任务是否存在，参数待定
     *
     @param key key
     @param type 类型
     @return boolean
     */
    boolean existUnClosedTask(String key, Integer type);
    
    /**过程
     * 处理任务
     * @param ariusOpTask 任务
     * @return result
     @param step 一步
     @param status 状态
     @param expandData 扩展数据
     */
    Result<Void> process(AriusOpTask ariusOpTask, Integer step, String status, String expandData);



}