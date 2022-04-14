package com.didichuxing.datachannel.arius.admin.remote.monitor.odin.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OdinSilence {
    private Integer id;

    /**
     * 屏蔽节点
     */
    private String  ns;

    /**
     * 按照策略屏蔽
     */
    private String  type;

    /**
     * 屏蔽的策略ID
     */
    private String  sids;

    /**
     * 屏蔽开始时间
     */
    private Long    begin_ts;

    /**
     * 屏蔽结束时间
     */
    private Long    end_ts;

    /**
     * 告警组
     */
    private String  notify_group;

    /**
     * 备注
     */
    private String  note;

    private String  created;

    private String  updated;

    private String  effective;
}