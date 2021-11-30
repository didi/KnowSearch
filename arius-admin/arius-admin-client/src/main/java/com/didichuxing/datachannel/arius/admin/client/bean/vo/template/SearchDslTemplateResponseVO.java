package com.didichuxing.datachannel.arius.admin.client.bean.vo.template;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: D10865
 * @description:
 * @date: Create on 2019/2/22 下午4:45
 * @modified By D10865
 *
 * 查询查询模板响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "SearchDslTemplateResponseVO", description = "查询查询模板响应")
public class SearchDslTemplateResponseVO {

    /**
     * 查询模板集合
     */
    @ApiModelProperty("查询模板集合")
    private List<DslTemplateVO> records;

    /**
     * 查询命中记录数
     */
    @ApiModelProperty("查询命中记录数")
    private Long totalHits;

}
