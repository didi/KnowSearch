package com.didichuxing.datachannel.arius.admin.client.bean.dto.account;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;
import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

/**
 * @author linyunan
 * @date 2021-04-21
 */
@Builder
@Data
@ApiModel(description = "用户登录记录")
public class UserLoginRecordDTO extends BaseDTO {
    /**
     * 主键
     */
    @ApiModelProperty("主键")
    private Long   id;

    /**
     * 登录人
     */
    @ApiModelProperty("登录人")
    private String loginName;

    /**
     * 登录时间
     */
    @ApiModelProperty("登录时间")
    private Date   loginTime;
}
