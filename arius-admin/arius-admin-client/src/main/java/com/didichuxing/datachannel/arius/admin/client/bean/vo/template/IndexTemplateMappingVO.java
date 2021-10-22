package com.didichuxing.datachannel.arius.admin.client.bean.vo.template;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;
import lombok.Data;

@Data
public class IndexTemplateMappingVO extends BaseVO {
    private int            id;

    /**
     * 集群名称
     */
    private String         clusterName;

    /**
     * 逻辑模板名称
     */
    private String         templateName;

    /**
     * mapping信息
     */
    private String         templateMapping;
}
