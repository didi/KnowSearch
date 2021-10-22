package com.didichuxing.arius.admin.extend.fastindex.bean.po;

import com.alibaba.fastjson.annotation.JSONField;
import com.didichuxing.datachannel.arius.admin.common.bean.po.BaseESPO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class FastIndexLoadDataPO extends BaseESPO {
    private String clusterName;
    private String templateName;
    private String indexName;
    private String indexUUID;
    private long shardNum;
    private String hostName;
    private int port;

    private String srcTag;
    private String hdfsShards;
    private String hdfsSrcDir;
    private String workDir;
    private String hdfsUser;
    private String hdfsPassword;

    private String esDstDir;
    private String esInnerPrimerKey = "_uid";

    private long zeusTaskId;
    private boolean start;
    private long startTime;


    private boolean isFinish;
    private long finishTime;

    private boolean highES = false;

    // 重试次数
    private long runCount = 0;


    @JSONField(serialize = false)
    public String getOpKey() {
        if (srcTag == null || srcTag.trim().length() == 0) {
            return clusterName + "_" + indexName;
        } else {
            return srcTag.trim() + "_" + clusterName + "_" + indexName;
        }
    }

    @Override
    public String getKey() {
        if (srcTag == null || srcTag.trim().length() == 0) {
            return clusterName + "_" + indexName + "_" + shardNum;
        } else {
            return srcTag.trim() + "_" + clusterName + "_" + indexName + "_" + shardNum;
        }
    }
}
