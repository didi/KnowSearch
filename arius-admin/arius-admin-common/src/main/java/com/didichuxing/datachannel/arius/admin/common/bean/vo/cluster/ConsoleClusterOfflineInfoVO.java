package com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ConsoleAppVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "逻辑集群状态信息")
public class ConsoleClusterOfflineInfoVO {
    private long        id;

    @ApiModelProperty("总的磁盘容量")
    private double      totalDisk;

    @ApiModelProperty("已使用的磁盘容量")
    private double      usedDisk;

    @ApiModelProperty("集群写入tps")
    private long        writeTps;

    @ApiModelProperty("集群查询qps")
    private long        qureyQps;

    @ApiModelProperty("索引数量")
    private long        indexNu;

    /**
     * 最近一段时间有访问的app
     */
    @ApiModelProperty("访问应用列表")
    private List<ConsoleAppVO> accessApps;
}
