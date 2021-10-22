package com.didi.arius.gateway.elasticsearch.client.response.batch;

import com.alibaba.fastjson.annotation.JSONField;
import com.didi.arius.gateway.elasticsearch.client.gateway.search.response.Shards;

public class IndexResultNode {
    @JSONField(name = "_index")
    private String index;

    @JSONField(name = "_type")
    private String type;

    @JSONField(name = "_id")
    private String id;

    @JSONField(name = "_version")
    private Long version;

    @JSONField(name = "result")
    private String result;

    @JSONField(name = "_shards")
    private Shards shards;

    @JSONField(name = "_seq_no")
    private Long seqNo;

    @JSONField(name = "_primary_term")
    private Long primaryTerm;

    @JSONField(name = "status")
    private Long status;

    @JSONField(name = "error")
    private Error error;

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Shards getShards() {
        return shards;
    }

    public void setShards(Shards shards) {
        this.shards = shards;
    }

    public Long getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(Long seqNo) {
        this.seqNo = seqNo;
    }

    public Long getPrimaryTerm() {
        return primaryTerm;
    }

    public void setPrimaryTerm(Long primaryTerm) {
        this.primaryTerm = primaryTerm;
    }

    public Long getStatus() {
        return status;
    }

    public void setStatus(Long status) {
        this.status = status;
    }

    /**
     * The failure message, <tt>null</tt> if it did not fail.
     */
    public String getFailureMessage() {
        if (error != null) {
            return error.getReason();
        }
        return null;
    }


    public Error getError() {
        return error;
    }

    public IndexResultNode setError(Error error) {
        this.error = error;
        return this;
    }
}
