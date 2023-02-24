package com.didi.cloud.fastdump.common.bean.adapter;

import java.io.Serializable;

import com.alibaba.fastjson.JSONObject;

import lombok.Data;

/**
 * Created by linyunan on 2022/8/30
 */
@Data
public class FastDumpDocument implements Serializable {
    /**
     * 文档所在索引
     */
    private String     index;
    private String     id;
    private String     type;
    private String     routing;
    private String     seqNo;
    private Long       version;
    /**
     * 原文
     */
    private JSONObject source;
}
