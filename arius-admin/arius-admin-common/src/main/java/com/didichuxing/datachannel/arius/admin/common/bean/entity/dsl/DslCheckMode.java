package com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: D10865
 * @Description:
 * @Date: Create on 2018/6/20 下午5:30
 * @Modified By
 *
 * 修改查询模板黑白名单
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DslCheckMode extends DslBase {

    /**
     * 黑白名单：black、write
     */
    private String checkMode;

    public DslCheckMode(Integer projectId, String dslTemplateMd5, String checkMode) {
        super(projectId, dslTemplateMd5);
        this.checkMode = checkMode;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}