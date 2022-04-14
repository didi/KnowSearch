package com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ESDDCloudClusterInfo {
    private String                 apiVersion;

    private List<ESAppClusterInfo> items;
}
