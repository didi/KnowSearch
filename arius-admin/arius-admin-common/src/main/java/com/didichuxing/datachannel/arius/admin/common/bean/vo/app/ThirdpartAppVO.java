package com.didichuxing.datachannel.arius.admin.common.bean.vo.app;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 第三方依赖使用
 *
 * @author d06679
 * @date 2019/3/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "应用信息")
public class ThirdpartAppVO extends BaseVO {

    @ApiModelProperty("应用ID")
    private Integer id;

    @ApiModelProperty("应用名字")
    private String  name;

    @ApiModelProperty("部门ID")
    private String  departmentId;

    @ApiModelProperty("部门名称")
    private String  department;

    @ApiModelProperty("责任人")
    private String  responsible;

}
