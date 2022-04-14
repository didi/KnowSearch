package com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl;

import com.didichuxing.datachannel.arius.admin.common.bean.po.dsl.DslTemplatePO;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Builder
public class SearchDslTemplateResponse {

    /**
     * 查询模板集合
     */
    private List<DslTemplatePO> records;

    /**
     * 查询命中记录数
     */
    private Long totalHits;

}
