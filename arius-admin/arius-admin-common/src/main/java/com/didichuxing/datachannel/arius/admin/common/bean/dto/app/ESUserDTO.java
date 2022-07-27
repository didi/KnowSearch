package com.didichuxing.datachannel.arius.admin.common.bean.dto.app;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author d06679
 * @date 2019/3/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "es user 信息")
public class ESUserDTO extends BaseDTO {

    @ApiModelProperty("es user name")
    private Integer id;

    @ApiModelProperty("root用户")
    private Integer isRoot = 0;

    @ApiModelProperty("验证码")
    private String  verifyCode;

   

    @ApiModelProperty("备注")
    private String  memo;

    @ApiModelProperty("删除标记（1:正常；0：删除）")
    private Integer isActive;

    @ApiModelProperty("限流值")
    private Integer queryThreshold;

    @ApiModelProperty("查询集群")
    private String  cluster;

    @ApiModelProperty("查询模式（0:集群模式；1:索引模式）")
    private Integer searchType;

    @ApiModelProperty("数据中心")
    private String  dataCenter;
    @ApiModelProperty("项目id")
    private Integer projectId;

}