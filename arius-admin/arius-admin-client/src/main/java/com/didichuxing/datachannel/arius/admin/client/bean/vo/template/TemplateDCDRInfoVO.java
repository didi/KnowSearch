package com.didichuxing.datachannel.arius.admin.client.bean.vo.template;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by linyunan on 12/14/21
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "TemplateDCDRInfoVO", description = "模板DCDR信息")
public class TemplateDCDRInfoVO extends BaseVO {
    @ApiModelProperty("是否开启dcdr链路")
    private Boolean dcdrFlag;

    @ApiModelProperty("主集群名称")
    private String  masterClusterName;

    @ApiModelProperty("从集群名称")
    private String  slaveClusterName;

    @ApiModelProperty("主模板checkPoint")
    private Long    masterTemplateCheckPoint;

    @ApiModelProperty("从模板checkPoint")
    private Long    slaveTemplateCheckPoint;

    @ApiModelProperty("位点差")
    private Long    templateCheckPointDiff;
}
