package com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: D10865
 * @description: Dsl模板
 * @date: Create on 2019/1/15 下午8:19
 * @modified By D10865
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DslTemplates {
    /**
     * 查询模板增加个数
     */
    private Long dslIncCnt;
    /**
     * 查询模板个数
     */
    private Long dslTotalCnt;
}
