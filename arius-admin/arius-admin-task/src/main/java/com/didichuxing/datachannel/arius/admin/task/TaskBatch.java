package com.didichuxing.datachannel.arius.admin.task;

import java.util.List;

import com.google.common.collect.Lists;

import lombok.Data;

/**
 * @author d06679
 * @date 2019/4/10
 */
@Data
public class TaskBatch {
    /**
     * 批次中需要处理的内容
     */
    private List items = Lists.newArrayList();
}
