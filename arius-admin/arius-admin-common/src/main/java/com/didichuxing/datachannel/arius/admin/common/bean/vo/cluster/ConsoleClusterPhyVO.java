package com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Deprecated 这里由泽颖处理
 *
 * @author linyunan
 * @date 2021-06-16
 */
@Deprecated
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("控制台物理集群信息")
public class ConsoleClusterPhyVO extends ClusterPhyVO {

    @ApiModelProperty("归属项目(Project)Id列表")
    private List<Integer> belongProjectIds;

	@ApiModelProperty("归属项目(App)Id, 保留兼容客户端")
    private Integer belongProjectId;

    @ApiModelProperty("归属项目名称列表")
    private List<String> belongProjectNames;

	@ApiModelProperty("归属项目名称, 保留兼容客户端")
    private String belongProjectName;

    @ApiModelProperty("当前App对集群的权限 1:配置管理, 2:访问, -1:无权限")
    private Integer       currentAppAuth;

    @ApiModelProperty("gateway地址")
    private String        gatewayAddress;

    @ApiModelProperty("负责人")
    private String        responsible;
}