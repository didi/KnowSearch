package com.didichuxing.datachannel.arius.admin.common.bean.vo.config;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;
import io.swagger.annotations.ApiModelProperty;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * op manger config
 *
 * @author shizeying
 * @date 2022/10/21
 * @since 0.3.2
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpManagerConfigVO extends BaseVO {
    @ApiModelProperty("配置 id")
    private Integer id;
    @ApiModelProperty("分组名")
    private String  groupName;
    /**
     * 系统配置
     */
    @ApiModelProperty("系统配置")
    private String  systemConfig;
    /**
     * 运行时配置
     */
    @ApiModelProperty("运行时配置")
    private String  runningConfig;
    /**
     * 文件配置
     */
    @ApiModelProperty("文件配置")
    private String    fileConfig;
    @ApiModelProperty("版本")
    private String    version;
    /**
     * 组件 id
     */
    @ApiModelProperty("组件 id")
    private Integer   componentId;
    /**
     * 安装目录配置
     */
    @ApiModelProperty("安装目录配置")
    private String    installDirectoryConfig;
    /**
     * 进程数配置
     */
    @ApiModelProperty("进程数配置")
    private String    processNumConfig;
    /**
     * 节点列表
     */
    @ApiModelProperty("节点列表")
    private String    hosts;
    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private Timestamp createTime;
    /**
     * 更新时间
     */
    @ApiModelProperty("更新时间")
    private Timestamp updateTime;
    @ApiModelProperty("机型")
    private String machineSpec;
    @ApiModelProperty("是否支持编辑和回滚")
    private  Boolean supportEditAndRollback=false;
}