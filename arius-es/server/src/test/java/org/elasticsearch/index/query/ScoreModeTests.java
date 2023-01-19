/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.index.query;

import org.elasticsearch.common.io.stream.BytesStreamOutput;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery;
import org.elasticsearch.test.ESTestCase;

import static org.hamcrest.Matchers.equalTo;

public class ScoreModeTests extends ESTestCase {

    public void testValidOrdinals() {
        assertThat(FunctionScoreQuery.ScoreMode.FIRST.ordinal(), equalTo(0));
        assertThat(FunctionScoreQuery.ScoreMode.AVG.ordinal(), equalTo(1));
        assertThat(FunctionScoreQuery.ScoreMode.MAX.ordinal(), equalTo(2));
        assertThat(FunctionScoreQuery.ScoreMode.SUM.ordinal(), equalTo(3));
        assertThat(FunctionScoreQuery.ScoreMode.MIN.ordinal(), equalTo(4));
        assertThat(FunctionScoreQuery.ScoreMode.MULTIPLY.ordinal(), equalTo(5));
    }

    public void testWriteTo() throws Exception {
        try (BytesStreamOutput out = new BytesStreamOutput()) {
            FunctionScoreQuery.ScoreMode.FIRST.writeTo(out);
            try (StreamInput in = out.bytes().streamInput()) {
                assertThat(in.readVInt(), equalTo(0));
            }
        }

        try (BytesStreamOutput out = new BytesStreamOutput()) {
            FunctionScoreQuery.ScoreMode.AVG.writeTo(out);
            try (StreamInput in = out.bytes().streamInput()) {
                assertThat(in.readVInt(), equalTo(1));
            }
        }

        try (BytesStreamOutput out = new BytesStreamOutput()) {
            FunctionScoreQuery.ScoreMode.MAX.writeTo(out);
            try (StreamInput in = out.bytes().streamInput()) {
                assertThat(in.readVInt(), equalTo(2));
            }
        }

        try (BytesStreamOutput out = new BytesStreamOutput()) {
            FunctionScoreQuery.ScoreMode.SUM.writeTo(out);
            try (StreamInput in = out.bytes().streamInput()) {
                assertThat(in.readVInt(), equalTo(3));
            }
        }
        try (BytesStreamOutput out = new BytesStreamOutput()) {
            FunctionScoreQuery.ScoreMode.MIN.writeTo(out);
            try (StreamInput in = out.bytes().streamInput()) {
                assertThat(in.readVInt(), equalTo(4));
            }
        }

        try (BytesStreamOutput out = new BytesStreamOutput()) {
            FunctionScoreQuery.ScoreMode.MULTIPLY.writeTo(out);
            try (StreamInput in = out.bytes().streamInput()) {
                assertThat(in.readVInt(), equalTo(5));
            }
        }
    }

    public void testReadFrom() throws Exception {
        try (BytesStreamOutput out = new BytesStreamOutput()) {
            out.writeVInt(0);
            try (StreamInput in = out.bytes().streamInput()) {
                assertThat(FunctionScoreQuery.ScoreMode.readFromStream(in), equalTo(FunctionScoreQuery.ScoreMode.FIRST));
            }
        }
        try (BytesStreamOutput out = new BytesStreamOutput()) {
            out.writeVInt(1);
            try (StreamInput in = out.bytes().streamInput()) {
                assertThat(FunctionScoreQuery.ScoreMode.readFromStream(in), equalTo(FunctionScoreQuery.ScoreMode.AVG));
            }
        }
        try (BytesStreamOutput out = new BytesStreamOutput()) {
            out.writeVInt(2);
            try (StreamInput in = out.bytes().streamInput()) {
                assertThat(FunctionScoreQuery.ScoreMode.readFromStream(in), equalTo(FunctionScoreQuery.ScoreMode.MAX));
            }
        }
        try (BytesStreamOutput out = new BytesStreamOutput()) {
            out.writeVInt(3);
            try (StreamInput in = out.bytes().streamInput()) {
                assertThat(FunctionScoreQuery.ScoreMode.readFromStream(in), equalTo(FunctionScoreQuery.ScoreMode.SUM));
            }
        }
        try (BytesStreamOutput out = new BytesStreamOutput()) {
            out.writeVInt(4);
            try (StreamInput in = out.bytes().streamInput()) {
                assertThat(FunctionScoreQuery.ScoreMode.readFromStream(in), equalTo(FunctionScoreQuery.ScoreMode.MIN));
            }
        }
        try (BytesStreamOutput out = new BytesStreamOutput()) {
            out.writeVInt(5);
            try (StreamInput in = out.bytes().streamInput()) {
                assertThat(FunctionScoreQuery.ScoreMode.readFromStream(in), equalTo(FunctionScoreQuery.ScoreMode.MULTIPLY));
            }
        }
    }

    public void testFromString() {
        assertThat(FunctionScoreQuery.ScoreMode.fromString("first"), equalTo(FunctionScoreQuery.ScoreMode.FIRST));
        assertThat(FunctionScoreQuery.ScoreMode.fromString("avg"), equalTo(FunctionScoreQuery.ScoreMode.AVG));
        assertThat(FunctionScoreQuery.ScoreMode.fromString("max"), equalTo(FunctionScoreQuery.ScoreMode.MAX));
        assertThat(FunctionScoreQuery.ScoreMode.fromString("sum"), equalTo(FunctionScoreQuery.ScoreMode.SUM));
        assertThat(FunctionScoreQuery.ScoreMode.fromString("min"), equalTo(FunctionScoreQuery.ScoreMode.MIN));
        assertThat(FunctionScoreQuery.ScoreMode.fromString("multiply"), equalTo(FunctionScoreQuery.ScoreMode.MULTIPLY));
    }
}
