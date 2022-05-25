package com.didichuxing.datachannel.arius.admin.common.bean.dto.app;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author d06679
 * @date 2019/3/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "es user 信息")
public class ConsoleESUserDTO extends BaseDTO {

    @ApiModelProperty("es user")
    private Integer id;
    

    @ApiModelProperty("备注")
    private String  memo;

    @ApiModelProperty("数据中心")
    private String  dataCenter;
}