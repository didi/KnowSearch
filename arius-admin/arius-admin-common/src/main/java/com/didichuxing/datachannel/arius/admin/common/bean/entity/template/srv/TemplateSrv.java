package com.didichuxing.datachannel.arius.admin.common.bean.entity.template.srv;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;
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
public class TemplateSrv extends BaseEntity {

    /**
     * 模板服务code
     */
    private Integer srvCode;

    /**
     * 模板服务名称
     */
    private String srvName;

    /**
     * todo: 这个暂时没用到，后续干掉
     * 模板服务所需的最低es版本号
     */
    private String esVersion;
}
