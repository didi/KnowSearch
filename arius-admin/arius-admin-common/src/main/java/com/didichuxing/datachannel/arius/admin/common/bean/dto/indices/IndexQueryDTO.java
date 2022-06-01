package com.didichuxing.datachannel.arius.admin.common.bean.dto.indices;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.PageDTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * @author lyn
 * @date 2021/09/29
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "索引条件查询实体")
public class IndexQueryDTO extends PageDTO {

    @ApiModelProperty("物理集群")
    private List<String> cluster;

    @ApiModelProperty("逻辑集群Id")
    private Integer resourceId;

    @ApiModelProperty("索引名称")
    private String index;

    @ApiModelProperty("状态 green yellow red")
    private String health;

    @ApiModelProperty("排序字段(priStoreSize)、主分配个数(pri)、副本个数(rep)、存储大小(storeSize)、文档数量(docsCount)、删除文档数量(docsDeleted)、索引名称（index）")
    private String sortTerm;

    @ApiModelProperty(value = "是否降序排序（默认降序）", dataType = "Boolean", required = false)
    private Boolean orderByDesc = true;
}
