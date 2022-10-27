package com.didichuxing.datachannel.arius.admin.common.bean.dto.indices;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.PageDTO;
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
@ApiModel(description = "索引条件查询实体")
public class IndexQueryDTO extends PageDTO {

    @ApiModelProperty("集群")
    private String  cluster;

    @ApiModelProperty("索引名称")
    private String  index;

    @ApiModelProperty("状态 green yellow red")
    private String  health;

    @ApiModelProperty("状态 open close")
    private String  status;

    @ApiModelProperty("排序字段(priStoreSize)、shard数(pri)、副本个数(rep)、存储大小(storeSize)、文档数量(docsCount)、删除文档数量(docsDeleted、索引名称（index）、segment数（totalSegmentCount）")
    private String  sortTerm;

    @ApiModelProperty(value = "是否降序排序（默认降序）", dataType = "Boolean", required = false)
    private Boolean orderByDesc = true;

    @ApiModelProperty("是否展示元数据索引")
    private Boolean isShowIndicesWithMetaData;
}