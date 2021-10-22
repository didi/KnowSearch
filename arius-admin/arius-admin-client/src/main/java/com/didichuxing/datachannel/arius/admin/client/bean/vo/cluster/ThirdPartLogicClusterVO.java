package com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ResourceLogicTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "逻辑集群信息")
public class ThirdPartLogicClusterVO extends BaseVO {
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
    @ApiModelProperty("类型(1:公共：2:独立)")
    private Integer type;

    /**
     * 所属APPID
     */
    @ApiModelProperty("所属应用ID")
    private Integer appId;

    /**
     * 数据中心
     */
    @ApiModelProperty("数据中心")
    private String  dataCenter;

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
     * 独立资源的大小
     */
    @ApiModelProperty("配额")
    private Double  quota;

    /**
     * 服务等级
     */
    @ApiModelProperty("服务等级")
    private Integer level;

    /**
     * 逻辑集群映射信息
     */
    @ApiModelProperty("逻辑集群映射信息")
    private List<LogicClusterRackVO> items;
}
