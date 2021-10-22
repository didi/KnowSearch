package com.didichuxing.datachannel.arius.admin.client.bean.vo.template;

import com.didichuxing.datachannel.arius.admin.client.bean.common.QuotaUsage;
import com.didichuxing.datachannel.arius.admin.client.constant.app.AppTemplateAuthEnum;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author d06679
 * @date 2019/3/29
 */
@Data
@ApiModel(description = "索引信息")
public class ConsoleTemplateVO extends BaseTemplateVO implements Comparable{

    /**
     * @see AppTemplateAuthEnum
     */
    @ApiModelProperty("权限（1:管理；2:读写；3:读）")
    private Integer    authType;

    @ApiModelProperty("所属集群")
    private String     cluster;

    @ApiModelProperty("配额使用情况")
    private QuotaUsage quotaUsage;

    @ApiModelProperty("模板价值")
    private Integer    value;

    @ApiModelProperty("是否具备DCDR")
    private Boolean    hasDCDR;

    @ApiModelProperty("项目名称")
    private String     appName;

    @Override
    public int compareTo(Object o) {
        if(null == o){return 0;}

        ConsoleTemplateVO c = (ConsoleTemplateVO)o;
        return c.getId().intValue() - this.getId().intValue();
    }
}
