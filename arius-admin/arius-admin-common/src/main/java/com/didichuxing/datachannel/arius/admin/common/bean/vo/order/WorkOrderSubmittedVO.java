package com.didichuxing.datachannel.arius.admin.common.bean.vo.order;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zengqiao
 * @date 20/10/20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "工单创建完成")
public class WorkOrderSubmittedVO extends BaseVO {
    @ApiModelProperty(value = "工单ID")
    private Long id;

    @ApiModelProperty(value = "工单标题")
    private String title;
}