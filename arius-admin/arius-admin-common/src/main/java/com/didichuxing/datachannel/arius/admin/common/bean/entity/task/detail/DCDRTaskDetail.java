package com.didichuxing.datachannel.arius.admin.common.bean.entity.task.detail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author fengqiongfeng
 * @date 2020/12/21
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DCDRTaskDetail extends AbstractTaskDetail{
    /**
     * 开始时间
     */
    private Date createTime;

    /**
     * 逻辑索引模版Id
     */
    private Integer logicTemplateId;

    /**
     * 逻辑索引模版名称
     */
    private String logicTemplateName;


    /**
     * 所属逻辑集群名称
     */
    private String logicClusterName;

    /**
     * 主物理索引模版
     */
    private String masterPhysicalTemplateName;

    /**
     * 所属主物理集群名称
     */
    private String masterPhysicalClusterName;

    /**
     * 副物理索引模版
     */
    private String slavePhysicalTemplateName;

    /**
     * 所属副物理集群名称
     */
    private String slavePhysicalClusterName;

    /**
     * 任务状态
     * success:成功 failed:失败
     * running:执行中
     */
    private String status;

    /**
     * 任务执行进度
     * 0.创建dcdr
     * 1.停止向主索引写入数据
     * 2.确保主从索引数据同步
     * 3.删除源dcdr模板和索引链路
     * 4.拷贝主模板的mapping信息到从模板
     * 5.关闭从索引dcdr索引开关，并打开主索引dcdr索引开关
     * 6.停止从索引写入
     * 7.创建新的dcdr链路
     * 8.恢复主从索引实时写入
     * 9.主从模板角色切换
     */
    private Integer taskProgress;

    /**
     * 备注
     */
    private String comment;

}
