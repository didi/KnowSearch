package com.didichuxing.datachannel.arius.admin.common.bean.vo.app;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户控制台使用
 *
 * @author d06679
 * @date 2019/3/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "es user 信息")
public class ConsoleESUserVO extends BaseVO {

    @ApiModelProperty("es user")
    private Integer id;


    @ApiModelProperty("备注")
    private String  memo;

    @ApiModelProperty("查询限流值")
    private Integer queryThreshold;

    @ApiModelProperty("数据中心")
    private String  dataCenter;

}