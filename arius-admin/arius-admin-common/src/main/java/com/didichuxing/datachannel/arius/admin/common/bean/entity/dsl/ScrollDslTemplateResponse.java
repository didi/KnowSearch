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
 * 滚动查询查询模板响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScrollDslTemplateResponse {

    /**
     * 查询模板集合
     */
    private List<DslTemplatePO> dslTemplatePoList;
    /**
     * 滚动查询游标
     */
    private String              scrollId;

}
