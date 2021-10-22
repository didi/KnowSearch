package com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;
import lombok.Data;

@Data
public class ESAppGroupStatusDTO extends BaseDTO {

    /**
     * 当前分组的index
     */
    private Integer index;

    /**
     * 当前分组总共的容器数
     */
    private Integer desired;

    /**
     * 当前更新容器组内index
     */
    private Integer current;

    /**
     * 当前分组的状态
     */
    private String  status;

}
