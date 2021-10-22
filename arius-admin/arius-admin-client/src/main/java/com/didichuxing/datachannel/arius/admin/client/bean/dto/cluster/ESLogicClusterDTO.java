package com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ResourceLogicTypeEnum;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author d06679
 * @date 2019/3/22
 */
@Data
@ApiModel(description ="逻辑集群信息")
public class ESLogicClusterDTO extends BaseDTO {

    /**
     * 主键
     */
    @ApiModelProperty("逻辑集群ID")
    private Long    id;

    /**
     * 名字
     */
    @ApiModelProperty("逻辑集群名字")
    private String  name;

    /**
     * 类型
     * @see ResourceLogicTypeEnum
     */
    @ApiModelProperty("类型(1:公共：2:独立, 3:独占)")
    private Integer type;

    /**
     * 所属APP ID
     */
    @ApiModelProperty("所属应用ID")
    private Integer appId;

    /**
     * 数据中心
     */
    @ApiModelProperty("数据中心")
    private String  dataCenter;

    /**
     * dataNode的个数
     */
    @ApiModelProperty("数据节点个数")
    private Integer  dataNodeNu;

    /**
     * 责任人
     */
    @ApiModelProperty("责任人")
    private String  responsible;

    /**
     * 成本部门
     */
    @ApiModelProperty("成本部门ID")
    private String  libraDepartmentId;

    /**
     * 成本部门
     */
    @ApiModelProperty("成本部门名称")
    private String  libraDepartment;

    /**
     * 备注
     */
    @ApiModelProperty("备注")
    private String  memo;

    /**
     * 服务等级
     */
    @ApiModelProperty("服务等级")
    private Integer level;

    /**
     * 独立资源的大小
     */
    @ApiModelProperty("配额")
    private Double  quota;

    /**
     * 配置
     */
    @ApiModelProperty("配置")
    private String  configJson;
}
