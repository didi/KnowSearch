package com.didichuxing.datachannel.arius.admin.common.bean.entity.task.detail;

import java.util.Date;
import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.constant.dcdr.DcdrStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.dcdr.DcdrSwithTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DCDRSingleTemplateMasterSlaveSwitchDetail extends AbstractTaskDetail {
    /**
     * 任务标题
     */
    private String  taskTitle;

    /**
     * dcdr主从切换类型 1 平滑 2 强切
     * @see DcdrSwithTypeEnum
     */
    private Integer switchType;

    /**
     * 模板Id
     */
    private Long    templateId;

    /**
     * 主集群
     */
    private String  masterCluster;

    /**
     * 从集群
     */
    private String  slaveCluster;

    /**
     * dcdr链路删除标识位
     */
    private Boolean deleteDcdrChannelFlag;

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
    private List<String> taskProgressList;

    /**
     * 任务状态: 0 取消 1 成功 2 执行中 3 失败 4 等待
     * @see DcdrStatusEnum
     */
    private Integer taskStatus;

    private Date    createTime;
    private Date    updateTime;

    public void editTaskTitle(String templateName) {
        this.taskTitle = templateName + "索引模板主从切换";
    }
}
