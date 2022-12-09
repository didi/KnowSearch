package com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;

import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "插件信息")
public class PluginVO extends BaseVO {
    @ApiModelProperty("ID主键自增")
    private Long    id;

    @ApiModelProperty("插件名")
    private String  name;

    @ApiModelProperty("插件版本")
    private String  version;

    @ApiModelProperty("插件存储地址")
    @Deprecated
    //TODO 需要下线
    private String  url;

    @ApiModelProperty("插件文件md5")
     @Deprecated
    //TODO 需要下线
    private String  md5;

    @ApiModelProperty("插件描述")
     @Deprecated
    //TODO 需要下线
    private String  desc;

    @ApiModelProperty("插件创建人")
    @Deprecated
    //TODO 需要下线
    private String  creator;

    @ApiModelProperty("上传插件类型: 0 系统默认插件, 1 ES能力插件, 2 平台能力插件")
    @Deprecated
    //TODO 需要下线
    private Integer pDefault;

    @ApiModelProperty("是否安装: true 是, false 否")
    @Deprecated
    //TODO 需要下线
    private Boolean installed;
    
    @ApiModelProperty("插件类型:1.平台插件;2.引擎插件;3.内核插件")
    private Integer pluginType;
    @ApiModelProperty("关联的组建 id")
    private Integer componentId;
    @ApiModelProperty("备注")
    private String  memo;
    @ApiModelProperty("插件状态:-1:unknown;1.green；2.yellow;3.red")
    private Integer                    status;
    @ApiModelProperty("插件配置")
    private List<ComponentGroupConfig> componentGroupConfigs;
    
    
}