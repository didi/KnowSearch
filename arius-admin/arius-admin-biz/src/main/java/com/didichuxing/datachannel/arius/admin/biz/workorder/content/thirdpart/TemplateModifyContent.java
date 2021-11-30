package com.didichuxing.datachannel.arius.admin.biz.workorder.content.thirdpart;

import com.didichuxing.datachannel.arius.admin.biz.workorder.content.BaseContent;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模板扩缩容
 * @author d06679
 * @date 2019/5/7
 */
@Data
@NoArgsConstructor
public class TemplateModifyContent extends BaseContent {

    private Integer id;

    /**
     * 索引模板名称
     */
    private String  name;

    /**************************************** 期望信息 ****************************************************/

    /**
     * 责任人
     */
    private String  responsible;

    /**
     * 部门id
     */
    private String  libraDepartmentId;

    /**
     * 部门名称
     */
    private String  libraDepartment;

    /**
     * 期望数据保存时长 单位天
     */
    private Integer expireTime;

    /**
     * 数据总量 单位台
     */
    private Double  quota;

    /**
     * 数据总量 单位G
     */
    private Double  diskQuota;

}
