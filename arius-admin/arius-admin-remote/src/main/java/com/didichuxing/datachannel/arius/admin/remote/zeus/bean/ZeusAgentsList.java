package com.didichuxing.datachannel.arius.admin.remote.zeus.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZeusAgentsList {
    private List<ZeusDat> dat;

    private Integer total;
}
