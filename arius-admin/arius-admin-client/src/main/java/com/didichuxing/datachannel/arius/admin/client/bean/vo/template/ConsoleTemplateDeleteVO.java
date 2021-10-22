package com.didichuxing.datachannel.arius.admin.client.bean.vo.template;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.app.ConsoleAppVO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author d06679
 * @date 2019/3/29
 */
@Data
@ApiModel(description = "索引下线信息")
public class ConsoleTemplateDeleteVO extends BaseVO {

    @ApiModelProperty("索引ID")
    private Integer            logicId;

    /**
     * 索引模板名称
     */
    @ApiModelProperty("索引名字")
    private String             name;

    /**
     * 最近一段时间有访问的app
     */
    @ApiModelProperty("访问应用列表")
    private List<ConsoleAppVO> accessApps;

}
