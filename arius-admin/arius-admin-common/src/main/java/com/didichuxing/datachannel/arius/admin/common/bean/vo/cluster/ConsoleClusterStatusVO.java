package com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "逻辑集群状态信息")
public class ConsoleClusterStatusVO extends BaseVO {

    @ApiModelProperty("逻辑集群名称")
    private String  name;

    @ApiModelProperty("逻辑集群id")
    private Long    id;

    @ApiModelProperty("逻辑集群状态(green/yellow/red)")
    private String  status;

    @ApiModelProperty("集群docNu个数")
    private Integer docNu;

    @ApiModelProperty("总的磁盘容量")
    private Double  totalDisk;

    @ApiModelProperty("已使用的磁盘容量")
    private Double  usedDisk;

    @ApiModelProperty("索引数量")
    private Long    indexNu;
}
