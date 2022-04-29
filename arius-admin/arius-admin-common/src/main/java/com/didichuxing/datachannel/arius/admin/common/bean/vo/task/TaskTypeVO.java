package com.didichuxing.datachannel.arius.admin.common.bean.vo.task;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author fengqiongfeng
 * @date 2020/12/21
 */
@Data
public class TaskTypeVO extends BaseVO {
    @ApiModelProperty(value = "任务类型")
    private Integer type;

    @ApiModelProperty(value = "描述信息")
    private String message;

    public TaskTypeVO(Integer type, String message) {
        this.type = type;
        this.message = message;
    }
}
