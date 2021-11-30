package com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.n9e;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Exp {
    private String optr;
    private String func;
    private String metric;
    private List<Object> params;
    private Integer threshold;
}
