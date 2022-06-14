package com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.indices.IndicesClearDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicAggregate;
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
    private List<IndexTemplateLogicAggregate> templateLogicAggregates;

    @ApiModelProperty("索引列表")
    private List<IndicesClearDTO> catIndexResults;
}