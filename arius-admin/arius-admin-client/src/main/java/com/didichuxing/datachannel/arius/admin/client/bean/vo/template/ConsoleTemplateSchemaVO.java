package com.didichuxing.datachannel.arius.admin.client.bean.vo.template;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;
import com.didichuxing.datachannel.arius.admin.client.mapping.AriusTypeProperty;
import com.didichuxing.datachannel.arius.admin.client.mapping.Field;

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
@ApiModel(description = "索引schema信息")
public class ConsoleTemplateSchemaVO extends BaseVO {

    @ApiModelProperty("索引ID")
    private Integer                 id;

    @ApiModelProperty("索引名字")
    private String                  name;

    /**
     * mapping信息 手动设置的
     */
    @ApiModelProperty("索引mapping列表信息")
    private List<Field>             fields;

    @ApiModelProperty("索引mappingjson信息")
    private List<AriusTypeProperty> typeProperties;

}
