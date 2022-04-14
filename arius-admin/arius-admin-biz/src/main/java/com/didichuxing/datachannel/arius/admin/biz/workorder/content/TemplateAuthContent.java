package com.didichuxing.datachannel.arius.admin.biz.workorder.content;

import com.didichuxing.datachannel.arius.admin.client.constant.app.AppTemplateAuthEnum;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模板扩缩容
 * @author d06679
 * @date 2019/5/7
 */
@Data
@NoArgsConstructor
public class TemplateAuthContent extends BaseContent {

    /**
     * 逻辑模板id
     */
    private Integer id;

    /**
     * 逻辑模板名字
     */
    private String  name;

    /**
     * 权限类型
     * @see AppTemplateAuthEnum
     */
    private Integer authCode;

    /**
     * 申请说明
     */
    private String  memo;

    /**
     * 责任人
     */
    private String  responsible;

}
