package com.didichuxing.arius.admin.extend.fastindex.bean.po;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BaseESPO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class FastIndexOpIndexPO extends BaseESPO {
    private String srcTag;

    private String clusterName;
    private String templateName;
    private String indexName;

    private boolean isOpen;
    protected long openTime;

    private boolean isFinish;
    private long finishTime;

    private String exceptionStr;

    @Override
    public String getKey() {
        if(srcTag==null || srcTag.trim().length()==0) {
            return clusterName + "_" + indexName;
        } else {
            return srcTag.trim() + "_" + clusterName + "_" + indexName;
        }
    }
}
