package com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster;

import com.didichuxing.datachannel.arius.admin.common.constant.app.AppClusterPhyAuthEnum;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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

    @ApiModelProperty("归属项目(App)Id列表")
    private List<Integer> belongAppIds;

	@ApiModelProperty("归属项目(App)Id, 保留兼容客户端")
    private Integer       belongAppId;

    @ApiModelProperty("归属项目名称列表")
    private List<String>  belongAppNames;

	@ApiModelProperty("归属项目名称, 保留兼容客户端")
    private String        belongAppName;

    /** @see AppClusterPhyAuthEnum */
    @ApiModelProperty("当前App对集群的权限 1:配置管理, 2:访问, -1:无权限")
    private Integer       currentAppAuth;

    @ApiModelProperty("gateway地址")
    private String        gatewayAddress;

    @ApiModelProperty("负责人")
    private String        responsible;
}
