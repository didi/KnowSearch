package com.didichuxing.datachannel.arius.admin.client.bean.vo.template;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author didi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "索引模板Setting视图信息")
public class TemplateSettingVO extends BaseVO {

    @ApiModelProperty("索引ID")
    private Integer logicId;

    @ApiModelProperty("是否开启副本")
    private boolean cancelCopy;

    @ApiModelProperty("是否开启异步translog")
    private boolean asyncTranslog;

    @ApiModelProperty("分词器")
    private JSONObject analysis;

    @ApiModelProperty("dynamic_templates设置")
    private JSONArray dynamicTemplates;
}
