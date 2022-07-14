package com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.PageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by linyunan on 2021-10-14
 * @author gyp
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "逻辑集群节点详情条件查询信息")
public class ClusterLogicNodeConditionDTO extends PageDTO {

    @ApiModelProperty("关键字")
    private String  keyword;

    @ApiModelProperty("暂无")
    private String  sortTerm;

    private String  sortType;

    @ApiModelProperty(value = "是否降序排序（默认降序）", dataType = "Boolean")
    private Boolean orderByDesc = true;

}
