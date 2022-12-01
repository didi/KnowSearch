package com.didichuxing.datachannel.arius.admin.common.bean.entity.workorder.detail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wuxuan
 * @date 2022/11/14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DslTemplateStatusDetail  extends AbstractOrderDetail {

    /**
     * 名字
     */
    private String  name;

    /**
     * 项目id
     */
    Integer projectId;

    /**
     * 操作者
     */
    String operator;

    /**
     * dsl模板MD5
     */
    String dslTemplateMd5;

}
