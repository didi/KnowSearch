package com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ESAppGroupStatusDTO extends BaseDTO {

    private static final long serialVersionUID = 4201599450177941491L;
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
