/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.sql.expression.predicate.nulls;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.xpack.sql.expression.gen.processor.Processor;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Function;

public class CheckNullProcessor implements Processor {

    public enum CheckNullOperation implements Function<Object, Boolean> {

        IS_NULL(Objects::isNull, "IS NULL"),
        IS_NOT_NULL(Objects::nonNull, "IS NOT NULL");

        private final Function<Object, Boolean> process;
        private final String symbol;

        CheckNullOperation(Function<Object, Boolean> process, String symbol) {
            this.process = process;
            this.symbol = symbol;
        }

        public String symbol() {
            return symbol;
        }

        @Override
        public String toString() {
            return symbol;
        }

        @Override
        public Boolean apply(Object o) {
            return process.apply(o);
        }
    }

    public static final String NAME = "nckn";

    private final CheckNullOperation operation;

    CheckNullProcessor(CheckNullOperation operation) {
        this.operation = operation;
    }

    public CheckNullProcessor(StreamInput in) throws IOException {
        this(in.readEnum(CheckNullOperation.class));
    }

    @Override
    public String getWriteableName() {
        return NAME;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeEnum(operation);
    }

    @Override
    public Object process(Object input) {
        return operation.apply(input);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CheckNullProcessor that = (CheckNullProcessor) o;
        return operation == that.operation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(operation);
    }
}
