package com.didichuxing.datachannel.arius.admin.common.bean.vo.template;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Alias;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;

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
@ApiModel(description = "物理模板信息")
public class GatewayTemplatePhysicalVO extends BaseVO {

    @ApiModelProperty("模板ID")
    private Long        id;

    /**
     * 索引模板名称
     */
    @ApiModelProperty("模板名称")
    private String      name;

    /**
     * 数据中心
     */
    @ApiModelProperty("数据中心")
    private String      dataCenter;

    /**
     * 表达式
     */
    @ApiModelProperty("表达式")
    private String      expression;

    /**
     * 版本号
     */
    @ApiModelProperty("版本")
    private Integer     version;

    /**
     * 别名
     */
    @ApiModelProperty("别名列表")
    private List<Alias> aliases;

}
