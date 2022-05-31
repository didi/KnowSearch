package com.didichuxing.datachannel.arius.admin.common.bean.vo.template.srv;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chengxiang
 * @date 2022/5/19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateSrvVO {

    /**
     * 模板服务code
     */
    private Integer srvCode;

    /**
     * 模板服务名称
     */
    private String srvName;

    /**
     * 模板服务所需的最低es版本
     */
    private String esVersion;
}