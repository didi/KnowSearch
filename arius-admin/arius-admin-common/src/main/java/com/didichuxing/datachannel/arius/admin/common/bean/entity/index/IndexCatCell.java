package com.didichuxing.datachannel.arius.admin.common.bean.entity.index;

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
    private String  key;
    private String  clusterPhy;
    private String  clusterLogic;
    private String  health;
    private String  status;
    private String  index;
    private String  pri;
    private String  rep;
    private String  docsCount;
    private String  docsDeleted;
    private String  storeSize;
    private String  priStoreSize;
    private Boolean readFlag;
    private Boolean writeFlag;
}
