package com.didichuxing.datachannel.arius.admin.common.bean.dto.task;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author d06679
 * @date 2018/10/25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "任务处理信息")
public class WorkTaskProcessDTO extends BaseDTO {

    @ApiModelProperty("任务id")
    private Integer              taskId;

    /**
     * 执行状态
     *   success     成功
     *   failed      失败
     *   running    执行中
     *   waiting     等待
     *   cancel      取消
     *   pause      暂停
     */
    @ApiModelProperty("执行状态")
    private String              status;

    @ApiModelProperty("任务步骤")
    private Integer              taskProgress;

    @ApiModelProperty("扩展信息")
    private String expandData;
}
