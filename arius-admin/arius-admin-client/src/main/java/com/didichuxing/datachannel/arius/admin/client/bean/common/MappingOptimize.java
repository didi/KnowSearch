package com.didichuxing.datachannel.arius.admin.client.bean.common;

import java.util.List;

import com.google.common.collect.Lists;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MappingOptimize {
    private String                    clusterName;

    private String                    templateName;

    private List<MappingOptimizeItem> optimizeItems = Lists.newArrayList();

    public MappingOptimize(String clusterName, String templateName) {
        this.clusterName = clusterName;
        this.templateName = templateName;
    }

    public void addOptimize(MappingOptimizeItem item) {
        optimizeItems.add(item);
    }
}