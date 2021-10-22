package com.didichuxing.datachannel.arius.admin.client.bean.vo.task;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;
import lombok.Data;

import java.util.Date;

/**
 * WorkTask PO 对象
 * 
 * @author fengqiongfeng
 * @date 2020-12-21
 */

@Data
public class WorkTaskVO extends BaseVO {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Integer id;

    /**
     * 标题 
     */
    private String title;

    /**
     * 任务类型
     */
    private Integer taskType;

    /**
     * 业务数据主键 
     */
    private Integer businessKey;

    /**
     * 任务状态
     * success:成功 failed:失败
     * running:执行中 waiting:等待
     * cancel:取消 pause:暂停
     */
    private String status;

    /**
     * 创建人 
     */
    private String creator;

    /**
     * 创建时间 
     */
    private Date createTime;

    /**
     * 更新时间 
     */
    private Date updateTime;

    /**
     * 标记删除 
     */
    private Boolean deleteFlag;

    /**
     * expandData 
     */
    private String expandData;

}

