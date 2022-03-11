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
 * @date: Create on 2019/2/27 下午2:14
 * @modified By D10865
 *
 * 索引维度访问次数
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel(value = "IndexNameAccessCountPO", description = "索引访问统计结果")
public class IndexNameAccessCountPO extends BaseESPO {

    /**
     * 索引模板主键
     */
    @ApiModelProperty("索引模板主键")
    private Integer templateId;
    /**
     * 逻辑模板id
     */
    @ApiModelProperty("逻辑模板id")
    private Integer logicTemplteId;
    /**
     * 索引模板名称
     */
    @ApiModelProperty("索引模板名称")
    private String templateName;
    /**
     * 集群名称
     */
    @ApiModelProperty("集群名称")
    private String clusterName;
    /**
     * 索引名称
     */
    @ApiModelProperty("索引名称")
    private String indexName;
    /**
     * 访问次数
     */
    @ApiModelProperty("访问次数")
    private Long count;
    /**
     * 统计日期
     */
    @ApiModelProperty("统计日期")
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
        // 由于存在索引双写的情况，需要加上索引模板主键作为联合id
        return String.format("%d_%s_%s", templateId, indexName, date);
    }

    @Override
    public String getRoutingValue() {
        return null;
    }

}
