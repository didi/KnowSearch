package com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.App;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class ESClusterLogicOfflineInfo extends BaseEntity {
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
    private List<App>   accessApps;
}
