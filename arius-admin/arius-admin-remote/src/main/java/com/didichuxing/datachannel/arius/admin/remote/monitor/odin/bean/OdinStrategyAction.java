package com.didichuxing.datachannel.arius.admin.remote.monitor.odin.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OdinStrategyAction {
    private String  type          = "notify";

    private String  notify_group;

    private String  converge;

    private Integer send_recovery = 1;

    private String  callback;
}