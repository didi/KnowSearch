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
    private Long         pri;
    private Long         rep;
    private Long         docsCount;
    private Long         docsDeleted;
    private String       storeSize;
    private String       priStoreSize;
    private Boolean      readFlag;
    private Boolean      writeFlag;
    private Boolean      deleteFlag;
    private Long         timestamp;

    private Long         primariesSegmentCount;
    private Long         totalSegmentCount;
    private Integer      templateId;
    private Boolean      platformCreateFlag;
    private List<String> aliases;
    private Boolean      translogAsync;
    private Integer      priorityLevel;

    private List<String> indexTypeList;

    public String getKey() {
        return cluster + "@" + index;
    }
}