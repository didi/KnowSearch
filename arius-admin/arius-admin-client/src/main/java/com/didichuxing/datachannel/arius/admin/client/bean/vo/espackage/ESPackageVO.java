package com.didichuxing.datachannel.arius.admin.client.bean.vo.espackage;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author lyn
 * @date 2021-01-12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "ES集群安装包VO")
public class ESPackageVO extends BaseVO {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("镜像地址或包地址")
    private String url;

    @ApiModelProperty("版本标识")
    private String esVersion;

    @ApiModelProperty("包创建人")
    private String creator;

    @ApiModelProperty("是否为发布版本")
    private Boolean release;

    @ApiModelProperty("类型(3 docker/4 host)")
    private Integer manifest;

    @ApiModelProperty("备注")
    private String desc;

    @ApiModelProperty("标记删除")
    private Boolean deleteFlag;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("类型(1 滴滴内部版本/2 开源版本)")
    private Integer packageType;
}
