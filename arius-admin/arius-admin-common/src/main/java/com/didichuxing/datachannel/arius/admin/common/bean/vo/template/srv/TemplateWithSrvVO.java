package com.didichuxing.datachannel.arius.admin.common.bean.vo.template.srv;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chengxiang
 * @date 2022/5/18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "「模板服务」信息")
public class TemplateWithSrvVO extends BaseVO {

    @ApiModelProperty("模板ID")
    private Integer                        id;

    @ApiModelProperty("模板名称")
    private String                         name;

    @ApiModelProperty("所属集群")
    private List<String>                   cluster;

    @ApiModelProperty("开启的服务")
    private List<TemplateSrvVO>            openSrv;

    @ApiModelProperty("不可用的服务")
    private List<UnavailableTemplateSrvVO> unavailableSrv;

    @ApiModelProperty("DCDR主从位点差")
    private Long                           checkPointDiff;

    @ApiModelProperty("数据保存时长 单位天")
    private Integer                        expireTime;

    @ApiModelProperty("热数据保存时长 单位天")
    private Integer                        hotTime;

    @ApiModelProperty("项目名称 ")
    private String                         projectName;
    @ApiModelProperty("是否具有dcdr")
    private Boolean hasDCDR;

}