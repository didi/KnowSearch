package com.didichuxing.datachannel.arius.admin.common.bean.vo.ecm;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lyn
 * @date 2020-12-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "ES集群配置VO")
public class ESConfigVO extends BaseVO {

    @ApiModelProperty("主键")
    private Long   id;

    @ApiModelProperty("集群id")
    private Long   clusterId;

    @ApiModelProperty("配置文件名称")
    private String typeName;

    @ApiModelProperty("组件名称")
    private String enginName;

    @ApiModelProperty("配置内容")
    private String configData;

    @ApiModelProperty("配置描述")
    private String desc;

    @ApiModelProperty("配置tag")
    private String versionTag;
}
