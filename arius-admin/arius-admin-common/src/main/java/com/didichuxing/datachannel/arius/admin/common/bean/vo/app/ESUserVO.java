package com.didichuxing.datachannel.arius.admin.common.bean.vo.app;

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
@ApiModel(description = "es user信息")
public class ESUserVO extends BaseVO {

    @ApiModelProperty("es user")
    private Integer id;



    @ApiModelProperty("root用户")
    private Integer isRoot;

    @ApiModelProperty("验证码")
    private String  verifyCode;

    @ApiModelProperty("备注")
    private String  memo;

    @ApiModelProperty("限流值")
    private Integer queryThreshold;

    @ApiModelProperty("查询集群")
    private String  cluster;

    @ApiModelProperty("查询模式（0:集群模式；1:索引模式, 3:原生模式）")
    private Integer searchType;
     @ApiModelProperty("项目默认使用的es user")
     private Boolean defaultDisplay;

}