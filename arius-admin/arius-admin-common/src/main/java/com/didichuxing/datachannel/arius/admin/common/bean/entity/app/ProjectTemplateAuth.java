package com.didichuxing.datachannel.arius.admin.common.bean.entity.app;

import com.didichuxing.datachannel.arius.admin.common.constant.app.ProjectTemplateAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.StringResponsible;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 * @date 2019/4/16
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectTemplateAuth extends BaseEntity implements StringResponsible {

    /**
     * 主键
     */
    private Long id;

    /**
     * project ID
     */
    private Integer projectId;

    /**
     * 模板ID
     */
    private Integer templateId;

    /**
     * 权限类型  读写  读
     * @see ProjectTemplateAuthEnum
     */
    private Integer type;

    /**
     * 责任人列表
     */
    private String responsible;

}