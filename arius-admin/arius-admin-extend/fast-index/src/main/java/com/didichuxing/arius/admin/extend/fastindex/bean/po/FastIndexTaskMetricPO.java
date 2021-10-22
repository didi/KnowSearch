package com.didichuxing.arius.admin.extend.fastindex.bean.po;

import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.admin.common.bean.po.BaseESPO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class FastIndexTaskMetricPO extends BaseESPO {

    private String srcTag;

    private String clusterName;

    private String indexName;

    private long shardNum;

    private long addTime;

    private JSONObject metrics;

    @Override
    public String getKey() {
        if(srcTag==null || srcTag.trim().length()==0) {
            return clusterName + "_" + indexName + "_" + shardNum;
        } else {
            return srcTag.trim() + "_" + clusterName + "_" + indexName + "_" + shardNum;
        }
    }
}
