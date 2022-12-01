package com.didi.cloud.fastdump.common.bean.readrate;

import com.didi.cloud.fastdump.common.bean.BaseEntity;

import lombok.Data;

/**
 * Created by linyunan on 2022/9/22
 */
@Data
public class ReadFileRateInfo extends BaseEntity {
    private String taskId;
    /**
     * 读取文档流控
     */
    private Double readFileRateLimit;
}
