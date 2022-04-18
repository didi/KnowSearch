package com.didichuxing.datachannel.arius.admin.common.bean.vo.template;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 多机房模板信息
 * @author wangshu
 * @date 2020/06/11
 */
@Data
@ApiModel(description = "多机房模板信息")
public class SinkSdkIDCTemplateDeployInfoVO extends BaseVO {
    /**
     * 模板基础信息
     */
    @ApiModelProperty("基本信息")
    private SinkSdkTemplateVO                     baseInfo;

    @ApiModelProperty("多机房模板主从元数据信息")
    private Map<String, SinkSdkIDCTemplateMasterSlaveMeta> templateMasterSlaveMetas;


    @Data
    public static class SinkSdkIDCTemplateMasterSlaveMeta extends BaseVO {
        /**
         * 主模板独有的信息
         */
        @ApiModelProperty("master信息")
        private SinkSdkTemplatePhysicalDeployVO       masterInfo;

        /**
         * 从模板独有的信息
         */
        @ApiModelProperty("slave信息")
        private List<SinkSdkTemplatePhysicalDeployVO> slaveInfos;
    }
}