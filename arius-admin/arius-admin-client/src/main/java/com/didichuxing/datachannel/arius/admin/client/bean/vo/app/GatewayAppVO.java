package com.didichuxing.datachannel.arius.admin.client.bean.vo.app;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 运维控制台使用
 *
 * @author d06679
 * @date 2019/3/13
 */
@Data
@ApiModel(description = "应用信息（包含权限）")
public class GatewayAppVO extends BaseVO {

    @ApiModelProperty("应用ID")
    private Integer      id;

    @ApiModelProperty("应用名称")
    private String       name;

    @ApiModelProperty("验证码")
    private String       verifyCode;

    @ApiModelProperty("限流值")
    private Integer      queryThreshold;

    @ApiModelProperty("查询集群")
    private String       cluster;

    @ApiModelProperty("查询模式")
    private Integer      searchType;

    /**
     * 有读权限的索引列表
     */
    @ApiModelProperty("读权限列表")
    private List<String> indexExp;

    /**
     * 有写权限的索引列表
     */
    @ApiModelProperty("读写权限列表")
    private List<String> wIndexExp;

    /**
     * 白名单
     */
    @ApiModelProperty("IP白名单")
    private List<String> ip;

    /**
     *
     */
    @ApiModelProperty("数据中心")
    private String       dataCenter;

    @ApiModelProperty("dsl解析开关")
    private Integer      dslAnalyzeEnable;

    @ApiModelProperty("聚合解析开关")
    private Integer      aggrAnalyzeEnable;

    @ApiModelProperty("查询结果解析开关")
    private Integer      analyzeResponseEnable;

    @ApiModelProperty("超级租户开关")
    private Integer isRoot;

}
