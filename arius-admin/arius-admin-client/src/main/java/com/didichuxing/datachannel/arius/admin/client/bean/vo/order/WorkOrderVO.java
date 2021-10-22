package com.didichuxing.datachannel.arius.admin.client.bean.vo.order;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author fengqiongfeng
 * @date 2020/8/25
 */
@Data
public class WorkOrderVO extends BaseVO {
    @ApiModelProperty(value = "工单ID")
    private Long id;

    @ApiModelProperty(value = "工单标题")
    private String title;
//
//    @ApiModelProperty(value = "工单类型, appCreate 创建app,clusterCreate 创建集群,"
//    		+ "clusterIndecrease 集群扩缩溶,clusterOffline 集群下线,clusterUpdate 集群修改,"
//    		+ "templateAuth 索引申请,templateCreate 索引创建,templateIndecrease 索引扩容,"
//    		+ "templateQueryDsl 查询语句创建,templateTransfer 索引转让,queryDslLimitEdit 查询语句编辑,"
//    		+ "responsibleGovern 员工离职,unhealthyTemplateGovern 不健康索引处理")

    @ApiModelProperty(value = "工单类型")
    private String type;

    @ApiModelProperty(value = "申请人")
    private String applicant;

    @ApiModelProperty(value = "描述信息")
    private String description;

    @ApiModelProperty(value = "工单状态, 0:待审批, 1:通过, 2:拒绝, 3:取消")
    private Integer status;

    @ApiModelProperty(value = "申请/审核时间")
    private Date createTime;
    
    @ApiModelProperty(value = "审批人appId")
    private Integer approverAppId;
}
