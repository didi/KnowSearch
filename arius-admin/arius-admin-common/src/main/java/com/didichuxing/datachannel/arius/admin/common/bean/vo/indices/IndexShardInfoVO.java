package com.didichuxing.datachannel.arius.admin.common.bean.vo.indices;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lyn
 * @date 2021/09/29
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "索引shard分配信息")
public class IndexShardInfoVO extends BaseVO {
    @ApiModelProperty("索引名称")
    private String  index;

    @ApiModelProperty("shard序号")
    private Integer shard;

    @ApiModelProperty("shard状态")
    private String  state;

    @ApiModelProperty("shard文档数")
    private Long    docs;

    @ApiModelProperty("shard存储空间 单位: kb,gb,mb")
    private String  store;

    @ApiModelProperty("shard存储空间 单位byte")
    private Long    storeInByte;

    @ApiModelProperty("shard所在节点ip")
    private String  ip;

    @ApiModelProperty("shard所在节点实例名称")
    private String  node;
}
