package com.didichuxing.datachannel.arius.admin.client.bean.vo.template;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.client.bean.common.QuotaUsage;
import com.didichuxing.datachannel.arius.admin.client.constant.app.AppTemplateAuthEnum;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 * @date 2019/3/29
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "模板信息")
public class ConsoleTemplateVO extends BaseTemplateVO implements Comparable<ConsoleTemplateVO> {

    /**
     * @see AppTemplateAuthEnum
     */
    @ApiModelProperty("权限（1:管理；2:读写；3:读）")
    private Integer      authType;

    @ApiModelProperty("所属集群")
    private List<String> clusterPhies;

    @ApiModelProperty("配额使用情况")
    private QuotaUsage   quotaUsage;

    @ApiModelProperty("模板价值")
    private Integer      value;

    @ApiModelProperty("是否具备DCDR")
    private Boolean      hasDCDR;

    @ApiModelProperty("项目名称")
    private String       appName;

    @Override
    public int compareTo(ConsoleTemplateVO o) {
        if (null == o) {
            return 0;
        }

        return o.getId().intValue() - this.getId().intValue();
    }
}
