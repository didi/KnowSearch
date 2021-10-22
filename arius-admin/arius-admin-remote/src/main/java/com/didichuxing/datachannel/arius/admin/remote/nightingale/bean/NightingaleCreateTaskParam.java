package com.didichuxing.datachannel.arius.admin.remote.nightingale.bean;

import lombok.Data;

import java.util.List;

@Data
public class NightingaleCreateTaskParam {
    /**
     * 标题
     */
    private String       title;

    /**
     * 并发度, 0表示全并发
     */
    private Integer      batch;

    /**
     * 容忍度, 0表示不允许任何一台处于失败的状态
     */
    private Integer      tolerance;

    /**
     * 超时时间(秒)
     */
    private Integer      timeout;

    /**
     * 暂停的主机
     */
    private String       pause;

    /**
     * 脚本
     */
    private String       script;

    /**
     * 参数
     */
    private String       args;

    /**
     * 账号
     */
    private String       account;

    /**
     * start|pause
     */
    private String       action;

    /**
     * 升级主机列表
     */
    private List<String> hosts;

    public NightingaleCreateTaskParam(List<String> hosts, String args) {
        this.args = args;
        this.hosts = hosts;
    }
}