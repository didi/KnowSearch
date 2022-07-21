package com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndexCatCellDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 模板和索引的详细信息
 * @author gyp
 * @Date 2022/5/31
 * @Version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClusterLogicTemplateIndexDetailDTO {
    @ApiModelProperty("模板列表")
    private List<IndexTemplate>    templates;

    @ApiModelProperty("索引列表")
    private List<IndexCatCellDTO>  catIndexResults;
}