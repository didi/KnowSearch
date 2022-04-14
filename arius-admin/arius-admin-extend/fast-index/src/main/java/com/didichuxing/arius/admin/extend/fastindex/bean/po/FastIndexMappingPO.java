package com.didichuxing.arius.admin.extend.fastindex.bean.po;

import com.alibaba.fastjson.annotation.JSONField;
import com.didichuxing.datachannel.arius.admin.common.bean.po.BaseESPO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.UnsupportedEncodingException;
import java.util.Base64;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class FastIndexMappingPO extends BaseESPO {

    private String srcTag;

    private String clusterName;

    private String indexName;

    private long shardNum;

    private String mappingMd5;

    private long addTime;


    @JSONField(serialize = false)
    public void setMapping(String mapping) throws UnsupportedEncodingException {
        this.mappingMd5 = Base64.getEncoder().encodeToString(mapping.toString().getBytes("UTF-8"));;
    }

    @JSONField(serialize = false)
    public String getMapping() throws UnsupportedEncodingException {
        return new String(Base64.getDecoder().decode(mappingMd5), "UTF-8");
    }

    @Override
    public String getKey() {
        if(srcTag==null || srcTag.trim().length()==0) {
            return clusterName + "_" + indexName + "_" + shardNum;
        } else {
            return srcTag.trim() + "_" + clusterName + "_" + indexName + "_" + shardNum;
        }
    }
}
