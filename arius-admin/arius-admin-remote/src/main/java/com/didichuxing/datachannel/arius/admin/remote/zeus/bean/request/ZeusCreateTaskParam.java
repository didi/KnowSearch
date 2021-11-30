package com.didichuxing.datachannel.arius.admin.remote.zeus.bean.request;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "新建宙斯任务参数")
public class ZeusCreateTaskParam {

    /**
     * 宙斯环境模板Id
     */
    private Integer      tpl_id;

    /**
     * 宙斯环境 账号
     */
    private String       account;

    /**
     * 主机名称 集合
     */
    private List<String> hosts;
    /**
     * 并发度，默认是0，表示全并发执行，1表示顺序执行，2表示每次执行2台
     */
    private Integer      batch;

    /**
     * 容忍几台机器失败，默认是0，表示一台都不容忍，只要失败了，立马暂停
     */
    private Integer      tolerance;

    /**
     * 暂停点 入参为机器hostname
     */
    private String       pause;

    /**
     * 单机脚本执行的超时时间，单位：秒
     */
    private Integer      timeout;

    /**
     * 附于脚本之后的参数，多个参数之间用双逗号,,分隔，比如arg1,,arg 2,,arg3
     */
    private String       args;
}
