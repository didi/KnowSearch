package com.didi.arius.gateway.elasticsearch.client.response.model.indices;

import com.alibaba.fastjson.annotation.JSONField;

public class Segments {

    @JSONField(name = "count")
    private long count;

    @JSONField(name = "memory_in_bytes")
    private long memoryInBytes;

    @JSONField(name = "terms_memory_in_bytes")
    private long termsMemoryInBytes;

    @JSONField(name = "stored_fields_memory_in_bytes")
    private long storedFieldsMemoryInBytes;

    @JSONField(name = "term_vectors_memory_in_bytes")
    private long termVectorsMemoryInBytes;

    @JSONField(name = "norms_memory_in_bytes")
    private long normsMemoryInBytes;

    @JSONField(name = "doc_values_memory_in_bytes")
    private long docValuesMemoryInBytes;

    @JSONField(name = "index_writer_memory_in_bytes")
    private long indexWriterMemoryInBytes;

    @JSONField(name = "index_writer_max_memory_in_bytes")
    private long indexWriterMaxMemoryInBytes;

    @JSONField(name = "version_map_memory_in_bytes")
    private long versionMapMemoryInBytes;

    @JSONField(name = "fixed_bit_set_memory_in_bytes")
    private long fixedBitSetMemoryInBytes;

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getMemoryInBytes() {
        return memoryInBytes;
    }

    public void setMemoryInBytes(long memoryInBytes) {
        this.memoryInBytes = memoryInBytes;
    }

    public long getTermsMemoryInBytes() {
        return termsMemoryInBytes;
    }

    public void setTermsMemoryInBytes(long termsMemoryInBytes) {
        this.termsMemoryInBytes = termsMemoryInBytes;
    }

    public long getStoredFieldsMemoryInBytes() {
        return storedFieldsMemoryInBytes;
    }

    public void setStoredFieldsMemoryInBytes(long storedFieldsMemoryInBytes) {
        this.storedFieldsMemoryInBytes = storedFieldsMemoryInBytes;
    }

    public long getTermVectorsMemoryInBytes() {
        return termVectorsMemoryInBytes;
    }

    public void setTermVectorsMemoryInBytes(long termVectorsMemoryInBytes) {
        this.termVectorsMemoryInBytes = termVectorsMemoryInBytes;
    }

    public long getNormsMemoryInBytes() {
        return normsMemoryInBytes;
    }

    public void setNormsMemoryInBytes(long normsMemoryInBytes) {
        this.normsMemoryInBytes = normsMemoryInBytes;
    }

    public long getDocValuesMemoryInBytes() {
        return docValuesMemoryInBytes;
    }

    public void setDocValuesMemoryInBytes(long docValuesMemoryInBytes) {
        this.docValuesMemoryInBytes = docValuesMemoryInBytes;
    }

    public long getIndexWriterMemoryInBytes() {
        return indexWriterMemoryInBytes;
    }

    public void setIndexWriterMemoryInBytes(long indexWriterMemoryInBytes) {
        this.indexWriterMemoryInBytes = indexWriterMemoryInBytes;
    }

    public long getIndexWriterMaxMemoryInBytes() {
        return indexWriterMaxMemoryInBytes;
    }

    public void setIndexWriterMaxMemoryInBytes(long indexWriterMaxMemoryInBytes) {
        this.indexWriterMaxMemoryInBytes = indexWriterMaxMemoryInBytes;
    }

    public long getVersionMapMemoryInBytes() {
        return versionMapMemoryInBytes;
    }

    public void setVersionMapMemoryInBytes(long versionMapMemoryInBytes) {
        this.versionMapMemoryInBytes = versionMapMemoryInBytes;
    }

    public long getFixedBitSetMemoryInBytes() {
        return fixedBitSetMemoryInBytes;
    }

    public void setFixedBitSetMemoryInBytes(long fixedBitSetMemoryInBytes) {
        this.fixedBitSetMemoryInBytes = fixedBitSetMemoryInBytes;
    }
}
