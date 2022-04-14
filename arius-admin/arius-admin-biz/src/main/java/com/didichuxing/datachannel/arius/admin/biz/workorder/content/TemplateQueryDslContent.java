package com.didichuxing.datachannel.arius.admin.biz.workorder.content;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模板扩缩容
 * @author d06679
 * @date 2019/5/7
 */
@Data
@NoArgsConstructor
public class TemplateQueryDslContent extends BaseContent {

    private Integer id;

    /**
     * 名字
     */
    private String  name;

    private String  dsl;

    private String  memo;

}
