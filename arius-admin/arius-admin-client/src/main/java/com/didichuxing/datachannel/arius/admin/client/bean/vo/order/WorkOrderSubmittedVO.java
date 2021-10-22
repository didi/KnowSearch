package com.didichuxing.datachannel.arius.admin.client.bean.vo.order;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author zengqiao
 * @date 20/10/20
 */
@Data
@ApiModel(description = "工单创建完成")
public class WorkOrderSubmittedVO extends BaseVO {
    @ApiModelProperty(value = "工单ID")
    private Long id;

    @ApiModelProperty(value = "工单标题")
    private String title;
}