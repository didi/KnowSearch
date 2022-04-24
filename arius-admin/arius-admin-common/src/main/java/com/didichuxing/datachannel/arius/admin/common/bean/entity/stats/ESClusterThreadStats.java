package com.didichuxing.datachannel.arius.admin.common.bean.entity.stats;

import com.alibaba.fastjson.annotation.JSONField;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ESClusterThreadStats extends BaseEntity {

    private String cluster;
    private Long    management;
    private Long    refresh;
    private Long    flush;
    private Long    merge;
    private Long    search;
    private Long    write;

}
