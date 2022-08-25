package com.didichuxing.datachannel.arius.admin.common.bean.vo.template;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;
import com.didichuxing.datachannel.arius.admin.common.constant.template.DataTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 索引模板签证官
 *
 * @author shizeying
 * @date 2022/08/23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseTemplateVO extends BaseEntity implements Comparable<BaseTemplateVO> {
    
    @ApiModelProperty("索引 ID")
    private Integer id;
    
    @ApiModelProperty("索引名称")
    private String name;
    
    /**
     * 用户数据类型
     *
     * @see DataTypeEnum
     */
    @ApiModelProperty("数据类型（0: 系统 1: 日志；2: 用户上报；3:RDS 数据；4：离线导入数据）")
    private Integer dataType;
    
    /**
     * 索引滚动格式
     */
    @ApiModelProperty("时间后缀")
    private String dateFormat;
    
    @ApiModelProperty("数据中心")
    private String dataCenter;
    
    /**
     * 数据保存时长 单位天
     */
    @ApiModelProperty("保存天数")
    private Integer expireTime;
    
    /**
     * 热数据保存时长 单位天
     */
    @ApiModelProperty("热数据保存天数")
    private Integer hotTime;
    
    @ApiModelProperty("索引应用 ID")
    private Integer projectId;
    
    @ApiModelProperty("时间字段")
    private String dateField;
    
    @ApiModelProperty("时间字段格式")
    private String dateFieldFormat;
    
    /**
     * id 地钻
     */
    @ApiModelProperty("主键字段")
    private String idField;
    
    @ApiModelProperty("routing 字段")
    private String routingField;
    
    @ApiModelProperty("表达式")
    private String expression;
    
    @ApiModelProperty("描述")
    private String desc;
    
    /**
     * 规格 单位台
     */
    @ApiModelProperty("配额")
    private Double quota;
    
    @ApiModelProperty("写入限流值，" + "writeRateLimit = 0 禁止写入；" + "writeRateLimit = -1 不限流；"
                      + "writeRateLimit = 123 具体的写入 tps 限流值，即单台 client 每秒写入 123 条文档")
    private Integer writeRateLimit;
    
    /**
     * 是否禁读
     */
    @ApiModelProperty("是否禁读")
    private Boolean blockRead;
    
    /**
     * 是否禁写
     */
    @ApiModelProperty("是否禁写")
    private Boolean blockWrite;
    
    /**
     * 副本保存时长 单位天
     */
    @ApiModelProperty("副本保存时长 单位天")
    private Integer replicaTime;
    
    /**
     * 逻辑集群 id
     */
    @ApiModelProperty("逻辑集群 id")
    private Long resourceId;
    
    /**
     * pipeline
     */
    @ApiModelProperty("pipeline")
    private String ingestPipeline;
    
    /**
     * 服务等级
     */
    @ApiModelProperty("服务等级")
    private Integer level;
    
    /**
     * 是否开启 dcdr
     */
    @ApiModelProperty("是否开启 dcdr")
    private Boolean hasDCDR;
    
    /**
     * 数据位点差
     */
    @ApiModelProperty("是否禁写")
    private Long checkPointDiff;
    
    /**
     * 已开启的模板服务
     */
    @ApiModelProperty("是否禁写")
    private String  openSrv;
    /**
     * regionId
     */
    @ApiModelProperty("regionId")
    private Integer regionId;
    
    /**
     * 可用磁盘容量
     */
    @ApiModelProperty("可用磁盘容量")
    private Double  diskSize;
    /**
     * 模版健康度
     */
    @ApiModelProperty("模版健康度")
    private Integer health;
    
    @Override
    public int compareTo(BaseTemplateVO o) {
        if (null == o) {
            return 0;
        }
        
        return o.getId().intValue() - this.getId().intValue();
    }
}