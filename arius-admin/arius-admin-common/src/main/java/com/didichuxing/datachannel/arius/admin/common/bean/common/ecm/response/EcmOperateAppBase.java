package com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.response;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;
import lombok.Data;

/**
 * @author zengqiao
 * @date 20/10/29
 */
@Data
public class EcmOperateAppBase extends BaseDTO {
    protected Integer taskId;

    public EcmOperateAppBase() {
    }

    public EcmOperateAppBase(Integer taskId) {
        this.taskId = taskId;
    }

}