package com.didichuxing.datachannel.arius.admin.client.bean.vo.indices;

import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lyn
 * @date 2021/09/28
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "IndexSettingVO", description = "索引setting信息")
public class IndexSettingVO extends BaseVO {
    @ApiModelProperty("索引名称")
    private String     indexName;

    @ApiModelProperty("配置信息（json格式）")
    private JSONObject properties;
}
