package com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.app.ConsoleAppVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "逻辑集群状态信息")
public class ConsoleClusterOfflineInfoVO {
    private long        id;
    /**
     * 总的磁盘容量
     */
    @ApiModelProperty("总的磁盘容量")
    private double      totalDisk;

    /**
     * 已使用的磁盘容量
     */
    @ApiModelProperty("已使用的磁盘容量")
    private double      usedDisk;

    /**
     * 集群写入tps
     */
    @ApiModelProperty("集群写入tps")
    private long        writeTps;

    /**
     * 集群查询qps
     */
    @ApiModelProperty("集群查询qps")
    private long        qureyQps;

    /**
     * 索引数量
     */
    @ApiModelProperty("索引数量")
    private long        indexNu;

    /**
     * 最近一段时间有访问的app
     */
    @ApiModelProperty("访问应用列表")
    private List<ConsoleAppVO> accessApps;
}
