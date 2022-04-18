package com.didichuxing.datachannel.arius.admin.common.bean.vo.app;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户控制台使用
 *
 * @author d06679
 * @date 2019/3/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "app信息")
public class ConsoleAppVO extends BaseVO {

    @ApiModelProperty("应用")
    private Integer id;

    @ApiModelProperty("应用名称")
    private String  name;

    @ApiModelProperty("部门ID")
    private String  departmentId;

    @ApiModelProperty("部门名称")
    private String  department;

    @ApiModelProperty("责任人")
    private String  responsible;

    @ApiModelProperty("备注")
    private String  memo;

    @ApiModelProperty("查询限流值")
    private Integer queryThreshold;

    @ApiModelProperty("数据中心")
    private String  dataCenter;

}
