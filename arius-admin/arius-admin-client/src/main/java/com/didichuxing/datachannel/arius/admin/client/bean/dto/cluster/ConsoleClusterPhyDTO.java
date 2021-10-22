package com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster;

import com.didichuxing.datachannel.arius.admin.client.constant.app.AppLogicClusterAuthEnum;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

/**
 * Created by linyunan on 2021-06-17
 */
@Data
@ApiOperation("控制台物理集群信息")
public class ConsoleClusterPhyDTO extends ESClusterDTO {
    @ApiModelProperty("归属项目(App)Id")
    private Integer belongAppId;

    @ApiModelProperty("归属项目名称")
    private String  belongAppName;

    /** @see AppLogicClusterAuthEnum */
    @ApiModelProperty("当前App对集群的权限 1:配置管理, 2:访问, -1:无权限")
    private Integer currentAppAuth;

    @ApiModelProperty("gateway地址")
    private String  gatewayAddress;

    @ApiModelProperty("负责人")
    private String  responsible;
}
