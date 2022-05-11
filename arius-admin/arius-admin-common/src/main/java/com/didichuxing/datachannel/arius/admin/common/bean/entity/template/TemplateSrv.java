package com.didichuxing.datachannel.arius.admin.common.bean.entity.template;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chengxiang
 * @date 2022/5/11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateSrv {
    /**
     * 索引服务id
     */
    private Integer serviceId;

    /**
     * 索引服务名称
     */
    private String serviceName;

    /**
     * 索引服务所需的最低es版本号
     */
    private String esVersion;
}
