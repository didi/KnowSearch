package com.didichuxing.datachannel.arius.admin.client.bean.vo.ecm;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author lyn
 * @date 2020-12-30
 */
@Data
@ApiModel(description = "ES集群配置VO")
public class ESConfigVO extends BaseVO {
    /**
     * ID主键自增
     */
    @ApiModelProperty("主键")
    private Long id;

    /**
     * 集群id
     */
    @ApiModelProperty("集群id")
    private Long clusterId;

    /**
     * 配置文件名称
     */
    @ApiModelProperty("配置文件名称")
    private String typeName;

    /**
     * 角色名称
     */
    @ApiModelProperty("组件名称")
    private String enginName;

    /**
     * 配置内容
     */
    @ApiModelProperty("配置内容")
    private String configData;

    /**
     * 配置描述
     */
    @ApiModelProperty("配置描述")
    private String desc;

    /**
     * 配置tag
     */
    @ApiModelProperty("配置tag")
    private String versionTag;
}
