package com.didiglobal.logi.op.manager.infrastructure.deployment;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author didi
 * @date 2022-07-14 3:13 下午
 */
@Data
public class ZeusTask {
    @JSONField(name = "tpl_id")
    private Integer templateId;
    private String hosts;
    private String args;
    private String action;
}
