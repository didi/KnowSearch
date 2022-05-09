package com.didichuxing.datachannel.arius.admin.common.bean.vo.template;

import java.util.Date;
import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "DCDRSingleTemplateMasterSlaveSwitchDetailVO", description = "模板DCDR详情")
public class DCDRSingleTemplateMasterSlaveSwitchDetailVO extends BaseVO {
    @ApiModelProperty("任务标题")
    private String  taskTitle;

    @ApiModelProperty("dcdr主从切换类型 1 平滑 2 强切")
    private Integer switchType;

    @ApiModelProperty("模板Id")
    private Long    templateId;

    @ApiModelProperty("主集群")
    private String  masterCluster;

    @ApiModelProperty("从集群")
    private String  slaveCluster;

    @ApiModelProperty("任务执行详情\n" +
            "     * 0.创建dcdr\n" +
            "     * 1.停止向主索引写入数据\n" +
            "     * 2.确保主从索引数据同步\n" +
            "     * 3.删除源dcdr模板和索引链路\n" +
            "     * 4.拷贝主模板的mapping信息到从模板\n" +
            "     * 5.关闭从索引dcdr索引开关，并打开主索引dcdr索引开关\n" +
            "     * 6.停止从索引写入\n" +
            "     * 7.创建新的dcdr链路\n" +
            "     * 8.恢复主从索引实时写入\n" +
            "     * 9.主从模板角色切换")
    private List<String> taskProgressList;

    @ApiModelProperty("任务状态: 0 取消 1 成功 2 执行中 3 失败 4待执行")
    private Integer taskStatus;

    private Date    createTime;
    private Date    finishTime;
}
