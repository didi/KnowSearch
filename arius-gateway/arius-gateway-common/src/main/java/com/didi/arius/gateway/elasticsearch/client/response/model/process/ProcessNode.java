package com.didi.arius.gateway.elasticsearch.client.response.model.process;

import com.alibaba.fastjson.annotation.JSONField;

public class ProcessNode {
    @JSONField(name = "timestamp")
    private long timestamp;

    @JSONField(name = "open_file_descriptors")
    private long openFileDescriptors;

    @JSONField(name = "max_file_descriptors")
    private long maxFileDescriptors;

    @JSONField(name = "cpu")
    private ProcessCpu cpu;

    @JSONField(name = "mem")
    private ProcessMem mem;

    public ProcessNode() {
        // pass
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getOpenFileDescriptors() {
        return openFileDescriptors;
    }

    public void setOpenFileDescriptors(long openFileDescriptors) {
        this.openFileDescriptors = openFileDescriptors;
    }

    public long getMaxFileDescriptors() {
        return maxFileDescriptors;
    }

    public void setMaxFileDescriptors(long maxFileDescriptors) {
        this.maxFileDescriptors = maxFileDescriptors;
    }

    public ProcessCpu getCpu() {
        return cpu;
    }

    public void setCpu(ProcessCpu cpu) {
        this.cpu = cpu;
    }

    public ProcessMem getMem() {
        return mem;
    }

    public void setMem(ProcessMem mem) {
        this.mem = mem;
    }
}
