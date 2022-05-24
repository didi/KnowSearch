package com.didichuxing.datachannel.arius.admin.common.bean.dto.template.srv;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.srv.BaseTemplateSrvOpenDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author chengxiang
 * @date 2022/5/16
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "模板清理参数")
public class TemplateClearDTO extends BaseTemplateSrvOpenDTO {

    @ApiModelProperty("模板id")
    private Integer templateId;

    @ApiModelProperty("需要清理的索引列表")
    private List<String> delIndices;

}
