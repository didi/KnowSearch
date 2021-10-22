package com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "逻辑集群状态信息")
public class ConsoleClusterStatusVO extends BaseVO {
    /**
     * 逻辑集群名称
     */
    @ApiModelProperty("逻辑集群名称")
    private String name;

    /**
     * 逻辑集群id
     */
    @ApiModelProperty("逻辑集群id")
    private Long id;

    /**
     * 逻辑集群状态
     */
    @ApiModelProperty("逻辑集群状态(green/yellow/red)")
    private String status;

    /**
     * 集群docNu个数
     */
    @ApiModelProperty("集群docNu个数")
    private Integer docNu;

    /**
     * 总的磁盘容量
     */
    @ApiModelProperty("总的磁盘容量")
    private Double totalDisk;

    /**
     * 已使用的磁盘容量
     */
    @ApiModelProperty("已使用的磁盘容量")
    private Double usedDisk;

    /**
     * 索引数量
     */
    @ApiModelProperty("索引数量")
    private Long indexNu;
}
