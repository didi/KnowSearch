/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.core.indexing;

import org.elasticsearch.Version;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.common.xcontent.ToXContentObject;

import java.io.IOException;
import java.util.Objects;

/**
 * This class holds the runtime statistics of a job.  The stats are not used by any internal process
 * and are only for external monitoring/reference.  Statistics are not persisted with the job, so if the
 * allocated task is shutdown/restarted on a different node all the stats will reset.
 */
public abstract class IndexerJobStats implements ToXContentObject, Writeable {

    public static final ParseField NAME = new ParseField("job_stats");

    protected long numPages = 0;
    protected long numInputDocuments = 0;
    protected long numOuputDocuments = 0;
    protected long numInvocations = 0;
    protected long indexTime = 0;
    protected long searchTime = 0;
    protected long indexTotal = 0;
    protected long searchTotal = 0;
    protected long indexFailures = 0;
    protected long searchFailures = 0;

    private long startIndexTime;
    private long startSearchTime;

    public IndexerJobStats() {
    }

    public IndexerJobStats(long numPages, long numInputDocuments, long numOuputDocuments, long numInvocations,
                           long indexTime, long searchTime, long indexTotal, long searchTotal,
                           long indexFailures, long searchFailures) {
        this.numPages = numPages;
        this.numInputDocuments = numInputDocuments;
        this.numOuputDocuments = numOuputDocuments;
        this.numInvocations = numInvocations;
        this.indexTime = indexTime;
        this.searchTime = searchTime;
        this.indexTotal = indexTotal;
        this.searchTotal = searchTotal;
        this.indexFailures = indexFailures;
        this.searchFailures = searchFailures;
    }

    public IndexerJobStats(StreamInput in) throws IOException {
        this.numPages = in.readVLong();
        this.numInputDocuments = in.readVLong();
        this.numOuputDocuments = in.readVLong();
        this.numInvocations = in.readVLong();
        if (in.getVersion().onOrAfter(Version.V_6_6_0)) {
            this.indexTime = in.readVLong();
            this.searchTime = in.readVLong();
            this.indexTotal = in.readVLong();
            this.searchTotal = in.readVLong();
            this.indexFailures = in.readVLong();
            this.searchFailures = in.readVLong();
        }
    }

    public long getNumPages() {
        return numPages;
    }

    public long getNumDocuments() {
        return numInputDocuments;
    }

    public long getNumInvocations() {
        return numInvocations;
    }

    public long getOutputDocuments() {
        return numOuputDocuments;
    }

    public long getIndexFailures() {
        return indexFailures;
    }

    public long getSearchFailures() {
        return searchFailures;
    }

    public long getIndexTime() {
        return indexTime;
    }

    public long getSearchTime() {
        return searchTime;
    }

    public long getIndexTotal() {
        return indexTotal;
    }

    public long getSearchTotal() {
        return searchTotal;
    }

    public void incrementNumPages(long n) {
        assert(n >= 0);
        numPages += n;
    }

    public void incrementNumDocuments(long n) {
        assert(n >= 0);
        numInputDocuments += n;
    }

    public void incrementNumInvocations(long n) {
        assert(n >= 0);
        numInvocations += n;
    }

    public void incrementNumOutputDocuments(long n) {
        assert(n >= 0);
        numOuputDocuments += n;
    }

    public void incrementIndexingFailures() {
        this.indexFailures += 1;
    }

    public void incrementSearchFailures() {
        this.searchFailures += 1;
    }

    public void markStartIndexing() {
        this.startIndexTime = System.nanoTime();
    }

    public void markEndIndexing() {
        indexTime += ((System.nanoTime() - startIndexTime) / 1000000);
        indexTotal += 1;
    }

    public void markStartSearch() {
        this.startSearchTime = System.nanoTime();
    }

    public void markEndSearch() {
        searchTime += ((System.nanoTime() - startSearchTime) / 1000000);
        searchTotal += 1;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeVLong(numPages);
        out.writeVLong(numInputDocuments);
        out.writeVLong(numOuputDocuments);
        out.writeVLong(numInvocations);
        if (out.getVersion().onOrAfter(Version.V_6_6_0)) {
            out.writeVLong(indexTime);
            out.writeVLong(searchTime);
            out.writeVLong(indexTotal);
            out.writeVLong(searchTotal);
            out.writeVLong(indexFailures);
            out.writeVLong(searchFailures);
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        IndexerJobStats that = (IndexerJobStats) other;

        return Objects.equals(this.numPages, that.numPages)
            && Objects.equals(this.numInputDocuments, that.numInputDocuments)
            && Objects.equals(this.numOuputDocuments, that.numOuputDocuments)
            && Objects.equals(this.numInvocations, that.numInvocations)
            && Objects.equals(this.indexTime, that.indexTime)
            && Objects.equals(this.searchTime, that.searchTime)
            && Objects.equals(this.indexFailures, that.indexFailures)
            && Objects.equals(this.searchFailures, that.searchFailures)
            && Objects.equals(this.indexTotal, that.indexTotal)
            && Objects.equals(this.searchTotal, that.searchTotal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numPages, numInputDocuments, numOuputDocuments, numInvocations,
            indexTime, searchTime, indexFailures, searchFailures, indexTotal, searchTotal);
    }
}
