package com.didichuxing.datachannel.arius.admin.common.bean.po.template;

import com.alibaba.fastjson.annotation.JSONField;
import com.didichuxing.datachannel.arius.admin.common.bean.po.BaseESPO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 索引标签bean
 *
 * @author d06679
 * @date 2017/10/9
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateValuePO extends BaseESPO {

    /**
     * 索引模板id
     */
    private Integer logicTemplateId;
    /**
     * 价值
     */
    private Integer value;
    /**
     * 访问量
     */
    private Long    accessCount;
    /**
     * 大小G
     */
    private Double  sizeG;
    /**
     * 逻辑集群
     */
    private String  logicCluster;

    /**
     * 获取主键key
     *
     * @return
     */
    @JSONField(serialize = false)
    @Override
    public String getKey() {
        return String.valueOf(logicTemplateId);
    }

    @Override
    public String getRoutingValue() {
        return null;
    }

}
