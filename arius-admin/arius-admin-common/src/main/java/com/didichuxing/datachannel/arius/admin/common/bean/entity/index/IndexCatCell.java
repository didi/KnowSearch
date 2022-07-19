package com.didichuxing.datachannel.arius.admin.common.bean.entity.index;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lyn
 * @date 2021/09/30
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndexCatCell {
    private String       cluster;
    private String       clusterLogic;
    private Long         resourceId;
    private Integer      projectId;
    private String       health;
    private String       status;
    private String       index;
    private String       pri;
    private String       rep;
    private String       docsCount;
    private String       docsDeleted;
    private String       storeSize;
    private String       priStoreSize;
    private Boolean      readFlag;
    private Boolean      writeFlag;
    private Boolean      deleteFlag;
    private Long         timestamp;

    private Long         primariesSegmentCount;
    private Long         totalSegmentCount;
    private Integer      templateId;
    private List<String> aliases;

    public String getKey() {
        return cluster + "@" + index;
    }
}