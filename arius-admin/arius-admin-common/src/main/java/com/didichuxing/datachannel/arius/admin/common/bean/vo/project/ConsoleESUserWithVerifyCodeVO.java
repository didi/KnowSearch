package com.didichuxing.datachannel.arius.admin.common.bean.vo.project;

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
@ApiModel(description = "es user 信息（包含验证码）")
public class ConsoleESUserWithVerifyCodeVO extends BaseVO {

    @ApiModelProperty("es user ")
    private Integer id;

    @ApiModelProperty("应用名称")
    private String  name;

    @ApiModelProperty("验证码")
    private String  verifyCode;

    @ApiModelProperty("部门ID")
    @Deprecated
    private String  departmentId;

    @ApiModelProperty("部门名称")
    @Deprecated
    private String  department;

    @ApiModelProperty("责任人")
    private String  responsible;

    @ApiModelProperty("备注")
    private String  memo;

    @ApiModelProperty("查询限流值")
    private Integer queryThreshold;

    @ApiModelProperty("数据中心")
    private String dataCenter;
}