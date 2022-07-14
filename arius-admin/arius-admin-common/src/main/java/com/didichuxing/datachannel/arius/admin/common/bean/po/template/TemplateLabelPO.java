package com.didichuxing.datachannel.arius.admin.common.bean.po.template;

import com.alibaba.fastjson.annotation.JSONField;
import com.didichuxing.datachannel.arius.admin.common.bean.po.BaseESPO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 索引标签
 *
 * @author d06679
 * @date 2017/10/9
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateLabelPO extends BaseESPO {

    /**
     * 索引模板id
     */
    private Integer indexTemplateId;
    /**
     * 标签id，有一定的规则
     */
    private String  labelId;
    /**
     * 标签名称
     */
    private String  labelName;
    /**
     * 标记时间
     */
    private Date    markTime;

    /**
     * 获取主键key
     *
     * @return
     */
    @JSONField(serialize = false)
    @Override
    public String getKey() {
        return String.format("%d_%s", indexTemplateId, labelId);
    }

    @Override
    public String getRoutingValue() {
        return null;
    }

    /**
     * 获取主键key
     *
     * @return
     */
    @JSONField(serialize = false)
    public String getId() {
        return getKey();
    }

}
