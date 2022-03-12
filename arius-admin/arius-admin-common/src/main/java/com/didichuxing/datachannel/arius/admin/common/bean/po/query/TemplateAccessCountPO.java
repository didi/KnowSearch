package com.didichuxing.datachannel.arius.admin.common.bean.po.query;

import com.alibaba.fastjson.annotation.JSONField;
import com.didichuxing.datachannel.arius.admin.common.bean.po.BaseESPO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author: D10865
 * @description:
 * @date: Create on 2019/2/27 下午2:09
 * @modified By D10865
 *
 * 索引模板维度访问次数
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel(value = "TemplateAccessCountPO", description = "索引模板访问次数统计")
public class TemplateAccessCountPO extends BaseESPO {
    /**
     * 索引模板主键
     */
    @ApiModelProperty(value = "物理索引模板ID")
    private Integer templateId;
    /**
     * 逻辑模板id
     */
    @ApiModelProperty(value = "逻辑模板id")
    private Integer logicTemplateId;
    /**
     * 索引模板名称
     */
    @ApiModelProperty(value = "索引模板名称")
    private String templateName;
    /**
     * 集群名称
     */
    @ApiModelProperty(value = "集群名称")
    private String clusterName;
    /**
     * 访问次数
     */
    @ApiModelProperty(value = "访问次数")
    private Long count;
    /**
     * 统计日期
     */
    @ApiModelProperty(value = "统计日期")
    private String date;

    /**
     * 累加访问次数
     */
    @JSONField(serialize = false)
    public void increase(Long value) {
        if (count == null) {
            count = 0L;
        }
        count += value;
    }

    /**
     * 获取主键key
     *
     * @return
     */
    @JSONField(serialize = false)
    @Override
    public String getKey() {
        return String.format("%d_%s", templateId, date);
    }

}
