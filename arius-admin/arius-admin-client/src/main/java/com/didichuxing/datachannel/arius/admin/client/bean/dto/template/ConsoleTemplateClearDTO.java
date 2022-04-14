package com.didichuxing.datachannel.arius.admin.client.bean.dto.template;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 * @date 2019-06-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "索引清理信息")
public class ConsoleTemplateClearDTO extends BaseDTO {

    @ApiModelProperty("索引ID")
    private Integer      logicId;

    @ApiModelProperty("需要清理的索引列表")
    private List<String> delIndices;

    @ApiModelProperty("删除条件")
    private String       delQueryDsl;

}
