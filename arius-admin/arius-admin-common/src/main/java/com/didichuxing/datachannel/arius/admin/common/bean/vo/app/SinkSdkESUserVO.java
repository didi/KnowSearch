package com.didichuxing.datachannel.arius.admin.common.bean.vo.app;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 运维控制台使用
 *
 * @author d06679
 * @date 2019/3/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "应用信息（包含权限）")
@Deprecated
public class SinkSdkESUserVO extends BaseVO {

    @ApiModelProperty("应用ID")
    private Integer      id;

    @ApiModelProperty("应用名称")
    private String       name;

    @ApiModelProperty("验证码")
    private String       verifyCode;

    /**
     * 有读权限的索引列表
     */
    @ApiModelProperty("读权限列表")
    private List<String> indexExp;

    /**
     * 有写权限的索引列表
     */
    @ApiModelProperty("读写权限列表")
    private List<String> wIndexExp;

}