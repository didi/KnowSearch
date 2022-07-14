package com.didichuxing.datachannel.arius.admin.common.bean.vo.template;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;
import com.didiglobal.logi.security.common.vo.project.ProjectBriefVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
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
@ApiModel(description = "索引清理信息")
public class ConsoleTemplateClearVO extends BaseVO {

    @ApiModelProperty("索引ID")
    private Integer              logicId;

    @ApiModelProperty("索引名字")
    private String               name;

    @ApiModelProperty("清理索引列表")
    private List<String>         indices;

    /**
     * 最近一段时间有访问的app
     */
    @ApiModelProperty("访问应用列表")
    private List<ProjectBriefVO> accessApps;

}