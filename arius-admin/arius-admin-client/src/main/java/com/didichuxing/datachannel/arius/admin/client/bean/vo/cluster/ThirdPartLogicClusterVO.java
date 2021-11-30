package com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ResourceLogicTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "逻辑集群信息")
public class ThirdPartLogicClusterVO extends BaseVO {

    @ApiModelProperty("逻辑集群ID")
    private Long    id;

    @ApiModelProperty("逻辑集群名字")
    private String  name;

    /**
     * 类型
     * @see ResourceLogicTypeEnum
     */
    @ApiModelProperty("类型(1:公共：2:独立)")
    private Integer type;

    @ApiModelProperty("所属应用ID")
    private Integer appId;

    @ApiModelProperty("数据中心")
    private String  dataCenter;

    @ApiModelProperty("责任人")
    private String  responsible;

    @ApiModelProperty("成本部门ID")
    private String  libraDepartmentId;

    @ApiModelProperty("成本部门名称")
    private String  libraDepartment;

    @ApiModelProperty("备注")
    private String  memo;

    @ApiModelProperty("配额")
    private Double  quota;

    @ApiModelProperty("服务等级")
    private Integer level;

    @ApiModelProperty("逻辑集群映射信息")
    private List<LogicClusterRackVO> items;
}
